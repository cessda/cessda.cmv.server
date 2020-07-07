package eu.cessda.cmv.server.api;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_XML_VALUE;

import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import eu.cessda.cmv.core.ValidationGateName;
import eu.cessda.cmv.core.ValidationService;
import eu.cessda.cmv.core.mediatype.validationreport.v0.ValidationReportV0;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping( ValidationControllerV0.BASE_PATH )
@Tag( name = SwaggerConfiguration.TAG_VALIDATIONS )
public class ValidationControllerV0
{
	public static final String BASE_PATH = "/api/V0";

	@Autowired
	private ValidationService.V10 validationService;

	@PostMapping(
			path = "/Validation",
			produces = { APPLICATION_JSON_VALUE, APPLICATION_XML_VALUE } )
	@Operation(
			responses = @ApiResponse( responseCode = "200" ) )
	public ValidationReportV0 validate(
			@RequestParam( required = true ) URI documentUri,
			@RequestParam( required = true ) URI profileUri,
			@RequestParam( required = true ) ValidationGateName validationGateName )
	{
		return validationService.validate( documentUri, profileUri, validationGateName );
	}
}
