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
package eu.cessda.cmv.server;

import static eu.cessda.cmv.server.Server.ALLOWED_CLI_OPTION;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.Test;

public class ServerTest
{
	@Test
	public void validateArgs()
	{
		assertDoesNotThrow( () -> Server.validateArgs() );
		assertDoesNotThrow( () -> Server.validateArgs( ALLOWED_CLI_OPTION ) );
		assertThrows( IllegalArgumentException.class, () -> Server.validateArgs( "--not-allowed" ) );
		assertThrows( IllegalArgumentException.class, () -> Server.validateArgs( "--not-allowed", "--not-allowed" ) );
	}
}
