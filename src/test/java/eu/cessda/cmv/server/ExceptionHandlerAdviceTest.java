package eu.cessda.cmv.server;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
@SpringBootTest( webEnvironment = WebEnvironment.RANDOM_PORT )
class ExceptionHandlerAdviceTest
{
	@Autowired
	private MockMvc mockMvc;

	@Test
	void testEndpoints() throws Exception
	{
		mockMvc.perform( get( "/" )
				.accept( MediaType.TEXT_HTML ) )
				.andExpect( status().is( 302 ) )
				.andExpect( header().string( "Location", "/index.html" ) );

		mockMvc.perform( get( "/api/swagger" )
				.accept( MediaType.TEXT_HTML ) )
				.andExpect( status().is( 302 ) )
				.andExpect( header().string( "Location",
						"/api/swagger-ui/index.html?configUrl=/api/oas3/swagger-config" ) );

		mockMvc.perform( get( "/not-found" )
				.accept( MediaType.APPLICATION_JSON ) )
				.andExpect( status().isNotFound() )
				.andExpect( jsonPath( "$.title" ).value( "Not Found" ) );

		mockMvc.perform( get( "/api/V0/not-found" )
				.accept( MediaType.APPLICATION_JSON ) )
				.andExpect( status().isNotFound() )
				.andExpect( jsonPath( "$.title" ).value( "Not Found" ) );

		mockMvc.perform( get( "/api/V0/Validation" )
				.accept( MediaType.APPLICATION_JSON ) )
				.andExpect( status().is( 405 ) )
				.andExpect( jsonPath( "$.title" ).value( "Method Not Allowed" ) );
	}
}
