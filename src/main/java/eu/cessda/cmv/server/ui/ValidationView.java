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
import eu.cessda.cmv.server.ui.ResourceSelectionComponent.ProvisioningOptions;
import org.gesis.commons.resource.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static eu.cessda.cmv.server.ui.ResourceSelectionComponent.ProvisioningOptions.BY_PREDEFINED;
import static eu.cessda.cmv.server.ui.ResourceSelectionComponent.ProvisioningOptions.BY_UPLOAD;
import static eu.cessda.cmv.server.ui.ResourceSelectionComponent.SelectionMode.MULTI;
import static eu.cessda.cmv.server.ui.ResourceSelectionComponent.SelectionMode.SINGLE;
import static java.util.Arrays.asList;

@UIScope
@SpringView( name = ValidationView.VIEW_NAME )
@SuppressWarnings( "java:S110" )
public class ValidationView extends VerticalLayout implements View
{
	public static final String VIEW_NAME = "validation";

	@Serial
	private static final long serialVersionUID = -5924926837826583950L;

	public ValidationView( @Autowired ValidatorEngine validationService,
						   @Autowired List<Resource.V10> demoDocuments,
						   @Autowired List<Resource.V10> demoProfiles,
						   @Autowired CessdaMetadataValidatorFactory cessdaMetadataValidatorFactory )
	{
		List<Resource.V10> profileResources = new ArrayList<>();
		List<Resource.V10> documentResources = new ArrayList<>();
		List<ValidationReport> validationReports = new ArrayList<>();

		ComboBox<ValidationGateName> validationGateNameComboBox = new ComboBox<>();
		validationGateNameComboBox.setCaption( "Validation Gate" );
		validationGateNameComboBox.setEmptySelectionAllowed( false );
		validationGateNameComboBox.setItems( ValidationGateName.values() );
		validationGateNameComboBox.setValue( ValidationGateName.BASIC );

		Grid<ValidationReport> validationReportGrid = new Grid<>();
		validationReportGrid.setHeaderVisible( false );
		validationReportGrid.setStyleName( ValoTheme.TABLE_BORDERLESS );
		validationReportGrid.setSizeFull();
		validationReportGrid.setSelectionMode( SelectionMode.NONE );
		validationReportGrid.setRowHeight( 700 );
		validationReportGrid.setItems( validationReports );
		validationReportGrid
				.addColumn( new ValidationReportGridValueProvider( documentResources ), new ComponentRenderer() )
				.setSortable( false )
				.setHandleWidgetEvents( true );

		var reportPanel = new Panel( "Reports", validationReportGrid );

		var validateButton = new Button( "Validate", listener ->
		{
			if ( profileResources.isEmpty() )
			{
				Notification.show( "No profile selected!" );
				return;
			}
			if ( documentResources.isEmpty() )
			{
				Notification.show( "No documents selected!" );
				return;
			}

			validationReports.clear();
			validationReportGrid.getDataProvider().refreshAll();

			var validationExceptions = new ArrayList<Exception>();

			for ( var documentResource : documentResources )
			{
				try
				{
					var validationReport = validationService.validate( documentResource,
							profileResources.get( 0 ),
							validationGateNameComboBox.getSelectedItem().orElseThrow() );
					validationReports.add( validationReport );
				}
				catch ( IOException | SAXException e )
				{
					validationExceptions.add( e );
				}
			}

			if ( !validationExceptions.isEmpty() )
			{
				var validationExceptionString = validationExceptions.stream()
						.map( Exception::toString )
						.collect( Collectors.joining( "/n" ) );
				Notification.show( "Some validations failed", validationExceptionString, Notification.Type.WARNING_MESSAGE );
			}

			validationReportGrid.getDataProvider().refreshAll();
			if ( !documentResources.isEmpty() )
			{
				validationReportGrid.setHeightByRows( documentResources.size() );
			}
			reportPanel.setVisible( true );
		} );

		validationGateNameComboBox.addSelectionListener( listener ->
		{
			validationReports.clear();
			validationReportGrid.getDataProvider().refreshAll();
			reportPanel.setVisible( false );
		} );

		var profileSelection = new ResourceSelectionComponent(
				SINGLE,
				asList( ProvisioningOptions.values() ),
				BY_PREDEFINED,
				demoProfiles,
				profileResources,
				() ->
				{
					validationReports.clear();
					validationReportGrid.getDataProvider().refreshAll();
					reportPanel.setVisible( false );
				},
				cessdaMetadataValidatorFactory );
		profileSelection.setCaption( "Profile" );
		profileSelection.setWidthFull();

		var documentSelection = new ResourceSelectionComponent(
				MULTI,
				asList( ProvisioningOptions.values() ),
				BY_UPLOAD,
				demoDocuments,
				documentResources,
				() ->
				{
					validationReports.clear();
					validationReportGrid.getDataProvider().refreshAll();
					reportPanel.setVisible( false );
				},
				cessdaMetadataValidatorFactory );
		documentSelection.setCaption( "Documents" );
		documentSelection.setWidthFull();

		var configurationFormLayout = new FormLayout();
		configurationFormLayout.setMargin( true );
		configurationFormLayout.addComponent( validationGateNameComboBox );
		configurationFormLayout.addComponent( profileSelection );
		configurationFormLayout.addComponent( documentSelection );
		var configurationPanel = new Panel( "Configuration", configurationFormLayout );

		this.setSizeFull();
		addComponent( configurationPanel );
		addComponent( validateButton );
		addComponent( reportPanel );
	}
}
