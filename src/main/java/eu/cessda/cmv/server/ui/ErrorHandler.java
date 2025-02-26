/*-
 * #%L
 * CESSDA Metadata Validator
 * %%
 * Copyright (C) 2020 - 2025 CESSDA ERIC
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

import com.vaadin.server.DefaultErrorHandler;
import com.vaadin.ui.UI;

import java.io.Serial;

class ErrorHandler extends DefaultErrorHandler {
	@Serial
	private static final long serialVersionUID = -3222914297712190571L;

	private final UI ui;

	public ErrorHandler(UI ui)
	{
		this.ui = ui;
	}

	@Override
	public void error(com.vaadin.server.ErrorEvent event) {
		// Construct the error window from the given locale
		var locale = ui.getLocale();
		var errorWindow = new ErrorWindow( event, locale );

		// Add the UI to the window
		ui.addWindow( errorWindow );
		doDefault( event );
	}
}
