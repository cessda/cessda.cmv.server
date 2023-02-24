/*-
 * #%L
 * CESSDA Metadata Validator
 * %%
 * Copyright (C) 2020 - 2023 CESSDA ERIC
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

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.NativeWebRequest;
import org.zalando.problem.Problem;
import org.zalando.problem.spring.web.advice.AdviceTrait;

import static org.zalando.problem.Status.BAD_REQUEST;
import static org.zalando.problem.Status.NOT_FOUND;

public interface ExceptionToProblemAdviceTrait extends AdviceTrait
{
	@ExceptionHandler
	default ResponseEntity<Problem> handle(
			IllegalArgumentException exception,
			NativeWebRequest request )
	{
		return create( BAD_REQUEST, exception, request );
	}

	@ExceptionHandler
	default ResponseEntity<Problem> handle(
			ResourceNotFoundException exception,
			NativeWebRequest request )
	{
		return create( NOT_FOUND, exception, request );
	}
}
