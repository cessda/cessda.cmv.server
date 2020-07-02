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
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

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
		setSizeFull();
		List<Resource> profileResources = new ArrayList<>();
		List<Resource> documentResources = new ArrayList<>();
		addComponent( newConfigurationPanel( profileResources, documentResources ) );
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
			documentResources.forEach( documentResource ->
			{
				StringBuilder stringBuilder = new StringBuilder();
				ValidationReportV0 validationReportV0 = validationService.validate( documentResource,
						profileResources.get( 0 ),
						ValidationGateName.BASIC );
				validationReportV0.getConstraintViolations().forEach( constraintViolation ->
				{
					stringBuilder.append( constraintViolation.getMessage() ).append( System.lineSeparator() );
				} );
				Notification.show( stringBuilder.toString() );
			} );
		} );
		addComponent( validateButton );
	}

	private Panel newConfigurationPanel( List<Resource> profiles, List<Resource> documents )
	{
		ComboBox<ValidationGateName> validationGateNameComboBox = new ComboBox<>();
		validationGateNameComboBox.setCaption( "Validation Gate" );
		validationGateNameComboBox.setEmptySelectionAllowed( false );
		validationGateNameComboBox.setItems( ValidationGateName.values() );
		validationGateNameComboBox.setValue( ValidationGateName.BASIC );

		ResourceSelectionComponent profileSelection = new ResourceSelectionComponent(
				SINGLE, asList( ProvisioningOptions.values() ), BY_PREDEFINED, profiles );
		profileSelection.setCaption( "Profile" );
		profileSelection.setWidthFull();

		ResourceSelectionComponent documentSelection = new ResourceSelectionComponent(
				MULTI, asList( BY_URL, BY_UPLOAD ), BY_UPLOAD, documents );
		documentSelection.setCaption( "Documents" );
		documentSelection.setWidthFull();

		FormLayout formLayout = new FormLayout();
		formLayout.setMargin( true );
		formLayout.addComponent( validationGateNameComboBox );
		formLayout.addComponent( profileSelection );
		formLayout.addComponent( documentSelection );

		Panel configurationPanel = new Panel( "Configuration" );
		configurationPanel.setContent( formLayout );
		return configurationPanel;
	}
}
