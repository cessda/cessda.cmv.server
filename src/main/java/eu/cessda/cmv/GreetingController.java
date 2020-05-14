package eu.cessda.cmv;

import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag( name = Swagger.TAG_GREETING )
public class GreetingController
{
	private static final Logger logger = LoggerFactory.getLogger( GreetingController.class );

	private static final String EXCEPTION_MESSAGE = "IllegalArgumentException thrown by intention and mapped to BAD_REQUEST";

	@GetMapping( path = "/hello-world", produces = TEXT_PLAIN_VALUE )
	public String helloWorld()
	{
		logger.info( "Hello requested" );
		return "Hello world!";
	}

	@GetMapping( path = "/good-evening", produces = TEXT_PLAIN_VALUE )
	public String goodEvening()
	{
		logger.info( "Good evening" );
		return "Good evening";
	}

	@ApiResponses( { @ApiResponse( responseCode = "400" ) } )
	@GetMapping( path = "/exception", produces = TEXT_PLAIN_VALUE )
	public String throwIllegalArgumentException()
	{
		// See ExceptionToProblemAdviceTrait for mapping
		throw new IllegalArgumentException( EXCEPTION_MESSAGE );
	}
}
