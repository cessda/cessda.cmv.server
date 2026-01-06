/*-
 * #%L
 * CESSDA Metadata Validator
 * %%
 * Copyright (C) 2020 - 2026 CESSDA ERIC
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

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfiguration
{
	public static final String TAG_VALIDATIONS = "Validations";
	public static final String TAG_ACTUATOR = "Actuator";

	@Bean
	public OpenAPI openAPI(
			@Autowired( required = false ) BuildProperties buildProperties,
			@Value( "${springdoc.show-actuator}" ) boolean showActuator )
	{
		final Info info;
		if ( buildProperties != null )
		{
			info = new Info().title( buildProperties.getName() ).version( buildProperties.getVersion() );
		}
		else
		{
			// Apply some default values if buildProperties is unset
			info = new Info().title( "CMV Server" ).version( "SNAPSHOT" );
		}

		OpenAPI openAPI = new OpenAPI()
				.addTagsItem( new Tag().name( TAG_VALIDATIONS ) )
				.info( info );
		if ( showActuator )
		{
			openAPI.addTagsItem( new Tag().name( TAG_ACTUATOR ) );
		}
		return openAPI;
	}
}
