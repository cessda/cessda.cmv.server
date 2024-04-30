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
package eu.cessda.cmv.server.ui;

import com.vaadin.navigator.View;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.ui.*;
import eu.cessda.cmv.core.CessdaMetadataValidatorFactory;
import eu.cessda.cmv.core.NotDocumentException;
import eu.cessda.cmv.core.Profile;
import eu.cessda.cmv.core.ValidationGateName;
import eu.cessda.cmv.server.ValidationReport;
import eu.cessda.cmv.server.ValidatorEngine;
import org.gesis.commons.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.util.HtmlUtils;

import java.io.IOException;
import java.io.Serial;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
	private final ResourceSelectionComponent<Profile> profileSelectionComponent;
	private final ResourceSelectionComponent<Resource.V10> documentSelectionComponent;
	private final Button validateButton;
	private final ProgressBar progressBar;


	public ValidationView( @Autowired ValidatorEngine validationService,
						   @Autowired List<Resource.V10> demoDocuments,
						   @Autowired List<Profile> demoProfiles,
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

		this.progressBar = new ProgressBar();
		this.progressBar.setVisible( false );

		var validateLayout = new HorizontalLayout( this.validateButton, this.progressBar );
		validateLayout.setStyleName( "validate-button-layout" );

		this.reportPanel = new Panel( bundle.getString( "report.panel.caption" ) );
		this.reportPanel.setVisible( false );

		this.profileSelectionComponent = new ResourceSelectionComponent<>(
                SINGLE,
                BY_PREDEFINED,
                demoProfiles,
                profile -> new Label( profile.getProfileName() + ": " + profile.getProfileVersion() ),
                this::parseProfile
		);
		this.profileSelectionComponent.setCaption( bundle.getString( "configuration.profileSelectionCaption" ) );
		this.profileSelectionComponent.setWidthFull();

		this.documentSelectionComponent = new ResourceSelectionComponent<>(
                MULTI,
                BY_UPLOAD,
                demoDocuments,
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

	private static Label labelFromResource( Resource.V10 resource )
	{
		Label label = new Label();
		if ( resource.getUri().getScheme().startsWith( "http" ) )
		{
			label.setContentMode( ContentMode.HTML );
			label.setValue( "<a href='" + resource.getUri() + "' target='_blank'>" + HtmlUtils.htmlEscape( resource.getLabel(), "UTF-8" ) + "</a>" );
		}
		else
		{
			label.setValue( resource.getLabel() );
		}
		return label;
	}

	private <T extends Resource> Optional<T> recognizeDdiDocument( T resource )
	{
		try( var inputStream = resource.readInputStream() )
		{
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

	private Optional<Profile> parseProfile( Resource resource )
	{
		try
		{
			var profile = cessdaMetadataValidatorFactory.newProfile( resource.getUri() );
			return Optional.of( profile );
		}
		catch ( NotDocumentException | IOException e )
		{
			// Profile couldn't be parsed - warn the user
			var resourceLabel = resource instanceof Resource.V10  ? ( (Resource.V10) resource ).getLabel() : resource.getUri();
			log.warn( "Parsing profile {} failed: {}", resourceLabel, e.toString() );
			Notification.show( this.bundle.getString("validate.profileError"), e.getMessage(), Notification.Type.WARNING_MESSAGE );
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
		var profile = profileResources.get( 0 );

		// Container for any validation errors encountered
		var validationExceptions = new ConcurrentHashMap<String, Throwable>(0);

		var documentsValidated = new AtomicInteger();

		// Validate all documents using a parallel stream
		var validationReportList = documentResources.parallelStream().flatMap( documentResource ->
		{
			try ( var inputStream = documentResource.readInputStream() )
			{
				var validationReport = this.validationService.validate( inputStream, profile, validationGate );
				return Stream.of( Map.entry( documentResource.getLabel(), validationReport ) );
			}
			catch ( Exception e )
			{
				// Report all exceptions encountered to the user
				log.warn( "Validation of {} failed", documentResource.getLabel(), e );
				validationExceptions.put( documentResource.getLabel(), e );
				return Stream.empty();
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
		} );

		CompletableFuture.runAsync( () ->
		{
			// Accumulate the stream in an asynchronous context so that the UI is not blocked
			var completedList = validationReportList.collect( Collectors.toMap( Map.Entry::getKey, Map.Entry::getValue ) );
			this.getUI().access( () -> updateView( completedList, validationExceptions ) );
		} ).whenComplete(
			(v, e) ->
            {
				// If an unexpected exception was thrown, handle it here and report it to the user
				if (e != null) {
					log.error( "Unexpected error when validating documents: {}", e, e );
					this.getUI().access( () ->
                    {
						var errorWindow = new ErrorWindow( e );
						UI.getCurrent().addWindow( errorWindow );
                        updateView( Collections.emptyMap(), validationExceptions );
                    } );
				}

				// Re-enable the button regardless if any exceptions were encountered
                this.getUI().access( this::resetPostValidation );
            }
		);
	}

	private void updateView( Map<String, ValidationReport> validationReportList, Map<String, Throwable> validationExceptions )
	{
		// If any errors were encountered, present them to the user
		if ( !validationExceptions.isEmpty() )
		{
			var validationExceptionString = validationExceptions.entrySet().stream()
					.map( e -> e.getKey() + ": " + e.getValue() )
					.collect( Collectors.joining( "/n" ) );
			Notification.show( this.bundle.getString("validate.validationErrors"), validationExceptionString, Notification.Type.WARNING_MESSAGE );
		}

		// Update the UI with the validation reports
		var reportComponent = new ResultsComponent( validationReportList );
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
	}
}
