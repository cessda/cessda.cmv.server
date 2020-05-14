package eu.cessda.cmv.server;

import static org.springframework.http.MediaType.APPLICATION_XML_VALUE;

import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import eu.cessda.cmv.core.BasicValidationGate;
import eu.cessda.cmv.core.CessdaMetadataValidatorFactory;
import eu.cessda.cmv.core.ConstraintViolation;
import eu.cessda.cmv.core.Document;
import eu.cessda.cmv.core.Profile;
import eu.cessda.cmv.core.StandardValidationGate;
import eu.cessda.cmv.core.ValidationGate;
import eu.cessda.cmv.core.mediatype.validationreport.v0.xml.JaxbConstraintViolationV0;
import eu.cessda.cmv.core.mediatype.validationreport.v0.xml.JaxbValidationReportV0;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping( "api" )
@Tag( name = Swagger.TAG_VALIDATIONGATES )
public class ValidationGateController
{
	private static final Logger LOGGER = LoggerFactory.getLogger( ValidationGateController.class );

	@Autowired
	private CessdaMetadataValidatorFactory factory;

	@GetMapping(
			path = "/basic-validation-gate",
			produces = { JaxbValidationReportV0.MEDIATYPE, APPLICATION_XML_VALUE } )
	@Operation(
			responses = @ApiResponse( responseCode = "200", content = @Content( schema = @Schema( hidden = true ) ) ) )
	public JaxbValidationReportV0 validateWithBasicValidationGate(
			@RequestParam( required = false ) URL documentUrl,
			@RequestParam( required = false ) URL profileUrl )
			throws Exception
	{
		URL dUrl = new URL(
				"https://bitbucket.org/cessda/cessda.cmv.core/raw/ad7e3ffd847ecb9c35faea329fbc7cfe14bfb7a6/src/main/resources/demo-documents/ddi-v25/ukds-2000.xml" );
		URL pUrl = new URL(
				"https://bitbucket.org/cessda/cessda.cmv.core/raw/ad7e3ffd847ecb9c35faea329fbc7cfe14bfb7a6/src/main/resources/demo-documents/ddi-v25/cdc25_profile.xml" );

		return validate( new BasicValidationGate(), dUrl, pUrl );
	}

	@GetMapping(
			path = "/standard-validation-gate",
			produces = { JaxbValidationReportV0.MEDIATYPE, APPLICATION_XML_VALUE } )
	@Operation(
			responses = @ApiResponse( responseCode = "200", content = @Content( schema = @Schema( hidden = true ) ) ) )
	public JaxbValidationReportV0 validate(
			@RequestParam( required = false ) URL documentUrl,
			@RequestParam( required = false ) URL profileUrl )
			throws Exception
	{
		URL dUrl = new URL(
				"https://bitbucket.org/cessda/cessda.cmv.core/raw/ad7e3ffd847ecb9c35faea329fbc7cfe14bfb7a6/src/main/resources/demo-documents/ddi-v25/ukds-2000.xml" );
		URL pUrl = new URL(
				"https://bitbucket.org/cessda/cessda.cmv.core/raw/ad7e3ffd847ecb9c35faea329fbc7cfe14bfb7a6/src/main/resources/demo-documents/ddi-v25/cdc25_profile.xml" );

		return validate( new StandardValidationGate(), dUrl, pUrl );
	}

	private JaxbValidationReportV0 validate( ValidationGate.V10 validationGate, URL documentUrl, URL profileUrl )
	{
		Document document = factory.newDocument( documentUrl );
		Profile profile = factory.newProfile( profileUrl );
		List<ConstraintViolation> constraintViolations = validationGate.validate( document, profile );
		JaxbValidationReportV0 validationReport = new JaxbValidationReportV0();
		validationReport.setConstraintViolations( constraintViolations.stream()
				.map( JaxbConstraintViolationV0::new )
				.collect( Collectors.toList() ) );
		LOGGER.info( "Validation executed: {}, {}, {}",
				validationGate.getClass().getCanonicalName(),
				documentUrl,
				profileUrl );
		return validationReport;
	}
}
