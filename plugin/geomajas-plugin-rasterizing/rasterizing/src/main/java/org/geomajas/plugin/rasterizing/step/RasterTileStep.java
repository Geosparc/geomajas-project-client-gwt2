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

package org.geomajas.plugin.rasterizing.step;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.geomajas.configuration.NamedStyleInfo;
import org.geomajas.configuration.client.ClientMapInfo;
import org.geomajas.configuration.client.ClientVectorLayerInfo;
import org.geomajas.global.ExceptionCode;
import org.geomajas.global.GeomajasException;
import org.geomajas.layer.VectorLayer;
import org.geomajas.layer.pipeline.GetTileContainer;
import org.geomajas.layer.tile.TileMetadata;
import org.geomajas.plugin.caching.service.CacheCategory;
import org.geomajas.plugin.rasterizing.api.ImageService;
import org.geomajas.plugin.rasterizing.api.RasterizingContainer;
import org.geomajas.plugin.rasterizing.api.RasterizingPipelineCode;
import org.geomajas.plugin.rasterizing.command.dto.MapRasterizingInfo;
import org.geomajas.plugin.rasterizing.command.dto.VectorLayerRasterizingInfo;
import org.geomajas.service.DtoConverterService;
import org.geomajas.service.TestRecorder;
import org.geomajas.service.pipeline.PipelineCode;
import org.geomajas.service.pipeline.PipelineContext;
import org.geomajas.service.pipeline.PipelineStep;
import org.jboss.serial.io.JBossObjectOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Pipeline step which rasterizes vector tiles.
 * 
 * @author Joachim Van der Auwera
 * @author Jan De Moerloose
 */
public class RasterTileStep implements PipelineStep<GetTileContainer> {

	private final Logger log = LoggerFactory.getLogger(RasterTileStep.class);

	@Autowired
	private ImageService imageService;

	@Autowired
	private TestRecorder recorder;

	@Autowired
	private DtoConverterService converterService;

	private String id;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void execute(PipelineContext context, GetTileContainer tileContainer) throws GeomajasException {
		VectorLayer layer = context.get(PipelineCode.LAYER_KEY, VectorLayer.class);
		TileMetadata tileMetadata = context.get(PipelineCode.TILE_METADATA_KEY, TileMetadata.class);
		// put the image in a raster container
		RasterizingContainer rasterizingContainer = new RasterizingContainer();
		NamedStyleInfo style = tileMetadata.getStyleInfo();
		if (style == null) {
			// no style specified, take the first
			style = layer.getLayerInfo().getNamedStyleInfos().get(0);
		} else if (style.getFeatureStyles().isEmpty()) {
			// only name specified, find it
			style = layer.getLayerInfo().getNamedStyleInfo(style.getName());
		}
		ClientMapInfo mapInfo = new ClientMapInfo();
		MapRasterizingInfo mapRasterizingInfo = new MapRasterizingInfo();
		mapRasterizingInfo.setBounds(converterService.toDto(tileContainer.getTile().getBounds()));
		mapInfo.setCrs(tileMetadata.getCrs());
		mapRasterizingInfo.setScale(tileMetadata.getScale());
		mapInfo.getWidgetInfo().put(MapRasterizingInfo.WIDGET_KEY, mapRasterizingInfo);
		ClientVectorLayerInfo clientVectorLayerInfo = new ClientVectorLayerInfo();
		clientVectorLayerInfo.setServerLayerId(tileMetadata.getLayerId());
		clientVectorLayerInfo.setNamedStyleInfo(style);
		VectorLayerRasterizingInfo vectorLayerRasterizingInfo = new VectorLayerRasterizingInfo();
		vectorLayerRasterizingInfo.setFilter(tileMetadata.getFilter());
		vectorLayerRasterizingInfo.setPaintGeometries(tileMetadata.isPaintGeometries());
		vectorLayerRasterizingInfo.setPaintLabels(tileMetadata.isPaintLabels());
		vectorLayerRasterizingInfo.setFilter(tileMetadata.getFilter());
		try {
			vectorLayerRasterizingInfo.setStyle((NamedStyleInfo) cloneObject(style));
		} catch (IOException e) {
			// should not happen
			throw new GeomajasException(e, ExceptionCode.UNEXPECTED_PROBLEM);
		}
		clientVectorLayerInfo.getWidgetInfo().put(VectorLayerRasterizingInfo.WIDGET_KEY, vectorLayerRasterizingInfo);
		mapInfo.getLayers().add(clientVectorLayerInfo);

		ByteArrayOutputStream imageStream = new ByteArrayOutputStream(1024 * 10);
		try {
			imageService.writeMap(imageStream, mapInfo);
			// rasterizingService.rasterize(imageStream, layer, style, tileMetadata, tileContainer.getTile());
			recorder.record(CacheCategory.RASTER, "Rasterization success");
		} catch (Exception ex) {
			recorder.record(CacheCategory.RASTER, "Rasterization failed");
			log.error("Problem while rasterizing tile, image will be zero-length.", ex);
		}

		byte[] image = imageStream.toByteArray();
		rasterizingContainer.setImage(image);
		context.put(RasterizingPipelineCode.CONTAINER_KEY, rasterizingContainer);
	}
	
	public Object cloneObject(Object input) throws IOException {
		JBossObjectOutputStream jbossSerializer = new JBossObjectOutputStream(null);
		Object obj = jbossSerializer.smartClone(input);
		return obj;
	}
}
