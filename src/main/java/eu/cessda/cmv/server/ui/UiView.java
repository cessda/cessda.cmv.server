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

import com.vaadin.annotations.JavaScript;
import com.vaadin.annotations.StyleSheet;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.navigator.NavigationStateManager;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.ViewProvider;
import com.vaadin.server.ThemeResource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.viritin.layouts.MCssLayout;
import org.vaadin.viritin.layouts.MVerticalLayout;

import java.io.Serial;
import java.util.Locale;

@Title( "CESSDA Metadata Validator" )
@StyleSheet( { "https://fonts.googleapis.com/css?family=Source+Sans+Pro:100,200,300,400,500,600,700,800,900" } )
@JavaScript( "https://cessda.atlassian.net/s/d41d8cd98f00b204e9800998ecf8427e-T/sb53l8/b/24/a44af77267a987a660377e5c46e0fb64/_/download/batch/com.atlassian.jira.collector.plugin.jira-issue-collector-plugin:issuecollector/com.atlassian.jira.collector.plugin.jira-issue-collector-plugin:issuecollector.js?locale=en-GB&collectorId=0dd601e4" )
@Theme( "cmv" )
@SpringUI( path = "/" )
@SuppressWarnings( "java:S2160" )
public class UiView extends UI
{
	@Serial
	private static final long serialVersionUID = 5352286420346188519L;

	public static final String CONTAINER = "container";

	@Autowired
	private ViewProvider viewProvider;

	@Override
	protected void init( VaadinRequest request )
	{
		setErrorHandler( new ErrorHandler() );
		Navigator navigator = new Navigator( this, newViewContainer() );
		navigator.addProvider( viewProvider );
		this.setNavigator( navigator );
		NavigationStateManager stateManager = new Navigator.UriFragmentManager( getPage() );
		stateManager.setState( ValidationView.VIEW_NAME );
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
		countryBox.addValueChangeListener( e -> setLocale( new Locale( e.getValue() ) ) );
		countryBox.setItemCaptionGenerator( item -> item.charAt( 0 ) + item.substring( 1 ).toLowerCase() );
		countryBox.setVisible( false );
		Embedded embeddedLogo = new Embedded( null, new ThemeResource( "img/logo/cessda_logo_cmv.svg" ) );
		embeddedLogo.setWidth( "100%" );
		MCssLayout headerMiddleContent = new MCssLayout()
				.withStyleName( "row header-content" )
				.withFullWidth();
		MCssLayout headerMiddle = new MCssLayout()
				.withFullWidth()
				.withStyleName( "common-header" )
				.add( new MCssLayout().withStyleName( CONTAINER ).add( headerMiddleContent ) );
		MCssLayout headerBottom = new MCssLayout()
				.withFullWidth()
				.withStyleName( "invis" )
				.add( new MCssLayout().withStyleName( CONTAINER ) );
		MCssLayout headerBar = new MCssLayout()
				.withResponsive( true )
				.withStyleName( "header-cessda" )
				.add( headerMiddle, headerBottom );
		CustomLayout header = new CustomLayout( "header" );
		header.setStyleName( "header" );
		header.setWidth( 100, Unit.PERCENTAGE );
		CustomLayout footer = new CustomLayout( "footer" );
		footer.setStyleName( "footer" );
		footer.setWidth( 100, Unit.PERCENTAGE );
		MVerticalLayout viewContainer = new MVerticalLayout()
				.withFullWidth()
				.withMargin( false )
				.withId( "viewcont" )
				.withStyleName( "content" );
		MVerticalLayout root = new MVerticalLayout()
				.withResponsive( true )
				.with( header, headerBar, viewContainer, footer )
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
