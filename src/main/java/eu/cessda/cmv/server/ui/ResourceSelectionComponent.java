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

import com.vaadin.icons.VaadinIcons;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.*;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.renderers.ComponentRenderer;
import com.wcs.wcslib.vaadin.widget.multifileupload.ui.MultiFileUpload;
import com.wcs.wcslib.vaadin.widget.multifileupload.ui.UploadStateWindow;
import eu.cessda.cmv.core.CessdaMetadataValidatorFactory;
import org.apache.commons.validator.routines.UrlValidator;
import org.gesis.commons.resource.Resource;

import java.io.InputStream;
import java.io.Serial;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static com.vaadin.shared.ui.grid.HeightMode.ROW;
import static com.vaadin.ui.Grid.SelectionMode.NONE;
import static com.vaadin.ui.themes.ValoTheme.OPTIONGROUP_HORIZONTAL;
import static eu.cessda.cmv.server.ui.ResourceSelectionComponent.ProvisioningOptions.*;
import static eu.cessda.cmv.server.ui.ResourceSelectionComponent.SelectionMode.MULTI;
import static eu.cessda.cmv.server.ui.ResourceSelectionComponent.SelectionMode.SINGLE;
import static java.util.Objects.requireNonNull;
import static org.gesis.commons.resource.Resource.newResource;

@SuppressWarnings( "java:S2160" )
public class ResourceSelectionComponent extends CustomComponent
{
	@Serial
	private static final long serialVersionUID = 8381371322203425719L;

	private final List<Resource.V10> selectedResources;

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
			Runnable refreshEvent,
			CessdaMetadataValidatorFactory cessdaMetadataValidatorFactory )
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
		comboBox.setItemCaptionGenerator( Resource.V10::getLabel );
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
					&& ( selectionMode.equals( MULTI )
					|| ( selectionMode.equals( SINGLE ) && selectedResources.isEmpty() ) ) );
			comboBox.clear();
			textField.setVisible( buttonGroup.getValue().equals( BY_URL )
					&& ( selectionMode.equals( MULTI )
					|| ( selectionMode.equals( SINGLE ) && selectedResources.isEmpty() ) ) );
			textField.clear();
			multiFileUpload.setVisible( buttonGroup.getValue().equals( BY_UPLOAD )
					&& ( selectionMode.equals( MULTI )
					|| ( selectionMode.equals( SINGLE ) && selectedResources.isEmpty() ) ) );
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
			refreshComponents.run();
		};
		textField.addBlurListener( listener ->
			// See https://bitbucket.org/cessda/cessda.cmv/issues/89
			textField.getOptionalValue().ifPresent( value ->
			{
				UrlValidator urlValidator = UrlValidator.getInstance();
				if ( urlValidator.isValid( value ) )
				{
					recognizeDdiDocument( cessdaMetadataValidatorFactory, newResource( value ) ).ifPresent( selectResource );
				}
				else
				{
					Notification.show( "Input is not a valid url", Type.WARNING_MESSAGE );
					textField.clear();
				}
			} )
		);
		comboBox.addSelectionListener( listener ->
				listener.getSelectedItem()
						.flatMap( selectedItem -> recognizeDdiDocument( cessdaMetadataValidatorFactory, selectedItem ) )
						.ifPresent( selectResource )
		);
		multiFileUpload.getUploadStatePanel().setFinishedHandler( ( InputStream inputStream, String fileName, String mimeType, long length, int filesLeftInQueue ) ->
		{
			Resource.V10 uploadedResource = newResource( inputStream, fileName );
			recognizeDdiDocument( cessdaMetadataValidatorFactory, uploadedResource ).ifPresent( selectResource );
		} );

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
		grid.addColumn( resource ->
		{
			Label label = new Label();
			label.setSizeFull();
			if ( resource.getUri().getScheme().startsWith( "http" ) )
			{
				label.setContentMode( ContentMode.HTML );
				label.setValue(
						"<a href='" + resource.getUri().toString() + "' target='_blank'>" + resource.getLabel() +
								"</a>" );
			}
			else
			{
				label.setValue( resource.getLabel() );
			}
			return label;
		}, new ComponentRenderer() ).setCaption( "Label" );
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
		multiFileUpload.setMaxFileSize( 100_000_000 );
		multiFileUpload.setSizeErrorMsgPattern( "File is too big (max = {0}): {2} ({1})" );
		multiFileUpload.setPanelCaption( "Files" );
		multiFileUpload.setMaxFileCount( 100 );
		multiFileUpload.getSmartUpload().setUploadButtonCaptions( "Upload File", "Upload Files" );
		multiFileUpload.getSmartUpload().setUploadButtonIcon( VaadinIcons.UPLOAD );
		return multiFileUpload;
	}

	private Optional<Resource.V10> recognizeDdiDocument(
			CessdaMetadataValidatorFactory cessdaMetadataValidatorFactory,
			Resource.V10 resource )
	{
		try
		{
			// TODO Avoid inefficiency of calling newDocument only to check if document is accepted
			cessdaMetadataValidatorFactory.newDocument( resource );
			return Optional.of( resource );
		}
		catch (Exception e)
		{
			Notification.show( e.getMessage(), Type.WARNING_MESSAGE );
			return Optional.empty();
		}
	}
}
