/*-
 * #%L
 * CESSDA Metadata Validator
 * %%
 * Copyright (C) 2020 - 2025 CESSDA ERIC
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
package eu.cessda.cmv.server.ui;

import com.vaadin.navigator.View;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.ui.*;
import eu.cessda.cmv.core.CessdaMetadataValidatorFactory;
import eu.cessda.cmv.core.NotDocumentException;
import eu.cessda.cmv.core.ValidationGateName;
import eu.cessda.cmv.server.ValidationReport;
import eu.cessda.cmv.server.ValidatorEngine;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.web.util.HtmlUtils;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.Serial;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static eu.cessda.cmv.server.ui.ResourceSelectionComponent.ProvisioningOptions.BY_PREDEFINED;
import static eu.cessda.cmv.server.ui.ResourceSelectionComponent.ProvisioningOptions.BY_UPLOAD;
import static eu.cessda.cmv.server.ui.ResourceSelectionComponent.SelectionMode.MULTI;
import static eu.cessda.cmv.server.ui.ResourceSelectionComponent.SelectionMode.SINGLE;

@UIScope
@SpringView( name = ValidationView.VIEW_NAME )
@SuppressWarnings( { "java:S110", "java:S1948", "java:S2160" } )
public class ValidationView extends VerticalLayout implements View
{
	private static final Logger log = LoggerFactory.getLogger( ValidationView.class );

	public static final String VIEW_NAME = "validation";

	@Serial
	private static final long serialVersionUID = -5924926837826583950L;

	// Localisation bundle
	private final ResourceBundle bundle;

	// Services
	private final ValidatorEngine validationService;
	private final CessdaMetadataValidatorFactory cessdaMetadataValidatorFactory;

	private final NativeSelect<ValidationGateName> validationGateNameComboBox;
	private final Panel reportPanel;
	private final ResourceSelectionComponent<UIProfile> profileSelectionComponent;
	private final ResourceSelectionComponent<Resource> documentSelectionComponent;
	private final Button validateButton;
	private final Button cancelButton;
	private final ProgressBar progressBar;

	// Computation disposable
	private Disposable subscription;

	public ValidationView( @Autowired ValidatorEngine validationService,
						   @Autowired List<Resource> demoDocuments,
						   @Autowired List<UIProfile> demoProfiles,
						   @Autowired CessdaMetadataValidatorFactory cessdaMetadataValidatorFactory )
	{
		this.validationService = validationService;
		this.cessdaMetadataValidatorFactory = cessdaMetadataValidatorFactory;

		this.bundle = ResourceBundle.getBundle( ValidationView.class.getName(), UI.getCurrent().getLocale() );

		this.validationGateNameComboBox = new NativeSelect<>();
		this.validationGateNameComboBox.setCaption( bundle.getString( "configuration.validationGate" ) );
		this.validationGateNameComboBox.setEmptySelectionAllowed( false );
		this.validationGateNameComboBox.setItems( ValidationGateName.values() );
		this.validationGateNameComboBox.setValue( ValidationGateName.BASIC );

		this.validateButton = new Button( bundle.getString( "validate.button" ), listener -> validate() );

		this.cancelButton = new Button("Cancel", listener -> subscription.dispose());
		this.cancelButton.setVisible( false );

		this.progressBar = new ProgressBar();
		this.progressBar.setVisible( false );

		var validateLayout = new HorizontalLayout( this.validateButton, this.progressBar, this.cancelButton );
		validateLayout.setStyleName( "validate-button-layout" );

		this.reportPanel = new Panel( bundle.getString( "report.panel.caption" ) );
		this.reportPanel.setVisible( false );

		this.profileSelectionComponent = new ResourceSelectionComponent<>(
                SINGLE,
                BY_PREDEFINED,
                demoProfiles,
				ValidationView::getProfileDescription,
                this::parseProfile
		);
		this.profileSelectionComponent.setCaption( bundle.getString( "configuration.profileSelectionCaption" ) );
		this.profileSelectionComponent.setWidthFull();

		this.documentSelectionComponent = new ResourceSelectionComponent<>(
                MULTI,
                BY_UPLOAD,
                demoDocuments,
				Resource::getFilename,
                ValidationView::labelFromResource,
                this::recognizeDdiDocument
		);
		this.documentSelectionComponent.setCaption( bundle.getString( "configuration.documentSelectionCaption" ) );
		this.documentSelectionComponent.setWidthFull();

		var configurationFormLayout = new FormLayout();
		configurationFormLayout.setMargin( true );
		configurationFormLayout.addComponent( validationGateNameComboBox );
		configurationFormLayout.addComponent( profileSelectionComponent );
		configurationFormLayout.addComponent( documentSelectionComponent );
		var configurationPanel = new Panel( bundle.getString( "configuration.configurationBoxCaption" ), configurationFormLayout );

		this.setSizeFull();
		addComponent( configurationPanel );
		addComponent( validateLayout );
		addComponent( reportPanel );
	}

	/**
	 * Get a description of a profile.
	 */
	private static String getProfileDescription( UIProfile uiProfile ) {
		// Extract the profile's name and version if present
		var profile = uiProfile.profile();
		if (profile.getProfileName() != null) {
			if (profile.getProfileVersion() != null) {
				return profile.getProfileName() + ": " + profile.getProfileVersion();
			} else {
				return profile.getProfileName();
			}
		} else {
			var resource = uiProfile.resource();
			if ( resource.getFilename() != null )
			{
				// Extract the filename
				return resource.getFilename();
			}
			else
			{
				// Return the description
				return resource.getDescription();
			}
		}
	}

	private static Label labelFromResource( Resource resource )
	{
		Label label = new Label();
		if (resource instanceof UrlResource urlResource )
		{
			var url = urlResource.getURL();
			if ( url.getProtocol().startsWith( "http" ) )
			{
				label.setContentMode( ContentMode.HTML );
				label.setValue( "<a href='" + url + "' target='_blank'>" + HtmlUtils.htmlEscape( url.toString(), "UTF-8" ) + "</a>" );
			}
			else if (url.getProtocol().equals( "file" ) || url.getProtocol().equals( "jar" ))
			{
				label.setValue( urlResource.getFilename() );
			}
			else
			{
				label.setValue( url.toString() );
			}
		}
		else if ( resource.getFilename() != null )
		{
			// Extract the filename
			label.setValue( resource.getFilename() );
		}
		else
		{
			// Fallback in case an unexpected resource time is provided
			label.setValue( resource.getDescription() );
		}
		return label;
	}

	private Optional<org.springframework.core.io.Resource> recognizeDdiDocument( org.springframework.core.io.Resource resource )
	{
		try( var inputStream = resource.getInputStream() )
		{
			if ( resource instanceof InputStreamResource inputStreamResource )
			{
				// An InputStreamResource can only be used once, copy to a ByteArrayResource which can be used multiple times
				resource = new ByteArrayResource( inputStreamResource.getInputStream().readAllBytes() );
			}

			// TODO Avoid inefficiency of calling newDocument only to check if document is accepted
			cessdaMetadataValidatorFactory.newDocument( inputStream );
			return Optional.of( resource );
		}
		catch ( IOException | NotDocumentException e)
		{
			Notification.show( e.getMessage(), Notification.Type.WARNING_MESSAGE );
			return Optional.empty();
		}
	}

	private Optional<UIProfile> parseProfile( org.springframework.core.io.Resource resource )
	{
		try(var inputStream = resource.getInputStream())
		{
			var profile = cessdaMetadataValidatorFactory.newProfile( inputStream );
			return Optional.of( new UIProfile( profile, resource ) );
		}
		catch ( NotDocumentException | IOException e )
		{
			// Profile couldn't be parsed - warn the user
			log.warn( "Parsing profile \"{}\" failed", resource, e );
			Notification.show( this.bundle.getString("validate.profileError"), e.getMessage(), Notification.Type.ERROR_MESSAGE );
			return Optional.empty();
		}
	}

	/**
	 * Run the validation using the currently selected profile and documents.
	 *
	 * @implNote This method is synchronized to ensure that only one validation can occur per UI instance.
	 */
	@SuppressWarnings( { "java:S3958", "OverlyBroadCatchBlock" } )
	private synchronized void validate()
	{
		var profileResources = this.profileSelectionComponent.getResources();
		var documentResources = this.documentSelectionComponent.getResources();

		if ( profileResources.isEmpty() )
		{
			Notification.show( this.bundle.getString("validate.noProfileSelected") );
			return;
		}
		if ( documentResources.isEmpty() )
		{
			Notification.show( this.bundle.getString("validate.noDocumentsSelected") );
			return;
		}

		// Enable the progress bar
		this.progressBar.setVisible( true );

		// Disable the validation button whilst validation is running
		this.validateButton.setEnabled( false );

		// Get the validation gate and validation profile to be used
		var validationGate = this.validationGateNameComboBox.getValue();
		var uiProfile = profileResources.getFirst();

		// Container for any validation errors encountered
		var validationExceptions = new ConcurrentHashMap<Resource, Throwable>(0);

		var documentsValidated = new AtomicInteger();

		subscription = Flowable.fromIterable( documentResources )
			.parallel()
			.runOn( Schedulers.computation() )
			// Validate each document
			.mapOptional( documentResource ->
			{
				log.warn( Thread.currentThread().getName() );
				try ( var inputStream = documentResource.getInputStream() )
				{
					var validationReport = this.validationService.validate( inputStream, uiProfile.profile(), validationGate );
					return Optional.of(Map.entry( documentResource, validationReport ));
				}
				catch ( IOException | SAXException | NotDocumentException e )
				{
					// Report all exceptions encountered to the user
					log.warn( "Validation of \"{}\" failed", documentResource, e );
					validationExceptions.put( documentResource, e );
					return Optional.empty();
				}
				finally
				{
					// Update the progress bar
					this.getUI().access( () ->
					{
						var progress = (float) documentsValidated.incrementAndGet() / documentResources.size();
						this.progressBar.setValue( progress );
					} );
				}
			})
			// Collect validation results into a map
			.sequential().toMap( Map.Entry::getKey, Map.Entry::getValue )
			// Re-enable the validate button regardless if any exceptions were encountered
			.doFinally( this::resetPostValidation )
			.subscribe(
				completedList -> this.getUI().access( () -> updateView( completedList, validationExceptions ) ),
				e ->
				{
					log.error( "Unexpected error when validating documents: {}", e, e );
					this.getUI().access( () ->
					{
						var errorWindow = new ErrorWindow( e );
						UI.getCurrent().addWindow( errorWindow );
						updateView( Collections.emptyMap(), validationExceptions );
					} );
				});

		// Enable the cancel button
		this.cancelButton.setVisible( true );
	}

	private void updateView( Map<Resource, ValidationReport> validationReportMap, Map<Resource, Throwable> validationExceptions )
	{
		// If any errors were encountered, present them to the user
		if ( !validationExceptions.isEmpty() )
		{
			var validationExceptionStringJoiner = new StringJoiner( "/n" );
			for ( Map.Entry<Resource, Throwable> e : validationExceptions.entrySet() )
			{
				validationExceptionStringJoiner.add( e.getKey().getFilename() + ": " + e.getValue().getMessage() );
			}
			var validationExceptionString = validationExceptionStringJoiner.toString();
			Notification.show( this.bundle.getString("validate.validationErrors"), validationExceptionString, Notification.Type.ERROR_MESSAGE );
		}

		// Update the UI with the validation reports
		var reportComponent = new ResultsComponent( validationReportMap );
		this.reportPanel.setContent( reportComponent );
		this.reportPanel.setVisible( true );
	}

	/**
	 * Reset the progress bar and the validation button.
	 */
	private void resetPostValidation()
	{
		// Reset the state of the progress bar
		progressBar.setValue( 0 );
		progressBar.setVisible( false );

		// Enable the validation button again
		validateButton.setEnabled( true );
		cancelButton.setVisible( false );
	}

	@Override
	public void detach()
	{
		// Terminate any running validations
		subscription.dispose();
		super.detach();
	}
}
