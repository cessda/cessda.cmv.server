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

public class ValidationReportGridValueProvider
		implements ValueProvider<ValidationReportV0, CustomComponent>
{
	private static final long serialVersionUID = 5087782841088695356L;

	@Override
	public CustomComponent apply( ValidationReportV0 validationReport )
	{
		Label documentLabel = new Label();
		documentLabel.setCaption( "Document" );
		documentLabel.setValue( "path to document" );

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
