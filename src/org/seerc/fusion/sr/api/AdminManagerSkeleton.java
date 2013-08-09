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
import org.seerc.fusion.sr.api.xsd.AddRFPToIndexResponse;
import org.seerc.fusion.sr.api.xsd.AddRFPToIndexResponse_type0;
import org.seerc.fusion.sr.api.xsd.RefreshIndexResponse;
import org.seerc.fusion.sr.api.xsd.RefreshIndexResponse_type0;
import org.seerc.fusion.sr.api.xsd.RemoveRFPFromIndexResponse;
import org.seerc.fusion.sr.api.xsd.RemoveRFPFromIndexResponse_type0;
import org.seerc.fusion.sr.api.xsd.SetupStandardConfigurationResponse;
import org.seerc.fusion.sr.api.xsd.SetupStandardConfigurationResponse_type0;
import org.seerc.fusion.sr.core.AdminHandler;
import org.seerc.fusion.sr.core.PublicationHandler;
import org.seerc.fusion.sr.exceptions.SemanticRegistryAuthException;
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
public class AdminManagerSkeleton
{
	
	
	/**
	 * @param setupStandardConfigurationRequest
	 * @return
	 * @throws AxisFault
	 */
	public org.seerc.fusion.sr.api.xsd.SetupStandardConfigurationResponse setupStandardConfiguration(
			org.seerc.fusion.sr.api.xsd.SetupStandardConfigurationRequest setupStandardConfigurationRequest) throws AxisFault
	{
		System.out.println("Invoked: setupStandardConfiguration");

		// Get username and password from the request message
		String usr = setupStandardConfigurationRequest.getUsername();
		String pwd = setupStandardConfigurationRequest.getPassword();
		String inquiryURL = setupStandardConfigurationRequest.getUddi_inquiry_url();
		String publishURL = setupStandardConfigurationRequest.getUddi_publish_url();
		
		System.out.println("Inputs provided: " 
				+ "username (" + usr + "), " 
				+ "password (" + pwd + "), " 
				+ "inquiryURL (" + inquiryURL + "), " 
				+ "publishURL (" + publishURL + ")" );
		
		try
		{			
			// Create a controller for UDDI admin operations
			AdminHandler adminHandler = null;
			try
			{
				adminHandler = new AdminHandler(inquiryURL, publishURL);
			}
			catch (SemanticRegistryMalformedInputException e)
			{
				throw new AxisFault(new QName("http://www.seerc.org", e.getClass().getSimpleName(),"fusion"), e.getMessage(), e);
			}
			
			// Ask the admin handler to perform the check/publication of tModels
			String[] details = adminHandler.deployCanonicalTModels(usr, pwd );
			
			for (int i = 0; i < details.length; i++)
			{
				System.out.println('\t' + details[i]);
			}
			
			// Replace all occurences of whitespace with underscore, to avoid wrapping in the 
			for (int i = 0; i < details.length; i++)
			{
				details[i] = details[i].replaceAll(" ", "_");
			}
			
			// Create response message
			SetupStandardConfigurationResponse response = new SetupStandardConfigurationResponse();
			
			// Create a content object to be stored inside the response message
			SetupStandardConfigurationResponse_type0 content = new SetupStandardConfigurationResponse_type0();
			
			// Add the returned list to the content object
			content.setString(details);
			
			// Add the content object to the response message
			response.setSetupStandardConfigurationResponse(content);
			
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
		catch (SemanticRegistryAuthException e)
		{
			throw new AxisFault(new QName("http://www.seerc.org", e.getClass().getSimpleName(), "fusion"), e.getMessage(), e);
		}
		catch (SemanticRegistryException e)
		{
			throw new AxisFault(new QName("http://www.seerc.org", e.getClass().getSimpleName(), "fusion"), e.getMessage(), e);
		}
	}
	
	
	
	
	
	
	
	/**
	 * @param addRFPToIndexRequest
	 * @return
	 * @throws AxisFault
	 */
	public org.seerc.fusion.sr.api.xsd.AddRFPToIndexResponse addRFPToIndex(
			org.seerc.fusion.sr.api.xsd.AddRFPToIndexRequest addRFPToIndexRequest) 
	
	throws AxisFault
	{
		System.out.println("Invoked: addRFPToIndex");
		
		// Get username and password from the request message
		String usr = addRFPToIndexRequest.getUsername();
		String pwd = addRFPToIndexRequest.getPassword();
		
		// Create a publication controller for getting an authToken
		PublicationHandler pubHandler;
		try
		{
			pubHandler = new PublicationHandler();
		}
		catch (SemanticRegistryConfigurationException e)
		{
			throw new AxisFault(new QName("http://www.seerc.org", e.getClass().getSimpleName(), "fusion"), e.getMessage(), e);
		}
		
		String authToken = null;

		// Get an authentication token from the controller
		try
		{
			authToken = pubHandler.generateAuthenticationToken(usr, pwd);
			System.out.println("Acquired authenticationToken (" + authToken + ")");
		}
		catch (SemanticRegistryMalformedInputException e)
		{
			throw new AxisFault(new QName("http://www.seerc.org", e.getClass().getSimpleName(), "fusion"), e.getMessage(), e);
		}
		catch (SemanticRegistryAuthException e)
		{
			throw new AxisFault(new QName("http://www.seerc.org", e.getClass().getSimpleName(), "fusion"), e.getMessage(), e);
		}
		catch (SemanticRegistryCommunicationException e)
		{
			throw new AxisFault(new QName("http://www.seerc.org", e.getClass().getSimpleName(), "fusion"), e.getMessage(), e);
		}
		catch (SemanticRegistryException e)
		{
			throw new AxisFault(new QName("http://www.seerc.org", e.getClass().getSimpleName(), "fusion"), e.getMessage(), e);
		}

		if (addRFPToIndexRequest.getRequestFunctionalProfileURI() != null)
			System.out.println("Inputs provided: profileURI (" + addRFPToIndexRequest.getRequestFunctionalProfileURI() + ")" );
		
		// Get the profileURI from the request object
		String profileURI = addRFPToIndexRequest.getRequestFunctionalProfileURI();
		
		// Create a controller for UDDI administration operations
		AdminHandler handler = null;
		try
		{
			handler = new AdminHandler();
			
		}
		catch (SemanticRegistryConfigurationException e)
		{
			throw new AxisFault(new QName("http://www.seerc.org", e.getClass().getSimpleName(), "fusion"), e.getMessage(), e);
		}
		
		// Get the list of UUIDs to be stored inside the content object
		String[] itemList;
		try
		{
			itemList = handler.addRFPToIndex(authToken, profileURI);
			
			// Create a response message object
			AddRFPToIndexResponse response = new AddRFPToIndexResponse();
			
			// Create a content object to be stored inside the response message
			AddRFPToIndexResponse_type0 listOfOfAffectedServiceUUIDs = new AddRFPToIndexResponse_type0();
			
			// Add the returned list to the content object
			listOfOfAffectedServiceUUIDs.setString(itemList);
			
			// Add the content object to the response message
			response.setAddRFPToIndexResponse(listOfOfAffectedServiceUUIDs);
			
			//System.out.println("Outputs provided: ");
			//for (int i=0; i < itemList.length; i++) System.out.println( i+1 + ": " + itemList[i]);
			
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
		catch (SemanticRegistryAuthException e)
		{
			throw new AxisFault(new QName("http://www.seerc.org", e.getClass().getSimpleName(), "fusion"), e.getMessage(), e);
		}
	}
	
	
	
	
	
	
	
	/**
	 * @param removeRFPFromIndexRequest
	 * @return
	 * @throws AxisFault
	 */
	public org.seerc.fusion.sr.api.xsd.RemoveRFPFromIndexResponse removeRFPFromIndex(
			org.seerc.fusion.sr.api.xsd.RemoveRFPFromIndexRequest removeRFPFromIndexRequest)
	throws AxisFault
	{
		System.out.println("Invoked: removeRFPFromIndex");
		
		// Get username and password from the request message
		String usr = removeRFPFromIndexRequest.getUsername();
		String pwd = removeRFPFromIndexRequest.getPassword();
		
		// Create a publication controller for getting an authToken
		PublicationHandler pubHandler;
		try
		{
			pubHandler = new PublicationHandler();
		}
		catch (SemanticRegistryConfigurationException e)
		{
			throw new AxisFault(new QName("http://www.seerc.org", e.getClass().getSimpleName(), "fusion"), e.getMessage(), e);
		}
		
		String authToken = null;

		// Get an authentication token from the controller
		try
		{
			authToken = pubHandler.generateAuthenticationToken(usr, pwd);
			System.out.println("Acquired authenticationToken (" + authToken + ")");
		}
		catch (SemanticRegistryMalformedInputException e)
		{
			throw new AxisFault(new QName("http://www.seerc.org", e.getClass().getSimpleName(), "fusion"), e.getMessage(), e);
		}
		catch (SemanticRegistryAuthException e)
		{
			throw new AxisFault(new QName("http://www.seerc.org", e.getClass().getSimpleName(), "fusion"), e.getMessage(), e);
		}
		catch (SemanticRegistryCommunicationException e)
		{
			throw new AxisFault(new QName("http://www.seerc.org", e.getClass().getSimpleName(), "fusion"), e.getMessage(), e);
		}
		catch (SemanticRegistryException e)
		{
			throw new AxisFault(new QName("http://www.seerc.org", e.getClass().getSimpleName(), "fusion"), e.getMessage(), e);
		}

		if (removeRFPFromIndexRequest.getRequestFunctionalProfileURI() != null)
			System.out.println("Inputs provided: profileURI (" + removeRFPFromIndexRequest.getRequestFunctionalProfileURI() + ")" );
		
		// Get the profileURI from the request object
		String profileURI = removeRFPFromIndexRequest.getRequestFunctionalProfileURI();
		
		// Create a controller for UDDI administration operations
		AdminHandler handler = null;
		try
		{
			handler = new AdminHandler();
			
		}
		catch (SemanticRegistryConfigurationException e)
		{
			throw new AxisFault(new QName("http://www.seerc.org", e.getClass().getSimpleName(), "fusion"), e.getMessage(), e);
		}
		
		// Get the list of UUIDs to be stored inside the content object
		String[] itemList;
		try
		{
			itemList = handler.removeRFPFromIndex(authToken, profileURI);
			
			// Create a response message object
			RemoveRFPFromIndexResponse response = new RemoveRFPFromIndexResponse();
			
			// Create a content object to be stored inside the response message
			RemoveRFPFromIndexResponse_type0 listOfOfAffectedServiceUUIDs = new RemoveRFPFromIndexResponse_type0();
			
			// Add the returned list to the content object
			listOfOfAffectedServiceUUIDs.setString(itemList);
			
			// Add the content object to the response message
			response.setRemoveRFPFromIndexResponse(listOfOfAffectedServiceUUIDs);
			
			//System.out.println("Outputs provided: ");
			//for (int i=0; i < itemList.length; i++) System.out.println( i+1 + ": " + itemList[i]);
			
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
		catch (SemanticRegistryAuthException e)
		{
			throw new AxisFault(new QName("http://www.seerc.org", e.getClass().getSimpleName(), "fusion"), e.getMessage(), e);
		}
	}
	
	
	
	
	
	
	
	/**
	 * @param refreshIndexRequest
	 * @return
	 * @throws AxisFault
	 */
	public org.seerc.fusion.sr.api.xsd.RefreshIndexResponse refreshIndex(
			org.seerc.fusion.sr.api.xsd.RefreshIndexRequest refreshIndexRequest)
	
	throws AxisFault
	{
		System.out.println("Invoked: refreshIndex");
		
		// Get username and password from the request message
		String usr = refreshIndexRequest.getUsername();
		String pwd = refreshIndexRequest.getPassword();
		
		// Create a publication controller for getting an authToken
		PublicationHandler pubHandler;
		try
		{
			pubHandler = new PublicationHandler();
		}
		catch (SemanticRegistryConfigurationException e)
		{
			throw new AxisFault(new QName("http://www.seerc.org", e.getClass().getSimpleName(), "fusion"), e.getMessage(), e);
		}
		
		String authToken = null;

		// Get an authentication token from the controller
		try
		{
			authToken = pubHandler.generateAuthenticationToken(usr, pwd);
			System.out.println("Acquired authenticationToken (" + authToken + ")");
		}
		catch (SemanticRegistryMalformedInputException e)
		{
			throw new AxisFault(new QName("http://www.seerc.org", e.getClass().getSimpleName(), "fusion"), e.getMessage(), e);
		}
		catch (SemanticRegistryAuthException e)
		{
			throw new AxisFault(new QName("http://www.seerc.org", e.getClass().getSimpleName(), "fusion"), e.getMessage(), e);
		}
		catch (SemanticRegistryCommunicationException e)
		{
			throw new AxisFault(new QName("http://www.seerc.org", e.getClass().getSimpleName(), "fusion"), e.getMessage(), e);
		}
		catch (SemanticRegistryException e)
		{
			throw new AxisFault(new QName("http://www.seerc.org", e.getClass().getSimpleName(), "fusion"), e.getMessage(), e);
		}

		
		// Create a controller for UDDI administration operations
		AdminHandler handler = null;
		try
		{
			handler = new AdminHandler();
			
		}
		catch (SemanticRegistryConfigurationException e)
		{
			throw new AxisFault(new QName("http://www.seerc.org", e.getClass().getSimpleName(), "fusion"), e.getMessage(), e);
		}
		
		// Get the list of UUIDs to be stored inside the content object
		String[] itemList;
		try
		{
			itemList = handler.refreshIndex(authToken);
			
			// Create a response message object
			RefreshIndexResponse response = new RefreshIndexResponse();
			
			// Create a content object to be stored inside the response message
			RefreshIndexResponse_type0 listOfOfAffectedServiceUUIDs = new RefreshIndexResponse_type0();
			
			// Add the returned list to the content object
			listOfOfAffectedServiceUUIDs.setString(itemList);
			
			// Add the content object to the response message
			response.setRefreshIndexResponse(listOfOfAffectedServiceUUIDs);
			
			//System.out.println("Outputs provided: ");
			//for (int i=0; i < itemList.length; i++) System.out.println( i+1 + ": " + itemList[i]);
			
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
		catch (SemanticRegistryAuthException e)
		{
			throw new AxisFault(new QName("http://www.seerc.org", e.getClass().getSimpleName(), "fusion"), e.getMessage(), e);
		}
		catch (SemanticRegistryConfigurationException e)
		{
			throw new AxisFault(new QName("http://www.seerc.org", e.getClass().getSimpleName(), "fusion"), e.getMessage(), e);
		}
		catch (SemanticRegistryException e)
		{
			throw new AxisFault(new QName("http://www.seerc.org", e.getClass().getSimpleName(), "fusion"), e.getMessage(), e);
		}
	}
}
