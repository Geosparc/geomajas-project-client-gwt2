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
import com.google.web.bindery.event.shared.HandlerRegistration;
import org.geomajas.geometry.Bbox;
import org.geomajas.geometry.service.BboxService;
import org.geomajas.gwt2.client.GeomajasImpl;
import org.geomajas.gwt2.client.map.View;
import org.geomajas.gwt2.client.map.ViewPort;
import org.geomajas.gwt2.client.map.render.FixedScaleRenderer;
import org.geomajas.gwt2.client.map.render.Tile;
import org.geomajas.gwt2.client.map.render.TileCode;
import org.geomajas.gwt2.client.map.render.TileLevelRenderedEvent;
import org.geomajas.gwt2.client.map.render.TileLevelRenderedHandler;
import org.geomajas.gwt2.client.map.render.dom.container.HtmlContainer;
import org.geomajas.gwt2.client.map.render.dom.container.HtmlImageImpl;
import org.geomajas.plugin.wms.client.WmsClient;
import org.geomajas.plugin.wms.client.service.WmsTileServiceImpl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Definition for a renderer for WMS layers.
 *
 * @author Pieter De Graef
 */
public class WmsTileLevelRenderer implements FixedScaleRenderer {

	private final HtmlContainer container;

	private final WmsLayer layer;

	private final double scale;

	private final Map<TileCode, Tile> tiles;

	private final int tileLevel;

	private final ViewPort viewPort;

	private int nrLoadingTiles;

	public WmsTileLevelRenderer(WmsLayer layer, int tileLevel, ViewPort viewPort, HtmlContainer container) {
		this.layer = layer;
		this.tileLevel = tileLevel;
		this.viewPort = viewPort;
		this.container = container;
		this.tiles = new HashMap<TileCode, Tile>();
		this.scale = viewPort.getFixedScale(tileLevel);
	}

	// ------------------------------------------------------------------------
	// FixedScaleRenderer implementation:
	// ------------------------------------------------------------------------

	@Override
	public int getTileLevel() {
		return tileLevel;
	}

	@Override
	public void render(View view) {
		if (layer.isShowing()) {
			Bbox bounds = asBounds(view);
			List<TileCode> tilesForBounds = WmsTileServiceImpl.getInstance().getTileCodesForBounds(viewPort,
					layer.getTileConfig(), bounds, view.getScale());
			for (TileCode tileCode : tilesForBounds) {
				if (!tiles.containsKey(tileCode)) {
					Tile tile = createTile(tileCode);

					// Add the tile to the list and render it:
					tiles.put(tileCode, tile);
					nrLoadingTiles++;
					renderTile(tile, new ImageCounter());
				}
			}
		}
	}

	@Override
	public void cancel() {
		nrLoadingTiles = 0;
	}

	@Override
	public boolean isRendered(View view) {
		return nrLoadingTiles == 0;
	}

	@Override
	public HandlerRegistration addTileLevelRenderedHandler(TileLevelRenderedHandler handler) {
		return GeomajasImpl.getInstance().getEventBus().addHandler(TileLevelRenderedHandler.TYPE, handler);
	}

	// ------------------------------------------------------------------------
	// Private methods:
	// ------------------------------------------------------------------------

	protected Bbox asBounds(View view) {
		double deltaScale = view.getScale() / scale;
		Bbox bounds = viewPort.asBounds(view);
		return BboxService.scale(bounds, deltaScale);
	}

	protected void renderTile(Tile tile, Callback<String, String> callback) {
		container.add(new HtmlImageImpl(tile.getUrl(), tile.getBounds(), callback));
	}

	private Tile createTile(TileCode tileCode) {
		Bbox worldBounds = WmsTileServiceImpl.getInstance().getWorldBoundsForTile(viewPort,
				layer.getTileConfig(), tileCode);
		Tile tile = new Tile(tileCode, getScreenBounds(worldBounds));
		tile.setCode(tileCode);
		tile.setUrl(WmsClient
				.getInstance()
				.getWmsService()
				.getMapUrl(layer.getConfig(), viewPort.getCrs(), worldBounds, layer.getTileConfig().getTileWidth(),
						layer.getTileConfig().getTileHeight()));
		return tile;
	}

	private Bbox getScreenBounds(Bbox worldBox) {
		return new Bbox(Math.round(scale * worldBox.getX()), -Math.round(scale * worldBox.getMaxY()), Math.round(scale
				* worldBox.getMaxX())
				- Math.round(scale * worldBox.getX()), Math.round(scale * worldBox.getMaxY())
				- Math.round(scale * worldBox.getY()));
	}

	/**
	 * Counts the number of images that are still inbound. If all images are effectively rendered, we fire an event.
	 *
	 * @author Pieter De Graef
	 */
	private class ImageCounter implements Callback<String, String> {

		// In case of failure, we can't just sit and wait. Instead we immediately consider the scale level rendered.
		public void onFailure(String reason) {
			GeomajasImpl.getInstance().getEventBus().fireEventFromSource(new TileLevelRenderedEvent(
					WmsTileLevelRenderer.this), WmsTileLevelRenderer.this);
		}

		public void onSuccess(String result) {
			if (nrLoadingTiles > 0) { // A cancel may have reset the number of loading tiles.
				nrLoadingTiles--;
				if (nrLoadingTiles == 0) {
					GeomajasImpl.getInstance().getEventBus().fireEventFromSource(new TileLevelRenderedEvent(
							WmsTileLevelRenderer.this), WmsTileLevelRenderer.this);
				}
			}
		}
	}
}