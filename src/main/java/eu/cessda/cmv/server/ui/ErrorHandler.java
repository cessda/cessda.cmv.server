package eu.cessda.cmv.server.ui;

import com.vaadin.server.DefaultErrorHandler;
import com.vaadin.ui.UI;

class ErrorHandler extends DefaultErrorHandler
{
	private static final long serialVersionUID = -3222914297712190571L;

	@Override
	public void error( com.vaadin.server.ErrorEvent event )
	{
		UI.getCurrent().addWindow( new ErrorWindow( event ) );
		doDefault( event );
	}
}
