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
import org.seerc.fusion.sr.api.xsd.AddServiceProviderResponse;
import org.seerc.fusion.sr.api.xsd.AddServiceResponse;
import org.seerc.fusion.sr.api.xsd.AddServiceWithoutSAWSDLResponse;
import org.seerc.fusion.sr.api.xsd.InitiatePublicationSessionResponse;
import org.seerc.fusion.sr.api.xsd.ModifyServiceProviderResponse;
import org.seerc.fusion.sr.api.xsd.ModifyServiceResponse;
import org.seerc.fusion.sr.api.xsd.RemoveServiceProviderResponse;
import org.seerc.fusion.sr.api.xsd.RemoveServiceResponse;
import org.seerc.fusion.sr.api.xsd.TerminatePublicationSessionResponse;
import org.seerc.fusion.sr.core.PublicationHandler;
import org.seerc.fusion.sr.exceptions.SemanticRegistryAuthException;
import org.seerc.fusion.sr.exceptions.SemanticRegistryCommunicationException;
import org.seerc.fusion.sr.exceptions.SemanticRegistryConfigurationException;
import org.seerc.fusion.sr.exceptions.SemanticRegistryException;
import org.seerc.fusion.sr.exceptions.SemanticRegistryMalformedInputException;
import org.seerc.fusion.sr.exceptions.SemanticRegistryMatchmakingException;

/**
 *  Semantic Registry Web App - Publication Manager service
 *  @author Dimitrios Kourtesis, South-East European Research Centre (SEERC)
 */
public class PublicationManagerSkeleton
{
	/**
	 * Used for initiating a publication session. It returns an authentication
	 * token that the client must subsequently provide as part of the input
	 * parameters when invoking any of the Publication Manager’s operations. The
	 * authentication token is actually granted by the UDDI server module and
	 * retained in database persistence until the client explicitly asks for it
	 * to be destroyed. If the username or password are invalid it returns a
	 * SOAPFaultException.
	 * 
	 * @param initiatePublicationSessionRequest
	 * @throws AxisFault
	 */
	public org.seerc.fusion.sr.api.xsd.InitiatePublicationSessionResponse initiatePublicationSession(
			org.seerc.fusion.sr.api.xsd.InitiatePublicationSessionRequest initiatePublicationSessionRequest) throws AxisFault
	{
		System.out.println("Invoked: initiatePublicationSession");

		// Get username and password from the request message
		String usr = initiatePublicationSessionRequest.getUsername();
		String pwd = initiatePublicationSessionRequest.getPassword();
		
		System.out.println("Inputs provided: " + "username (" + usr + "), " + "password (" + pwd + ")");

		// Create a controller for UDDI publication operations
		PublicationHandler handler;
		try
		{
			handler = new PublicationHandler();
		}
		catch (SemanticRegistryConfigurationException e)
		{
			throw new AxisFault(new QName("http://www.seerc.org", e.getClass().getSimpleName(), "fusion"), e.getMessage(), e);
		}

		// Get an authentication token from the controller
		String authToken;
		try
		{
			authToken = handler.generateAuthenticationToken(usr, pwd);
			System.out.println("Outputs provided: authenticationToken ("
					+ authToken + ")");

			// Create response message
			InitiatePublicationSessionResponse response = new InitiatePublicationSessionResponse();

			// Set the token as the content of the response message
			response.setAuthenticationToken(authToken);
			return response;
		}
		catch (SemanticRegistryMalformedInputException e)
		{
			//throw new javax.xml.rpc.soap.SOAPFaultException(null, "One or more input parameters are invalid", null, null);
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
	 * Used for terminating a publication session. It erases the given
	 * authentication token from the UDDI server’s database persistence. It
	 * returns true if the provided authentication token exists in persistence,
	 * and false otherwise.
	 * 
	 * @param terminatePublicationSessionRequest
	 * @throws AxisFault 
	 */
	public org.seerc.fusion.sr.api.xsd.TerminatePublicationSessionResponse terminatePublicationSession(
			org.seerc.fusion.sr.api.xsd.TerminatePublicationSessionRequest terminatePublicationSessionRequest) throws AxisFault
	{
		System.out.println("Invoked: terminatePublicationSession");

		// Get authToken from the request message
		String authToken = terminatePublicationSessionRequest.getAuthenticationToken();

		System.out.println("Inputs provided: authToken (" + authToken + ")");

		// Create a controller for UDDI publication operations
		PublicationHandler handler;
		try
		{
			handler = new PublicationHandler();
		}
		catch (SemanticRegistryConfigurationException e)
		{
			throw new AxisFault(new QName("http://www.seerc.org", e.getClass().getSimpleName(), "fusion"), e.getMessage(), e);
		}
		
		// Ask the controller to destroy the authentication token
		Boolean terminationSuccess = false;
		try
		{
			terminationSuccess = handler.destroyAuthenticationToken(authToken);
			System.out.println("Outputs provided: terminationSuccess ("
					+ terminationSuccess + ")");

			// Create response message
			TerminatePublicationSessionResponse response = new TerminatePublicationSessionResponse();

			// Set the result as the content of the response message
			response.setTerminationSuccess(terminationSuccess);
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
	 * Used for creating a UDDI record to represent a Service-Oriented Business
	 * Application host system, providing one or more enterprise services.
	 * Descriptive information about the host system must be provided in the
	 * parameters list (the system name and a textual description). A valid
	 * publication authentication token must be also provided as part of the
	 * input parameters. It stores the service provider description as a
	 * BusinessEntity record in the UDDI server and returns the record’s UUID
	 * identifier. If the authentication token is invalid it returns a
	 * SOAPFaultException.
	 * 
	 * @param addServiceProviderRequest
	 * @throws AxisFault 
	 */
	public org.seerc.fusion.sr.api.xsd.AddServiceProviderResponse addServiceProvider(
			org.seerc.fusion.sr.api.xsd.AddServiceProviderRequest addServiceProviderRequest) throws AxisFault
	{
		System.out.println("Invoked: addServiceProvider");

		// Get the (mandatory) authToken and serviceProviderName from the
		// request message
		String authToken = addServiceProviderRequest.getAuthenticationToken();
		String serviceProviderName = addServiceProviderRequest.getServiceProviderName();

		// Get the (optional) values to be updated
		String serviceProviderFreeTextDescription = addServiceProviderRequest
				.getServiceProviderFreeTextDescription();

		System.out.println("Inputs provided: " 
				+ "authToken (" + authToken + "), " 
				+ "serviceProviderName (" + serviceProviderName + "), " 
				+ "serviceProviderFreeTextDescription (" + serviceProviderFreeTextDescription + ")");
		
		// Create a controller for UDDI publication operations
		PublicationHandler handler;
		try
		{
			handler = new PublicationHandler();
		}
		catch (SemanticRegistryConfigurationException e)
		{
			throw new AxisFault(new QName("http://www.seerc.org", e.getClass().getSimpleName(), "fusion"), e.getMessage(), e);
		}
		
		// Ask the controller to add the service provider
		String serviceProviderUUID;
		try
		{
			serviceProviderUUID = handler.addServiceProvider(
					authToken,
					serviceProviderName, 
					serviceProviderFreeTextDescription);
			
			System.out.println("Outputs provided: serviceProviderUUID ("
					+ serviceProviderUUID + ")");
			
			// Create response message
			AddServiceProviderResponse response = new AddServiceProviderResponse();
			
			// Set the UUID as the content of the response message
			response.setServiceProviderUUID(serviceProviderUUID);
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
	 * Used for updating the name or text-description of a Service-Oriented
	 * Business Application host system that has been published in the UDDI
	 * server. The client must specify the UUID identifier of the respective
	 * BusinessEntity record (representing the service provider) in the
	 * parameters list, and also provide a valid publication authentication
	 * token. The values in the serviceProviderName and
	 * serviceProviderFreeTextDescription parameters will replace the old ones.
	 * It returns true if the specified UDDI record exists and gets updated, and
	 * false otherwise. If the authentication token is invalid it returns a
	 * SOAPFaultException.
	 * 
	 * @param modifyServiceProviderRequest
	 * @throws AxisFault
	 */
	public org.seerc.fusion.sr.api.xsd.ModifyServiceProviderResponse modifyServiceProvider(
			org.seerc.fusion.sr.api.xsd.ModifyServiceProviderRequest modifyServiceProviderRequest) throws AxisFault
	{
		System.out.println("Invoked: modifyServiceProvider");

		// Get the (mandatory) authToken and UUID from the request message
		String authToken = modifyServiceProviderRequest.getAuthenticationToken();
		String serviceProviderUUID = modifyServiceProviderRequest.getServiceProviderUUID();

		// Get the (optional) values to be updated
		String serviceProviderName = modifyServiceProviderRequest.getServiceProviderName();
		String serviceProviderFreeTextDescription = modifyServiceProviderRequest
				.getServiceProviderFreeTextDescription();

		System.out.print("Inputs provided: " + "authToken (" + authToken
				+ "), " + "serviceProviderUUID (" + serviceProviderUUID + "), "
				+ "serviceProviderName (" + serviceProviderName + "), "
				+ "serviceProviderFreeTextDescription ("
				+ serviceProviderFreeTextDescription + ")");

		// Create a controller for UDDI publication operations
		PublicationHandler handler;
		try
		{
			handler = new PublicationHandler();
		}
		catch (SemanticRegistryConfigurationException e)
		{
			throw new AxisFault(new QName("http://www.seerc.org", e.getClass().getSimpleName(), "fusion"), e.getMessage(), e);
		}
		
		// Ask the controller to modify the service provider
		Boolean success;
		try
		{
			success = handler.modifyServiceProvider(
					authToken,
					serviceProviderUUID, 
					serviceProviderName,
					serviceProviderFreeTextDescription);
			
			System.out.println("Outputs provided: modificationSuccess (" + success
					+ ")");

			// Create response message
			ModifyServiceProviderResponse response = new ModifyServiceProviderResponse();

			// Set the result as the content of the response message
			response.setServiceProviderModificationSuccess(success);
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
	 * Used for removing the record of a Service-Oriented Business Application
	 * host system from the UDDI server. A valid publication authentication
	 * token and the UUID identifier of the system must be provided in the input
	 * parameters. It returns true if the specified UDDI record exists and gets
	 * removed, and false otherwise. If the authentication token is invalid it
	 * returns a SOAPFaultException.
	 * 
	 * @param removeServiceProviderRequest
	 * @throws AxisFault
	 */
	public org.seerc.fusion.sr.api.xsd.RemoveServiceProviderResponse removeServiceProvider(
			org.seerc.fusion.sr.api.xsd.RemoveServiceProviderRequest removeServiceProviderRequest) throws AxisFault
	{
		System.out.println("Invoked: removeServiceProvider");

		// Get the (mandatory) authToken and UUID from the request message
		String authToken = removeServiceProviderRequest.getAuthenticationToken();
		String serviceProviderUUID = removeServiceProviderRequest.getServiceProviderUUID();

		System.out.println("Inputs provided: " + "authToken (" + authToken
				+ "), " + "serviceProviderUUID (" + serviceProviderUUID + ")");

		// Create a controller for UDDI publication operations
		PublicationHandler handler;
		try
		{
			handler = new PublicationHandler();
		}
		catch (SemanticRegistryConfigurationException e)
		{
			throw new AxisFault(new QName("http://www.seerc.org", e.getClass().getSimpleName(), "fusion"), e.getMessage(), e);
		}
		
		// Ask the controller to remove the service provider
		Boolean removalSuccess;
		try
		{
			removalSuccess = handler.removeServiceProvider(
					authToken,
					serviceProviderUUID);
			
			System.out.println("Outputs provided: removalSuccess ("
					+ removalSuccess + ")");

			// Create response message
			RemoveServiceProviderResponse response = new RemoveServiceProviderResponse();

			// Set the result as the content of the response message
			response.setServiceProviderRemovalSuccess(removalSuccess);
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
	 * Used for creating a UDDI record to represent an enterprise service.
	 * Descriptive information about the service must be provided in the
	 * parameters list (the URL of the service’s WSDL document, the UUID of the
	 * service provider that exposes the service, a textual description). A
	 * valid publication authentication token must be also provided as part of
	 * the input parameters. It stores the service description as a
	 * BusinessService record in the UDDI server and returns the record’s UUID
	 * identifier. If the authentication token is invalid it returns a
	 * SOAPFaultException.
	 * 
	 * @param addServiceRequest
	 * @throws AxisFault 
	 */
	public org.seerc.fusion.sr.api.xsd.AddServiceResponse addService(
			org.seerc.fusion.sr.api.xsd.AddServiceRequest addServiceRequest) throws AxisFault
	{
		System.out.println("Invoked: addService");

		// Get the mandatory parameter values from the request message
		String authToken = addServiceRequest.getAuthenticationToken();
		String serviceSAWSDLURL = addServiceRequest.getSawsdlURL();
		String serviceProviderUUID = addServiceRequest.getServiceProviderUUID();

		// Get the optional parameter values from the request message
		String serviceName = addServiceRequest.getServiceName();
		String serviceFreeTextDescription = addServiceRequest.getServiceFreeTextDescription();

		System.out.println("Inputs provided: " + "authToken (" + authToken + "), "
				+ "serviceSAWSDLURL (" + serviceSAWSDLURL + "), " + "serviceProviderUUID ("
				+ serviceProviderUUID + "), " + "serviceName (" + serviceName + "), "
				+ "serviceFreeTextDescription (" + serviceFreeTextDescription + ")");

		// Create a controller for UDDI publication operations
		PublicationHandler handler;
		try
		{
			handler = new PublicationHandler();
		}
		catch (SemanticRegistryConfigurationException e)
		{
			throw new AxisFault(new QName("http://www.seerc.org", e.getClass().getSimpleName(),
					"fusion"), e.getMessage(), e);
		}

		// Ask the controller to add the service
		String serviceUUID;
		try
		{
			try
			{
				serviceUUID = handler.addService(
						authToken, 
						serviceSAWSDLURL, 
						serviceProviderUUID,
						serviceName, 
						serviceFreeTextDescription);
			}
			catch (SemanticRegistryMatchmakingException e)
			{
				throw new AxisFault(new QName("http://www.seerc.org", e.getClass().getSimpleName(),
						"fusion"), e.getMessage(), e);
			}
			catch (SemanticRegistryConfigurationException e)
			{
				throw new AxisFault(new QName("http://www.seerc.org", e.getClass().getSimpleName(),
				"fusion"), e.getMessage(), e);
			}

			System.out.println("Outputs provided: serviceUUID (" + serviceUUID + ")");

			// Create response message
			AddServiceResponse response = new AddServiceResponse();

			// Set the UUID as the content of the response message
			response.setServiceUUID(serviceUUID);
			return response;
		}
		catch (SemanticRegistryMalformedInputException e)
		{
			throw new AxisFault(new QName("http://www.seerc.org", e.getClass().getSimpleName(),
					"fusion"), e.getMessage(), e);
		}
		catch (SemanticRegistryCommunicationException e)
		{
			throw new AxisFault(new QName("http://www.seerc.org", e.getClass().getSimpleName(),
					"fusion"), e.getMessage(), e);
		}
		catch (SemanticRegistryAuthException e)
		{
			throw new AxisFault(new QName("http://www.seerc.org", e.getClass().getSimpleName(),
					"fusion"), e.getMessage(), e);
		}
		catch (SemanticRegistryException e)
		{
			throw new AxisFault(new QName("http://www.seerc.org", e.getClass().getSimpleName(),
					"fusion"), e.getMessage(), e);
		}
	}
	
	
	
	
	
	
	
	/**
	 * Used for creating a UDDI record to represent an enterprise service.
	 * Descriptive information about the service must be provided in the
	 * parameters list (the URL of the service’s WSDL document, the UUID of the
	 * service provider that exposes the service, a textual description). A
	 * valid publication authentication token must be also provided as part of
	 * the input parameters. It stores the service description as a
	 * BusinessService record in the UDDI server and returns the record’s UUID
	 * identifier. If the authentication token is invalid it returns a
	 * SOAPFaultException.
	 * 
	 * @param addServiceWithoutSAWSDLRequest
	 * @throws AxisFault 
	 */
	public org.seerc.fusion.sr.api.xsd.AddServiceWithoutSAWSDLResponse addServiceWithoutSAWSDL(
			org.seerc.fusion.sr.api.xsd.AddServiceWithoutSAWSDLRequest addServiceWithoutSAWSDLRequest) throws AxisFault
	{
		System.out.println("Invoked: addServiceWithoutSAWSDL");

		// Get the mandatory parameter values from the request message
		String authToken = addServiceWithoutSAWSDLRequest.getAuthenticationToken();
		String serviceSAWSDLURL = addServiceWithoutSAWSDLRequest.getSawsdlURL();
		String serviceProviderUUID = addServiceWithoutSAWSDLRequest.getServiceProviderUUID();
		String hasCategoryAnnotationURI = addServiceWithoutSAWSDLRequest.getHasCategoryAnnotationURI();
		String[] hasInputAnnotationURIArray = addServiceWithoutSAWSDLRequest.getHasInputAnnotationURIList().getString();
		String[] hasOutputAnnotationURIArray = addServiceWithoutSAWSDLRequest.getHasOutputAnnotationURIList().getString();

		// Get the optional parameter values from the request message
		String serviceName = addServiceWithoutSAWSDLRequest.getServiceName();
		String serviceFreeTextDescription = addServiceWithoutSAWSDLRequest.getServiceFreeTextDescription();

		System.out.println(
				"Inputs provided: " + "authToken (" + authToken + "), "
				+ "serviceSAWSDLURL (" + serviceSAWSDLURL + "), " + "serviceProviderUUID ("
				+ serviceProviderUUID + "), " + "serviceName (" + serviceName + "), "
				+ "serviceFreeTextDescription (" + serviceFreeTextDescription + ")" 
				+ "hasCategoryAnnotationURI (" + hasCategoryAnnotationURI + ")" 
				+ "hasInputAnnotationURIArray with length (" + hasInputAnnotationURIArray.length + ")" 
				+ "hasOutputAnnotationURIArray with length (" + hasOutputAnnotationURIArray.length + ")"  );

		// Create a controller for UDDI publication operations
		PublicationHandler handler;
		try
		{
			handler = new PublicationHandler();
		}
		catch (SemanticRegistryConfigurationException e)
		{
			throw new AxisFault(new QName("http://www.seerc.org", e.getClass().getSimpleName(),
					"fusion"), e.getMessage(), e);
		}

		// Ask the controller to add the service
		String serviceUUID;
		try
		{
			try
			{
				serviceUUID = handler.addServiceWithoutSAWSDL(
						authToken, 
						serviceSAWSDLURL, 
						serviceProviderUUID, 
						serviceName, 
						serviceFreeTextDescription, 
						hasCategoryAnnotationURI, 
						hasInputAnnotationURIArray, 
						hasOutputAnnotationURIArray);
			}
			catch (SemanticRegistryMatchmakingException e)
			{
				throw new AxisFault(new QName("http://www.seerc.org", e.getClass().getSimpleName(),
						"fusion"), e.getMessage(), e);
			}
			catch (SemanticRegistryConfigurationException e)
			{
				throw new AxisFault(new QName("http://www.seerc.org", e.getClass().getSimpleName(),
				"fusion"), e.getMessage(), e);
			}
			
			System.out.println("Outputs provided: serviceUUID (" + serviceUUID + ")");

			// Create response message
			AddServiceWithoutSAWSDLResponse response = new AddServiceWithoutSAWSDLResponse();

			// Set the UUID as the content of the response message
			response.setServiceUUID(serviceUUID);
			return response;
		}
		catch (SemanticRegistryMalformedInputException e)
		{
			throw new AxisFault(new QName("http://www.seerc.org", e.getClass().getSimpleName(),
					"fusion"), e.getMessage(), e);
		}
		catch (SemanticRegistryCommunicationException e)
		{
			throw new AxisFault(new QName("http://www.seerc.org", e.getClass().getSimpleName(),
					"fusion"), e.getMessage(), e);
		}
		catch (SemanticRegistryAuthException e)
		{
			throw new AxisFault(new QName("http://www.seerc.org", e.getClass().getSimpleName(),
					"fusion"), e.getMessage(), e);
		}
		catch (SemanticRegistryException e)
		{
			throw new AxisFault(new QName("http://www.seerc.org", e.getClass().getSimpleName(),
					"fusion"), e.getMessage(), e);
		}
	}
	
	
	
	
	
	
	
	/**
	 * Used for updating the name, text-description, or provider of an
	 * enterprise service that has been published in the UDDI server. The client
	 * must specify the UUID identifier of the service record in the parameters
	 * list, and also provide a valid publication authentication token. The
	 * values in the serviceName, serviceFreeTextDescription, and
	 * serviceProviderUUID parameters will replace the old ones. It returns true
	 * if the specified UDDI record exists and gets updated, and false
	 * otherwise. If the authentication token is invalid it returns a
	 * SOAPFaultException.
	 * 
	 * @param modifyServiceRequest
	 * @throws AxisFault 
	 */
	public org.seerc.fusion.sr.api.xsd.ModifyServiceResponse modifyService(
			org.seerc.fusion.sr.api.xsd.ModifyServiceRequest modifyServiceRequest) throws AxisFault
	{
		System.out.println("Invoked: modifyService");

		// Get the (mandatory) authToken and UUID from the request message
		String authToken = modifyServiceRequest.getAuthenticationToken();
		String serviceUUID = modifyServiceRequest.getServiceUUID();

		// Get the (optional) values to be updated
		String serviceName = modifyServiceRequest.getServiceName();
		String serviceFreeTextDescription = modifyServiceRequest
				.getServiceFreeTextDescription();
		String serviceProviderUUID = modifyServiceRequest.getServiceProviderUUID();

		System.out.println("Inputs provided: " + "authToken (" + authToken
				+ "), " + "serviceUUID (" + serviceUUID + "), "
				+ "serviceName (" + serviceName + "), "
				+ "serviceFreeTextDescription (" + serviceFreeTextDescription
				+ "), " + "serviceProviderUUID (" + serviceProviderUUID + ")");

		// Create a controller for UDDI publication operations
		PublicationHandler handler;
		try
		{
			handler = new PublicationHandler();
		}
		catch (SemanticRegistryConfigurationException e)
		{
			throw new AxisFault(new QName("http://www.seerc.org", e.getClass().getSimpleName(), "fusion"), e.getMessage(), e);
		}
		
		// Ask the controller to modify the service provider
		Boolean success;
		try
		{
			success = handler.modifyService(
					authToken, 
					serviceUUID,
					serviceName, 
					serviceFreeTextDescription, 
					serviceProviderUUID);
			
			System.out.println("Outputs provided: modificationSuccess (" + success
					+ ")");

			// Create response message
			ModifyServiceResponse response = new ModifyServiceResponse();

			// Set the result as the content of the response message
			response.setServiceModificationSuccess(success);
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
	 * Used for removing the record of a Service-Oriented Business Application
	 * host system from the UDDI server. A valid publication authentication
	 * token and the UUID identifier of the system must be provided in the input
	 * parameters. It returns true if the specified UDDI record exists and gets
	 * removed, and false otherwise. If the authentication token is invalid it
	 * returns a SOAPFaultException.
	 * 
	 * @param removeServiceRequest
	 * @throws AxisFault 
	 */
	public org.seerc.fusion.sr.api.xsd.RemoveServiceResponse removeService(
			org.seerc.fusion.sr.api.xsd.RemoveServiceRequest removeServiceRequest) throws AxisFault
	{
		System.out.println("Invoked: removeService");

		// Get the (mandatory) authToken and UUID from the request message
		String authToken = removeServiceRequest.getAuthenticationToken();
		String serviceUUID = removeServiceRequest.getServiceUUID();

		System.out.println("Inputs provided: authToken (" + authToken
				+ "), serviceUUID (" + serviceUUID + ")");

		// Create a controller for UDDI publication operations
		PublicationHandler handler;
		try
		{
			handler = new PublicationHandler();
		}
		catch (SemanticRegistryConfigurationException e)
		{
			throw new AxisFault(new QName("http://www.seerc.org", e.getClass().getSimpleName(), "fusion"), e.getMessage(), e);
		}
		
		// Ask the controller to remove the service
		Boolean removalSuccess;
		try
		{
			removalSuccess = handler.removeService(authToken, serviceUUID);
			
			System.out.println("Outputs provided: removalSuccess ("
					+ removalSuccess + ")");

			// Create response message
			RemoveServiceResponse response = new RemoveServiceResponse();

			// Set the result as the content of the response message
			response.setServiceRemovalSuccess(removalSuccess);
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
		catch (SemanticRegistryConfigurationException e)
		{
			throw new AxisFault(new QName("http://www.seerc.org", e.getClass().getSimpleName(), "fusion"), e.getMessage(), e);
		}
	}
}
