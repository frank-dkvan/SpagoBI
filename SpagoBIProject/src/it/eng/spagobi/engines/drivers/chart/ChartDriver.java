/*
 * SpagoBI, the Open Source Business Intelligence suite
 * � 2005-2015 Engineering Group
 *
 * This file is part of SpagoBI. SpagoBI is free software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation, either version 2.1 of the License, or any later version. 
 * SpagoBI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details. You should have received
 * a copy of the GNU Lesser General Public License along with SpagoBI. If not, see: http://www.gnu.org/licenses/.
 * The complete text of SpagoBI license is included in the COPYING.LESSER file. 
 */
package it.eng.spagobi.engines.drivers.chart;

import it.eng.spago.base.RequestContainer;
import it.eng.spago.base.SessionContainer;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.bo.ObjTemplate;
import it.eng.spagobi.analiticalmodel.document.bo.SubObject;
import it.eng.spagobi.analiticalmodel.document.dao.IObjTemplateDAO;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IBinContentDAO;
import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.commons.utilities.ParameterValuesEncoder;
import it.eng.spagobi.commons.utilities.messages.IMessageBuilder;
import it.eng.spagobi.commons.utilities.messages.MessageBuilderFactory;
import it.eng.spagobi.engines.config.bo.Engine;
import it.eng.spagobi.engines.drivers.AbstractDriver;
import it.eng.spagobi.engines.drivers.EngineURL;
import it.eng.spagobi.engines.drivers.IEngineDriver;
import it.eng.spagobi.engines.drivers.exceptions.InvalidOperationRequest;
import it.eng.spagobi.utilities.assertion.Assert;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;



/**
 * Driver Implementation (IEngineDriver Interface) for Chart External Engine. 
 */
public class ChartDriver extends AbstractDriver implements IEngineDriver {
	
	static private Logger logger = Logger.getLogger(ChartDriver.class);
	 
		
	/**
	 * Returns a map of parameters which will be send in the request to the
	 * engine application.
	 * 
	 * @param profile Profile of the user
	 * @param roleName the name of the execution role
	 * @param analyticalDocument the biobject
	 * 
	 * @return Map The map of the execution call parameters
	 */
	public Map getParameterMap(Object analyticalDocument, IEngUserProfile profile, String roleName) {
		Map parameters;
		BIObject biObject;
		
		logger.debug("IN");
		
		try {
			Assert.assertNotNull(analyticalDocument, "Input parameter [analyticalDocument] cannot be null");
			Assert.assertTrue((analyticalDocument instanceof BIObject), "Input parameter [analyticalDocument] cannot be an instance of [" + analyticalDocument.getClass().getName()+ "]");
			
			biObject = (BIObject)analyticalDocument;
			
			parameters = new Hashtable();
			parameters = getRequestParameters(biObject);
			parameters = applySecurity(parameters, profile);
			parameters = addDocumentParametersInfo(parameters, biObject);
			parameters = applyService(parameters, biObject);
		} finally {
			logger.debug("OUT");
		}
		
		return parameters;
	}
	
	/**
	 * Returns a map of parameters which will be send in the request to the
	 * engine application.
	 * 
	 * @param analyticalDocumentSubObject SubObject to execute
	 * @param profile Profile of the user
	 * @param roleName the name of the execution role
	 * @param analyticalDocument the object
	 * 
	 * @return Map The map of the execution call parameters
	 */
	public Map getParameterMap(Object analyticalDocument, Object analyticalDocumentSubObject, IEngUserProfile profile, String roleName) {
		
		Map parameters;
		BIObject biObject;
		SubObject subObject;
		
		logger.debug("IN");
		
		try{
			Assert.assertNotNull(analyticalDocument, "Input parameter [analyticalDocument] cannot be null");
			Assert.assertTrue((analyticalDocument instanceof BIObject), "Input parameter [analyticalDocument] cannot be an instance of [" + analyticalDocument.getClass().getName()+ "]");
			biObject = (BIObject)analyticalDocument;
			
			if(analyticalDocumentSubObject == null) {
				logger.warn("Input parameter [subObject] is null");
				return getParameterMap(analyticalDocument, profile, roleName);
			}				
			Assert.assertTrue((analyticalDocumentSubObject instanceof SubObject), "Input parameter [subObjectDetail] cannot be an instance of [" + analyticalDocumentSubObject.getClass().getName()+ "]");
			subObject = (SubObject) analyticalDocumentSubObject;
						
			parameters = getRequestParameters(biObject);
			
			parameters.put("nameSubObject",  subObject.getName() != null? subObject.getName(): "" );
			parameters.put("descriptionSubObject", subObject.getDescription() != null? subObject.getDescription(): "");
			parameters.put("visibilitySubObject", subObject.getIsPublic().booleanValue()?"Public":"Private" );
			parameters.put("subobjectId", subObject.getId());
			
			parameters = applySecurity(parameters, profile);
			parameters = addDocumentParametersInfo(parameters, biObject);
			parameters = applyService(parameters, biObject);
			parameters.put("isFromCross", "false");
		
		} finally {
			logger.debug("OUT");
		}
		return parameters;
		
	}
	
	/**
	 * Adds a system parameter contaning info about document parameters (url name, label, type)
	 * @param biobject The BIObject under execution
	 * @param map The parameters map
	 * @return the modified map with the new parameter
	 */
    private Map addDocumentParametersInfo(Map map, BIObject biobject) {
    	logger.debug("IN");
    	JSONArray parametersJSON = new JSONArray();
    	try {
	    	Locale locale = getLocale();
			List parameters = biobject.getBiObjectParameters();
			if (parameters != null && parameters.size() > 0) {
				Iterator iter = parameters.iterator();
				while (iter.hasNext()) {
					BIObjectParameter biparam = (BIObjectParameter) iter.next();
					JSONObject jsonParam = new JSONObject();
					jsonParam.put("id", biparam.getParameterUrlName());
					IMessageBuilder msgBuilder = MessageBuilderFactory.getMessageBuilder();
					//String interLabel = msgBuilder.getUserMessage(biparam.getLabel(), SpagoBIConstants.DEFAULT_USER_BUNDLE, locale);
					String interLabel = msgBuilder.getI18nMessage(locale, biparam.getLabel());
					jsonParam.put("label", interLabel);
					jsonParam.put("type", biparam.getParameter().getType());
					parametersJSON.put(jsonParam);
				}
			}
    	} catch (Exception e) {
    		logger.error("Error while adding document parameters info", e);
    	}
    	map.put("SBI_DOCUMENT_PARAMETERS", parametersJSON.toString());
    	logger.debug("OUT");
		return map;
	}

	/**
     * Starting from a BIObject extracts from it the map of the paramaeters for the
     * execution call
     * @param biObject BIObject to execute
     * @return Map The map of the execution call parameters
     */    
	private Map getRequestParameters(BIObject biObject) {
		logger.debug("IN");
		
		Map parameters;
		ObjTemplate template;
		IBinContentDAO contentDAO;
		byte[] content;
		
		logger.debug("IN");
		
		parameters = null;
		
		try {		
			parameters = new Hashtable();
			template = this.getTemplate(biObject);
			
			try {
				contentDAO = DAOFactory.getBinContentDAO();
				Assert.assertNotNull(contentDAO, "Impossible to instantiate contentDAO");
				
				content = contentDAO.getBinContent(template.getBinId());		    
				Assert.assertNotNull(content, "Template content cannot be null");
			} catch (Throwable t){
				throw new RuntimeException("Impossible to load template content for document [" + biObject.getLabel()+ "]", t);
			}
					
			appendRequestParameter(parameters, "document", biObject.getId().toString());
			appendAnalyticalDriversToRequestParameters(biObject, parameters);
			addBIParameterDescriptions(biObject, parameters);
		} finally {
			logger.debug("OUT");
		}
		
		return parameters;
	} 
	
	
	
    /**
     * Add into the parameters map the BIObject's BIParameter names and values
     * @param biobj BIOBject to execute
     * @param pars Map of the parameters for the execution call  
     * @return Map The map of the execution call parameters
     */
	private Map appendAnalyticalDriversToRequestParameters(BIObject biobj, Map pars) {
		logger.debug("IN");
		
		if(biobj==null) {
			logger.warn("BIObject parameter null");	    
		    return pars;
		}
		
		ParameterValuesEncoder parValuesEncoder = new ParameterValuesEncoder();
		if(biobj.getBiObjectParameters() != null){
			BIObjectParameter biobjPar = null;
			for(Iterator it = biobj.getBiObjectParameters().iterator(); it.hasNext();){
				try {
					biobjPar = (BIObjectParameter)it.next();									
					String value = parValuesEncoder.encode(biobjPar);
					pars.put(biobjPar.getParameterUrlName(), value);
					logger.debug("Add parameter:"+biobjPar.getParameterUrlName()+"/"+value);
				} catch (Exception e) {
					logger.error("Error while processing a BIParameter",e);
				}
			}
		}
		
		logger.debug("OUT");
  		return pars;
	}
	
	 /**
 	 * Function not implemented. Thid method should not be called
 	 * 
 	 * @param biobject The BIOBject to edit
 	 * @param profile the profile
 	 * 
 	 * @return the edits the document template build url
 	 * 
 	 * @throws InvalidOperationRequest the invalid operation request
 	 */
    public EngineURL getEditDocumentTemplateBuildUrl(Object biobject, IEngUserProfile profile)
	throws InvalidOperationRequest {
    	
    	EngineURL engineURL;
    	BIObject obj;
    	String documentId;
    	Engine engine;
    	String url;
    	HashMap parameters;
    	
    	logger.debug("IN");
    	
    	try {
	    	obj = null;			
			try {
				obj = (BIObject) biobject;
			} catch (ClassCastException cce) {
				logger.error("The input object is not a BIObject type", cce);
				return null;
			}
			
			documentId = obj.getId().toString();
			engine = obj.getEngine();
			url = engine.getUrl();
			//url = url.replaceFirst("/servlet/AdapterHTTP", "");
			//url += "/templateBuilder.jsp";
				
			parameters = new HashMap();
			parameters.put("document", documentId);
			parameters.put(PARAM_SERVICE_NAME, "FORM_ENGINE_TEMPLATE_BUILD_ACTION");
			parameters.put(PARAM_NEW_SESSION, "TRUE");
			parameters.put(PARAM_MODALITY, "EDIT");
			applySecurity(parameters, profile);
			
			engineURL = new EngineURL(url, parameters);
    	} finally {
			logger.debug("OUT");
		}
    	
		return engineURL;
    }

    /**
     * Function not implemented. This method should not be called
     * 
     * @param biobject  The BIOBject to edit
     * @param profile the profile
     * 
     * @return the new document template build url
     * 
     * @throws InvalidOperationRequest the invalid operation request
     */
    public EngineURL getNewDocumentTemplateBuildUrl(Object biobject, IEngUserProfile profile)
	throws InvalidOperationRequest {
    	
    	EngineURL engineURL;
    	BIObject obj;
    	String documentId;
    	Engine engine;
    	String url;
    	HashMap parameters;
    	
    	logger.debug("IN");
    	
    	try {
	    	obj = null;			
			try {
				obj = (BIObject) biobject;
			} catch (ClassCastException cce) {
				logger.error("The input object is not a BIObject type", cce);
				return null;
			}
			
			documentId = obj.getId().toString();
			engine = obj.getEngine();
			url = engine.getUrl();
			//url = url.replaceFirst("/servlet/AdapterHTTP", "");
			//url += "/templateBuilder.jsp";
				
			parameters = new HashMap();
			parameters.put("document", documentId);
			parameters.put(PARAM_SERVICE_NAME, "FORM_ENGINE_TEMPLATE_BUILD_ACTION");
			parameters.put(PARAM_NEW_SESSION, "TRUE");
			parameters.put(PARAM_MODALITY, "NEW");
			applySecurity(parameters, profile);
			
			engineURL = new EngineURL(url, parameters);
    	} finally {
			logger.debug("OUT");
		}
    	
		return engineURL;
    }

    
    
    private final static String PARAM_SERVICE_NAME = "ACTION_NAME";
    private final static String PARAM_NEW_SESSION = "NEW_SESSION";
    private final static String PARAM_MODALITY = "MODALITY";
    
	private Map applyService(Map parameters, BIObject biObject) {
		ObjTemplate template;
		
		logger.debug("IN");
		
		try {
			Assert.assertNotNull(parameters, "Input [parameters] cannot be null");
			//at the moment the initial aztion_name is fixed for extJS... in the future it will be set
			//by the template content (firstTag EXTCHART,...)
			parameters.put(PARAM_SERVICE_NAME, "CHART_ENGINE_EXTJS_START_ACTION");
			parameters.put(PARAM_MODALITY, "VIEW");			
			parameters.put(PARAM_NEW_SESSION, "TRUE");
		} catch(Throwable t) {
			throw new RuntimeException("Impossible to guess from template extension the engine startup service to call");
		} finally {
			logger.debug("OUT");
		}
		
		return parameters;
	}
	
	private ObjTemplate getTemplate(BIObject biObject) {
		ObjTemplate template;
		IObjTemplateDAO templateDAO;
		
		logger.debug("IN");
		
		try {
			Assert.assertNotNull(biObject, "Input [biObject] cannot be null");
			
			templateDAO = DAOFactory.getObjTemplateDAO();
			Assert.assertNotNull(templateDAO, "Impossible to instantiate templateDAO");
		
			template = templateDAO.getBIObjectActiveTemplate( biObject.getId() );
			Assert.assertNotNull(template, "Loaded template cannot be null");	
			
			logger.debug("Active template [" + template.getName() + "] of document [" + biObject.getLabel() + "] loaded succesfully");
		} catch(Throwable t) {
			throw new RuntimeException("Impossible to load template for document [" + biObject.getLabel()+ "]", t);
		} finally {
			logger.debug("OUT");
		}
		
		return template;
	}
	
    private Locale getLocale() {
    	logger.debug("IN");
		try {
			Locale locale = null;
			RequestContainer requestContainer = RequestContainer.getRequestContainer();
			SessionContainer permanentSession = requestContainer.getSessionContainer().getPermanentContainer();
			String language = (String) permanentSession.getAttribute(SpagoBIConstants.AF_LANGUAGE);
			String country = (String) permanentSession.getAttribute(SpagoBIConstants.AF_COUNTRY);
			logger.debug("Language retrieved: [" + language + "]; country retrieved: [" + country + "]");
			locale = new Locale(language, country);
			return locale;
		} catch (Exception e) {
		    logger.error("Error while getting locale; using default one", e);
		    return GeneralUtilities.getDefaultLocale();
		} finally  {
			logger.debug("OUT");
		}	
	}
    
    private void appendRequestParameter(Map parameters, String pname, String pvalue) {
		parameters.put(pname, pvalue);
		logger.debug("Added parameter [" + pname + "] with value [" + pvalue + "] to request parameters list");
	}
    
    
}

