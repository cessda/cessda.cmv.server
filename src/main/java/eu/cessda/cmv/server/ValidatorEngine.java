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
package eu.cessda.cmv.server;

import eu.cessda.cmv.core.ValidationGateName;
import eu.cessda.cmv.core.ValidationService;
import org.gesis.commons.resource.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class ValidatorEngine
{
	private final ThreadLocal<javax.xml.validation.Validator> xmlValidators;
	private final ValidationService.V10 validationService;

	@Autowired
	public ValidatorEngine( ValidationService.V10 validationService )
	{
		this.validationService = validationService;
		this.xmlValidators = ThreadLocal.withInitial( () ->
		{
			try
			{
				// Find the resources for the XML schemas
				var codebookResource = this.getClass().getResource( "/static/schemas/codebook/codebook.xsd" );
				var lifecycleResource = this.getClass().getResource( "/static/schemas/lifecycle/instance.xsd" );
				var nesstarResource = this.getClass().getResource( "/static/schemas/nesstar/Version1-2-2.xsd" );
				var oaiResource = this.getClass().getResource( "/static/schemas/oai-pmh/OAI-PMH.xsd" );

				// Assert schemas are not null
				assert codebookResource != null;
				assert lifecycleResource != null;
				assert nesstarResource != null;
				assert oaiResource != null;

				// Construct schema objects from the XML schemas
				var schema = SchemaFactory.newDefaultInstance().newSchema( new Source[]{
						new StreamSource( codebookResource.toExternalForm() ),
						new StreamSource( lifecycleResource.toExternalForm() ),
						new StreamSource( nesstarResource.toExternalForm() ),
						new StreamSource( oaiResource.toExternalForm() )
				} );

				// Create a validator and set its error handler
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

	/**
	 * Validates a given XML document against XML schema and a CMV profile.
	 *
	 * @param validationRequest the document to validate.
	 * @param profile the profile to use when validating.
	 * @param validationGate the validation gate to use when validating
	 * @return a {@link ValidationReport} containing the list of XML validation errors and the CMV validation report.
	 * @throws IOException if an IO error occurred when parsing the document.
	 * @throws SAXException if the XML document was invalid.
	 */
	public ValidationReport validate( Resource validationRequest, Resource profile, ValidationGateName validationGate ) throws IOException, SAXException
	{
		// Validate XML
		var validator = xmlValidators.get();
		validator.validate( new StreamSource( validationRequest.readInputStream() ) );

		// Extract errors from the error handler, then reset the validator
		var errorHandler = (LoggingErrorHandler) validator.getErrorHandler();
		var errors = errorHandler.getErrors();
		errorHandler.reset();

		// Validate against profile
		return new ValidationReport( errors, validationService.validate( validationRequest, profile, validationGate ) );
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
