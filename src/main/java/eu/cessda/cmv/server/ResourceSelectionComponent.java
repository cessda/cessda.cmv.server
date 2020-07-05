package eu.cessda.cmv.server;

import static com.vaadin.shared.ui.grid.HeightMode.ROW;
import static com.vaadin.ui.Grid.SelectionMode.NONE;
import static com.vaadin.ui.themes.ValoTheme.OPTIONGROUP_HORIZONTAL;
import static eu.cessda.cmv.server.ResourceSelectionComponent.ProvisioningOptions.BY_PREDEFINED;
import static eu.cessda.cmv.server.ResourceSelectionComponent.ProvisioningOptions.BY_UPLOAD;
import static eu.cessda.cmv.server.ResourceSelectionComponent.ProvisioningOptions.BY_URL;
import static eu.cessda.cmv.server.ResourceSelectionComponent.SelectionMode.MULTI;
import static eu.cessda.cmv.server.ResourceSelectionComponent.SelectionMode.SINGLE;
import static java.util.Objects.requireNonNull;

import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.gesis.commons.resource.InMemoryResource;
import org.gesis.commons.resource.Resource;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Grid;
import com.vaadin.ui.RadioButtonGroup;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.wcs.wcslib.vaadin.widget.multifileupload.ui.MultiFileUpload;
import com.wcs.wcslib.vaadin.widget.multifileupload.ui.UploadFinishedHandler;
import com.wcs.wcslib.vaadin.widget.multifileupload.ui.UploadStateWindow;

public class ResourceSelectionComponent extends CustomComponent
{
	private static final long serialVersionUID = 8381371322203425719L;

	private List<Resource> resources;

	public enum SelectionMode
	{
		SINGLE,
		MULTI
	}

	public enum ProvisioningOptions
	{
		BY_PREDEFINED,
		BY_URL,
		BY_UPLOAD
	}

	public ResourceSelectionComponent(
			SelectionMode selectionMode,
			List<ProvisioningOptions> provisioningOptions,
			ProvisioningOptions selectedProvisioningOption )
	{
		this( selectionMode,
				provisioningOptions,
				selectedProvisioningOption,
				new ArrayList<>(),
				() ->
				{
				} );
	}

	public ResourceSelectionComponent(
			SelectionMode selectionMode,
			List<ProvisioningOptions> provisioningOptions,
			ProvisioningOptions selectedProvisioningOption,
			List<Resource> resources,
			Runnable refreshEvent )
	{
		requireNonNull( selectionMode );
		requireNonNull( provisioningOptions );
		requireNonNull( resources );
		this.resources = resources;

		MultiFileUpload multiFileUpload = newMultiFileUpload( selectionMode );
		RadioButtonGroup<ProvisioningOptions> buttonGroup = newButtonGroup( provisioningOptions,
				selectedProvisioningOption );

		ComboBox<Resource> comboBox = new ComboBox<>();
		comboBox.setPlaceholder( "Select profile" );
		comboBox.setItemCaptionGenerator( resource -> resource.getUri().toString() );
		comboBox.setWidth( 100, Unit.PERCENTAGE );
		comboBox.setTextInputAllowed( false );
		comboBox.setItems( Resource.newResource(
				"https://bitbucket.org/cessda/cessda.cmv.core/raw/master/src/main/resources/demo-documents/ddi-v25/cdc25_profile.xml" ) );

		TextField textField = new TextField();
		textField.setWidthFull();
		textField.setPlaceholder( "Paste url" );
		textField.setWidth( 100, Unit.PERCENTAGE );

		Button clearButton = new Button( "Clear" );
		Grid<Resource> grid = newGrid();
		grid.setItems( resources );

		Runnable refreshComponents = () ->
		{
			comboBox.setVisible( buttonGroup.getValue().equals( BY_PREDEFINED )
					& (selectionMode.equals( SINGLE ) && resources.isEmpty()) );
			textField.setVisible( buttonGroup.getValue().equals( BY_URL )
					& (selectionMode.equals( MULTI ) || (selectionMode.equals( SINGLE ) & resources.isEmpty())) );
			textField.clear();
			multiFileUpload.setVisible( buttonGroup.getValue().equals( BY_UPLOAD )
					& (selectionMode.equals( MULTI ) || (selectionMode.equals( SINGLE ) & resources.isEmpty())) );
			clearButton.setVisible( !resources.isEmpty() );
			grid.getDataProvider().refreshAll();
			grid.setVisible( !resources.isEmpty() );
			if ( !resources.isEmpty() )
			{
				grid.setHeightByRows( resources.size() );
			}
			refreshEvent.run();
		};

		textField.addValueChangeListener( listener ->
		{
			if ( listener.getValue() != null
					&& !listener.getValue().trim().contentEquals( "" )
					&& listener.getValue().startsWith( "http" ) )
			{
				if ( selectionMode.equals( SINGLE ) )
				{
					resources.clear();
				}
				resources.add( Resource.newResource( listener.getValue() ) );

				refreshComponents.run();
			}
		} );

		comboBox.addSelectionListener( listener ->
		{
			listener.getSelectedItem().ifPresent( resource ->
			{
				resources.clear();
				resources.add( resource );
				refreshComponents.run();
				comboBox.clear();
			} );
		} );
		buttonGroup.addSelectionListener( listener -> refreshComponents.run() );
		clearButton.addClickListener( listener ->
		{
			resources.clear();
			refreshComponents.run();
		} );

		UploadFinishedHandler uploadFinishedHandler = (
				InputStream inputStream,
				String fileName,
				String mimeType,
				long length,
				int filesLeftInQueue ) ->
		{
			resources.add( new InMemoryResource( URI.create( fileName ), inputStream ) );
			refreshComponents.run();
		};
		multiFileUpload.getUploadStatePanel().setFinishedHandler( uploadFinishedHandler );

		VerticalLayout verticalLayout = new VerticalLayout();
		verticalLayout.setMargin( false );
		verticalLayout.setWidth( 100, Unit.PERCENTAGE );
		verticalLayout.addComponent( buttonGroup );
		verticalLayout.addComponent( textField );
		verticalLayout.addComponent( comboBox );
		verticalLayout.addComponent( multiFileUpload );
		verticalLayout.addComponent( grid );
		verticalLayout.addComponent( clearButton );
		setCompositionRoot( verticalLayout );
		setWidthFull();
		refreshComponents.run();
	}

	public List<Resource> getResources()
	{
		return Collections.unmodifiableList( resources );
	}

	private Grid<Resource> newGrid()
	{
		Grid<Resource> grid = new Grid<>();
		grid.addColumn( Resource::getUri ).setCaption( "URI" );
		grid.setSelectionMode( NONE );
		grid.setWidthFull();
		grid.setHeightMode( ROW );
		grid.setVisible( false );
		grid.setHeaderVisible( false );
		return grid;
	}

	private RadioButtonGroup<ProvisioningOptions> newButtonGroup(
			List<ProvisioningOptions> provisioningOptions,
			ProvisioningOptions selectedProvisioningOption )
	{
		RadioButtonGroup<ProvisioningOptions> buttonGroup = new RadioButtonGroup<>();
		buttonGroup.setItems( provisioningOptions );
		buttonGroup.addStyleName( OPTIONGROUP_HORIZONTAL );
		buttonGroup.setValue( selectedProvisioningOption );
		return buttonGroup;
	}

	private MultiFileUpload newMultiFileUpload( SelectionMode selectionMode )
	{
		UploadStateWindow uploadStateWindow = new UploadStateWindow();
		uploadStateWindow.setWindowPosition( UploadStateWindow.WindowPosition.CENTER );
		uploadStateWindow.setOverallProgressVisible( true );
		uploadStateWindow.setResizable( true );
		MultiFileUpload multiFileUpload = new MultiFileUpload( null, uploadStateWindow, selectionMode.equals( MULTI ) );
		// multiFileUpload.setAcceptedMimeTypes( Arrays.asList( "xml" ) );
		multiFileUpload.setMaxFileSize( 100_000_000 );
		multiFileUpload.setSizeErrorMsgPattern( "File is too big (max = {0}): {2} ({1})" );
		multiFileUpload.setPanelCaption( "Files" );
		multiFileUpload.setMaxFileCount( 100 );
		multiFileUpload.getSmartUpload().setUploadButtonCaptions( "Upload File", "Upload Files" );
		multiFileUpload.getSmartUpload().setUploadButtonIcon( VaadinIcons.UPLOAD );
		return multiFileUpload;
	}
}
