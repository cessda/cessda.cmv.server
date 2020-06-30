package eu.cessda.cmv.server;

import static com.vaadin.ui.Grid.SelectionMode.NONE;
import static com.vaadin.ui.themes.ValoTheme.OPTIONGROUP_HORIZONTAL;

import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.gesis.commons.resource.InMemoryResource;
import org.gesis.commons.resource.Resource;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Grid;
import com.vaadin.ui.RadioButtonGroup;
import com.vaadin.ui.VerticalLayout;
import com.wcs.wcslib.vaadin.widget.multifileupload.ui.MultiFileUpload;
import com.wcs.wcslib.vaadin.widget.multifileupload.ui.UploadFinishedHandler;
import com.wcs.wcslib.vaadin.widget.multifileupload.ui.UploadStateWindow;

public class ResourceSelectionComponent extends CustomComponent
{
	private static final long serialVersionUID = 8381371322203425719L;

	private List<Resource> resources;

	private enum ProvisioningOptions
	{
		BY_PREDEFINED,
		BY_URL,
		BY_UPLOAD
	}

	public ResourceSelectionComponent()
	{
		this( new ArrayList<>() );
	}

	public ResourceSelectionComponent( List<Resource> resources )
	{
		this.resources = resources;
		Grid<Resource> grid = newGrid();
		grid.setItems( resources );

		Button clearButton = new Button( "Clear" );
		clearButton.setVisible( false );
		clearButton.addClickListener( listener ->
		{
			resources.clear();
			grid.getDataProvider().refreshAll();
			grid.setVisible( false );
			clearButton.setVisible( false );
		} );

		UploadFinishedHandler uploadFinishedHandler = (
				InputStream inputStream,
				String fileName,
				String mimeType,
				long length,
				int filesLeftInQueue ) ->
		{
			resources.add( new InMemoryResource( URI.create( fileName ), inputStream ) );
			grid.getDataProvider().refreshAll();
			grid.setVisible( true );
			grid.setHeightByRows( resources.size() );
			clearButton.setVisible( true );
		};
		MultiFileUpload multiFileUpload = newMultiFileUpload( uploadFinishedHandler );

		RadioButtonGroup<ProvisioningOptions> buttonGroup = newButtonGroup();
		buttonGroup.addSelectionListener( listener ->
		{
			multiFileUpload.setVisible( listener.getValue().equals( ProvisioningOptions.BY_UPLOAD ) );
		} );

		VerticalLayout verticalLayout = new VerticalLayout();
		verticalLayout.setMargin( false );
		verticalLayout.addComponent( buttonGroup );
		verticalLayout.addComponent( multiFileUpload );
		verticalLayout.addComponent( grid );
		verticalLayout.addComponent( clearButton );
		setCompositionRoot( verticalLayout );
		setSizeUndefined();
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
		grid.setHeightMode( HeightMode.ROW );
		grid.setVisible( false );
		while (grid.getHeaderRowCount() > 0)
		{
			grid.removeHeaderRow( 0 );
		}
		return grid;
	}

	private RadioButtonGroup<ProvisioningOptions> newButtonGroup()
	{
		RadioButtonGroup<ProvisioningOptions> buttonGroup = new RadioButtonGroup<>();
		buttonGroup.setItems( ProvisioningOptions.BY_URL, ProvisioningOptions.BY_UPLOAD );
		buttonGroup.addStyleName( OPTIONGROUP_HORIZONTAL );
		buttonGroup.setValue( ProvisioningOptions.BY_UPLOAD );
		return buttonGroup;
	}

	private MultiFileUpload newMultiFileUpload( UploadFinishedHandler uploadFinishedHandler )
	{
		UploadStateWindow uploadStateWindow = new UploadStateWindow();
		uploadStateWindow.setWindowPosition( UploadStateWindow.WindowPosition.CENTER );
		uploadStateWindow.setOverallProgressVisible( true );
		uploadStateWindow.setResizable( true );
		MultiFileUpload multiFileUpload = new MultiFileUpload( uploadFinishedHandler, uploadStateWindow );
		multiFileUpload.setMaxFileSize( 100_000_000 );
		multiFileUpload.setSizeErrorMsgPattern( "File is too big (max = {0}): {2} ({1})" );
		multiFileUpload.setPanelCaption( "Files" );
		multiFileUpload.setMaxFileCount( 100 );
		multiFileUpload.getSmartUpload().setUploadButtonCaptions( "Upload File", "Upload Files" );
		multiFileUpload.getSmartUpload().setUploadButtonIcon( VaadinIcons.UPLOAD );
		return multiFileUpload;
	}
}
