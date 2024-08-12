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

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
import eu.cessda.cmv.core.CessdaMetadataValidatorFactory;
import eu.cessda.cmv.core.NotDocumentException;
import eu.cessda.cmv.core.Profile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.zalando.problem.jackson.ProblemModule;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

@SpringBootApplication
public class Server extends SpringBootServletInitializer
{
	private static final Logger log = LoggerFactory.getLogger( Server.class );

	private final ApplicationContext applicationContext;

	@Autowired
	public Server( ApplicationContext applicationContext )
	{
		this.applicationContext = applicationContext;
	}

	public static void main( String[] args )
	{
		SpringApplication.run( Server.class, args  );
	}

	@Override
	protected SpringApplicationBuilder configure( SpringApplicationBuilder application )
	{
		return application.sources( Server.class );
	}

	@Bean
	public ObjectMapper objectMapper() {
		return JsonMapper.builder()
				.configure( MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true )
				.configure( DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true )
				.addModule( new JaxbAnnotationModule().setPriority( JaxbAnnotationModule.Priority.SECONDARY ) )
				.addModule( new ProblemModule().withStackTraces( false ) )
				.enable( SerializationFeature.INDENT_OUTPUT )
				.build();
	}

	@Bean
	public CessdaMetadataValidatorFactory cessdaMetadataValidatorFactory()
	{
		return new CessdaMetadataValidatorFactory();
	}

	@Bean
	public List<Profile> demoProfiles( CessdaMetadataValidatorFactory factory ) throws IOException
	{
		log.info( "Loading built-in profiles" );

		var resources = applicationContext.getResources( "classpath*:**/profiles/**/*.xml" );
		var profiles = new ArrayList<Profile>(resources.length);
		for ( var resource : resources )
		{
			log.debug( "Loading profile from \"{}\"", resource );
			try( var inputStream = resource.getInputStream() )
			{
				var profile = factory.newProfile( inputStream );
				profiles.add( profile );
			}
			catch ( NotDocumentException | IOException e )
			{
				log.error( "Couldn't load profile from \"{}\"", resource, e );
			}
		}

		return profiles;
	}

	@Bean
	public CorsFilter corsFilter()
	{
		var corsConfiguration = new CorsConfiguration().applyPermitDefaultValues();

		// Register CORS filters for public APIs
		var source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration( "/api/V0/**", corsConfiguration );
		source.registerCorsConfiguration( "/api/oas3", corsConfiguration );

		return new CorsFilter( source );
	}

	@Bean
	public List<org.springframework.core.io.Resource> demoDocuments() throws IOException
	{
		log.info( "Discovering built-in documents" );
		var resources = applicationContext.getResources("classpath*:**/demo-documents/ddi-v25/*.xml");
		var excludedResources = applicationContext.getResources( "classpath*:**/demo-documents/ddi-v25/*profile*.xml");

		var includedResourcesSet = new HashSet<>( Arrays.asList( resources ) );
		for (var excluded : excludedResources)
		{
			// Remove profiles from the document list
			includedResourcesSet.remove( excluded );
			log.debug( "\"{}\" excluded", excluded );
		}

		return new ArrayList<>( includedResourcesSet );
	}
}
