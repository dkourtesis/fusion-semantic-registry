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
 *  PublicationManagerMessageReceiverInOut message receiver
 */
public class PublicationManagerMessageReceiverInOut
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

			PublicationManagerSkeleton skel = (PublicationManagerSkeleton) obj;

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
				if ("initiatePublicationSession".equals(methodName))
				{
					org.seerc.fusion.sr.api.xsd.InitiatePublicationSessionResponse initiatePublicationSessionResponse19 = null;
					org.seerc.fusion.sr.api.xsd.InitiatePublicationSessionRequest wrappedParam = (org.seerc.fusion.sr.api.xsd.InitiatePublicationSessionRequest) fromOM(
							msgContext.getEnvelope().getBody().getFirstElement(),
							org.seerc.fusion.sr.api.xsd.InitiatePublicationSessionRequest.class,
							getEnvelopeNamespaces(msgContext.getEnvelope()));

					initiatePublicationSessionResponse19 = skel
							.initiatePublicationSession(wrappedParam);

					envelope = toEnvelope(getSOAPFactory(msgContext),
							initiatePublicationSessionResponse19, false);
				}
				else if ("terminatePublicationSession".equals(methodName))
				{
					org.seerc.fusion.sr.api.xsd.TerminatePublicationSessionResponse terminatePublicationSessionResponse21 = null;
					org.seerc.fusion.sr.api.xsd.TerminatePublicationSessionRequest wrappedParam = (org.seerc.fusion.sr.api.xsd.TerminatePublicationSessionRequest) fromOM(
							msgContext.getEnvelope().getBody().getFirstElement(),
							org.seerc.fusion.sr.api.xsd.TerminatePublicationSessionRequest.class,
							getEnvelopeNamespaces(msgContext.getEnvelope()));

					terminatePublicationSessionResponse21 = skel
							.terminatePublicationSession(wrappedParam);

					envelope = toEnvelope(getSOAPFactory(msgContext),
							terminatePublicationSessionResponse21, false);
				}
				else if ("addServiceProvider".equals(methodName))
				{
					org.seerc.fusion.sr.api.xsd.AddServiceProviderResponse addServiceProviderResponse23 = null;
					org.seerc.fusion.sr.api.xsd.AddServiceProviderRequest wrappedParam = (org.seerc.fusion.sr.api.xsd.AddServiceProviderRequest) fromOM(
							msgContext.getEnvelope().getBody().getFirstElement(),
							org.seerc.fusion.sr.api.xsd.AddServiceProviderRequest.class,
							getEnvelopeNamespaces(msgContext.getEnvelope()));

					addServiceProviderResponse23 = skel.addServiceProvider(wrappedParam);

					envelope = toEnvelope(getSOAPFactory(msgContext), addServiceProviderResponse23,
							false);
				}
				else if ("modifyServiceProvider".equals(methodName))
				{
					org.seerc.fusion.sr.api.xsd.ModifyServiceProviderResponse modifyServiceProviderResponse25 = null;
					org.seerc.fusion.sr.api.xsd.ModifyServiceProviderRequest wrappedParam = (org.seerc.fusion.sr.api.xsd.ModifyServiceProviderRequest) fromOM(
							msgContext.getEnvelope().getBody().getFirstElement(),
							org.seerc.fusion.sr.api.xsd.ModifyServiceProviderRequest.class,
							getEnvelopeNamespaces(msgContext.getEnvelope()));

					modifyServiceProviderResponse25 = skel.modifyServiceProvider(wrappedParam);

					envelope = toEnvelope(getSOAPFactory(msgContext),
							modifyServiceProviderResponse25, false);
				}
				else if ("removeServiceProvider".equals(methodName))
				{
					org.seerc.fusion.sr.api.xsd.RemoveServiceProviderResponse removeServiceProviderResponse27 = null;
					org.seerc.fusion.sr.api.xsd.RemoveServiceProviderRequest wrappedParam = (org.seerc.fusion.sr.api.xsd.RemoveServiceProviderRequest) fromOM(
							msgContext.getEnvelope().getBody().getFirstElement(),
							org.seerc.fusion.sr.api.xsd.RemoveServiceProviderRequest.class,
							getEnvelopeNamespaces(msgContext.getEnvelope()));

					removeServiceProviderResponse27 = skel.removeServiceProvider(wrappedParam);

					envelope = toEnvelope(getSOAPFactory(msgContext),
							removeServiceProviderResponse27, false);
				}
				else if ("addService".equals(methodName))
				{
					org.seerc.fusion.sr.api.xsd.AddServiceResponse addServiceResponse29 = null;
					org.seerc.fusion.sr.api.xsd.AddServiceRequest wrappedParam = (org.seerc.fusion.sr.api.xsd.AddServiceRequest) fromOM(
							msgContext.getEnvelope().getBody().getFirstElement(),
							org.seerc.fusion.sr.api.xsd.AddServiceRequest.class,
							getEnvelopeNamespaces(msgContext.getEnvelope()));

					addServiceResponse29 = skel.addService(wrappedParam);

					envelope = toEnvelope(getSOAPFactory(msgContext), addServiceResponse29, false);
				}
				else if ("addServiceWithoutSAWSDL".equals(methodName))
				{
					org.seerc.fusion.sr.api.xsd.AddServiceWithoutSAWSDLResponse addServiceWithoutSAWSDLResponse31 = null;
					org.seerc.fusion.sr.api.xsd.AddServiceWithoutSAWSDLRequest wrappedParam = (org.seerc.fusion.sr.api.xsd.AddServiceWithoutSAWSDLRequest) fromOM(
							msgContext.getEnvelope().getBody().getFirstElement(),
							org.seerc.fusion.sr.api.xsd.AddServiceWithoutSAWSDLRequest.class,
							getEnvelopeNamespaces(msgContext.getEnvelope()));

					addServiceWithoutSAWSDLResponse31 = skel.addServiceWithoutSAWSDL(wrappedParam);

					envelope = toEnvelope(getSOAPFactory(msgContext),
							addServiceWithoutSAWSDLResponse31, false);
				}
				else if ("modifyService".equals(methodName))
				{
					org.seerc.fusion.sr.api.xsd.ModifyServiceResponse modifyServiceResponse33 = null;
					org.seerc.fusion.sr.api.xsd.ModifyServiceRequest wrappedParam = (org.seerc.fusion.sr.api.xsd.ModifyServiceRequest) fromOM(
							msgContext.getEnvelope().getBody().getFirstElement(),
							org.seerc.fusion.sr.api.xsd.ModifyServiceRequest.class,
							getEnvelopeNamespaces(msgContext.getEnvelope()));

					modifyServiceResponse33 = skel.modifyService(wrappedParam);

					envelope = toEnvelope(getSOAPFactory(msgContext), modifyServiceResponse33,
							false);
				}
				else if ("removeService".equals(methodName))
				{
					org.seerc.fusion.sr.api.xsd.RemoveServiceResponse removeServiceResponse35 = null;
					org.seerc.fusion.sr.api.xsd.RemoveServiceRequest wrappedParam = (org.seerc.fusion.sr.api.xsd.RemoveServiceRequest) fromOM(
							msgContext.getEnvelope().getBody().getFirstElement(),
							org.seerc.fusion.sr.api.xsd.RemoveServiceRequest.class,
							getEnvelopeNamespaces(msgContext.getEnvelope()));

					removeServiceResponse35 = skel.removeService(wrappedParam);

					envelope = toEnvelope(getSOAPFactory(msgContext), removeServiceResponse35,
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
			org.seerc.fusion.sr.api.xsd.ModifyServiceRequest param, boolean optimizeContent)
			throws org.apache.axis2.AxisFault
	{
		try
		{
			return param.getOMElement(org.seerc.fusion.sr.api.xsd.ModifyServiceRequest.MY_QNAME,
					org.apache.axiom.om.OMAbstractFactory.getOMFactory());
		}
		catch (org.apache.axis2.databinding.ADBException e)
		{
			throw org.apache.axis2.AxisFault.makeFault(e);
		}
	}

	private org.apache.axiom.om.OMElement toOM(
			org.seerc.fusion.sr.api.xsd.ModifyServiceResponse param, boolean optimizeContent)
			throws org.apache.axis2.AxisFault
	{
		try
		{
			return param.getOMElement(org.seerc.fusion.sr.api.xsd.ModifyServiceResponse.MY_QNAME,
					org.apache.axiom.om.OMAbstractFactory.getOMFactory());
		}
		catch (org.apache.axis2.databinding.ADBException e)
		{
			throw org.apache.axis2.AxisFault.makeFault(e);
		}
	}

	private org.apache.axiom.om.OMElement toOM(org.seerc.fusion.sr.api.xsd.AddServiceRequest param,
			boolean optimizeContent) throws org.apache.axis2.AxisFault
	{
		try
		{
			return param.getOMElement(org.seerc.fusion.sr.api.xsd.AddServiceRequest.MY_QNAME,
					org.apache.axiom.om.OMAbstractFactory.getOMFactory());
		}
		catch (org.apache.axis2.databinding.ADBException e)
		{
			throw org.apache.axis2.AxisFault.makeFault(e);
		}
	}

	private org.apache.axiom.om.OMElement toOM(
			org.seerc.fusion.sr.api.xsd.AddServiceResponse param, boolean optimizeContent)
			throws org.apache.axis2.AxisFault
	{
		try
		{
			return param.getOMElement(org.seerc.fusion.sr.api.xsd.AddServiceResponse.MY_QNAME,
					org.apache.axiom.om.OMAbstractFactory.getOMFactory());
		}
		catch (org.apache.axis2.databinding.ADBException e)
		{
			throw org.apache.axis2.AxisFault.makeFault(e);
		}
	}

	private org.apache.axiom.om.OMElement toOM(
			org.seerc.fusion.sr.api.xsd.InitiatePublicationSessionRequest param,
			boolean optimizeContent) throws org.apache.axis2.AxisFault
	{
		try
		{
			return param.getOMElement(
					org.seerc.fusion.sr.api.xsd.InitiatePublicationSessionRequest.MY_QNAME,
					org.apache.axiom.om.OMAbstractFactory.getOMFactory());
		}
		catch (org.apache.axis2.databinding.ADBException e)
		{
			throw org.apache.axis2.AxisFault.makeFault(e);
		}
	}

	private org.apache.axiom.om.OMElement toOM(
			org.seerc.fusion.sr.api.xsd.InitiatePublicationSessionResponse param,
			boolean optimizeContent) throws org.apache.axis2.AxisFault
	{
		try
		{
			return param.getOMElement(
					org.seerc.fusion.sr.api.xsd.InitiatePublicationSessionResponse.MY_QNAME,
					org.apache.axiom.om.OMAbstractFactory.getOMFactory());
		}
		catch (org.apache.axis2.databinding.ADBException e)
		{
			throw org.apache.axis2.AxisFault.makeFault(e);
		}
	}

	private org.apache.axiom.om.OMElement toOM(
			org.seerc.fusion.sr.api.xsd.RemoveServiceProviderRequest param, boolean optimizeContent)
			throws org.apache.axis2.AxisFault
	{
		try
		{
			return param.getOMElement(
					org.seerc.fusion.sr.api.xsd.RemoveServiceProviderRequest.MY_QNAME,
					org.apache.axiom.om.OMAbstractFactory.getOMFactory());
		}
		catch (org.apache.axis2.databinding.ADBException e)
		{
			throw org.apache.axis2.AxisFault.makeFault(e);
		}
	}

	private org.apache.axiom.om.OMElement toOM(
			org.seerc.fusion.sr.api.xsd.RemoveServiceProviderResponse param, boolean optimizeContent)
			throws org.apache.axis2.AxisFault
	{
		try
		{
			return param.getOMElement(
					org.seerc.fusion.sr.api.xsd.RemoveServiceProviderResponse.MY_QNAME,
					org.apache.axiom.om.OMAbstractFactory.getOMFactory());
		}
		catch (org.apache.axis2.databinding.ADBException e)
		{
			throw org.apache.axis2.AxisFault.makeFault(e);
		}
	}

	private org.apache.axiom.om.OMElement toOM(
			org.seerc.fusion.sr.api.xsd.ModifyServiceProviderRequest param, boolean optimizeContent)
			throws org.apache.axis2.AxisFault
	{
		try
		{
			return param.getOMElement(
					org.seerc.fusion.sr.api.xsd.ModifyServiceProviderRequest.MY_QNAME,
					org.apache.axiom.om.OMAbstractFactory.getOMFactory());
		}
		catch (org.apache.axis2.databinding.ADBException e)
		{
			throw org.apache.axis2.AxisFault.makeFault(e);
		}
	}

	private org.apache.axiom.om.OMElement toOM(
			org.seerc.fusion.sr.api.xsd.ModifyServiceProviderResponse param, boolean optimizeContent)
			throws org.apache.axis2.AxisFault
	{
		try
		{
			return param.getOMElement(
					org.seerc.fusion.sr.api.xsd.ModifyServiceProviderResponse.MY_QNAME,
					org.apache.axiom.om.OMAbstractFactory.getOMFactory());
		}
		catch (org.apache.axis2.databinding.ADBException e)
		{
			throw org.apache.axis2.AxisFault.makeFault(e);
		}
	}

	private org.apache.axiom.om.OMElement toOM(
			org.seerc.fusion.sr.api.xsd.RemoveServiceRequest param, boolean optimizeContent)
			throws org.apache.axis2.AxisFault
	{
		try
		{
			return param.getOMElement(org.seerc.fusion.sr.api.xsd.RemoveServiceRequest.MY_QNAME,
					org.apache.axiom.om.OMAbstractFactory.getOMFactory());
		}
		catch (org.apache.axis2.databinding.ADBException e)
		{
			throw org.apache.axis2.AxisFault.makeFault(e);
		}
	}

	private org.apache.axiom.om.OMElement toOM(
			org.seerc.fusion.sr.api.xsd.RemoveServiceResponse param, boolean optimizeContent)
			throws org.apache.axis2.AxisFault
	{
		try
		{
			return param.getOMElement(org.seerc.fusion.sr.api.xsd.RemoveServiceResponse.MY_QNAME,
					org.apache.axiom.om.OMAbstractFactory.getOMFactory());
		}
		catch (org.apache.axis2.databinding.ADBException e)
		{
			throw org.apache.axis2.AxisFault.makeFault(e);
		}
	}

	private org.apache.axiom.om.OMElement toOM(
			org.seerc.fusion.sr.api.xsd.TerminatePublicationSessionRequest param,
			boolean optimizeContent) throws org.apache.axis2.AxisFault
	{
		try
		{
			return param.getOMElement(
					org.seerc.fusion.sr.api.xsd.TerminatePublicationSessionRequest.MY_QNAME,
					org.apache.axiom.om.OMAbstractFactory.getOMFactory());
		}
		catch (org.apache.axis2.databinding.ADBException e)
		{
			throw org.apache.axis2.AxisFault.makeFault(e);
		}
	}

	private org.apache.axiom.om.OMElement toOM(
			org.seerc.fusion.sr.api.xsd.TerminatePublicationSessionResponse param,
			boolean optimizeContent) throws org.apache.axis2.AxisFault
	{
		try
		{
			return param.getOMElement(
					org.seerc.fusion.sr.api.xsd.TerminatePublicationSessionResponse.MY_QNAME,
					org.apache.axiom.om.OMAbstractFactory.getOMFactory());
		}
		catch (org.apache.axis2.databinding.ADBException e)
		{
			throw org.apache.axis2.AxisFault.makeFault(e);
		}
	}

	private org.apache.axiom.om.OMElement toOM(
			org.seerc.fusion.sr.api.xsd.AddServiceWithoutSAWSDLRequest param,
			boolean optimizeContent) throws org.apache.axis2.AxisFault
	{
		try
		{
			return param.getOMElement(
					org.seerc.fusion.sr.api.xsd.AddServiceWithoutSAWSDLRequest.MY_QNAME,
					org.apache.axiom.om.OMAbstractFactory.getOMFactory());
		}
		catch (org.apache.axis2.databinding.ADBException e)
		{
			throw org.apache.axis2.AxisFault.makeFault(e);
		}
	}

	private org.apache.axiom.om.OMElement toOM(
			org.seerc.fusion.sr.api.xsd.AddServiceWithoutSAWSDLResponse param,
			boolean optimizeContent) throws org.apache.axis2.AxisFault
	{
		try
		{
			return param.getOMElement(
					org.seerc.fusion.sr.api.xsd.AddServiceWithoutSAWSDLResponse.MY_QNAME,
					org.apache.axiom.om.OMAbstractFactory.getOMFactory());
		}
		catch (org.apache.axis2.databinding.ADBException e)
		{
			throw org.apache.axis2.AxisFault.makeFault(e);
		}
	}

	private org.apache.axiom.om.OMElement toOM(
			org.seerc.fusion.sr.api.xsd.AddServiceProviderRequest param, boolean optimizeContent)
			throws org.apache.axis2.AxisFault
	{
		try
		{
			return param.getOMElement(
					org.seerc.fusion.sr.api.xsd.AddServiceProviderRequest.MY_QNAME,
					org.apache.axiom.om.OMAbstractFactory.getOMFactory());
		}
		catch (org.apache.axis2.databinding.ADBException e)
		{
			throw org.apache.axis2.AxisFault.makeFault(e);
		}
	}

	private org.apache.axiom.om.OMElement toOM(
			org.seerc.fusion.sr.api.xsd.AddServiceProviderResponse param, boolean optimizeContent)
			throws org.apache.axis2.AxisFault
	{
		try
		{
			return param.getOMElement(
					org.seerc.fusion.sr.api.xsd.AddServiceProviderResponse.MY_QNAME,
					org.apache.axiom.om.OMAbstractFactory.getOMFactory());
		}
		catch (org.apache.axis2.databinding.ADBException e)
		{
			throw org.apache.axis2.AxisFault.makeFault(e);
		}
	}

	private org.apache.axiom.soap.SOAPEnvelope toEnvelope(
			org.apache.axiom.soap.SOAPFactory factory,
			org.seerc.fusion.sr.api.xsd.ModifyServiceResponse param, boolean optimizeContent)
			throws org.apache.axis2.AxisFault
	{
		try
		{
			org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();

			emptyEnvelope.getBody().addChild(
					param.getOMElement(org.seerc.fusion.sr.api.xsd.ModifyServiceResponse.MY_QNAME,
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
			org.seerc.fusion.sr.api.xsd.AddServiceResponse param, boolean optimizeContent)
			throws org.apache.axis2.AxisFault
	{
		try
		{
			org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();

			emptyEnvelope.getBody().addChild(
					param.getOMElement(org.seerc.fusion.sr.api.xsd.AddServiceResponse.MY_QNAME,
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
			org.seerc.fusion.sr.api.xsd.InitiatePublicationSessionResponse param,
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
											org.seerc.fusion.sr.api.xsd.InitiatePublicationSessionResponse.MY_QNAME,
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
			org.seerc.fusion.sr.api.xsd.RemoveServiceProviderResponse param, boolean optimizeContent)
			throws org.apache.axis2.AxisFault
	{
		try
		{
			org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();

			emptyEnvelope.getBody().addChild(
					param.getOMElement(
							org.seerc.fusion.sr.api.xsd.RemoveServiceProviderResponse.MY_QNAME,
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
			org.seerc.fusion.sr.api.xsd.ModifyServiceProviderResponse param, boolean optimizeContent)
			throws org.apache.axis2.AxisFault
	{
		try
		{
			org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();

			emptyEnvelope.getBody().addChild(
					param.getOMElement(
							org.seerc.fusion.sr.api.xsd.ModifyServiceProviderResponse.MY_QNAME,
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
			org.seerc.fusion.sr.api.xsd.RemoveServiceResponse param, boolean optimizeContent)
			throws org.apache.axis2.AxisFault
	{
		try
		{
			org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();

			emptyEnvelope.getBody().addChild(
					param.getOMElement(org.seerc.fusion.sr.api.xsd.RemoveServiceResponse.MY_QNAME,
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
			org.seerc.fusion.sr.api.xsd.TerminatePublicationSessionResponse param,
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
											org.seerc.fusion.sr.api.xsd.TerminatePublicationSessionResponse.MY_QNAME,
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
			org.seerc.fusion.sr.api.xsd.AddServiceWithoutSAWSDLResponse param,
			boolean optimizeContent) throws org.apache.axis2.AxisFault
	{
		try
		{
			org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();

			emptyEnvelope.getBody().addChild(
					param.getOMElement(
							org.seerc.fusion.sr.api.xsd.AddServiceWithoutSAWSDLResponse.MY_QNAME,
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
			org.seerc.fusion.sr.api.xsd.AddServiceProviderResponse param, boolean optimizeContent)
			throws org.apache.axis2.AxisFault
	{
		try
		{
			org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();

			emptyEnvelope.getBody().addChild(
					param.getOMElement(
							org.seerc.fusion.sr.api.xsd.AddServiceProviderResponse.MY_QNAME,
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
			if (org.seerc.fusion.sr.api.xsd.ModifyServiceRequest.class.equals(type))
			{
				return org.seerc.fusion.sr.api.xsd.ModifyServiceRequest.Factory.parse(param
						.getXMLStreamReaderWithoutCaching());
			}

			if (org.seerc.fusion.sr.api.xsd.ModifyServiceResponse.class.equals(type))
			{
				return org.seerc.fusion.sr.api.xsd.ModifyServiceResponse.Factory.parse(param
						.getXMLStreamReaderWithoutCaching());
			}

			if (org.seerc.fusion.sr.api.xsd.AddServiceRequest.class.equals(type))
			{
				return org.seerc.fusion.sr.api.xsd.AddServiceRequest.Factory.parse(param
						.getXMLStreamReaderWithoutCaching());
			}

			if (org.seerc.fusion.sr.api.xsd.AddServiceResponse.class.equals(type))
			{
				return org.seerc.fusion.sr.api.xsd.AddServiceResponse.Factory.parse(param
						.getXMLStreamReaderWithoutCaching());
			}

			if (org.seerc.fusion.sr.api.xsd.InitiatePublicationSessionRequest.class.equals(type))
			{
				return org.seerc.fusion.sr.api.xsd.InitiatePublicationSessionRequest.Factory
						.parse(param.getXMLStreamReaderWithoutCaching());
			}

			if (org.seerc.fusion.sr.api.xsd.InitiatePublicationSessionResponse.class.equals(type))
			{
				return org.seerc.fusion.sr.api.xsd.InitiatePublicationSessionResponse.Factory
						.parse(param.getXMLStreamReaderWithoutCaching());
			}

			if (org.seerc.fusion.sr.api.xsd.RemoveServiceProviderRequest.class.equals(type))
			{
				return org.seerc.fusion.sr.api.xsd.RemoveServiceProviderRequest.Factory.parse(param
						.getXMLStreamReaderWithoutCaching());
			}

			if (org.seerc.fusion.sr.api.xsd.RemoveServiceProviderResponse.class.equals(type))
			{
				return org.seerc.fusion.sr.api.xsd.RemoveServiceProviderResponse.Factory
						.parse(param.getXMLStreamReaderWithoutCaching());
			}

			if (org.seerc.fusion.sr.api.xsd.ModifyServiceProviderRequest.class.equals(type))
			{
				return org.seerc.fusion.sr.api.xsd.ModifyServiceProviderRequest.Factory.parse(param
						.getXMLStreamReaderWithoutCaching());
			}

			if (org.seerc.fusion.sr.api.xsd.ModifyServiceProviderResponse.class.equals(type))
			{
				return org.seerc.fusion.sr.api.xsd.ModifyServiceProviderResponse.Factory
						.parse(param.getXMLStreamReaderWithoutCaching());
			}

			if (org.seerc.fusion.sr.api.xsd.RemoveServiceRequest.class.equals(type))
			{
				return org.seerc.fusion.sr.api.xsd.RemoveServiceRequest.Factory.parse(param
						.getXMLStreamReaderWithoutCaching());
			}

			if (org.seerc.fusion.sr.api.xsd.RemoveServiceResponse.class.equals(type))
			{
				return org.seerc.fusion.sr.api.xsd.RemoveServiceResponse.Factory.parse(param
						.getXMLStreamReaderWithoutCaching());
			}

			if (org.seerc.fusion.sr.api.xsd.TerminatePublicationSessionRequest.class.equals(type))
			{
				return org.seerc.fusion.sr.api.xsd.TerminatePublicationSessionRequest.Factory
						.parse(param.getXMLStreamReaderWithoutCaching());
			}

			if (org.seerc.fusion.sr.api.xsd.TerminatePublicationSessionResponse.class.equals(type))
			{
				return org.seerc.fusion.sr.api.xsd.TerminatePublicationSessionResponse.Factory
						.parse(param.getXMLStreamReaderWithoutCaching());
			}

			if (org.seerc.fusion.sr.api.xsd.AddServiceWithoutSAWSDLRequest.class.equals(type))
			{
				return org.seerc.fusion.sr.api.xsd.AddServiceWithoutSAWSDLRequest.Factory
						.parse(param.getXMLStreamReaderWithoutCaching());
			}

			if (org.seerc.fusion.sr.api.xsd.AddServiceWithoutSAWSDLResponse.class.equals(type))
			{
				return org.seerc.fusion.sr.api.xsd.AddServiceWithoutSAWSDLResponse.Factory
						.parse(param.getXMLStreamReaderWithoutCaching());
			}

			if (org.seerc.fusion.sr.api.xsd.AddServiceProviderRequest.class.equals(type))
			{
				return org.seerc.fusion.sr.api.xsd.AddServiceProviderRequest.Factory.parse(param
						.getXMLStreamReaderWithoutCaching());
			}

			if (org.seerc.fusion.sr.api.xsd.AddServiceProviderResponse.class.equals(type))
			{
				return org.seerc.fusion.sr.api.xsd.AddServiceProviderResponse.Factory.parse(param
						.getXMLStreamReaderWithoutCaching());
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
