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
package org.geomajas.plugin.deskmanager.command.manager;

import java.util.Map;

import javax.annotation.Resource;

import org.geomajas.command.Command;
import org.geomajas.configuration.Parameter;
import org.geomajas.plugin.deskmanager.command.manager.dto.GetVectorLayerConfigurationRequest;
import org.geomajas.plugin.deskmanager.command.manager.dto.GetVectorLayerConfigurationResponse;
import org.geomajas.plugin.deskmanager.domain.dto.LayerConfiguration;
import org.geomajas.plugin.deskmanager.service.manager.DiscoveryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Kristof Heirwegh
 */
@Component(GetVectorLayerConfigurationRequest.COMMAND)
public class GetVectorLayerConfigurationCommand implements
		Command<GetVectorLayerConfigurationRequest, GetVectorLayerConfigurationResponse> {

	@SuppressWarnings("unused")
	private final Logger log = LoggerFactory.getLogger(GetVectorLayerConfigurationCommand.class);

	@Autowired
	private DiscoveryService discoServ;

	@Resource(name = "postGisDatastoreParams")
	private Map<String, String> postgisDataStoreParams;

	public void execute(GetVectorLayerConfigurationRequest request, GetVectorLayerConfigurationResponse response)
			throws Exception {
		if (request.getConnectionProperties() == null || request.getConnectionProperties().size() < 1
				|| request.getLayerName() == null || "".equals(request.getLayerName())) {
			response.getErrorMessages().add("Required parameter missing (connectionprops, layerName");
		} else {
			String sourceType = request.getConnectionProperties().get(LayerConfiguration.PARAM_SOURCE_TYPE);
			Map<String, String> connProps;
			if (LayerConfiguration.SOURCE_TYPE_SHAPE.equals(sourceType)) {
				connProps = postgisDataStoreParams; // get database properties
			} else {
				connProps = request.getConnectionProperties();
			}

			response.setVectorLayerConfiguration(discoServ.getVectorLayerConfiguration(connProps,
					request.getLayerName()));

			if (LayerConfiguration.SOURCE_TYPE_SHAPE.equals(sourceType)) {
				// remove connection properties, these are private and should not be sent to the client
				Parameter stp = new Parameter();
				stp.setName(LayerConfiguration.PARAM_SOURCE_TYPE);
				stp.setValue(LayerConfiguration.SOURCE_TYPE_SHAPE);
				response.getVectorLayerConfiguration().getParameters().clear();
				response.getVectorLayerConfiguration().getParameters().add(stp);
			}
		}
	}

	public GetVectorLayerConfigurationResponse getEmptyCommandResponse() {
		return new GetVectorLayerConfigurationResponse();
	}
}
