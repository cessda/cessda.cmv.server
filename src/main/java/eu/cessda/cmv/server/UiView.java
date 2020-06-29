package eu.cessda.cmv.server;

import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.navigator.Navigator;
import com.vaadin.server.VaadinRequest;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.spring.navigator.SpringViewProvider;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

@SpringUI( path = "/ui" )
public class UiView extends UI
{
	private static final long serialVersionUID = 5352286420346188519L;

	@Autowired
	private SpringViewProvider viewProvider;

	@Override
	protected void init( VaadinRequest request )
	{
		VerticalLayout verticalLayout = new VerticalLayout();
		verticalLayout.setMargin( false );
		verticalLayout.setSpacing( false );
		verticalLayout.setSizeFull();
		setContent( verticalLayout );

		Navigator navigator = new Navigator( this, verticalLayout );
		navigator.addProvider( viewProvider );
		navigator.navigateTo( ValidationView.VIEW_NAME );
	}
}
