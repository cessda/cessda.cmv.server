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
package eu.cessda.cmv.server;

import eu.cessda.cmv.core.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.DescriptiveResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.*;

import static eu.cessda.cmv.server.Server.NOT_LIST_RECORDS_RESPONSE;

@Component
public class ValidatorEngine
{
	private static final Logger log = LoggerFactory.getLogger( ValidatorEngine.class );

	private final ThreadLocal<javax.xml.validation.Validator> xmlValidators;
	private final CessdaMetadataValidatorFactory validatorFactory;

	@Autowired
	public ValidatorEngine( CessdaMetadataValidatorFactory validatorFactory ) throws SAXException
	{
		this.validatorFactory = validatorFactory;

		// Find the resources for the XML schemas
		var schemaURLs = new URL[] {
			this.getClass().getResource("/static/schemas/codebook/codebook.xsd"),
			this.getClass().getResource("/static/schemas/lifecycle/3.2/instance.xsd"),
			this.getClass().getResource("/static/schemas/lifecycle/3.3/instance.xsd"),
			this.getClass().getResource("/static/schemas/nesstar/Version1-2-2.xsd"),
			this.getClass().getResource("/static/schemas/oai-pmh/OAI-PMH.xsd")
		};

		var sources = Arrays.stream(schemaURLs)
			.map(URL::toExternalForm)
			.map(StreamSource::new)
			.toArray(StreamSource[]::new);

		// Construct schema objects from the XML schemas
		var schema = SchemaFactory.newDefaultInstance().newSchema(sources);

		// Create a ThreadLocal to construct an XMLValidator for each thread
		this.xmlValidators = ThreadLocal.withInitial( () ->
		{
			// Create a validator and set its error handler
			var validator = schema.newValidator();
			validator.setErrorHandler( new LoggingErrorHandler() );
			return validator;
		} );
	}

	/**
	 * Validates a given XML document against XML schema and a CMV profile.
	 *
	 * @param documentResource the document to validate.
	 * @param profile the profile to use when validating.
	 * @param validationGate the validation gate to use when validating.
	 * @return a map of {@link ValidationReport}s containing the list of XML validation errors and the CMV validation report.
	 * @throws IOException if an IO error occurred when parsing the document.
	 * @throws SAXException if the XML document was invalid.
	 */
	public Map<Resource, ValidationReport> validate( Resource documentResource, Profile profile, ValidationGateName validationGate ) throws IOException, SAXException, NotDocumentException
	{
		try
		{
			return validateMultiple( documentResource, profile, validationGate );
		}
		catch ( IllegalArgumentException | NotDocumentException e )
		{
			log.debug(NOT_LIST_RECORDS_RESPONSE, documentResource );
		}

		var validationReport = validateSingle( documentResource, profile, validationGate );
		return Map.of( documentResource, validationReport );
	}


	/**
	 * Validates a given XML document against XML schema and a CMV profile.
	 *
	 * @param documentResource the document to validate.
	 * @param profile the profile to use when validating.
	 * @param validationGate the validation gate to use when validating.
	 * @return a {@link ValidationReport} containing the list of XML validation errors and the CMV validation report.
	 * @throws IOException if an IO error occurred when parsing the document.
	 * @throws SAXException if the XML document was invalid.
	 */
	private ValidationReport validateSingle( Resource documentResource, Profile profile, ValidationGateName validationGate ) throws IOException, SAXException, NotDocumentException
	{
		// Validate XML
		var validationErrors = getXMLSchemaViolations( documentResource );

		// Validate against profile
		eu.cessda.cmv.core.mediatype.validationreport.ValidationReport validationReport;
		try (var inputStream = documentResource.getInputStream())
		{
			var document = validatorFactory.newDocument( inputStream );
			validationReport = validatorFactory.validate( document, profile, validationGate );
		}

		return new ValidationReport( validationErrors, validationReport.getConstraintViolations() );
	}

	private List<SchemaViolation> getXMLSchemaViolations( Resource documentResource ) throws IOException, SAXException
	{
		var validator = xmlValidators.get();
		var errorHandler = (LoggingErrorHandler) validator.getErrorHandler();
		List<SAXParseException> errors;
		try (var inputStream = documentResource.getInputStream())
		{
			StreamSource streamSource;
			try
			{
				streamSource = new StreamSource( inputStream, documentResource.getURL().toString() );
			}
			catch ( IOException e )
			{
				streamSource = new StreamSource(inputStream);
			}
			validator.validate( streamSource );

			// Extract errors from the error handler
			errors = errorHandler.getErrors();
		}
		finally
		{
			// Reset the error handler
			errorHandler.reset();
		}

		// Convert SAXParseException instances to SchemaViolation instances
		var validationErrors = new ArrayList<SchemaViolation>(errors.size());
		for ( var error : errors )
		{
			validationErrors.add( new SchemaViolation( error ) );
		}
		return validationErrors;
	}

	/**
	 * Validate all documents in the ListRecords document using the provided profile and validation gate.
	 *
	 * @param documentResource a resource representing a ListRecords document.
	 * @param profile the profile to use when validating.
	 * @param validationGate the validation gate to use when validating.
	 * @return a map with the document URI as a key, and the validation report as the value.
	 * @throws NotDocumentException if the document is not a ListRecords response.
	 * @throws IOException if an IO error occurs.
	 * @throws SAXException if the XML document is invalid.
	 */
	private Map<Resource, ValidationReport> validateMultiple( Resource documentResource, Profile profile, ValidationGate validationGate ) throws IOException, NotDocumentException, SAXException
	{
		var inputSource = new InputSource(documentResource.getInputStream());

		try
		{
			var url = documentResource.getURL();
			inputSource.setSystemId( url.toString() );
		}
		catch ( IOException e )
		{
			inputSource.setSystemId( documentResource.getFilename() );
		}

		// Parse the source as a ListRecords response, this will throw NotDocumentException if
		// the document is not a ListRecords response skipping the schema violations validation
		var documents = validatorFactory.splitListRecordsResponse( inputSource );
		var results = new HashMap<URI, eu.cessda.cmv.core.mediatype.validationreport.ValidationReport>();
		for ( var document : documents )
		{
			var report = validatorFactory.validate( document, profile, validationGate );
			results.put( document.getURI(), report );
		}

		var schemaViolations = getXMLSchemaViolations( documentResource );

		var compiledRes = new HashMap<Resource, ValidationReport>();
		for (var res : results.entrySet()) {
			var validationReport = new ValidationReport( schemaViolations, res.getValue().getConstraintViolations() );
			compiledRes.put( new DescriptiveResource( res.getKey().toString() ), validationReport );
		}

		return compiledRes;
	}

	/**
	 * An error handler that stores all encountered SAX errors and warnings.
	 * <p>
	 * This error handler is stateful and must be reset before validating the next document.
	 *
	 * @implNote Fatal errors are rethrown as is and are not reported by this error handler.
	 */
	private static class LoggingErrorHandler implements ErrorHandler
	{
		private final ArrayList<SAXParseException> errors = new ArrayList<>();

		@Override
		public void warning( SAXParseException exception )
		{
			errors.add( exception );
		}

		@Override
		public void error( SAXParseException exception )
		{
			errors.add( exception );
		}

		@Override
		public void fatalError( SAXParseException exception ) throws SAXParseException
		{
			throw exception;
		}

		/**
		 * Clears the list of {@link SAXParseException}s from the handler.
		 */
		public void reset()
		{
			errors.clear();
		}

		/**
		 * Gets an unmodifiable list of the {@link SAXParseException}s encountered by the error handler.
		 */
		public List<SAXParseException> getErrors()
		{
			return List.copyOf( errors );
		}
	}
}
