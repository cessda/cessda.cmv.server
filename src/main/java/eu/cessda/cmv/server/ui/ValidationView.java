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
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.renderers.ComponentRenderer;
import com.vaadin.ui.themes.ValoTheme;
import eu.cessda.cmv.core.CessdaMetadataValidatorFactory;
import eu.cessda.cmv.core.ValidationGateName;
import eu.cessda.cmv.server.ValidationReport;
import eu.cessda.cmv.server.ValidatorEngine;
import org.gesis.commons.resource.Resource;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serial;
import java.util.ArrayList;
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
@SuppressWarnings( "java:S110" )
public class ValidationView extends VerticalLayout implements View
{
	public static final String VIEW_NAME = "validation";

	@Serial
	private static final long serialVersionUID = -5924926837826583950L;

	private final ResourceBundle bundle;
	private final ArrayList<ValidationReport> validationReports = new ArrayList<>();
	private final ComboBox<ValidationGateName> validationGateNameComboBox;
	private final Grid<ValidationReport> validationReportGrid;
	private final Panel reportPanel;
	private final ValidatorEngine validationService;
	private final ResourceSelectionComponent profileSelectionComponent;
	private final ResourceSelectionComponent documentSelectionComponent;
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

		var validationReportLabel = new Label( bundle.getString( "report.label" ) );

		this.validationReportGrid = new Grid<>();
		this.validationReportGrid.setHeaderVisible( false );
		this.validationReportGrid.setStyleName( ValoTheme.TABLE_BORDERLESS );
		this.validationReportGrid.setSizeFull();
		this.validationReportGrid.setSelectionMode( SelectionMode.NONE );
		this.validationReportGrid.setRowHeight( 700 );
		this.validationReportGrid.setItems( validationReports );
		this.validationReportGrid
				.addColumn( new ValidationReportGridValueProvider(), new ComponentRenderer() )
				.setSortable( false )
				.setHandleWidgetEvents( true );

		var validationReportLayout = new VerticalLayout();
		validationReportLayout.addComponent( validationReportLabel );
		validationReportLayout.addComponent( this.validationReportGrid );

		var validateButton = new Button( bundle.getString( "validate.button" ), listener -> validate() );

		this.progressBar = new ProgressBar();
		this.progressBar.setVisible( false );

		var validateLayout = new HorizontalLayout( validateButton, this.progressBar );
		validateLayout.setStyleName( "validate-button-layout" );

		this.reportPanel = new Panel( bundle.getString( "report.panel.caption" ), validationReportLayout );

		this.profileSelectionComponent = new ResourceSelectionComponent(
			SINGLE,
			BY_PREDEFINED,
			demoProfiles,
			this::refresh,
			cessdaMetadataValidatorFactory );
		this.profileSelectionComponent.setCaption( bundle.getString( "configuration.profileSelectionCaption" ) );
		this.profileSelectionComponent.setWidthFull();

		this.documentSelectionComponent = new ResourceSelectionComponent(
			MULTI,
			BY_UPLOAD,
			demoDocuments,
			this::refresh,
			cessdaMetadataValidatorFactory );
		this.documentSelectionComponent.setCaption( bundle.getString( "configuration.documentSelectionCaption" ) );
		this.documentSelectionComponent.setWidthFull();

		this.validationGateNameComboBox.addSelectionListener( listener -> refresh() );

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

	private void validate()
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

		// Refresh the view before proceeding
		refresh();

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
				return Stream.of( validationReport );
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

		// Enable the progress bar
		this.progressBar.setVisible( true );

		CompletableFuture.supplyAsync( validationReportList::toList )
			.thenAcceptAsync( completedList -> this.getUI().access( () -> updateView( completedList, validationExceptions ) ) );
	}

	private void updateView( List<ValidationReport> validationReportList, Map<String, Exception> validationExceptions )
	{
		// Copy the validation reports into the UI's list
		this.validationReports.addAll( validationReportList );

		// If any errors were encountered, present them to the user
		if ( !validationExceptions.isEmpty() )
		{
			var validationExceptionString = validationExceptions.entrySet().stream()
					.map( e -> e.getKey() + ": " + e.getValue() )
					.collect( Collectors.joining( "/n" ) );
			Notification.show( this.bundle.getString("validate.validationErrors"), validationExceptionString, Notification.Type.WARNING_MESSAGE );
		}

		// Update the UI with the validation reports
		this.validationReportGrid.getDataProvider().refreshAll();
		if ( !this.validationReports.isEmpty() )
		{
			this.validationReportGrid.setHeightByRows( this.validationReports.size() );
		}
		this.reportPanel.setVisible( true );
		this.progressBar.setValue( 0 );
		this.progressBar.setVisible( false );
	}

	private void refresh()
	{
		validationReports.clear();
		validationReportGrid.getDataProvider().refreshAll();
		reportPanel.setVisible( false );
		progressBar.setValue( 0 );
		progressBar.setVisible( false );
	}
}
