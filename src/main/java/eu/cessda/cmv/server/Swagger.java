package eu.cessda.cmv.server;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.tags.Tag;

@Configuration
@RestController
public class Swagger implements WebMvcConfigurer
{
	public static final String TAG_VALIDATIONGATES = "Validation Gates";
	public static final String TAG_ACTUATOR = "Actuator";

	@GetMapping( "/" )
	public void delegateToSite( HttpServletResponse response ) throws IOException
	{
		response.sendRedirect( "/index.html" );
	}

	@Override
	public void addResourceHandlers( ResourceHandlerRegistry registry )
	{
		registry.addResourceHandler( "/**" ).addResourceLocations( "classpath:/eu.cessda.cmv/cmv/" );
	}

	@Bean
	public OpenAPI openAPI(BuildProperties buildProperties)
	{
		Info info = new Info().title( buildProperties.getName() ).version( buildProperties.getVersion() );
		return new OpenAPI()
				.addTagsItem( new Tag().name( TAG_VALIDATIONGATES ) )
				.addTagsItem( new Tag().name( TAG_ACTUATOR ) )
				.info( info );
	}
}