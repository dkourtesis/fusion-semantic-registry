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

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import org.seerc.fusion.sr.api.xsd.GetServiceDetailsResponse;
import org.seerc.fusion.sr.api.xsd.GetServiceProviderDetailsResponse;
import org.seerc.fusion.sr.api.xsd.URIListType;
import org.seerc.fusion.sr.api.xsd.UUIDListType;
import org.seerc.fusion.sr.exceptions.SemanticRegistryMatchmakingException;
import org.seerc.fusion.sr.exceptions.SemanticRegistryCommunicationException;
import org.seerc.fusion.sr.exceptions.SemanticRegistryConfigurationException;
import org.seerc.fusion.sr.exceptions.SemanticRegistryException;
import org.seerc.fusion.sr.exceptions.SemanticRegistryMalformedInputException;
import org.seerc.fusion.sr.exceptions.SemanticRegistryNoMatchFoundException;
import org.seerc.fusion.sr.utils.FileUtils;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyCreationException;
import org.semanticweb.owl.model.OWLOntologyManager;

import org.uddi4j.UDDIException;
import org.uddi4j.client.UDDIProxy;
import org.uddi4j.datatype.Name;
import org.uddi4j.datatype.business.BusinessEntity;
import org.uddi4j.datatype.service.BusinessService;
import org.uddi4j.response.BusinessDetail;
import org.uddi4j.response.BusinessInfo;
import org.uddi4j.response.BusinessList;
import org.uddi4j.response.DispositionReport;
import org.uddi4j.response.Result;
import org.uddi4j.response.ServiceDetail;
import org.uddi4j.response.ServiceInfo;
import org.uddi4j.response.ServiceList;
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
public class DiscoveryHandler
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
	 * Default constructor
	 * 
	 * @throws SemanticRegistryConfigurationException
	 */
	public DiscoveryHandler() 
	
	throws SemanticRegistryConfigurationException
	{
		// Specify which of the 3 SOAP transports that UDDI4j supports should
		// be used to talk to jUDDI (Apache Axis, Apache SOAP 2.2, HP SOAP)
		System.setProperty("org.uddi4j.TransportClassName", "org.uddi4j.transport.ApacheAxisTransport");

		// Initialise the UDDIProxy object
		proxy = new UDDIProxy();
		
		// Create a new properties object to store the settings loaded 
		// from the registry.properties file in WEB-INF/classes
		Properties properties = new Properties();

		try
		{
			// This will look in the current classloader's context and find the
			// registry.properties files that is placed under /WEB-INF/classes/
			// (Any *.properties file that is placed inside the "src" folder 
			// of an Eclipse project will get deployed to WEB-INF/classes automatically)
			InputStream inputStream = getClass().getClassLoader().getResourceAsStream("registry.properties");
			
			if (inputStream == null)
			{
				System.out.println("The registry.properties file could not be located");
				throw new SemanticRegistryConfigurationException("The classloader could not locate and load the registry.properties file");				
			}
			else properties.load(inputStream);
			
			// Get the URL values for the UDDI Inquiry and Publish API
			String inquiryURL = properties.getProperty("inquiry_url");
			String publishURL = properties.getProperty("publish_url");

			// Check the validity of URIs loaded from the registry.properties file
			if (!InputValidator.isURIWellFormed(publishURL))
				throw new SemanticRegistryConfigurationException("publish_url in registry.properties file is not a valid URI");
			if (!InputValidator.isURIWellFormed(inquiryURL))
				throw new SemanticRegistryConfigurationException("inquiry_url in registry.properties file is not a valid URI");				

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

			// Check the validity of URIs loaded from the registry.properties file
			// To be valid they must point to a document that can be retrieved via 
			// http or file protocol (no other protocol is supported)
			if (!InputValidator.isURIWellformedAndResolvable(properties.getProperty("srkb_ontology_physical_URI")))
				throw new SemanticRegistryConfigurationException("srkb_ontology_physical_URI in registry.properties file is not a valid URI");
			if (!InputValidator.isURIWellformedAndResolvable(properties.getProperty("eai_ontology_physical_URI")))
				throw new SemanticRegistryConfigurationException("eai_ontology_physical_URI in registry.properties file is not a valid URI");


			// Set the locations (physical URIs) for the ontologies needed
			srkbOntologyPhysicalURI = URI.create(properties.getProperty("srkb_ontology_physical_URI"));
			eaiOntologyPhysicalURI = URI.create(properties.getProperty("eai_ontology_physical_URI"));
		}
		catch (IOException e)
		{
			System.out.println("SemanticRegistryConfigurationException occured!");
			throw new SemanticRegistryConfigurationException("Problem loading the registry.properties file");
		}
	}
	
	
	
	
	
	
	
	/**
	 * Returns a list with the UUIDs of all Service Provider records existing in
	 * the UDDI server.
	 * 
	 * @return
	 * @throws SemanticRegistryMalformedInputException
	 * @throws SemanticRegistryCommunicationException
	 * @throws SemanticRegistryNoMatchFoundException
	 * @throws SemanticRegistryException
	 */
	public String[] getAllServiceProviderUUIDs() 
	
	throws SemanticRegistryMalformedInputException,
			SemanticRegistryCommunicationException, 
			SemanticRegistryNoMatchFoundException,
			SemanticRegistryException

	{
		String[] response = null;
		try
		{
			response = doKeywordSearchForServiceProviders("%");
		}
		catch (SemanticRegistryNoMatchFoundException e)
		{
			throw new SemanticRegistryNoMatchFoundException(
					"No service provider record was found in the registry");
		}
		return response;
	}
	
	
	
	
	
	
	
	/**
	 * Returns a list with the UUIDs of all Service Provider records existing in
	 * the UDDI server, having a name that contains the specified keyword.
	 * 
	 * @param keyword
	 * @return
	 * @throws SemanticRegistryMalformedInputException
	 * @throws SemanticRegistryCommunicationException
	 * @throws SemanticRegistryNoMatchFoundException
	 * @throws SemanticRegistryException
	 */
	public String[] doKeywordSearchForServiceProviders(String keyword)
	
	throws SemanticRegistryMalformedInputException, 
		SemanticRegistryCommunicationException,
		SemanticRegistryNoMatchFoundException, 
		SemanticRegistryException
	{
		
		if (InputValidator.isTextParameterWellFormed(keyword))
		{
			String[] serviceProviderUUIDs = null;
			
			try
			{
				// Define a search qualifier as per: 
				// http://www.uddi.org/pubs/ProgrammersAPI-V2.04-Published-20020719.htm
				FindQualifiers findQualifiers = new FindQualifiers(); 
				FindQualifier findQualifier = new FindQualifier("sortByNameAsc"); 
				findQualifiers.add(findQualifier); 
				
				// Invoke search with "%" in the beginning of the search text
				BusinessList searchResults = proxy.find_business('%'+keyword, findQualifiers, 0);
				
				Vector results = searchResults.getBusinessInfos().getBusinessInfoVector();

				if (results != null)
				{
					int vectorSize = results.size();
					if (vectorSize > 0)
					{
						// Initialise the list of UUIDs to the vector size
						serviceProviderUUIDs = new String[(vectorSize)];
						
						// Extract the UUID key from every BusinessInfo object
						// in the vector and copy it to the list of service 
						// provider UUIDs to be returned
						for (int i = 0; i < vectorSize; i++)
						{
							BusinessInfo info = (BusinessInfo) results.elementAt(i);
							serviceProviderUUIDs[i] = info.getBusinessKey();
						}
					}
				}
				else
				// i.e. if nothing was found
				{
					throw new SemanticRegistryNoMatchFoundException(
							"The given keyword (" + keyword + ") does not match any known service provider name");
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
						throw new SemanticRegistryException("UDDI exception with error number: "
								+ r.getErrno());
					}
				}
				else
				{
					System.out.println(
							"The UDDI server reported an internal error but did not provide a Disposition Report " +
							"to explain its cause. The problem resulted while trying to invoke the 'find_business' " +
							"operation, and the cause of the problem may be in the data provided (e.g. some parameter " +
							"value exceeding max character length), in a failure to communicate with the UDDI server, " +
							"or in a failure to communicate with the database that the UDDI server relies on.");
					throw new SemanticRegistryCommunicationException(
							"The UDDI server reported an internal error but did not provide a Disposition Report " +
							"to explain its cause. The problem resulted while trying to invoke the 'find_business' " +
							"operation, and the cause of the problem may be in the data provided (e.g. some parameter " +
							"value exceeding max character length), in a failure to communicate with the UDDI server, " +
							"or in a failure to communicate with the database that the UDDI server relies on.");
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
				throw new SemanticRegistryException("An exception occured for unspecified reasons");
			}

			// if results were null or empty
			if (serviceProviderUUIDs == null || serviceProviderUUIDs.length == 0) throw new SemanticRegistryNoMatchFoundException(
					"The given keyword (" + keyword + ") does not match any known service provider name");

			// If all goes well
			return serviceProviderUUIDs;

		} // endif input is well-formed
		else
		{
			System.out.println("SemanticRegistryMalformedInputException occured!");
			throw new SemanticRegistryMalformedInputException(
					"Input parameter value 'keyword' is malformed");
		}
	}
	
	
	
	
	
	
	
	/**
	 * Retrieves a Service Provider record from the UDDI server matching the
	 * specified UUID key, and returns all available information (name, free
	 * text description, offered services).
	 * 
	 * @param serviceProviderUUID
	 * @return
	 * @throws SemanticRegistryMalformedInputException
	 * @throws SemanticRegistryCommunicationException
	 * @throws SemanticRegistryNoMatchFoundException
	 * @throws SemanticRegistryException
	 */
	public GetServiceProviderDetailsResponse getServiceProviderDetails(String serviceProviderUUID)
	
	throws SemanticRegistryMalformedInputException, 
		SemanticRegistryCommunicationException,
		SemanticRegistryNoMatchFoundException, 
		SemanticRegistryException
	{
		// Remove leading and trailing whitespaces from values to be used as keys
		serviceProviderUUID = serviceProviderUUID.trim();

		if (InputValidator.isUUIDKeyWellFormed(serviceProviderUUID))
		{
			// Create a response message object
			GetServiceProviderDetailsResponse response = new GetServiceProviderDetailsResponse();

			BusinessDetail businessDetail = null;
			try
			{
				// Get the business detail from the registry based on the key
				businessDetail = proxy.get_businessDetail(serviceProviderUUID);

				// Get the BusinessEntity vector from the returned result 
				Vector businessEntityVector = businessDetail.getBusinessEntityVector();

				// If no match was found for that key...
				if (businessEntityVector == null || businessEntityVector.size() == 0) 
					throw new SemanticRegistryNoMatchFoundException(
						"No matching service provider was found for key: " + serviceProviderUUID);

				// Get the first and only BusinessEntity element from the vector
				BusinessEntity provider = (BusinessEntity) businessEntityVector.get(0);

				// Get a vector of all the services that the BusinessEntity provides
				Vector providedServices = provider.getBusinessServices().getBusinessServiceVector();

				// Create an array of strings to hold the UUIDs of 
				// the services that the Service Provider provides
				String[] itemList = new String[providedServices.size()];

				// Extract the UUID of each BusinessService entity
				for (int i = 0; i < providedServices.size(); i++)
				{
					BusinessService service = (BusinessService) providedServices.get(i);
					itemList[i] = service.getServiceKey();
				}

				// Create an object to hold the list of strings
				UUIDListType serviceUUIDs = new UUIDListType();
				serviceUUIDs.setString(itemList);

				// Add the list-of-UUIDs object to the response message
				response.setListOfProvidedServiceUUIDs(serviceUUIDs);

				if (provider.getDefaultNameString() == null)
				{
					// Add an empty service provider name to the response
					// message
					response.setServiceProviderName("");
				}
				else
				{
					// Add the service provider name to the response message
					response.setServiceProviderName(provider.getDefaultNameString());
				}

				if (provider.getDefaultDescriptionString() == null)
				{
					// Add an empty service provider description to the response message
					response.setServiceProviderFreeTextDescription("");
				}
				else
				{
					// Add the service provider description to the response message
					response.setServiceProviderFreeTextDescription(provider.getDefaultDescriptionString());
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
							throw new SemanticRegistryNoMatchFoundException(
									"The serviceProviderUUID key (" + serviceProviderUUID
											+ ") does not match any known values");
						}
						throw new SemanticRegistryException("UDDI exception with error number: " + r.getErrno());
					}
				}
				else
				{
					System.out.println(
							"The UDDI server reported an internal error but did not provide a Disposition Report " +
							"to explain its cause. The problem resulted while trying to invoke the 'get_businessDetail' " +
							"operation, and the cause of the problem may be in the data provided (e.g. some parameter " +
							"value exceeding max character length), in a failure to communicate with the UDDI server, " +
							"or in a failure to communicate with the database that the UDDI server relies on.");
					throw new SemanticRegistryCommunicationException(
							"The UDDI server reported an internal error but did not provide a Disposition Report " +
							"to explain its cause. The problem resulted while trying to invoke the 'get_businessDetail' " +
							"operation, and the cause of the problem may be in the data provided (e.g. some parameter " +
							"value exceeding max character length), in a failure to communicate with the UDDI server, " +
							"or in a failure to communicate with the database that the UDDI server relies on.");
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
				throw new SemanticRegistryException("An exception occured for unspecified reasons");
			}

			// If all goes well
			return response;

		} // endif input is well-formed
		else
		{
			System.out.println("SemanticRegistryMalformedInputException occured!");
			throw new SemanticRegistryMalformedInputException(
					"Input parameter value 'serviceProviderUUID' is malformed");
		}
	}
	
	
	
	
	
	
	
	/**
	 * Returns a list with the UUIDs of all Service records existing in the UDDI
	 * server. 
	 * 
	 * @return
	 * @throws SemanticRegistryMalformedInputException
	 * @throws SemanticRegistryCommunicationException
	 * @throws SemanticRegistryNoMatchFoundException
	 * @throws SemanticRegistryException
	 */
	public String[] getAllServiceUUIDs() 
	
	throws SemanticRegistryMalformedInputException,
			SemanticRegistryCommunicationException, 
			SemanticRegistryNoMatchFoundException,
			SemanticRegistryException
	{
		String[] response = null;
		try
		{
			response = doKeywordSearchForServices("%");
		}
		catch (SemanticRegistryNoMatchFoundException e)
		{
			throw new SemanticRegistryNoMatchFoundException(
					"No service advertisement record was found in the registry");
		}
		return response;
	}
	
	
	
	
	
	
	
	/**
	 * Returns a list with the UUIDs of all Service records existing in the UDDI
	 * server, having a name that contains the specified keyword.
	 * 
	 * @param keyword
	 * @return
	 * @throws SemanticRegistryMalformedInputException
	 * @throws SemanticRegistryCommunicationException
	 * @throws SemanticRegistryNoMatchFoundException
	 * @throws SemanticRegistryException
	 */
	public String[] doKeywordSearchForServices(String keyword)
	
	throws SemanticRegistryMalformedInputException, 
		SemanticRegistryCommunicationException,
		SemanticRegistryNoMatchFoundException, 
		SemanticRegistryException
	{
		if (InputValidator.isTextParameterWellFormed(keyword))
		{
			String[] serviceUUIDs = null;
			
			try
			{
				// Define a search qualifier as per: 
				// http://www.uddi.org/pubs/ProgrammersAPI-V2.04-Published-20020719.htm
				FindQualifiers findQualifiers = new FindQualifiers(); 
				FindQualifier findQualifier = new FindQualifier("sortByNameAsc"); 
				findQualifiers.add(findQualifier); 
				
				// Create a vector to store the keyword(s), and 
				// add "%" in the beginning of the search text
				Vector nameVector = new Vector(); 
				nameVector.add(new Name('%'+keyword)); 
				
				// Invoke search 
				ServiceList searchResults = proxy.find_service(null, nameVector, null, null, findQualifiers, 0);
				
				//ServiceList searchResults = proxy.find_service(null, '%'+keyword, null, 10000);

				Vector results = searchResults.getServiceInfos().getServiceInfoVector();

				if (results != null)
				{
					int vectorSize = results.size();
					if (vectorSize > 0)
					{
						// Initialise the list of UUIDs to the vector size
						serviceUUIDs = new String[(vectorSize)];

						// Extract the UUID key from every ServiceInfo object in
						// the vector and copy it to the list of service UUIDs
						// to be returned
						for (int i = 0; i < vectorSize; i++)
						{
							ServiceInfo info = (ServiceInfo) results.elementAt(i);
							serviceUUIDs[i] = info.getServiceKey();
						}
					}
				}
				else
				// i.e. if nothing was found
				{
					throw new SemanticRegistryNoMatchFoundException(
							"The given keyword (" + keyword + ") does not match any known service name");
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
						throw new SemanticRegistryException("UDDI exception with error number: "
								+ r.getErrno());
					}
				}
				else
				{
					System.out.println(
							"The UDDI server reported an internal error but did not provide a Disposition Report " +
							"to explain its cause. The problem resulted while trying to invoke the 'find_service' " +
							"operation, and the cause of the problem may be in the data provided (e.g. some parameter " +
							"value exceeding max character length), in a failure to communicate with the UDDI server, " +
							"or in a failure to communicate with the database that the UDDI server relies on.");
					throw new SemanticRegistryCommunicationException(
							"The UDDI server reported an internal error but did not provide a Disposition Report " +
							"to explain its cause. The problem resulted while trying to invoke the 'find_service' " +
							"operation, and the cause of the problem may be in the data provided (e.g. some parameter " +
							"value exceeding max character length), in a failure to communicate with the UDDI server, " +
							"or in a failure to communicate with the database that the UDDI server relies on.");
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
				throw new SemanticRegistryException("An exception occured for unspecified reasons");
			}

			// if results were null or empty
			if (serviceUUIDs == null || serviceUUIDs.length == 0) 
				throw new SemanticRegistryNoMatchFoundException(
					"The given keyword (" + keyword + ") does not match any known service provider name");

			// If all goes well
			return serviceUUIDs;

		} // endif input is well-formed
		else
		{
			System.out.println("SemanticRegistryMalformedInputException occured!");
			throw new SemanticRegistryMalformedInputException(
					"Input parameter value 'keyword' is malformed");
		}
	}
	
	
	
	
	
	
	
	/**
	 * Retrieves a Service record from the UDDI server matching the specified
	 * UUID key, and returns all of the information available for this service:
	 * name, free text description, UUID of service provider, URL of the
	 * service's SAWSDL document, URIs of the category, input and output
	 * annotations, and URIs of the RFPs that the service is known to match
	 * with.
	 * 
	 * @param serviceUUID
	 * @return
	 * @throws SemanticRegistryMalformedInputException
	 * @throws SemanticRegistryCommunicationException
	 * @throws SemanticRegistryNoMatchFoundException
	 * @throws SemanticRegistryException
	 */
	public GetServiceDetailsResponse getServiceDetails(String serviceUUID)
	
	throws SemanticRegistryMalformedInputException, 
		SemanticRegistryCommunicationException,
		SemanticRegistryNoMatchFoundException, 
		SemanticRegistryException
	{
		// Remove leading and trailing whitespaces from values to be used as
		// keys
		serviceUUID = serviceUUID.trim();
		
		if (InputValidator.isUUIDKeyWellFormed(serviceUUID))
		{
			// Create a response message object
			GetServiceDetailsResponse response = new GetServiceDetailsResponse();

			ServiceDetail serviceDetail = null;
			try
			{
				// Get the service detail from the registry based on the key
				serviceDetail = proxy.get_serviceDetail(serviceUUID);
				
				// Get the first business entity from the returned vector
				Vector businessServices = serviceDetail.getBusinessServiceVector();

				// Get the first and only BusinessService element from the
				// vector
				BusinessService srvInfo = (BusinessService) businessServices.get(0);

				if (srvInfo.getDefaultNameString() == null)
				{
					// Add an empty service name to the response message
					response.setServiceName("");
				}
				else
				{
					// Add the service name to the response message
					response.setServiceName(srvInfo.getDefaultNameString());
				}

				if (srvInfo.getDefaultDescriptionString() == null)
				{
					// Add an empty service free text description to the
					// response message
					response.setServiceFreeTextDescription("");
				}
				else
				{
					// Add the service free text description to the response
					// message
					response.setServiceFreeTextDescription(srvInfo.getDefaultDescriptionString());
				}

				if (srvInfo.getBusinessKey() == null)
				{
					// Add an empty service provider UUID to the response
					// message
					response.setServiceProviderUUID("");
				}
				else
				{
					// Add the service provider UUID to the response message
					response.setServiceProviderUUID(srvInfo.getBusinessKey());
				}

				// Get the categoryBag from the BusinessService
				CategoryBag categoryBag = srvInfo.getCategoryBag();
				
				// Create variables to hold the information to be 
				// returned and initialise to avoid returning null
				String sawsdlURL = "";
				String hasCategoryAnnotationURI = "";
				List<String> inputAnnotationURIs = new ArrayList<String>();
				List<String> outputAnnotationURIs = new ArrayList<String>();
				List<String> matchingRFPURIs = new ArrayList<String>();
				
				// Temp
				String tModelKey = "";
				
				// Extract the information from each KeyedReference
				if (categoryBag != null)
				{
					Vector keyedRefVector = categoryBag.getKeyedReferenceVector();
					if (keyedRefVector != null)
					{
						int vectorSize = keyedRefVector.size();
						if (vectorSize > 0)
						{
							// For every KeyedReference
							for (int i = 0; i < vectorSize; i++)
							{
								KeyedReference keyedRef = (KeyedReference) keyedRefVector.elementAt(i);
								tModelKey = keyedRef.getTModelKey();

								if (tModelKey.equalsIgnoreCase("uuid:" + SAWSDL_TMODEL_KEY))
								{
									// there will be only one keyedReference referring 
									// to the URL of the service's SAWSDL, so this 
									// assignment will be made only once
									sawsdlURL = keyedRef.getKeyValue();
								}
								else if (tModelKey.equalsIgnoreCase("uuid:" + CATEGORY_TMODEL_KEY))
								{
									// there will be only one keyedReference referring
									// to the URI of the service's category annotation, 
									// so this assignment will be made only once
									hasCategoryAnnotationURI = keyedRef.getKeyValue();
								}
								else if (tModelKey.equalsIgnoreCase("uuid:" + INPUT_TMODEL_KEY))
								{
									// there may be zero or multiple input annotation URIs
									inputAnnotationURIs.add(keyedRef.getKeyValue());
								}
								else if (tModelKey.equalsIgnoreCase("uuid:" + OUTPUT_TMODEL_KEY))
								{
									// there may be zero or multiple output annotation URIs
									outputAnnotationURIs.add(keyedRef.getKeyValue());
								}
								else if (tModelKey.equalsIgnoreCase("uuid:" + INDEXING_TMODEL_KEY))
								{
									// there may be zero or multiple Request Functional Profile URIs
									matchingRFPURIs.add(keyedRef.getKeyValue());
								}
							}
						}
					}
				}

				// Add the URL location of the SAWSDL document to the response
				response.setLocationOfSAWSDLDocument(sawsdlURL);
				
				// Add the category annotation URI to the response
				response.setCategoryAnnotationURI(hasCategoryAnnotationURI);
						
				// Convert the list of input annotation URIs to a URIListType 
				// and add it to the response
				URIListType uriList = new URIListType();
				String[] inputs = new String[inputAnnotationURIs.size()];
				int i = 0;
				for (Iterator it = inputAnnotationURIs.iterator(); it.hasNext();)
				{
					inputs[i] = (String) it.next();
					i++;
				}
				uriList.setString(inputs);
				response.setListOfInputAnnotationURIs(uriList);

				// Convert the list of output annotation URIs to a URIListType 
				// and add it to the response
				uriList = new URIListType();
				String[] outputs = new String[outputAnnotationURIs.size()];
				i = 0;
				for (Iterator it = outputAnnotationURIs.iterator(); it.hasNext();)
				{
					outputs[i] = (String) it.next();
					i++;
				}
				uriList.setString(outputs);
				response.setListOfOutputAnnotationURIs(uriList);
				
				// Convert the list of Request Functional Profile URIs to a URIListType 
				// and add it to the response
				uriList = new URIListType();
				String[] matches = new String[matchingRFPURIs.size()];
				i = 0;
				for (Iterator it = matchingRFPURIs.iterator(); it.hasNext();)
				{
					matches[i] = (String) it.next();
					i++;
				}
				uriList.setString(matches);
				response.setListOfMatchingRequestFunctionalProfileURIs(uriList);
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
						System.out
								.println("The UDDI server raised an exception with error number: "
										+ r.getErrno());

						if (r.getErrInfo().getErrCode().equals("E_invalidKeyPassed"))
						{
							System.out
									.println("E_invalidKeyPassed: (10210) The uuid_key did not match any known key values");
							throw new SemanticRegistryNoMatchFoundException("The serviceUUID key ("
									+ serviceUUID + ") does not match any known values");
						}
						throw new SemanticRegistryException("UDDI exception with error number: "
								+ r.getErrno());
					}
				}
				else
				{
					System.out.println(
							"The UDDI server reported an internal error but did not provide a Disposition Report " +
							"to explain its cause. The problem resulted while trying to invoke the 'get_serviceDetail' " +
							"operation, and the cause of the problem may be in the data provided (e.g. some parameter " +
							"value exceeding max character length), in a failure to communicate with the UDDI server, " +
							"or in a failure to communicate with the database that the UDDI server relies on.");
					throw new SemanticRegistryCommunicationException(
							"The UDDI server reported an internal error but did not provide a Disposition Report " +
							"to explain its cause. The problem resulted while trying to invoke the 'get_serviceDetail' " +
							"operation, and the cause of the problem may be in the data provided (e.g. some parameter " +
							"value exceeding max character length), in a failure to communicate with the UDDI server, " +
							"or in a failure to communicate with the database that the UDDI server relies on.");
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
				throw new SemanticRegistryException("An exception occured for unspecified reasons");
			}

			// If all goes well
			return response;

		} // endif input is well-formed
		else
		{
			System.out.println("SemanticRegistryMalformedInputException occured!");
			throw new SemanticRegistryMalformedInputException(
					"Input parameter value 'serviceUUID' is malformed");
		}
	}
	
	
	
	
	
	
	
	/**
	 * Returns a list with the UUIDs of all Service records in the UDDI server
	 * that have been indexed as matching the Request Functional Profile (RFP)
	 * identified by the given URI. 
	 * 
	 * @param requestFunctionalProfileURI
	 * @param serviceProviderUUID
	 * @return
	 * @throws SemanticRegistryMalformedInputException
	 * @throws SemanticRegistryCommunicationException
	 * @throws SemanticRegistryNoMatchFoundException
	 * @throws SemanticRegistryException
	 * @throws SemanticRegistryMatchmakingException
	 * @throws SemanticRegistryConfigurationException 
	 */
	public String[] doSemanticSearchForServices(
		String requestFunctionalProfileURI,
		String serviceProviderUUID)
	
	throws SemanticRegistryMalformedInputException,
		SemanticRegistryCommunicationException, 
		SemanticRegistryNoMatchFoundException, 
		SemanticRegistryException, 
		SemanticRegistryMatchmakingException, SemanticRegistryConfigurationException
	{
		// Remove leading and trailing whitespaces 
		if (serviceProviderUUID != null)
			serviceProviderUUID = serviceProviderUUID.trim();
		if (requestFunctionalProfileURI != null)
			requestFunctionalProfileURI = requestFunctionalProfileURI.trim();
		
		// Assert that the RFP URI is well formed (although not necessarily retrievable)
		if ( InputValidator.isURIWellFormed(requestFunctionalProfileURI) )
		{
			// We need to make sure that the UUID is either a valid businessKey, or null
			if (serviceProviderUUID != null) 
			{
				// ...and it is also non-empty...
				if (serviceProviderUUID.length() > 1)
				{
					// ...but it contains some ill-formed value
					if (!InputValidator.isCanonicalUUID(serviceProviderUUID))
					{
						// ...stop execution
						throw new SemanticRegistryMalformedInputException("Input parameter value 'serviceProviderUUID' is malformed");
					}
				}
				else // i.e. if it is not null but actually empty 
				{
					// nullify it
					serviceProviderUUID = null;
				}
			}
			
			boolean isValid = false;
			
			// Check if the provided RFP URI is amenable to processing: 
			// 1) it is a valid  URI, 
			// 2) it contains a fragment component, 
			// 3) the associated ontology file can be retrieved, and 
			// 4) the fragment identifier identifies an ontological 
			// concept that exists within the ontology file (in an 
			// rdf:about="#X" rdf:about="uri#X" or rdf:ID="X" element)
			try
			{
				isValid = InputValidator.isValidURIWithFragmentDefinedInRetrievableDocument(requestFunctionalProfileURI);
			}
			catch (MalformedURLException e)
			{
				throw new SemanticRegistryMalformedInputException("Input parameter value 'profileURI' is malformed");
			}
			catch (IOException e)
			{
				throw new SemanticRegistryMalformedInputException("Input parameter value 'profileURI' is malformed");
			}
			
			if (!isValid) 
				throw new SemanticRegistryMalformedInputException("Input parameter value 'profileURI' is malformed");
									
			// Get the base URI of the RFP URI (remove the fragment)
			String rfpBaseURI = requestFunctionalProfileURI.toString().substring(0,requestFunctionalProfileURI.toString().indexOf("#"));
			System.out.println("Physical URI of ontology where RFP is defined: " + requestFunctionalProfileURI.toString().substring(0,requestFunctionalProfileURI.toString().indexOf("#")));
			System.out.println("Base/Logical URI of ontology where RFP is defined: " + rfpBaseURI);
			
			// Get the base URI of the SRKB ontology
			String srkbOntologyBaseURI = FileUtils.getBaseURIFromOntologyDocument(srkbOntologyPhysicalURI.normalize().toString());
			System.out.println("Physical URI of srkbOntology: " + srkbOntologyPhysicalURI);
			System.out.println("Base/Logical URI of srkbOntology: " + srkbOntologyBaseURI);
			
//			// Get the base URI of the EAI ontology
//			String eaiOntologyBaseURI = FileUtils.getBaseURIFromOntologyDocument(eaiOntologyPhysicalURI.normalize().toString());
//			System.out.println("Physical URI of eaiOntology: " + eaiOntologyPhysicalURI);
//			System.out.println("Base/Logical URI of eaiOntology: " + eaiOntologyBaseURI);
			
			
			
			// Get the base URI of the FunctionalFacet ontology
			
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
	        
	        // Get the base/logical URI of the FunctionalFacet ontology
			URI functionalFacetOntologyBaseURI = DiscoveryTimeMatchmaker.getDefiningOntology("#FunctionalFacetEntity", importsClosure, factory).getURI();
			
			
			
			
			
			boolean rfpDefinedInKnownNamespace = false;
			
			// Check if the baseURI of the URI of the requested Functional Profile
			// coincides with the base/logical URI of the SRKB or of the XXX EAI ontology XXX FunctionalFacet 
			if ( rfpBaseURI.equalsIgnoreCase(srkbOntologyBaseURI) 
					//|| rfpBaseURI.equalsIgnoreCase(eaiOntologyBaseURI) )
					|| rfpBaseURI.equalsIgnoreCase(functionalFacetOntologyBaseURI.toString()) )
			{
				rfpDefinedInKnownNamespace = true;
			}
			
			// Array for storing the UUIDs of the matching services
			String[] serviceUUIDs = null;
			
			// If the baseURI of the RFP is a known namespace
			if (rfpDefinedInKnownNamespace)
			{
				System.out.println("The provided RFP is defined in a known namespace. Attempting syntactic lookup in UDDI.");
				
				// Perform a syntactic search in the UDDI server
				try
				{
					// Create a categoryBag
					CategoryBag categoryBag = new CategoryBag();
	
					// Create a vector to hold all keyedReferences
					Vector<KeyedReference> krList = new Vector<KeyedReference>();
	
					// Create a keyedReference to represent the pointer to
					// the INDEXING_TMODEL_KEY with a value equal to the URI
					KeyedReference kr = new KeyedReference();
					kr.setTModelKey("uuid:" + INDEXING_TMODEL_KEY);
					kr.setKeyName("Subsumption_Indexing_tModel");
					kr.setKeyValue(requestFunctionalProfileURI);
	
					// Add the keyedReference to the vector
					krList.add(kr);
	
					// Add the keyedReferences vector to the categoryBag
					categoryBag.setKeyedReferenceVector(krList);
					
					// Get the search results
					ServiceList searchResults = proxy.find_service(serviceProviderUUID, categoryBag, null, 10000);
					
					Vector results = searchResults.getServiceInfos().getServiceInfoVector();
	
					if (results != null)
					{
						if (results.size() > 0)
						{
							// Initialise the list of UUIDs to the vector size
							serviceUUIDs = new String[(results.size())];
		
							// Extract the UUID key from every ServiceInfo object in
							// the vector and copy it to the list of service UUIDs
							// to be returned
							for (int i = 0; i < results.size(); i++)
							{
								ServiceInfo info = (ServiceInfo) results.elementAt(i);
								serviceUUIDs[i] = info.getServiceKey();
							}							
						}
						else
						{
							// System.out.println("The UDDI lookup did not return any matching advertisements.");
						}
					}
					else
					{
						// System.out.println("The UDDI lookup did not return any matching advertisements.");
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
							throw new SemanticRegistryException( "UDDI exception with error number: " + r.getErrno());
						}
					}
					else
					{
						System.out.println(
								"The UDDI server reported an internal error but did not provide a Disposition Report " +
								"to explain its cause. The problem resulted while trying to invoke the 'find_service' " +
								"operation, and the cause of the problem may be in the data provided (e.g. some parameter " +
								"value exceeding max character length), in a failure to communicate with the UDDI server, " +
								"or in a failure to communicate with the database that the UDDI server relies on.");
						throw new SemanticRegistryCommunicationException(
								"The UDDI server reported an internal error but did not provide a Disposition Report " +
								"to explain its cause. The problem resulted while trying to invoke the 'find_service' " +
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
				
				// if no matching service advertisements were returned...
				if (serviceUUIDs == null || serviceUUIDs.length == 0)
				{
					// When no matching advertisement is found, there might be two cases:
					// 1) True Negative: None of the AFPs matches the RFP
					// 2) False Negative: There is some AFP that could match the
					// RFP, but this indexing information is missing
					// 
					// The second case can only happen if the administrator of the 
					// Semantic Registry does use the reclassifyAdvertisementsForRequest 
					// operation consistently. This operation must be invoked whenever a 
					// new RFP is stored in the EAI ontology. If this is not observed, 
					// the registry's index will eventually become outdated. However, 
					// even if such a situation occurs, the administrator can use the 
					// reclassifyAdvertisementsForAllRequests operation to perform a 
					// complete re-indexing and bring the registry's index up to date 
					// with the contents of the EAI ontology. 
					
					System.out.println("The UDDI lookup did not return any matching advertisements.");
					
					throw new SemanticRegistryNoMatchFoundException("No matching service was found for profile: " 
							+ requestFunctionalProfileURI + ". Semantic matchmaking at publication-time didn't " +
									"reveal any Advertisement Functional Profile (AFP) matching the specified " +
									"Request Functional Profile (RFP).");
				}
				
			}// end if (rfpDefinedInKnownNamespace)
			
			// If the baseURI of the RFP is in an unknown namespace
			else
			{
				System.out.println("The provided RFP is defined in an unknown namespace. Attempting semantic matchmaking.");
				
				// Create a matchmaker to find AFPs that can match the provided RFP
				DiscoveryTimeMatchmaker matchmaker = new DiscoveryTimeMatchmaker();
				
				// Get the URIs of all AFPs that can satisfy the RFP
				SemanticProfileURICollection matchingAFPs = null;

				// Instead of the EAI ontology, the ontology in which the
				// RFP is defined must be provided to the matchmaker for
				// loading into the DL reasoner (this is why the 
				// URI.create(rfpBaseURI) is used
				try
				{
					matchingAFPs = matchmaker.getMatchingAFPsForRFP(srkbOntologyPhysicalURI, 
																	URI.create(rfpBaseURI), 
																	URI.create(requestFunctionalProfileURI));
				}
				catch (SemanticRegistryMatchmakingException e)
				{
					if (e.getMessage() == null || e.getMessage().length() == 0)
						throw new SemanticRegistryConfigurationException("Problem occured during the semantic classification of the advertisement");
					else
						throw new SemanticRegistryConfigurationException(e.getMessage());
				}
				catch (SemanticRegistryConfigurationException e)
				{
					if (e.getMessage() == null || e.getMessage().length() == 0)
						throw new SemanticRegistryConfigurationException("Problem occured during the semantic classification of the advertisement");
					else
						throw new SemanticRegistryConfigurationException(e.getMessage());
				}
				
				// if no matching service advertisements were returned...
				if (matchingAFPs == null || matchingAFPs.getProfileURIs().size() == 0)
				{
					throw new SemanticRegistryNoMatchFoundException("No matching service was found for profile: " 
							+ requestFunctionalProfileURI + ". Semantic matchmaking at discovery-time didn't " +
							"reveal any Advertisement Functional Profile (AFP) matching the specified " +
							"Request Functional Profile (RFP).");
				}
				// if matching service advertisements were successfully returned...
				else
				{
					List<String> matches = matchingAFPs.getProfileURIs();
					URI matchingAFPURI = null;
				    
					// Initialise the list of UUIDs to the vector size
					serviceUUIDs = new String[(matches.size())];
					
				    for (int i = 0; i < matches.size(); i++)
				    {
				    	matchingAFPURI = URI.create(matches.get(i));
				    	serviceUUIDs[i] = matchingAFPURI.getFragment();
				    }
				}
			} // end else if baseURI of the RFP is in an unknown namespace
			
			
			// Return the (what should be a non empty) list of matching service UUIDs
			return serviceUUIDs;
			
			
		} // endif input is well-formed
		else
		{
			System.out.println("SemanticRegistryMalformedInputException occured!");
			if (!InputValidator.isTextParameterWellFormed(requestFunctionalProfileURI)) 
				throw new SemanticRegistryMalformedInputException("Input parameter value 'profileURI' is malformed");
			if (!InputValidator.isUUIDKeyWellFormed(serviceProviderUUID)) 
				throw new SemanticRegistryMalformedInputException("Input parameter value 'serviceProviderUUID' is malformed");
			throw new SemanticRegistryMalformedInputException("One or more input parameter values are malformed");

		}
	}
	
	
	
	
	
	
		
	/**
	 * Checks if all five Canonical TModels that are necessary for the Semantic
	 * Registry's operation have been properly registered with the UDDI server.
	 * 
	 * @throws SemanticRegistryException
	 */
	private void validateHealthOfCanonicalTModelDeployment() 
	
	throws SemanticRegistryException
	{
		// Assert that key values for all 5 canonical tModels have been read from the registry.properties file 
		if ( (SAWSDL_TMODEL_KEY == null || SAWSDL_TMODEL_KEY.length() != 36) 
				|| (CATEGORY_TMODEL_KEY == null || CATEGORY_TMODEL_KEY.length() != 36)  
				|| (INPUT_TMODEL_KEY == null || INPUT_TMODEL_KEY.length() != 36) 
				|| (OUTPUT_TMODEL_KEY == null || OUTPUT_TMODEL_KEY.length() != 36) 
				|| (INDEXING_TMODEL_KEY == null || INDEXING_TMODEL_KEY.length() != 36))
		{
			System.out.println("Incomplete Semantic Registry deployment - the key values for one or more Canonical tModels have not been set correctly in the registry.properties file");
			throw new SemanticRegistryException("Incomplete Semantic Registry deployment - the key values for one or more Canonical tModels have not been set correctly in the registry.properties file");
		}
		
		// Key values have been read for all 5 Canonical tModels, but 
		// do they reflect the real keys of the published tModels, if any?
		try
		{
			String[] realTModels = getCanonicalTModelDetails();
			
			// Assert that 5 canonical tModels been published in the registry
			if (realTModels.length != 5) 
			{
				System.out.println("Incomplete Semantic Registry deployment - not all Canonical tModels have been deployed in the Semantic Registry");
				throw new SemanticRegistryException("Incomplete Semantic Registry deployment - not all Canonical tModels have been deployed in the Semantic Registry");
			}
			// 5 canonical tModels been published, but do their keys match the ones that were read from the registry.properties file?
			else
			{
				Set setOfReadKeys = new HashSet();
				setOfReadKeys.add(SAWSDL_TMODEL_KEY);
				setOfReadKeys.add(CATEGORY_TMODEL_KEY);
				setOfReadKeys.add(INPUT_TMODEL_KEY);
				setOfReadKeys.add(OUTPUT_TMODEL_KEY);
				setOfReadKeys.add(INDEXING_TMODEL_KEY);
				
				// Assert that the keys read from the registry.properties file are all different
				if (setOfReadKeys.size() != 5) 
				{
					System.out.println("Incomplete Semantic Registry deployment - erroneous duplicate key values have been set in the registry.properties file");
					throw new SemanticRegistryException("Incomplete Semantic Registry deployment - erroneous duplicate key values have been set in the registry.properties file");
				}
				
				// Extract the tModelKeys from the canonical TModel details information text, 
				// which is of the form: TMODELNAME(uuid:TMODELKEY)
				Set setOfPublishedKeys = new HashSet();
				for (int i = 0; i < realTModels.length; i++)
				{
					String key = (realTModels[i].substring(realTModels[i].indexOf(":")+1)).replace(")", "");
					//System.out.println("Extracted key: " + key + ", taken from: " + realTModels[i]);
					setOfPublishedKeys.add(key);
				}
				
				// Assert that the two sets contain the exact same elements
				if ( ! (setOfPublishedKeys.containsAll(setOfReadKeys) && setOfReadKeys.containsAll(setOfPublishedKeys)))
				{
					System.out.println("Incomplete Semantic Registry deployment - the key values that have been set in the registry.properties file do not match the key values of the Canonical tModels published in the UDDI server");
					throw new SemanticRegistryException("Incomplete Semantic Registry deployment - the key values that have been set in the registry.properties file do not match the key values of the Canonical tModels published in the UDDI server");
				}
			}			
		}
		catch (SemanticRegistryCommunicationException e)
		{
			throw new SemanticRegistryException("Problem occured while trying to validate the health of the Semantic Registry deployment");
		}
		catch (SemanticRegistryNoMatchFoundException e)
		{
			throw new SemanticRegistryException("Problem occured while trying to validate the health of the Semantic Registry deployment");
		}
	}
	
	
	
	
	
	
	
	/**
	 * Returns an array of strings, with each string corresponding to the name
	 * and key of a canonical tModel, belonging to the set of five canonical
	 * tModels that are necessary for the operation of the Semantic Registry
	 * and have been specified in the registry.properties file.
	 * 
	 * @return An array of Strings of the form [tModelName][:][tModelKey] 
	 *         for each of the Canonical tModels found, if any
	 * 
	 * @throws SemanticRegistryNoMatchFoundException
	 * @throws SemanticRegistryCommunicationException
	 * @throws SemanticRegistryException
	 * @throws SemanticRegistryNoMatchFoundException
	 */
	private String[] getCanonicalTModelDetails() 
	
	throws SemanticRegistryCommunicationException, 
		SemanticRegistryException, 
		SemanticRegistryNoMatchFoundException
	{
		String[] tModelNames = { 
				"SAWSDL_Document_URL_tModel", 
				"Category_Annotation_tModel",
				"Input_Annotation_tModel", 
				"Output_Annotation_tModel",
				"Subsumption_Indexing_tModel" };

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
				// tModelDetails.add(tModelNames[i] + ":" +
				// tempKey.replace("uuid:", ""));
				tModelDetails.add(tModelNames[i] + "(" + tempKey + ")");
			}
			else
			{
				//System.out.println(tModelNames[i] + " was not found!");
			}
		}
		if (tModelDetails.size() == 0)
		{
			throw new SemanticRegistryNoMatchFoundException(
					"No canonical tModels have been deployed yet!");
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
	 * @throws SemanticRegistryException
	 * @throws SemanticRegistryCommunicationException
	 */
	private String findTModel(String tModelName) throws SemanticRegistryException,
			SemanticRegistryCommunicationException
	{
		Vector tModelInfoVector = null;

		// Define findQualifier, to make sure the most recent model is on top of
		// the list
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
				//System.out.println("tModel key found: " + tModelKey);
			}
			else
			{
				System.out.println("No Technical Models found for name: " + tModelName);
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
				System.out.println(
						"The UDDI server reported an internal error but did not provide a Disposition Report " +
						"to explain its cause. The problem resulted while trying to invoke the 'find_tModel' " +
						"operation, and the cause of the problem may be in the data provided (e.g. some parameter " +
						"value exceeding max character length), in a failure to communicate with the UDDI server, " +
						"or in a failure to communicate with the database that the UDDI server relies on.");
				throw new SemanticRegistryCommunicationException(
						"The UDDI server reported an internal error but did not provide a Disposition Report " +
						"to explain its cause. The problem resulted while trying to invoke the 'find_tModel' " +
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
		return tModelKey;
	}
	
		
}
