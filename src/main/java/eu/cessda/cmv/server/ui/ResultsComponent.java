/*-
 * #%L
 * CESSDA Metadata Validator
 * %%
 * Copyright (C) 2020 - 2025 CESSDA ERIC
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
import eu.cessda.cmv.core.mediatype.validationreport.ConstraintViolation;
import eu.cessda.cmv.server.SchemaViolation;
import eu.cessda.cmv.server.ValidationReport;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.FilenameUtils;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static java.lang.Math.min;

@SuppressWarnings( "java:S2160" )
public class ResultsComponent extends CustomComponent
{
	@Serial
	private static final long serialVersionUID = 7050472282523014449L;


	public ResultsComponent( Map<Resource, ValidationReport> reports )
	{
		var bundle = ResourceBundle.getBundle( ResultsComponent.class.getName(), UI.getCurrent().getLocale() );

		// Configure the download all reports button
		var jsonButton = new Button( bundle.getString( "results.downloadAllReports" ) );
		createJSONFileDownloader( "Reports", reports ).extend( jsonButton );

		var csvButton = new Button( bundle.getString("results.downloadCSVBundle") );
		createCSVFileDownloader( reports ).extend( csvButton );

		// Add the buttons to a single horizontal layout
		var buttonLayout = new HorizontalLayout( jsonButton, csvButton );

		// Configure the panels
		var resultsPanels = reports.entrySet().stream().map(
			resource -> createResultsPanel( resource.getKey(), resource.getValue() )
		).toArray( Panel[]::new );

		// Configure the layout
		var layout = new VerticalLayout();
		layout.addComponent( buttonLayout );
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

	private static Panel createResultsPanel( Resource resource, ValidationReport report )
	{
		var bundle = ResourceBundle.getBundle( ResultsComponent.class.getName(), UI.getCurrent().getLocale() );
		var documentLabelString = resource.getFilename();
		if (documentLabelString == null)
		{
			documentLabelString = resource.getDescription();
		}

		var documentLabel = new Label( documentLabelString );

		// Export buttons
		var downloadJSONButton = new Button( bundle.getString( "results.downloadReport" ) );
		createJSONFileDownloader( FilenameUtils.removeExtension( documentLabelString ), report ).extend( downloadJSONButton );

		var csvButton = new Button( bundle.getString("results.downloadCSV") );
		createCSVFileDownloader( FilenameUtils.removeExtension( documentLabelString ), report ).extend( csvButton );

		// Combine buttons into their own layout
		var buttonLayout = new HorizontalLayout(downloadJSONButton, csvButton);

		var documentLayout = new HorizontalLayout( documentLabel, buttonLayout );
		documentLayout.setCaption( bundle.getString( "document.title" ) );
		documentLayout.addStyleName( "results-label-layout" );

		/*
		 * Schema Violations
		 */
		var schemaViolations = report.schemaViolations();

		// Configure the schema violation grid
		var schemaViolationGrid = createResultsGrid(
			schemaViolations,
			SchemaViolation::toString,
			5,
			bundle.getString( "result.XSDSchemaViolations" ),
			bundle.getString( "result.noXSDSchemaViolations" )
		);

		/*
		 * Constraint Violations
		 */
		var constraintViolations = report.constraintViolations();

		// Configure the constraint violation grid
		var constraintViolationGrid = createResultsGrid(
			constraintViolations,
			ConstraintViolation::getMessage,
			10,
			bundle.getString( "result.constraintViolations" ),
			bundle.getString( "result.noConstraintViolations" )
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
		if ( !results.isEmpty() )
		{
			var resultsGridWithData = new Grid<T>();
			resultsGridWithData.setItems( results );
			resultsGridWithData.addColumn( valueProvider );
			resultsGridWithData.setHeightByRows( min( results.size(), maxSize ) );
			resultsGrid = resultsGridWithData;
		}
		else
		{
			// Display a message stating no results were found
			resultsGrid = getStringGrid( noResults );
			resultsGrid.setHeightByRows( 1 );
		}

		resultsGrid.setCaption( caption );
		resultsGrid.setHeaderVisible( false );
		resultsGrid.setStyleName( ValoTheme.TABLE_BORDERLESS );
		resultsGrid.setWidth( 100, Unit.PERCENTAGE );
		resultsGrid.setSelectionMode( SelectionMode.NONE );

		return resultsGrid;
	}

	/**
	 * Create a downloader that returns a JSON representation of the given object.
	 *
	 * @param fileName the file name without extension.
	 * @param object the object to serialise.
	 */
	private static FileDownloader createJSONFileDownloader( String fileName, Object object )
	{
		var resource = new StreamResource(
			() ->
			{
				try
				{
                    byte[] jsonByteArray = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsBytes( object );
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

	/**
	 * Create a downloader that returns a ZIP file containing CSVs detailing any schema or constraint violations found.
  	 *
	 * @param fileName the name of the ZIP containing the CSVs.
	 * @param validationReport the report to convert into CSVs.
	 */
	private static FileDownloader createCSVFileDownloader( String fileName, ValidationReport validationReport)
	{
		var resource = new StreamResource(
			() ->
			{
				var outputStream = new ByteArrayOutputStream();
				try ( var zip = new ZipOutputStream( outputStream ) )
				{
					// Schema violations
					zip.putNextEntry( new ZipEntry(  "Schema violations.csv" ) );
					generateCSV( validationReport.schemaViolations(), ResultsComponent::convertSchemaViolationToCSV, zip );

					// Constraint violations
					zip.putNextEntry( new ZipEntry(  "Constraint violations.csv" ) );
					generateCSV( validationReport.constraintViolations(), ResultsComponent::convertConstraintViolationToCSV, zip );
				}
				catch ( IOException e )
				{
					throw new CSVException( e );
				}

				return new ByteArrayInputStream( outputStream.toByteArray() );
			},
			fileName + ".zip"
		);

		// Set the MIME type of the resource
		resource.setMIMEType( "application/zip" );

		var fileDownloader = new FileDownloader( resource );
		fileDownloader.setOverrideContentType( false );
		return fileDownloader;
	}

	/**
	 * Create a downloader that returns a ZIP file containing CSVs detailing any schema or constraint violations found.
  	 *
	 * @param reports the reports to convert into CSVs.
	 */
	private static FileDownloader createCSVFileDownloader( Map<Resource, ValidationReport> reports )
	{
		var resource = new StreamResource(
			() ->
			{
				var outputStream = new ByteArrayOutputStream();
				try ( var zip = new ZipOutputStream( outputStream ) )
				{
					for ( var report : reports.entrySet() )
					{
						var sourceFileName = FilenameUtils.removeExtension( report.getKey().getFilename() );

						// Schema violations
						zip.putNextEntry( new ZipEntry( sourceFileName + " - schema violations.csv" ) );
						generateCSV( report.getValue().schemaViolations(), ResultsComponent::convertSchemaViolationToCSV, zip );

						// Constraint violations
						zip.putNextEntry( new ZipEntry( sourceFileName + " - constraint violations.csv" ) );
						generateCSV( report.getValue().constraintViolations(), ResultsComponent::convertConstraintViolationToCSV, zip );
					}
				}
				catch ( IOException e )
				{
					throw new CSVException( e );
				}

				return new ByteArrayInputStream( outputStream.toByteArray() );
			},
			"Report" + ".zip"
		);

		// Set the MIME type of the resource
		resource.setMIMEType( "application/zip" );

		var fileDownloader = new FileDownloader( resource );
		fileDownloader.setOverrideContentType( false );
		return fileDownloader;
	}

	/**
	 * Prints a series of values to a CSV.
	 *
	 * @param values the source of values.
	 * @param stringMapper a function to map a {@link T} to a string array.
	 * @param outputStream the stream to write the CSV to.
	 */
	private static <T> void generateCSV( Iterable<T> values, Function<T, String[]> stringMapper, OutputStream outputStream )
	{
		// Add headers to the CSV
		var csvFormat = CSVFormat.RFC4180.builder().setHeader( "lineNumber", "columnNumber", "message" ).get();

		try
		{
			// Configure the CSV printer
			var writer = new BufferedWriter( new OutputStreamWriter( outputStream, StandardCharsets.UTF_8 ) );
			var csvPrinter = new CSVPrinter( writer, csvFormat );

			// Iterate through the list of entries
			for( var entry : values )
			{
				// Print the string representation to the CSV file
				var entryStrings = stringMapper.apply( entry );
				csvPrinter.printRecord( (Object[]) entryStrings );
			}

			// Flush the output from the writer
			writer.flush();
		}
		catch ( IOException e )
		{
			throw new CSVException( e );
		}
	}

	/**
	 * Convert a constraint violation into a string array suitable for use when generating a CSV.
	 * @return a string array in the form of {@code [lineNumber, columnNumber, message]}.
	 */
	private static String[] convertConstraintViolationToCSV( ConstraintViolation constraintViolation )
	{
		String lineNumber;
		String columnNumber;

		// Only set the line and column numbers if location information is present
		var locationInfo = constraintViolation.getLocationInfo();
		if ( locationInfo != null )
		{
			lineNumber = String.valueOf( locationInfo.getLineNumber() );
			columnNumber = String.valueOf( locationInfo.getColumnNumber() );
		}
		else
		{
			lineNumber = "";
			columnNumber = "";
		}

		return new String[]{
			lineNumber,
			columnNumber,
			constraintViolation.getMessage()
		};
	}

	/**
	 * Convert a schema violation into a string array suitable for use when generating a CSV.
	 * @return a string array in the form of {@code [lineNumber, columnNumber, message]}.
	 */
	private static String[] convertSchemaViolationToCSV( SchemaViolation schemaViolation )
	{
		return new String[]{
			String.valueOf( schemaViolation.lineNumber() ),
			String.valueOf( schemaViolation.columnNumber() ),
			schemaViolation.message()
		};
	}
}
