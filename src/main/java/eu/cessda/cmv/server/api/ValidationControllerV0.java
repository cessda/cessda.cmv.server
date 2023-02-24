/*-
 * #%L
 * CESSDA Metadata Validator
 * %%
 * Copyright (C) 2020 - 2023 CESSDA ERIC
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

import eu.cessda.cmv.core.ValidationGateName;
import eu.cessda.cmv.core.ValidationService;
import eu.cessda.cmv.core.mediatype.validationreport.v0.ValidationReportV0;
import eu.cessda.cmv.core.mediatype.validationrequest.v0.ValidationRequestV0;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.gesis.commons.resource.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;

import static io.swagger.v3.oas.annotations.enums.ParameterIn.QUERY;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_XML_VALUE;

@RestController
@RequestMapping( ValidationControllerV0.BASE_PATH )
@Tag( name = SwaggerConfiguration.TAG_VALIDATIONS )
public class ValidationControllerV0
{
	public static final String BASE_PATH = "/api/V0";

	@Autowired
	private ValidationService.V10 validationService;

	@PostMapping(
			path = "/Validation",
			consumes = { APPLICATION_JSON_VALUE, APPLICATION_XML_VALUE },
			produces = { APPLICATION_JSON_VALUE, APPLICATION_XML_VALUE } )
	@Operation(
			operationId = "validate",
			description = "Use either the query parameters or the request body, query parameters are deprecated",
			parameters = {
					@Parameter( in = QUERY, name = "documentUri", deprecated = true ),
					@Parameter( in = QUERY, name = "profileUri", deprecated = true ),
					@Parameter( in = QUERY, name = "validationGateName", deprecated = true )
			},
			responses = @ApiResponse( responseCode = "200" ) )
	public ValidationReportV0 validate(
			@RequestParam( required = false ) URI documentUri,
			@RequestParam( required = false ) URI profileUri,
			@RequestParam( required = false ) ValidationGateName validationGateName,
			@RequestBody( required = false ) @Valid ValidationRequestV0 validationRequest )
	{
		boolean hasRequestParams = documentUri != null || profileUri != null || validationGateName != null;
		boolean hasRequestBody = validationRequest != null;

		if ( hasRequestParams && !hasRequestBody )
		{
			return validationService.validate( documentUri, profileUri, validationGateName );
		}
		if ( !hasRequestParams && hasRequestBody )
		{
			Resource document = validationRequest.getDocument().toResource();
			Resource profile = validationRequest.getProfile().toResource();
			validationGateName = validationRequest.getValidationGateName();
			return validationService.validate( document, profile, validationGateName );
		}
		else
		{
			throw new IllegalArgumentException( "Invalid request: Use either the query parameters or the request body!" );
		}
	}
}
