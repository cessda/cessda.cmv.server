package eu.cessda.cmv.server;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.UnsupportedEncodingException;

import org.gesis.commons.resource.SpringUriBuilder;
import org.gesis.commons.resource.UriBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.web.servlet.MockMvc;

import eu.cessda.cmv.core.mediatype.validationreport.v0.xml.JaxbValidationReportV0;

@AutoConfigureMockMvc
@SpringBootTest( webEnvironment = WebEnvironment.RANDOM_PORT )
public class ValidationGateControllerTest
{
	@Autowired
	private MockMvc mockMvc;

	private String profileUrl = "https://bitbucket.org/cessda/cessda.cmv.core/raw/ad7e3ffd847ecb9c35faea329fbc7cfe14bfb7a6/src/main/resources/demo-documents/ddi-v25/cdc25_profile.xml";
	private String documentUrl = "https://bitbucket.org/cessda/cessda.cmv.core/raw/ad7e3ffd847ecb9c35faea329fbc7cfe14bfb7a6/src/main/resources/demo-documents/ddi-v25/ukds-2000.xml";

	@Test
	public void validateWithBasicValidationGate() throws Exception
	{
		UriBuilder.V10 uriBuilder = new SpringUriBuilder( "" )
				.path( "/api" )
				.path( "/basic-validation-gate" )
				.queryParameter( "documentUrl", documentUrl )
				.queryParameter( "profileUrl", profileUrl );
		System.out.println( uriBuilder.toEncodedString() );
		String body = mockMvc.perform( get( uriBuilder.toEncodedString() ) )
				.andExpect( status().isOk() )
				.andReturn().getResponse().getContentAsString();
		JaxbValidationReportV0 validationReport = JaxbValidationReportV0.read( body );
		assertThat( validationReport.getConstraintViolations(), hasSize( 9 ) );
	}

	@Test
	public void validateWithStandardValidationGate() throws UnsupportedEncodingException, Exception
	{
		UriBuilder.V10 uriBuilder = new SpringUriBuilder( "" )
				.path( "/api" )
				.path( "/standard-validation-gate" )
				.queryParameter( "documentUrl", documentUrl )
				.queryParameter( "profileUrl", profileUrl );
		System.out.println( uriBuilder.toEncodedString() );
		String body = mockMvc.perform( get( uriBuilder.toEncodedString() ) )
				.andExpect( status().isOk() )
				.andReturn().getResponse().getContentAsString();
		JaxbValidationReportV0 validationReport = JaxbValidationReportV0.read( body );
		assertThat( validationReport.getConstraintViolations(), hasSize( 21 ) );
	}
}
