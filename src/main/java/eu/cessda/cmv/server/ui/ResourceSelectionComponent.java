package eu.cessda.cmv.server.ui;

import static com.vaadin.shared.ui.grid.HeightMode.ROW;
import static com.vaadin.ui.Grid.SelectionMode.NONE;
import static com.vaadin.ui.Notification.Type.WARNING_MESSAGE;
import static com.vaadin.ui.themes.ValoTheme.OPTIONGROUP_HORIZONTAL;
import static eu.cessda.cmv.server.ui.ResourceSelectionComponent.ProvisioningOptions.BY_PREDEFINED;
import static eu.cessda.cmv.server.ui.ResourceSelectionComponent.ProvisioningOptions.BY_UPLOAD;
import static eu.cessda.cmv.server.ui.ResourceSelectionComponent.ProvisioningOptions.BY_URL;
import static eu.cessda.cmv.server.ui.ResourceSelectionComponent.SelectionMode.MULTI;
import static eu.cessda.cmv.server.ui.ResourceSelectionComponent.SelectionMode.SINGLE;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.gesis.commons.resource.Resource.newResource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import org.apache.commons.validator.routines.UrlValidator;
import org.gesis.commons.resource.Resource;
import org.gesis.commons.resource.io.DdiInputStream;
import org.gesis.commons.resource.io.NotDdiInputStreamException;
import org.gesis.commons.xml.XercesXalanDocument;
import org.gesis.commons.xml.XmlNotWellformedException;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.RadioButtonGroup;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.wcs.wcslib.vaadin.widget.multifileupload.ui.MultiFileUpload;
import com.wcs.wcslib.vaadin.widget.multifileupload.ui.UploadFinishedHandler;
import com.wcs.wcslib.vaadin.widget.multifileupload.ui.UploadStateWindow;

public class ResourceSelectionComponent extends CustomComponent
{
	private static final long serialVersionUID = 8381371322203425719L;

	private List<Resource.V10> selectedResources;

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
			ProvisioningOptions selectedProvisioningOption,
			List<Resource.V10> predefinedResources,
			List<Resource.V10> selectedResources,
			Runnable refreshEvent )
	{
		requireNonNull( selectionMode );
		requireNonNull( provisioningOptions );
		requireNonNull( predefinedResources );
		requireNonNull( selectedResources );
		this.selectedResources = selectedResources;

		MultiFileUpload multiFileUpload = newMultiFileUpload( selectionMode );
		RadioButtonGroup<ProvisioningOptions> buttonGroup = newButtonGroup( provisioningOptions,
				selectedProvisioningOption );

		ComboBox<Resource.V10> comboBox = new ComboBox<>();
		comboBox.setPlaceholder( "Select resource" );
		comboBox.setItemCaptionGenerator( resource ->
		{
			String uri = resource.getUri().toString();
			return uri.substring( uri.lastIndexOf( '/' ) + 1 );
		} );
		comboBox.setWidth( 100, Unit.PERCENTAGE );
		comboBox.setTextInputAllowed( false );
		comboBox.setItems( predefinedResources );

		TextField textField = new TextField();
		textField.setWidthFull();
		textField.setPlaceholder( "Paste url" );
		textField.setWidth( 100, Unit.PERCENTAGE );
		// TODO https://vaadin.com/forum/thread/15426235/vaadin8-field-validation-without-binders

		Button clearButton = new Button( "Clear" );
		Grid<Resource.V10> grid = newGrid();
		grid.setItems( selectedResources );

		Runnable refreshComponents = () ->
		{
			comboBox.setVisible( buttonGroup.getValue().equals( BY_PREDEFINED )
					& (selectionMode.equals( MULTI )
							|| (selectionMode.equals( SINGLE ) & selectedResources.isEmpty())) );
			comboBox.clear();
			textField.setVisible( buttonGroup.getValue().equals( BY_URL )
					& (selectionMode.equals( MULTI )
							|| (selectionMode.equals( SINGLE ) & selectedResources.isEmpty())) );
			textField.clear();
			multiFileUpload.setVisible( buttonGroup.getValue().equals( BY_UPLOAD )
					& (selectionMode.equals( MULTI )
							|| (selectionMode.equals( SINGLE ) & selectedResources.isEmpty())) );
			clearButton.setVisible( !selectedResources.isEmpty() );
			grid.getDataProvider().refreshAll();
			grid.setVisible( !selectedResources.isEmpty() );
			if ( !selectedResources.isEmpty() )
			{
				grid.setHeightByRows( selectedResources.size() );
			}
			refreshEvent.run();
		};
		Consumer<Resource.V10> selectResource = resource ->
		{
			if ( selectionMode.equals( SINGLE ) )
			{
				selectedResources.clear();
			}
			selectedResources.add( resource );
		};
		textField.addBlurListener( listener ->
		{
			// See https://bitbucket.org/cessda/cessda.cmv/issues/89
			textField.getOptionalValue().ifPresent( value ->
			{
				UrlValidator urlValidator = UrlValidator.getInstance();
				if ( urlValidator.isValid( value ) )
				{
					recognizeDdiDocument( newResource( value ) ).ifPresent( selectResource );
					refreshComponents.run();
				}
				else
				{
					Notification.show( "Input is not a valid url", Type.WARNING_MESSAGE );
					textField.clear();
				}
			} );
		} );
		comboBox.addSelectionListener( listener ->
		{
			listener.getSelectedItem().ifPresent( selectedItem ->
			{
				recognizeDdiDocument( selectedItem ).ifPresent( selectResource );
				refreshComponents.run();
			} );
		} );
		UploadFinishedHandler uploadFinishedHandler = (
				InputStream inputStream,
				String fileName,
				String mimeType,
				long length,
				int filesLeftInQueue ) ->
		{
			Resource.V10 uploadedResource = newResource( inputStream, fileName );
			recognizeDdiDocument( uploadedResource ).ifPresent( selectResource );
			refreshComponents.run();
		};
		multiFileUpload.getUploadStatePanel().setFinishedHandler( uploadFinishedHandler );

		buttonGroup.addSelectionListener( listener -> refreshComponents.run() );
		clearButton.addClickListener( listener ->
		{
			selectedResources.clear();
			refreshComponents.run();
		} );

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
		return Collections.unmodifiableList( selectedResources );
	}

	private Grid<Resource.V10> newGrid()
	{
		Grid<Resource.V10> grid = new Grid<>();
		grid.addColumn( Resource.V10::getLabel ).setCaption( "Label" );
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

	private Optional<Resource.V10> recognizeDdiDocument( Resource.V10 resource )
	{
		requireNonNull( resource );
		try ( DdiInputStream inputStream = new DdiInputStream( resource.readInputStream() ) )
		{
			// TODO Do well-formed check with SAX only more efficiently
			XercesXalanDocument.newBuilder().ofInputStream( inputStream ).build();
			return Optional.of( resource );
		}
		catch (NotDdiInputStreamException e)
		{
			String message = format( "%s is not recognized as DDI document", resource.getUri() );
			Notification.show( message, WARNING_MESSAGE );
			return Optional.empty();
		}
		catch (XmlNotWellformedException e)
		{
			String message = format( "%s is not well-formed XML: %s", resource.getUri(), e.getMessage() );
			Notification.show( message, WARNING_MESSAGE );
			return Optional.empty();
		}
		catch (IOException e)
		{
			throw new IllegalArgumentException( e );
		}
	}
}
