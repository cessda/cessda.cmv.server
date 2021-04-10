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

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.gesis.commons.resource.ClasspathResourceRepository;
import org.gesis.commons.resource.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.zalando.problem.ProblemModule;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;

import eu.cessda.cmv.core.CessdaMetadataValidatorFactory;
import eu.cessda.cmv.core.ValidationService;

@SpringBootApplication
public class Server extends SpringBootServletInitializer
{
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
		if ( args.length > 1 )
		{
			throw new IllegalArgumentException( message );
		}
		else if ( args.length == 1 && !args[0].contentEquals( ALLOWED_CLI_OPTION ) )
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
				.includeLocationPattern( "classpath*:**/demo-documents/ddi-v25/*profile.xml" )
				.build()
				.findAll()
				.map( Resource.V10.class::cast )
				.collect( Collectors.toList() );
	}

	@Bean
	public List<Resource.V10> demoDocuments()
	{
		return ClasspathResourceRepository.newBuilder()
				.includeLocationPattern( "classpath*:**/demo-documents/ddi-v25/*.xml" )
				.excludeLocationPattern( "classpath*:**/demo-documents/ddi-v25/*profile*.xml" )
				.build()
				.findAll()
				.map( Resource.V10.class::cast )
				.collect( Collectors.toList() );
	}
}
