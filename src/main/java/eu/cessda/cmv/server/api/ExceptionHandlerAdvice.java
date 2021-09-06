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
package eu.cessda.cmv.server.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.ServletWebRequest;
import org.zalando.problem.Problem;
import org.zalando.problem.spring.web.advice.ProblemHandling;

import javax.servlet.http.HttpServletRequest;

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
				.append( ( (ServletWebRequest) webRequest ).getHttpMethod() ).append( " " )
				.append( request.getRequestURI().replace( request.getContextPath(), "" ) );
		if ( request.getQueryString() != null )
		{
			stringBuilder.append( "?" ).append( request.getQueryString() );
		}
		final String message = stringBuilder.toString();
		logger.warn( message );
	}
}
