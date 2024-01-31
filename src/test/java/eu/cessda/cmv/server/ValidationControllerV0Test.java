/*-
 * #%L
 * CESSDA Metadata Validator
 * %%
 * Copyright (C) 2020 - 2024 CESSDA ERIC
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

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.cessda.cmv.core.ValidationGateName;
import eu.cessda.cmv.core.mediatype.validationrequest.ValidationRequest;
import eu.cessda.cmv.server.api.ValidationControllerV0;
import org.gesis.commons.resource.SpringUriBuilder;
import org.gesis.commons.resource.TextResource;
import org.gesis.commons.resource.UriBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.zalando.problem.Problem;

import java.net.URI;
import java.util.Collections;

import static org.gesis.commons.resource.Resource.newResource;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest( webEnvironment = RANDOM_PORT )
class ValidationControllerV0Test
{
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	/**
	 * Test profile to validate against.
	 */
	private static final String PROFILE_URI = "https://raw.githubusercontent.com/cessda/cessda.cmv.core/8d0ea9d6a731fa06bde8c8f2b231c2e974aa7130/src/main/resources/demo-documents/ddi-v25/cdc25_profile.xml";
	/** Test document to validate against. */
	private static final String DOCUMENT_URI = "https://raw.githubusercontent.com/cessda/cessda.cmv.core/8d0ea9d6a731fa06bde8c8f2b231c2e974aa7130/src/main/resources/demo-documents/ddi-v25/ukds-2000.xml";

	@Test
	void validateWithBasicValidationGate() throws Exception {
		String responseBody;
		eu.cessda.cmv.core.mediatype.validationreport.ValidationReport validationReport;
		UriBuilder.V10 uriBuilder = new SpringUriBuilder("")
				.path(ValidationControllerV0.BASE_PATH)
				.path("/Validation")
				.queryParameter("documentUri", DOCUMENT_URI)
				.queryParameter("profileUri", PROFILE_URI)
				.queryParameter("validationGateName", ValidationGateName.BASIC.toString());

		// XML
		responseBody = mockMvc.perform( post( uriBuilder.toEncodedString() )
				.accept( MediaType.APPLICATION_XML ) )
				.andExpect( status().isOk() )
				.andReturn().getResponse().getContentAsString();
		validationReport = eu.cessda.cmv.core.mediatype.validationreport.ValidationReport.read( responseBody );
		assertThat( validationReport.getConstraintViolations(), hasSize( 7 ) );

		// JSON
		responseBody = mockMvc.perform( post( uriBuilder.toEncodedString() )
				.accept( MediaType.APPLICATION_JSON ) )
				.andExpect( status().isOk() )
				.andReturn().getResponse().getContentAsString();
		validationReport = objectMapper.readValue( responseBody, eu.cessda.cmv.core.mediatype.validationreport.ValidationReport.class );
		assertThat( validationReport.getConstraintViolations(), hasSize(7));
	}

	@Test
	void validateWithStandardValidationGate() throws Exception {
		String responseBody;
		eu.cessda.cmv.core.mediatype.validationreport.ValidationReport validationReport;
		UriBuilder.V10 uriBuilder = new SpringUriBuilder("")
				.path(ValidationControllerV0.BASE_PATH)
				.path("/Validation")
				.queryParameter("documentUri", DOCUMENT_URI)
				.queryParameter("profileUri", PROFILE_URI)
				.queryParameter("validationGateName", ValidationGateName.STANDARD.toString());

		// XML
		responseBody = mockMvc.perform( post( uriBuilder.toEncodedString() )
				.accept( MediaType.APPLICATION_XML ) )
				.andExpect( status().isOk() )
				.andReturn().getResponse().getContentAsString();
		validationReport = eu.cessda.cmv.core.mediatype.validationreport.ValidationReport.read( responseBody );
		assertThat( validationReport.getConstraintViolations(), hasSize( 19 ) );
		assertThat( validationReport.getDocumentUri().toString(), equalTo( DOCUMENT_URI ) );

		// JSON
		responseBody = mockMvc.perform( post( uriBuilder.toEncodedString() )
				.accept( MediaType.APPLICATION_JSON ) )
				.andExpect( status().isOk() )
				.andReturn().getResponse().getContentAsString();
		validationReport = objectMapper.readValue( responseBody, eu.cessda.cmv.core.mediatype.validationreport.ValidationReport.class );
		assertThat( validationReport.getConstraintViolations(), hasSize( 19 ) );
		assertThat( validationReport.getDocumentUri().toString(), equalTo( DOCUMENT_URI));
	}

	@Test
	void validateWithStandardValidationGateByValidationRequestV0() throws Exception {
		String responseBody;
		MediaType mediaType;
		eu.cessda.cmv.core.mediatype.validationreport.ValidationReport validationReport;
		UriBuilder.V10 uriBuilder = new SpringUriBuilder("")
				.path(ValidationControllerV0.BASE_PATH)
				.path("/Validation");
		var validationRequest = new ValidationRequest();
		validationRequest.setDocument(URI.create(DOCUMENT_URI));
		validationRequest.setProfile(new TextResource(newResource(PROFILE_URI)).toString());
		validationRequest.setValidationGateName(ValidationGateName.STANDARD );

		mediaType = MediaType.APPLICATION_JSON;
		responseBody = mockMvc.perform( post( uriBuilder.toEncodedString() )
						.accept( mediaType )
						.contentType( mediaType )
						.content( objectMapper.writeValueAsString( validationRequest ) ) )
				.andExpect( status().isOk() )
				.andReturn().getResponse().getContentAsString();
		validationReport = objectMapper.readValue( responseBody, eu.cessda.cmv.core.mediatype.validationreport.ValidationReport.class );
		assertThat( validationReport.getConstraintViolations(), hasSize( 19 ) );
		assertThat( validationReport.getDocumentUri().toString(), equalTo( DOCUMENT_URI ) );

		mediaType = MediaType.APPLICATION_XML;
		responseBody = mockMvc.perform( post( uriBuilder.toEncodedString() )
				.accept( mediaType )
				.contentType( mediaType )
				.content( validationRequest.toString() ) )
				.andExpect( status().isOk() )
				.andReturn().getResponse().getContentAsString();
		validationReport = eu.cessda.cmv.core.mediatype.validationreport.ValidationReport.read( responseBody );
		assertThat( validationReport.getConstraintViolations(), hasSize( 19 ) );
		assertThat( validationReport.getDocumentUri().toString(), equalTo( DOCUMENT_URI));
	}

	@Test
	void validateWithCustomValidationGateByValidationRequestV0() throws Exception {
		String responseBody;
		MediaType mediaType;
		eu.cessda.cmv.core.mediatype.validationreport.ValidationReport validationReport;
		UriBuilder.V10 uriBuilder = new SpringUriBuilder("")
				.path(ValidationControllerV0.BASE_PATH)
				.path("/Validation");

		var requestJSON = this.getClass().getResource( "/request.json" );
		var validationRequest = objectMapper.readValue( requestJSON, ValidationRequest.class );

		mediaType = MediaType.APPLICATION_JSON;
		responseBody = mockMvc.perform( post( uriBuilder.toEncodedString() )
						.accept( mediaType )
						.contentType( mediaType )
						.content( objectMapper.writeValueAsString( validationRequest ) ) )
				.andExpect( status().isOk() )
				.andReturn().getResponse().getContentAsString();
		validationReport = objectMapper.readValue( responseBody, eu.cessda.cmv.core.mediatype.validationreport.ValidationReport.class );
		assertThat( validationReport.getConstraintViolations(), hasSize( 22 ) );
		assertThat( validationReport.getDocumentUri().toString(), startsWith( "urn:uuid:" ) );
	}

	@Test
	void invalidRequest() throws Exception {
		// Use either the query parameters or the request body!
		UriBuilder.V10 uriBuilder = new SpringUriBuilder("")
				.path(ValidationControllerV0.BASE_PATH)
				.path("/Validation")
				.queryParameter("validationGateName", ValidationGateName.STANDARD.toString());
		var validationRequest = new ValidationRequest();
		validationRequest.setDocument(URI.create(DOCUMENT_URI));
		validationRequest.setProfile(new TextResource(newResource(PROFILE_URI)).toString());
		validationRequest.setValidationGateName(ValidationGateName.STANDARD);
		MediaType mediaType = MediaType.APPLICATION_JSON;
		String responseBody = mockMvc.perform( post( uriBuilder.toEncodedString() )
						.accept( mediaType ).contentType( mediaType )
						.content( objectMapper.writeValueAsString( validationRequest ) ) )
				.andExpect( status().is( 400 ) )
				.andReturn().getResponse().getContentAsString();
		Problem problem = objectMapper.readValue( responseBody, Problem.class );
		assertThat( problem.getDetail(), containsString( "Invalid request"));
	}

	@Test
	void invalidValidationRequestObject() throws Exception {
		UriBuilder.V10 uriBuilder = new SpringUriBuilder("")
				.path(ValidationControllerV0.BASE_PATH)
				.path("/Validation");
		var validationRequest = new ValidationRequest();
		validationRequest.setDocument( (String) null ); // violates @NotNull
		validationRequest.setProfile(new TextResource(newResource(PROFILE_URI)).toString());
		validationRequest.setValidationGateName(ValidationGateName.STANDARD);
		MediaType mediaType = MediaType.APPLICATION_JSON;
		String responseBody = mockMvc.perform(post(uriBuilder.toEncodedString() )
				.accept( mediaType ).contentType( mediaType )
				.content( objectMapper.writeValueAsString( validationRequest ) ) )
				.andExpect( status().is( 400 ) )
				.andReturn().getResponse().getContentAsString();
		Problem problem = objectMapper.readValue( responseBody, Problem.class );
		assertThat( problem.getTitle(), containsString( "Constraint Violation" ) );
		assertThat( responseBody, containsString( "must not be null" ) );
	}

	@Test
	void invalidValidationRequestObjectWithoutGateOrConstraints() throws Exception {
		UriBuilder.V10 uriBuilder = new SpringUriBuilder("")
				.path(ValidationControllerV0.BASE_PATH)
				.path("/Validation");
		var validationRequest = new ValidationRequest();
		validationRequest.setDocument(URI.create(DOCUMENT_URI));
		validationRequest.setProfile(new TextResource(newResource(PROFILE_URI)).toString());
		MediaType mediaType = MediaType.APPLICATION_JSON;
		String responseBody = mockMvc.perform(post(uriBuilder.toEncodedString() )
						.accept( mediaType ).contentType( mediaType )
						.content( objectMapper.writeValueAsString( validationRequest ) ) )
				.andExpect( status().is( 400 ) )
				.andReturn().getResponse().getContentAsString();
		Problem problem = objectMapper.readValue( responseBody, Problem.class );
		assertThat( problem.getTitle(), containsString( "Bad Request" ) );
		assertThat( responseBody, containsString( "Validation gate or constraints is missing" ) );
	}

	@Test
	void invalidValidationRequestObjectWithInvalidConstraints() throws Exception {
		UriBuilder.V10 uriBuilder = new SpringUriBuilder("")
				.path(ValidationControllerV0.BASE_PATH)
				.path("/Validation");

		var requestJSON = this.getClass().getResource( "/invalidRequest.json" );
		var validationRequest = objectMapper.readValue( requestJSON, ValidationRequest.class );

		MediaType mediaType = MediaType.APPLICATION_JSON;
		String responseBody = mockMvc.perform(post(uriBuilder.toEncodedString() )
						.accept( mediaType ).contentType( mediaType )
						.content( objectMapper.writeValueAsString( validationRequest ) ) )
				.andExpect( status().is( 400 ) )
				.andReturn().getResponse().getContentAsString();
		Problem problem = objectMapper.readValue( responseBody, Problem.class );
		assertThat( problem.getTitle(), containsString( "Bad Request" ) );
		assertThat( responseBody, containsString( "are not valid constraints" ) );
	}

	@Test
	void invalidValidationRequestObjectWithInvalidConstraint() throws Exception {
		UriBuilder.V10 uriBuilder = new SpringUriBuilder("")
				.path(ValidationControllerV0.BASE_PATH)
				.path("/Validation");

		var requestJSON = this.getClass().getResource( "/invalidRequest.json" );
		var validationRequest = objectMapper.readValue( requestJSON, ValidationRequest.class );

		validationRequest.setConstraints( Collections.singleton( validationRequest.getConstraints().get( 0 ) ) );

		MediaType mediaType = MediaType.APPLICATION_JSON;
		String responseBody = mockMvc.perform(post(uriBuilder.toEncodedString() )
						.accept( mediaType ).contentType( mediaType )
						.content( objectMapper.writeValueAsString( validationRequest ) ) )
				.andExpect( status().is( 400 ) )
				.andReturn().getResponse().getContentAsString();
		Problem problem = objectMapper.readValue( responseBody, Problem.class );
		assertThat( problem.getTitle(), containsString( "Bad Request" ) );
		assertThat( responseBody, containsString( "is not a valid constraint" ) );
	}
}
