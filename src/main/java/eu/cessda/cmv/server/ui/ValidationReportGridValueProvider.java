package eu.cessda.cmv.server.ui;

import java.util.List;

import org.gesis.commons.resource.Resource;

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

	private List<Resource.V10> documentResources;

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
