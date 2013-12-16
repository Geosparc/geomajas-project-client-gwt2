/*
 * This is part of Geomajas, a GIS framework, http://www.geomajas.org/.
 *
 * Copyright 2008-2013 Geosparc nv, http://www.geosparc.com/, Belgium.
 *
 * The program is available in open source according to the GNU Affero
 * General Public License. All contributions in this program are covered
 * by the Geomajas Contributors License Agreement. For full licensing
 * details, see LICENSE.txt in the project root.
 */

package org.geomajas.gwt2.client;

import org.geomajas.annotation.Api;
import org.geomajas.gwt2.client.gfx.GfxUtil;
import org.geomajas.gwt2.client.gfx.GfxUtilImpl;
import org.geomajas.gwt2.client.map.MapOptions;
import org.geomajas.gwt2.client.map.MapPresenter;
import org.geomajas.gwt2.client.map.MapPresenterImpl;

import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.SimpleEventBus;

/**
 * Geomajas starting point. This class allows you to request singleton services or create new instances.
 * 
 * @author Pieter De Graef
 * @since 1.0.0
 */
@Api(allMethods = true)
public final class GeomajasImpl implements Geomajas {

	private static Geomajas instance;

	private EventBus eventBus;

	private GeomajasImpl() {
	}

	/**
	 * Get a singleton instance.
	 * 
	 * @return Return Geomajas!
	 */
	public static Geomajas getInstance() {
		if (instance == null) {
			instance = new GeomajasImpl();
		}
		return instance;
	}

	/**
	 * Apply a new singleton instance. This method provides a way to overwrite the default Geomajas implementations.
	 * 
	 * @param geomajas
	 *            The new Geomajas starting point.
	 */
	public static void setInstance(Geomajas geomajas) {
		instance = geomajas;
	}

	@Override
	public MapPresenter createMapPresenter() {
		return new MapPresenterImpl(getEventBus());
	}

	@Override
	public MapPresenter createMapPresenter(MapOptions mapOptions, int mapWidth, int mapHeight) {
		MapPresenterImpl mapPresenter = new MapPresenterImpl(getEventBus());
		mapPresenter.setSize(mapWidth, mapHeight);
		mapPresenter.initialize(mapOptions);
		return mapPresenter;
	}

	@Override
	public GfxUtil getGfxUtil() {
		return GfxUtilImpl.getInstance();
	}

	@Override
	public EventBus getEventBus() {
		if (eventBus == null) {
			eventBus = new SimpleEventBus();
		}
		return eventBus;
	}
}