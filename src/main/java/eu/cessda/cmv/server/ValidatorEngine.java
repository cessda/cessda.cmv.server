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

import eu.cessda.cmv.core.NotDocumentException;
import eu.cessda.cmv.core.ValidationGateName;
import eu.cessda.cmv.core.ValidationService;
import org.gesis.commons.resource.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class ValidatorEngine
{
	private final ThreadLocal<javax.xml.validation.Validator> xmlValidators;
	private final ValidationService validationService;

	@Autowired
	public ValidatorEngine( ValidationService validationService ) throws SAXException
	{
		this.validationService = validationService;

		// Find the resources for the XML schemas
		var schemaURLs = new URL[] {
			this.getClass().getResource("/static/schemas/codebook/codebook.xsd"),
			this.getClass().getResource("/static/schemas/lifecycle/3.2/instance.xsd"),
			this.getClass().getResource("/static/schemas/lifecycle/3.3/instance.xsd"),
			this.getClass().getResource("/static/schemas/nesstar/Version1-2-2.xsd"),
			this.getClass().getResource("/static/schemas/oai-pmh/OAI-PMH.xsd")
		};

		var sources = Arrays.stream(schemaURLs)
			.map(URL::toExternalForm)
			.map(StreamSource::new)
			.toArray(StreamSource[]::new);

		// Construct schema objects from the XML schemas
		var schema = SchemaFactory.newDefaultInstance().newSchema(sources);

		// Create a ThreadLocal to construct an XMLValidator for each thread
		this.xmlValidators = ThreadLocal.withInitial( () ->
		{
			// Create a validator and set its error handler
			var validator = schema.newValidator();
			validator.setErrorHandler( new LoggingErrorHandler() );
			return validator;
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
	public ValidationReport validate( Resource.V10 validationRequest, Resource profile, ValidationGateName validationGate ) throws IOException, SAXException, NotDocumentException
	{
		// Validate XML
		var validator = xmlValidators.get();
		validator.validate( new StreamSource( validationRequest.readInputStream() ) );

		// Extract errors from the error handler, then reset the error handler
		var errorHandler = (LoggingErrorHandler) validator.getErrorHandler();
		var errors = errorHandler.getErrors();
		errorHandler.reset();

		// Validate against profile
		var validationReport = validationService.validate( validationRequest, profile, validationGate );
		var validationErrors = errors.stream().map( SchemaViolation::new ).toList();
		return new ValidationReport( validationErrors, validationReport.getConstraintViolations() );
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
