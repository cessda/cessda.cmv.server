/*-
 * #%L
 * CESSDA Metadata Validator
 * %%
 * Copyright (C) 2020 - 2024 CESSDA ERIC
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

import eu.cessda.cmv.core.CessdaMetadataValidatorFactory;
import eu.cessda.cmv.core.NotDocumentException;
import org.gesis.commons.resource.Resource;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import java.io.IOException;

import static eu.cessda.cmv.core.ValidationGateName.BASIC;
import static org.assertj.core.api.Assertions.assertThat;

class ValidationEngineTest
{
	@Test
	void shouldValidateDocument() throws IOException, SAXException, NotDocumentException
	{
		// Init
		var validationService = new CessdaMetadataValidatorFactory().newValidationService();
		var validatorEngine = new ValidatorEngine( validationService );

		// Get the document and the profile
		var document = this.getClass().getResource( "/synthetic_compliant_cmm.xml" );
		var profile = this.getClass().getResource( "/static/profiles/cdc/ddi-2.5/1.0.4/profile.xml" );

		// Assert that the profile and document are found
		assertThat(profile).isNotNull();
		assertThat(document).isNotNull();

		// Validate
		var report = validatorEngine.validate(
				Resource.newResource(document),
				Resource.newResource( profile ),
				BASIC
		);

		// Schema and CMV violations should be found
		assertThat( report.schemaViolations() ).isNotEmpty();
		assertThat( report.constraintViolations() ).isNotEmpty();
	}
}
