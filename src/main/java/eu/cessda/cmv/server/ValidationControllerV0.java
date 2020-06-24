package eu.cessda.cmv.server;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_XML_VALUE;

import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import eu.cessda.cmv.core.CessdaMetadataValidatorFactory;
import eu.cessda.cmv.core.ConstraintViolation;
import eu.cessda.cmv.core.Document;
import eu.cessda.cmv.core.Profile;
import eu.cessda.cmv.core.ValidationGate;
import eu.cessda.cmv.core.ValidationGateName;
import eu.cessda.cmv.core.mediatype.validationreport.v0.ConstraintViolationV0;
import eu.cessda.cmv.core.mediatype.validationreport.v0.ValidationReportV0;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping( ValidationControllerV0.BASE_PATH )
@Tag( name = Swagger.TAG_VALIDATIONS )
public class ValidationControllerV0
{
	private static final Logger LOGGER = LoggerFactory.getLogger( ValidationControllerV0.class );

	public static final String BASE_PATH = "/api/V0";

	@Autowired
	private CessdaMetadataValidatorFactory factory;

	@PostMapping(
			path = "/Validation",
			produces = { APPLICATION_JSON_VALUE, APPLICATION_XML_VALUE } )
	@Operation(
			responses = @ApiResponse( responseCode = "200" ) )
	public ValidationReportV0 validateWithBasicValidationGate(
			@RequestParam( required = true ) URL documentUrl,
			@RequestParam( required = true ) URL profileUrl,
			@RequestParam( required = true ) ValidationGateName validationGateName )
	{
		Document document = factory.newDocument( documentUrl );
		Profile profile = factory.newProfile( profileUrl );
		ValidationGate.V10 validationGate = factory.newValidationGate( validationGateName );
		List<ConstraintViolation> constraintViolations = validationGate.validate( document, profile );
		ValidationReportV0 validationReport = new ValidationReportV0();
		validationReport.setConstraintViolations( constraintViolations.stream()
				.map( ConstraintViolationV0::new )
				.collect( Collectors.toList() ) );
		LOGGER.info( "Validation executed: {}, {}, {}",
				validationGate.getClass().getCanonicalName(),
				documentUrl,
				profileUrl );
		return validationReport;
	}
}
