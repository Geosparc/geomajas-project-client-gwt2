/*
 * This is part of Geomajas, a GIS framework, http://www.geomajas.org/.
 *
 * Copyright 2008-2014 Geosparc nv, http://www.geosparc.com/, Belgium.
 *
 * The program is available in open source according to the GNU Affero
 * General Public License. All contributions in this program are covered
 * by the Geomajas Contributors License Agreement. For full licensing
 * details, see LICENSE.txt in the project root.
 */

package org.geomajas.plugin.editing.client.event;

import java.util.List;

import org.geomajas.annotation.Api;
import org.geomajas.geometry.Geometry;
import org.geomajas.plugin.editing.client.service.GeometryIndex;

/**
 * Event which is passed when some part of a geometry has been moved during geometry editing.
 * 
 * @author Pieter De Graef
 * @since 2.0.0
 */
@Api(allMethods = true)
public class GeometryEditMoveEvent extends AbstractGeometryEditEvent<GeometryEditMoveHandler> {

	/**
	 * Main constructor.
	 * 
	 * @param geometry
	 *            geometry
	 * @param indices
	 *            indices
	 */
	public GeometryEditMoveEvent(Geometry geometry, List<GeometryIndex> indices) {
		super(geometry, indices);
	}

	/**
	 * Get the current editing state.
	 * 
	 * @return Returns the current editing state.
	 */
	public Type<GeometryEditMoveHandler> getAssociatedType() {
		return GeometryEditMoveHandler.TYPE;
	}

	protected void dispatch(GeometryEditMoveHandler handler) {
		handler.onGeometryEditMove(this);
	}
}