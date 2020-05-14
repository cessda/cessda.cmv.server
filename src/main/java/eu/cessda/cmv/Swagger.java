package eu.cessda.cmv;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.tags.Tag;

@Configuration
@RestController
public class Swagger implements WebMvcConfigurer
{
	public static final String TAG_GREETING = "Greeting";
	public static final String TAG_ACTUATOR = "Actuator";

	@Autowired
	private BuildProperties buildProperties;

	@GetMapping( "/" )
	public void redirectToSwaggerUi( HttpServletResponse response ) throws IOException
	{
		response.sendRedirect( "/api/swagger" );
	}

	@Bean
	public OpenAPI openAPI()
	{
		Info info = new Info().title( buildProperties.getArtifact() ).version( buildProperties.getVersion() );
		return new OpenAPI()
				.addTagsItem( new Tag().name( TAG_GREETING ) )
				.addTagsItem( new Tag().name( TAG_ACTUATOR ) )
				.info( info );
	}
}