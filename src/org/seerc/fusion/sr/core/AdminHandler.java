/**
 * Copyright 2007-2013 South-East European Research Centre (SEERC), 
 * The University of Sheffield (http://www.seerc.org)
 * 
 * Developed by Dimitrios Kourtesis (dkourtesis@seerc.org; d.kourtesis@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
 package org.seerc.fusion.sr.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import org.apache.axis2.AxisFault;
import org.seerc.fusion.sr.api.DiscoveryManagerSkeleton;
import org.seerc.fusion.sr.api.PublicationManagerSkeleton;
import org.seerc.fusion.sr.api.xsd.GetAllServiceProviderUUIDsResponse;
import org.seerc.fusion.sr.api.xsd.GetAllServiceProviderUUIDsResponse_type0;
import org.seerc.fusion.sr.api.xsd.GetAllServiceUUIDsResponse;
import org.seerc.fusion.sr.api.xsd.GetAllServiceUUIDsResponse_type0;
import org.seerc.fusion.sr.api.xsd.InitiatePublicationSessionRequest;
import org.seerc.fusion.sr.api.xsd.RemoveServiceProviderRequest;
import org.seerc.fusion.sr.api.xsd.RemoveServiceRequest;
import org.seerc.fusion.sr.exceptions.SemanticRegistryAuthException;
import org.seerc.fusion.sr.exceptions.SemanticRegistryCommunicationException;
import org.seerc.fusion.sr.exceptions.SemanticRegistryConfigurationException;
import org.seerc.fusion.sr.exceptions.SemanticRegistryException;
import org.seerc.fusion.sr.exceptions.SemanticRegistryMalformedInputException;
import org.seerc.fusion.sr.exceptions.SemanticRegistryMatchmakingException;
import org.seerc.fusion.sr.exceptions.SemanticRegistryNoMatchFoundException;
import org.seerc.fusion.sr.utils.FileUtils;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyCreationException;
import org.semanticweb.owl.model.OWLOntologyManager;

import org.uddi4j.UDDIException;
import org.uddi4j.client.UDDIProxy;
import org.uddi4j.datatype.service.BusinessService;
import org.uddi4j.datatype.tmodel.TModel;
import org.uddi4j.response.AuthToken;
import org.uddi4j.response.DispositionReport;
import org.uddi4j.response.Result;
import org.uddi4j.response.ServiceDetail;
import org.uddi4j.response.TModelDetail;
import org.uddi4j.response.TModelInfo;
import org.uddi4j.response.TModelList;
import org.uddi4j.transport.TransportException;
import org.uddi4j.util.CategoryBag;
import org.uddi4j.util.FindQualifier;
import org.uddi4j.util.FindQualifiers;
import org.uddi4j.util.KeyedReference;

/**
 * @author Dimitrios Kourtesis
 *
 */
public class AdminHandler
{
	// The UDDI4j proxy object
	UDDIProxy proxy;

	// The 5 tModels that need to be pre-registered with the UDDI server
	String SAWSDL_TMODEL_KEY;
	String CATEGORY_TMODEL_KEY;
	String INPUT_TMODEL_KEY;
	String OUTPUT_TMODEL_KEY;
	String INDEXING_TMODEL_KEY;

	// The location of the Semantic Registry Knowledge Base Ontology
	URI srkbOntologyPhysicalURI;

	// The location of the EAI Ontology
	URI eaiOntologyPhysicalURI;
	
	
	
	
	
	
	
	/**
	 * @throws SemanticRegistryConfigurationException
	 */
	public AdminHandler() 
	
	throws SemanticRegistryConfigurationException
	{						
		// Specify which of the 3 SOAP transports that UDDI4j supports should
		// be used to talk to jUDDI (Apache Axis, Apache SOAP 2.2, HP SOAP)
		System.setProperty("org.uddi4j.TransportClassName",
				"org.uddi4j.transport.ApacheAxisTransport");

		// Initialise the UDDIProxy object
		proxy = new UDDIProxy();

		// Create a new properties object to store the settings loaded
		// from the registry.properties file in WEB-INF/classes
		Properties properties = new Properties();

		// Try to obtain an inputstream to the registry.properties file
		InputStream in = getClass().getClassLoader().getResourceAsStream("registry.properties");
		
		if (in == null)
		{
			System.out.println("The registry.properties file could not be loaded from the classloader's default directory.");
			throw new SemanticRegistryConfigurationException("The registry.properties file could not be loaded from the classloader's default directory. " +
					"Make sure a registry.properties file is placed under the WEB-INF/classes/ directory");
		}
		else
		{
			System.out.println("The registry.properties file was loaded from the classloader's default directory.");
			try
			{
				// This will look in the current classloader's context and find the 
				// registry.properties files that is placed under /WEB-INF/classes/ 
				// (any file placed in the "src" folder of an Eclipse Dynamic Web 
				// Project gets deployed to WEB-INF/classes automatically)
				properties.load(in);
				
				String inquiryURL = null;
				String publishURL = null;
					
				// Get the URL values for the UDDI Inquiry and Publish API
				if (properties.getProperty("inquiry_url") != null)
					inquiryURL = properties.getProperty("inquiry_url");
				if (properties.getProperty("publish_url") != null)
					publishURL = properties.getProperty("publish_url");

				// Check the validity of URIs loaded from the
				// registry.properties file
				if (!InputValidator.isURIWellFormed(publishURL)) 
					throw new SemanticRegistryConfigurationException(
						"publish_url in registry.properties file is not a valid URI");
				if (!InputValidator.isURIWellFormed(inquiryURL)) 
					throw new SemanticRegistryConfigurationException(
						"inquiry_url in registry.properties file is not a valid URI");

				// Set the values
				proxy.setInquiryURL(inquiryURL);
				proxy.setPublishURL(publishURL);

				// Set the UUID values of the canonical tModels needed 
				SAWSDL_TMODEL_KEY = properties.getProperty("SAWSDL_Document_URL_tModel").replace("uuid:", "").replace(" ", "");
				CATEGORY_TMODEL_KEY = properties.getProperty("Category_Annotation_tModel").replace("uuid:", "").replace(" ", "");
				INPUT_TMODEL_KEY = properties.getProperty("Input_Annotation_tModel").replace("uuid:", "").replace(" ", "");
				OUTPUT_TMODEL_KEY = properties.getProperty("Output_Annotation_tModel").replace("uuid:", "").replace(" ", "");
				INDEXING_TMODEL_KEY = properties.getProperty("Subsumption_Indexing_tModel").replace("uuid:", "").replace(" ", "");
				
				// Check if the UUID keys loaded from the registry.properties file are valid
				if (!InputValidator.isUUIDKeyWellFormed(SAWSDL_TMODEL_KEY))
					throw new SemanticRegistryConfigurationException("SAWSDL_Document_URL_tModel property in registry.properties file is not a valid UUID");
				if (!InputValidator.isUUIDKeyWellFormed(CATEGORY_TMODEL_KEY))
					throw new SemanticRegistryConfigurationException("Category_Annotation_tModel property in registry.properties file is not a valid UUID");
				if (!InputValidator.isUUIDKeyWellFormed(INPUT_TMODEL_KEY))
					throw new SemanticRegistryConfigurationException("Input_Annotation_tModel property in registry.properties file is not a valid UUID");
				if (!InputValidator.isUUIDKeyWellFormed(OUTPUT_TMODEL_KEY))
					throw new SemanticRegistryConfigurationException("Output_Annotation_tModel property in registry.properties file is not a valid UUID");
				if (!InputValidator.isUUIDKeyWellFormed(INDEXING_TMODEL_KEY))
					throw new SemanticRegistryConfigurationException("Subsumption_Indexing_tModel property in registry.properties file is not a valid UUID");

				// Make sure that the tModel keys loaded from registry.properties 
				// correspond to the ones registered in the UDDI server
				try
				{
					validateHealthOfCanonicalTModelDeployment();
				}
				catch (SemanticRegistryException e)
				{
					throw new SemanticRegistryConfigurationException(e.getMessage());
				}

				// Check the validity of URIs loaded from the
				// registry.properties file
				if (!InputValidator.isURIWellFormed(properties
						.getProperty("srkb_ontology_physical_URI"))) throw new SemanticRegistryConfigurationException(
						"srkb_ontology_physical_URI in registry.properties file is not a valid URI");
				if (!InputValidator.isURIWellFormed(properties
						.getProperty("eai_ontology_physical_URI"))) throw new SemanticRegistryConfigurationException(
						"eai_ontology_physical_URI in registry.properties file is not a valid URI");

				// Set the locations (physical URIs) for the ontologies needed
				srkbOntologyPhysicalURI = URI.create(properties.getProperty("srkb_ontology_physical_URI"));
				eaiOntologyPhysicalURI = URI.create(properties.getProperty("eai_ontology_physical_URI"));
			}
			catch (IOException e)
			{
				System.out.println("SemanticRegistryConfigurationException occured!");
				throw new SemanticRegistryConfigurationException(
						"Problem loading the registry.properties file");
			}
		}
	}
	
	
	
	
	
	
	
	/**
	 * @param inquiryURL
	 * @param publishURL
	 * @throws SemanticRegistryMalformedInputException
	 * @throws SemanticRegistryException
	 */
	public AdminHandler(
			String inquiryURL, 
			String publishURL) 
	
	throws SemanticRegistryMalformedInputException, 
	SemanticRegistryException
	{
		// Specify which of the 3 SOAP transports that UDDI4j supports should
		// be used to talk to jUDDI (Apache Axis, Apache SOAP 2.2, HP SOAP)
		System.setProperty("org.uddi4j.TransportClassName", "org.uddi4j.transport.ApacheAxisTransport");

		// Initialise the UDDIProxy object
		proxy = new UDDIProxy();

		// Check the validity of URIs provided 
		if (!InputValidator.isURIWellFormed(publishURL)) 
			throw new SemanticRegistryMalformedInputException("Input parameter value 'publishURL' is not a valid URL");
		if (!InputValidator.isURIWellFormed(inquiryURL)) 
			throw new SemanticRegistryMalformedInputException("Input parameter value 'inquiryURL' is not a valid URL");

		
		// Set the values
		try
		{
			proxy.setInquiryURL(inquiryURL);
			proxy.setPublishURL(publishURL);
		}
		catch (MalformedURLException e)
		{
			throw new SemanticRegistryException("Input parameters 'inquiryURL' and 'publishURL' are valid but could not be set");
		}
	}
	
	
	
	
	
	
	
	/**
	 * Attempts to publish all the Canonical tModels that are necessary for the
	 * operation of the Semantic Registry, as specified in the
	 * registry.properties file.
	 * 
	 * @param authToken
	 * @return An array of Strings of the form TMODELNAME: TMODELKEY for each of
	 *         the Canonical tModels that got published, if any
	 * 
	 * @throws SemanticRegistryMalformedInputException
	 * @throws SemanticRegistryAuthException
	 * @throws SemanticRegistryCommunicationException
	 * @throws SemanticRegistryException
	 */
	public String[] deployCanonicalTModels(
			String username, 
			String password)
	
	throws SemanticRegistryMalformedInputException, 
			SemanticRegistryAuthException, 
			SemanticRegistryCommunicationException, 
			SemanticRegistryException
	{
		if (InputValidator.isTextParameterWellFormed(username) 
				&& InputValidator.isTextParameterWellFormed(password))
		{			
			// //////////////////////
			// GET AUTH TOKEN 
			// //////////////////////
			
			AuthToken token = null;
			try
			{
				// Provide userid and password to the UDDI server and get token
				token = proxy.get_authToken(username, password);
			}
			catch (UDDIException e)
			{
				DispositionReport dr = e.getDispositionReport();
				if (dr != null)
				{
					Vector results = dr.getResultVector();
					for (int i = 0; i < results.size(); i++)
					{
						Result r = (Result) results.elementAt(i);
						System.out.println("The UDDI server raised an exception with error number: " + r.getErrno());
						if (r.getErrInfo().getErrCode().equals("E_unknownUser"))
						{
							System.out.println("E_unknownUser: (10150) User and password are not known or not valid");
							throw new SemanticRegistryAuthException("Username and/or password do not match any known values");
						}
						throw new SemanticRegistryException("UDDI exception with error number: " + r.getErrno());
					}
				}
				else
				{
					System.out.println(
							"The UDDI server reported an internal error but did not provide a Disposition Report " +
							"to explain its cause. The problem resulted while trying to invoke the 'get_authToken' " +
							"operation, and the cause of the problem may be in the data provided (e.g. some parameter " +
							"value exceeding max character length), in a failure to communicate with the UDDI server, " +
							"or in a failure to communicate with the database that the UDDI server relies on.");
					throw new SemanticRegistryCommunicationException(
							"The UDDI server reported an internal error but did not provide a Disposition Report " +
							"to explain its cause. The problem resulted while trying to invoke the 'get_authToken' " +
							"operation, and the cause of the problem may be in the data provided (e.g. some parameter " +
							"value exceeding max character length), in a failure to communicate with the UDDI server, " +
							"or in a failure to communicate with the database that the UDDI server relies on.");
				}
			}
			catch (TransportException e)
			{
				System.out.println("TransportException occured!");
				throw new SemanticRegistryCommunicationException("Problem communicating with the UDDI server");
			}
			catch (Exception e)
			{
				System.out.println("SemanticRegistryException occured!");
				throw new SemanticRegistryException("An exception occured for unspecified reasons");
			}
			
			// If all goes well
			String authToken = token.getAuthInfoString();
			
			
			// //////////////////////
			// DEPLOY CANONICAL TMODELS
			// //////////////////////
			
			// A vector to hold all the tModels that are already published
			Vector<TModel> existingTModels = new Vector<TModel>();
			
			// A vector to hold all the tModels to be published
			Vector<TModel> tModelsToBePublished = new Vector<TModel>();
			
			// A vector to hold the tModels that were published successfully
			Vector<TModel> publishedTModels = new Vector<TModel>();
			
			// Create the tModels (tModelKey (UUID) must be empty for new tModels)
			TModel sawsdl = new TModel("", "SAWSDL_Document_URL_tModel");
			TModel category = new TModel("", "Category_Annotation_tModel");
			TModel input = new TModel("", "Input_Annotation_tModel");
			TModel output = new TModel("", "Output_Annotation_tModel");
			TModel subsumption = new TModel("", "Subsumption_Indexing_tModel");
			
			// Add each tModel to the appropriate list, depending on if it has been published or not
			System.out.println("Checking if canonical tModels have been already published...");
			
			String tmpKey = null;
			
			// Look for sawsdl
			tmpKey = findTModel(sawsdl.getNameString());
			if (tmpKey == null)
			{
				tModelsToBePublished.add(sawsdl);
			}
			else
			{
				sawsdl.setTModelKey(tmpKey);
				existingTModels.add(sawsdl);	
			}
			
			// Look for category
			tmpKey = findTModel(category.getNameString());
			if (tmpKey == null)
			{
				tModelsToBePublished.add(category);
			}
			else
			{
				category.setTModelKey(tmpKey);
				existingTModels.add(category);	
			}
			
			// Look for input
			tmpKey = findTModel(input.getNameString());
			if (tmpKey == null)
			{
				tModelsToBePublished.add(input);
			}
			else
			{
				input.setTModelKey(tmpKey);
				existingTModels.add(input);	
			}
			
			// Look for output
			tmpKey = findTModel(output.getNameString());
			if (tmpKey == null)
			{
				tModelsToBePublished.add(output);
			}
			else
			{
				output.setTModelKey(tmpKey);
				existingTModels.add(output);	
			}
			
			// Look for subsumption
			tmpKey = findTModel(subsumption.getNameString());
			if (tmpKey == null)
			{
				tModelsToBePublished.add(subsumption);
			}
			else
			{
				subsumption.setTModelKey(tmpKey);
				existingTModels.add(subsumption);	
			}
			
			
			// If all tModels have been already published there is no point in continuing
			if (tModelsToBePublished.isEmpty()) 
			{
				System.out.println("All canonical tModels have been already deployed and will not be replaced. Returning the existing tModel keys.");
			}
			else
			{
				System.out.println(tModelsToBePublished.size() + " tModels need to be published");
				
				// Create a categoryBag
				CategoryBag categoryBag = new CategoryBag();
	
				// Create a vector to hold all keyedReferences to be included in the categoryBag
				Vector<KeyedReference> krVector = new Vector<KeyedReference>();
	
				// Create and add a keyedReference to the vector
				// tModelKey="uuid:c1acf26d-9672-4404-9d70-39b756e62ab4"
				// keyName="uddi-org:types" 
				// keyValue="categorization"
				KeyedReference kr1 = new KeyedReference();
				kr1.setTModelKey("uuid:c1acf26d-9672-4404-9d70-39b756e62ab4");
				kr1.setKeyName("uddi-org:types");
				kr1.setKeyValue("categorization");
				krVector.add(kr1);
	
				// Create and add a keyedReference to the vector
				// tModelKey="uuid:c1acf26d-9672-4404-9d70-39b756e62ab4"
				// keyName="uddi-org:types" 
				// keyValue="unchecked"
				KeyedReference kr2 = new KeyedReference();
				kr2.setTModelKey("uuid:c1acf26d-9672-4404-9d70-39b756e62ab4");
				kr2.setKeyName("uddi-org:types");
				kr2.setKeyValue("unchecked");
				krVector.add(kr2);
	
				// Add the keyedReferences vector to the categoryBag
				categoryBag.setKeyedReferenceVector(krVector);
				
				// Add the categoryBag and a text description to each of the tModels to be published
				for (int i = 0; i < tModelsToBePublished.size(); i++)
				{
					tModelsToBePublished.elementAt(i).setCategoryBag(categoryBag);
					tModelsToBePublished.elementAt(i).setDefaultDescriptionString("Canonical tModel used by the Semantic Registry");
				}
				
				try
				{
					// Publish the tModels inside the vector to the UDDI server
					TModelDetail tModelDetailResponse = proxy.save_tModel(authToken, tModelsToBePublished);
	
					// Process returned details
					Vector tModelVectorResponse = tModelDetailResponse.getTModelVector();
					
					TModel returnedTModel = null;
					for (int i = 0; i < tModelVectorResponse.size(); i++)
					{
						returnedTModel = (TModel) tModelVectorResponse.elementAt(i);
						System.out.println("TModel Saved: " + returnedTModel.getNameString() + "=" + returnedTModel.getTModelKey());
						
						publishedTModels.add(returnedTModel);
					}
				}
				catch (UDDIException e)
				{
					DispositionReport dr = e.getDispositionReport();
					if (dr != null)
					{
						Vector results = dr.getResultVector();
						for (int i = 0; i < results.size(); i++)
						{
							Result r = (Result) results.elementAt(i);
							System.out.println("The UDDI server raised an exception with error number: " + r.getErrno());
							if (r.getErrInfo().getErrCode().equals("E_invalidKeyPassed"))
							{
								System.out.println("E_invalidKeyPassed: (10210) The uuid_key did not match any known key values");
								throw new SemanticRegistryException("The authentication token does not match any known values");
							}
							if (r.getErrInfo().getErrCode().equals("E_authTokenExpired"))
							{
								System.out.println("E_invalidKeyPassed: (10110) The authentication token has timed out");
								throw new SemanticRegistryAuthException("The authentication token has timed out");
							}
							if (r.getErrInfo().getErrCode().equals("E_authTokenRequired"))
							{
								System.out.println("E_authTokenRequired: (10120) The authentication token is invalid");
								throw new SemanticRegistryAuthException("The authentication token is invalid");
							}
							throw new SemanticRegistryException("UDDI exception with error number: " + r.getErrno());
						}
					}
					else
					{
						System.out.println(
								"The UDDI server reported an internal error but did not provide a Disposition Report " +
								"to explain its cause. The problem resulted while trying to invoke the 'save_tModel' " +
								"operation, and the cause of the problem may be in the data provided (e.g. some parameter " +
								"value exceeding max character length), in a failure to communicate with the UDDI server, " +
								"or in a failure to communicate with the database that the UDDI server relies on.");
						throw new SemanticRegistryCommunicationException(
								"The UDDI server reported an internal error but did not provide a Disposition Report " +
								"to explain its cause. The problem resulted while trying to invoke the 'save_tModel' " +
								"operation, and the cause of the problem may be in the data provided (e.g. some parameter " +
								"value exceeding max character length), in a failure to communicate with the UDDI server, " +
								"or in a failure to communicate with the database that the UDDI server relies on.");
					}
				}
				catch (TransportException e)
				{
					System.out.println("TransportException occured!");
					throw new SemanticRegistryCommunicationException("Problem communicating with the UDDI server");
				}
				catch (Exception e)
				{
					System.out.println("SemanticRegistryException occured!");
					throw new SemanticRegistryException("An exception occured for unspecified reasons");
				}
			}
			
			// Add the published TModels to the existing TModels
			existingTModels.addAll(publishedTModels);
			
			// A list of tModelName=tModelKey assignments to be returned
			String[] returnedParams = new String[existingTModels.size()];
			
			TModel tmp = null;
			for (int i = 0; i < existingTModels.size(); i++)
			{
				tmp = (TModel) existingTModels.elementAt(i);
				
				// Set the name-key pair to be returned as name=key assignment
				returnedParams[i] = new String((tmp.getNameString() + "=" + tmp.getTModelKey()));
			}
			
			// Return a list of the deployed tModel names and UUIDs
			return returnedParams;

		} // endif input is well-formed
		else
		{		
			System.out.println("SemanticRegistryMalformedInputException occured!");
			
			if (!InputValidator.isTextParameterWellFormed(username))
				throw new SemanticRegistryMalformedInputException("Input parameter value 'username' is not a valid text parameter");
			if (!InputValidator.isTextParameterWellFormed(password))
				throw new SemanticRegistryMalformedInputException("Input parameter value 'password' is not a valid text parameter");
			
			throw new SemanticRegistryMalformedInputException("One or more input parameter values are malformed");
		}
		
	}
	
	
	
	
	
	
		
	/**
	 * Checks if the canonical tModels that are necessary for the operation of
	 * the Semantic Registry have been published in the UDDI server specified in
	 * the registry.properties file.
	 * 
	 * @return An array of Strings of the form TMODELNAME: TMODELKEY for each of
	 *         the Canonical tModels found, if any
	 *         
	 * @throws SemanticRegistryNoMatchFoundException
	 * @throws SemanticRegistryCommunicationException
	 * @throws SemanticRegistryException
	 * @throws SemanticRegistryNoMatchFoundException
	 */
	public String[] getDeployedCanonicalTModels() 
		
	throws SemanticRegistryCommunicationException, 
		SemanticRegistryException, SemanticRegistryNoMatchFoundException
	{
		String[] tModelNames = { "SAWSDL_Document_URL_tModel",
				"Category_Annotation_tModel", "Input_Annotation_tModel",
				"Output_Annotation_tModel", "Subsumption_Indexing_tModel" };
		
		// Create the list of tModelDetails to be returned
		Vector<String> tModelDetails = new Vector<String>();
		String tempKey = null;

		for (int i = 0; i < tModelNames.length; i++)
		{
			//System.out.println("Trying to retrieve tModel:" + tModelNames[i]);
			tempKey = findTModel(tModelNames[i]);
			if (tempKey != null) 
			{
				//System.out.println(tModelNames[i] + " was found: " + tempKey);
				//tModelDetails.add(tModelNames[i] + ":" + tempKey.replace("uuid:", ""));
				tModelDetails.add(tModelNames[i] + "(" + tempKey + ")");
			}
			else
			{
				//System.out.println(tModelNames[i] + " was not found!");
			}
		}
		if (tModelDetails.size() == 0)
		{
			throw new SemanticRegistryNoMatchFoundException("No canonical tModels have been deployed yet!");
		}
		else 
		{
			//System.out.println(tModelDetails.size() + " tModels were found in total");
			String[] tModelDetailsArray = new String[(tModelDetails.size())];
			tModelDetails.copyInto(tModelDetailsArray);
			return tModelDetailsArray;
		}
	}
	
	
	
	
	
	
		
	/**
	 * Retrieves the key of a tModel, based on its name
	 * 
	 * @param tModelName The Name of the Model to search for
	 * @return The UUID key of the tModel located
	 * 
	 * @throws SemanticRegistryException 
	 * @throws SemanticRegistryCommunicationException 
	 */
	private String findTModel(String tModelName)
			
	throws SemanticRegistryException,
			SemanticRegistryCommunicationException
	{
		Vector tModelInfoVector = null;
		
		// Define findQualifier, to make sure the most recent model is on top of the list
		FindQualifier findQualifier = new FindQualifier();
		findQualifier.setText(FindQualifier.sortByDateDesc);

		FindQualifiers findQualifiers = new FindQualifiers();
		findQualifiers.add(findQualifier);

		String tModelKey = null;
		TModelList tModelList = null;
		try
		{
			// find model based on Name and findQualifiers
			tModelList = proxy.find_tModel(tModelName, null, null, findQualifiers, 0);

			tModelInfoVector = tModelList.getTModelInfos().getTModelInfoVector();

			if (tModelInfoVector.size() > 0)
			{
				TModelInfo tModelInfo = (TModelInfo) tModelInfoVector.get(0);
				tModelKey = tModelInfo.getTModelKey();
				System.out.println("Found published tModel in UDDI: " + tModelName +"="+ tModelKey );
			}
			else
			{
				System.out.println("tModel not found in UDDI: " + tModelName);
			}
		}
		catch (UDDIException e)
		{
			DispositionReport dr = e.getDispositionReport();
			if (dr != null)
			{
				Vector results = dr.getResultVector();
				for (int i = 0; i < results.size(); i++)
				{
					Result r = (Result) results.elementAt(i);
					System.out.println("The UDDI server raised an exception with error number: " + r.getErrno());
					throw new SemanticRegistryException("UDDI exception with error number: " + r.getErrno());
				}
			}
			else
			{
				System.out.println("UDDI exception without Disposition Report -> SemanticRegistryCommunicationException");
				throw new SemanticRegistryCommunicationException("Problem communicating with the UDDI server");
			}
		}
		catch (TransportException e)
		{
			System.out.println("TransportException occured!");
			throw new SemanticRegistryCommunicationException("Problem communicating with the UDDI server");
		}
		catch (Exception e)
		{
			System.out.println("SemanticRegistryException occured!");
			throw new SemanticRegistryException("An exception occured for unspecified reasons");
		}

		// If all goes well
		return tModelKey;
	}
	
	
	
	
	
	
	
	
	/**
	 * Checks if a tModel exists, based on its name
	 * 
	 * @param tModelName The Name of the Model to search for
	 * @return True, if a tModel with the specified name exists 
	 * 
	 * @throws SemanticRegistryException
	 * @throws SemanticRegistryCommunicationException
	 */
	private boolean existsCanonicalTModel(String tModelName)
			
	throws SemanticRegistryException,
			SemanticRegistryCommunicationException
	{
		System.out.println("Checking tModel: " + tModelName);
		if (findTModel(tModelName) != null) return true;
		return false;
	}
	
	
	
	
	
	
	
	
	/**
	 * @throws SemanticRegistryException
	 */
	private void validateHealthOfCanonicalTModelDeployment() 
	
	throws SemanticRegistryException
	{
		// Assert that key values for all 5 canonical tModels have been read
		// from the registry.properties file
		if ((SAWSDL_TMODEL_KEY == null || SAWSDL_TMODEL_KEY.length() != 36)
				|| (CATEGORY_TMODEL_KEY == null || CATEGORY_TMODEL_KEY.length() != 36)
				|| (INPUT_TMODEL_KEY == null || INPUT_TMODEL_KEY.length() != 36)
				|| (OUTPUT_TMODEL_KEY == null || OUTPUT_TMODEL_KEY.length() != 36)
				|| (INDEXING_TMODEL_KEY == null || INDEXING_TMODEL_KEY.length() != 36))
		{
			System.out.println(
					"Incomplete Semantic Registry deployment - " +
					"the key values for one or more Canonical tModels have not been set correctly in the registry.properties file");
			throw new SemanticRegistryException(
					"Incomplete Semantic Registry deployment - " +
					"the key values for one or more Canonical tModels have not been set correctly in the registry.properties file");
		}

		// Key values have been read for all 5 Canonical tModels, but
		// do they reflect the real keys of the published tModels, if any?
		try
		{
			String[] realTModels = getDeployedCanonicalTModels();

			// Assert that 5 canonical tModels been published in the registry
			if (realTModels.length != 5)
			{
				System.out.println("Incomplete Semantic Registry deployment - not all Canonical tModels have been deployed in the Semantic Registry");
				throw new SemanticRegistryException(
						"Incomplete Semantic Registry deployment - not all Canonical tModels have been deployed in the Semantic Registry");
			}
			// 5 canonical tModels been published, but do their keys match the
			// ones that were read from the registry.properties file?
			else
			{
				Set setOfReadKeys = new HashSet();
				setOfReadKeys.add(SAWSDL_TMODEL_KEY);
				setOfReadKeys.add(CATEGORY_TMODEL_KEY);
				setOfReadKeys.add(INPUT_TMODEL_KEY);
				setOfReadKeys.add(OUTPUT_TMODEL_KEY);
				setOfReadKeys.add(INDEXING_TMODEL_KEY);

				// Assert that the keys read from the registry.properties file
				// are all different
				if (setOfReadKeys.size() != 5)
				{
					System.out.println(
							"Incomplete Semantic Registry deployment - " +
							"erroneous duplicate key values have been set in the registry.properties file");
					throw new SemanticRegistryException(
							"Incomplete Semantic Registry deployment - " +
							"erroneous duplicate key values have been set in the registry.properties file");
				}

				// Extract the tModelKeys from the canonical TModel details
				// information text,
				// which is of the form: TMODELNAME(uuid:TMODELKEY)
				Set setOfPublishedKeys = new HashSet();
				for (int i = 0; i < realTModels.length; i++)
				{
					String key = (realTModels[i].substring(realTModels[i].indexOf(":") + 1)).replace(")", "");
					// System.out.println("Extracted key: " + key + ", taken
					// from: " + realTModels[i]);
					setOfPublishedKeys.add(key);
				}

				// Assert that the two sets contain the exact same elements
				if (!(setOfPublishedKeys.containsAll(setOfReadKeys) && setOfReadKeys
						.containsAll(setOfPublishedKeys)))
				{
					System.out.println(
							"Incomplete Semantic Registry deployment - " +
							"the key values that have been set in the registry.properties file do not match the key values of the Canonical tModels published in the UDDI server");
					throw new SemanticRegistryException(
							"Incomplete Semantic Registry deployment - " +
							"the key values that have been set in the registry.properties file do not match the key values of the Canonical tModels published in the UDDI server");
				}
			}
		}
		catch (SemanticRegistryCommunicationException e)
		{
			throw new SemanticRegistryException(
					"Problem occured while trying to validate the health of the Semantic Registry deployment");
		}
		catch (SemanticRegistryNoMatchFoundException e)
		{
			throw new SemanticRegistryException(
					"Problem occured while trying to validate the health of the Semantic Registry deployment");
		}
	}
	
	
	
	
	
	
		
	/**
	 * @param authToken
	 * @throws SemanticRegistryMalformedInputException
	 * @throws SemanticRegistryCommunicationException
	 * @throws SemanticRegistryNoMatchFoundException
	 * @throws SemanticRegistryException
	 * @throws SemanticRegistryMatchmakingException
	 * @throws SemanticRegistryAuthException
	 */
	public void removeAllCanonicalTModels(String authToken)
	throws SemanticRegistryMalformedInputException,
	SemanticRegistryCommunicationException, 
	SemanticRegistryNoMatchFoundException, 
	SemanticRegistryException, 
	SemanticRegistryMatchmakingException, 
	SemanticRegistryAuthException
	{		
		if (authToken != null) authToken = authToken.trim();
		
		if (InputValidator.isAuthenticationTokenWellFormed(authToken))	
		{
			String[] deployedTModels = null;
			try
			{
				deployedTModels = getDeployedCanonicalTModels();
			}
			catch (SemanticRegistryNoMatchFoundException e)
			{
			}
			
			if (deployedTModels == null) 
				throw new SemanticRegistryException("No canonical tModels could be retrieved from UDDI");
			
			// Load all properties already set in the registry.properties file
			Properties properties = new Properties();
			InputStream in = getClass().getClassLoader().getResourceAsStream("registry.properties");
			try
			{
				properties.load(in);
			}
			catch (IOException e)
			{
				throw new SemanticRegistryCommunicationException("Problem occured while attempting to read the registry.properties file");
			}
			
			// Extract the tModelKeys from the canonical TModel details
			// information text, which is of the form: TMODELNAME(uuid:TMODELKEY)
			for (int i = 0; i < deployedTModels.length; i++)
			{
				String tModelKey = (deployedTModels[i].substring(deployedTModels[i].indexOf(":") + 1)).replace(")", "");
				System.out.println("Extracted key: " + tModelKey + ", taken from: " + deployedTModels[i]);
				try
				{
					System.out.println("Trying to remove tModel: " + tModelKey + " from the UDDI server");
					proxy.delete_tModel(authToken, tModelKey);
					
					System.out.println("Trying to remove tModel: " + tModelKey + " from the registry.properties file");
					properties.remove(tModelKey);
				}
				catch (UDDIException e)
				{
					DispositionReport dr = e.getDispositionReport();
					if (dr != null)
					{
						Vector results = dr.getResultVector();
						for (int k = 0; k < results.size(); k++)
						{
							Result r = (Result) results.elementAt(k);
							System.out.println("The UDDI server raised an exception with error number: "
											+ r.getErrno());
								if (r.getErrInfo().getErrCode().equals("E_authTokenRequired"))
							{
								System.out.println("E_authTokenRequired: (10120) The authentication token is invalid");
								throw new SemanticRegistryAuthException("The authentication token is invalid");
							}
							if (r.getErrInfo().getErrCode()
									.equals("E_authTokenExpired"))
							{
								System.out.println("E_invalidKeyPassed: (10110) The authentication token has timed out");
								throw new SemanticRegistryAuthException("The authentication token has timed out");
							}
							if (r.getErrInfo().getErrCode().equals("E_invalidKeyPassed"))
							{
								System.out.println("E_invalidKeyPassed: (10210) The uuid_key did not match any known key values");
								throw new SemanticRegistryException("One or more UUID keys do not match any known values");
							}
							throw new SemanticRegistryException("UDDI exception with error number: " + r.getErrno());
						}
					}
					else
					{
						System.out.println("UDDI exception without Disposition Report -> SemanticRegistryCommunicationException");
						throw new SemanticRegistryCommunicationException("Problem communicating with the UDDI server");
					}
				}
				catch (TransportException e)
				{
					System.out.println("TransportException occured!");
					throw new SemanticRegistryCommunicationException("Problem communicating with the UDDI server");
				}
				catch (Exception e)
				{
					System.out.println("SemanticRegistryException occured!");
					throw new SemanticRegistryException("An exception occured for unspecified reasons");
				}
			}
			
			// Prepare writing to the file
			File registryPropertiesFile = null;
			OutputStream stream = null;
			try
			{
				registryPropertiesFile = new File(getClass().getClassLoader().getResource("registry.properties").getFile());
				if (registryPropertiesFile.exists()) 
					System.out.println("registry.properties file found in: " + getClass().getClassLoader().getResource("registry.properties").getFile());
				stream = new FileOutputStream(registryPropertiesFile);
			}
			catch (FileNotFoundException e1)
			{
				throw new SemanticRegistryCommunicationException("registry.properties file");
			}
			
			// Save the updated properties back to the registry.properties file
			try
			{
				properties.store(stream, null);
				stream.close();
			}
			catch (IOException e)
			{
				throw new SemanticRegistryCommunicationException("Problem occured while attempting to write to the registry.properties file");
			}
			
		} // endif input is well-formed
		else
		{
			System.out.println("SemanticRegistryMalformedInputException occured!");
			throw new SemanticRegistryMalformedInputException("One or more input parameter values are malformed");
		}
	}
	
	
	
	
	
	
	
	/**
	 * @param authToken
	 * @return
	 * @throws SemanticRegistryMalformedInputException
	 * @throws SemanticRegistryCommunicationException
	 * @throws SemanticRegistryNoMatchFoundException
	 * @throws SemanticRegistryException
	 * @throws SemanticRegistryMatchmakingException
	 * @throws SemanticRegistryAuthException
	 */
	public boolean removeAllServiceProviders(String authToken)
	throws SemanticRegistryMalformedInputException,
	SemanticRegistryCommunicationException, 
	SemanticRegistryNoMatchFoundException, 
	SemanticRegistryException, 
	SemanticRegistryMatchmakingException, 
	SemanticRegistryAuthException
	{		
		if (authToken != null) authToken = authToken.trim();
		boolean success = false;
		
		if (InputValidator.isAuthenticationTokenWellFormed(authToken))	
		{
			DiscoveryManagerSkeleton man = new DiscoveryManagerSkeleton();
			GetAllServiceProviderUUIDsResponse res = null;
			GetAllServiceProviderUUIDsResponse_type0 sub = null;
			try
			{
				res = man.getAllServiceProviderUUIDs();
			}
			catch (AxisFault e)
			{
				System.out.println("Problem retrieving the UUIDs of service providers");
			}
						
			sub = res.getGetAllServiceProviderUUIDsResponse();
			String[] uuids = sub.getString();
			
			PublicationManagerSkeleton pm = new PublicationManagerSkeleton();
			InitiatePublicationSessionRequest init = new InitiatePublicationSessionRequest();

			for (int i = 0; i < uuids.length; i++)
			{
				RemoveServiceProviderRequest rem = new RemoveServiceProviderRequest();
				rem.setServiceProviderUUID(uuids[i]);
				rem.setAuthenticationToken(authToken);
				try
				{
					pm.removeServiceProvider(rem);
				}
				catch (AxisFault e)
				{
					System.out.println("Problem removing service with UUID: " + uuids[i]);
				}
			}
			
		} // endif input is well-formed
		else
		{
			System.out.println("SemanticRegistryMalformedInputException occured!");
			throw new SemanticRegistryMalformedInputException("One or more input parameter values are malformed");
		}
		return success;
	}
	




	
	
	
	/**
	 * @param authToken
	 * @return
	 * @throws SemanticRegistryMalformedInputException
	 * @throws SemanticRegistryCommunicationException
	 * @throws SemanticRegistryNoMatchFoundException
	 * @throws SemanticRegistryException
	 * @throws SemanticRegistryMatchmakingException
	 * @throws SemanticRegistryAuthException
	 */
	public boolean removeAllServices(String authToken)
	throws SemanticRegistryMalformedInputException,
	SemanticRegistryCommunicationException, 
	SemanticRegistryNoMatchFoundException, 
	SemanticRegistryException, 
	SemanticRegistryMatchmakingException, 
	SemanticRegistryAuthException
	{		
		if (authToken != null) authToken = authToken.trim();
		boolean success = false;
		
		if (InputValidator.isAuthenticationTokenWellFormed(authToken))	
		{
			DiscoveryManagerSkeleton man = new DiscoveryManagerSkeleton();
			GetAllServiceUUIDsResponse res = null;
			GetAllServiceUUIDsResponse_type0 sub = null;
			try
			{
				res = man.getAllServiceUUIDs();
			}
			catch (AxisFault e)
			{
				System.out.println("Problem retrieving the UUIDs of published services");
			}
						
			sub = res.getGetAllServiceUUIDsResponse();
			String[] uuids = sub.getString();
			
			PublicationManagerSkeleton pm = new PublicationManagerSkeleton();
			InitiatePublicationSessionRequest init = new InitiatePublicationSessionRequest();

			for (int i = 0; i < uuids.length; i++)
			{
				RemoveServiceRequest rem = new RemoveServiceRequest();
				rem.setServiceUUID(uuids[i]);
				rem.setAuthenticationToken(authToken);
				try
				{
					pm.removeService(rem);
				}
				catch (AxisFault e)
				{
					System.out.println("Problem removing service with UUID: " + uuids[i]);
				}
			}
			
		} // endif input is well-formed
		else
		{
			System.out.println("SemanticRegistryMalformedInputException occured!");
			throw new SemanticRegistryMalformedInputException("One or more input parameter values are malformed");
		}
		return success;
	}
	
	
	
	
		
	
	
	/**
	 * @param authToken
	 * @param requestFunctionalProfileURI
	 * @return
	 * @throws SemanticRegistryMalformedInputException
	 * @throws SemanticRegistryCommunicationException
	 * @throws SemanticRegistryNoMatchFoundException
	 * @throws SemanticRegistryException
	 * @throws SemanticRegistryMatchmakingException
	 * @throws SemanticRegistryAuthException
	 * @throws SemanticRegistryConfigurationException
	 */
	public String[] addRFPToIndex(
			String authToken, 
			String requestFunctionalProfileURI)
			
	throws SemanticRegistryMalformedInputException,
			SemanticRegistryCommunicationException, 
			SemanticRegistryNoMatchFoundException, 
			SemanticRegistryException, 
			SemanticRegistryMatchmakingException, 
			SemanticRegistryAuthException, 
			SemanticRegistryConfigurationException
	{
		// Remove leading and trailing whitespaces 
		if (requestFunctionalProfileURI != null)
			requestFunctionalProfileURI = requestFunctionalProfileURI.trim();
		
		if (authToken != null)
			authToken = authToken.trim();
			
		if (InputValidator.isURIWellFormed(requestFunctionalProfileURI) 
				&& InputValidator.isAuthenticationTokenWellFormed(authToken))	
		{
			boolean success = false;
			
			// Check if the provided requestedFunctionalProfileURI 
			// is amenable to processing, i.e.: 
			// 1) if it is a valid URI, 
			// 2) if it contains a fragment component, 
			// 3) if the ontology file can be downloaded from the URI, and 
			// 4) if the fragment identifier identifies an ontological 
			// concept that exists within the ontology document (in an 
			// rdf:about="#X" rdf:about="uri#X" or rdf:ID="X" statement)
			try
			{
				success = InputValidator.isValidURIWithFragmentDefinedInRetrievableDocument(requestFunctionalProfileURI);
			}
			catch (MalformedURLException e)
			{
				throw new SemanticRegistryMalformedInputException(e.getMessage());
			}
			catch (IOException e)
			{
				throw new SemanticRegistryMalformedInputException(e.getMessage());
			}
			
			if (!success) 
				throw new SemanticRegistryMalformedInputException("The provided parameter value is not a valid URI pointing to a retrievable RFP");
			
			// Get the base URI of the RFP URI (remove the fragment)
			String rfpBaseURI = requestFunctionalProfileURI.toString().substring(0,requestFunctionalProfileURI.toString().indexOf("#"));
			
			// Get the base URI of the SRKB ontology
			String srkbOntologyBaseURI = FileUtils.getBaseURIFromOntologyDocument(srkbOntologyPhysicalURI.normalize().toString());
			
			// Get the base URI of the EAI ontology
			String eaiOntologyBaseURI = FileUtils.getBaseURIFromOntologyDocument(eaiOntologyPhysicalURI.normalize().toString());
			
			boolean rfpDefinedInKnownNamespace = false;
			
			// Check if the baseURI of the requestedFunctionalProfileURI
			// coincides with the physicalURI of the SRKB ontology or the EAI ontology
			if ( rfpBaseURI.equalsIgnoreCase(srkbOntologyBaseURI) 
					|| rfpBaseURI.equalsIgnoreCase(eaiOntologyBaseURI) )
			{
				rfpDefinedInKnownNamespace = true;
				//System.out.println("The RFP is defined in a known namespace");
			}
			
			// Vector for storing the UUIDs of the updated services
			Vector<String> updatedAdvertisementUUIDs = new Vector<String>();
			
			// Get the URIs of all AFPs that can satisfy the RFP
			SemanticProfileURICollection matchingAFPs = null;
			
			// Create a matchmaker to find AFPs that can match the provided RFP
			AdminTimeMatchmaker matchmaker = new AdminTimeMatchmaker();
			
			try
			{
				matchingAFPs = matchmaker.getMatchingAFPsForRFP(srkbOntologyPhysicalURI, 
																eaiOntologyPhysicalURI, 
																URI.create(requestFunctionalProfileURI)); 
			}
			catch (SemanticRegistryMatchmakingException e)
			{
				throw new SemanticRegistryMatchmakingException(
						"Problem occured during the semantic classification of the advertisement");
			}
			catch (SemanticRegistryConfigurationException e)
			{
				if (e.getMessage() == null || e.getMessage().length() == 0)
					throw new SemanticRegistryConfigurationException("Problem occured during the semantic classification of the advertisement");
				else
					throw new SemanticRegistryConfigurationException(e.getMessage());
			}			
			
			// A counter to assist in providing meaningful feedback to the user 
			int numberOfServicesAlreadyIndexed = 0;
			
			// if no matching service advertisements were returned...
			if (matchingAFPs == null || matchingAFPs.getProfileURIs().size() == 0)
			{
				// throw new SemanticRegistryNoMatchFoundException
				System.out
						.println("No matching service was found for profile: "
								+ requestFunctionalProfileURI
								+ ". Semantic matchmaking didn't reveal any Advertisement "
								+ "Functional Profile (AFP) matching the provided Request Functional Profile (RFP).");
			}
			// if matching service advertisements were successfully returned...
			else
			{
				System.out.println("There are some changes to be made in the indexing of advertisements");

				List<String> affectedServiceAdvertisements = matchingAFPs.getProfileURIs();
				URI matchingAFPURI = null;
				String serviceUUID = "";
				
				// for every affected service, retrieve its advertisement
				// and add a tModelReference to the INDEXING_TMODEL_KEY,
				// with the provided RFP URI as value
				for (int i = 0; i < affectedServiceAdvertisements.size(); i++)
				{
					matchingAFPURI = URI.create(affectedServiceAdvertisements.get(i));
					serviceUUID = matchingAFPURI.getFragment();
					ServiceDetail serviceDetail = null;

					// Try to get the service detail from UDDI using the service
					// key
					try
					{
						serviceDetail = proxy.get_serviceDetail(serviceUUID);
					}
					catch (UDDIException e)
					{
						DispositionReport dr = e.getDispositionReport();
						if (dr != null)
						{
							Vector results = dr.getResultVector();
							for (int k = 0; k < results.size(); k++)
							{
								Result r = (Result) results.elementAt(k);
								if (r.getErrInfo().getErrCode().equals("E_invalidKeyPassed")) 
									System.out.println("The advertisement of service " + serviceUUID + " was not updated (no such UUID)");
							}
						}
					}
					catch (TransportException e1)
					{
						System.out.println("TransportException occured!");
						throw new SemanticRegistryCommunicationException("Problem communicating with the UDDI server");
					}

					if (serviceDetail != null)
					{
						System.out.println("Preparing to update: " + serviceUUID);
						
						// Get the first business entity from the returned vector
						Vector<BusinessService> businessServiceVector = serviceDetail.getBusinessServiceVector();

						// Get the first and only BusinessService element
						// from the vector
						BusinessService businessService = (BusinessService) businessServiceVector.get(0);

						// Get the categoryBag from the BusinessService
						CategoryBag oldCategoryBag = businessService.getCategoryBag();

						// Get the old keyedReferences vector
						Vector<KeyedReference> oldKeyedRefVector = oldCategoryBag.getKeyedReferenceVector();

						// Copy the keyedReferences vector from the old
						// category bag into a new vector
						Vector<KeyedReference> newKeyedRefVector = oldKeyedRefVector;		
						
						// Check if the old keyed reference vector already contains a reference to this RFP
						if (!newKeyedRefVector.contains(generateKeyedReference(INDEXING_TMODEL_KEY, requestFunctionalProfileURI)))
						{				
							// Add a new keyedReference to the keyed reference
							// vector for the AFP that matches the RFP
							newKeyedRefVector.add(generateKeyedReference(INDEXING_TMODEL_KEY, requestFunctionalProfileURI));

							// Copy the old category bag into a new one
							CategoryBag newCategoryBag = oldCategoryBag;

							// Replace the old keyedReferences vector with the
							// new (enriched) one
							newCategoryBag.setKeyedReferenceVector(newKeyedRefVector);

							// Replace the old categoryBag with the new one (the
							// rest of the
							// attributes in the businessService object should
							// remain unchanged)
							businessService.setCategoryBag(newCategoryBag);

							// Empty the businessServiceVector (i.e. remove the
							// old businessService object)
							businessServiceVector.clear();

							// Add the old businessService again, which now
							// contains the new categoryBag
							businessServiceVector.addElement(businessService);

							// Try to save the service
							try
							{
								serviceDetail = proxy.save_service(authToken, businessServiceVector);
								System.out.println("The advertisement of service " + serviceUUID + " was updated successfully");
								updatedAdvertisementUUIDs.add(serviceUUID);
							}
							catch (UDDIException e)
							{
								DispositionReport dr = e.getDispositionReport();
								if (dr != null)
								{
									Vector results = dr.getResultVector();
									for (int k = 0; k < results.size(); k++)
									{
										Result r = (Result) results.elementAt(k);
										System.out
												.println("The UDDI server raised an exception with error number: "
														+ r.getErrno());
										if (r.getErrInfo().getErrCode().equals("E_authTokenRequired"))
										{
											System.out
													.println("E_authTokenRequired: (10120) The authentication token is invalid");
											throw new SemanticRegistryAuthException(
													"The authentication token is invalid");
										}
										if (r.getErrInfo().getErrCode().equals("E_authTokenExpired"))
										{
											System.out
													.println("E_invalidKeyPassed: (10110) The authentication token has timed out");
											throw new SemanticRegistryAuthException(
													"The authentication token has timed out");
										}
										if (r.getErrInfo().getErrCode().equals("E_invalidKeyPassed"))
										{
											System.out
													.println("E_invalidKeyPassed: (10210) The uuid_key did not match any known key values");
											throw new SemanticRegistryException(
													"One or more UUID keys do not match any known values");
										}
										throw new SemanticRegistryException(
												"UDDI exception with error number: " + r.getErrno());
									}
								}
								else
								{
									System.out
											.println("UDDI exception without Disposition Report -> SemanticRegistryCommunicationException");
									throw new SemanticRegistryCommunicationException(
											"Problem communicating with the UDDI server");
								}
							}
							catch (TransportException e)
							{
								System.out.println("TransportException occured!");
								throw new SemanticRegistryCommunicationException(
										"Problem communicating with the UDDI server");
							}
							catch (Exception e)
							{
								System.out.println("SemanticRegistryException occured!");
								throw new SemanticRegistryException(
										"An exception occured for unspecified reasons");
							}
						}// if RFP URI already indexed for this service advertisement
						else
						{
							numberOfServicesAlreadyIndexed ++;
							System.out.println("The advertisement of service " + serviceUUID + " was not updated (RFP already exists in its index)");
						}
					}// end if serviceDetail != null
				}// end for every affected service
			} // end else if matching service advertisements were successfully returned
				
			
			if (updatedAdvertisementUUIDs.size() == 0)
			{
				System.out.println("Semantic matchmaking did not yield any new service advertisements matching this request.");
				
				throw new SemanticRegistryNoMatchFoundException(
						"Semantic matchmaking did not yield any new service advertisements matching this request (" +
						"the specified RFP URI is currently included in the index of " + numberOfServicesAlreadyIndexed + 
						" service advertisements).");
			}
			
			// A list of tModelKeys to be returned
			String[] returnedParams = new String[updatedAdvertisementUUIDs.size()];
			
			String tmp = null;
			for (int i = 0; i < updatedAdvertisementUUIDs.size(); i++)
			{
				tmp = (String) updatedAdvertisementUUIDs.elementAt(i);
				returnedParams[i] = new String((tmp));
			}
			
			// Return a list of the deployed tModel names and UUIDs
			return returnedParams;
			
			
		} // endif input is well-formed
		else
		{
			System.out.println("SemanticRegistryMalformedInputException occured!");
			if (!InputValidator.isTextParameterWellFormed(requestFunctionalProfileURI)) 
				throw new SemanticRegistryMalformedInputException("Input parameter value 'profileURI' is malformed");
			throw new SemanticRegistryMalformedInputException("One or more input parameter values are malformed");
		}
	}
	
	
	
	
	
	
	
	/**
	 * @param authToken
	 * @return
	 * @throws SemanticRegistryMalformedInputException
	 * @throws SemanticRegistryCommunicationException
	 * @throws SemanticRegistryNoMatchFoundException
	 * @throws SemanticRegistryException
	 * @throws SemanticRegistryMatchmakingException
	 * @throws SemanticRegistryAuthException
	 * @throws SemanticRegistryConfigurationException 
	 */
	public String[] refreshIndex(String authToken)
			
	throws SemanticRegistryMalformedInputException,
			SemanticRegistryCommunicationException, 
			SemanticRegistryNoMatchFoundException, 
			SemanticRegistryException, 
			SemanticRegistryMatchmakingException, 
			SemanticRegistryAuthException, SemanticRegistryConfigurationException
	{
		// Remove leading and trailing whitespaces 
		if (authToken != null)
			authToken = authToken.trim();
			
		if (InputValidator.isAuthenticationTokenWellFormed(authToken))	
		{
			
			// //////////////////////
			// LOAD THE SEMANTIC REGISTRY KB ONTOLOGY
			// //////////////////////
			
			// Load the SRKB ontology
			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

			// Load the SRKB ontology from its physical URI (OWL file)
			OWLOntology srkbOntology = null;
			try
			{
				srkbOntology = manager.loadOntologyFromPhysicalURI(srkbOntologyPhysicalURI);
				System.out.println("SRKB ontology loaded from " + srkbOntologyPhysicalURI);
			}
			catch (OWLOntologyCreationException e1)
			{
				System.out.println("OWLOntologyCreationException: " + e1.getCause());
				throw new SemanticRegistryMatchmakingException("An error occured while trying to load the SRKB Ontology from its physical URI (" + srkbOntologyPhysicalURI + ")");
			}

			// Get the ontology's data factory to create the various objects
			OWLDataFactory factory = manager.getOWLDataFactory();

			// Get the SemanticRegistryKB ontology's logical URI
			URI srkbLogicalURI = srkbOntology.getURI();
			
			// Obtain and load the imports closure of the semRegKBontology
	        Set<OWLOntology> importsClosure = manager.getImportsClosure(srkbOntology);
	        
			System.out.println("Ontologies contained in the SRKB ontology's import closure:");
			for (OWLOntology ont : importsClosure)
			{
				System.out.println(ont.getURI());
			}
	        
			URI functionalFacetOntologyLogicalURI = getDefiningOntology("#FunctionalFacetEntity", importsClosure, factory).getURI();
			OWLOntology rfpOntology = getDefiningOntology(functionalFacetOntologyLogicalURI+"#RequestFunctionalProfile", importsClosure, factory);

			// Create a reference to the Requested Functional Profile root class
			OWLClass requestFunctionalProfileRootClass = factory.getOWLClass(URI.create(functionalFacetOntologyLogicalURI + "#RequestFunctionalProfile"));
			
			// Get a set of all the RFPs asserted in the ontology 
			Set<OWLDescription> allRFPs = requestFunctionalProfileRootClass.getSubClasses(rfpOntology);
			
			String rfpURI = null;
	   	    String [] affectedServices = null;
	   	    
			// Array for storing the UUIDs of the updated services
			Vector<String> updatedAdvertisementUUIDs = new Vector<String>();
	   	    
	   	    // Iterate over all references to RFP classes
			for (OWLDescription rfp : allRFPs)
			{
				// Call the reclassifyAdvertisements() method
				try
				{
					affectedServices = addRFPToIndex(authToken, rfp.asOWLClass().getURI().normalize().toString());
				}
				catch (SemanticRegistryNoMatchFoundException e)
				{
					// 
				}
				
				// Add the URI of every updated advertisement to the returned list
				if (affectedServices != null)
				{
					for (int i = 0; i < affectedServices.length; i++)
					{
						updatedAdvertisementUUIDs.add(affectedServices[i]);									
					}
				}
			}
			
			if (updatedAdvertisementUUIDs.size() == 0)
			{
				System.out.println("The re-classification procedure did not yield any new matches among RFPs and AFPs.");
				
				throw new SemanticRegistryNoMatchFoundException(
						"The re-classification procedure did not yield any new matches among RFPs and AFPs.");
			}
			
			// A list of tModelKeys to be returned
			String[] returnedParams = new String[updatedAdvertisementUUIDs.size()];
			
			String tmp = null;
			for (int i = 0; i < updatedAdvertisementUUIDs.size(); i++)
			{
				tmp = (String) updatedAdvertisementUUIDs.elementAt(i);
				returnedParams[i] = new String((tmp));
			}
			
			// Return a list of the deployed tModel names and UUIDs
			return returnedParams;
			
			
		} // endif input is well-formed
		else
		{
			System.out.println("SemanticRegistryMalformedInputException occured!");
			if (!InputValidator.isUUIDKeyWellFormed(authToken)) 
				throw new SemanticRegistryMalformedInputException("Input parameter value 'authToken' is malformed");
			throw new SemanticRegistryMalformedInputException("One or more input parameter values are malformed");
		}
	}
	
	
	
	
	
	
		
	/**
	 * @param targetClassName
	 * @param importsClosure
	 * @param factory
	 * @return
	 * @throws SemanticRegistryConfigurationException
	 */
	private static OWLOntology getDefiningOntology(
			String targetClassName, 
			Set<OWLOntology> importsClosure, 
			OWLDataFactory factory) 
	
	throws SemanticRegistryConfigurationException
	{
		// Determine which of the ontologies imported by the SRKB 
		// ontology (directly or indirectly) is the ontology in which 
		// the targetClassName is defined, and retrieve its logical URI
		System.out.println("Scanning for " + targetClassName + " in the import closure of the SRKB ontology:");
		
		OWLClass tmpClass = null; 
		OWLOntology returnedOntology = null;
		
		// Create a map to store search result tuples (ontology logical URI, number of subclasses detected)
		Map<OWLOntology, Integer> ontologySearchResults = new HashMap<OWLOntology, Integer>();
		
		URI targetClassURI = URI.create(targetClassName);
		
		// Iterate over every ontology in the imports closure and check  
		// if it contains any asserted subclasses of targetClassName
		for (OWLOntology ont : importsClosure)
		{
			if (targetClassURI.isAbsolute())
			{
				tmpClass = factory.getOWLClass(targetClassURI);
			}
			else
			{
				tmpClass = factory.getOWLClass(URI.create(ont.getURI() + targetClassName));
			}
			
			//System.out.println("Looking for subclasses of " + tmpClass.getURI() + " in " + ont.getURI());
			Set<OWLDescription> foundSubClasses = tmpClass.getSubClasses(ont);
			
			// The containsEntityDeclaration method offers the best technique for 
			// the specific purpose but cannot be used as it is still not implemented 
			// in the latest binary release of the OWL-API (v2.1.1 - 16th August 2007)
			//if (ont.containsEntityDeclaration(target)) 
			//System.out.println("A declaration for concept " + tmpClass.getURI() + " was found in " + ont.getURI());
			
			// Since a concept with the specified name (targetClassName) may be defined in more 
			// than one ontologies in the closure, judging which is the correct ontology that we 
			// are after can only be based on a heuristic (the number of concepts in the ontology 
			// that are defined as asserted subclasses of the target class - the more the better)
			
			// If at least one asserted subclass of the target class has been found
			if (foundSubClasses.size() > 0)
			{				
				// Add it to the search results map
				ontologySearchResults.put(ont, foundSubClasses.size());
			}
		}
		
		// If none of the ontologies in the import closure of the SRKB contains a 
		// subclass of the target class, there is a configuration problem
		if (ontologySearchResults.size() == 0)
		{
			throw new SemanticRegistryConfigurationException(
					"Configuration problem detected: None of the ontologies in the import closure " +
					"of the SRKB ontology contains subclasses of the " + targetClassName + " concept. " +
					"Make sure that the SRKB ontology imports (directly or indirectly) some ontology " +
					"in which a " + targetClassName + " concept as well as several subclass concepts are defined.");
		}
		
		// Standard case: only one ontology from the SRKB's imports closure 
		// contains asserted subclasses of the target concept 
		if (ontologySearchResults.size() == 1)
		{
			returnedOntology = ontologySearchResults.keySet().iterator().next();
			//int subclasses = ontologySearchResults.get(targetOntologyLogicalURI);
			//System.out.println("FOUND " + subclasses + " subclasses of #TaxonomyEntity in " + taxonomyOntologyLogicalURI);
		}
		
		// Exceptional case: more than one ontology from the SRKB's imports closure 
		// contain asserted subclasses of the target class. Therefore Judging which 
		// is the correct ontology can only be based on a heuristic (we choose the number of 
		// is the correct asserted subclasses that were detected - the more being the better) 
		if (ontologySearchResults.size() > 1)
		{
			int maxSubClassesFound = 0 ;
			OWLOntology ontContainingMaxSubClasses = null;
		    
			// Get the keys (URIs) and values (number of subclasses) for every entry in the map
		    for (Iterator it = ontologySearchResults.entrySet().iterator(); it.hasNext(); ) 
		    {
		        Map.Entry entry = (Map.Entry)it.next();
		        OWLOntology onto = (OWLOntology) entry.getKey();
		        Integer numOfSubclasses = (Integer) entry.getValue();
		        
		        if (numOfSubclasses.intValue() > maxSubClassesFound)
		        {
		        	maxSubClassesFound = numOfSubclasses.intValue();
		        	ontContainingMaxSubClasses = onto;
		        }
		    }
		    returnedOntology = ontContainingMaxSubClasses;
		}
		
		System.out.println("Subclasses of the " + tmpClass + " concept are defined in " + returnedOntology.getURI());
		return returnedOntology;
	}
	
	
	
	
	
	
			
	/**
	 * @param tModelKey
	 * @param keyValue
	 * @return
	 * @throws SemanticRegistryException
	 */
	private KeyedReference generateKeyedReference(String tModelKey, String keyValue)
			throws SemanticRegistryException
	{
		KeyedReference kr = new KeyedReference();
		kr.setTModelKey("uuid:" + tModelKey);
		kr.setKeyValue(keyValue);
		String keyName = null;

		if (tModelKey.equalsIgnoreCase(SAWSDL_TMODEL_KEY)) keyName = "SAWSDL_Document_URL_tModel";
		if (tModelKey.equalsIgnoreCase(CATEGORY_TMODEL_KEY)) keyName = "Category_Annotation_tModel";
		if (tModelKey.equalsIgnoreCase(INPUT_TMODEL_KEY)) keyName = "Input_Annotation_tModel";
		if (tModelKey.equalsIgnoreCase(OUTPUT_TMODEL_KEY)) keyName = "Output_Annotation_tModel";
		if (tModelKey.equalsIgnoreCase(INDEXING_TMODEL_KEY)) keyName = "Subsumption_Indexing_tModel";

		kr.setKeyName(keyName);
		return kr;
	}
	
	
	
	
	
	
	
	/**
	 * @param authToken
	 * @param requestFunctionalProfileURI
	 * @return
	 * @throws SemanticRegistryException
	 * @throws SemanticRegistryCommunicationException
	 * @throws SemanticRegistryAuthException
	 * @throws SemanticRegistryNoMatchFoundException
	 * @throws SemanticRegistryMalformedInputException
	 * @throws AxisFault 
	 */
	public String[] removeRFPFromIndex(
			String authToken, 
			String requestFunctionalProfileURI) 
	
	throws SemanticRegistryException, 
	SemanticRegistryCommunicationException, 
	SemanticRegistryAuthException, 
	SemanticRegistryNoMatchFoundException, 
	SemanticRegistryMalformedInputException, AxisFault
	{
		// Remove leading and trailing whitespaces
		if (authToken != null) authToken = authToken.trim();

		if ( InputValidator.isAuthenticationTokenWellFormed(authToken) && 
				InputValidator.isTextParameterWellFormed(requestFunctionalProfileURI) )
		{
			// Array for storing the UUIDs of the updated services
			Vector<String> updatedAdvertisementUUIDs = new Vector<String>();
			
			// Create a discovery manager to get all service UUIDs
			DiscoveryManagerSkeleton discoveryManager = new DiscoveryManagerSkeleton();
			GetAllServiceUUIDsResponse response = null;
			GetAllServiceUUIDsResponse_type0 response2 = null;
			try
			{
				response = discoveryManager.getAllServiceUUIDs();
			}
			catch (AxisFault e)
			{
				System.out.println("Problem retrieving the UUIDs of published services");
				throw e;
			}
			
			// Get all service UUIDs in an array
			response2 = response.getGetAllServiceUUIDsResponse();
			String[] uuids = response2.getString();
			
			
			// For every service...
			for (int i = 0; i < uuids.length; i++)
			{
				String serviceUUID = uuids[i];
				ServiceDetail serviceDetail = null;

				// Try to get the service detail from UDDI using the service key
				try
				{
					serviceDetail = proxy.get_serviceDetail(serviceUUID);
				}
				catch (UDDIException e)
				{
					DispositionReport dr = e.getDispositionReport();
					if (dr != null)
					{
						Vector results = dr.getResultVector();
						for (int k = 0; k < results.size(); k++)
						{
							Result r = (Result) results.elementAt(k);
							if (r.getErrInfo().getErrCode().equals("E_invalidKeyPassed")) 
								System.out.println("The advertisement of service " + serviceUUID + " was not updated (no such UUID)");
						}
					}
				}
				catch (TransportException e1)
				{
					System.out.println("TransportException occured!");
					throw new SemanticRegistryCommunicationException("Problem communicating with the UDDI server");
				}

				if (serviceDetail != null)
				{				
					// Get the first business entity from the returned vector
					Vector<BusinessService> businessServiceVector = serviceDetail.getBusinessServiceVector();

					// Get the first and only BusinessService element from the vector
					BusinessService businessService = (BusinessService) businessServiceVector.get(0);

					// Get the categoryBag from the BusinessService
					CategoryBag oldCategoryBag = businessService.getCategoryBag();

					// Get the old keyedReferences vector
					Vector<KeyedReference> oldKeyedRefVector = oldCategoryBag.getKeyedReferenceVector();
					
					// Check if the old keyed reference vector contains a reference to this RFP
					if (oldKeyedRefVector.contains(generateKeyedReference(INDEXING_TMODEL_KEY, requestFunctionalProfileURI)))
					{
						// Copy the keyedReferences vector from the old
						// category bag into a new vector
						Vector<KeyedReference> newKeyedRefVector = oldKeyedRefVector;		
						
						// Remove the keyedReference from the new vector 
						newKeyedRefVector.remove(generateKeyedReference(INDEXING_TMODEL_KEY, requestFunctionalProfileURI));

						// Copy the old category bag into a new one
						CategoryBag newCategoryBag = oldCategoryBag;

						// Replace the old keyedReferences vector with the
						// new (enriched) one
						newCategoryBag.setKeyedReferenceVector(newKeyedRefVector);

						// Replace the old categoryBag with the new one (the
						// rest of the attributes in the businessService object 
						// should remain unchanged)
						businessService.setCategoryBag(newCategoryBag);

						// Empty the businessServiceVector (i.e. remove the
						// old businessService object)
						businessServiceVector.clear();

						// Add the old businessService again, which now
						// contains the new categoryBag
						businessServiceVector.addElement(businessService);

						// Try to save the service
						try
						{
							serviceDetail = proxy.save_service(authToken, businessServiceVector);
							System.out.println("The advertisement of service " + serviceUUID + " was updated successfully");
							updatedAdvertisementUUIDs.add(serviceUUID);
						}
						catch (UDDIException e)
						{
							DispositionReport dr = e.getDispositionReport();
							if (dr != null)
							{
								Vector results = dr.getResultVector();
								for (int k = 0; k < results.size(); k++)
								{
									Result r = (Result) results.elementAt(k);
									System.out.println("The UDDI server raised an exception with error number: "+ r.getErrno());
									if (r.getErrInfo().getErrCode().equals("E_authTokenRequired"))
									{
										System.out.println("E_authTokenRequired: (10120) The authentication token is invalid");
										throw new SemanticRegistryAuthException(
												"The authentication token is invalid");
									}
									if (r.getErrInfo().getErrCode().equals("E_authTokenExpired"))
									{
										System.out.println("E_invalidKeyPassed: (10110) The authentication token has timed out");
										throw new SemanticRegistryAuthException(
												"The authentication token has timed out");
									}
									if (r.getErrInfo().getErrCode().equals("E_invalidKeyPassed"))
									{
										System.out.println("E_invalidKeyPassed: (10210) The uuid_key did not match any known key values");
										throw new SemanticRegistryException(
												"One or more UUID keys do not match any known values");
									}
									throw new SemanticRegistryException("UDDI exception with error number: " + r.getErrno());
								}
							}
							else
							{
								System.out
										.println("UDDI exception without Disposition Report -> SemanticRegistryCommunicationException");
								throw new SemanticRegistryCommunicationException(
										"Problem communicating with the UDDI server");
							}
						}
						catch (TransportException e)
						{
							System.out.println("TransportException occured!");
							throw new SemanticRegistryCommunicationException(
									"Problem communicating with the UDDI server");
						}
						catch (Exception e)
						{
							System.out.println("SemanticRegistryException occured!");
							throw new SemanticRegistryException(
									"An exception occured for unspecified reasons");
						}
					}// if RFP URI already indexed for this service advertisement
				}// end if serviceDetail != null
			}
						
			if (updatedAdvertisementUUIDs.size() == 0)
			{
				System.out.println("There where no necessary changes to the index. " +
						"The specified RFP URI was not included as a match in any of the service advertisements.");
				throw new SemanticRegistryNoMatchFoundException("There where no necessary changes to the index. " +
						"The specified RFP URI was not included as a match in any of the service advertisements.");
			}

			// A list of tModelKeys to be returned
			String[] returnedParams = new String[updatedAdvertisementUUIDs.size()];

			for (int i = 0; i < updatedAdvertisementUUIDs.size(); i++)
			{
				returnedParams[i] = (String) updatedAdvertisementUUIDs.elementAt(i);
			}

			// Return a list of the deployed tModel names and UUIDs
			return returnedParams;

		} // endif input is well-formed
		else
		{
			System.out.println("SemanticRegistryMalformedInputException occured!");
			if (!InputValidator.isUUIDKeyWellFormed(authToken)) 
				throw new SemanticRegistryMalformedInputException("Input parameter value 'authToken' is malformed");
			if (!InputValidator.isTextParameterWellFormed(requestFunctionalProfileURI)) 
				throw new SemanticRegistryMalformedInputException("Input parameter value 'requestFunctionalProfileURI' is malformed");
			throw new SemanticRegistryMalformedInputException( "One or more input parameter values are malformed");
		}
	}
}
