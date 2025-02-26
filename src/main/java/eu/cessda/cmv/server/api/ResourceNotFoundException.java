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

import java.io.Serial;

import static java.lang.String.format;

public class ResourceNotFoundException extends RuntimeException
{
	@Serial
	private static final long serialVersionUID = 9203612830248772991L;

	public ResourceNotFoundException( String httpMethod, String requestPath )
	{
		super( format( "No handler found for %s /%s", httpMethod, requestPath ) );
	}
}
