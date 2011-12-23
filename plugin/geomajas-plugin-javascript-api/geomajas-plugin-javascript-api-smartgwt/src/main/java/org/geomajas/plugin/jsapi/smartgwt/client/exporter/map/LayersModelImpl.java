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
package org.geomajas.plugin.jsapi.smartgwt.client.exporter.map;

import org.geomajas.annotation.Api;
import org.geomajas.gwt.client.map.MapModel;
import org.geomajas.plugin.jsapi.client.map.LayersModel;
import org.geomajas.plugin.jsapi.client.map.layer.Layer;
import org.geomajas.plugin.jsapi.smartgwt.client.exporter.map.layer.LayerImpl;
import org.geomajas.plugin.jsapi.smartgwt.client.exporter.map.layer.VectorLayer;
import org.timepedia.exporter.client.Export;
import org.timepedia.exporter.client.ExportPackage;
import org.timepedia.exporter.client.Exportable;

/**
 * Exportable facade for {@link LayersModel}. This implementation uses the {@link MapModel} to delegate to.
 * 
 * @author Oliver May
 * @author Pieter De Graef
 */
@Export("LayersModel")
@ExportPackage("org.geomajas.jsapi.map")
public class LayersModelImpl implements Exportable, LayersModel {

	private MapModel mapModel;

	public LayersModelImpl() {
	}

	/**
	 * Construct the wrapper based on the given mapModel.
	 * 
	 * @param mapModel
	 *            the mapModel to wrap.
	 * @since 1.0.0
	 */
	@Api
	public LayersModelImpl(MapModel mapModel) {
		this.mapModel = mapModel;
	}

	// ------------------------------------------------------------------------
	// LayersModel implementation:
	// ------------------------------------------------------------------------

	/** {@inheritDoc} */
	public Layer getLayer(String layerId) {
		org.geomajas.gwt.client.map.layer.Layer<?> layer = mapModel.getLayer(layerId);
		if (layer instanceof org.geomajas.gwt.client.map.layer.VectorLayer) {
			return new VectorLayer((org.geomajas.gwt.client.map.layer.VectorLayer) layer);
		}
		return new LayerImpl(layer);
	}

	/** {@inheritDoc} */
	public org.geomajas.plugin.jsapi.client.map.layer.Layer getLayerAt(int index) {
		org.geomajas.gwt.client.map.layer.Layer<?> layer = mapModel.getLayers().get(index);
		if (layer instanceof org.geomajas.gwt.client.map.layer.VectorLayer) {
			return new VectorLayer((org.geomajas.gwt.client.map.layer.VectorLayer) layer);
		}
		return new LayerImpl(layer);
	}

	/** {@inheritDoc} */
	public int getLayerCount() {
		return mapModel.getLayers().size();
	}
}