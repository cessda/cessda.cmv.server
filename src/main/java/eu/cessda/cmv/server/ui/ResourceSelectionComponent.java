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
import eu.cessda.cmv.core.NotDocumentException;
import org.apache.commons.validator.routines.UrlValidator;
import org.gesis.commons.resource.Resource;
import org.springframework.web.util.HtmlUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

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
	private static final long serialVersionUID = -808880272310582714L;

	private final ArrayList<Resource.V10> selectedResources = new ArrayList<>(1);
	private final NativeSelect<Resource.V10> predefinedSelect;
	private final TextField textField;
	private final Button clearButton;
	private final Grid<Resource.V10> selectedResourcesGrid;
	private final MultiFileUpload fileUpload;
	private final RadioButtonGroup<ProvisioningOptions> provisioningOptions;
	private final SelectionMode selectionMode;
	private final transient CessdaMetadataValidatorFactory cessdaMetadataValidatorFactory;
	private final ResourceBundle bundle;

	public enum SelectionMode
	{
		SINGLE,
		MULTI
	}

	public enum ProvisioningOptions
	{
		BY_PREDEFINED,
		BY_URL,
		BY_UPLOAD;

		public String getLocalisedName()
		{
			var bundle = ResourceBundle.getBundle( ResourceSelectionComponent.class.getName(), UI.getCurrent().getLocale() );
			return bundle.getString(this.name());
		}
	}

	public ResourceSelectionComponent(
			SelectionMode selectionMode,
			ProvisioningOptions selectedProvisioningOption,
			List<Resource.V10> predefinedResources,
			CessdaMetadataValidatorFactory cessdaMetadataValidatorFactory )
	{
		requireNonNull( selectionMode );
		requireNonNull( predefinedResources );

		this.bundle = ResourceBundle.getBundle( ResourceSelectionComponent.class.getName(), UI.getCurrent().getLocale() );

		this.cessdaMetadataValidatorFactory = cessdaMetadataValidatorFactory;
		this.selectionMode = selectionMode;

		this.provisioningOptions = provisioningOptionsButtonGroup( selectedProvisioningOption );
		this.provisioningOptions.addSelectionListener( listener -> refreshComponents() );

		this.predefinedSelect = new NativeSelect<>();
		this.predefinedSelect.setEmptySelectionCaption( bundle.getString( "comboBox.select" ) );
		this.predefinedSelect.setItemCaptionGenerator( Resource.V10::getLabel );
		this.predefinedSelect.setWidth( 100, Unit.PERCENTAGE );
		this.predefinedSelect.setItems( predefinedResources );
		this.predefinedSelect.addSelectionListener( listener -> listener.getSelectedItem()
				.flatMap( this::recognizeDdiDocument )
				.ifPresent( this::selectResource ) );

		this.textField = new TextField();
		this.textField.setWidthFull();
		this.textField.setPlaceholder( bundle.getString( "pasteUrl" ) );
		this.textField.setWidth( 100, Unit.PERCENTAGE );
		// TODO https://vaadin.com/forum/thread/15426235/vaadin8-field-validation-without-binders
		this.textField.addBlurListener( listener ->
			// See https://bitbucket.org/cessda/cessda.cmv/issues/89
			this.textField.getOptionalValue().ifPresent( value ->
			{
				UrlValidator urlValidator = UrlValidator.getInstance();
				if ( urlValidator.isValid( value ) )
				{
					recognizeDdiDocument( newResource( value ) ).ifPresent( this::selectResource );
				}
				else
				{
					Notification.show( bundle.getString("invalidURL"), Type.WARNING_MESSAGE );
					this.textField.clear();
				}
			} )
		);

		this.selectedResourcesGrid = newGrid();
		this.selectedResourcesGrid.setItems( this.selectedResources );

		this.fileUpload = newMultiFileUpload( this.selectionMode );
		this.fileUpload.getUploadStatePanel().setFinishedHandler( ( InputStream inputStream, String fileName, String mimeType, long length, int filesLeftInQueue ) ->
		{
			Resource.V10 uploadedResource = newResource( inputStream, fileName );
			recognizeDdiDocument( uploadedResource ).ifPresent( this::selectResource );
		} );

		this.clearButton = new Button();
		this.clearButton.setCaption( switch ( selectionMode )
		{
			case SINGLE -> bundle.getString("clear");
			case MULTI -> bundle.getString("clearAll");
		} );
		this.clearButton.addClickListener( listener ->
		{
			this.selectedResources.clear();
			refreshComponents();
		} );

		// Create the VerticalLayout that will serve as the composition root
		VerticalLayout verticalLayout = new VerticalLayout();
		verticalLayout.setMargin( false );
		verticalLayout.setWidth( 100, Unit.PERCENTAGE );
		verticalLayout.addComponent( this.provisioningOptions );
		verticalLayout.addComponent( this.textField );
		verticalLayout.addComponent( this.predefinedSelect );
		verticalLayout.addComponent( this.fileUpload );
		verticalLayout.addComponent( this.selectedResourcesGrid );
		verticalLayout.addComponent( this.clearButton );

		setCompositionRoot( verticalLayout );
		setWidthFull();
		refreshComponents();
	}

	private void refreshComponents()
	{
		predefinedSelect.setVisible( provisioningOptions.getValue().equals( BY_PREDEFINED )
				&& ( selectionMode.equals( MULTI )
				|| ( selectionMode.equals( SINGLE ) && selectedResources.isEmpty() ) ) );
		predefinedSelect.clear();
		textField.setVisible( provisioningOptions.getValue().equals( BY_URL )
				&& ( selectionMode.equals( MULTI )
				|| ( selectionMode.equals( SINGLE ) && selectedResources.isEmpty() ) ) );
		textField.clear();
		fileUpload.setVisible( provisioningOptions.getValue().equals( BY_UPLOAD )
				&& ( selectionMode.equals( MULTI )
				|| ( selectionMode.equals( SINGLE ) && selectedResources.isEmpty() ) ) );
		clearButton.setVisible( !selectedResources.isEmpty() );
		selectedResourcesGrid.getDataProvider().refreshAll();
		selectedResourcesGrid.setVisible( !selectedResources.isEmpty() );
		if ( !selectedResources.isEmpty() )
		{
			selectedResourcesGrid.setHeightByRows( selectedResources.size() );
		}
	}

	private void selectResource( Resource.V10 resource )
	{
		if ( selectionMode.equals( SINGLE ) )
		{
			selectedResources.clear();
		}
		selectedResources.add( resource );
		refreshComponents();
	}

	/**
	 * Return the list of selected resources from this component. The returned list is unmodifiable.
	 * <p>
	 * For a ResourceSelectionComponent constructed with {@link SelectionMode#SINGLE},
	 * this will contain zero or one elements.
	 */
	public List<Resource.V10> getResources()
	{
		return List.copyOf( selectedResources );
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
				label.setValue( "<a href='" + resource.getUri() + "' target='_blank'>" + HtmlUtils.htmlEscape( resource.getLabel(), "UTF-8" ) + "</a>" );
			}
			else
			{
				label.setValue( resource.getLabel() );
			}

			if (selectionMode.equals( MULTI ))
			{
				// If this is a multi select component, add a remove button on each element
				var button = new Button( bundle.getString("clear") );
				// right: 2px
				button.addClickListener( event ->
				{
					selectedResources.remove( resource );
					refreshComponents();
				} );
				var layout = new HorizontalLayout( label, button );
				layout.addStyleName( "multiselect" );
				return layout;
			}
			else
			{
				return label;
			}
		}, new ComponentRenderer() ).setCaption( "Label" );
		grid.setSelectionMode( NONE );
		grid.setWidthFull();
		grid.setHeightMode( ROW );
		grid.setVisible( false );
		grid.setHeaderVisible( false );
		return grid;
	}

	private RadioButtonGroup<ProvisioningOptions> provisioningOptionsButtonGroup( ProvisioningOptions selectedProvisioningOption )
	{
		RadioButtonGroup<ProvisioningOptions> buttonGroup = new RadioButtonGroup<>();
		buttonGroup.addStyleName( OPTIONGROUP_HORIZONTAL );
		buttonGroup.setItemCaptionGenerator( ProvisioningOptions::getLocalisedName );
		buttonGroup.setItems( ProvisioningOptions.values() );
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
		multiFileUpload.setSizeErrorMsgPattern( bundle.getString("upload.fileTooBigPattern") );
		multiFileUpload.setPanelCaption( bundle.getString("upload.caption") );
		multiFileUpload.setMaxFileCount( 100 );
		multiFileUpload.getSmartUpload().setUploadButtonCaptions( bundle.getString("upload.singleFile"), bundle.getString("upload.multipleFiles") );
		multiFileUpload.getSmartUpload().setUploadButtonIcon( VaadinIcons.UPLOAD );
		return multiFileUpload;
	}

	private Optional<Resource.V10> recognizeDdiDocument( Resource.V10 resource )
	{
		try
		{
			// TODO Avoid inefficiency of calling newDocument only to check if document is accepted
			cessdaMetadataValidatorFactory.newDocument( resource );
			return Optional.of( resource );
		}
		catch ( IOException | NotDocumentException e)
		{
			Notification.show( e.getMessage(), Type.WARNING_MESSAGE );
			return Optional.empty();
		}
	}
}
