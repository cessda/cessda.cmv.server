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
package eu.cessda.cmv.server.ui;

import com.vaadin.navigator.View;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.ui.*;
import eu.cessda.cmv.core.CessdaMetadataValidatorFactory;
import eu.cessda.cmv.core.ValidationGateName;
import eu.cessda.cmv.server.ValidationReport;
import eu.cessda.cmv.server.ValidatorEngine;
import org.gesis.commons.resource.Resource;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serial;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
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
	public static final String VIEW_NAME = "validation";

	@Serial
	private static final long serialVersionUID = -5924926837826583950L;

	// Localisation bundle
	private final ResourceBundle bundle;
	private final ValidatorEngine validationService;

	private final ComboBox<ValidationGateName> validationGateNameComboBox;
	private final Panel reportPanel;
	private final ResourceSelectionComponent profileSelectionComponent;
	private final ResourceSelectionComponent documentSelectionComponent;
	private final Button validateButton;
	private final ProgressBar progressBar;


	public ValidationView( @Autowired ValidatorEngine validationService,
						   @Autowired List<Resource.V10> demoDocuments,
						   @Autowired List<Resource.V10> demoProfiles,
						   @Autowired CessdaMetadataValidatorFactory cessdaMetadataValidatorFactory )
	{
		this.validationService = validationService;

		this.bundle = ResourceBundle.getBundle( ValidationView.class.getName(), UI.getCurrent().getLocale() );

		this.validationGateNameComboBox = new ComboBox<>();
		this.validationGateNameComboBox.setCaption( bundle.getString( "configuration.validationGate" ) );
		this.validationGateNameComboBox.setEmptySelectionAllowed( false );
		this.validationGateNameComboBox.setItems( ValidationGateName.values() );
		this.validationGateNameComboBox.setValue( ValidationGateName.BASIC );

		this.validateButton = new Button( bundle.getString( "validate.button" ), listener -> validate() );

		this.progressBar = new ProgressBar();
		this.progressBar.setVisible( false );

		var validateLayout = new HorizontalLayout( validateButton, this.progressBar );
		validateLayout.setStyleName( "validate-button-layout" );

		this.reportPanel = new Panel( bundle.getString( "report.panel.caption" ) );
		this.reportPanel.setVisible( false );

		this.profileSelectionComponent = new ResourceSelectionComponent(
			SINGLE,
			BY_PREDEFINED,
			demoProfiles,
			cessdaMetadataValidatorFactory );
		this.profileSelectionComponent.setCaption( bundle.getString( "configuration.profileSelectionCaption" ) );
		this.profileSelectionComponent.setWidthFull();

		this.documentSelectionComponent = new ResourceSelectionComponent(
			MULTI,
			BY_UPLOAD,
			demoDocuments,
			cessdaMetadataValidatorFactory );
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
		var validationExceptions = new ConcurrentHashMap<String, Exception>(0);

		var documentsValidated = new AtomicInteger();
		int documentsToValidate = documentResources.size();

		// Validate all documents using a parallel stream
		var validationReportList = documentResources.parallelStream().flatMap( documentResource ->
		{
			try
			{
				var validationReport = this.validationService.validate( documentResource, profile, validationGate );
				return Stream.of( Map.entry( documentResource.getLabel(), validationReport ) );
			}
			catch ( Exception e )
			{
				// Report all exceptions encountered to the user
				validationExceptions.put( documentResource.getLabel(), e );
				return Stream.empty();
			}
			finally
			{
				// Update the progress bar
				this.getUI().access( () -> this.progressBar.setValue( (float) documentsValidated.incrementAndGet() / documentsToValidate ) );
			}
		} );

		CompletableFuture.runAsync( () ->
		{
			// Accumulate the stream in an asynchronous context so that the UI is not blocked
			var completedList = validationReportList.collect( Collectors.toMap( Map.Entry::getKey, Map.Entry::getValue ) );
			this.getUI().access( () -> updateView( completedList, validationExceptions ) );
		} ).whenComplete(
			// Re-enable the button regardless if any exceptions were encountered
			(v, e) -> this.getUI().access( this::resetPostValidation )
		);
	}

	private void updateView( Map<String, ValidationReport> validationReportList, Map<String, Exception> validationExceptions )
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
