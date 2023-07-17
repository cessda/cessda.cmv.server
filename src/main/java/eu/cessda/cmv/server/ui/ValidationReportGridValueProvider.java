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
import com.vaadin.server.Sizeable.Unit;
import com.vaadin.ui.*;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.themes.ValoTheme;
import eu.cessda.cmv.core.mediatype.validationreport.v0.ConstraintViolationV0;
import eu.cessda.cmv.server.ValidationReport;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.io.Serial;
import java.util.ResourceBundle;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class ValidationReportGridValueProvider
		implements ValueProvider<ValidationReport, CustomComponent>
{
	@Serial
	private static final long serialVersionUID = 5087782841088695356L;
	private static final int ELEMENT_SIZE = 30;

	private static Grid<String> getStringGrid( String message )
	{
		var stringGrid = new Grid<String>();
		stringGrid.setItems( message );
		stringGrid.addColumn( String::toString );
		return stringGrid;
	}

	@Override
	public CustomComponent apply( ValidationReport report )
	{
		var bundle = ResourceBundle.getBundle( ValidationReportGridValueProvider.class.getName(), UI.getCurrent().getLocale() );

		var validationReport = report.validationReport();
		var documentLabelString =  report.documentResource().getLabel();

		var documentLabel = new Label();
		documentLabel.setCaption( bundle.getString("document.title") );
		documentLabel.setValue( documentLabelString );

		var constraintViolations = validationReport.getConstraintViolations();

		// Configure the schema violation grid
		final Grid<?> schemaViolationGrid;
		if (!report.validationErrors().isEmpty())
		{
			var saxExceptionGrid = new Grid<SAXParseException>();
			saxExceptionGrid.setItems( report.validationErrors() );
			saxExceptionGrid.addColumn( SAXException::toString );
			schemaViolationGrid = saxExceptionGrid;
		} else {
			// Display a message stating no schema violations were found
			schemaViolationGrid = getStringGrid( bundle.getString( "result.noXSDSchemaViolations" ) );
		}
		schemaViolationGrid.setCaption( bundle.getString("result.XSDSchemaViolations") );
		schemaViolationGrid.setHeaderVisible( false );
		schemaViolationGrid.setStyleName( ValoTheme.TABLE_BORDERLESS );
		schemaViolationGrid.setSizeFull();
		schemaViolationGrid.setSelectionMode( SelectionMode.NONE );
		schemaViolationGrid.setHeight( min( max( ( report.validationErrors().size() + 1 ) * ELEMENT_SIZE, ELEMENT_SIZE ), 200 ), Unit.PIXELS );

		// Configure the constraint violation grid
		final Grid<?> constraintViolationGrid;
		if (!report.validationErrors().isEmpty())
		{
			var constraintGrid = new Grid<ConstraintViolationV0>();
			constraintGrid.setItems( constraintViolations );
			constraintGrid.addColumn( ConstraintViolationV0::getMessage );
			constraintViolationGrid = constraintGrid;
		} else {
			// Display a message stating no constraint violations were found
			constraintViolationGrid = getStringGrid( bundle.getString("result.noConstraintViolations") );
		}
		constraintViolationGrid.setCaption( bundle.getString("result.constraintViolations") );
		constraintViolationGrid.setHeaderVisible( false );
		constraintViolationGrid.setStyleName( ValoTheme.TABLE_BORDERLESS );
		constraintViolationGrid.setSizeFull();
		constraintViolationGrid.setSelectionMode( SelectionMode.NONE );
		constraintViolationGrid.setHeight( min( max( ( constraintViolations.size() + 1 ) * ELEMENT_SIZE, ELEMENT_SIZE ), 400 ), Unit.PIXELS );

		var constraintViolationForm = new FormLayout();
		constraintViolationForm.addComponent( documentLabel );
		constraintViolationForm.addComponent( constraintViolationGrid );

		var schemaViolationForm = new FormLayout();
		schemaViolationForm.addComponent( schemaViolationGrid );

		var verticalLayout = new VerticalLayout();
		verticalLayout.addComponent( constraintViolationForm );
		verticalLayout.addComponent( schemaViolationForm );

		return new CustomComponent( verticalLayout );
	}
}
