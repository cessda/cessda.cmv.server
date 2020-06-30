package eu.cessda.cmv.server;

import static com.vaadin.ui.Grid.SelectionMode.NONE;
import static com.vaadin.ui.themes.ValoTheme.OPTIONGROUP_HORIZONTAL;
import static eu.cessda.cmv.server.ValidationView.ProvisioningOptions.BY_PREDEFINED;
import static eu.cessda.cmv.server.ValidationView.ProvisioningOptions.BY_UPLOAD;
import static eu.cessda.cmv.server.ValidationView.ProvisioningOptions.BY_URL;

import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.gesis.commons.resource.InMemoryResource;
import org.gesis.commons.resource.TextResource;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.navigator.View;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Panel;
import com.vaadin.ui.RadioButtonGroup;
import com.vaadin.ui.VerticalLayout;
import com.wcs.wcslib.vaadin.widget.multifileupload.ui.MultiFileUpload;
import com.wcs.wcslib.vaadin.widget.multifileupload.ui.UploadFinishedHandler;
import com.wcs.wcslib.vaadin.widget.multifileupload.ui.UploadStateWindow;

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

		ComboBox<ValidationGateName> validationGateNameComboBox = new ComboBox<>();
		validationGateNameComboBox.setItems( ValidationGateName.values() );
		validationGateNameComboBox.setCaption( "Validation Gate" );
		validationGateNameComboBox.setValue( ValidationGateName.BASIC );

		RadioButtonGroup<ProvisioningOptions> profileRadioButtonGroup = new RadioButtonGroup<>( "Profile" );
		profileRadioButtonGroup.setItems( ProvisioningOptions.values() );
		profileRadioButtonGroup.addStyleName( OPTIONGROUP_HORIZONTAL );
		profileRadioButtonGroup.setValue( BY_PREDEFINED );

		Grid<InMemoryResource> documentsGrid = new Grid<InMemoryResource>();
		documentsGrid.addColumn( InMemoryResource::getUri ).setCaption( "URI" );
		documentsGrid.setSelectionMode( NONE );
		documentsGrid.setWidthFull();
		documentsGrid.setHeightMode( HeightMode.ROW );
		documentsGrid.setVisible( false );
		while (documentsGrid.getHeaderRowCount() > 0)
		{
			documentsGrid.removeHeaderRow( 0 );
		}

		List<InMemoryResource> validations = new ArrayList<>();
		Button clearButton = new Button( "Clear" );
		clearButton.setVisible( false );
		clearButton.addClickListener( listener ->
		{
			validations.clear();
			documentsGrid.getDataProvider().refreshAll();
			documentsGrid.setVisible( false );
			clearButton.setVisible( false );
		} );

		documentsGrid.setItems( validations );
		UploadFinishedHandler uploadFinishedHandler = (
				InputStream inputStream,
				String fileName,
				String mimeType,
				long length,
				int filesLeftInQueue ) ->
		{
			validations.add( new InMemoryResource( URI.create( fileName ), inputStream ) );
			documentsGrid.getDataProvider().refreshAll();
			documentsGrid.setVisible( true );
			documentsGrid.setHeightByRows( validations.size() );
			clearButton.setVisible( true );
		};

		UploadStateWindow uploadStateWindow = new UploadStateWindow();
		uploadStateWindow.setWindowPosition( UploadStateWindow.WindowPosition.CENTER );
		uploadStateWindow.setOverallProgressVisible( true );
		uploadStateWindow.setResizable( true );

		MultiFileUpload multiFileUpload = new MultiFileUpload( uploadFinishedHandler, uploadStateWindow );
		multiFileUpload.setMaxFileSize( 100_000_000 );
		multiFileUpload.setSizeErrorMsgPattern( "File is too big (max = {0}): {2} ({1})" );
		multiFileUpload.setPanelCaption( "Documents" );
		multiFileUpload.setMaxFileCount( 100 );
		multiFileUpload.getSmartUpload().setUploadButtonCaptions( "Upload File", "Upload Files" );
		multiFileUpload.getSmartUpload().setUploadButtonIcon( VaadinIcons.UPLOAD );

		RadioButtonGroup<ProvisioningOptions> documentsRadioButtonGroup = new RadioButtonGroup<>( "Documents" );
		documentsRadioButtonGroup.setItems( BY_URL, BY_UPLOAD );
		documentsRadioButtonGroup.addStyleName( OPTIONGROUP_HORIZONTAL );
		documentsRadioButtonGroup.addSelectionListener( listener ->
		{
			boolean isVisible = listener.getValue().equals( BY_UPLOAD );
			multiFileUpload.setVisible( isVisible );
		} );
		documentsRadioButtonGroup.setValue( BY_UPLOAD );

		FormLayout formLayout = new FormLayout();
		formLayout.setMargin( true );
		formLayout.addComponent( validationGateNameComboBox );
		formLayout.addComponent( profileRadioButtonGroup );
		formLayout.addComponent( documentsRadioButtonGroup );
		formLayout.addComponent( multiFileUpload );
		formLayout.addComponent( documentsGrid );
		formLayout.addComponent( clearButton );

		Panel configurationPanel = new Panel( "Configuration" );
		configurationPanel.setContent( formLayout );

		Button validateButton = new Button( "Validate" );
		validateButton.addClickListener( listener ->
		{
			validations.forEach( resource ->
			{
				System.out.println( new TextResource( resource ).toString() );
			} );
		} );

		addComponent( configurationPanel );
		addComponent( validateButton );
	}
}
