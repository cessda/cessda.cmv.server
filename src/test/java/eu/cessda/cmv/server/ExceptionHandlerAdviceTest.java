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
package eu.cessda.cmv.server;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
				.andExpect( status().isOk() );

		mockMvc.perform( get( "/not-found" )
				.accept( MediaType.APPLICATION_JSON ) )
				.andExpect( status().isNotFound() )
				.andExpect( jsonPath( "$.title" ).value( "Not Found" ) );

		mockMvc.perform( get( "/documentation" )
				.accept( MediaType.TEXT_HTML ) )
				.andExpect( status().is( 302 ) )
				.andExpect( header().string( "Location", "/documentation/index.html" ) );

		mockMvc.perform( get( "/documentation/not-found" )
				.accept( MediaType.APPLICATION_JSON ) )
				.andExpect( status().isNotFound() )
				.andExpect( jsonPath( "$.title" ).value( "Not Found" ) );

		mockMvc.perform( get( "/api/swagger" )
						.accept( MediaType.TEXT_HTML ) )
				.andExpect( status().is( 302 ) )
				.andExpect( header().string( "Location",
						"/api/swagger-ui/index.html" ) );

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
