package eu.cessda.cmv.server;

import static org.zalando.problem.Status.BAD_REQUEST;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.NativeWebRequest;
import org.zalando.problem.Problem;
import org.zalando.problem.spring.web.advice.AdviceTrait;

public interface ExceptionToProblemAdviceTrait extends AdviceTrait
{
	@ExceptionHandler
	default ResponseEntity<Problem> handle(
			IllegalArgumentException exception,
			NativeWebRequest request )
	{
		return create( BAD_REQUEST, exception, request );
	}
}
