package eu.cessda.cmv.server.ui;

/*-
 * #%L
 * CESSDA Metadata Validator
 * %%
 * Copyright (C) 2020 - 2026 CESSDA ERIC
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

import java.io.Serial;

/**
 * Thrown when an error occurs generating a CSV export.
 */
public class CSVException extends RuntimeException
{
	@Serial
	private static final long serialVersionUID = -1872344688892683911L;

	/**
	 * Constructs a new CSV exception with the specified cause and a
	 * detail message of {@code (cause==null ? null : cause.toString())}
	 * (which typically contains the class and detail message of {@code cause}).
	 *
	 * @param cause the cause of this exception.
	 */
	public CSVException( Throwable cause )
	{
		super( cause );
	}
}
