package eu.cessda.cmv.server;

import eu.cessda.cmv.core.CessdaMetadataValidatorFactory;
import org.gesis.commons.resource.Resource;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import java.io.IOException;

import static eu.cessda.cmv.core.ValidationGateName.BASIC;
import static org.assertj.core.api.Assertions.assertThat;

class ValidationEngineTest
{
	@Test
	void shouldValidateDocument() throws IOException, SAXException
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
		assertThat( report.validationErrors() ).isNotEmpty();
		assertThat( report.validationReport().getConstraintViolations() ).isNotEmpty();
	}
}
