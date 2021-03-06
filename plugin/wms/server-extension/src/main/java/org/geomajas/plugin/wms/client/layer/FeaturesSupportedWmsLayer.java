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

package org.geomajas.plugin.wms.client.layer;

import com.google.gwt.core.client.Callback;
import org.geomajas.annotation.Api;
import org.geomajas.geometry.Coordinate;
import org.geomajas.geometry.Geometry;
import org.geomajas.gwt2.client.map.attribute.AttributeDescriptor;
import org.geomajas.gwt2.client.map.feature.Feature;
import org.geomajas.gwt2.client.map.layer.FeaturesSupported;
import org.geomajas.plugin.wms.client.service.WmsService.GetFeatureInfoFormat;

import java.util.List;

/**
 * <p> Extension of the default {@link WmsLayer} that also supports features. Since a WMS service can use either a
 * raster data set or a vector data set, we too make that distinction here. If you know that a WMS services uses a
 * vector data set, this layer implementation will give you some more options. </p> <p> These extra options include a
 * WMS GetFeatureInfo request and general feature selection and filtering. Note though that filtering is not a default
 * WMS construct and may not always work. Some WMS servers will support it though. </p>
 *
 * @author Pieter De Graef
 * @author An Buyle
 * @since 2.0.0
 */
@Api(allMethods = true)
public interface FeaturesSupportedWmsLayer extends WmsLayer, FeaturesSupported {

	/**
	 * Get an ordered list of attribute descriptors that describe the attributes of features that belong to this layer.
	 *
	 * @return The list of attribute descriptors that describe this layer.
	 */
	List<AttributeDescriptor> getAttributeDescriptors();

	/**
	 * Execute a WMS GetFeatureInfo request.
	 *
	 * @param location The location in world space to get information for.
	 * @param callback The callback that is executed when the response returns. If features are found at the requested
	 *                 location, they will be returned here.
	 */
	void getFeatureInfo(Coordinate location, Callback<List<Feature>, String> callback);

	/**
	 * Execute a WMS GetFeatureInfo request.
	 *
	 * @param location The location in world space to get information for.
	 * @param format   The format for the response.
	 * @param callback The callback that is executed when the response returns. If features are found at the requested
	 *                 location, they will be returned here. Note that the callback returns a string on success. It is
	 *                 up to you to parse this.
	 */
	void getFeatureInfo(Coordinate location, GetFeatureInfoFormat format, Callback<Object, String> callback);

	/**
	 * Search for features at the given location.
	 *
	 * @param coordinate The location to search at.
	 * @param tolerance  Tolerance in pixels.
	 * @param callback   Callback containing the features.
	 */
	void searchFeatures(Coordinate coordinate, double tolerance, Callback<List<Feature>, String> callback);

	/**
	 * Search for features at the given location.
	 *
	 * @param geometry The location to search at.
	 * @param callback Callback containing the features.
	 */
	void searchFeatures(Geometry geometry, Callback<List<Feature>, String> callback);
}
