package eu.cessda.cmv;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith( SpringRunner.class )
@SpringBootTest( webEnvironment = WebEnvironment.RANDOM_PORT )
@AutoConfigureMockMvc
@DirtiesContext( classMode = ClassMode.AFTER_EACH_TEST_METHOD )
public class GreetingControllerTest
{
	@Autowired
	private MockMvc mockMvc;

	@Test
	public void helloWorld() throws Exception
	{
		mockMvc.perform( get( "/hello-world" )
				.accept( MediaType.TEXT_PLAIN ) )
				.andExpect( status().isOk() )
				.andExpect( content().string( "Hello world!" ) )
				.andReturn();
	}

	@Test
	public void goodEvening() throws Exception
	{
		mockMvc.perform( get( "/good-evening" )
				.accept( MediaType.TEXT_PLAIN ) )
				.andExpect( status().isOk() )
				.andExpect( content().string( "Good evening" ) )
				.andReturn();
	}

	@Test
	public void exception() throws Exception
	{
		String expectedDetail = "IllegalArgumentException thrown by intention and mapped to BAD_REQUEST";
		mockMvc.perform( get( "/exception" )
				.accept( MediaType.TEXT_PLAIN ) )
				.andExpect( status().isBadRequest() )
				.andExpect( jsonPath( "$.detail", equalTo( expectedDetail ) ) );
	}

	@Test
	public void swagger() throws Exception
	{
		mockMvc.perform( get( "/" ) )
				.andExpect( status().is( 302 ) )
				.andExpect( header().string( "Location", equalTo( "/api/swagger" ) ) );
		mockMvc.perform( get( "/api/swagger" ) )
				.andExpect( status().is( 302 ) )
				.andExpect( header().string( "Location", containsString( "swagger-ui/index.html" ) ) );
	}
}
