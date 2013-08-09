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
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import org.seerc.fusion.sr.exceptions.SemanticRegistryAuthException;
import org.seerc.fusion.sr.exceptions.SemanticRegistryMatchmakingException;
import org.seerc.fusion.sr.exceptions.SemanticRegistryCommunicationException;
import org.seerc.fusion.sr.exceptions.SemanticRegistryConfigurationException;
import org.seerc.fusion.sr.exceptions.SemanticRegistryException;
import org.seerc.fusion.sr.exceptions.SemanticRegistryMalformedInputException;
import org.seerc.fusion.sr.exceptions.SemanticRegistryNoMatchFoundException;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyChangeException;
import org.semanticweb.owl.model.OWLOntologyCreationException;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.semanticweb.owl.model.OWLOntologyStorageException;
import org.semanticweb.owl.model.UnknownOWLOntologyException;
import org.semanticweb.owl.util.OWLEntityCollector;
import org.semanticweb.owl.util.OWLEntityRemover;

import org.uddi4j.UDDIException;
import org.uddi4j.client.UDDIProxy;
import org.uddi4j.datatype.business.BusinessEntity;
import org.uddi4j.datatype.service.BusinessService;
import org.uddi4j.response.AuthToken;
import org.uddi4j.response.BusinessDetail;
import org.uddi4j.response.DispositionReport;
import org.uddi4j.response.Result;
import org.uddi4j.response.ServiceDetail;
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
public class PublicationHandler
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
	public PublicationHandler()
	
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
	 * Calls the UDDI server's get_authToken function to obtain an
	 * authentication token (authToken). Authentication tokens are opaque values
	 * that are required for all other calls to the UDDI server's publisher API.
	 * The get_authToken function may not be supported by all UDDI server
	 * implementations.
	 * 
	 * @param username
	 * @param password
	 * @return
	 * @throws SemanticRegistryMalformedInputException
	 * @throws SemanticRegistryAuthException
	 * @throws SemanticRegistryCommunicationException
	 * @throws SemanticRegistryException
	 */
	public String generateAuthenticationToken(
		String username, 
		String password)
	
	throws 
		SemanticRegistryMalformedInputException,
		SemanticRegistryAuthException,
		SemanticRegistryCommunicationException, 
		SemanticRegistryException
	{
		if (InputValidator.isTextParameterWellFormed(username)
				&& InputValidator.isTextParameterWellFormed(password))
		{
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
			return token.getAuthInfoString();

		} // endif input is well-formed
		else
		{
			System.out.println("SemanticRegistryMalformedInputException occured!");
			if (!InputValidator.isTextParameterWellFormed(username))
				throw new SemanticRegistryMalformedInputException("Input parameter value 'username' is malformed");
			if (!InputValidator.isTextParameterWellFormed(password))
				throw new SemanticRegistryMalformedInputException("Input parameter value 'password' is malformed");
			throw new SemanticRegistryMalformedInputException("One or more input parameter values are malformed");
		}
	}
		
	
	
	
	
	
	
	/**
	 * Calls the UDDI server's discard_authToken function to inform the UDDI
	 * server that the authentication token is to be discarded, effectively
	 * ending the session. Subsequent calls that use the same authToken will be
	 * rejected. The discard_authToken function is optional for UDDI servers
	 * that do not manage session state or that do not support the get_authToken
	 * message.
	 * 
	 * @param authToken
	 * @return
	 * @throws SemanticRegistryMalformedInputException
	 * @throws SemanticRegistryAuthException
	 * @throws SemanticRegistryCommunicationException
	 * @throws SemanticRegistryException
	 */
	public Boolean destroyAuthenticationToken(String authToken)
	
	throws SemanticRegistryMalformedInputException,
			SemanticRegistryAuthException,
			SemanticRegistryCommunicationException, 
			SemanticRegistryException
	{
		if (InputValidator.isAuthenticationTokenWellFormed(authToken))
		{
			//The authToken key has been asserted to be valid, but should also have the proper prefix
			if (authToken.length() == 36) authToken = "authToken:".concat(authToken);
			
			try
			{
				proxy.discard_authToken(authToken);
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
							"to explain its cause. The problem resulted while trying to invoke the 'discard_authToken' " +
							"operation, and the cause of the problem may be in the data provided (e.g. some parameter " +
							"value exceeding max character length), in a failure to communicate with the UDDI server, " +
							"or in a failure to communicate with the database that the UDDI server relies on.");
					throw new SemanticRegistryCommunicationException(
							"The UDDI server reported an internal error but did not provide a Disposition Report " +
							"to explain its cause. The problem resulted while trying to invoke the 'discard_authToken' " +
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
			return true;

		} // endif input is well-formed
		else
		{
			System.out.println("SemanticRegistryMalformedInputException occured!");
			throw new SemanticRegistryMalformedInputException("Input parameter value 'authToken' is malformed");
		}
	}
	
	
	
	
	
	
		
	/**
	 * Creates a Service Provider record in the UDDI server. In UDDI, a Service
	 * Provider is represented as a businessEntity element. Before proceeding,
	 * the values of all provided input parameters are validated.
	 * 
	 * @param authToken
	 * @param serviceProviderName
	 * @param serviceProviderFreeTextDescription
	 * @return
	 * @throws SemanticRegistryMalformedInputException
	 * @throws SemanticRegistryAuthException
	 * @throws SemanticRegistryCommunicationException
	 * @throws SemanticRegistryException
	 */
	public String addServiceProvider(
			String authToken,
			String serviceProviderName,
			String serviceProviderFreeTextDescription)
			
	throws SemanticRegistryMalformedInputException,
			SemanticRegistryAuthException,
			SemanticRegistryCommunicationException, 
			SemanticRegistryException
	{
		// CAUTION: serviceProviderFreeTextDescription is declared as nillable
		// so it may be null, empty, or full
		
		// Remove leading and trailing whitespaces from values to be used as keys
		authToken = authToken.trim();
		
		if (InputValidator.isAuthenticationTokenWellFormed(authToken)
				&& InputValidator.isTextParameterWellFormed(serviceProviderName)
				&& ( serviceProviderFreeTextDescription == null 
						|| serviceProviderFreeTextDescription.length() == 0
						|| InputValidator.isTextParameterWellFormed(serviceProviderFreeTextDescription)))
		{
			//The authToken key has been asserted to be valid, but should also have the proper prefix
			if (authToken.length() == 36) authToken = "authToken:".concat(authToken);
			
			// Create a vector for storing the business entities to be
			// published
			Vector<BusinessEntity> vector = new Vector<BusinessEntity>();

			// Create a new business entity (BusinessKey must be empty "")
			BusinessEntity be = new BusinessEntity("", serviceProviderName);
			be.setDefaultDescriptionString(serviceProviderFreeTextDescription);

			// Add the business entity to the vector
			vector.addElement(be);
			
			BusinessEntity returnedBusinessEntity = null;
			try
			{
				// Save business and retrieve a BusinessDetail message
				BusinessDetail bd = proxy.save_business(authToken, vector);
				
				// Get the first (and logically only) business entity
				// from the returned BusinessDetail
				returnedBusinessEntity = (BusinessEntity) (bd.getBusinessEntityVector().elementAt(0));
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
							"to explain its cause. The problem resulted while trying to invoke the 'save_business' " +
							"operation, and the cause of the problem may be in the data provided (e.g. some parameter " +
							"value exceeding max character length), in a failure to communicate with the UDDI server, " +
							"or in a failure to communicate with the database that the UDDI server relies on.");
					throw new SemanticRegistryCommunicationException(
							"The UDDI server reported an internal error but did not provide a Disposition Report " +
							"to explain its cause. The problem resulted while trying to invoke the 'save_business' " +
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
			return returnedBusinessEntity.getBusinessKey();

		} // endif input is well-formed
		else
		{
			System.out.println("SemanticRegistryMalformedInputException occured!");
			if (!InputValidator.isAuthenticationTokenWellFormed(authToken))
				throw new SemanticRegistryMalformedInputException("Input parameter value 'authToken' is malformed");
			if (!InputValidator.isTextParameterWellFormed(serviceProviderName))
				throw new SemanticRegistryMalformedInputException("Input parameter value 'serviceProviderName' is malformed");
			if (!InputValidator.isTextParameterWellFormed(serviceProviderFreeTextDescription))
				throw new SemanticRegistryMalformedInputException("Input parameter value 'serviceProviderFreeTextDescription' is malformed");
			throw new SemanticRegistryMalformedInputException("One or more input parameter values are malformed");
		}
	}
	
	
	
	
	
	
	
	
	/**
	 * Deletes a Service Provider record from the UDDI server. In UDDI, a
	 * Service Provider is represented as a businessEntity element. Before
	 * proceeding, the values of all provided input parameters are validated.
	 * 
	 * @param authToken
	 * @param serviceProviderUUID
	 * @return
	 * @throws SemanticRegistryMalformedInputException
	 * @throws SemanticRegistryAuthException
	 * @throws SemanticRegistryCommunicationException
	 * @throws SemanticRegistryException
	 */
	public Boolean removeServiceProvider(
			String authToken,
			String serviceProviderUUID) 
			
	throws SemanticRegistryMalformedInputException,
			SemanticRegistryAuthException,
			SemanticRegistryCommunicationException, 
			SemanticRegistryException
	{
		// Remove leading and trailing whitespaces from values to be used as keys
		authToken = authToken.trim();
		serviceProviderUUID = serviceProviderUUID.trim();
		
		if (InputValidator.isAuthenticationTokenWellFormed(authToken)
				&& InputValidator.isUUIDKeyWellFormed(serviceProviderUUID))
		{
			//The authToken key has been asserted to be valid, but should also have the proper prefix
			if (authToken.length() == 36) authToken = "authToken:".concat(authToken);

			try
			{
				proxy.delete_business(authToken, serviceProviderUUID);
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
						throw new SemanticRegistryException("UDDI exception with error number: "
								+ r.getErrno());
					}
				}
				else
				{
					System.out.println(
							"The UDDI server reported an internal error but did not provide a Disposition Report " +
							"to explain its cause. The problem resulted while trying to invoke the 'delete_business' " +
							"operation, and the cause of the problem may be in the data provided (e.g. some parameter " +
							"value exceeding max character length), in a failure to communicate with the UDDI server, " +
							"or in a failure to communicate with the database that the UDDI server relies on.");
					throw new SemanticRegistryCommunicationException(
							"The UDDI server reported an internal error but did not provide a Disposition Report " +
							"to explain its cause. The problem resulted while trying to invoke the 'delete_business' " +
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
			return true;

		} // endif input is well-formed
		else
		{
			System.out.println("SemanticRegistryMalformedInputException occured!");
			if (!InputValidator.isAuthenticationTokenWellFormed(authToken)) throw new SemanticRegistryMalformedInputException(
					"Input parameter value 'authToken' is malformed");
			if (!InputValidator.isUUIDKeyWellFormed(serviceProviderUUID)) throw new SemanticRegistryMalformedInputException(
					"Input parameter value 'serviceProviderUUID' is malformed");
			throw new SemanticRegistryMalformedInputException(
					"One or more input parameter values are malformed");
		}
	}
	
	
	
	
	
	
	
	/**
	 * Updates a Service Provider record in the UDDI server. In UDDI, a Service
	 * Provider is represented as a businessEntity element. Before proceeding,
	 * the values of all provided input parameters are validated. 
	 * 
	 * @param authToken
	 * @param serviceProviderUUID
	 * @param serviceProviderName
	 * @param serviceProviderFreeTextDescription
	 * @return
	 * @throws SemanticRegistryMalformedInputException
	 * @throws SemanticRegistryAuthException
	 * @throws SemanticRegistryCommunicationException
	 * @throws SemanticRegistryException
	 */
	public Boolean modifyServiceProvider(
			String authToken,
			String serviceProviderUUID, 
			String serviceProviderName,
			String serviceProviderFreeTextDescription)
			
	throws SemanticRegistryMalformedInputException,
			SemanticRegistryAuthException,
			SemanticRegistryCommunicationException, 
			SemanticRegistryException
	{
		// CAUTION: serviceProviderName and serviceProviderFreeTextDescription 
		// are declared as nillable so they may be null, empty, or full
		
		// Remove leading and trailing whitespaces from values to be used as keys
		authToken = authToken.trim();
		serviceProviderUUID = serviceProviderUUID.trim();
		
		if (InputValidator.isAuthenticationTokenWellFormed(authToken) 
				&& InputValidator.isUUIDKeyWellFormed(serviceProviderUUID))
		{
			
			// Check if nothing needs to be updated
			if (  (serviceProviderName == null || serviceProviderName.length() == 0) &&
				(serviceProviderFreeTextDescription == null || serviceProviderFreeTextDescription.length() == 0)  )
			{
				System.out.println("No modification requirements have been specified " +
						"(Input parameters 'serviceProviderName' and 'serviceProviderFreeTextDescription' are both empty)");
				throw new SemanticRegistryMalformedInputException(
						"Nothing to modify: Input parameters 'serviceProviderName' and 'serviceProviderFreeTextDescription' are both empty");
			}
			
			//The authToken key has been asserted to be valid, but should also have the proper prefix
			if (authToken.length() == 36) authToken = "authToken:".concat(authToken);
			
			// Create a vector for storing the business entities to be modified
			Vector<BusinessEntity> vector = new Vector<BusinessEntity>();
			
			// Create a new business entity
			BusinessEntity newProvider = null;
			
			try
			{
				// Get the business detail from the registry based on the key
				BusinessDetail bd = proxy.get_businessDetail(serviceProviderUUID);

				Vector ve = bd.getBusinessEntityVector();

				// Get the first business entity from the returned vector
				BusinessEntity provider = (BusinessEntity) ve.get(0);
				
				// Check if the serviceProviderName needs to be updated
				if (serviceProviderName == null || serviceProviderName.length() == 0)
				{
					// Retrieve the old serviceProviderName and reuse it
					newProvider = new BusinessEntity(serviceProviderUUID, provider.getDefaultNameString());
				}
				else
				{
					if (serviceProviderName.length() < 256)
						newProvider = new BusinessEntity(serviceProviderUUID, serviceProviderName);
					else
						throw new SemanticRegistryMalformedInputException("Input parameter value 'serviceProviderName' is too long");
				}

				// Check if the serviceProviderFreeTextDescription needs to be updated
				if (serviceProviderFreeTextDescription == null || serviceProviderFreeTextDescription.length() == 0)
				{
					// Retrieve the old serviceProviderFreeTextDescription and reuse it
					newProvider.setDefaultDescriptionString(provider.getDefaultDescriptionString());
				}
				else
				{
					if (serviceProviderFreeTextDescription.length() < 256)
						newProvider.setDefaultDescriptionString(serviceProviderFreeTextDescription);
					else 
						throw new SemanticRegistryMalformedInputException("Input parameter value 'serviceProviderFreeTextDescription' is too long");
				}
				
				// Set the new key equal to the old, or else a new business entity will be created
				newProvider.setBusinessKey(provider.getBusinessKey());
				
				// Add the business entity to the vector
				vector.addElement(newProvider);

				// Save business entity
				proxy.save_business(authToken, vector);
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
						
						if (r.getErrInfo().getErrCode().equals("E_authTokenRequired"))
						{
							System.out.println("E_authTokenRequired: (10120) The authentication token is invalid");
							throw new SemanticRegistryAuthException("The authentication token is invalid");
						}
						if (r.getErrInfo().getErrCode().equals("E_authTokenExpired"))
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
				throw new SemanticRegistryCommunicationException("Problem communicating with the UDDI server");
			}
			catch (Exception e)
			{
				System.out.println("SemanticRegistryException occured!");
				throw new SemanticRegistryException("An exception occured for unspecified reasons");
			}
			
			// If all goes well
			return true;

		} // endif input is well-formed
		else
		{
			System.out.println("SemanticRegistryMalformedInputException occured!");
			if (!InputValidator.isAuthenticationTokenWellFormed(authToken))
				throw new SemanticRegistryMalformedInputException("Input parameter value 'authToken' is malformed");
			if (!InputValidator.isUUIDKeyWellFormed(serviceProviderUUID))
				throw new SemanticRegistryMalformedInputException("Input parameter value 'serviceProviderUUID' is malformed");
			throw new SemanticRegistryMalformedInputException("One or more input parameter values are malformed");
		}
	}
	
	
	
	
	
	
	
	/**
	 * Create a new service advertisement. In UDDI, a service is represented as
	 * a businessService element. The advertisement includes the information
	 * provided by the client and some additional information that is generated
	 * by creating an Advertisement Functional Profile (AFP), performing
	 * matchmaking and discovering Request Functional Profiles (RFP) that can be
	 * readily satisfied by the new service advertisement. Before proceeding,
	 * the values of all provided input parameters are validated.
	 * 
	 * @param authToken
	 * @param serviceSAWSDLURL
	 * @param serviceProviderUUID
	 * @param serviceName
	 * @param serviceFreeTextDescription
	 * @param hasCategoryAnnotationURI
	 * @param hasInputAnnotationURIArray
	 * @param hasOutputAnnotationURIArray
	 * @return
	 * @throws SemanticRegistryMalformedInputException
	 * @throws SemanticRegistryAuthException
	 * @throws SemanticRegistryCommunicationException
	 * @throws SemanticRegistryMatchmakingException
	 * @throws SemanticRegistryException
	 * @throws SemanticRegistryConfigurationException 
	 */
	public String addServiceWithoutSAWSDL(
			String authToken, 
			String serviceSAWSDLURL,
			String serviceProviderUUID, 
			String serviceName,
			String serviceFreeTextDescription, 
			String hasCategoryAnnotationURI, 
			String[] hasInputAnnotationURIArray, 
			String[] hasOutputAnnotationURIArray)
			
	throws SemanticRegistryMalformedInputException,
			SemanticRegistryAuthException,
			SemanticRegistryCommunicationException, 
			SemanticRegistryMatchmakingException, 
			SemanticRegistryException, SemanticRegistryConfigurationException 
	{
		// Remove leading and trailing whitespaces from values to be used as keys
		if (authToken != null) authToken = authToken.trim();
		if (serviceProviderUUID != null) serviceProviderUUID = serviceProviderUUID.trim();

		if ( InputValidator.isAuthenticationTokenWellFormed(authToken) 
		&& InputValidator.isUUIDKeyWellFormed(serviceProviderUUID)
		&& InputValidator.isURLWellFormed(serviceSAWSDLURL)
		&& InputValidator.isURIWellFormed(hasCategoryAnnotationURI)	)
		{
			//The authToken key has been asserted to be valid, but should also have the proper prefix
			if (authToken.length() == 36) authToken = "authToken:".concat(authToken);
			
			// The client should provide a preferred name 
			if ( serviceName == null || 
				 serviceName.length() == 0 || 
				(serviceName.length() == 1 && serviceName.equalsIgnoreCase(" ")) )
			{
				throw new SemanticRegistryMalformedInputException("Input parameter value 'serviceName' cannot be empty");
			}
			
			// If the client has provided a preferred name make sure it's not too long
			if (serviceName != null && serviceName.length() > 0)
			{
				if (serviceName.length() > 256) 
					throw new SemanticRegistryMalformedInputException("Input parameter value 'serviceName' is too long");
			}
			
			// If the client has provided a service description make sure it's not too long
			if (serviceFreeTextDescription != null && serviceFreeTextDescription.length() > 0)
			{
				if (serviceFreeTextDescription.length() > 256) 
					throw new SemanticRegistryMalformedInputException("Input parameter value 'serviceFreeTextDescription' is too long");
			}
			
			// Make sure that the specified UUID of the Service Provider 
			// that offers this service is known to the UDDI server by 
			// trying to retrieve a business detail object for the specified key
			BusinessDetail businessDetail;
			try
			{
				businessDetail = proxy.get_businessDetail(serviceProviderUUID);

				// Get the BusinessEntity vector from the returned result
				Vector businessEntityVector = businessDetail.getBusinessEntityVector();
				
				// If no match was found for that key...
				if (businessEntityVector == null || businessEntityVector.size() == 0) 
					throw new SemanticRegistryMalformedInputException("The UUID key specified in the input parameter " +
						"value 'serviceProviderUUID' does not match any known Service Provider UUID keys");
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

						if (r.getErrInfo().getErrCode().equals("E_authTokenRequired"))
						{
							System.out.println("E_authTokenRequired: (10120) The authentication token is invalid");
							throw new SemanticRegistryMalformedInputException("The authentication token is invalid");
						}
						if (r.getErrInfo().getErrCode().equals("E_authTokenExpired"))
						{
							System.out.println("E_invalidKeyPassed: (10110) The authentication token has timed out");
							throw new SemanticRegistryMalformedInputException("The authentication token has timed out");
						}
						if (r.getErrInfo().getErrCode().equals("E_invalidKeyPassed"))
						{
							System.out.println("E_invalidKeyPassed: (10210) The uuid_key did not match any known key values");
							throw new SemanticRegistryMalformedInputException("The UUID key specified in the input parameter " +
							"value 'serviceProviderUUID' does not match any known Service Provider UUID keys");
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
				throw new SemanticRegistryCommunicationException("Problem communicating with the UDDI server");
			}
			catch (Exception e)
			{
				System.out.println("SemanticRegistryException occured!");
				throw new SemanticRegistryException("An exception occured for unspecified reasons");
			}

			
			// Make sure that there is at least one input or one output for the
			// service (publishing a service with no inputs and no outputs doesn't make sense)
			if ( (hasInputAnnotationURIArray == null || hasInputAnnotationURIArray.length == 0) && 
				 (hasOutputAnnotationURIArray == null || hasOutputAnnotationURIArray.length == 0 ) )
				throw new SemanticRegistryMalformedInputException(
						"No semantic annotations have been provided for the service's " +
						"inputs and outputs. A service must have at least one input " +
						"or produce at least one output.");
						
			// If the service has inputs (i.e. MEP is in-only or in-out), 
			// make sure that every input annotation for the service is 
			// a valid URI (although not necessarily retrievable)
			if (hasInputAnnotationURIArray != null)
			{
				// Special case where the client's SOAP engine sends an empty 
				// XSD structure (e.g. <q0:hasInputAnnotationURIList />) but 
				// it is interpreted as an array with a single empty string 
				if (hasInputAnnotationURIArray.length == 1 && 
						hasInputAnnotationURIArray[0].length() == 0)
				{
					// Better nullified
					hasInputAnnotationURIArray = null;
				}
				
				// Case were one URI has been provided
				else if (hasInputAnnotationURIArray.length == 1 && 
						hasInputAnnotationURIArray[0].length() > 0)
				{
					if (!InputValidator.isURIWellFormed(hasInputAnnotationURIArray[0]))
					{
						throw new SemanticRegistryMalformedInputException(
								"The 'hasInputAnnotationURIArray' parameter value is not a valid URI");
					}
				}

				// Case were two or more URIs have been provided
				else if (hasInputAnnotationURIArray.length > 1)
				{
					for (int i = 0; i < hasInputAnnotationURIArray.length; i++)
					{
						if (!InputValidator.isURIWellFormed(hasInputAnnotationURIArray[i]))
						{
							throw new SemanticRegistryMalformedInputException(
									"One or more of the 'hasInputAnnotationURIArray' parameter values is not a valid URI");
						}
					}
				}
			}
			
			// If the service has outputs (i.e. MEP is out-only or in-out), 
			// make sure that every output annotation for the service is 
			// a valid URI (although not necessarily retrievable)
			if (hasOutputAnnotationURIArray != null)
			{
				// Special case where the client's SOAP engine sends an empty 
				// XSD structure (e.g. <q0:hasOutputAnnotationURIList />) but 
				// it is interpreted as an array with a single empty string 
				if (hasOutputAnnotationURIArray.length == 1 && 
						hasOutputAnnotationURIArray[0].length() == 0)
				{
					// Better nullified
					hasOutputAnnotationURIArray = null;
				}
				
				// Case were one URI has been provided
				else if (hasOutputAnnotationURIArray.length == 1 && 
						hasOutputAnnotationURIArray[0].length() > 0)
				{
					if (!InputValidator.isURIWellFormed(hasOutputAnnotationURIArray[0]))
					{
						throw new SemanticRegistryMalformedInputException(
								"The 'hasOutputAnnotationURIArray' parameter value is not a valid URI");
					}
				}

				// Case were two or more URIs have been provided
				else if (hasOutputAnnotationURIArray.length > 1)
				{
					for (int i = 0; i < hasOutputAnnotationURIArray.length; i++)
					{
						if (!InputValidator.isURIWellFormed(hasOutputAnnotationURIArray[i]))
						{
							throw new SemanticRegistryMalformedInputException(
									"One or more of the 'hasOutputAnnotationURIArray' parameter values is not a valid URI");
						}
					}
				}
			}

			// Create a service advertisement java bean using: 
			// (i) information provided (serviceProviderUUID, serviceName, serviceFreeTextDescription) and 
			// (ii) modelReference URIs provided (no need to parse the SAWSDL document to extract them)
			// ... but with a keyedReferences vector in the categoryBag that doesn't contain subsumption information
			UDDIServiceAdvertisement adv = createServiceAdvertisementWithoutSAWSDL(
					serviceSAWSDLURL, 
					serviceProviderUUID, 
					serviceName, 
					serviceFreeTextDescription, 
					hasCategoryAnnotationURI, 
					hasInputAnnotationURIArray, 
					hasOutputAnnotationURIArray);
			
			// Create a vector for the businessService entities to be published
			Vector<BusinessService> businessServiceVector = new Vector<BusinessService>();

			// Create a new businessService entity
			BusinessService businessService = new BusinessService();

			// Set the business key of the service provider
			businessService.setBusinessKey(adv.getServiceProviderUUID());

			// Set default businessService name
			businessService.setDefaultNameString(adv.getServiceName(), "en");

			// Set default businessService description
			businessService.setDefaultDescriptionString(adv.getServiceFreeTextDescription());

			// Add the categoryBag to the businessService
			businessService.setCategoryBag(adv.getServiceCategoryBag());

			// Add the businessService to the vector
			businessServiceVector.addElement(businessService);

			// Create a ServiceDetail to store the outcome of
			// the save_service call
			ServiceDetail serviceDetail = null;

			try
			{
				// Save the service and retrieve a serviceDetail message
				serviceDetail = proxy.save_service(authToken, businessServiceVector);
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

						if (r.getErrInfo().getErrCode().equals("E_authTokenRequired"))
						{
							System.out.println("E_authTokenRequired: (10120) The authentication token is invalid");
							throw new SemanticRegistryAuthException("The authentication token is invalid");
						}
						if (r.getErrInfo().getErrCode().equals("E_authTokenExpired"))
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
					System.out.println(
							"The UDDI server reported an internal error but did not provide a Disposition Report " +
							"to explain its cause. The problem resulted while trying to invoke the 'save_service' " +
							"operation, and the cause of the problem may be in the data provided (e.g. some parameter " +
							"value exceeding max character length), in a failure to communicate with the UDDI server, " +
							"or in a failure to communicate with the database that the UDDI server relies on.");
					throw new SemanticRegistryCommunicationException(
							"The UDDI server reported an internal error but did not provide a Disposition Report " +
							"to explain its cause. The problem resulted while trying to invoke the 'save_service' " +
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

			// If all goes well: i.e. the service advertisement gets saved
			BusinessService returnedBusinessService = null;
			String serviceKey = null;

			// Get the first (and logically only) business service entity from
			// the returned serviceDetail object
			returnedBusinessService = (BusinessService) serviceDetail.getBusinessServiceVector().elementAt(0);

			// Get the Business Service key
			serviceKey = returnedBusinessService.getServiceKey();

			// Create a new categoryBag, including subsumption indexing
			// information, and replace the old categoryBag in the saved advertisement

			// Get the old category bag
			CategoryBag oldCategoryBag = adv.getServiceCategoryBag();

			// Get the old keyedReferences vector
			Vector<KeyedReference> oldKeyedRefVector = oldCategoryBag.getKeyedReferenceVector();

			// Create lists to hold the modelReference annotations that will be read from the categoryBag
			List<String> functionalModelReferenceAnnotations = new ArrayList<String>();
			List<String> inputModelReferenceAnnotations = new ArrayList<String>();
			List<String> outputModelReferenceAnnotations = new ArrayList<String>();

			// Populate the lists
			if (oldKeyedRefVector != null)
			{
				int vectorSize = oldKeyedRefVector.size();
				if (vectorSize > 0)
				{
					// For every KeyedReference
					for (int i = 0; i < vectorSize; i++)
					{
						KeyedReference keyedRef = (KeyedReference) oldKeyedRefVector.elementAt(i);
						String tModelKey = keyedRef.getTModelKey();

						if (tModelKey.equalsIgnoreCase("uuid:" + CATEGORY_TMODEL_KEY))
						{
							functionalModelReferenceAnnotations.add(keyedRef.getKeyValue());
						}
						else if (tModelKey.equalsIgnoreCase("uuid:" + INPUT_TMODEL_KEY))
						{
							inputModelReferenceAnnotations.add(keyedRef.getKeyValue());
						}
						else if (tModelKey.equalsIgnoreCase("uuid:" + OUTPUT_TMODEL_KEY))
						{
							outputModelReferenceAnnotations.add(keyedRef.getKeyValue());
						}
					}
				}
			}				

			// Create an object to group all useful information
			SemanticProfileAnnotationCollection annotations = new SemanticProfileAnnotationCollection(
					serviceName, 
					functionalModelReferenceAnnotations,
					inputModelReferenceAnnotations, 
					outputModelReferenceAnnotations);

			// Create a matchmaker to construct an AFP concept, store it in
			// the ontology, and classify it against all RFPs contained in it
			PublicationTimeMatchmaker matchmaker = new PublicationTimeMatchmaker();

			// Get the URIs of all RFPs that the AFP can satisfy
			// (i.e. get all requests that can be readily satisfied by the new advertisement)
			SemanticProfileURICollection matchingRFPs = null;
			try
			{
				matchingRFPs = matchmaker.createAFPAndGetMatchingRFPs(
						srkbOntologyPhysicalURI,
						annotations,
						serviceKey.replace("uuid:", ""));
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


			// No point in going on and updating the advertisement if
			// there is no subsumption-indexing information to add to it
			if (matchingRFPs != null)
			{
				// Copy the keyedReferences vector from the old
				// category bag into a new vector
				Vector<KeyedReference> newKeyedRefVector = oldKeyedRefVector;				
				
				// Create and add a keyedReference to the new keyed
				// references vector for every RFP that subsumes the AFP
				List<String> superClassURIs = matchingRFPs.getProfileURIs();
				for (int i = 0; i < superClassURIs.size(); i++)
				{
					newKeyedRefVector.add(generateKeyedReference(INDEXING_TMODEL_KEY,
							superClassURIs.get(i)));
				}

				// Copy the old category bag into a new one
				CategoryBag newCategoryBag = oldCategoryBag;

				// Replace the old keyedReferences vector with the new
				// (enriched) one
				newCategoryBag.setKeyedReferenceVector(newKeyedRefVector);

				// Replace the old categoryBag with the new one (the rest of
				// the attributes in the businessService object should remain
				// unchanged)
				businessService.setCategoryBag(newCategoryBag);

				// Set the key equal to that of the previously saved service record, 
				// or a new service record will be created
				businessService.setServiceKey(serviceKey);
				
				// Empty the businessServiceVector (i.e. remove the old
				// businessService object)
				businessServiceVector.clear();

				// Add the old businessService again, which now contains the new
				// categoryBag
				businessServiceVector.addElement(businessService);

				// Save the service
				try
				{
					serviceDetail = proxy.save_service(authToken, businessServiceVector);
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
							throw new SemanticRegistryException(
									"UDDI exception with error number: " + r.getErrno());
						}
					}
					else
					{
						System.out.println(
								"The UDDI server reported an internal error but did not provide a Disposition Report " +
								"to explain its cause. The problem resulted while trying to invoke the 'save_service' " +
								"operation, and the cause of the problem may be in the data provided (e.g. some parameter " +
								"value exceeding max character length), in a failure to communicate with the UDDI server, " +
								"or in a failure to communicate with the database that the UDDI server relies on.");
						throw new SemanticRegistryCommunicationException(
								"The UDDI server reported an internal error but did not provide a Disposition Report " +
								"to explain its cause. The problem resulted while trying to invoke the 'save_service' " +
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
					throw new SemanticRegistryException(
							"An exception occured for unspecified reasons");
				}

			} // end if (matchingRFPs != null)

			// Return the service key
			return serviceKey;


		} // endif input is well-formed
		else
		{
			System.out.println("SemanticRegistryMalformedInputException occured!");
			if (!InputValidator.isAuthenticationTokenWellFormed(authToken)) 
				throw new SemanticRegistryMalformedInputException("Input parameter value 'authToken' is malformed");
			if (!InputValidator.isURLWellFormed(serviceSAWSDLURL)) 
				throw new SemanticRegistryMalformedInputException("Input parameter value 'serviceSAWSDLURL' is malformed");
			if (!InputValidator.isUUIDKeyWellFormed(serviceProviderUUID)) 
				throw new SemanticRegistryMalformedInputException("Input parameter value 'serviceProviderUUID' is malformed");
			if (!InputValidator.isTextParameterWellFormed(serviceName)) 
				throw new SemanticRegistryMalformedInputException("Input parameter value 'serviceName' is malformed");
			if (!InputValidator.isTextParameterWellFormed(serviceFreeTextDescription)) 
				throw new SemanticRegistryMalformedInputException("Input parameter value 'serviceFreeTextDescription' is malformed");
			if (hasCategoryAnnotationURI == null || hasCategoryAnnotationURI.length() == 0)
				throw new SemanticRegistryMalformedInputException("The semantic annotations provided for the service do not " +
						"include any categorisation value. A service must be categorised by exactly one taxonomy concept.");
			if (!InputValidator.isURIWellFormed(hasCategoryAnnotationURI))
				throw new SemanticRegistryMalformedInputException("Input parameter value 'hasCategoryAnnotationURI' is malformed");
			throw new SemanticRegistryMalformedInputException("One or more input parameter values are malformed");
		}
	}
	
	
	
	
	
	
	
	/**
	 * Create a new service advertisement. In UDDI, a service is represented as
	 * a businessService element. The advertisement includes the information
	 * provided by the client and some additional information that is generated
	 * by parsing the specified SAWSDL document and creating an Advertisement
	 * Functional Profile (AFP), performing matchmaking, and discovering Request
	 * Functional Profiles (RFP) that can be readily satisfied by the new
	 * service advertisement. Before proceeding, the values of all provided
	 * input parameters are validated.
	 * 
	 * @param authToken
	 * @param serviceSAWSDLURL
	 * @param serviceProviderUUID
	 * @param serviceName
	 * @param serviceFreeTextDescription
	 * @return
	 * @throws SemanticRegistryMalformedInputException
	 * @throws SemanticRegistryAuthException
	 * @throws SemanticRegistryCommunicationException
	 * @throws SemanticRegistryMatchmakingException
	 * @throws SemanticRegistryException
	 * @throws SemanticRegistryConfigurationException 
	 */
	public String addService(
			String authToken, 
			String serviceSAWSDLURL,
			String serviceProviderUUID, 
			String serviceName,
			String serviceFreeTextDescription)
			
	throws SemanticRegistryMalformedInputException,
			SemanticRegistryAuthException,
			SemanticRegistryCommunicationException, 
			SemanticRegistryMatchmakingException, 
			SemanticRegistryException, SemanticRegistryConfigurationException 
	{
		// Remove leading and trailing whitespaces from values to be used as keys
		if (authToken != null) authToken = authToken.trim();
		if (serviceProviderUUID != null) serviceProviderUUID = serviceProviderUUID.trim();
		
		if ( InputValidator.isAuthenticationTokenWellFormed(authToken) 
				&& InputValidator.isUUIDKeyWellFormed(serviceProviderUUID)
				&& InputValidator.isURLWellFormedAndResolvable(serviceSAWSDLURL) )
				{
					//The authToken key has been asserted to be valid, but should also have the proper prefix
					if (authToken.length() == 36) authToken = "authToken:".concat(authToken);

					// If the client has provided a preferred name make sure it's not too long
					if (serviceName != null && serviceName.length() > 0)
					{
						if (serviceName.length() > 256) 
							throw new SemanticRegistryMalformedInputException("Input parameter value 'serviceName' is too long");
					}

					// If the client has provided a service description make sure it's not too long
					if (serviceFreeTextDescription != null && serviceFreeTextDescription.length() > 0)
					{
						if (serviceFreeTextDescription.length() > 256) 
							throw new SemanticRegistryMalformedInputException("Input parameter value 'serviceFreeTextDescription' is too long");
					}

					// Make sure that the specified UUID of the Service Provider 
					// that offers this service is known to the UDDI server by 
					// trying to retrieve a business detail object for the specified key
					BusinessDetail businessDetail;
					try
					{
						businessDetail = proxy.get_businessDetail(serviceProviderUUID);

						// Get the BusinessEntity vector from the returned result
						Vector businessEntityVector = businessDetail.getBusinessEntityVector();
						
						// If no match was found for that key...
						if (businessEntityVector == null || businessEntityVector.size() == 0) 
							throw new SemanticRegistryMalformedInputException("The UUID key specified in the input parameter " +
								"value 'serviceProviderUUID' does not match any known Service Provider UUID keys");
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

								if (r.getErrInfo().getErrCode().equals("E_authTokenRequired"))
								{
									System.out.println("E_authTokenRequired: (10120) The authentication token is invalid");
									throw new SemanticRegistryMalformedInputException("The authentication token is invalid");
								}
								if (r.getErrInfo().getErrCode().equals("E_authTokenExpired"))
								{
									System.out.println("E_invalidKeyPassed: (10110) The authentication token has timed out");
									throw new SemanticRegistryMalformedInputException("The authentication token has timed out");
								}
								if (r.getErrInfo().getErrCode().equals("E_invalidKeyPassed"))
								{
									System.out.println("E_invalidKeyPassed: (10210) The uuid_key did not match any known key values");
									throw new SemanticRegistryMalformedInputException("The UUID key specified in the input parameter " +
									"value 'serviceProviderUUID' does not match any known Service Provider UUID keys");
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
						throw new SemanticRegistryCommunicationException("Problem communicating with the UDDI server");
					}
					catch (Exception e)
					{
						System.out.println("SemanticRegistryException occured!");
						throw new SemanticRegistryException("An exception occured for unspecified reasons");
					}

					
					
					// Create a service advertisement java bean using: 
					// (i) information provided (serviceProviderUUID, serviceName, serviceFreeTextDescription) and 
					// (ii) modelReference URIs parsed from the SAWSDL document in the serviceSAWSDLURL text parameter 
					// ... but with a keyedReferences vector in the categoryBag that doesn't contain subsumption information
					UDDIServiceAdvertisement adv = createServiceAdvertisementFromSAWSDL(
							serviceSAWSDLURL, 
							serviceProviderUUID, 
							serviceName, 
							serviceFreeTextDescription);
					
					// Create a vector for the businessService entities to be published
					Vector<BusinessService> businessServiceVector = new Vector<BusinessService>();
					
					// Create a new businessService entity
					BusinessService businessService = new BusinessService();
					
					// Set the business key of the service provider
					businessService.setBusinessKey(adv.getServiceProviderUUID());
					
					// Set default businessService name
					businessService.setDefaultNameString(adv.getServiceName(), "en");
					
					// Set default businessService description
					businessService.setDefaultDescriptionString(adv.getServiceFreeTextDescription());
					
					// Add the categoryBag to the businessService
					businessService.setCategoryBag(adv.getServiceCategoryBag());
					
					// Add the businessService to the vector
					businessServiceVector.addElement(businessService);
								
					// Create a ServiceDetail to store the outcome of the save_service call
					ServiceDetail serviceDetail = null;
					
					try
					{
						// Save the service and retrieve a serviceDetail message
						serviceDetail = proxy.save_service(authToken, businessServiceVector);
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
								
								if (r.getErrInfo().getErrCode().equals("E_authTokenRequired"))
								{
									System.out.println("E_authTokenRequired: (10120) The authentication token is invalid");
									throw new SemanticRegistryAuthException("The authentication token is invalid");
								}
								if (r.getErrInfo().getErrCode().equals("E_authTokenExpired"))
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
							System.out.println(
									"The UDDI server reported an internal error but did not provide a Disposition Report " +
									"to explain its cause. The problem resulted while trying to invoke the 'save_service' " +
									"operation, and the cause of the problem may be in the data provided (e.g. some parameter " +
									"value exceeding max character length), in a failure to communicate with the UDDI server, " +
									"or in a failure to communicate with the database that the UDDI server relies on.");
							throw new SemanticRegistryCommunicationException(
									"The UDDI server reported an internal error but did not provide a Disposition Report " +
									"to explain its cause. The problem resulted while trying to invoke the 'save_service' " +
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
					
					
					
					// If all goes well: i.e. no exception occurs 
					// and the service advertisement gets saved
					BusinessService returnedBusinessService = null;
					String serviceKey = null; 
					
					// Get the first (and logically only) business service entity from the returned serviceDetail object
					returnedBusinessService = (BusinessService) serviceDetail.getBusinessServiceVector().elementAt(0);
					
					// Get the Business Service key
					serviceKey = returnedBusinessService.getServiceKey();
					
					
					
					// Create a new categoryBag (which will shortly include subsumption 
					// indexing information, and replace the old categoryBag in the saved advertisement
					
					// Get the old category bag
					CategoryBag oldCategoryBag = adv.getServiceCategoryBag();
					
					// Get the old keyedReferences vector
					Vector<KeyedReference> oldKeyedRefVector = oldCategoryBag.getKeyedReferenceVector();
					
					// Create lists to hold the modelReference annotations that will be read from the categoryBag
					List<String> functionalModelReferenceAnnotations = new ArrayList<String>();
					List<String> inputModelReferenceAnnotations = new ArrayList<String>();
					List<String> outputModelReferenceAnnotations = new ArrayList<String>();
					
					// Populate the lists
					if (oldKeyedRefVector != null)
					{
						int vectorSize = oldKeyedRefVector.size();
						if (vectorSize > 0)
						{
							// For every KeyedReference
							for (int i = 0; i < vectorSize; i++)
							{
								KeyedReference keyedRef = (KeyedReference) oldKeyedRefVector.elementAt(i);
								String tModelKey = keyedRef.getTModelKey();

								if (tModelKey.equalsIgnoreCase("uuid:" + CATEGORY_TMODEL_KEY))
								{
									functionalModelReferenceAnnotations.add(keyedRef.getKeyValue());
								}
								else if (tModelKey.equalsIgnoreCase("uuid:" + INPUT_TMODEL_KEY))
								{
									inputModelReferenceAnnotations.add(keyedRef.getKeyValue());
								}
								else if (tModelKey.equalsIgnoreCase("uuid:" + OUTPUT_TMODEL_KEY))
								{
									outputModelReferenceAnnotations.add(keyedRef.getKeyValue());
								}
							}
						}
					}				
					
					// Create an object to group all useful information
					SemanticProfileAnnotationCollection annotations = new SemanticProfileAnnotationCollection(
							serviceName,
							functionalModelReferenceAnnotations,
							inputModelReferenceAnnotations, 
							outputModelReferenceAnnotations);
					
					// Create a matchmaker that will construct an AFP class, store it in 
					// the ontology, and classify it against all RFPs contained in it
					PublicationTimeMatchmaker matchmaker = new PublicationTimeMatchmaker();
					
					// Get the URIs of all RFPs that the AFP can match 
					// (i.e. get all requests that can be readily satisfied by the new advertisement)
					SemanticProfileURICollection matchingRFPs = null;
					try
					{
						matchingRFPs = matchmaker.createAFPAndGetMatchingRFPs(
								srkbOntologyPhysicalURI, 
								annotations, 
								serviceKey.replace("uuid:", ""));
					}
					catch (SemanticRegistryMatchmakingException e)
					{
						if (e.getMessage() == null || e.getMessage().length() == 0)
							throw new SemanticRegistryMatchmakingException("Problem occured during the semantic classification of the advertisement");
						else
							throw new SemanticRegistryMatchmakingException(e.getMessage());
					}
					catch (SemanticRegistryConfigurationException e)
					{
						if (e.getMessage() == null || e.getMessage().length() == 0)
							throw new SemanticRegistryConfigurationException("Problem occured during the semantic classification of the advertisement");
						else
							throw new SemanticRegistryConfigurationException(e.getMessage());
					}
					
					// No point in going on and updating the advertisement if 
					// there is no subsumption-indexing information to add to it
					if (matchingRFPs != null)
					{
						// Copy the keyedReferences vector from the old category bag into a new vector
						Vector<KeyedReference> newKeyedRefVector = oldKeyedRefVector;
						
						// Create and add a keyedReference to the new keyed 
						// references vector for every RFP that subsumes the AFP
						List<String> superClassURIs = matchingRFPs.getProfileURIs();
						for (int i = 0; i < superClassURIs.size(); i++)
						{
							newKeyedRefVector.add(generateKeyedReference(INDEXING_TMODEL_KEY, superClassURIs.get(i)));
						}

						// Copy the old category bag into a new one
						CategoryBag newCategoryBag = oldCategoryBag;
						
						// Replace the old keyedReferences vector with the new (enriched) one
						newCategoryBag.setKeyedReferenceVector(newKeyedRefVector);
						
						// Replace the old categoryBag with the new one (the rest of 
						// the attributes in the businessService object should remain unchanged)
						businessService.setCategoryBag(newCategoryBag);
						
						// Set the key equal to that of the previously saved service record, 
						// or a new service record will be created
						businessService.setServiceKey(serviceKey);
						
						// Empty the businessServiceVector (i.e. remove the old businessService object)
						businessServiceVector.clear();
						
						// Add the old businessService again, which now contains the new categoryBag 
						businessServiceVector.addElement(businessService);
						
						// Save the service
						try
						{	 
							serviceDetail = proxy.save_service(authToken, businessServiceVector);
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
									
									if (r.getErrInfo().getErrCode().equals("E_authTokenRequired"))
									{
										System.out.println("E_authTokenRequired: (10120) The authentication token is invalid");
										throw new SemanticRegistryAuthException("The authentication token is invalid");
									}
									if (r.getErrInfo().getErrCode().equals("E_authTokenExpired"))
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
								System.out.println(
										"The UDDI server reported an internal error but did not provide a Disposition Report " +
										"to explain its cause. The problem resulted while trying to invoke the 'save_service' " +
										"operation, and the cause of the problem may be in the data provided (e.g. some parameter " +
										"value exceeding max character length), in a failure to communicate with the UDDI server, " +
										"or in a failure to communicate with the database that the UDDI server relies on.");
								throw new SemanticRegistryCommunicationException(
										"The UDDI server reported an internal error but did not provide a Disposition Report " +
										"to explain its cause. The problem resulted while trying to invoke the 'save_service' " +
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
					
					}// end if (matchingRFPs != null)
					
					
					// Return the service key
					return serviceKey;
					
					
				} // endif input is well-formed
				else
				{
					System.out.println("SemanticRegistryMalformedInputException occured!");
					if (!InputValidator.isAuthenticationTokenWellFormed(authToken))
						throw new SemanticRegistryMalformedInputException("Input parameter value 'authToken' is malformed");
					if (!InputValidator.isURLWellFormedAndResolvable(serviceSAWSDLURL))
						throw new SemanticRegistryMalformedInputException("Input parameter value 'serviceSAWSDLURL' is malformed");					
					if (!InputValidator.isUUIDKeyWellFormed(serviceProviderUUID))
						throw new SemanticRegistryMalformedInputException("Input parameter value 'serviceProviderUUID' is malformed");
					if (!InputValidator.isTextParameterWellFormed(serviceName))
						throw new SemanticRegistryMalformedInputException("Input parameter value 'serviceName' is malformed");
					if (!InputValidator.isTextParameterWellFormed(serviceFreeTextDescription))
						throw new SemanticRegistryMalformedInputException("Input parameter value 'serviceFreeTextDescription' is malformed");
					throw new SemanticRegistryMalformedInputException("One or more input parameter values are malformed");
				}
	}
	
	
	
	
	
		
	
	/**
	 * Deletes a service advertisement (businessService element) from the UDDI
	 * server and the corresponding AFP concept from the SRKB ontology. Before
	 * proceeding, the values of all provided input parameters are validated.
	 * 
	 * @param authToken
	 * @param serviceUUID
	 * @return
	 * @throws SemanticRegistryMalformedInputException
	 * @throws SemanticRegistryAuthException
	 * @throws SemanticRegistryCommunicationException
	 * @throws SemanticRegistryConfigurationException 
	 * @throws SemanticRegistryException
	 */
	public Boolean removeService(
			String authToken, 
			String serviceUUID)
			
	throws SemanticRegistryMalformedInputException,
			SemanticRegistryAuthException,
			SemanticRegistryCommunicationException, 
			SemanticRegistryException, SemanticRegistryConfigurationException
	{
		// Remove leading and trailing whitespaces from values to be used as keys
		authToken = authToken.trim();
		serviceUUID = serviceUUID.trim();
		
		if (InputValidator.isAuthenticationTokenWellFormed(authToken)
				&& InputValidator.isUUIDKeyWellFormed(serviceUUID))
			{
				//The authToken key has been asserted to be valid, but should also have the proper prefix
				if (authToken.length() == 36) authToken = "authToken:".concat(authToken);
			
				try
				{
					proxy.delete_service(authToken, serviceUUID);
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
							
							if (r.getErrInfo().getErrCode().equals("E_authTokenRequired"))
							{
								System.out.println("E_authTokenRequired: (10120) The authentication token is invalid");
								throw new SemanticRegistryAuthException("The authentication token is invalid");
							}
							if (r.getErrInfo().getErrCode().equals("E_authTokenExpired"))
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
						System.out.println(
								"The UDDI server reported an internal error but did not provide a Disposition Report " +
								"to explain its cause. The problem resulted while trying to invoke the 'delete_service' " +
								"operation, and the cause of the problem may be in the data provided (e.g. some parameter " +
								"value exceeding max character length), in a failure to communicate with the UDDI server, " +
								"or in a failure to communicate with the database that the UDDI server relies on.");
						throw new SemanticRegistryCommunicationException(
								"The UDDI server reported an internal error but did not provide a Disposition Report " +
								"to explain its cause. The problem resulted while trying to invoke the 'delete_service' " +
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
				
				try
				{
					deleteAFP(serviceUUID, srkbOntologyPhysicalURI);
				}
				catch (OWLOntologyCreationException e)
				{
					if (e.getMessage() == null || e.getMessage().length() == 0)
						throw new SemanticRegistryConfigurationException(
								"The service was removed from the UDDI server, but an exception occured while trying to " +
								"remove its corresponding AFP concept from the SRKB ontology (OWLOntologyCreationException)");
					else
						throw new SemanticRegistryConfigurationException(e.getMessage());
				}
				catch (OWLOntologyChangeException e)
				{
					if (e.getMessage() == null || e.getMessage().length() == 0)
						throw new SemanticRegistryConfigurationException(
								"The service was removed from the UDDI server, but an exception occured while trying to " +
								"remove its corresponding AFP concept from the SRKB ontology (OWLOntologyChangeException)");
					else
						throw new SemanticRegistryConfigurationException(e.getMessage());
				}
				catch (OWLOntologyStorageException e)
				{
					if (e.getMessage() == null || e.getMessage().length() == 0)
						throw new SemanticRegistryConfigurationException(
								"The service was removed from the UDDI server, but an exception occured while trying to " +
								"remove its corresponding AFP concept from the SRKB ontology (OWLOntologyStorageException)");
					else
						throw new SemanticRegistryConfigurationException(e.getMessage());
				}
				catch (Exception e)
				{
					if (e.getMessage() == null || e.getMessage().length() == 0)
						throw new SemanticRegistryConfigurationException(
								"The service was removed from the UDDI server, but an exception occured while trying to " +
								"remove its corresponding AFP concept from the SRKB ontology (Exception)");
					else
						throw new SemanticRegistryConfigurationException(e.getMessage());
				}
				
				System.out.println("Advertisement " + serviceUUID + " removed successfully from the SRKB ontology");

				// If all goes well
				return true;

			} // endif input is well-formed
			else
			{
				System.out.println("SemanticRegistryMalformedInputException occured!");
				if (!InputValidator.isAuthenticationTokenWellFormed(authToken))
					throw new SemanticRegistryMalformedInputException("Input parameter value 'authToken' is malformed");
				if (!InputValidator.isUUIDKeyWellFormed(serviceUUID))
					throw new SemanticRegistryMalformedInputException("Input parameter value 'serviceUUID' is malformed");
				throw new SemanticRegistryMalformedInputException("One or more input parameter values are malformed");
			}
	}
	
	
	
	
	
	
			
	/**
	 * Modifies a service advertisement. In UDDI, a service is represented as a
	 * businessService element. Before proceeding, the values of all provided
	 * input parameters are validated.
	 * 
	 * @param authToken
	 * @param serviceUUID
	 * @param serviceName
	 * @param serviceFreeTextDescription
	 * @param serviceProviderUUID
	 * @return
	 * @throws SemanticRegistryMalformedInputException
	 * @throws SemanticRegistryAuthException
	 * @throws SemanticRegistryCommunicationException
	 * @throws SemanticRegistryException
	 */
	public Boolean modifyService(
			String authToken, 
			String serviceUUID,
			String serviceName, 
			String serviceFreeTextDescription,
			String serviceProviderUUID)
			
	throws SemanticRegistryMalformedInputException,
			SemanticRegistryAuthException,
			SemanticRegistryCommunicationException, 
			SemanticRegistryException
	{
		// CAUTION: serviceName, serviceFreeTextDescription and serviceProviderUUID 
		// are declared as nillable so they may be null, empty, or full
		
		// Remove leading and trailing whitespaces from values to be used as keys
		authToken = authToken.trim();
		serviceUUID = serviceUUID.trim();
		
		// This is a nillable parameter, so caution is needed
		if (serviceProviderUUID != null) serviceProviderUUID = serviceProviderUUID.trim();
		
		// Check if nothing needs to be updated
		if (	(serviceProviderUUID == null || serviceProviderUUID.length() == 0) &&
				(serviceName == null || serviceName.length() == 0) &&
				(serviceFreeTextDescription == null || serviceFreeTextDescription.length() == 0)  )
		{
			System.out.println("No modification requirements have been specified " +
					"(Input parameters 'serviceProviderUUID', 'serviceName' and 'serviceFreeTextDescription' are all empty)");
			throw new SemanticRegistryMalformedInputException(
					"Nothing to modify: Input parameters 'serviceProviderUUID', 'serviceName' and 'serviceFreeTextDescription' are all empty");
		}
		
		if (InputValidator.isAuthenticationTokenWellFormed(authToken) 
				&& InputValidator.isUUIDKeyWellFormed(serviceUUID))
		{
			//The authToken key has been asserted to be valid, but should also have the proper prefix
			if (authToken.length() == 36) authToken = "authToken:".concat(authToken);
			
			// Create a vector for the businessService entities to be published
			Vector<BusinessService> businessServiceVector = new Vector<BusinessService>();

			// Create a new businessService entity
			BusinessService newService = new BusinessService();
			
			try
			{
				// Set the new key equal to the old (or else a new service will be created)
				newService.setServiceKey(serviceUUID);
				
				// Get the existing service detail from the registry based on the key
				ServiceDetail serviceDetail = proxy.get_serviceDetail(serviceUUID);

				// Get the first (and logically only) business service entity from the returned serviceDetail object
				BusinessService oldService = (BusinessService) serviceDetail.getBusinessServiceVector().elementAt(0);
				
				// Check if the serviceName needs to be updated
				if (serviceName == null || serviceName.length() == 0)
				{
					// Retrieve the old serviceName and reuse it
					newService.setDefaultNameString(oldService.getDefaultNameString(),"en");	
				}
				else
				{
					if (serviceName.length() < 256)
						newService.setDefaultNameString(serviceName, "en");
					else
						throw new SemanticRegistryMalformedInputException("Input parameter value 'serviceName' is too long");
				}

				// Check if the serviceFreeTextDescription needs to be updated
				if (serviceFreeTextDescription == null || serviceFreeTextDescription.length() == 0)
				{
					// Retrieve the old serviceProviderFreeTextDescription and reuse it
					newService.setDefaultDescriptionString(oldService.getDefaultDescriptionString());					
}
				else
				{
					if (serviceFreeTextDescription.length() < 256)
						newService.setDefaultDescriptionString(serviceFreeTextDescription);
					else 
						throw new SemanticRegistryMalformedInputException("Input parameter value 'serviceFreeTextDescription' is too long");
				}
				
				// Check if the serviceProviderUUID needs to be updated
				if (serviceProviderUUID == null || serviceProviderUUID.length() == 0)
				{
					// Retrieve the old serviceProviderUUID and reuse it
					newService.setBusinessKey(oldService.getBusinessKey());
				}
				else
				{
					if (InputValidator.isUUIDKeyWellFormed(serviceProviderUUID))
						newService.setBusinessKey(serviceProviderUUID);
					else 
						throw new SemanticRegistryMalformedInputException("Input parameter value 'serviceProviderUUID' is malformed");
				}
				
				// Retrieve the old categoryBag and reuse it
				newService.setCategoryBag(oldService.getCategoryBag());	
				
				// Add the modified business entity to the vector
				businessServiceVector.addElement(newService);

				// Save the modified business entity
				proxy.save_service(authToken, businessServiceVector);
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
						
						if (r.getErrInfo().getErrCode().equals("E_authTokenRequired"))
						{
							System.out.println("E_authTokenRequired: (10120) The authentication token is invalid");
							throw new SemanticRegistryAuthException("The authentication token is invalid");
						}
						if (r.getErrInfo().getErrCode().equals("E_authTokenExpired"))
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
					System.out.println(
							"The UDDI server reported an internal error but did not provide a Disposition Report " +
							"to explain its cause. The problem resulted while trying to invoke the 'save_service' " +
							"operation, and the cause of the problem may be in the data provided (e.g. some parameter " +
							"value exceeding max character length), in a failure to communicate with the UDDI server, " +
							"or in a failure to communicate with the database that the UDDI server relies on.");
					throw new SemanticRegistryCommunicationException(
							"The UDDI server reported an internal error but did not provide a Disposition Report " +
							"to explain its cause. The problem resulted while trying to invoke the 'save_service' " +
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
			return true;

		} // endif input is well-formed
		else
		{
			System.out.println("SemanticRegistryMalformedInputException occured!");
			if (!InputValidator.isAuthenticationTokenWellFormed(authToken))
				throw new SemanticRegistryMalformedInputException("Input parameter value 'authToken' is malformed");
			if (!InputValidator.isUUIDKeyWellFormed(serviceUUID))
				throw new SemanticRegistryMalformedInputException("Input parameter value 'serviceUUID' is malformed");
			throw new SemanticRegistryMalformedInputException("One or more input parameter values are malformed");
		}
	}
	
	
	
	
	
	
		
	/**
	 * Creates and returns a UDDIServiceAdvertisement object by combining the
	 * information provided (serviceName, serviceFreeTextDescription,
	 * serviceProviderUUID) and by parsing the SAWSDL document found in the
	 * specified sawsdlURL location to extract category, input, and output
	 * annotations
	 * 
	 * @param sawsdlURL
	 * @param serviceProviderUUID
	 * @param serviceName
	 * @param serviceFreeTextDescription
	 * @return
	 * @throws SemanticRegistryMalformedInputException
	 * @throws SemanticRegistryException
	 */
	private UDDIServiceAdvertisement createServiceAdvertisementFromSAWSDL(
			String sawsdlURL,
			String serviceProviderUUID, 
			String serviceName, 
			String serviceFreeTextDescription) 
	
	throws SemanticRegistryMalformedInputException,
			SemanticRegistryException
	{
		// Extract information from SAWSDL document
		SAWSDLDocumentParser parser = new SAWSDLDocumentParser();
		SemanticProfileAnnotationCollection annotations = parser.extractAnnotationsFromSAWSDLDocument(parser.getSAWSDLDefinition(sawsdlURL));
	
		// Make sure that there is at least one category annotation for the service
		// (by convention in the FUSION project, a service must be categorised)
		if ( annotations.getFunctionalModelReferenceAnnotationURIs() == null || 
				annotations.getFunctionalModelReferenceAnnotationURIs().size() == 0 )
			throw new SemanticRegistryMalformedInputException(
					"The semantic annotations provided for the service do not " +
					"include any categorisation value. A service must be " +
					"categorised by exactly one taxonomy concept.");
		
		// Make sure that there is exactly one category annotation for the service
		// (by convention in FUSION, a service must be categorised exactly once)
		if ( annotations.getFunctionalModelReferenceAnnotationURIs().size() > 1 )
			throw new SemanticRegistryMalformedInputException(
					"The semantic annotations provided for the service specify " +
					"more than one categorisation values. A service must be " +
					"categorised by exactly one taxonomy concept.");
				
		// Make sure that the category annotation for the service is a valid URI
		if ( !InputValidator.isURIWellFormed(annotations.getFunctionalModelReferenceAnnotationURIs().get(0)))
			throw new SemanticRegistryMalformedInputException(
					"The category annotation that was retrieved from the SAWSDL document (" + 
					annotations.getFunctionalModelReferenceAnnotationURIs().get(0) +
					") is not a valid URI or could not be parsed correctly.");
		
		// Make sure that there is at least one input or one output for the service
		// (publishing a service with no inputs and no outputs doesn't make sense)
		if ( (annotations.getInputModelReferenceAnnotationURIs() == null || 
			  annotations.getInputModelReferenceAnnotationURIs().size() == 0) 
			  && 
			 (annotations.getOutputModelReferenceAnnotationURIs() == null ||
			  annotations.getOutputModelReferenceAnnotationURIs().size() == 0 ) )
			
			throw new SemanticRegistryMalformedInputException(
					"No semantic annotations have been provided for the service's " +
					"inputs and outputs. A service must have at least one input " +
					"or produce at least one output (not necessarily both).");
		
		// Make sure that every input annotation for the service is a valid URI
		if (annotations.getInputModelReferenceAnnotationURIs() != null)
		{
			for (String inputAnnotationURI : annotations.getInputModelReferenceAnnotationURIs())
			{
				if ( !InputValidator.isURIWellFormed(inputAnnotationURI))
					throw new SemanticRegistryMalformedInputException(
							"The input annotation that was retrieved from " +
							"the SAWSDL document (" +  inputAnnotationURI +
							") is not a valid URI or could not be parsed correctly.");
			}
		}
		
		// Make sure that every output annotation for the service is a valid URI
		if (annotations.getOutputModelReferenceAnnotationURIs() != null)
		{
			for (String outputAnnotationURI : annotations.getOutputModelReferenceAnnotationURIs())
			{
				if ( !InputValidator.isURIWellFormed(outputAnnotationURI))
					throw new SemanticRegistryMalformedInputException(
							"The input annotation that was retrieved from " +
							"the SAWSDL document (" +  outputAnnotationURI +
							") is not a valid URI or could not be parsed correctly.");
			}
		}
		
		// If the client has not provided any preferred name use the one
		// that was extracted from the portType in the SAWSDL document
		if (serviceName == null || serviceName.length() == 0) serviceName = annotations.getServiceName();

		// Use the extracted and inferred information to create a category bag
		// holding all indexing information for the businessService to be published
		CategoryBag categoryBag = generateCategoryBag(sawsdlURL, annotations, null);
		
		// Check the serviceFreeTextDescription 
		if (serviceFreeTextDescription == null || serviceFreeTextDescription.length() == 0)
			serviceFreeTextDescription = "No description provided";
		
		// Create a UDDIServiceAdvertisement corresponding to the businessService
		// to be published
		UDDIServiceAdvertisement adv = new UDDIServiceAdvertisement(
				serviceProviderUUID,
				serviceName, 
				serviceFreeTextDescription, 
				categoryBag);

		return adv;
	}
	
	
	
	
	
	
	
	/**
	 * Creates and returns a UDDIServiceAdvertisement object by combining all
	 * the information provided (serviceName, serviceFreeTextDescription,
	 * serviceProviderUUID, sawsdlURL, hasCategoryAnnotationURI,
	 * hasInputAnnotationURIArray, and hasOutputAnnotationURIArray)
	 * 
	 * @param sawsdlURL
	 * @param serviceProviderUUID
	 * @param serviceName
	 * @param serviceFreeTextDescription
	 * @param hasCategoryAnnotationURI
	 * @param hasInputAnnotationURIArray
	 * @param hasOutputAnnotationURIArray
	 * @return
	 * @throws SemanticRegistryException
	 * @throws SemanticRegistryMalformedInputException
	 */
	private UDDIServiceAdvertisement createServiceAdvertisementWithoutSAWSDL(
			String sawsdlURL,
			String serviceProviderUUID, 
			String serviceName, 
			String serviceFreeTextDescription, 
			String hasCategoryAnnotationURI, 
			String[] hasInputAnnotationURIArray, 
			String[] hasOutputAnnotationURIArray) 
	
	throws SemanticRegistryMalformedInputException,
		SemanticRegistryException
	{		
		List<String> categoryModelReferenceAnnotationURIs = new ArrayList<String>();
		List<String> inputModelReferenceAnnotationURIs = new ArrayList<String>();
		List<String> outputModelReferenceAnnotationURIs = new ArrayList<String>();

		// Copy a single URI (hasCategoryAnnotationURI) to the list
		categoryModelReferenceAnnotationURIs.add(hasCategoryAnnotationURI);
		
		// Iterate through all URIs in the hasInputAnnotationURIArray and copy them to the list
		if (hasInputAnnotationURIArray != null)
		{
			for (int i = 0; i < hasInputAnnotationURIArray.length; i++)
			{
				inputModelReferenceAnnotationURIs.add(hasInputAnnotationURIArray[i]);
			}
		}
		
		// Iterate through all URIs in the hasOutputAnnotationURIArray and copy them to the list
		if (hasOutputAnnotationURIArray != null)
		{
			for (int i = 0; i < hasOutputAnnotationURIArray.length; i++)
			{
				outputModelReferenceAnnotationURIs.add(hasOutputAnnotationURIArray[i]);
			}
		}
		
		// Make sure that there is at least one input or one output for the service
		// (publishing a service with no inputs and no outputs doesn't make sense)
		if ( inputModelReferenceAnnotationURIs.size() == 0 && 
				outputModelReferenceAnnotationURIs.size() == 0 )
			throw new SemanticRegistryMalformedInputException(
					"The semantic annotations provided for the service declare " +
					"zero inputs and zero outputs. A service must have at " +
					"at least one input or produce at least one output.");
		
		// Create an object to hold all annotations useful for the CategoryBag
		SemanticProfileAnnotationCollection annotations = new SemanticProfileAnnotationCollection(
				serviceName, 
				categoryModelReferenceAnnotationURIs, 
				inputModelReferenceAnnotationURIs, 
				outputModelReferenceAnnotationURIs);

		// Use the extracted and inferred information to create a category bag
		// holding all indexing information for the businessService to be published
		CategoryBag categoryBag = generateCategoryBag(sawsdlURL, annotations, null);
		
		// Check the serviceFreeTextDescription 
		if (serviceFreeTextDescription == null || serviceFreeTextDescription.length() == 0)
			serviceFreeTextDescription = "No description provided";
		
		// Create a UDDIServiceAdvertisement corresponding to the businessService
		// to be published
		UDDIServiceAdvertisement adv = new UDDIServiceAdvertisement(
				serviceProviderUUID,
				serviceName, 
				serviceFreeTextDescription, 
				categoryBag);

		return adv;
	}
	
	
	
	
	
	
	
	/**
	 * Generates a UDDI CategoryBag to store all provided information in the
	 * UDDI BusinessService object that represents the service advertisement
	 * 
	 * @param sawsdlURL
	 * @param annotations
	 * @param subsumerProfileURIs
	 * @return
	 * @throws SemanticRegistryException
	 */
	private CategoryBag generateCategoryBag(
			String sawsdlURL,
			SemanticProfileAnnotationCollection annotations,
			SemanticProfileURICollection subsumerProfileURIs) 
	
	throws SemanticRegistryException
	{		
		// Create a categoryBag
		CategoryBag categoryBag = new CategoryBag();

		// Create a vector to hold all keyedReferences
		Vector<KeyedReference> krList = new Vector<KeyedReference>();

		// Create a temp keyedReference
		KeyedReference kr = null;
		
		
		if (sawsdlURL != null)
		{
			// Create and add a keyedReference for the SAWSDL document URL
			kr = generateKeyedReference(SAWSDL_TMODEL_KEY, sawsdlURL);
			krList.add(kr);
		}
		
		
		if (annotations != null)
		{
			// Create and add a keyedReference for every functional annotation
			List<String> functionalAnnotations = annotations.getFunctionalModelReferenceAnnotationURIs();
			for (int i = 0; i < functionalAnnotations.size(); i++)
			{
				kr = generateKeyedReference(CATEGORY_TMODEL_KEY, functionalAnnotations.get(i));
				krList.add(kr);
			}

			// Create and add a keyedReference for every input annotation
			List<String> inputAnnotations = annotations.getInputModelReferenceAnnotationURIs();
			for (int i = 0; i < inputAnnotations.size(); i++)
			{
				kr = generateKeyedReference(INPUT_TMODEL_KEY, inputAnnotations.get(i));
				krList.add(kr);
			}

			// Create and add a keyedReference for every output annotation
			List<String> outputAnnotations = annotations.getOutputModelReferenceAnnotationURIs();
			for (int i = 0; i < outputAnnotations.size(); i++)
			{
				kr = generateKeyedReference(OUTPUT_TMODEL_KEY, outputAnnotations.get(i));
				krList.add(kr);
			}
		}
		
		
		if (subsumerProfileURIs != null)
		{
			// Create and add a keyedReference for every FFE superclass that can
			// subsume the advertisement
			List<String> superClassURIs = subsumerProfileURIs.getProfileURIs();
			for (int i = 0; i < superClassURIs.size(); i++)
			{
				kr = generateKeyedReference(INDEXING_TMODEL_KEY, superClassURIs.get(i));
				krList.add(kr);
			}
		}
		
		
		// Add the keyedReferences vector to the categoryBag
		categoryBag.setKeyedReferenceVector(krList);

		return categoryBag;
	}
	
	
	
	
	
	
		
	/**
	 * Generates a UDDI KeyedReference to be included in a UDDI CategoryBag
	 * object
	 * 
	 * @param tModelKey
	 * @param keyValue
	 * @return
	 * @throws SemanticRegistryException
	 */
	private KeyedReference generateKeyedReference(
			String tModelKey,
			String keyValue) 
	
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
	 * Checks if all five Canonical TModels that are necessary for the Semantic
	 * Registry's operation have been properly registered with the UDDI server.
	 * 
	 * @throws SemanticRegistryException
	 */
	private void validateHealthOfCanonicalTModelDeployment() throws SemanticRegistryException
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
	public String[] getCanonicalTModelDetails() 
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
				//System.out.println("tModel key found: " + tModelKey );
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
	
	
	
	
	
	
	
	/**
	 * Loads the specified ontology file and removes the following named OWL
	 * classes: (i) the OWLClass of the AFP, (ii) the OWLClass of the
	 * InputDataSet with which the AFP is associated, and (iii), the OWLClass of
	 * the OutputDataSet with which the AFP is associated. The ontology is saved
	 * back in its original format (e.g. RDF/XML or OWL/XML). 
	 * 
	 * @param afpClassName the name of the AFP class to remove
	 * @param targetOntologyPhysicalURI the URI
	 * 
	 * @throws OWLOntologyCreationException if one of the ontologies could not
	 *             be loaded, or if the Functional Facet ontology failed the
	 *             validation check
	 * @throws OWLOntologyChangeException if the change could not be applied in
	 *             the OWLOntology model
	 * @throws OWLOntologyStorageException if the ontology file could not be
	 *             saved successfully
	 * @throws Exception if an AFP with the provided name does not exist in the
	 *             specified ontology
	 */
	public void deleteAFP(
		String afpClassName, 
		URI targetOntologyPhysicalURI) 
		
		throws 
		OWLOntologyCreationException, 
		OWLOntologyChangeException, 
		OWLOntologyStorageException, 
		Exception 
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
        
        URI taxonomyOntologyLogicalURI = getDefiningOntology("#TaxonomyEntity", importsClosure, factory).getURI();
		URI dataFacetOntologyLogicalURI = getDefiningOntology("#DataFacetEntity", importsClosure, factory).getURI();
		URI functionalFacetOntologyLogicalURI = getDefiningOntology("#FunctionalFacetEntity", importsClosure, factory).getURI();
		
		// Create a reference to an object representing the class of the
		// AFP, using the provided class name
		OWLClass afpClass = factory.getOWLClass(URI.create(srkbLogicalURI + "#" + afpClassName));
		
		// Check if a class by the same name (and thus by the same 
		// URI) is already referenced in the ontology. In this is 
		// not the case there is no point in continuing with execution.
		if (!srkbOntology.getReferencedClasses().contains(afpClass)) 
			throw new Exception("The specified ontology does not contain any reference to an AFP with the provided name");
		
		// Create an entity remover that will be asked to visit: 
		// 1. the OWLClass of the AFP 
		// 2. the OWLClass of the InputDataSet with which the AFP is associated
		// 3. the OWLClass of the OutputDataSet with which the AFP is associated
		OWLEntityRemover remover = new OWLEntityRemover(manager, Collections.singleton(srkbOntology));
				
		// Create a collector to pick up all named classes
		// referenced within the equivalent class axiom of the AFP
		OWLEntityCollector collector = new OWLEntityCollector();

		// Speed things up by asking the collector to collect only named classes
		collector.setCollectClasses(true);
		collector.setCollectDataProperties(false);
		collector.setCollectObjectProperties(false);
		collector.setCollectIndividuals(false);
		collector.setCollectDataTypes(false);
		
		// Get the equivalent class axiom of the AFP class. By convention
		// in the FUSION project, there should be only one equivalent
		// class axiom, so this loop should be executed only once
		for (OWLEquivalentClassesAxiom ax : srkbOntology.getEquivalentClassesAxioms(afpClass))
		{
			// System.out.println("Equivalent class axiom: " + ax.toString());

			// By convention in the FUSION project, there should be two
			// OWLDescription objects in the axiom: 1) the named AFP class,
			// and 2) the anonymous class defined as an intersection of
			// existential and universal restrictions on object properties
			// (i.e. the hasCategory, hasInput, and hasOutput
			// properties)
			for (OWLDescription desc : ax.getDescriptions())
			{
				// Send the collector only to the second component
				// of the axiom (i.e. only to the anonymous class)
				if (desc.isAnonymous())
				{
					// System.out.println("Collecting filler classes from
					// equivalent class axiom...");
					desc.accept(collector);
				}
			}
		}
		
		// Get the filler classes from the collector
		Set<OWLEntity> collectedFillerClasses = collector.getObjects();
					
		// By convention in the FUSION project, there should be three
		// classes left inside the collectedFillerClasses set:
		// 1) the filler class for the hasCategory property restriction,
		// 2) the filler class for the hasInput property restriction, and
		// 3) the filler class for the hasOutput property restriction
		
		// We will need to convert each retrieved OWLEntity to an OWLClass
		OWLClass fillerCls = null;
		
		// Create a reference to an object representing the InputDataSet class
		OWLClass inputDataSetRootClass = factory.getOWLClass(URI.create(functionalFacetOntologyLogicalURI + "#InputDataSet"));
		
		// Create a reference to an object representing the OutputDataSet class
		OWLClass outputDataSetRootClass = factory.getOWLClass(URI.create(functionalFacetOntologyLogicalURI + "#OutputDataSet"));
		
		// Determine the property corresponding to each of the three
		// fillers, by checking if a filler is an asserted subclass of
		// 1) inputDataSetRootClass, 2) outputDataSetRootClass,
		// or 3) none of the two (i.e. a taxonomy annotation)
		for (OWLEntity filler : collectedFillerClasses)
		{
			fillerCls = factory.getOWLClass(filler.getURI().normalize());
			// If the fillerCls is an asserted subclass of #InputDataSet in the given ontology...
			if (inputDataSetRootClass.getSubClasses(srkbOntology).contains((OWLDescription)fillerCls))
			{
				System.out.println("Removing " + fillerCls + " (" + afpClass + " hasInput " + filler.getURI().normalize() + ")" );
				fillerCls.accept(remover);
			}
			// If the fillerCls is an asserted subclass of #OutputDataSet in the given ontology...			
			if (outputDataSetRootClass.getSubClasses(srkbOntology).contains((OWLDescription)fillerCls))
			{
				System.out.println("Removing " + fillerCls + " (" + afpClass + " hasOutput " + filler.getURI().normalize() + ")" );
				fillerCls.accept(remover);
			}
		}
	
		// Finally, ask the AFP class to also accept a visit from the entity remover
		System.out.println("Removing " + afpClass);
		afpClass.accept(remover);

		// Get the changes necessary to remove the class from the ontology
		manager.applyChanges(remover.getChanges());

		// Store the updated ontology back to the file
		try
		{
			manager.saveOntology(srkbOntology);
			System.out.println("AFP (" + afpClassName + ") removed successfully from the ontology");
		}
		catch (UnknownOWLOntologyException e)
		{
			throw new Exception("Problem removing AFP (" + afpClassName + ") from the ontology file (" + targetOntologyPhysicalURI + ")");
		}
		catch (OWLOntologyStorageException e)
		{
			throw new OWLOntologyStorageException("Problem saving the updated ontology (" + targetOntologyPhysicalURI + ")");
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
	
	
}// end class
