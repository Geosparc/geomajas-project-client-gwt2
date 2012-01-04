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

package org.geomajas.plugin.editing.gwt.client.gfx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geomajas.geometry.Coordinate;
import org.geomajas.geometry.Geometry;
import org.geomajas.gwt.client.controller.GraphicsController;
import org.geomajas.gwt.client.controller.MapEventParser;
import org.geomajas.gwt.client.gfx.GraphicsContext;
import org.geomajas.gwt.client.gfx.paintable.Composite;
import org.geomajas.gwt.client.gfx.style.ShapeStyle;
import org.geomajas.gwt.client.map.event.MapViewChangedEvent;
import org.geomajas.gwt.client.map.event.MapViewChangedHandler;
import org.geomajas.gwt.client.spatial.Bbox;
import org.geomajas.gwt.client.spatial.geometry.LineString;
import org.geomajas.gwt.client.spatial.geometry.LinearRing;
import org.geomajas.gwt.client.spatial.geometry.Point;
import org.geomajas.gwt.client.spatial.geometry.Polygon;
import org.geomajas.gwt.client.util.Dom;
import org.geomajas.gwt.client.util.GeometryConverter;
import org.geomajas.gwt.client.widget.MapWidget;
import org.geomajas.gwt.client.widget.MapWidget.RenderGroup;
import org.geomajas.plugin.editing.client.event.GeometryEditChangeStateEvent;
import org.geomajas.plugin.editing.client.event.GeometryEditChangeStateHandler;
import org.geomajas.plugin.editing.client.event.GeometryEditMoveEvent;
import org.geomajas.plugin.editing.client.event.GeometryEditMoveHandler;
import org.geomajas.plugin.editing.client.event.GeometryEditShapeChangedEvent;
import org.geomajas.plugin.editing.client.event.GeometryEditShapeChangedHandler;
import org.geomajas.plugin.editing.client.event.GeometryEditStartEvent;
import org.geomajas.plugin.editing.client.event.GeometryEditStartHandler;
import org.geomajas.plugin.editing.client.event.GeometryEditStopEvent;
import org.geomajas.plugin.editing.client.event.GeometryEditStopHandler;
import org.geomajas.plugin.editing.client.event.GeometryEditTentativeMoveEvent;
import org.geomajas.plugin.editing.client.event.GeometryEditTentativeMoveHandler;
import org.geomajas.plugin.editing.client.event.state.GeometryIndexDeselectedEvent;
import org.geomajas.plugin.editing.client.event.state.GeometryIndexDeselectedHandler;
import org.geomajas.plugin.editing.client.event.state.GeometryIndexDisabledEvent;
import org.geomajas.plugin.editing.client.event.state.GeometryIndexDisabledHandler;
import org.geomajas.plugin.editing.client.event.state.GeometryIndexEnabledEvent;
import org.geomajas.plugin.editing.client.event.state.GeometryIndexEnabledHandler;
import org.geomajas.plugin.editing.client.event.state.GeometryIndexHighlightBeginEvent;
import org.geomajas.plugin.editing.client.event.state.GeometryIndexHighlightBeginHandler;
import org.geomajas.plugin.editing.client.event.state.GeometryIndexHighlightEndEvent;
import org.geomajas.plugin.editing.client.event.state.GeometryIndexHighlightEndHandler;
import org.geomajas.plugin.editing.client.event.state.GeometryIndexMarkForDeletionBeginEvent;
import org.geomajas.plugin.editing.client.event.state.GeometryIndexMarkForDeletionBeginHandler;
import org.geomajas.plugin.editing.client.event.state.GeometryIndexMarkForDeletionEndEvent;
import org.geomajas.plugin.editing.client.event.state.GeometryIndexMarkForDeletionEndHandler;
import org.geomajas.plugin.editing.client.event.state.GeometryIndexSelectedEvent;
import org.geomajas.plugin.editing.client.event.state.GeometryIndexSelectedHandler;
import org.geomajas.plugin.editing.client.event.state.GeometryIndexSnappingBeginEvent;
import org.geomajas.plugin.editing.client.event.state.GeometryIndexSnappingBeginHandler;
import org.geomajas.plugin.editing.client.event.state.GeometryIndexSnappingEndEvent;
import org.geomajas.plugin.editing.client.event.state.GeometryIndexSnappingEndHandler;
import org.geomajas.plugin.editing.client.handler.AbstractGeometryIndexMapHandler;
import org.geomajas.plugin.editing.client.service.GeometryEditService;
import org.geomajas.plugin.editing.client.service.GeometryEditState;
import org.geomajas.plugin.editing.client.service.GeometryIndex;
import org.geomajas.plugin.editing.client.service.GeometryIndexNotFoundException;
import org.geomajas.plugin.editing.client.service.GeometryIndexType;
import org.geomajas.plugin.editing.client.snap.event.CoordinateSnapEvent;
import org.geomajas.plugin.editing.client.snap.event.CoordinateSnapHandler;
import org.geomajas.plugin.editing.gwt.client.controller.CompositeGeometryIndexController;
import org.geomajas.plugin.editing.gwt.client.handler.EditingHandlerRegistry;

import com.google.gwt.event.shared.HandlerRegistration;
import com.smartgwt.client.types.Cursor;

/**
 * ...
 * 
 * @author Pieter De Graef
 */
public class GeometryRenderer implements GeometryEditStartHandler, GeometryEditStopHandler,
		GeometryIndexHighlightBeginHandler, GeometryIndexHighlightEndHandler, GeometryEditMoveHandler,
		GeometryEditShapeChangedHandler, GeometryEditChangeStateHandler, GeometryIndexSelectedHandler,
		GeometryIndexDeselectedHandler, GeometryIndexDisabledHandler, GeometryIndexEnabledHandler,
		GeometryIndexMarkForDeletionBeginHandler, GeometryIndexMarkForDeletionEndHandler,
		GeometryIndexSnappingBeginHandler, GeometryIndexSnappingEndHandler, GeometryEditTentativeMoveHandler,
		MapViewChangedHandler, CoordinateSnapHandler {

	private static final int VERTEX_SIZE = 12;

	private static final int HALF_VERTEX_SIZE = 6;

	private final MapWidget mapWidget;

	private final GeometryEditService editingService;

	private final Map<String, Composite> groups = new HashMap<String, Composite>();

	private final String insertMoveEdgeId1 = "insert-move-edge1";

	private final String insertMoveEdgeId2 = "insert-move-edge2";

	private StyleService styleService = new DefaultStyleService();

	private String baseName = "editing";

	private HandlerRegistration mapViewRegistration;

	private boolean closeRingWhileInserting;

	public GeometryRenderer(MapWidget mapWidget, GeometryEditService editingService, MapEventParser eventParser) {
		this.mapWidget = mapWidget;
		this.editingService = editingService;
	}

	// ------------------------------------------------------------------------
	// Public methods:
	// ------------------------------------------------------------------------

	public void redraw() {
		groups.clear();
		mapWidget.getVectorContext().deleteGroup(editingService.getGeometry());
		draw(editingService.getGeometry());
	}

	// ------------------------------------------------------------------------
	// MapViewChangedHandler:
	// ------------------------------------------------------------------------

	public void onMapViewChanged(MapViewChangedEvent event) {
		// Slow, but it works:
		redraw();
	}

	// ------------------------------------------------------------------------
	// GeometryEditWorkflowHandler:
	// ------------------------------------------------------------------------

	public void onGeometryEditStart(GeometryEditStartEvent event) {
		// Also look at the map for changes in the MapView:
		mapViewRegistration = mapWidget.getMapModel().getMapView().addMapViewChangedHandler(this);

		// Render the geometry on the map:
		groups.clear();
		draw(event.getGeometry());
	}

	public void onGeometryEditStop(GeometryEditStopEvent event) {
		// Remove the handler that follows changes in the map view:
		if (mapViewRegistration != null) {
			mapViewRegistration.removeHandler();
			mapViewRegistration = null;
		}

		// Cleanup the geometry from the map:
		groups.clear();
		mapWidget.getVectorContext().deleteGroup(event.getGeometry());
	}

	// ------------------------------------------------------------------------
	// GeometryEditSelectionHandler:
	// ------------------------------------------------------------------------

	public void onGeometryIndexSelected(GeometryIndexSelectedEvent event) {
		for (GeometryIndex index : event.getIndices()) {
			update(event.getGeometry(), index);
		}
	}

	public void onGeometryIndexDeselected(GeometryIndexDeselectedEvent event) {
		for (GeometryIndex index : event.getIndices()) {
			update(event.getGeometry(), index);
		}
	}

	// ------------------------------------------------------------------------
	// GeometryEditDisableHandler:
	// ------------------------------------------------------------------------

	public void onGeometryIndexDisabled(GeometryIndexDisabledEvent event) {
		for (GeometryIndex index : event.getIndices()) {
			update(event.getGeometry(), index);
		}
	}

	public void onGeometryIndexEnabled(GeometryIndexEnabledEvent event) {
		for (GeometryIndex index : event.getIndices()) {
			update(event.getGeometry(), index);
		}
	}

	// ------------------------------------------------------------------------
	// GeometryEditHighlightHandler:
	// ------------------------------------------------------------------------

	public void onGeometryIndexHighlightBegin(GeometryIndexHighlightBeginEvent event) {
		for (GeometryIndex index : event.getIndices()) {
			update(event.getGeometry(), index);
		}
	}

	public void onGeometryIndexHighlightEnd(GeometryIndexHighlightEndEvent event) {
		for (GeometryIndex index : event.getIndices()) {
			update(event.getGeometry(), index);
		}
	}

	// ------------------------------------------------------------------------
	// GeometryEditMarkForDeletionHandlers:
	// ------------------------------------------------------------------------

	public void onGeometryIndexMarkForDeletionBegin(GeometryIndexMarkForDeletionBeginEvent event) {
		for (GeometryIndex index : event.getIndices()) {
			update(event.getGeometry(), index);
		}
	}

	public void onGeometryIndexMarkForDeletionEnd(GeometryIndexMarkForDeletionEndEvent event) {
		for (GeometryIndex index : event.getIndices()) {
			update(event.getGeometry(), index);
		}
	}

	// ------------------------------------------------------------------------
	// GeometryEditSnappingHandlers:
	// ------------------------------------------------------------------------

	public void onGeometryIndexSnappingEnd(GeometryIndexSnappingEndEvent event) {
		for (GeometryIndex index : event.getIndices()) {
			update(event.getGeometry(), index);
		}
	}

	public void onGeometryIndexSnappingBegin(GeometryIndexSnappingBeginEvent event) {
		for (GeometryIndex index : event.getIndices()) {
			update(event.getGeometry(), index);
		}
	}

	// ------------------------------------------------------------------------
	// GeometryEditOperationHandler:
	// ------------------------------------------------------------------------

	public void onGeometryEditMove(GeometryEditMoveEvent event) {
		// First find out what exactly must change with the move event (indices + neighbors):
		List<GeometryIndex> toRedraw = new ArrayList<GeometryIndex>();
		for (GeometryIndex index : event.getIndices()) {
			if (!toRedraw.contains(index)) {
				toRedraw.add(index);
				try {
					List<GeometryIndex> neighbors = null;
					switch (editingService.getIndexService().getType(index)) {
						case TYPE_VERTEX:
							neighbors = editingService.getIndexService().getAdjacentEdges(event.getGeometry(), index);
							break;
						case TYPE_EDGE:
							neighbors = editingService.getIndexService()
									.getAdjacentVertices(event.getGeometry(), index);
							break;
						case TYPE_GEOMETRY:
						default:
							neighbors = new ArrayList<GeometryIndex>();
					}
					if (neighbors != null) {
						toRedraw.addAll(neighbors);
					}
				} catch (GeometryIndexNotFoundException e) {
				}
			}

			// Check if we need to draw the background (nice, but slows down):
			if (styleService.getBackgroundStyle() != null && styleService.getBackgroundStyle().getFillOpacity() > 0) {
				if (event.getGeometry().getGeometryType().equals(Geometry.POLYGON)) {
					org.geomajas.gwt.client.spatial.geometry.Geometry transformed = mapWidget.getMapModel()
							.getMapView().getWorldViewTransformer()
							.worldToPan(GeometryConverter.toGwt(event.getGeometry()));
					mapWidget.getVectorContext().drawPolygon(groups.get(baseName + ".background"), "background",
							(Polygon) transformed, styleService.getBackgroundStyle());
				} else if (event.getGeometry().getGeometryType().equals(Geometry.MULTI_POLYGON)) {
					// ....
				}
			}
		}

		// Next, redraw the list:
		for (GeometryIndex index : toRedraw) {
			update(event.getGeometry(), index);
		}
	}

	public void onGeometryShapeChanged(GeometryEditShapeChangedEvent event) {
		redraw();
	}

	// ------------------------------------------------------------------------
	// GeometryEditChangeStateHandler:
	// ------------------------------------------------------------------------

	public void onChangeEditingState(GeometryEditChangeStateEvent event) {
		switch (event.getEditingState()) {
			case DRAGGING:
				mapWidget.setCursor(Cursor.MOVE);
				break;
			case IDLE:
			default:
				mapWidget.setCursor(Cursor.DEFAULT);
				redraw();

				// Remove the temporary insert move line:
				if (editingService.getInsertIndex() != null) {
					String id = baseName + "."
							+ editingService.getIndexService().format(editingService.getInsertIndex());
					Object parentGroup = groups.get(id.substring(0, id.lastIndexOf('.')) + ".edges");
					mapWidget.getVectorContext().deleteElement(parentGroup, insertMoveEdgeId1);
					mapWidget.getVectorContext().deleteElement(parentGroup, insertMoveEdgeId2);
				}
		}
	}

	// ------------------------------------------------------------------------
	// GeometryEditInsertMoveHandler:
	// ------------------------------------------------------------------------

	public void onTentativeMove(GeometryEditTentativeMoveEvent event) {
		try {
			Coordinate[] vertices = editingService.getIndexService().getSiblingVertices(editingService.getGeometry(),
					editingService.getInsertIndex());
			String geometryType = editingService.getIndexService().getGeometryType(editingService.getGeometry(),
					editingService.getInsertIndex());

			if (vertices != null && Geometry.LINE_STRING.equals(geometryType)) {
				String identifier = baseName + "."
						+ editingService.getIndexService().format(editingService.getInsertIndex());
				Object parentGroup = groups.get(identifier.substring(0, identifier.lastIndexOf('.')) + ".edges");

				Coordinate temp1 = event.getOrigin();
				Coordinate temp2 = event.getCurrentPosition();
				Coordinate c1 = mapWidget.getMapModel().getMapView().getWorldViewTransformer().worldToPan(temp1);
				Coordinate c2 = mapWidget.getMapModel().getMapView().getWorldViewTransformer().worldToPan(temp2);

				LineString edge = mapWidget.getMapModel().getGeometryFactory()
						.createLineString(new Coordinate[] { c1, c2 });
				mapWidget.getVectorContext().drawLine(parentGroup, insertMoveEdgeId1, edge,
						styleService.getEdgeTentativeMoveStyle());
			} else if (vertices != null && Geometry.LINEAR_RING.equals(geometryType)) {
				String identifier = baseName + "."
						+ editingService.getIndexService().format(editingService.getInsertIndex());
				Object parentGroup = groups.get(identifier.substring(0, identifier.lastIndexOf('.')) + ".edges");

				// Line 1
				Coordinate temp1 = event.getOrigin();
				Coordinate temp2 = event.getCurrentPosition();
				Coordinate c1 = mapWidget.getMapModel().getMapView().getWorldViewTransformer().worldToPan(temp1);
				Coordinate c2 = mapWidget.getMapModel().getMapView().getWorldViewTransformer().worldToPan(temp2);
				LineString edge = mapWidget.getMapModel().getGeometryFactory()
						.createLineString(new Coordinate[] { c1, c2 });
				mapWidget.getVectorContext().drawLine(parentGroup, insertMoveEdgeId1, edge,
						styleService.getEdgeTentativeMoveStyle());

				// Line 2
				if (closeRingWhileInserting) {
					temp1 = vertices[vertices.length - 1];
					c1 = mapWidget.getMapModel().getMapView().getWorldViewTransformer().worldToPan(temp1);
					edge = mapWidget.getMapModel().getGeometryFactory().createLineString(new Coordinate[] { c1, c2 });
					mapWidget.getVectorContext().drawLine(parentGroup, insertMoveEdgeId2, edge,
							styleService.getEdgeTentativeMoveStyle());
				}
			}
		} catch (GeometryIndexNotFoundException e) {
			throw new IllegalStateException(e);
		}
	}

	// ------------------------------------------------------------------------
	// CoordinateSnappedHandler implementation:
	// ------------------------------------------------------------------------

	public void onCoordinateSnapAttempt(CoordinateSnapEvent event) {
		if (editingService.getEditingState() == GeometryEditState.INSERTING) {
			String identifier = baseName + "."
					+ editingService.getIndexService().format(editingService.getInsertIndex());
			Object parentGroup = groups.get(identifier.substring(0, identifier.lastIndexOf('.')) + ".vertices");

			Coordinate temp = event.getTo();
			Coordinate coordinate = mapWidget.getMapModel().getMapView().getWorldViewTransformer().worldToPan(temp);
			Bbox rectangle = new Bbox(coordinate.getX() - HALF_VERTEX_SIZE, coordinate.getY() - HALF_VERTEX_SIZE,
					VERTEX_SIZE, VERTEX_SIZE);
			if (event.hasSnapped()) {
				mapWidget.getVectorContext().drawRectangle(parentGroup, "first", rectangle,
						styleService.getVertexSnappedStyle());
			} else {
				mapWidget.getVectorContext().drawRectangle(parentGroup, "first", rectangle, new ShapeStyle());
			}
		}
	}

	// ------------------------------------------------------------------------
	// Geometry rendering methods:
	// ------------------------------------------------------------------------

	private void update(Geometry geometry, GeometryIndex index) {
		try {
			switch (editingService.getIndexService().getType(index)) {
				case TYPE_VERTEX:
					updateVertex(geometry, index);
					break;
				case TYPE_EDGE:
					updateEdge(geometry, index);
					break;
				case TYPE_GEOMETRY:
				default:
					updateGeometry(geometry, index);
			}
		} catch (GeometryIndexNotFoundException e) {
		}
	}

	// TODO make use of the findGeometryStyle method.
	private void updateGeometry(Geometry geometry, GeometryIndex index) throws GeometryIndexNotFoundException {
		// Some initialization:
		String identifier = baseName + "." + editingService.getIndexService().format(index);
		boolean marked = editingService.getIndexStateService().isMarkedForDeletion(index);

		// Find current and previous parent groups:
		Composite parentGroup = groups.get(identifier.substring(0, identifier.lastIndexOf('.')) + ".geometries");

		// Find the correct style:
		ShapeStyle style = new ShapeStyle();
		if (marked) {
			style = styleService.getBackgroundMarkedForDeletionStyle();
		}

		// Draw the inner ring:
		org.geomajas.gwt.client.spatial.geometry.Geometry transformed = mapWidget.getMapModel().getMapView()
				.getWorldViewTransformer()
				.worldToPan(GeometryConverter.toGwt(editingService.getIndexService().getGeometry(geometry, index)));
		if (transformed instanceof LinearRing) {
			Polygon polygon = mapWidget.getMapModel().getGeometryFactory()
					.createPolygon((LinearRing) transformed, null);
			mapWidget.getVectorContext().drawPolygon(parentGroup, identifier + ".background", polygon, style);
		}
	}

	private void updateVertex(Geometry geometry, GeometryIndex index) throws GeometryIndexNotFoundException {
		// Some initialization:
		String identifier = baseName + "." + editingService.getIndexService().format(index);
		Composite parentGroup = groups.get(identifier.substring(0, identifier.lastIndexOf('.')) + ".vertices");

		Coordinate temp = editingService.getIndexService().getVertex(geometry, index);
		Coordinate coordinate = mapWidget.getMapModel().getMapView().getWorldViewTransformer().worldToPan(temp);
		Bbox rectangle = new Bbox(coordinate.getX() - HALF_VERTEX_SIZE, coordinate.getY() - HALF_VERTEX_SIZE,
				VERTEX_SIZE, VERTEX_SIZE);

		// Draw:
		if (Dom.isIE() && editingService.getEditingState() == GeometryEditState.DRAGGING
				&& editingService.getIndexStateService().isSelected(index)) {
			// Cheap trick so switch order:
			mapWidget.getVectorContext().drawRectangle(parentGroup, "first", rectangle, findVertexStyle(index));
			mapWidget.getVectorContext().drawRectangle(parentGroup, identifier, rectangle, new ShapeStyle());
		} else {
			mapWidget.getVectorContext().drawRectangle(parentGroup, identifier, rectangle, findVertexStyle(index));
			mapWidget.getVectorContext().drawRectangle(parentGroup, "first", rectangle, new ShapeStyle());
		}
	}

	private void updateEdge(Geometry geometry, GeometryIndex index) throws GeometryIndexNotFoundException {
		// Some initialization:
		String identifier = baseName + "." + editingService.getIndexService().format(index);
		Object parentGroup = groups.get(identifier.substring(0, identifier.lastIndexOf('.')) + ".edges");

		Coordinate[] c = editingService.getIndexService().getEdge(geometry, index);
		LineString temp = mapWidget.getMapModel().getGeometryFactory().createLineString(c);
		LineString edge = (LineString) mapWidget.getMapModel().getMapView().getWorldViewTransformer().worldToPan(temp);
		mapWidget.getVectorContext().drawLine(parentGroup, identifier, edge, findEdgeStyle(index));
	}

	private void draw(Geometry geometry) {
		org.geomajas.gwt.client.spatial.geometry.Geometry transformed = mapWidget.getMapModel().getMapView()
				.getWorldViewTransformer().worldToPan(GeometryConverter.toGwt(geometry));

		GraphicsContext graphics = mapWidget.getVectorContext();
		graphics.drawGroup(mapWidget.getGroup(RenderGroup.VECTOR), geometry);

		if (transformed instanceof Polygon) {
			draw(geometry, null, (Polygon) transformed, graphics);
		} else if (transformed instanceof LineString) {
			draw(geometry, null, (LineString) transformed, graphics);
		} else if (transformed instanceof Point) {
			draw(geometry, null, (Point) transformed, graphics);
		}
	}

	private void draw(Object parentGroup, GeometryIndex parentIndex, Polygon polygon, GraphicsContext graphics) {
		String groupName = baseName;
		if (parentIndex != null) {
			groupName += "." + editingService.getIndexService().format(parentIndex);
		}

		Composite bgGroup = getOrCreateGroup(parentGroup, groupName + ".background");
		Composite geometryGroup = getOrCreateGroup(parentGroup, groupName + ".geometries");

		// Draw the exterior ring:
		GeometryIndex shellIndex = editingService.getIndexService().addChildren(parentIndex,
				GeometryIndexType.TYPE_GEOMETRY, 0);
		if (!polygon.isEmpty()) {
			if (styleService.getBackgroundStyle() != null && styleService.getBackgroundStyle().getFillOpacity() > 0) {
				graphics.drawPolygon(bgGroup, "background", polygon, findGeometryStyle(shellIndex));
			}
			draw(geometryGroup, shellIndex, polygon.getExteriorRing(), graphics);
		}

		// Draw the interior rings:
		for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
			GeometryIndex holeIndex = editingService.getIndexService().addChildren(parentIndex,
					GeometryIndexType.TYPE_GEOMETRY, i + 1);
			draw(geometryGroup, holeIndex, polygon.getInteriorRingN(i), graphics);
		}
	}

	private void draw(Object parentGroup, GeometryIndex parentIndex, LinearRing linearRing, GraphicsContext graphics) {
		String groupName = baseName;
		if (parentIndex != null) {
			groupName += "." + editingService.getIndexService().format(parentIndex);
		}
		Composite edgeGroup = getOrCreateGroup(parentGroup, groupName + ".edges");
		Composite vertexGroup = getOrCreateGroup(parentGroup, groupName + ".vertices");

		Coordinate[] coordinates = linearRing.getCoordinates();
		if (coordinates != null) {
			// Check if we have to draw the background as well (if there are controllers defined for it):
			GraphicsController controller = createGeometryController(parentIndex);
			if (controller != null) {
				Polygon polygon = mapWidget.getMapModel().getGeometryFactory().createPolygon(linearRing, null);
				graphics.drawPolygon(parentGroup, groupName + ".background", polygon, new ShapeStyle());
				graphics.setController(parentGroup, groupName + ".background", controller);
			}

			// Draw individual edges:
			int max = coordinates.length;
			if (!closeRingWhileInserting && editingService.getEditingState() == GeometryEditState.INSERTING
					&& editingService.getIndexService().isChildOf(parentIndex, editingService.getInsertIndex())) {
				max--;
			}
			for (int i = 1; i < max; i++) {
				GeometryIndex edgeIndex = editingService.getIndexService().addChildren(parentIndex,
						GeometryIndexType.TYPE_EDGE, i - 1);
				String identifier = baseName + "." + editingService.getIndexService().format(edgeIndex);

				LineString edge = linearRing.getGeometryFactory().createLineString(
						new Coordinate[] { coordinates[i - 1], coordinates[i] });
				graphics.drawLine(edgeGroup, identifier, edge, findEdgeStyle(edgeIndex));
				graphics.setController(edgeGroup, identifier, createEdgeController(edgeIndex));
			}

			// Draw individual vertices, but first an invisible rectangle. Can be used later!:
			Bbox firstRectangle = new Bbox(0, 0, VERTEX_SIZE, VERTEX_SIZE);
			graphics.drawRectangle(vertexGroup, "first", firstRectangle, new ShapeStyle());
			for (int i = 0; i < coordinates.length - 1; i++) {
				GeometryIndex vertexIndex = editingService.getIndexService().addChildren(parentIndex,
						GeometryIndexType.TYPE_VERTEX, i);
				String identifier = baseName + "." + editingService.getIndexService().format(vertexIndex);

				Bbox rectangle = new Bbox(coordinates[i].getX() - HALF_VERTEX_SIZE, coordinates[i].getY()
						- HALF_VERTEX_SIZE, VERTEX_SIZE, VERTEX_SIZE);
				graphics.drawRectangle(vertexGroup, identifier, rectangle, findVertexStyle(vertexIndex));
				graphics.setController(vertexGroup, identifier, createVertexController(vertexIndex));
			}
		}
	}

	private void draw(Object parentGroup, GeometryIndex parentIndex, LineString lineString, GraphicsContext graphics) {
		String groupName = baseName;
		if (parentIndex != null) {
			groupName += "." + editingService.getIndexService().format(parentIndex);
		}
		Composite edgeGroup = getOrCreateGroup(parentGroup, groupName + ".edges");
		Composite vertexGroup = getOrCreateGroup(parentGroup, groupName + ".vertices");

		Coordinate[] coordinates = lineString.getCoordinates();
		if (coordinates != null) {
			// Draw individual edges:
			for (int i = 1; i < coordinates.length; i++) {
				GeometryIndex edgeIndex = editingService.getIndexService().addChildren(parentIndex,
						GeometryIndexType.TYPE_EDGE, i - 1);
				String identifier = baseName + "." + editingService.getIndexService().format(edgeIndex);

				LineString edge = lineString.getGeometryFactory().createLineString(
						new Coordinate[] { coordinates[i - 1], coordinates[i] });
				graphics.drawLine(edgeGroup, identifier, edge, findEdgeStyle(edgeIndex));
				graphics.setController(edgeGroup, identifier, createEdgeController(edgeIndex));
			}

			// Draw individual vertices, but first an invisible rectangle. Can be used later!:
			Bbox firstRectangle = new Bbox(0, 0, VERTEX_SIZE, VERTEX_SIZE);
			graphics.drawRectangle(vertexGroup, "first", firstRectangle, new ShapeStyle());
			for (int i = 0; i < coordinates.length; i++) {
				GeometryIndex vertexIndex = editingService.getIndexService().addChildren(parentIndex,
						GeometryIndexType.TYPE_VERTEX, i);
				String identifier = baseName + "." + editingService.getIndexService().format(vertexIndex);

				Bbox rectangle = new Bbox(coordinates[i].getX() - HALF_VERTEX_SIZE, coordinates[i].getY()
						- HALF_VERTEX_SIZE, VERTEX_SIZE, VERTEX_SIZE);
				graphics.drawRectangle(vertexGroup, identifier, rectangle, findVertexStyle(vertexIndex));
				graphics.setController(vertexGroup, identifier, createVertexController(vertexIndex));
			}
		}
	}

	private void draw(Object parentGroup, GeometryIndex parentIndex, Point point, GraphicsContext graphics) {
		String groupName = baseName;
		if (parentIndex != null) {
			groupName += "." + editingService.getIndexService().format(parentIndex);
		}
		getOrCreateGroup(parentGroup, groupName + ".edges-selection");
		getOrCreateGroup(parentGroup, groupName + ".edges");
		getOrCreateGroup(parentGroup, groupName + ".vertices-selection");
		Composite vertexGroup = getOrCreateGroup(parentGroup, groupName + ".vertices");

		if (!point.isEmpty()) {
			GeometryIndex vertexIndex = editingService.getIndexService().addChildren(parentIndex,
					GeometryIndexType.TYPE_VERTEX, 0);
			String identifier = baseName + "." + editingService.getIndexService().format(vertexIndex);

			Bbox rectangle = new Bbox(point.getCoordinate().getX() - HALF_VERTEX_SIZE, point.getCoordinate().getY()
					- HALF_VERTEX_SIZE, VERTEX_SIZE, VERTEX_SIZE);
			graphics.drawRectangle(vertexGroup, identifier, rectangle, findVertexStyle(vertexIndex));
			graphics.setController(vertexGroup, identifier, createVertexController(vertexIndex));
		}
	}

	/**
	 * Used in creating the "edges", "selection" and "vertices" groups for LineStrings and LinearRings.
	 */
	private Composite getOrCreateGroup(Object parent, String name) {
		if (groups.containsKey(name)) {
			return groups.get(name);
		}
		Composite group = new Composite(name);
		mapWidget.getVectorContext().drawGroup(parent, group);
		groups.put(name, group);
		return group;
	}

	private ShapeStyle findVertexStyle(GeometryIndex index) {
		if (editingService.getIndexStateService().isMarkedForDeletion(index)) {
			return styleService.getVertexMarkForDeletionStyle();
		} else if (!editingService.getIndexStateService().isEnabled(index)) {
			return styleService.getVertexDisabledStyle();
		} else if (editingService.getIndexStateService().isSnapped(index)) {
			return styleService.getVertexSnappedStyle();
		}

		boolean selected = editingService.getIndexStateService().isSelected(index);
		boolean highlighted = editingService.getIndexStateService().isHightlighted(index);
		if (selected && highlighted) {
			return styleService.getVertexSelectHoverStyle();
		} else if (selected) {
			return styleService.getVertexSelectStyle();
		} else if (highlighted) {
			return styleService.getVertexHoverStyle();
		}
		return styleService.getVertexStyle();
	}

	private ShapeStyle findEdgeStyle(GeometryIndex index) {
		if (editingService.getIndexStateService().isMarkedForDeletion(index)) {
			return styleService.getEdgeMarkForDeletionStyle();
		} else if (!editingService.getIndexStateService().isEnabled(index)) {
			return styleService.getEdgeDisabledStyle();
		}

		boolean selected = editingService.getIndexStateService().isSelected(index);
		boolean highlighted = editingService.getIndexStateService().isHightlighted(index);
		if (selected && highlighted) {
			return styleService.getEdgeSelectHoverStyle();
		} else if (selected) {
			return styleService.getEdgeSelectStyle();
		} else if (highlighted) {
			return styleService.getEdgeHoverStyle();
		}
		return styleService.getEdgeStyle();
	}

	private ShapeStyle findGeometryStyle(GeometryIndex index) {
		if (!editingService.getIndexStateService().isEnabled(index)) {
			return styleService.getBackgroundDisabledStyle();
		} else if (editingService.getIndexStateService().isMarkedForDeletion(index)) {
			return styleService.getBackgroundMarkedForDeletionStyle();
		}
		return styleService.getBackgroundStyle();
	}

	private GraphicsController createVertexController(GeometryIndex index) {
		CompositeGeometryIndexController controller = new CompositeGeometryIndexController(mapWidget, editingService,
				index, editingService.getEditingState() == GeometryEditState.DRAGGING);
		for (AbstractGeometryIndexMapHandler handler : EditingHandlerRegistry.getVertexHandlers()) {
			controller.addMapHandler(handler);
		}
		return controller;
	}

	private GraphicsController createEdgeController(GeometryIndex index) {
		CompositeGeometryIndexController controller = new CompositeGeometryIndexController(mapWidget, editingService,
				index, editingService.getEditingState() == GeometryEditState.DRAGGING);
		for (AbstractGeometryIndexMapHandler handler : EditingHandlerRegistry.getEdgeHandlers()) {
			controller.addMapHandler(handler);
		}

		EdgeMarkerHandler edgeMarkerHandler = new EdgeMarkerHandler(mapWidget, editingService, controller);
		controller.addMouseOutHandler(edgeMarkerHandler);
		controller.addMouseMoveHandler(edgeMarkerHandler);
		controller.addMapDownHandler(edgeMarkerHandler);
		return controller;
	}

	private GraphicsController createGeometryController(GeometryIndex index) {
		List<AbstractGeometryIndexMapHandler> handlers = EditingHandlerRegistry.getGeometryHandlers();
		if (handlers == null || handlers.size() == 0) {
			return null;
		}

		CompositeGeometryIndexController controller = new CompositeGeometryIndexController(mapWidget, editingService,
				index, editingService.getEditingState() == GeometryEditState.DRAGGING);
		for (AbstractGeometryIndexMapHandler handler : handlers) {
			controller.addMapHandler(handler);
		}
		return controller;
	}
}