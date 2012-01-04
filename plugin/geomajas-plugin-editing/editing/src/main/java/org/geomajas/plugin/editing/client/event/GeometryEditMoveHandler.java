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

package org.geomajas.plugin.editing.client.event;

import org.geomajas.annotation.FutureApi;
import org.geomajas.annotation.UserImplemented;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

/**
 * Interface for handling move events during geometry editing. The Geometry indices will point to the locations
 * (vertices/edges/sub-geometries) where coordinates have been translated.
 * 
 * @author Pieter De Graef
 * @since 1.0.0
 */
@FutureApi(allMethods = true)
@UserImplemented
public interface GeometryEditMoveHandler extends EventHandler {

	GwtEvent.Type<GeometryEditMoveHandler> TYPE = new GwtEvent.Type<GeometryEditMoveHandler>();

	/**
	 * Executed when coordinates have been translated in the geometry during editing.
	 * 
	 * @param event
	 *            The geometry edit move event.
	 */
	void onGeometryEditMove(GeometryEditMoveEvent event);
}