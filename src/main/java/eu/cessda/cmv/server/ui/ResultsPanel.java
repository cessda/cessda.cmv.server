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

import com.vaadin.data.ValueProvider;
import com.vaadin.server.Sizeable;
import com.vaadin.ui.*;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.themes.ValoTheme;
import eu.cessda.cmv.core.mediatype.validationreport.v0.ConstraintViolationV0;

import java.util.Collection;
import java.util.ResourceBundle;

import static java.lang.Math.min;

public class ResultsPanel
{
	private ResultsPanel()
	{
	}

	private static Grid<String> getStringGrid( String message )
	{
		var stringGrid = new Grid<String>();
		stringGrid.setItems( message );
		stringGrid.addColumn( String::toString );
		return stringGrid;
	}

	public static Panel createResultsPanel( eu.cessda.cmv.server.ValidationReport report )
	{
		var bundle = ResourceBundle.getBundle( ResultsPanel.class.getName(), UI.getCurrent().getLocale() );

		var validationReport = report.validationReport();
		var documentLabelString =  report.documentResource().getLabel();

		var documentLabel = new Label();
		documentLabel.setCaption( bundle.getString("document.title") );
		documentLabel.setValue( documentLabelString );

		/*
		 * Schema Violations
		 */
		var schemaViolations = report.validationErrors();

		// Configure the schema violation grid
		var schemaViolationGrid = createResultsGrid(
			schemaViolations,
			saxException -> {
				// Remove org.xml.sax.SAXException from the message
				var exception = saxException.toString();
				return exception.substring( exception.indexOf( ';' ) + 1 );
			},
			5,
			bundle.getString("result.XSDSchemaViolations"),
			bundle.getString( "result.noXSDSchemaViolations" )
		);

		/*
		 * Constraint Violations
		 */
		var constraintViolations = validationReport.getConstraintViolations();

		// Configure the constraint violation grid
		var constraintViolationGrid = createResultsGrid(
			constraintViolations,
			ConstraintViolationV0::getMessage,
			10,
			bundle.getString("result.constraintViolations"),
			bundle.getString("result.noConstraintViolations")
		);

		var resultsForm = new FormLayout();
		resultsForm.addComponent( documentLabel );
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
		resultsGrid.setWidth(1020, Sizeable.Unit.PIXELS );
		resultsGrid.setSelectionMode( SelectionMode.NONE );

		return resultsGrid;
	}
}
