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

/**
 *  DiscoveryManagerMessageReceiverInOut message receiver
 */
public class DiscoveryManagerMessageReceiverInOut
		extends
		org.apache.axis2.receivers.AbstractInOutSyncMessageReceiver
{
	public void invokeBusinessLogic(org.apache.axis2.context.MessageContext msgContext,
			org.apache.axis2.context.MessageContext newMsgContext)
			throws org.apache.axis2.AxisFault
	{
		try
		{
			// get the implementation class for the Web Service
			Object obj = getTheImplementationObject(msgContext);

			DiscoveryManagerSkeleton skel = (DiscoveryManagerSkeleton) obj;

			//Out Envelop
			org.apache.axiom.soap.SOAPEnvelope envelope = null;

			//Find the axisOperation that has been set by the Dispatch phase.
			org.apache.axis2.description.AxisOperation op = msgContext.getOperationContext()
					.getAxisOperation();

			if (op == null)
			{
				throw new org.apache.axis2.AxisFault(
						"Operation is not located, if this is doclit style the SOAP-ACTION should specified via the SOAP Action to use the RawXMLProvider");
			}

			java.lang.String methodName;

			if ((op.getName() != null)
					&& ((methodName = org.apache.axis2.util.JavaUtils.xmlNameToJava(op.getName()
							.getLocalPart())) != null))
			{
				if ("getAllServiceProviderUUIDs".equals(methodName))
				{
					org.seerc.fusion.sr.api.xsd.GetAllServiceProviderUUIDsResponse getAllServiceProviderUUIDsResponse17 = null;
					getAllServiceProviderUUIDsResponse17 = skel.getAllServiceProviderUUIDs();

					envelope = toEnvelope(getSOAPFactory(msgContext),
							getAllServiceProviderUUIDsResponse17, false);
				}
				else if ("doKeywordSearchForServiceProviders".equals(methodName))
				{
					org.seerc.fusion.sr.api.xsd.DoKeywordSearchForServiceProvidersResponse doKeywordSearchForServiceProvidersResponse19 = null;
					org.seerc.fusion.sr.api.xsd.DoKeywordSearchForServiceProvidersRequest wrappedParam = (org.seerc.fusion.sr.api.xsd.DoKeywordSearchForServiceProvidersRequest) fromOM(
							msgContext.getEnvelope().getBody().getFirstElement(),
							org.seerc.fusion.sr.api.xsd.DoKeywordSearchForServiceProvidersRequest.class,
							getEnvelopeNamespaces(msgContext.getEnvelope()));

					doKeywordSearchForServiceProvidersResponse19 = skel
							.doKeywordSearchForServiceProviders(wrappedParam);

					envelope = toEnvelope(getSOAPFactory(msgContext),
							doKeywordSearchForServiceProvidersResponse19, false);
				}
				else if ("getServiceProviderDetails".equals(methodName))
				{
					org.seerc.fusion.sr.api.xsd.GetServiceProviderDetailsResponse getServiceProviderDetailsResponse21 = null;
					org.seerc.fusion.sr.api.xsd.GetServiceProviderDetailsRequest wrappedParam = (org.seerc.fusion.sr.api.xsd.GetServiceProviderDetailsRequest) fromOM(
							msgContext.getEnvelope().getBody().getFirstElement(),
							org.seerc.fusion.sr.api.xsd.GetServiceProviderDetailsRequest.class,
							getEnvelopeNamespaces(msgContext.getEnvelope()));

					getServiceProviderDetailsResponse21 = skel
							.getServiceProviderDetails(wrappedParam);

					envelope = toEnvelope(getSOAPFactory(msgContext),
							getServiceProviderDetailsResponse21, false);
				}
				else if ("getAllServiceUUIDs".equals(methodName))
				{
					org.seerc.fusion.sr.api.xsd.GetAllServiceUUIDsResponse getAllServiceUUIDsResponse23 = null;
					getAllServiceUUIDsResponse23 = skel.getAllServiceUUIDs();

					envelope = toEnvelope(getSOAPFactory(msgContext), getAllServiceUUIDsResponse23,
							false);
				}
				else if ("doKeywordSearchForServices".equals(methodName))
				{
					org.seerc.fusion.sr.api.xsd.DoKeywordSearchForServicesResponse doKeywordSearchForServicesResponse25 = null;
					org.seerc.fusion.sr.api.xsd.DoKeywordSearchForServicesRequest wrappedParam = (org.seerc.fusion.sr.api.xsd.DoKeywordSearchForServicesRequest) fromOM(
							msgContext.getEnvelope().getBody().getFirstElement(),
							org.seerc.fusion.sr.api.xsd.DoKeywordSearchForServicesRequest.class,
							getEnvelopeNamespaces(msgContext.getEnvelope()));

					doKeywordSearchForServicesResponse25 = skel
							.doKeywordSearchForServices(wrappedParam);

					envelope = toEnvelope(getSOAPFactory(msgContext),
							doKeywordSearchForServicesResponse25, false);
				}
				else if ("doSemanticSearchForServices".equals(methodName))
				{
					org.seerc.fusion.sr.api.xsd.DoSemanticSearchForServicesResponse doSemanticSearchForServicesResponse27 = null;
					org.seerc.fusion.sr.api.xsd.DoSemanticSearchForServicesRequest wrappedParam = (org.seerc.fusion.sr.api.xsd.DoSemanticSearchForServicesRequest) fromOM(
							msgContext.getEnvelope().getBody().getFirstElement(),
							org.seerc.fusion.sr.api.xsd.DoSemanticSearchForServicesRequest.class,
							getEnvelopeNamespaces(msgContext.getEnvelope()));

					doSemanticSearchForServicesResponse27 = skel
							.doSemanticSearchForServices(wrappedParam);

					envelope = toEnvelope(getSOAPFactory(msgContext),
							doSemanticSearchForServicesResponse27, false);
				}
				else if ("getServiceDetails".equals(methodName))
				{
					org.seerc.fusion.sr.api.xsd.GetServiceDetailsResponse getServiceDetailsResponse29 = null;
					org.seerc.fusion.sr.api.xsd.GetServiceDetailsRequest wrappedParam = (org.seerc.fusion.sr.api.xsd.GetServiceDetailsRequest) fromOM(
							msgContext.getEnvelope().getBody().getFirstElement(),
							org.seerc.fusion.sr.api.xsd.GetServiceDetailsRequest.class,
							getEnvelopeNamespaces(msgContext.getEnvelope()));

					getServiceDetailsResponse29 = skel.getServiceDetails(wrappedParam);

					envelope = toEnvelope(getSOAPFactory(msgContext), getServiceDetailsResponse29,
							false);
				}
				else
				{
					throw new java.lang.RuntimeException("method not found");
				}

				newMsgContext.setEnvelope(envelope);
			}
		}
		catch (java.lang.Exception e)
		{
			throw org.apache.axis2.AxisFault.makeFault(e);
		}
	}

	//
	private org.apache.axiom.om.OMElement toOM(
			org.seerc.fusion.sr.api.xsd.GetAllServiceProviderUUIDsResponse param,
			boolean optimizeContent) throws org.apache.axis2.AxisFault
	{
		try
		{
			return param.getOMElement(
					org.seerc.fusion.sr.api.xsd.GetAllServiceProviderUUIDsResponse.MY_QNAME,
					org.apache.axiom.om.OMAbstractFactory.getOMFactory());
		}
		catch (org.apache.axis2.databinding.ADBException e)
		{
			throw org.apache.axis2.AxisFault.makeFault(e);
		}
	}

	private org.apache.axiom.om.OMElement toOM(
			org.seerc.fusion.sr.api.xsd.DoKeywordSearchForServicesRequest param,
			boolean optimizeContent) throws org.apache.axis2.AxisFault
	{
		try
		{
			return param.getOMElement(
					org.seerc.fusion.sr.api.xsd.DoKeywordSearchForServicesRequest.MY_QNAME,
					org.apache.axiom.om.OMAbstractFactory.getOMFactory());
		}
		catch (org.apache.axis2.databinding.ADBException e)
		{
			throw org.apache.axis2.AxisFault.makeFault(e);
		}
	}

	private org.apache.axiom.om.OMElement toOM(
			org.seerc.fusion.sr.api.xsd.DoKeywordSearchForServicesResponse param,
			boolean optimizeContent) throws org.apache.axis2.AxisFault
	{
		try
		{
			return param.getOMElement(
					org.seerc.fusion.sr.api.xsd.DoKeywordSearchForServicesResponse.MY_QNAME,
					org.apache.axiom.om.OMAbstractFactory.getOMFactory());
		}
		catch (org.apache.axis2.databinding.ADBException e)
		{
			throw org.apache.axis2.AxisFault.makeFault(e);
		}
	}

	private org.apache.axiom.om.OMElement toOM(
			org.seerc.fusion.sr.api.xsd.DoSemanticSearchForServicesRequest param,
			boolean optimizeContent) throws org.apache.axis2.AxisFault
	{
		try
		{
			return param.getOMElement(
					org.seerc.fusion.sr.api.xsd.DoSemanticSearchForServicesRequest.MY_QNAME,
					org.apache.axiom.om.OMAbstractFactory.getOMFactory());
		}
		catch (org.apache.axis2.databinding.ADBException e)
		{
			throw org.apache.axis2.AxisFault.makeFault(e);
		}
	}

	private org.apache.axiom.om.OMElement toOM(
			org.seerc.fusion.sr.api.xsd.DoSemanticSearchForServicesResponse param,
			boolean optimizeContent) throws org.apache.axis2.AxisFault
	{
		try
		{
			return param.getOMElement(
					org.seerc.fusion.sr.api.xsd.DoSemanticSearchForServicesResponse.MY_QNAME,
					org.apache.axiom.om.OMAbstractFactory.getOMFactory());
		}
		catch (org.apache.axis2.databinding.ADBException e)
		{
			throw org.apache.axis2.AxisFault.makeFault(e);
		}
	}

	private org.apache.axiom.om.OMElement toOM(
			org.seerc.fusion.sr.api.xsd.GetAllServiceUUIDsResponse param, boolean optimizeContent)
			throws org.apache.axis2.AxisFault
	{
		try
		{
			return param.getOMElement(
					org.seerc.fusion.sr.api.xsd.GetAllServiceUUIDsResponse.MY_QNAME,
					org.apache.axiom.om.OMAbstractFactory.getOMFactory());
		}
		catch (org.apache.axis2.databinding.ADBException e)
		{
			throw org.apache.axis2.AxisFault.makeFault(e);
		}
	}

	private org.apache.axiom.om.OMElement toOM(
			org.seerc.fusion.sr.api.xsd.DoKeywordSearchForServiceProvidersRequest param,
			boolean optimizeContent) throws org.apache.axis2.AxisFault
	{
		try
		{
			return param.getOMElement(
					org.seerc.fusion.sr.api.xsd.DoKeywordSearchForServiceProvidersRequest.MY_QNAME,
					org.apache.axiom.om.OMAbstractFactory.getOMFactory());
		}
		catch (org.apache.axis2.databinding.ADBException e)
		{
			throw org.apache.axis2.AxisFault.makeFault(e);
		}
	}

	private org.apache.axiom.om.OMElement toOM(
			org.seerc.fusion.sr.api.xsd.DoKeywordSearchForServiceProvidersResponse param,
			boolean optimizeContent) throws org.apache.axis2.AxisFault
	{
		try
		{
			return param
					.getOMElement(
							org.seerc.fusion.sr.api.xsd.DoKeywordSearchForServiceProvidersResponse.MY_QNAME,
							org.apache.axiom.om.OMAbstractFactory.getOMFactory());
		}
		catch (org.apache.axis2.databinding.ADBException e)
		{
			throw org.apache.axis2.AxisFault.makeFault(e);
		}
	}

	private org.apache.axiom.om.OMElement toOM(
			org.seerc.fusion.sr.api.xsd.GetServiceDetailsRequest param, boolean optimizeContent)
			throws org.apache.axis2.AxisFault
	{
		try
		{
			return param.getOMElement(
					org.seerc.fusion.sr.api.xsd.GetServiceDetailsRequest.MY_QNAME,
					org.apache.axiom.om.OMAbstractFactory.getOMFactory());
		}
		catch (org.apache.axis2.databinding.ADBException e)
		{
			throw org.apache.axis2.AxisFault.makeFault(e);
		}
	}

	private org.apache.axiom.om.OMElement toOM(
			org.seerc.fusion.sr.api.xsd.GetServiceDetailsResponse param, boolean optimizeContent)
			throws org.apache.axis2.AxisFault
	{
		try
		{
			return param.getOMElement(
					org.seerc.fusion.sr.api.xsd.GetServiceDetailsResponse.MY_QNAME,
					org.apache.axiom.om.OMAbstractFactory.getOMFactory());
		}
		catch (org.apache.axis2.databinding.ADBException e)
		{
			throw org.apache.axis2.AxisFault.makeFault(e);
		}
	}

	private org.apache.axiom.om.OMElement toOM(
			org.seerc.fusion.sr.api.xsd.GetServiceProviderDetailsRequest param,
			boolean optimizeContent) throws org.apache.axis2.AxisFault
	{
		try
		{
			return param.getOMElement(
					org.seerc.fusion.sr.api.xsd.GetServiceProviderDetailsRequest.MY_QNAME,
					org.apache.axiom.om.OMAbstractFactory.getOMFactory());
		}
		catch (org.apache.axis2.databinding.ADBException e)
		{
			throw org.apache.axis2.AxisFault.makeFault(e);
		}
	}

	private org.apache.axiom.om.OMElement toOM(
			org.seerc.fusion.sr.api.xsd.GetServiceProviderDetailsResponse param,
			boolean optimizeContent) throws org.apache.axis2.AxisFault
	{
		try
		{
			return param.getOMElement(
					org.seerc.fusion.sr.api.xsd.GetServiceProviderDetailsResponse.MY_QNAME,
					org.apache.axiom.om.OMAbstractFactory.getOMFactory());
		}
		catch (org.apache.axis2.databinding.ADBException e)
		{
			throw org.apache.axis2.AxisFault.makeFault(e);
		}
	}

	private org.apache.axiom.soap.SOAPEnvelope toEnvelope(
			org.apache.axiom.soap.SOAPFactory factory,
			org.seerc.fusion.sr.api.xsd.GetAllServiceProviderUUIDsResponse param,
			boolean optimizeContent) throws org.apache.axis2.AxisFault
	{
		try
		{
			org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();

			emptyEnvelope
					.getBody()
					.addChild(
							param
									.getOMElement(
											org.seerc.fusion.sr.api.xsd.GetAllServiceProviderUUIDsResponse.MY_QNAME,
											factory));

			return emptyEnvelope;
		}
		catch (org.apache.axis2.databinding.ADBException e)
		{
			throw org.apache.axis2.AxisFault.makeFault(e);
		}
	}

	private org.apache.axiom.soap.SOAPEnvelope toEnvelope(
			org.apache.axiom.soap.SOAPFactory factory,
			org.seerc.fusion.sr.api.xsd.DoKeywordSearchForServicesResponse param,
			boolean optimizeContent) throws org.apache.axis2.AxisFault
	{
		try
		{
			org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();

			emptyEnvelope
					.getBody()
					.addChild(
							param
									.getOMElement(
											org.seerc.fusion.sr.api.xsd.DoKeywordSearchForServicesResponse.MY_QNAME,
											factory));

			return emptyEnvelope;
		}
		catch (org.apache.axis2.databinding.ADBException e)
		{
			throw org.apache.axis2.AxisFault.makeFault(e);
		}
	}

	private org.apache.axiom.soap.SOAPEnvelope toEnvelope(
			org.apache.axiom.soap.SOAPFactory factory,
			org.seerc.fusion.sr.api.xsd.DoSemanticSearchForServicesResponse param,
			boolean optimizeContent) throws org.apache.axis2.AxisFault
	{
		try
		{
			org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();

			emptyEnvelope
					.getBody()
					.addChild(
							param
									.getOMElement(
											org.seerc.fusion.sr.api.xsd.DoSemanticSearchForServicesResponse.MY_QNAME,
											factory));

			return emptyEnvelope;
		}
		catch (org.apache.axis2.databinding.ADBException e)
		{
			throw org.apache.axis2.AxisFault.makeFault(e);
		}
	}

	private org.apache.axiom.soap.SOAPEnvelope toEnvelope(
			org.apache.axiom.soap.SOAPFactory factory,
			org.seerc.fusion.sr.api.xsd.GetAllServiceUUIDsResponse param, boolean optimizeContent)
			throws org.apache.axis2.AxisFault
	{
		try
		{
			org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();

			emptyEnvelope.getBody().addChild(
					param.getOMElement(
							org.seerc.fusion.sr.api.xsd.GetAllServiceUUIDsResponse.MY_QNAME,
							factory));

			return emptyEnvelope;
		}
		catch (org.apache.axis2.databinding.ADBException e)
		{
			throw org.apache.axis2.AxisFault.makeFault(e);
		}
	}

	private org.apache.axiom.soap.SOAPEnvelope toEnvelope(
			org.apache.axiom.soap.SOAPFactory factory,
			org.seerc.fusion.sr.api.xsd.DoKeywordSearchForServiceProvidersResponse param,
			boolean optimizeContent) throws org.apache.axis2.AxisFault
	{
		try
		{
			org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();

			emptyEnvelope
					.getBody()
					.addChild(
							param
									.getOMElement(
											org.seerc.fusion.sr.api.xsd.DoKeywordSearchForServiceProvidersResponse.MY_QNAME,
											factory));

			return emptyEnvelope;
		}
		catch (org.apache.axis2.databinding.ADBException e)
		{
			throw org.apache.axis2.AxisFault.makeFault(e);
		}
	}

	private org.apache.axiom.soap.SOAPEnvelope toEnvelope(
			org.apache.axiom.soap.SOAPFactory factory,
			org.seerc.fusion.sr.api.xsd.GetServiceDetailsResponse param, boolean optimizeContent)
			throws org.apache.axis2.AxisFault
	{
		try
		{
			org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();

			emptyEnvelope.getBody().addChild(
					param
							.getOMElement(
									org.seerc.fusion.sr.api.xsd.GetServiceDetailsResponse.MY_QNAME,
									factory));

			return emptyEnvelope;
		}
		catch (org.apache.axis2.databinding.ADBException e)
		{
			throw org.apache.axis2.AxisFault.makeFault(e);
		}
	}

	private org.apache.axiom.soap.SOAPEnvelope toEnvelope(
			org.apache.axiom.soap.SOAPFactory factory,
			org.seerc.fusion.sr.api.xsd.GetServiceProviderDetailsResponse param,
			boolean optimizeContent) throws org.apache.axis2.AxisFault
	{
		try
		{
			org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();

			emptyEnvelope.getBody().addChild(
					param.getOMElement(
							org.seerc.fusion.sr.api.xsd.GetServiceProviderDetailsResponse.MY_QNAME,
							factory));

			return emptyEnvelope;
		}
		catch (org.apache.axis2.databinding.ADBException e)
		{
			throw org.apache.axis2.AxisFault.makeFault(e);
		}
	}

	/**
	 *  get the default envelope
	 */
	private org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory)
	{
		return factory.getDefaultEnvelope();
	}

	private java.lang.Object fromOM(org.apache.axiom.om.OMElement param, java.lang.Class type,
			java.util.Map extraNamespaces) throws org.apache.axis2.AxisFault
	{
		try
		{
			if (org.seerc.fusion.sr.api.xsd.GetAllServiceProviderUUIDsResponse.class.equals(type))
			{
				return org.seerc.fusion.sr.api.xsd.GetAllServiceProviderUUIDsResponse.Factory
						.parse(param.getXMLStreamReaderWithoutCaching());
			}

			if (org.seerc.fusion.sr.api.xsd.DoKeywordSearchForServicesRequest.class.equals(type))
			{
				return org.seerc.fusion.sr.api.xsd.DoKeywordSearchForServicesRequest.Factory
						.parse(param.getXMLStreamReaderWithoutCaching());
			}

			if (org.seerc.fusion.sr.api.xsd.DoKeywordSearchForServicesResponse.class.equals(type))
			{
				return org.seerc.fusion.sr.api.xsd.DoKeywordSearchForServicesResponse.Factory
						.parse(param.getXMLStreamReaderWithoutCaching());
			}

			if (org.seerc.fusion.sr.api.xsd.DoSemanticSearchForServicesRequest.class.equals(type))
			{
				return org.seerc.fusion.sr.api.xsd.DoSemanticSearchForServicesRequest.Factory
						.parse(param.getXMLStreamReaderWithoutCaching());
			}

			if (org.seerc.fusion.sr.api.xsd.DoSemanticSearchForServicesResponse.class.equals(type))
			{
				return org.seerc.fusion.sr.api.xsd.DoSemanticSearchForServicesResponse.Factory
						.parse(param.getXMLStreamReaderWithoutCaching());
			}

			if (org.seerc.fusion.sr.api.xsd.GetAllServiceUUIDsResponse.class.equals(type))
			{
				return org.seerc.fusion.sr.api.xsd.GetAllServiceUUIDsResponse.Factory.parse(param
						.getXMLStreamReaderWithoutCaching());
			}

			if (org.seerc.fusion.sr.api.xsd.DoKeywordSearchForServiceProvidersRequest.class
					.equals(type))
			{
				return org.seerc.fusion.sr.api.xsd.DoKeywordSearchForServiceProvidersRequest.Factory
						.parse(param.getXMLStreamReaderWithoutCaching());
			}

			if (org.seerc.fusion.sr.api.xsd.DoKeywordSearchForServiceProvidersResponse.class
					.equals(type))
			{
				return org.seerc.fusion.sr.api.xsd.DoKeywordSearchForServiceProvidersResponse.Factory
						.parse(param.getXMLStreamReaderWithoutCaching());
			}

			if (org.seerc.fusion.sr.api.xsd.GetServiceDetailsRequest.class.equals(type))
			{
				return org.seerc.fusion.sr.api.xsd.GetServiceDetailsRequest.Factory.parse(param
						.getXMLStreamReaderWithoutCaching());
			}

			if (org.seerc.fusion.sr.api.xsd.GetServiceDetailsResponse.class.equals(type))
			{
				return org.seerc.fusion.sr.api.xsd.GetServiceDetailsResponse.Factory.parse(param
						.getXMLStreamReaderWithoutCaching());
			}

			if (org.seerc.fusion.sr.api.xsd.GetServiceProviderDetailsRequest.class.equals(type))
			{
				return org.seerc.fusion.sr.api.xsd.GetServiceProviderDetailsRequest.Factory
						.parse(param.getXMLStreamReaderWithoutCaching());
			}

			if (org.seerc.fusion.sr.api.xsd.GetServiceProviderDetailsResponse.class.equals(type))
			{
				return org.seerc.fusion.sr.api.xsd.GetServiceProviderDetailsResponse.Factory
						.parse(param.getXMLStreamReaderWithoutCaching());
			}
		}
		catch (java.lang.Exception e)
		{
			throw org.apache.axis2.AxisFault.makeFault(e);
		}

		return null;
	}

	/**
	 *  A utility method that copies the namepaces from the SOAPEnvelope
	 */
	private java.util.Map getEnvelopeNamespaces(org.apache.axiom.soap.SOAPEnvelope env)
	{
		java.util.Map returnMap = new java.util.HashMap();
		java.util.Iterator namespaceIterator = env.getAllDeclaredNamespaces();

		while (namespaceIterator.hasNext())
		{
			org.apache.axiom.om.OMNamespace ns = (org.apache.axiom.om.OMNamespace) namespaceIterator
					.next();
			returnMap.put(ns.getPrefix(), ns.getNamespaceURI());
		}

		return returnMap;
	}

	private org.apache.axis2.AxisFault createAxisFault(java.lang.Exception e)
	{
		org.apache.axis2.AxisFault f;
		Throwable cause = e.getCause();

		if (cause != null)
		{
			f = new org.apache.axis2.AxisFault(e.getMessage(), cause);
		}
		else
		{
			f = new org.apache.axis2.AxisFault(e.getMessage());
		}

		return f;
	}
} //end of class
