/*
 * This file is part of Geomajas, a component framework for building
 * rich Internet applications (RIA) with sophisticated capabilities for the
 * display, analysis and management of geographic information.
 * It is a building block that allows developers to add maps
 * and other geographic data capabilities to their web applications.
 *
 * Copyright 2008-2010 Geosparc, http://www.geosparc.com, Belgium
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.geomajas.gwt.client.samples.base;

import org.geomajas.gwt.client.samples.LoginSample;
import org.geomajas.gwt.client.samples.OpenStreetMapSample;
import org.geomajas.gwt.client.samples.i18n.I18nProvider;

/**
 * <p>
 * List of all test samples available in the <code>TreeGrid</code> on the left side.
 * 
 * @author Pieter De Graef
 */
public final class SampleArray {

	private SampleArray() {
	}

	public static SampleTreeNode[] getSampleArray() {
		return new SampleTreeNode[] {
				new SampleTreeNode("MapWidget", "/images/world.png", "MapWidget", "topLevel"),
				new SampleTreeNode(new OpenStreetMapSample(), "OSM", "MapWidget"),
				new SampleTreeNode(I18nProvider.getSampleMessages().treeGroupSecurity(),
						"[ISOMORPHIC]/geomajas/silk/key.png", "Security", "topLevel"),
				new SampleTreeNode(new LoginSample(), "Login", "Security") };
	}
}
