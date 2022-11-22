/*-
 * #%L
 * CESSDA Metadata Validator
 * %%
 * Copyright (C) 2020 - 2021 CESSDA ERIC
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
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
import eu.cessda.cmv.core.CessdaMetadataValidatorFactory;
import eu.cessda.cmv.core.ValidationService;
import org.gesis.commons.resource.ClasspathResourceRepository;
import org.gesis.commons.resource.FileNameResourceLabelProvider;
import org.gesis.commons.resource.Resource;
import org.gesis.commons.xml.XercesXalanDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.zalando.problem.jackson.ProblemModule;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.gesis.commons.resource.Resource.newResource;

@SpringBootApplication
public class Server extends SpringBootServletInitializer
{
	private static final Logger log = LoggerFactory.getLogger( Server.class );

	static final String ALLOWED_CLI_OPTION = "--spring.config.additional-location=file:./application.properties";

	@Autowired
	private ObjectMapper objectMapper;

	public static void main( String[] args )
	{
		SpringApplication.run( Server.class, validateArgs( args ) );
	}

	static String[] validateArgs( String... args )
	{
		String message = "Commandline arguments not as expected - Good bye!";
		if ( args.length > 1 || args.length == 1 && !args[0].contentEquals( ALLOWED_CLI_OPTION ) )
		{
			throw new IllegalArgumentException( message );
		}
		return args;
	}

	@Override
	protected SpringApplicationBuilder configure( SpringApplicationBuilder application )
	{
		return application.sources( Server.class );
	}

	@PostConstruct
	public void postConstruct()
	{
		objectMapper.configure( MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true );
		objectMapper.configure( DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false );
		objectMapper.registerModule( new ProblemModule().withStackTraces( false ) );
		objectMapper.registerModule( new JaxbAnnotationModule() );
		objectMapper.enable( SerializationFeature.INDENT_OUTPUT );
	}

	@Bean
	public CessdaMetadataValidatorFactory cessdaMetadataValidatorFactory()
	{
		return new CessdaMetadataValidatorFactory();
	}

	@Bean
	public ValidationService.V10 validationService()
	{
		return cessdaMetadataValidatorFactory().newValidationService();
	}

	@Bean
	public List<Resource.V10> demoProfiles()
	{
		return ClasspathResourceRepository.newBuilder()
				.includeLocationPattern( "classpath*:**/profiles/**/*.xml" )
				.build()
				.findAll()
				.map( resource ->
				{
					var profile = XercesXalanDocument.newBuilder().ofInputStream( resource.readInputStream() ).build();

					// Extract the profile name and version.
					// There isn't a public method to do this, so use the XPath directly.
					var profileName = profile.selectNode( "/DDIProfile/DDIProfileName" ).getTextContent().trim();
					var profileVersion = profile.selectNode( "/DDIProfile/Version" ).getTextContent().trim();

					return newResource( resource.getUri(), profileName + ": " + profileVersion );
				} )
				.map( Resource.V10.class::cast )
				.sorted( Comparator.comparing( Resource.V10::getLabel ) )
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
		var labelProvider = new FileNameResourceLabelProvider();
		try
		{
			return ClasspathResourceRepository.newBuilder()
					.includeLocationPattern( "classpath*:**/demo-documents/ddi-v25/*.xml" )
					.excludeLocationPattern( "classpath*:**/demo-documents/ddi-v25/*profile*.xml" )
					.build()
					.findAll()
					.map( Resource::getUri )
					.map( uri -> newResource( uri, labelProvider ) )
					.map( Resource.V10.class::cast )
					.sorted( Comparator.comparing( Resource.V10::getLabel ) )
					.toList();
		}
		catch ( RuntimeException e )
		{
			log.warn( "Couldn't discover demo documents", e );
			return Collections.emptyList();
		}
	}
}
