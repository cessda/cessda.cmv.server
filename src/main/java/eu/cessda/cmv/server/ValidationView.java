package eu.cessda.cmv.server;

import static eu.cessda.cmv.server.ResourceSelectionComponent.ProvisioningOptions.BY_PREDEFINED;
import static eu.cessda.cmv.server.ResourceSelectionComponent.ProvisioningOptions.BY_UPLOAD;
import static eu.cessda.cmv.server.ResourceSelectionComponent.ProvisioningOptions.BY_URL;
import static eu.cessda.cmv.server.ResourceSelectionComponent.SelectionMode.MULTI;
import static eu.cessda.cmv.server.ResourceSelectionComponent.SelectionMode.SINGLE;
import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.List;

import org.gesis.commons.resource.Resource;
import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.navigator.View;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.renderers.ComponentRenderer;
import com.vaadin.ui.themes.ValoTheme;

import eu.cessda.cmv.core.ValidationGateName;
import eu.cessda.cmv.core.ValidationService;
import eu.cessda.cmv.core.mediatype.validationreport.v0.ValidationReportV0;
import eu.cessda.cmv.server.ResourceSelectionComponent.ProvisioningOptions;

@UIScope
@SpringView( name = ValidationView.VIEW_NAME )
public class ValidationView extends VerticalLayout implements View
{
	public static final String VIEW_NAME = "validation";

	private static final long serialVersionUID = -5924926837826583950L;

	public ValidationView( @Autowired ValidationService.V10 validationService )
	{
		List<Resource> profileResources = new ArrayList<>();
		List<Resource> documentResources = new ArrayList<>();
		List<ValidationReportV0> validationReports = new ArrayList<>();

		ComboBox<ValidationGateName> validationGateNameComboBox = new ComboBox<>();
		validationGateNameComboBox.setCaption( "Validation Gate" );
		validationGateNameComboBox.setEmptySelectionAllowed( false );
		validationGateNameComboBox.setItems( ValidationGateName.values() );
		validationGateNameComboBox.setValue( ValidationGateName.BASIC );

		Grid<ValidationReportV0> validationReportGrid = new Grid<>();
		validationReportGrid.setHeaderVisible( false );
		validationReportGrid.setStyleName( ValoTheme.TABLE_BORDERLESS );
		validationReportGrid.setSizeFull();
		validationReportGrid.setSelectionMode( SelectionMode.NONE );
		validationReportGrid.setRowHeight( 500 );
		validationReportGrid.setItems( validationReports );
		validationReportGrid.addColumn( new ValidationReportGridValueProvider(), new ComponentRenderer() )
				.setSortable( false )
				.setHandleWidgetEvents( true );

		Panel reportPanel = new Panel( "Reports" );
		reportPanel.setContent( validationReportGrid );

		Button validateButton = new Button( "Validate" );
		validateButton.addClickListener( listener ->
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
			documentResources.forEach( documentResource ->
			{
				ValidationReportV0 validationReport = validationService.validate( documentResource,
						profileResources.get( 0 ),
						ValidationGateName.STRICT );
				validationReports.add( validationReport );
			} );
			validationReportGrid.getDataProvider().refreshAll();
			if ( !documentResources.isEmpty() )
			{
				validationReportGrid.setHeightByRows( documentResources.size() );
			}
			reportPanel.setVisible( true );
		} );

		Runnable refreshReportPanel = () ->
		{
			validationReports.clear();
			validationReportGrid.getDataProvider().refreshAll();
			reportPanel.setVisible( false );
		};

		validationGateNameComboBox.addSelectionListener( listener -> refreshReportPanel.run() );

		ResourceSelectionComponent profileSelection = new ResourceSelectionComponent(
				SINGLE,
				asList( ProvisioningOptions.values() ),
				BY_PREDEFINED,
				profileResources,
				refreshReportPanel );
		profileSelection.setCaption( "Profile" );
		profileSelection.setWidthFull();

		ResourceSelectionComponent documentSelection = new ResourceSelectionComponent(
				MULTI,
				asList( BY_URL, BY_UPLOAD ),
				BY_UPLOAD,
				documentResources,
				refreshReportPanel );
		documentSelection.setCaption( "Documents" );
		documentSelection.setWidthFull();

		FormLayout configurationFormLayout = new FormLayout();
		configurationFormLayout.setMargin( true );
		configurationFormLayout.addComponent( validationGateNameComboBox );
		configurationFormLayout.addComponent( profileSelection );
		configurationFormLayout.addComponent( documentSelection );
		Panel configurationPanel = new Panel( "Configuration" );
		configurationPanel.setContent( configurationFormLayout );

		setSizeFull();
		addComponent( configurationPanel );
		addComponent( validateButton );
		addComponent( reportPanel );
	}
}
