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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.data.ValueProvider;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.*;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.themes.ValoTheme;
import eu.cessda.cmv.core.mediatype.validationreport.v0.ConstraintViolationV0;
import eu.cessda.cmv.server.SchemaViolation;
import eu.cessda.cmv.server.ValidationReport;
import org.apache.commons.io.FilenameUtils;
import org.springframework.http.MediaType;

import java.io.ByteArrayInputStream;
import java.io.Serial;
import java.io.UncheckedIOException;
import java.util.Collection;
import java.util.Map;
import java.util.ResourceBundle;

import static java.lang.Math.min;

@SuppressWarnings( "java:S2160" )
public class ResultsComponent extends CustomComponent
{
	@Serial
	private static final long serialVersionUID = 7050472282523014449L;


	public ResultsComponent( Map<String, ValidationReport> reports )
	{
		var bundle = ResourceBundle.getBundle( ResultsComponent.class.getName(), UI.getCurrent().getLocale() );

		// Configure the download all reports button
		var downloadAllButton = new Button( bundle.getString("results.downloadAllReports") );
		createJSONFileDownloader( "Reports", reports ).extend( downloadAllButton );

		// Configure the panels
		var resultsPanels = reports.entrySet().stream().map( ResultsComponent::createResultsPanel ).toArray(Panel[]::new);

		// Configure the layout
		var layout = new VerticalLayout();
		layout.addComponent( downloadAllButton );
		layout.addComponents( resultsPanels );

		super.setCompositionRoot( layout );
	}

	private static Grid<String> getStringGrid( String message )
	{
		var stringGrid = new Grid<String>();
		stringGrid.setItems( message );
		stringGrid.addColumn( String::toString );
		return stringGrid;
	}

	private static Panel createResultsPanel( Map.Entry<String, ValidationReport> report )
	{
		var bundle = ResourceBundle.getBundle( ResultsComponent.class.getName(), UI.getCurrent().getLocale() );
		var documentLabelString =  report.getKey();

		var documentLabel = new Label( documentLabelString );

		// Export button
		var downloadButton = new Button( bundle.getString("result.downloadReport") );
		createJSONFileDownloader( FilenameUtils.removeExtension( report.getKey() ), report.getValue() ).extend( downloadButton );

		var documentLayout = new HorizontalLayout( documentLabel, downloadButton );
		documentLayout.setCaption( bundle.getString("document.title") );
		documentLayout.addStyleName( "results-label-layout" );

		/*
		 * Schema Violations
		 */
		var schemaViolations = report.getValue().schemaViolations();

		// Configure the schema violation grid
		var schemaViolationGrid = createResultsGrid(
			schemaViolations,
			SchemaViolation::toString,
			5,
			bundle.getString("result.XSDSchemaViolations"),
			bundle.getString( "result.noXSDSchemaViolations" )
		);

		/*
		 * Constraint Violations
		 */
		var constraintViolations = report.getValue().constraintViolations();

		// Configure the constraint violation grid
		var constraintViolationGrid = createResultsGrid(
			constraintViolations,
			ConstraintViolationV0::getMessage,
			10,
			bundle.getString("result.constraintViolations"),
			bundle.getString("result.noConstraintViolations")
		);

		var resultsForm = new FormLayout();
		resultsForm.addComponent( documentLayout );
		resultsForm.addComponent( constraintViolationGrid );
		resultsForm.addComponent( schemaViolationGrid );

		return new Panel( new VerticalLayout( resultsForm ) );
	}

	private static <T> Grid<?> createResultsGrid( Collection<T> results, ValueProvider<T, String> valueProvider, int maxSize, String caption, String noResults )
	{
		final Grid<?> resultsGrid;
		if (!results.isEmpty())
		{
			var resultsGridWithData = new Grid<T>();
			resultsGridWithData.setItems( results );
			resultsGridWithData.addColumn( valueProvider );
			resultsGridWithData.setHeightByRows( min( results.size(), maxSize ) );
			resultsGrid = resultsGridWithData;
		} else {
			// Display a message stating no results were found
			resultsGrid = getStringGrid( noResults );
			resultsGrid.setHeightByRows( 1 );
		}

		resultsGrid.setCaption( caption );
		resultsGrid.setHeaderVisible( false );
		resultsGrid.setStyleName( ValoTheme.TABLE_BORDERLESS );
		resultsGrid.setWidth(100, Unit.PERCENTAGE );
		resultsGrid.setSelectionMode( SelectionMode.NONE );

		return resultsGrid;
	}

	private static <T> FileDownloader createJSONFileDownloader( String fileName, T download )
	{
		var resource = new StreamResource(
			() ->
			{
				try
				{
					byte[] jsonByteArray = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsBytes( download );
					return new ByteArrayInputStream( jsonByteArray );
				}
				catch ( JsonProcessingException e )
				{
					throw new UncheckedIOException( e );
				}
			},
			fileName + ".json"
		);

		// Set the MIME type of the resource
		resource.setMIMEType( MediaType.APPLICATION_JSON_VALUE );

		var fileDownloader = new FileDownloader( resource );
		fileDownloader.setOverrideContentType( false );
		return fileDownloader;
	}
}
