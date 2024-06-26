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
import org.gesis.commons.resource.ClasspathResourceRepository;
import org.gesis.commons.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.zalando.problem.jackson.ProblemModule;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.gesis.commons.resource.Resource.newResource;

@SpringBootApplication
public class Server extends SpringBootServletInitializer
{
	private static final Logger log = LoggerFactory.getLogger( Server.class );

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
	public List<Profile> demoProfiles( CessdaMetadataValidatorFactory factory )
	{
		log.info( "Loading built-in profiles" );
		return ClasspathResourceRepository.newBuilder()
				.includeLocationPattern( "classpath*:**/profiles/**/*.xml" )
				.build()
				.findAll()
				.flatMap( resource ->
				{
					var uri = resource.getUri();
					try
					{
						var profile = factory.newProfile( uri );
						return Stream.of( profile );
					}
					catch ( NotDocumentException | IOException e )
					{
						log.error( "Couldn't load profile from {}", uri, e );
						return Stream.empty();
					}
				} )
				.toList();
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
	public List<Resource.V10> demoDocuments()
	{
		log.info( "Discovering built-in documents" );
        try
		{
			return ClasspathResourceRepository.newBuilder()
					.includeLocationPattern( "classpath*:**/demo-documents/ddi-v25/*.xml" )
					.excludeLocationPattern( "classpath*:**/demo-documents/ddi-v25/*profile*.xml" )
					.build()
					.findAll()
					.map( Resource::getUri )
					.map( uri ->
					{
						var path = uri.toString();
						var lastPathIndex = path.lastIndexOf( '/' );
						var fileName = path.substring( lastPathIndex + 1 );
						return newResource( uri, fileName );
					} )
					.map( Resource.V10.class::cast )
					.toList();
		}
		catch ( RuntimeException e )
		{
			log.warn( "Couldn't discover demo documents", e );
			return Collections.emptyList();
		}
	}
}
