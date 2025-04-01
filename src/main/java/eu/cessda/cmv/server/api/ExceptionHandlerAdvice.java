/*-
 * #%L
 * CESSDA Metadata Validator
 * %%
 * Copyright (C) 2020 - 2025 CESSDA ERIC
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
package eu.cessda.cmv.server.api;

import eu.cessda.cmv.core.NotDocumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.ServletWebRequest;
import org.zalando.problem.Problem;
import org.zalando.problem.spring.web.advice.AdviceTrait;
import org.zalando.problem.spring.web.advice.ProblemHandling;

import javax.servlet.http.HttpServletRequest;

import static org.zalando.problem.Status.BAD_REQUEST;
import static org.zalando.problem.Status.NOT_FOUND;

@ControllerAdvice
public class ExceptionHandlerAdvice implements ProblemHandling, AdviceTrait
{
	private static final Logger logger = LoggerFactory.getLogger( ExceptionHandlerAdvice.class );

	@Override
	public void log( Throwable throwable, Problem problem, NativeWebRequest webRequest, HttpStatus status )
	{
		HttpServletRequest request = ((ServletWebRequest) webRequest).getRequest();
		var stringBuilder = new StringBuilder();
		stringBuilder.append( throwable.getMessage() ).append( " by " )
				.append( request.getProtocol() ).append( " " )
				.append( ( (ServletWebRequest) webRequest ).getHttpMethod() ).append( " " )
				.append( request.getRequestURI().replace( request.getContextPath(), "" ) );
		if ( request.getQueryString() != null )
		{
			stringBuilder.append( "?" ).append( request.getQueryString() );
		}
		final String message = stringBuilder.toString();
		if (status.is5xxServerError())
		{
			logger.error( message, throwable );
		}
		else
		{
			logger.warn( message );
		}
	}

	@ExceptionHandler(value = { IllegalArgumentException.class, NotDocumentException.class })
	public ResponseEntity<Problem> handle(
			Exception exception,
			NativeWebRequest request )
	{
		return create( BAD_REQUEST, exception, request );
	}

	@ExceptionHandler
	public ResponseEntity<Problem> handle(
			ResourceNotFoundException exception,
			NativeWebRequest request )
	{
		return create( NOT_FOUND, exception, request );
	}
}
