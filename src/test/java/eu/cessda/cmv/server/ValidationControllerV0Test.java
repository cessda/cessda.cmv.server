package eu.cessda.cmv.server;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.UnsupportedEncodingException;

import org.gesis.commons.resource.SpringUriBuilder;
import org.gesis.commons.resource.UriBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.cessda.cmv.core.ValidationGateName;
import eu.cessda.cmv.core.mediatype.validationreport.v0.ValidationReportV0;
import eu.cessda.cmv.server.api.ValidationControllerV0;

@AutoConfigureMockMvc
@SpringBootTest( webEnvironment = WebEnvironment.RANDOM_PORT )
class ValidationControllerV0Test
{
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMaper;

	private String profileUri = "https://bitbucket.org/cessda/cessda.cmv.core/raw/ad7e3ffd847ecb9c35faea329fbc7cfe14bfb7a6/src/main/resources/demo-documents/ddi-v25/cdc25_profile.xml";
	private String documentUri = "https://bitbucket.org/cessda/cessda.cmv.core/raw/ad7e3ffd847ecb9c35faea329fbc7cfe14bfb7a6/src/main/resources/demo-documents/ddi-v25/ukds-2000.xml";

	@Test
	void validateWithBasicValidationGate() throws Exception
	{
		String responseBody;
		ValidationReportV0 validationReport;
		UriBuilder.V10 uriBuilder = new SpringUriBuilder( "" )
				.path( ValidationControllerV0.BASE_PATH )
				.path( "/Validation" )
				.queryParameter( "documentUri", documentUri )
				.queryParameter( "profileUri", profileUri )
				.queryParameter( "validationGateName", ValidationGateName.BASIC.toString() );

		// XML
		responseBody = mockMvc.perform( post( uriBuilder.toEncodedString() )
				.accept( MediaType.APPLICATION_XML ) )
				.andExpect( status().isOk() )
				.andReturn().getResponse().getContentAsString();
		validationReport = ValidationReportV0.read( responseBody );
		assertThat( validationReport.getConstraintViolations(), hasSize( 9 ) );

		// JSON
		responseBody = mockMvc.perform( post( uriBuilder.toEncodedString() )
				.accept( MediaType.APPLICATION_JSON ) )
				.andExpect( status().isOk() )
				.andReturn().getResponse().getContentAsString();
		validationReport = objectMaper.readValue( responseBody, ValidationReportV0.class );
		assertThat( validationReport.getConstraintViolations(), hasSize( 9 ) );
	}

	@Test
	void validateWithStandardValidationGate() throws UnsupportedEncodingException, Exception
	{
		String responseBody;
		ValidationReportV0 validationReport;
		UriBuilder.V10 uriBuilder = new SpringUriBuilder( "" )
				.path( ValidationControllerV0.BASE_PATH )
				.path( "/Validation" )
				.queryParameter( "documentUri", documentUri )
				.queryParameter( "profileUri", profileUri )
				.queryParameter( "validationGateName", ValidationGateName.STANDARD.toString() );

		// XML
		responseBody = mockMvc.perform( post( uriBuilder.toEncodedString() )
				.accept( MediaType.APPLICATION_XML ) )
				.andExpect( status().isOk() )
				.andReturn().getResponse().getContentAsString();
		validationReport = ValidationReportV0.read( responseBody );
		assertThat( validationReport.getConstraintViolations(), hasSize( 21 ) );
		assertThat( validationReport.getDocumentUri().toString(), equalTo( documentUri ) );

		// JSON
		responseBody = mockMvc.perform( post( uriBuilder.toEncodedString() )
				.accept( MediaType.APPLICATION_JSON ) )
				.andExpect( status().isOk() )
				.andReturn().getResponse().getContentAsString();
		validationReport = objectMaper.readValue( responseBody, ValidationReportV0.class );
		assertThat( validationReport.getConstraintViolations(), hasSize( 21 ) );
		assertThat( validationReport.getDocumentUri().toString(), equalTo( documentUri ) );
	}
}
