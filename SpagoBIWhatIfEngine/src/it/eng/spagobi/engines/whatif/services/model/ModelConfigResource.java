/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

/**
 * @author Alberto Ghedin (alberto.ghedin@eng.it)
 *
 */
package it.eng.spagobi.engines.whatif.services.model;

import it.eng.spagobi.engines.whatif.WhatIfEngineInstance;
import it.eng.spagobi.engines.whatif.services.common.AbstractWhatIfEngineService;
import it.eng.spagobi.engines.whatif.services.serializer.SerializationException;
import it.eng.spagobi.engines.whatif.services.serializer.SerializationManager;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.apache.log4j.Logger;


@Path("/v1.0/modelconfig")
public class ModelConfigResource extends AbstractWhatIfEngineService {

	public static transient Logger logger = Logger.getLogger(ModelConfigResource.class);
	
	/**
	 * Sets the drill type
	 * @param drillType the drill type
	 * @return the html table representing the cellset
	 */
	@PUT
	@Path("/drilltype/{type}")
	public String setDrillType(@PathParam("type") String drillType){
		logger.debug("IN");
		WhatIfEngineInstance ei = getWhatIfEngineInstance();
		ModelConfig config = ei.getModelConfig();
		
		config.setDrillType(drillType);
		
		String table = renderModel(ei.getPivotModel());
		logger.debug("OUT");
		return table;
	}
	
	
	/**
	 * Gets the drill type
	 * @return the drill type
	 */
	@GET
	@Path("/")
	public String getDrillType(){
		logger.debug("IN");
		WhatIfEngineInstance ei = getWhatIfEngineInstance();
		ModelConfig config = ei.getModelConfig();
		
		String configSerialized=null;
		try {
			configSerialized = (String) SerializationManager.getDefaultSerializer().serialize(config);
		} catch (SerializationException e) {
			logger.error("Error serializing the model config",e);
			throw new SpagoBIEngineRuntimeException("Error serializing the model config",e);
		}
		
		logger.debug("OUT");
		return configSerialized;
	}
	
	
}
