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

package org.geomajas.plugin.jsapi.client.map.controller;

import org.geomajas.annotation.Api;
import org.timepedia.exporter.client.Export;
import org.timepedia.exporter.client.ExportClosure;
import org.timepedia.exporter.client.Exportable;

import com.google.gwt.event.dom.client.MouseMoveEvent;

/**
 * JavaScript exportable handler for catching mouse move events.
 * 
 * @author Pieter De Graef
 * @since 1.0.0
 */
@Export
@ExportClosure
@Api(allMethods = true)
public interface MouseMoveHandler extends Exportable {
	
	/**
	 * Executed when a mouse move event occurred.
	 * 
	 * @param event
	 *            The mouse move event.
	 */
	void onMouseMove(MouseMoveEvent event);
}