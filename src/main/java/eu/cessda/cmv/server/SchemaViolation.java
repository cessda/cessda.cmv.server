package eu.cessda.cmv.server;

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

import org.xml.sax.SAXParseException;

public record SchemaViolation(
	Integer lineNumber,
	Integer columnNumber,
	String message
)
{
	/**
	 * Create a SchemaViolation from a {@link SAXParseException}.
	 */
	public SchemaViolation( SAXParseException saxException )
	{
		this(
			saxException.getLineNumber() != -1 ? saxException.getLineNumber() : null,
			saxException.getColumnNumber() != -1 ? saxException.getColumnNumber() : null,
			saxException.getMessage()
		);
	}

	@Override
	public String toString()
	{
		return
			"lineNumber: " + lineNumber +
			", columnNumber: " + columnNumber +
			", " + message;
	}
}
