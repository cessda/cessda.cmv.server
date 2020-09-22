package eu.cessda.cmv.server.ui;

import static com.vaadin.ui.Alignment.MIDDLE_RIGHT;
import static com.vaadin.ui.Notification.show;
import static com.vaadin.ui.Notification.Type.ERROR_MESSAGE;

import java.io.PrintWriter;
import java.io.StringWriter;

import com.vaadin.jsclipboard.JSClipboard;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

@SuppressWarnings( "squid:S110" )
class ErrorWindow extends Window
{
	private static final long serialVersionUID = -7126192827853434568L;

	ErrorWindow( com.vaadin.server.ErrorEvent event )
	{
		super( "Oops, something went wrong!" );
		setHeight( 80, Unit.PERCENTAGE );
		setWidth( 80, Unit.PERCENTAGE );
		setResizable( false );
		setModal( true );
		center();

		TextField textField = new TextField();
		textField.setCaption( "Message" );
		textField.setValue( getMessage( event.getThrowable() ) );
		textField.setReadOnly( true );
		textField.setWidthFull();

		TextArea textArea = new TextArea();
		textArea.setCaption( "StackTrace" );
		textArea.setValue( getStackTrace( event.getThrowable() ) );
		textArea.setSizeFull();
		textArea.setId( "tocopie" );
		textArea.setReadOnly( true );

		HorizontalLayout horizontalLayout = new HorizontalLayout();
		horizontalLayout.setWidthFull();
		Button clipBoardButton = new Button();
		clipBoardButton.setCaption( "Copy to clipboard" );
		JSClipboard clipboard = new JSClipboard();
		clipboard.apply( clipBoardButton, textArea );
		clipboard.addErrorListener( () -> show( "Copy to clipboard failed", ERROR_MESSAGE ) );
		horizontalLayout.addComponent( clipBoardButton );
		horizontalLayout.setComponentAlignment( clipBoardButton, MIDDLE_RIGHT );
		horizontalLayout.setExpandRatio( clipBoardButton, 1.0f );

		Button closeButton = new Button();
		closeButton.setCaption( "Close" );
		closeButton.addClickListener( listener -> this.close() );
		horizontalLayout.addComponent( closeButton );
		horizontalLayout.setComponentAlignment( closeButton, Alignment.MIDDLE_RIGHT );

		VerticalLayout verticalLayout = new VerticalLayout();
		verticalLayout.setSizeFull();
		setContent( verticalLayout );
		verticalLayout.addComponent( textField );
		verticalLayout.addComponent( textArea );
		verticalLayout.addComponent( horizontalLayout );
		verticalLayout.setExpandRatio( textArea, 1.0f );
	}

	private String getMessage( Throwable throwable )
	{
		for ( Throwable t = throwable; t != null; t = t.getCause() )
		{
			if ( t.getCause() == null )
			{
				return t.getClass().getName() + ": " + t.getMessage();
			}
		}
		return "Unknown";
	}

	private String getStackTrace( Throwable throwable )
	{
		StringWriter stringWriter = new StringWriter();
		if ( throwable != null )
		{
			PrintWriter printWriter = new PrintWriter( stringWriter );
			throwable.printStackTrace( printWriter );
		}
		return stringWriter.toString();
	}
}
