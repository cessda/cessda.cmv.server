package eu.cessda.cmv.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.zalando.problem.ProblemModule;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;

import eu.cessda.cmv.core.CessdaMetadataValidatorFactory;

@SpringBootApplication
public class Server extends SpringBootServletInitializer
{
	public static void main( String[] args )
	{
		SpringApplication.run( Server.class, args );
	}

	@Override
	protected SpringApplicationBuilder configure( SpringApplicationBuilder application )
	{
		return application.sources( Server.class );
	}

	@Bean
	public ObjectMapper objectMapper()
	{
		// See https://github.com/zalando/problem-spring-web/tree/master/problem-spring-web
		ProblemModule problemModule = new ProblemModule().withStackTraces( false );

		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule( problemModule ).registerModule( new JaxbAnnotationModule() );
		objectMapper.setAnnotationIntrospector( new JaxbAnnotationIntrospector() );
		return objectMapper;
	}

	@Bean
	public CessdaMetadataValidatorFactory cessdaMetadataValidatorFactory()
	{
		return new CessdaMetadataValidatorFactory();
	}
}
