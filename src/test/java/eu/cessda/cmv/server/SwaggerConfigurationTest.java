package eu.cessda.cmv.server;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
@SpringBootTest( webEnvironment = WebEnvironment.RANDOM_PORT )
class SwaggerConfigurationTest
{
	@Autowired
	private MockMvc mockMvc;

	@Test
	void swagger() throws Exception
	{
		mockMvc.perform( get( "/api/swagger" ) )
				.andExpect( status().is( 302 ) )
				.andExpect( header().string( "Location", containsString( "swagger-ui/index.html" ) ) );
	}
}
