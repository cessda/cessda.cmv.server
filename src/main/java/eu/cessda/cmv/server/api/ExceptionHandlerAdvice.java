package eu.cessda.cmv.server.api;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.ServletWebRequest;
import org.zalando.problem.Problem;
import org.zalando.problem.spring.web.advice.ProblemHandling;

@ControllerAdvice
public class ExceptionHandlerAdvice implements ProblemHandling,
		ExceptionToProblemAdviceTrait
{
	private static final Logger logger = LoggerFactory.getLogger( ExceptionHandlerAdvice.class );

	@Override
	public void log( Throwable throwable, Problem problem, NativeWebRequest webRequest, HttpStatus status )
	{
		HttpServletRequest request = ((ServletWebRequest) webRequest).getRequest();
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append( throwable.getMessage() ).append( " by " )
				.append( request.getProtocol() ).append( " " )
				.append( ((ServletWebRequest) webRequest).getHttpMethod().toString() ).append( " " )
				.append( request.getRequestURI().replace( request.getContextPath(), "" ) );
		if ( request.getQueryString() != null )
		{
			stringBuilder.append( "?" ).append( request.getQueryString() );
		}
		final String message = stringBuilder.toString();
		logger.warn( message );
	}
}
