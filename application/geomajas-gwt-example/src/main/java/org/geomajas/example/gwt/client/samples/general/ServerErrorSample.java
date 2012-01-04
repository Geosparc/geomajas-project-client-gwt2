/*
 * This is part of Geomajas, a GIS framework, http://www.geomajas.org/.
 *
 * Copyright 2008-2012 Geosparc nv, http://www.geosparc.com/, Belgium.
 *
 * The program is available in open source according to the GNU Affero
 * General Public License. All contributions in this program are covered
 * by the Geomajas Contributors License Agreement. For full licensing
 * details, see LICENSE.txt in the project root.
 */

package org.geomajas.example.gwt.client.samples.general;

import org.geomajas.command.CommandResponse;
import org.geomajas.command.EmptyCommandRequest;
import org.geomajas.example.gwt.client.samples.base.SamplePanel;
import org.geomajas.example.gwt.client.samples.base.SamplePanelFactory;
import org.geomajas.example.gwt.client.samples.i18n.I18nProvider;
import org.geomajas.gwt.client.command.CommandCallback;
import org.geomajas.gwt.client.command.GwtCommand;
import org.geomajas.gwt.client.command.GwtCommandDispatcher;

import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.IButton;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.layout.VLayout;

/**
 * <p>
 * Sample that displays an error messages when a command on the server has failed.
 * </p>
 * 
 * @author Pieter De Graef
 */
public class ServerErrorSample extends SamplePanel {

	public static final String TITLE = "ServerErrorMessage";

	public static final SamplePanelFactory FACTORY = new SamplePanelFactory() {

		public SamplePanel createPanel() {
			return new ServerErrorSample();
		}
	};

	public Canvas getViewPanel() {
		VLayout layout = new VLayout();
		layout.setWidth100();
		layout.setHeight100();
		layout.setLayoutTopMargin(200);

		IButton button = new IButton(I18nProvider.getSampleMessages().serverErrorButton());
		button.setIcon("[ISOMORPHIC]/geomajas/osgeo/help-contents.png");
		button.setWidth(300);
		button.setLayoutAlign(Alignment.CENTER);
		button.addClickHandler(new ClickHandler() {

			public void onClick(ClickEvent event) {
				GwtCommand command = new GwtCommand("example.gwt.server.samples.GetExceptionCommand");
				command.setCommandRequest(new EmptyCommandRequest());
				GwtCommandDispatcher.getInstance().execute(command, new CommandCallback() {

					public void execute(CommandResponse response) {
						// Do nothing... an error message is shown automatically...
					}
				});
			}
		});
		layout.addMember(button);

		return layout;
	}

	public String getDescription() {
		return I18nProvider.getSampleMessages().serverErrorDescription();
	}

	public String getSourceFileName() {
		return "classpath:org/geomajas/example/gwt/client/samples/general/ServerErrorSample.txt";
	}

	public String[] getConfigurationFiles() {
		return new String[] {};
	}

	public String ensureUserLoggedIn() {
		return "luc";
	}
}
