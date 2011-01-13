/*
 * This is part of Geomajas, a GIS framework, http://www.geomajas.org/.
 *
 * Copyright 2008-2011 Geosparc nv, http://www.geosparc.com/, Belgium.
 *
 * The program is available in open source according to the GNU Affero
 * General Public License. All contributions in this program are covered
 * by the Geomajas Contributors License Agreement. For full licensing
 * details, see LICENSE.txt in the project root.
 */

package org.geomajas.example.gwt.client.samples.mapwidget;

import org.geomajas.example.gwt.client.samples.base.SamplePanel;
import org.geomajas.example.gwt.client.samples.base.SamplePanelFactory;
import org.geomajas.example.gwt.client.samples.i18n.I18nProvider;
import org.geomajas.gwt.client.Geomajas;
import org.geomajas.gwt.client.controller.PanController;
import org.geomajas.gwt.client.gfx.paintable.Image;
import org.geomajas.gwt.client.gfx.style.PictureStyle;
import org.geomajas.gwt.client.spatial.Bbox;
import org.geomajas.gwt.client.widget.MapWidget;
import org.geomajas.gwt.client.widget.MapWidget.RenderGroup;
import org.geomajas.gwt.client.widget.MapWidget.RenderStatus;

import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.IButton;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;

/**
 * Sample that shows the difference in rendering in screen space versus world space.
 * 
 * @author Pieter De Graef
 */
public class WorldScreenSample extends SamplePanel {

	public static final String TITLE = "ScreenVersusWorld";

	public static final SamplePanelFactory FACTORY = new SamplePanelFactory() {

		public SamplePanel createPanel() {
			return new WorldScreenSample();
		}
	};

	public Canvas getViewPanel() {
		VLayout layout = new VLayout();
		layout.setWidth100();
		layout.setHeight100();
		layout.setMembersMargin(10);

		// Create map with OSM layer, and add a PanController to it:
		VLayout mapLayout = new VLayout();
		mapLayout.setShowEdges(true);
		mapLayout.setHeight("60%");

		final MapWidget map = new MapWidget("mapOsm", "gwt-samples");
		map.setController(new PanController(map));
		mapLayout.addMember(map);

		// Create a button layout:
		HLayout buttonLayout = new HLayout();
		buttonLayout.setHeight(25);
		buttonLayout.setMembersMargin(10);
		IButton button1 = new IButton(I18nProvider.getSampleMessages().screenWorldBTNScreen());
		button1.setWidth("50%");
		
		final Image screenImage = new Image("imageInScreenSpace");
		screenImage.setHref(Geomajas.getIsomorphicDir() + "geomajas/example/images/smile.png");
		screenImage.setBounds(new Bbox(60, 60, 48, 48)); // Pixel coordinates
		screenImage.setStyle(new PictureStyle(0.6));
		
		button1.addClickHandler(new ClickHandler() {

			// Draw an image in screen space:
			public void onClick(ClickEvent event) {
				map.render(screenImage, RenderGroup.SCREEN, RenderStatus.ALL);
			}
		});
		buttonLayout.addMember(button1);

		IButton button2 = new IButton(I18nProvider.getSampleMessages().screenWorldBTNWorld());
		button2.setWidth("50%");
		
		final Image worldImage = new Image("imageInWorldSpace");
		worldImage.setHref(Geomajas.getIsomorphicDir() + "geomajas/example/images/smile.png");
		worldImage.setBounds(new Bbox(-2000000, -2000000, 4000000, 4000000)); // Mercator coordinates
		worldImage.setStyle(new PictureStyle(0.8));
		
		button2.addClickHandler(new ClickHandler() {

			// Draw an image in world space:
			public void onClick(ClickEvent event) {
				map.registerWorldPaintable(worldImage);
			}
		});
		buttonLayout.addMember(button2);

		// Create a second button layout (delete buttons):
		HLayout buttonLayout2 = new HLayout();
		buttonLayout2.setMembersMargin(10);
		IButton button3 = new IButton(I18nProvider.getSampleMessages().screenWorldBTNScreenDelete());
		button3.setWidth("50%");
		button3.addClickHandler(new ClickHandler() {

			// Delete the image in screen space:
			public void onClick(ClickEvent event) {
				map.render(screenImage, RenderGroup.SCREEN, RenderStatus.DELETE);
			}
		});
		buttonLayout2.addMember(button3);

		IButton button4 = new IButton(I18nProvider.getSampleMessages().screenWorldBTNWorldDelete());
		button4.setWidth("50%");
		button4.addClickHandler(new ClickHandler() {

			// Delete the image in world space:
			public void onClick(ClickEvent event) {
				map.unregisterWorldPaintable(worldImage);
			}
		});
		buttonLayout2.addMember(button4);
		
		// Place both in the layout:
		layout.addMember(mapLayout);
		layout.addMember(buttonLayout);
		layout.addMember(buttonLayout2);

		return layout;
	}

	public String getDescription() {
		return I18nProvider.getSampleMessages().screenWorldDescription();
	}

	public String getSourceFileName() {
		return "classpath:org/geomajas/example/gwt/client/samples/mapwidget/WorldScreenSample.txt";
	}

	public String[] getConfigurationFiles() {
		return new String[] { "WEB-INF/layerOsm.xml",
				"WEB-INF/mapOsm.xml" };
	}

	public String ensureUserLoggedIn() {
		return "luc";
	}
}
