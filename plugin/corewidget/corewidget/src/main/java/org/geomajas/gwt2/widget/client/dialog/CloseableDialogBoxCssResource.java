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

package org.geomajas.gwt2.widget.client.dialog;

import org.geomajas.annotation.Api;

import com.google.gwt.resources.client.CssResource;

/**
 * CSS resource bundle that contains all generic styles used in pure gwt geomajas widgets.
 * 
 * @author Dosi Bingov
 * @author Jan De Moerloose
 * @since 2.0.0
 */
@Api(allMethods = true)
public interface CloseableDialogBoxCssResource extends CssResource {

	/**
	 * Get a CSS style class.
	 * @return
	 */
	String closeableDialogCloseIcon();
	
	/**
	 * Get a CSS style class.
	 * @return
	 */
	String closeableDialogTitle();

	/**
	 * Get a CSS style class.
	 * @return
	 */
	String closeableDialogBox();

	/**
	 * Get a CSS style class.
	 * @return
	 */
	String captionBar();
	
}