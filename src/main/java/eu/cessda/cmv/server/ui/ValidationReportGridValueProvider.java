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
import eu.cessda.cmv.core.mediatype.validationreport.v0.ValidationReportV0;
import org.gesis.commons.resource.Resource;

import java.io.Serial;
import java.util.List;

public class ValidationReportGridValueProvider
		implements ValueProvider<ValidationReportV0, CustomComponent>
{
	@Serial
	private static final long serialVersionUID = 5087782841088695356L;

	private final List<Resource.V10> documentResources;

	public ValidationReportGridValueProvider( List<Resource.V10> documentResources )
	{
		this.documentResources = documentResources;
	}

	@Override
	public CustomComponent apply( ValidationReportV0 validationReport )
	{
		Label documentLabel = new Label();
		documentLabel.setCaption( "Document" );
		documentResources.stream()
				.filter( r -> r.getUri().equals( validationReport.getDocumentUri() ) ).findFirst()
				.ifPresentOrElse( r -> documentLabel.setValue( r.getLabel() ),
						() -> documentLabel.setValue( validationReport.getDocumentUri().toString() ) );
		Grid<ConstraintViolationV0> constraintViolationGrid = new Grid<>();
		constraintViolationGrid.setCaption( "Constraint Violations" );
		constraintViolationGrid.setHeaderVisible( false );
		constraintViolationGrid.setStyleName( ValoTheme.TABLE_BORDERLESS );
		constraintViolationGrid.setSizeFull();
		constraintViolationGrid.setSelectionMode( SelectionMode.NONE );
		constraintViolationGrid.setItems( validationReport.getConstraintViolations() );
		constraintViolationGrid.addColumn( ConstraintViolationV0::getMessage );
		constraintViolationGrid.setHeight( 400, Unit.PIXELS );

		FormLayout formLayout = new FormLayout();
		formLayout.addComponent( documentLabel );
		formLayout.addComponent( constraintViolationGrid );
		return new CustomComponent( formLayout );
	}
}
