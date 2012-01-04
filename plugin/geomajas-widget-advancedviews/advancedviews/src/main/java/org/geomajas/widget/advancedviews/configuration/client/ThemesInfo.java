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
package org.geomajas.widget.advancedviews.configuration.client;

import java.util.List;

import org.geomajas.configuration.client.ClientWidgetInfo;
import org.geomajas.widget.advancedviews.configuration.client.themes.ViewConfig;

/**
 * @author Oliver May
 *
 */
public class ThemesInfo implements ClientWidgetInfo {

	/**
	 * Use this identifier in your configuration files (beans).
	 */
	public static final String IDENTIFIER = "ThemesInfo";

	private static final long serialVersionUID = 100L;
	private List<ViewConfig> themeConfigs;

	/**
	 * If true, hide available layers that are not in the configuration.
	 */
	private boolean hideOtherlayers; // false
	
	/**
	 * If true show description next to the button (false == tooltip).
	 */
	private boolean showDescription; // false
	
	/**
	 * Width of the label showing the description. 
	 * Can be any css accepted width formulation (100px, 50%, 10em).
	 */
	private String descriptionWidth = "300px";

	public boolean isHideOtherlayers() {
		return hideOtherlayers;
	}

	public void setHideOtherlayers(boolean hideOtherlayers) {
		this.hideOtherlayers = hideOtherlayers;
	}

	/**
	 * @param themeConfigs the themeConfigs to set
	 */
	public void setThemeConfigs(List<ViewConfig> themeConfigs) {
		this.themeConfigs = themeConfigs;
	}

	/**
	 * @return the themeConfigs
	 */
	public List<ViewConfig> getThemeConfigs() {
		return themeConfigs;
	}

	public boolean isShowDescription() {
		return showDescription;
	}

	public void setShowDescription(boolean showDescription) {
		this.showDescription = showDescription;
	}

	public String getDescriptionWidth() {
		return descriptionWidth;
	}

	public void setDescriptionWidth(String descriptionWidth) {
		this.descriptionWidth = descriptionWidth;
	}
}
