package eu.cessda.cmv.server;

import static org.assertj.core.api.Assertions.fail;
import static org.gesis.commons.resource.Resource.newResource;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.gesis.commons.resource.SpringUriBuilder;
import org.gesis.commons.resource.UriBuilder;
import org.gesis.commons.resource.io.DdiInputStream;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
	private static final Logger LOGGER = LoggerFactory.getLogger( ValidationControllerV0Test.class );

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
		assertThat( validationReport.getConstraintViolations(), hasSize( 7 ) );

		// JSON
		responseBody = mockMvc.perform( post( uriBuilder.toEncodedString() )
				.accept( MediaType.APPLICATION_JSON ) )
				.andExpect( status().isOk() )
				.andReturn().getResponse().getContentAsString();
		validationReport = objectMaper.readValue( responseBody, ValidationReportV0.class );
		assertThat( validationReport.getConstraintViolations(), hasSize( 7 ) );
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
		assertThat( validationReport.getConstraintViolations(), hasSize( 19 ) );
		assertThat( validationReport.getDocumentUri().toString(), equalTo( documentUri ) );

		// JSON
		responseBody = mockMvc.perform( post( uriBuilder.toEncodedString() )
				.accept( MediaType.APPLICATION_JSON ) )
				.andExpect( status().isOk() )
				.andReturn().getResponse().getContentAsString();
		validationReport = objectMaper.readValue( responseBody, ValidationReportV0.class );
		assertThat( validationReport.getConstraintViolations(), hasSize( 19 ) );
		assertThat( validationReport.getDocumentUri().toString(), equalTo( documentUri ) );
	}

	@Test
	void loadDdiInputStreams()
	{
		// https://bitbucket.org/cessda/cessda.cmv.server/issues/24/sometimes-validationcontrollerv0test-fails

		loadDdiInputStream( profileUri );
		loadDdiInputStream( documentUri );
	}

	private void loadDdiInputStream( String uri )
	{
		assertDoesNotThrow( () ->
		{
			LOGGER.trace( "Instantiate inputStream " + uri );
			try ( InputStream inputStream = new DdiInputStream( newResource( uri ).readInputStream() ) )
			{
				LOGGER.trace( "Read " + uri );
			}
			catch (IOException e)
			{
				fail( e.getMessage() );
			}
		} );
	}
}
