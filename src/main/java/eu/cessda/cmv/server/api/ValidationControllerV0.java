/*-
 * #%L
 * CESSDA Metadata Validator
 * %%
 * Copyright (C) 2020 - 2025 CESSDA ERIC
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package eu.cessda.cmv.server.api;

import eu.cessda.cmv.core.*;
import eu.cessda.cmv.core.mediatype.validationreport.ValidationReport;
import eu.cessda.cmv.core.mediatype.validationrequest.ValidationRequest;
import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.io.input.CloseShieldInputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.system.ApplicationTemp;
import org.springframework.web.bind.annotation.*;
import org.xml.sax.InputSource;
import org.zalando.problem.Problem;

import javax.validation.Valid;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.channels.Channels;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static io.swagger.v3.oas.annotations.enums.ParameterIn.QUERY;
import static java.nio.file.StandardOpenOption.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_XML_VALUE;

@RestController
@RequestMapping( ValidationControllerV0.BASE_PATH )
@Tag( name = SwaggerConfiguration.TAG_VALIDATIONS )
public class ValidationControllerV0
{
	private static final String LIST_RECORD_NOT_SUPPORTED = "OAI-PMH ListRecord responses are not supported";

	public static final String BASE_PATH = "/api/V0";

	private final ApplicationTemp applicationTemp;
	private final CessdaMetadataValidatorFactory validatorFactory;

	@Autowired
	public ValidationControllerV0( ApplicationTemp applicationTemp, CessdaMetadataValidatorFactory validatorFactory )
	{
		this.applicationTemp = applicationTemp;
		this.validatorFactory = validatorFactory;
	}

	@PostMapping(
			path = "/Validation",
			consumes = { APPLICATION_JSON_VALUE, APPLICATION_XML_VALUE },
			produces = { APPLICATION_JSON_VALUE, APPLICATION_XML_VALUE } )
	@Operation(
			operationId = "validate",
			description = """
Perform a validation of a given DDI document against a specified profile.

This API allows constraints to be specified in two forms, either using the preset validation gates or by specifying the constraints manually.

The API will return an object with the URL of the document that was validated, as well as a list of constraint violations found in the document.
The constraint violations will include a line and column number of the location of the violation, if applicable.

## Useful links

* [Documentation on the definition of the constraints, and what constraints are part of each validation gate](/documentation/constraints.html).
* [CESSDA profiles for the Data Catalogue and European Question Bank](/documentation/profiles.html).

## *Deprecation notice*

Using query parameters to call the API is deprecated and doesn't allow specifying individual constraints, only validation gates. This method of calling the API will be removed in a future API version.""",
			parameters = {
					@Parameter( in = QUERY, name = "documentUri", deprecated = true ),
					@Parameter( in = QUERY, name = "profileUri", deprecated = true ),
					@Parameter( in = QUERY, name = "validationGateName", deprecated = true )
			},
			responses = {
					@ApiResponse( responseCode = "200" ),
					@ApiResponse( responseCode = "400", content = @Content( schema = @Schema( implementation = Problem.class ) ) )
			} )
	public ValidationReport validate(
			@RequestParam( required = false ) URI documentUri,
			@RequestParam( required = false ) URI profileUri,
			@RequestParam( required = false ) ValidationGateName validationGateName,
			@RequestBody( required = false ) @Valid ValidationRequest validationRequest ) throws IOException, NotDocumentException
	{
		boolean hasRequestBody = validationRequest != null;

		if ( documentUri != null && profileUri != null && validationGateName != null && !hasRequestBody )
		{
			// All request params specified and request body not present
			return fromRequestParams( documentUri, profileUri, validationGateName );
		}
		else if ( !(documentUri != null || profileUri != null || validationGateName != null) && hasRequestBody )
		{
			// Validate the request
			var requestValidationResult = validationRequest.validate();
			if ( requestValidationResult.isEmpty() )
			{
				if ( validationRequest.getValidationGateName() != null )
				{
					return validateUsingGate( validationRequest );
				}
				else
				{
					return validateUsingConstraints( validationRequest );
				}
			}
			else
			{
				throw new IllegalArgumentException( "Invalid request: " + requestValidationResult );
			}
		}
		else
		{
			throw new IllegalArgumentException( "Invalid request: Use either the query parameters or the request body!" );
		}
	}

	private ValidationReport fromRequestParams( URI documentUri, URI profileUri, ValidationGateName validationGateName ) throws IOException, NotDocumentException
	{
		Path tempDocumentFile = Files.createTempFile( applicationTemp.getDir().toPath(), null, null );
		try( var tempDocumentChannel = Files.newByteChannel( tempDocumentFile, READ, WRITE, DELETE_ON_CLOSE) )
		{
			// Download the document
			try ( var docInputStream = documentUri.toURL().openStream() )
			{
				docInputStream.transferTo( Channels.newOutputStream( tempDocumentChannel ) );
			}

			// Seek to the start of the file
			tempDocumentChannel.position(0);

			// Check if this is a ListRecords response
			var inputSource = createSourceFromStream( documentUri.toString(), Channels.newInputStream( tempDocumentChannel ) );
			if (isListRecordsResponse( inputSource ))
			{
				throw new IllegalArgumentException( LIST_RECORD_NOT_SUPPORTED );
			}


			// Seek to the start of the file
			tempDocumentChannel.position(0);

			// Create the input source that will be used by the XML parser
			inputSource = createSourceFromStream( documentUri.toString(), Channels.newInputStream( tempDocumentChannel ) );

			Document document = validatorFactory.newDocument( inputSource );
			Profile profile = validatorFactory.newProfile( profileUri );

			return validatorFactory.validate( document, profile, validationGateName );
		}
		finally
		{
			// Best effort deletion of the temporary file
			Files.deleteIfExists(tempDocumentFile);
		}
	}

	private boolean isListRecordsResponse( InputSource inputSource ) throws IOException, NotDocumentException
	{
		try
		{
			// Validate that this is not a ListRecords response
			validatorFactory.splitListRecordsResponse( inputSource );
			return true;
		}
		catch ( NotDocumentException e )
		{
			if ( e.getCause() != null )
			{
				throw e;
			}
			return false;
		}
	}

	private static InputSource createSourceFromStream( String documentUri, InputStream inputStream )
	{
		var inputSource = new InputSource();

		// Set the system ID of the document source
		inputSource.setSystemId( documentUri );

		// Shield the input stream from being closed by the XML parser
		var shieldedInputStream = CloseShieldInputStream.wrap( inputStream );
		inputSource.setByteStream( shieldedInputStream );

		return inputSource;
	}

	@GetMapping(path = "/Constraints", produces = { APPLICATION_JSON_VALUE, APPLICATION_XML_VALUE })
	@Operation(
			description = "Get the list of supported constraints, use with /Validation",
			externalDocs = @ExternalDocumentation( description = "Constraint documentation", url = "/documentation/constraints.html" ) )
	public Set<String> constraints()
	{
		return CessdaMetadataValidatorFactory.getConstraints();
	}

	private ValidationReport validateUsingGate( ValidationRequest validationRequest ) throws IOException, NotDocumentException
	{
		// Check if this is a ListRecords response
		var source = validationRequest.getDocument().toInputSource();
		if (isListRecordsResponse( source ))
		{
			throw new IllegalArgumentException( LIST_RECORD_NOT_SUPPORTED );
		}

		var document = validatorFactory.newDocument( validationRequest.getDocument() );
		var profile = validatorFactory.newProfile( validationRequest.getProfile() );
		return validatorFactory.validate(
				document,
				profile,
				validationRequest.getValidationGateName()
		);
	}

	private ValidationReport validateUsingConstraints( ValidationRequest validationRequest ) throws IOException, NotDocumentException
	{
		try
		{
			// Check if this is a ListRecords response
			var source = validationRequest.getDocument().toInputSource();
			if (isListRecordsResponse( source ))
			{
				throw new IllegalArgumentException( LIST_RECORD_NOT_SUPPORTED );
			}

			var document = validatorFactory.newDocument( validationRequest.getDocument() );
			var profile = validatorFactory.newProfile( validationRequest.getProfile() );
			var validationGate = CessdaMetadataValidatorFactory.newValidationGate( validationRequest.getConstraints() );
			return validatorFactory.validate(
					document,
					profile,
					validationGate
			);
		}
		catch ( InvalidGateException e )
		{
			// Extract the names of the constraints that couldn't be found
			var stringBuilder = new StringBuilder();
			var innerExceptions = e.getSuppressed();
			for ( int i = 0; i < innerExceptions.length; i++ )
			{

				if (i > 0)
				{
					stringBuilder.append( ", " );
				}

				stringBuilder.append( ( (InvalidConstraintException) innerExceptions[i] ).getConstraintName() );
			}

			if ( innerExceptions.length == 1 )
			{
				throw new IllegalArgumentException( stringBuilder + " is not a valid constraint", e );
			}
			else
			{
				throw new IllegalArgumentException( stringBuilder + " are not valid constraints", e );
			}
		}
	}
}
