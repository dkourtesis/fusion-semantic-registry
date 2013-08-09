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
 
 package org.seerc.fusion.sr.api;

import javax.xml.namespace.QName;

import org.apache.axis2.AxisFault;
import org.seerc.fusion.sr.api.xsd.DoKeywordSearchForServiceProvidersResponse;
import org.seerc.fusion.sr.api.xsd.DoKeywordSearchForServiceProvidersResponse_type0;
import org.seerc.fusion.sr.api.xsd.DoKeywordSearchForServicesResponse;
import org.seerc.fusion.sr.api.xsd.DoKeywordSearchForServicesResponse_type0;
import org.seerc.fusion.sr.api.xsd.DoSemanticSearchForServicesResponse;
import org.seerc.fusion.sr.api.xsd.DoSemanticSearchForServicesResponse_type0;
import org.seerc.fusion.sr.api.xsd.GetAllServiceProviderUUIDsResponse;
import org.seerc.fusion.sr.api.xsd.GetAllServiceProviderUUIDsResponse_type0;
import org.seerc.fusion.sr.api.xsd.GetAllServiceUUIDsResponse;
import org.seerc.fusion.sr.api.xsd.GetAllServiceUUIDsResponse_type0;
import org.seerc.fusion.sr.api.xsd.GetServiceDetailsResponse;
import org.seerc.fusion.sr.api.xsd.GetServiceProviderDetailsResponse;
import org.seerc.fusion.sr.core.DiscoveryHandler;
import org.seerc.fusion.sr.exceptions.SemanticRegistryCommunicationException;
import org.seerc.fusion.sr.exceptions.SemanticRegistryConfigurationException;
import org.seerc.fusion.sr.exceptions.SemanticRegistryException;
import org.seerc.fusion.sr.exceptions.SemanticRegistryMalformedInputException;
import org.seerc.fusion.sr.exceptions.SemanticRegistryMatchmakingException;
import org.seerc.fusion.sr.exceptions.SemanticRegistryNoMatchFoundException;

/**
 *  Semantic Registry Web App - Discovery Manager service
 *  @author Dimitrios Kourtesis, South-East European Research Centre (SEERC)
 */
public class DiscoveryManagerSkeleton
{
	/**
	 * Used for retrieving the list of UUIDs of all Service Providers (i.e.
	 * systems) registered with the UDDI Server. 
	 * 
	 * @return
	 * @throws AxisFault
	 */
	public org.seerc.fusion.sr.api.xsd.GetAllServiceProviderUUIDsResponse getAllServiceProviderUUIDs() 
	
	throws AxisFault
	{
		System.out.println("Invoked: getAllServiceProviderUUIDs");
		System.out.println("Inputs provided: No inputs are needed");

		// Create a controller for UDDI discovery operations
		DiscoveryHandler handler = null;
		try
		{
			handler = new DiscoveryHandler();
		}
		catch (SemanticRegistryConfigurationException e)
		{
			throw new AxisFault(new QName("http://www.seerc.org", e.getClass().getSimpleName(), "fusion"), e.getMessage(), e);
		}
		
		// Get the list of UUIDs to be stored inside the content object
		String[] itemList;
		try
		{
			itemList = handler.getAllServiceProviderUUIDs();
			
			// Create a response message object
			GetAllServiceProviderUUIDsResponse response = new GetAllServiceProviderUUIDsResponse();
			
			// Create a content object to be stored inside the response message
			GetAllServiceProviderUUIDsResponse_type0 listOfServiceProviderUUIDs = new GetAllServiceProviderUUIDsResponse_type0();
			
			// Add the returned list to the content object
			listOfServiceProviderUUIDs.setString(itemList);
			
			// Add the content object to the response message
			response.setGetAllServiceProviderUUIDsResponse(listOfServiceProviderUUIDs);
			
			System.out.println("Outputs provided: ");
			for (int i=0; i < itemList.length; i++) System.out.println( i+1 + ": " + itemList[i]);
			
			return response;
		}
		catch (SemanticRegistryMalformedInputException e)
		{
			throw new AxisFault(new QName("http://www.seerc.org", e.getClass().getSimpleName(), "fusion"), e.getMessage(), e);
		}
		catch (SemanticRegistryCommunicationException e)
		{
			throw new AxisFault(new QName("http://www.seerc.org", e.getClass().getSimpleName(), "fusion"), e.getMessage(), e);
		}
		catch (SemanticRegistryNoMatchFoundException e)
		{
			throw new AxisFault(new QName("http://www.seerc.org", e.getClass().getSimpleName(), "fusion"), e.getMessage(), e);
		}
		catch (SemanticRegistryException e)
		{
			throw new AxisFault(new QName("http://www.seerc.org", e.getClass().getSimpleName(), "fusion"), e.getMessage(), e);
		}
	}
	
	
	
	
	
	
	
	/**
	 * Used for retrieving a list of Service Provider systems having a textual
	 * description that matches the keyword provided as an input parameter. 
	 * 
	 * @param doKeywordSearchForServiceProvidersRequest
	 * @throws AxisFault
	 */
	public org.seerc.fusion.sr.api.xsd.DoKeywordSearchForServiceProvidersResponse doKeywordSearchForServiceProviders(
			org.seerc.fusion.sr.api.xsd.DoKeywordSearchForServiceProvidersRequest doKeywordSearchForServiceProvidersRequest) 
	
	throws AxisFault
	{	
		System.out.println("Invoked: doKeywordSearchForServiceProviders");
		System.out.println("Inputs provided: keyword (" + doKeywordSearchForServiceProvidersRequest.getKeyword() + ")");
		
		// Get the keyword from the request object
		String keyword = doKeywordSearchForServiceProvidersRequest.getKeyword();
		
		// Create a controller for UDDI discovery operations
		DiscoveryHandler handler = null;
		try
		{
			handler = new DiscoveryHandler();
		}
		catch (SemanticRegistryConfigurationException e)
		{
			throw new AxisFault(new QName("http://www.seerc.org", e.getClass().getSimpleName(), "fusion"), e.getMessage(), e);
		}
		
		// Get the list of UUIDs to be stored inside the content object
		String[] itemList;
		try
		{
			itemList = handler.doKeywordSearchForServiceProviders(keyword);
			
			// Create a response message object
			DoKeywordSearchForServiceProvidersResponse response = new DoKeywordSearchForServiceProvidersResponse();
			
			// Create a content object to be stored inside the response message
			DoKeywordSearchForServiceProvidersResponse_type0 listOfServiceProviderUUIDs = new DoKeywordSearchForServiceProvidersResponse_type0();
			
			// Add the returned list to the content object
			listOfServiceProviderUUIDs.setString(itemList);
			
			// Add the content object to the response message
			response.setDoKeywordSearchForServiceProvidersResponse(listOfServiceProviderUUIDs);
			
			System.out.println("Outputs provided: ");
			for (int i=0; i < itemList.length; i++) System.out.println( i+1 + ": " + itemList[i]);
			
			return response;
		}
		catch (SemanticRegistryMalformedInputException e)
		{
			throw new AxisFault(new QName("http://www.seerc.org", e.getClass().getSimpleName(), "fusion"), e.getMessage(), e);
		}
		catch (SemanticRegistryCommunicationException e)
		{
			throw new AxisFault(new QName("http://www.seerc.org", e.getClass().getSimpleName(), "fusion"), e.getMessage(), e);
		}
		catch (SemanticRegistryNoMatchFoundException e)
		{
			throw new AxisFault(new QName("http://www.seerc.org", e.getClass().getSimpleName(), "fusion"), e.getMessage(), e);
		}
		catch (SemanticRegistryException e)
		{
			throw new AxisFault(new QName("http://www.seerc.org", e.getClass().getSimpleName(), "fusion"), e.getMessage(), e);
		}
	}
	
	
	
	
	
	
	
	/**
	 * Used for retrieving information about a specific Service Provider system.
	 * The UUID identifier of the system must be provided in the parameters
	 * list. It returns the name and textual description associated with the
	 * system, and a list of UUIDs for all the services it exposes.
	 * 
	 * @param getServiceProviderDetailsRequest
	 * @throws AxisFault
	 */
	public org.seerc.fusion.sr.api.xsd.GetServiceProviderDetailsResponse getServiceProviderDetails(
			org.seerc.fusion.sr.api.xsd.GetServiceProviderDetailsRequest getServiceProviderDetailsRequest) 
	
	throws AxisFault
	{
		System.out.println("Invoked: getServiceProviderDetails");
		System.out.println("Inputs provided: serviceProviderUUID ("
				+ getServiceProviderDetailsRequest.getServiceProviderUUID() + ")");

		// Create a controller for UDDI discovery operations
		DiscoveryHandler handler = null;
		try
		{
			handler = new DiscoveryHandler();
		}
		catch (SemanticRegistryConfigurationException e)
		{
			throw new AxisFault(new QName("http://www.seerc.org", e.getClass().getSimpleName(), "fusion"), e.getMessage(), e);
		}

		// Create a response message object
		GetServiceProviderDetailsResponse response = new GetServiceProviderDetailsResponse();

		// Set the returned data as the content of the response message
		try
		{
			response = handler.getServiceProviderDetails(getServiceProviderDetailsRequest.getServiceProviderUUID());
			
			System.out.println("Outputs provided: " + "serviceProviderName ("
					+ response.getServiceProviderName() + "), "
					+ "serviceProviderDescription ("
					+ response.getServiceProviderFreeTextDescription() + "), "
					+ "UUIDs of provided services ("
					+ response.getListOfProvidedServiceUUIDs().toString() + ")");
			
			return response;
		}
		catch (SemanticRegistryMalformedInputException e)
		{
			throw new AxisFault(new QName("http://www.seerc.org", e.getClass().getSimpleName(), "fusion"), e.getMessage(), e);
		}
		catch (SemanticRegistryCommunicationException e)
		{
			throw new AxisFault(new QName("http://www.seerc.org", e.getClass().getSimpleName(), "fusion"), e.getMessage(), e);
		}
		catch (SemanticRegistryNoMatchFoundException e)
		{
			throw new AxisFault(new QName("http://www.seerc.org", e.getClass().getSimpleName(), "fusion"), e.getMessage(), e);
		}
		catch (SemanticRegistryException e)
		{
			throw new AxisFault(new QName("http://www.seerc.org", e.getClass().getSimpleName(), "fusion"), e.getMessage(), e);
		}
	}
	
	
	
	
	
	
	
	/**
	 * Used for retrieving a list of all Service Provider systems registered
	 * with the UDDI server.
	 * 
	 * @throws AxisFault
	 */
	public org.seerc.fusion.sr.api.xsd.GetAllServiceUUIDsResponse getAllServiceUUIDs() 
	
	throws AxisFault
	{
		System.out.println("Invoked: getAllServiceUUIDs");
		System.out.println("Inputs provided: No inputs are needed");

		// Create a controller for UDDI discovery operations
		DiscoveryHandler handler = null;
		try
		{
			handler = new DiscoveryHandler();
		}
		catch (SemanticRegistryConfigurationException e)
		{
			throw new AxisFault(new QName("http://www.seerc.org", e.getClass().getSimpleName(), "fusion"), e.getMessage(), e);
		}
		
		// Get the list of UUIDs to be stored inside the content object
		String[] itemList;
		try
		{
			itemList = handler.getAllServiceUUIDs();
			
			// Create a response message object
			GetAllServiceUUIDsResponse response = new GetAllServiceUUIDsResponse();
			
			// Create a content object to be stored inside the response message
			GetAllServiceUUIDsResponse_type0 listOfServiceUUIDs = new GetAllServiceUUIDsResponse_type0();
			
			// Add the returned list to the content object
			listOfServiceUUIDs.setString(itemList);
			
			// Add the content object to the response message
			response.setGetAllServiceUUIDsResponse(listOfServiceUUIDs);
			
			System.out.println("Outputs provided: ");
			for (int i=0; i < itemList.length; i++) System.out.println( i+1 + ": " + itemList[i]);
			
			return response;
		}
		catch (SemanticRegistryMalformedInputException e)
		{
			throw new AxisFault(new QName("http://www.seerc.org", e.getClass().getSimpleName(), "fusion"), e.getMessage(), e);
		}
		catch (SemanticRegistryCommunicationException e)
		{
			throw new AxisFault(new QName("http://www.seerc.org", e.getClass().getSimpleName(), "fusion"), e.getMessage(), e);
		}
		catch (SemanticRegistryNoMatchFoundException e)
		{
			throw new AxisFault(new QName("http://www.seerc.org", e.getClass().getSimpleName(), "fusion"), e.getMessage(), e);
		}
		catch (SemanticRegistryException e)
		{
			throw new AxisFault(new QName("http://www.seerc.org", e.getClass().getSimpleName(), "fusion"), e.getMessage(), e);
		}
	}
	
	
	
	
	
	
	
	/**
	 * Used for retrieving a list of Services having a textual description that
	 * matches the keyword provided as an input parameter.
	 * 
	 * @param doKeywordSearchForServicesRequest
	 * @throws AxisFault
	 */
	public org.seerc.fusion.sr.api.xsd.DoKeywordSearchForServicesResponse doKeywordSearchForServices(
			org.seerc.fusion.sr.api.xsd.DoKeywordSearchForServicesRequest doKeywordSearchForServicesRequest) 
	
	throws AxisFault
	{	
		System.out.println("Invoked: doKeywordSearchForServices");
		System.out.println("Inputs provided: keyword (" + doKeywordSearchForServicesRequest.getKeyword() + ")");
		
		// Get the keyword from the request object
		String keyword = doKeywordSearchForServicesRequest.getKeyword();
		
		// Create a controller for UDDI discovery operations
		DiscoveryHandler handler = null;
		try
		{
			handler = new DiscoveryHandler();
		}
		catch (SemanticRegistryConfigurationException e)
		{
			throw new AxisFault(new QName("http://www.seerc.org", e.getClass().getSimpleName(), "fusion"), e.getMessage(), e);
		}
		
		// Get the list of UUIDs to be stored inside the content object
		String[] itemList;
		try
		{
			itemList = handler.doKeywordSearchForServices(keyword);
			
			// Create a response message object
			DoKeywordSearchForServicesResponse response = new DoKeywordSearchForServicesResponse();
			
			// Create a content object to be stored inside the response message
			DoKeywordSearchForServicesResponse_type0 listOfServiceUUIDs = new DoKeywordSearchForServicesResponse_type0();
			
			// Add the returned list to the content object
			listOfServiceUUIDs.setString(itemList);
			
			// Add the content object to the response message
			response.setDoKeywordSearchForServicesResponse(listOfServiceUUIDs);
			
			System.out.println("Outputs provided: ");
			for (int i=0; i < itemList.length; i++) System.out.println( i+1 + ": " + itemList[i]);
			
			return response;
		}
		catch (SemanticRegistryMalformedInputException e)
		{
			throw new AxisFault(new QName("http://www.seerc.org", e.getClass().getSimpleName(), "fusion"), e.getMessage(), e);
		}
		catch (SemanticRegistryCommunicationException e)
		{
			throw new AxisFault(new QName("http://www.seerc.org", e.getClass().getSimpleName(), "fusion"), e.getMessage(), e);
		}
		catch (SemanticRegistryNoMatchFoundException e)
		{
			throw new AxisFault(new QName("http://www.seerc.org", e.getClass().getSimpleName(), "fusion"), e.getMessage(), e);
		}
		catch (SemanticRegistryException e)
		{
			throw new AxisFault(new QName("http://www.seerc.org", e.getClass().getSimpleName(), "fusion"), e.getMessage(), e);
		}
	}
	
	
	
	
	
	
	
	/**
	 * Used for retrieving a list of Services that have been indexed as matching
	 * the Request Functional Profile (RFP) identified by the URI provided as an
	 * input parameter. The parameter list may optionally include the UUID of
	 * the system that should expose the service. 
	 * 
	 * @param doSemanticSearchForServicesRequest
	 * @throws AxisFault
	 */
	public org.seerc.fusion.sr.api.xsd.DoSemanticSearchForServicesResponse doSemanticSearchForServices(
			org.seerc.fusion.sr.api.xsd.DoSemanticSearchForServicesRequest doSemanticSearchForServicesRequest) throws AxisFault
	{
		System.out.println("Invoked: doSemanticSearchForServices");
		System.out.println("Inputs provided: profileURI (" + doSemanticSearchForServicesRequest.getRequestFunctionalProfileURI() 
				+ "), serviceProviderUUID (" + doSemanticSearchForServicesRequest.getServiceProviderUUID() + ")");
		
		// Get the profileURI from the request object
		String profileURI = doSemanticSearchForServicesRequest.getRequestFunctionalProfileURI();
		
		// Get the serviceProviderUUID from the request object
		String serviceProviderUUID = doSemanticSearchForServicesRequest.getServiceProviderUUID();
		
		// Create a controller for UDDI discovery operations
		DiscoveryHandler handler = null;
		try
		{
			handler = new DiscoveryHandler();
		}
		catch (SemanticRegistryConfigurationException e)
		{
			throw new AxisFault(new QName("http://www.seerc.org", e.getClass().getSimpleName(), "fusion"), e.getMessage(), e);
		}
		
		// Get the list of UUIDs to be stored inside the content object
		String[] itemList;
		try
		{
			itemList = handler.doSemanticSearchForServices(profileURI, serviceProviderUUID);

			// Create a response message object
			DoSemanticSearchForServicesResponse response = new DoSemanticSearchForServicesResponse();
			
			// Create a content object to be stored inside the response message
			DoSemanticSearchForServicesResponse_type0 listOfServiceUUIDs = new DoSemanticSearchForServicesResponse_type0();
			
			// Add the returned list to the content object
			listOfServiceUUIDs.setString(itemList);
			
			// Add the content object to the response message
			response.setDoSemanticSearchForServicesResponse(listOfServiceUUIDs);
			
			System.out.println("Outputs provided: ");
			for (int i=0; i < itemList.length; i++) System.out.println( i+1 + ": " + itemList[i]);
			
			return response;
		}
		catch (SemanticRegistryMalformedInputException e)
		{
			throw new AxisFault(new QName("http://www.seerc.org", e.getClass().getSimpleName(), "fusion"), e.getMessage(), e);
		}
		catch (SemanticRegistryCommunicationException e)
		{
			throw new AxisFault(new QName("http://www.seerc.org", e.getClass().getSimpleName(), "fusion"), e.getMessage(), e);
		}
		catch (SemanticRegistryNoMatchFoundException e)
		{
			throw new AxisFault(new QName("http://www.seerc.org", e.getClass().getSimpleName(), "fusion"), e.getMessage(), e);
		}
		catch (SemanticRegistryMatchmakingException e)
		{
			throw new AxisFault(new QName("http://www.seerc.org", e.getClass().getSimpleName(), "fusion"), e.getMessage(), e);
		}
		catch (SemanticRegistryException e)
		{
			throw new AxisFault(new QName("http://www.seerc.org", e.getClass().getSimpleName(), "fusion"), e.getMessage(), e);
		}
		catch (SemanticRegistryConfigurationException e)
		{
			throw new AxisFault(new QName("http://www.seerc.org", e.getClass().getSimpleName(), "fusion"), e.getMessage(), e);
		}
	}
	
	
	
	
	
	
	
	/**
	 * Used for retrieving information about a specific service. The UUID
	 * identifier of the service must be provided in the parameters list. It
	 * returns all information available about the service: service name,
	 * textual description, the URL location of the service’s SAWSDL document,
	 * the UUID of the system that exposes the service, the URIs of its
	 * category, input, and output annotations, and the URIs of all the Request
	 * Functional Profiles (RFPs) that are known to be satisfied by the specific
	 * service (i.e. having a match at the category, input, and output level).
	 * 
	 * @param getServiceDetailsRequest
	 * @throws AxisFault
	 */
	public org.seerc.fusion.sr.api.xsd.GetServiceDetailsResponse getServiceDetails(
			org.seerc.fusion.sr.api.xsd.GetServiceDetailsRequest getServiceDetailsRequest) throws AxisFault
	{
		System.out.println("Invoked: getServiceDetails");
		System.out.println("Inputs provided: serviceUUID ("
				+ getServiceDetailsRequest.getServiceUUID() + ")");

		// Create a controller for UDDI discovery operations
		DiscoveryHandler handler = null;
		try
		{
			handler = new DiscoveryHandler();
		}
		catch (SemanticRegistryConfigurationException e)
		{
			throw new AxisFault(new QName("http://www.seerc.org", e.getClass().getSimpleName(), "fusion"), e.getMessage(), e);
		}
		
		// Create a response message object
		GetServiceDetailsResponse response = new GetServiceDetailsResponse();

		// Set the returned data as the content of the response message
		try
		{
			response = handler.getServiceDetails(getServiceDetailsRequest.getServiceUUID());
			
			System.out.println("Outputs provided: "); 
			System.out.println("ServiceName: " + response.getServiceName());
			System.out.println("serviceFreeTextDescription: " + response.getServiceFreeTextDescription());
			System.out.println("URL location of SAWSDL Document: " + response.getLocationOfSAWSDLDocument());
			System.out.println("Service Provider UUID: " + response.getServiceProviderUUID());
			System.out.println("Service hasCategory annotation URI: " + response.getServiceProviderUUID());
			
			System.out.println("Service hasInput annotation URIs: ");
			String [] itemList = response.getListOfInputAnnotationURIs().getString();
			for (int i=0; i < itemList.length; i++) 
				System.out.println("\t" + itemList[i]);
		
			System.out.println("Service hasOutput annotation URIs: ");
			itemList = response.getListOfOutputAnnotationURIs().getString();
			for (int i=0; i < itemList.length; i++) 
				System.out.println("\t" + itemList[i]);
			
			System.out.println("Service matching RFP URIs: ");
			itemList = response.getListOfMatchingRequestFunctionalProfileURIs().getString();
			for (int i=0; i < itemList.length; i++) 
				System.out.println("\t" + itemList[i]);
			
			return response;
		}
		catch (SemanticRegistryMalformedInputException e)
		{
			throw new AxisFault(new QName("http://www.seerc.org", e.getClass().getSimpleName(), "fusion"), e.getMessage(), e);
		}
		catch (SemanticRegistryCommunicationException e)
		{
			throw new AxisFault(new QName("http://www.seerc.org", e.getClass().getSimpleName(), "fusion"), e.getMessage(), e);
		}
		catch (SemanticRegistryNoMatchFoundException e)
		{
			throw new AxisFault(new QName("http://www.seerc.org", e.getClass().getSimpleName(), "fusion"), e.getMessage(), e);
		}
		catch (SemanticRegistryException e)
		{
			throw new AxisFault(new QName("http://www.seerc.org", e.getClass().getSimpleName(), "fusion"), e.getMessage(), e);
		}
	}
}
