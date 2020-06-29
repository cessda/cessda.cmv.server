package eu.cessda.cmv.server;

import java.io.IOException;

import javax.annotation.PostConstruct;

import com.vaadin.navigator.View;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.ui.Button;
import com.vaadin.ui.VerticalLayout;

@UIScope
@SpringView( name = ValidationView.VIEW_NAME )
public class ValidationView extends VerticalLayout implements View
{
	private static final long serialVersionUID = -5924926837826583950L;

	public static final String VIEW_NAME = "validation";

	@PostConstruct
	public void init() throws IOException
	{
		setSizeFull();
		addComponent( new Button() );
	}
}
