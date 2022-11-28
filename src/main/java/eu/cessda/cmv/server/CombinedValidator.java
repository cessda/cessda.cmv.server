package eu.cessda.cmv.server;

import eu.cessda.cmv.core.ValidationGateName;
import eu.cessda.cmv.core.ValidationService;
import eu.cessda.cmv.server.ui.ValidationReportGridValueProvider;
import org.gesis.commons.resource.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class CombinedValidator
{
	private final ThreadLocal<javax.xml.validation.Validator> xmlValidators;
	private final ValidationService.V10 validationService;

	@Autowired
	public CombinedValidator( ValidationService.V10 validationService )
	{
		this.validationService = validationService;
		this.xmlValidators = ThreadLocal.withInitial( () ->
		{
			try
			{
				var resource = this.getClass().getResource( "/static/schemas/codebook/codebook.xsd" );
				var schema = SchemaFactory.newDefaultInstance().newSchema( resource );
				var validator = schema.newValidator();
				validator.setErrorHandler( new LoggingErrorHandler() );
				return validator;
			}
			catch ( SAXException e )
			{
				throw new IllegalStateException( e );
			}
		} );
	}

	public ValidationReportGridValueProvider.Report validate( Resource validationRequest, Resource profile, ValidationGateName validationGate ) throws IOException
	{
		// Validate XML
		var validator = xmlValidators.get();
		try
		{
			validator.validate( new StreamSource( validationRequest.readInputStream() ) );
		}
		catch ( SAXException e )
		{
			System.out.println( "XML error " + e );
		}

		// Extract errors from the error handler, then reset the validator
		var errorHandler = (LoggingErrorHandler) validator.getErrorHandler();
		var errors = errorHandler.getErrors();
		errorHandler.reset();

		// Validate against profile
		return new ValidationReportGridValueProvider.Report( errors, validationService.validate( validationRequest, profile, validationGate ) );
	}

	/**
	 * An error handler that stores all encountered SAX errors and warnings.
	 * <p>
	 * This error handler is stateful and must be reset before validating the next document.
	 *
	 * @implNote Fatal errors are rethrown as is and are not reported by this error handler.
	 */
	private static class LoggingErrorHandler implements ErrorHandler
	{
		private final ArrayList<SAXParseException> errors = new ArrayList<>();

		@Override
		public void warning( SAXParseException exception )
		{
			errors.add( exception );
		}

		@Override
		public void error( SAXParseException exception )
		{
			errors.add( exception );
		}

		@Override
		public void fatalError( SAXParseException exception ) throws SAXParseException
		{
			throw exception;
		}

		/**
		 * Clears the list of {@link SAXParseException}s from the handler.
		 */
		public void reset()
		{
			errors.clear();
		}

		/**
		 * Gets an unmodifiable list of the {@link SAXParseException}s encountered by the error handler.
		 */
		public List<SAXParseException> getErrors()
		{
			return List.copyOf( errors );
		}
	}
}
