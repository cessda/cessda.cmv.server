package eu.cessda.cmv.server.ui;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.viritin.button.MButton;
import org.vaadin.viritin.label.MLabel;
import org.vaadin.viritin.layouts.MCssLayout;
import org.vaadin.viritin.layouts.MVerticalLayout;

import com.vaadin.annotations.StyleSheet;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.ViewProvider;
import com.vaadin.server.ThemeResource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.CustomLayout;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

@Title( "CESSDA Metadata Validator" )
@StyleSheet( { "https://fonts.googleapis.com/css?family=Source+Sans+Pro:100,200,300,400,500,600,700,800,900" } )
@Theme( "mytheme" )
@SpringUI( path = "/ui" )
public class UiView extends UI
{
	private static final long serialVersionUID = 5352286420346188519L;

	public static final String CONTAINER = "container";
	public static final String PULL_LEFT = " pull-left";

	@Autowired
	private ViewProvider viewProvider;

	@Override
	protected void init( VaadinRequest request )
	{
		Navigator navigator = new Navigator( this, newViewContainer() );
		// navigator.setErrorView( ErrorView.class );
		navigator.addProvider( viewProvider );
		navigator.navigateTo( ValidationView.VIEW_NAME );
	}

	private VerticalLayout newViewContainer()
	{
		ComboBox<String> countryBox = new ComboBox<>();
		countryBox.setTextInputAllowed( false );
		countryBox.setItems( "English", "German" );
		countryBox.setEmptySelectionAllowed( false );
		countryBox.setStyleName( "language-option" );
		countryBox.setValue( "English" );
		countryBox.setWidth( "100px" );
		countryBox.addValueChangeListener( e -> setLocale( new Locale( e.getValue().toString() ) ) );
		countryBox.setItemCaptionGenerator( item -> item.substring( 0, 1 ) + item.substring( 1 ).toLowerCase() );
		countryBox.setVisible( false );
		MCssLayout headerTopContainer = new MCssLayout()
				.withStyleName( "row" ).withFullWidth()
				.add( new MLabel().withContentMode( ContentMode.HTML ).withStyleName( "col-md-6 social" )
						.withValue(
								"<div class=\"email\"><span>"
										+ "<a id=\"cessdahome\" href=\"https://www.cessda.eu/\" target=\"_blank\">Consortium of European Social Science Data Archives</a>"
										+ "</span></div>" ),
						new MCssLayout().withStyleName( "col-md-6 log-in pull-right" ).add( countryBox ) );
		Embedded embeddedLogo = new Embedded( null, new ThemeResource( "img/logo/cessda_logo_cmv.svg" ) );
		embeddedLogo.setWidth( "100%" );
		MCssLayout headerMiddleContent = new MCssLayout()
				.withStyleName( "row header-content" )
				.withFullWidth()
				.add( new MCssLayout().withStyleName( "col-md-4 logo" ).add( embeddedLogo ) );
		MCssLayout headerTop = new MCssLayout()
				.withFullWidth()
				.withStyleName( "primary-header" )
				.add( new MCssLayout().withStyleName( CONTAINER ).add( headerTopContainer ) );
		MCssLayout headerMiddle = new MCssLayout()
				.withFullWidth()
				.withStyleName( "common-header header-bg" )
				.add( new MCssLayout().withStyleName( CONTAINER ).add( headerMiddleContent ) );
		MButton validationButton = new MButton( "Validation" )
				.withStyleName( ValoTheme.BUTTON_LINK + PULL_LEFT );
		MCssLayout headerBottom = new MCssLayout()
				.withFullWidth()
				.withStyleName( "menu-bg" )
				.add( new MCssLayout().withStyleName( CONTAINER ).add( validationButton ) );
		MCssLayout headerBar = new MCssLayout()
				.withResponsive( true )
				.withStyleName( "header-cessda" )
				.add( headerTop, headerMiddle, headerBottom );
		CustomLayout footer = new CustomLayout( "footer" );
		footer.setStyleName( "footer" );
		footer.setWidth( 100, Unit.PERCENTAGE );
		MVerticalLayout viewContainer = new MVerticalLayout()
				.withFullWidth()
				.withMargin( false )
				.withStyleName( "content" );
		MVerticalLayout root = new MVerticalLayout()
				.withResponsive( true )
				.with( headerBar, viewContainer, footer )
				.withFullWidth()
				.withMargin( false )
				.withSpacing( false )
				.withUndefinedSize()
				.withDefaultComponentAlignment( Alignment.TOP_CENTER )
				.withExpand( viewContainer, 1.0f );
		this.setStyleName( "mainlayout" );
		this.setId( "main-container" );
		this.setContent( root );
		this.setLocale( Locale.ENGLISH );
		getCurrent().setPollInterval( 2000 );
		return viewContainer;
	}
}
