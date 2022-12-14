/*-
 * #%L
 * CESSDA Metadata Validator
 * %%
 * Copyright (C) 2020 - 2021 CESSDA ERIC
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
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.Label;
import com.vaadin.ui.themes.ValoTheme;
import eu.cessda.cmv.core.mediatype.validationreport.v0.ConstraintViolationV0;
import eu.cessda.cmv.server.ValidationReport;
import org.gesis.commons.resource.Resource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.io.Serial;
import java.util.List;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class ValidationReportGridValueProvider
		implements ValueProvider<ValidationReport, CustomComponent>
{
	@Serial
	private static final long serialVersionUID = 5087782841088695356L;
	private static final int ELEMENT_SIZE = 30;

	private final List<Resource.V10> documentResources;

	public ValidationReportGridValueProvider( List<Resource.V10> documentResources )
	{
		this.documentResources = documentResources;
	}

	@Override
	public CustomComponent apply( ValidationReport report )
	{
		var validationReport = report.validationReport();


		var documentLabel = new Label();
		documentLabel.setCaption( "Document" );
		documentResources.stream()
				.filter( r -> r.getUri().equals( validationReport.getDocumentUri() ) ).findFirst()
				.ifPresentOrElse( r -> documentLabel.setValue( r.getLabel() ),
						() -> documentLabel.setValue( validationReport.getDocumentUri().toString() ) );

		var constraintViolations = validationReport.getConstraintViolations();
		var schemaViolationGrid = new Grid<SAXParseException>();
		schemaViolationGrid.setCaption( "Schema Violations" );
		schemaViolationGrid.setHeaderVisible( false );
		schemaViolationGrid.setStyleName( ValoTheme.TABLE_BORDERLESS );
		schemaViolationGrid.setSizeFull();
		schemaViolationGrid.setSelectionMode( SelectionMode.NONE );
		schemaViolationGrid.setItems( report.validationErrors() );
		schemaViolationGrid.addColumn( SAXException::toString );
		schemaViolationGrid.setHeight( min( max( ( report.validationErrors().size() + 1 ) * ELEMENT_SIZE, ELEMENT_SIZE ), 200 ), Unit.PIXELS );

		Grid<ConstraintViolationV0> constraintViolationGrid = new Grid<>();
		constraintViolationGrid.setCaption( "Constraint Violations" );
		constraintViolationGrid.setHeaderVisible( false );
		constraintViolationGrid.setStyleName( ValoTheme.TABLE_BORDERLESS );
		constraintViolationGrid.setSizeFull();
		constraintViolationGrid.setSelectionMode( SelectionMode.NONE );
		constraintViolationGrid.setItems( constraintViolations );
		constraintViolationGrid.addColumn( ConstraintViolationV0::getMessage );
		constraintViolationGrid.setHeight( min( max( ( constraintViolations.size() + 1 ) * ELEMENT_SIZE, ELEMENT_SIZE ), 400 ), Unit.PIXELS );

		var formLayout = new FormLayout();
		formLayout.addComponent( documentLabel );
		formLayout.addComponent( schemaViolationGrid );
		formLayout.addComponent( constraintViolationGrid );
		return new CustomComponent( formLayout );
	}
}
