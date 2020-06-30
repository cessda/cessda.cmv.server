package eu.cessda.cmv.server;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.gesis.commons.resource.Resource;
import org.gesis.commons.resource.TextResource;

import com.vaadin.navigator.View;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

import eu.cessda.cmv.core.ValidationGateName;

@UIScope
@SpringView( name = ValidationView.VIEW_NAME )
public class ValidationView extends VerticalLayout implements View
{
	public static final String VIEW_NAME = "validation";

	private static final long serialVersionUID = -5924926837826583950L;

	enum ProvisioningOptions
	{
		BY_PREDEFINED,
		BY_URL,
		BY_UPLOAD
	}

	@PostConstruct
	public void init()
	{
		setSizeFull();
		List<Resource> documents = new ArrayList<>();
		Button validateButton = new Button( "Validate" );
		validateButton.addClickListener( listener ->
		{
			documents.forEach( resource ->
			{
				System.out.println( new TextResource( resource ).toString() );
			} );
		} );
		addComponent( newConfigurationPanel( documents ) );
		addComponent( validateButton );
	}

	private Panel newConfigurationPanel( List<Resource> documents )
	{
		ComboBox<ValidationGateName> validationGateNameComboBox = new ComboBox<>();
		validationGateNameComboBox.setItems( ValidationGateName.values() );
		validationGateNameComboBox.setCaption( "Validation Gate" );
		validationGateNameComboBox.setValue( ValidationGateName.BASIC );

		ResourceSelectionComponent profileSelection = new ResourceSelectionComponent();
		profileSelection.setCaption( "Profile" );

		ResourceSelectionComponent documentSelection = new ResourceSelectionComponent( documents );
		documentSelection.setCaption( "Documents" );

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
