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
package org.geomajas.plugin.printing.client.template;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.geomajas.configuration.FeatureStyleInfo;
import org.geomajas.configuration.FontStyleInfo;
import org.geomajas.configuration.client.ClientRasterLayerInfo;
import org.geomajas.configuration.client.ClientVectorLayerInfo;
import org.geomajas.geometry.Coordinate;
import org.geomajas.gwt.client.map.MapModel;
import org.geomajas.gwt.client.map.MapView;
import org.geomajas.gwt.client.map.layer.Layer;
import org.geomajas.gwt.client.map.layer.RasterLayer;
import org.geomajas.gwt.client.map.layer.VectorLayer;
import org.geomajas.plugin.printing.command.dto.PrintTemplateInfo;
import org.geomajas.plugin.printing.component.dto.ImageComponentInfo;
import org.geomajas.plugin.printing.component.dto.LabelComponentInfo;
import org.geomajas.plugin.printing.component.dto.LayoutConstraintInfo;
import org.geomajas.plugin.printing.component.dto.LegendComponentInfo;
import org.geomajas.plugin.printing.component.dto.LegendIconComponentInfo;
import org.geomajas.plugin.printing.component.dto.LegendItemComponentInfo;
import org.geomajas.plugin.printing.component.dto.MapComponentInfo;
import org.geomajas.plugin.printing.component.dto.PageComponentInfo;
import org.geomajas.plugin.printing.component.dto.PrintComponentInfo;
import org.geomajas.plugin.printing.component.dto.RasterLayerComponentInfo;
import org.geomajas.plugin.printing.component.dto.ScaleBarComponentInfo;
import org.geomajas.plugin.printing.component.dto.VectorLayerComponentInfo;

/**
 * Default print template builder, parameters include title, size, raster DPI, orientation, etc...
 * 
 * @author Jan De Moerloose
 * 
 */
public class DefaultTemplateBuilder extends TemplateBuilder {

	private double pageWidth;

	private double pageHeight;

	private int marginX;

	private int marginY;

	private String titleText;

	private int rasterDpi;

	private boolean withScaleBar;

	private boolean withArrow;

	private MapModel mapModel;

	private String applicationId;

	@Override
	public PrintTemplateInfo buildTemplate() {
		PrintTemplateInfo template = super.buildTemplate();
		template.setTemplate(false);
		template.setId(1L);
		template.setName("default");
		return template;
	}

	@Override
	protected PageComponentInfo buildPage() {
		PageComponentInfo page = super.buildPage();
		page.getLayoutConstraint().setWidth((float) pageWidth);
		page.getLayoutConstraint().setHeight((float) pageHeight);
		return page;
	}

	@Override
	protected MapComponentInfo buildMap() {
		MapComponentInfo map = super.buildMap();
		map.getLayoutConstraint().setMarginX(marginX);
		map.getLayoutConstraint().setMarginY(marginY);
		MapView view = mapModel.getMapView();
		double mapWidth = getPageWidth() - 2 * marginX;
		double mapHeight = getPageHeight() - 2 * marginY;
		Coordinate origin = view.getBounds().createFittingBox(mapWidth, mapHeight).getOrigin();
		map.setLocation(new org.geomajas.geometry.Coordinate(origin.getX(), origin.getY()));
		map.setPpUnit((float) (mapWidth / view.getBounds().createFittingBox(mapWidth, mapHeight).getWidth()));
		map.setTag("map");
		map.setMapId(mapModel.getMapInfo().getId());
		map.setApplicationId(applicationId);
		map.setRasterResolution(rasterDpi);
		List<Layer> layers = getLayersInPrintOrder();
		List<PrintComponentInfo> layerChildren = new ArrayList<PrintComponentInfo>();
		for (Layer layer : layers) {
			if (layer instanceof VectorLayer && layer.isShowing()) {
				VectorLayerComponentInfo info = new VectorLayerComponentInfo();
				VectorLayer vectorLayer = (VectorLayer) layer;
				info.setLayerId(vectorLayer.getServerLayerId());
				ClientVectorLayerInfo layerInfo = vectorLayer.getLayerInfo();
				info.setStyleInfo(layerInfo.getNamedStyleInfo());
				info.setFilter(vectorLayer.getFilter());
				info.setLabelsVisible(vectorLayer.isLabeled());
				info.setSelected(vectorLayer.isSelected());
				info.setSelectedFeatureIds(vectorLayer.getSelectedFeatures().toArray(new String[0]));
				layerChildren.add(info);
			} else if (layer instanceof RasterLayer && layer.isShowing()) {
				RasterLayerComponentInfo info = new RasterLayerComponentInfo();
				RasterLayer rasterLayer = (RasterLayer) layer;
				info.setLayerId(rasterLayer.getServerLayerId());
				layerChildren.add(info);
			}
		}
		map.getChildren().addAll(0, layerChildren);
		return map;
	}

	@Override
	protected ImageComponentInfo buildArrow() {
		if (isWithArrow()) {
			ImageComponentInfo northarrow = super.buildArrow();
			northarrow.setImagePath("/images/northarrow.gif");
			northarrow.getLayoutConstraint().setAlignmentX(LayoutConstraintInfo.RIGHT);
			northarrow.getLayoutConstraint().setAlignmentY(LayoutConstraintInfo.TOP);
			northarrow.getLayoutConstraint().setMarginX(20);
			northarrow.getLayoutConstraint().setMarginY(20);
			northarrow.getLayoutConstraint().setWidth(10);
			northarrow.setTag("arrow");
			return northarrow;
		} else {
			return null;
		}
	}

	@Override
	protected LegendComponentInfo buildLegend() {
		LegendComponentInfo legend = super.buildLegend();
		FontStyleInfo style = new FontStyleInfo();
		style.setFamily("Dialog");
		style.setStyle("Italic");
		style.setSize(14);
		legend.setFont(style);
		legend.setMapId(mapModel.getMapInfo().getId());
		legend.setTag("legend");
		List<Layer> layers = getLayersInPrintOrder();
		for (Layer layer : layers) {
			if (layer instanceof VectorLayer && layer.isShowing()) {
				VectorLayer vectorLayer = (VectorLayer) layer;
				ClientVectorLayerInfo layerInfo = vectorLayer.getLayerInfo();
				String label = layerInfo.getLabel();
				List<FeatureStyleInfo> defs = layerInfo.getNamedStyleInfo().getFeatureStyles();
				for (FeatureStyleInfo styleDefinition : defs) {
					String text = "";
					if (defs.size() > 1) {
						text = label + "(" + styleDefinition.getName() + ")";
					} else {
						text = label;
					}
					LegendItemComponentInfo item = new LegendItemComponentInfo();
					LegendIconComponentInfo icon = new LegendIconComponentInfo();
					icon.setLabel(text);
					icon.setStyleInfo(styleDefinition);
					icon.setLayerType(layerInfo.getLayerType());
					LabelComponentInfo legendLabel = new LabelComponentInfo();
					legendLabel.setBackgroundColor("0xFFFFFF");
					legendLabel.setBorderColor("0x000000");
					legendLabel.setFontColor("0x000000");
					legendLabel.setFont(legend.getFont());
					legendLabel.setText(text);
					legendLabel.setTextOnly(true);
					item.addChild(icon);
					item.addChild(legendLabel);
					legend.addChild(item);
				}
			} else if (layer instanceof RasterLayer && layer.isShowing()) {
				RasterLayer rasterLayer = (RasterLayer) layer;
				ClientRasterLayerInfo layerInfo = rasterLayer.getLayerInfo();
				LegendItemComponentInfo item = new LegendItemComponentInfo();
				LegendIconComponentInfo icon = new LegendIconComponentInfo();
				icon.setLabel(layerInfo.getLabel());
				icon.setLayerType(layerInfo.getLayerType());
				LabelComponentInfo legendLabel = new LabelComponentInfo();
				legendLabel.setFont(legend.getFont());
				legendLabel.setBackgroundColor("0xFFFFFF");
				legendLabel.setBorderColor("0x000000");
				legendLabel.setFontColor("0x000000");
				legendLabel.setText(layerInfo.getLabel());
				legendLabel.setTextOnly(true);
				item.addChild(icon);
				item.addChild(legendLabel);
				legend.addChild(item);
			}
		}
		return legend;
	}

	@Override
	protected ScaleBarComponentInfo buildScaleBar() {
		if (isWithScaleBar()) {
			ScaleBarComponentInfo bar = super.buildScaleBar();
			bar.setTicNumber(3);
			bar.setTag("scalebar");
			return bar;
		} else {
			return null;
		}
	}

	@Override
	protected LabelComponentInfo buildTitle() {
		if (titleText != null) {
			LabelComponentInfo title = super.buildTitle();
			title.setText(titleText);
			title.getLayoutConstraint().setMarginY(2 * marginY);
			return title;
		} else {
			return null;
		}
	}

	public double getPageWidth() {
		return pageWidth;
	}

	public void setPageWidth(double pageWidth) {
		this.pageWidth = pageWidth;
	}

	public double getPageHeight() {
		return pageHeight;
	}

	public void setPageHeight(double pageHeight) {
		this.pageHeight = pageHeight;
	}

	public String getTitleText() {
		return titleText;
	}

	public void setTitleText(String titleText) {
		this.titleText = titleText;
	}

	public int getRasterDpi() {
		return rasterDpi;
	}

	public void setRasterDpi(int rasterDpi) {
		this.rasterDpi = rasterDpi;
	}

	public boolean isWithScaleBar() {
		return withScaleBar;
	}

	public void setWithScaleBar(boolean withScaleBar) {
		this.withScaleBar = withScaleBar;
	}

	public boolean isWithArrow() {
		return withArrow;
	}

	public void setWithArrow(boolean withArrow) {
		this.withArrow = withArrow;
	}

	public int getMarginX() {
		return marginX;
	}

	public void setMarginX(int marginX) {
		this.marginX = marginX;
	}

	public int getMarginY() {
		return marginY;
	}

	public void setMarginY(int marginY) {
		this.marginY = marginY;
	}

	public MapModel getMapModel() {
		return mapModel;
	}

	public void setMapModel(MapModel mapModel) {
		this.mapModel = mapModel;
	}

	public String getApplicationId() {
		return applicationId;
	}

	public void setApplicationId(String applicationId) {
		this.applicationId = applicationId;
	}

	private List<Layer> getLayersInPrintOrder() {
		List<Layer> layers = new ArrayList<Layer>(mapModel.getLayers());
		Collections.reverse(layers);
		return layers;

	}

}
