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

package org.geomajas.puregwt.client.map.gfx;

import org.geomajas.global.FutureApi;
import org.geomajas.puregwt.client.map.ViewPort;

/**
 * A container for drawing vector objects onto the map. This is the recommended way of quickly drawing vector objects.
 * As an extra, this interface also defines a way for such containers to delete themselves again from the map.
 * 
 * @author Pieter De Graef
 * @author Jan De Moerloose
 * @since 1.0.0
 */
@FutureApi
public interface WorldContainer extends VectorContainer {

	/**
	 * Should children of this world container be resized when zooming in or out? Default is true.
	 * 
	 * @param resizeChildren
	 *            Determine if children should be resized when zooming in or out.
	 */
	void setResizeChildren(boolean resizeChildren);

	/**
	 * Transform all objects within this container to match the given view port.
	 * 
	 * @param viewPort
	 *            The view port for whom to transform this container.
	 */
	void transform(ViewPort viewPort);
}