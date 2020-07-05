package eu.cessda.cmv.server.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.tags.Tag;

@Configuration
public class SwaggerConfiguration
{
	public static final String TAG_VALIDATIONGATES = "Validation Gates";
	public static final String TAG_VALIDATIONS = "Validations";
	public static final String TAG_ACTUATOR = "Actuator";

	@Bean
	public OpenAPI openAPI(
			BuildProperties buildProperties,
			@Value( "${springdoc.show-actuator}" ) boolean showActuator )
	{
		Info info = new Info().title( buildProperties.getName() ).version( buildProperties.getVersion() );
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