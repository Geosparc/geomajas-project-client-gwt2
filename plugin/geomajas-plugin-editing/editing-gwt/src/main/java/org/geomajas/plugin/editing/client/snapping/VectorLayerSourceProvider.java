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

package org.geomajas.plugin.editing.client.snapping;

import java.util.List;

import org.geomajas.command.dto.SearchByLocationRequest;
import org.geomajas.command.dto.SearchByLocationResponse;
import org.geomajas.geometry.Bbox;
import org.geomajas.geometry.Coordinate;
import org.geomajas.geometry.Geometry;
import org.geomajas.global.GeomajasConstant;
import org.geomajas.gwt.client.command.CommandCallback;
import org.geomajas.gwt.client.command.GwtCommand;
import org.geomajas.gwt.client.command.GwtCommandDispatcher;
import org.geomajas.gwt.client.map.layer.VectorLayer;
import org.geomajas.layer.feature.Feature;

/**
 * Source provider implementation for the snapping service that fetches it's geometries from the features of a vector
 * layer.
 * 
 * @author Pieter De Graef
 */
public class VectorLayerSourceProvider implements SnappingSourceProvider {

	private final VectorLayer layer;

	private Bbox mapBounds;

	public VectorLayerSourceProvider(VectorLayer layer) {
		this.layer = layer;
	}

	/**
	 * Get the geometries of all features within the map view bounds.
	 */
	public void getSnappingSources(final GeometryArrayCallback callback) {
		GwtCommand commandRequest = new GwtCommand(SearchByLocationRequest.COMMAND);
		SearchByLocationRequest request = new SearchByLocationRequest();
		request.setLayerIds(new String[] { layer.getServerLayerId() });
		request.setFeatureIncludes(GeomajasConstant.FEATURE_INCLUDE_GEOMETRY);
		request.setLocation(boundsAsGeometry());
		request.setCrs(layer.getMapModel().getCrs());
		request.setQueryType(SearchByLocationRequest.QUERY_INTERSECTS);
		request.setSearchType(SearchByLocationRequest.SEARCH_ALL_LAYERS);
		commandRequest.setCommandRequest(request);
		GwtCommandDispatcher.getInstance().execute(commandRequest, new CommandCallback<SearchByLocationResponse>() {

			public void execute(SearchByLocationResponse response) {
				for (String serverLayerId : response.getFeatureMap().keySet()) {
					List<Feature> features = response.getFeatureMap().get(serverLayerId);
					Geometry[] geometries = new Geometry[features.size()];
					for (int i = 0; i < features.size(); i++) {
						geometries[i] = features.get(i).getGeometry();
					}
					callback.execute(geometries);
					break;
				}
			}
		});
	}

	public void update(Bbox mapBounds) {
		this.mapBounds = mapBounds;
	}

	private Geometry boundsAsGeometry() {
		Geometry shell = new Geometry(Geometry.LINEAR_RING, 0, 0);
		shell.setCoordinates(new Coordinate[] { new Coordinate(mapBounds.getX(), mapBounds.getY()),
				new Coordinate(mapBounds.getMaxX(), mapBounds.getY()),
				new Coordinate(mapBounds.getMaxX(), mapBounds.getMaxY()),
				new Coordinate(mapBounds.getX(), mapBounds.getMaxY()),
				new Coordinate(mapBounds.getX(), mapBounds.getY()) });

		Geometry polygon = new Geometry(Geometry.POLYGON, 0, 0);
		polygon.setGeometries(new Geometry[] { shell });

		return polygon;
	}
}