package eu.cessda.cmv.server;

import java.util.List;
import java.util.stream.Collectors;

import org.gesis.commons.resource.ClasspathResourceRepository;
import org.gesis.commons.resource.Resource;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.zalando.problem.ProblemModule;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.cessda.cmv.core.CessdaMetadataValidatorFactory;
import eu.cessda.cmv.core.ValidationService;

@SpringBootApplication
public class Server extends SpringBootServletInitializer
{
	static final String ALLOWED_CLI_OPTION = "--spring.config.additional-location=file:./application.properties";

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

	@Bean
	public ObjectMapper objectMapper()
	{
		// See https://github.com/zalando/problem-spring-web/tree/master/problem-spring-web
		ProblemModule problemModule = new ProblemModule().withStackTraces( false );

		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule( problemModule );
		return objectMapper;
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
