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
 *  AdminManagerMessageReceiverInOut message receiver
 */
public class AdminManagerMessageReceiverInOut
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

			AdminManagerSkeleton skel = (AdminManagerSkeleton) obj;

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
				if ("setupStandardConfiguration".equals(methodName))
				{
					org.seerc.fusion.sr.api.xsd.SetupStandardConfigurationResponse setupStandardConfigurationResponse9 = null;
					org.seerc.fusion.sr.api.xsd.SetupStandardConfigurationRequest wrappedParam = (org.seerc.fusion.sr.api.xsd.SetupStandardConfigurationRequest) fromOM(
							msgContext.getEnvelope().getBody().getFirstElement(),
							org.seerc.fusion.sr.api.xsd.SetupStandardConfigurationRequest.class,
							getEnvelopeNamespaces(msgContext.getEnvelope()));

					setupStandardConfigurationResponse9 = skel
							.setupStandardConfiguration(wrappedParam);

					envelope = toEnvelope(getSOAPFactory(msgContext),
							setupStandardConfigurationResponse9, false);
				}
				else if ("addRFPToIndex".equals(methodName))
				{
					org.seerc.fusion.sr.api.xsd.AddRFPToIndexResponse addRFPToIndexResponse11 = null;
					org.seerc.fusion.sr.api.xsd.AddRFPToIndexRequest wrappedParam = (org.seerc.fusion.sr.api.xsd.AddRFPToIndexRequest) fromOM(
							msgContext.getEnvelope().getBody().getFirstElement(),
							org.seerc.fusion.sr.api.xsd.AddRFPToIndexRequest.class,
							getEnvelopeNamespaces(msgContext.getEnvelope()));

					addRFPToIndexResponse11 = skel.addRFPToIndex(wrappedParam);

					envelope = toEnvelope(getSOAPFactory(msgContext), addRFPToIndexResponse11,
							false);
				}
				else if ("removeRFPFromIndex".equals(methodName))
				{
					org.seerc.fusion.sr.api.xsd.RemoveRFPFromIndexResponse removeRFPFromIndexResponse13 = null;
					org.seerc.fusion.sr.api.xsd.RemoveRFPFromIndexRequest wrappedParam = (org.seerc.fusion.sr.api.xsd.RemoveRFPFromIndexRequest) fromOM(
							msgContext.getEnvelope().getBody().getFirstElement(),
							org.seerc.fusion.sr.api.xsd.RemoveRFPFromIndexRequest.class,
							getEnvelopeNamespaces(msgContext.getEnvelope()));

					removeRFPFromIndexResponse13 = skel.removeRFPFromIndex(wrappedParam);

					envelope = toEnvelope(getSOAPFactory(msgContext), removeRFPFromIndexResponse13,
							false);
				}
				else if ("refreshIndex".equals(methodName))
				{
					org.seerc.fusion.sr.api.xsd.RefreshIndexResponse refreshIndexResponse15 = null;
					org.seerc.fusion.sr.api.xsd.RefreshIndexRequest wrappedParam = (org.seerc.fusion.sr.api.xsd.RefreshIndexRequest) fromOM(
							msgContext.getEnvelope().getBody().getFirstElement(),
							org.seerc.fusion.sr.api.xsd.RefreshIndexRequest.class,
							getEnvelopeNamespaces(msgContext.getEnvelope()));

					refreshIndexResponse15 = skel.refreshIndex(wrappedParam);

					envelope = toEnvelope(getSOAPFactory(msgContext), refreshIndexResponse15, false);
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
			org.seerc.fusion.sr.api.xsd.RemoveRFPFromIndexRequest param, boolean optimizeContent)
			throws org.apache.axis2.AxisFault
	{
		try
		{
			return param.getOMElement(
					org.seerc.fusion.sr.api.xsd.RemoveRFPFromIndexRequest.MY_QNAME,
					org.apache.axiom.om.OMAbstractFactory.getOMFactory());
		}
		catch (org.apache.axis2.databinding.ADBException e)
		{
			throw org.apache.axis2.AxisFault.makeFault(e);
		}
	}

	private org.apache.axiom.om.OMElement toOM(
			org.seerc.fusion.sr.api.xsd.RemoveRFPFromIndexResponse param, boolean optimizeContent)
			throws org.apache.axis2.AxisFault
	{
		try
		{
			return param.getOMElement(
					org.seerc.fusion.sr.api.xsd.RemoveRFPFromIndexResponse.MY_QNAME,
					org.apache.axiom.om.OMAbstractFactory.getOMFactory());
		}
		catch (org.apache.axis2.databinding.ADBException e)
		{
			throw org.apache.axis2.AxisFault.makeFault(e);
		}
	}

	private org.apache.axiom.om.OMElement toOM(
			org.seerc.fusion.sr.api.xsd.SetupStandardConfigurationRequest param,
			boolean optimizeContent) throws org.apache.axis2.AxisFault
	{
		try
		{
			return param.getOMElement(
					org.seerc.fusion.sr.api.xsd.SetupStandardConfigurationRequest.MY_QNAME,
					org.apache.axiom.om.OMAbstractFactory.getOMFactory());
		}
		catch (org.apache.axis2.databinding.ADBException e)
		{
			throw org.apache.axis2.AxisFault.makeFault(e);
		}
	}

	private org.apache.axiom.om.OMElement toOM(
			org.seerc.fusion.sr.api.xsd.SetupStandardConfigurationResponse param,
			boolean optimizeContent) throws org.apache.axis2.AxisFault
	{
		try
		{
			return param.getOMElement(
					org.seerc.fusion.sr.api.xsd.SetupStandardConfigurationResponse.MY_QNAME,
					org.apache.axiom.om.OMAbstractFactory.getOMFactory());
		}
		catch (org.apache.axis2.databinding.ADBException e)
		{
			throw org.apache.axis2.AxisFault.makeFault(e);
		}
	}

	private org.apache.axiom.om.OMElement toOM(
			org.seerc.fusion.sr.api.xsd.RefreshIndexRequest param, boolean optimizeContent)
			throws org.apache.axis2.AxisFault
	{
		try
		{
			return param.getOMElement(org.seerc.fusion.sr.api.xsd.RefreshIndexRequest.MY_QNAME,
					org.apache.axiom.om.OMAbstractFactory.getOMFactory());
		}
		catch (org.apache.axis2.databinding.ADBException e)
		{
			throw org.apache.axis2.AxisFault.makeFault(e);
		}
	}

	private org.apache.axiom.om.OMElement toOM(
			org.seerc.fusion.sr.api.xsd.RefreshIndexResponse param, boolean optimizeContent)
			throws org.apache.axis2.AxisFault
	{
		try
		{
			return param.getOMElement(org.seerc.fusion.sr.api.xsd.RefreshIndexResponse.MY_QNAME,
					org.apache.axiom.om.OMAbstractFactory.getOMFactory());
		}
		catch (org.apache.axis2.databinding.ADBException e)
		{
			throw org.apache.axis2.AxisFault.makeFault(e);
		}
	}

	private org.apache.axiom.om.OMElement toOM(
			org.seerc.fusion.sr.api.xsd.AddRFPToIndexRequest param, boolean optimizeContent)
			throws org.apache.axis2.AxisFault
	{
		try
		{
			return param.getOMElement(org.seerc.fusion.sr.api.xsd.AddRFPToIndexRequest.MY_QNAME,
					org.apache.axiom.om.OMAbstractFactory.getOMFactory());
		}
		catch (org.apache.axis2.databinding.ADBException e)
		{
			throw org.apache.axis2.AxisFault.makeFault(e);
		}
	}

	private org.apache.axiom.om.OMElement toOM(
			org.seerc.fusion.sr.api.xsd.AddRFPToIndexResponse param, boolean optimizeContent)
			throws org.apache.axis2.AxisFault
	{
		try
		{
			return param.getOMElement(org.seerc.fusion.sr.api.xsd.AddRFPToIndexResponse.MY_QNAME,
					org.apache.axiom.om.OMAbstractFactory.getOMFactory());
		}
		catch (org.apache.axis2.databinding.ADBException e)
		{
			throw org.apache.axis2.AxisFault.makeFault(e);
		}
	}

	private org.apache.axiom.soap.SOAPEnvelope toEnvelope(
			org.apache.axiom.soap.SOAPFactory factory,
			org.seerc.fusion.sr.api.xsd.RemoveRFPFromIndexResponse param, boolean optimizeContent)
			throws org.apache.axis2.AxisFault
	{
		try
		{
			org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();

			emptyEnvelope.getBody().addChild(
					param.getOMElement(
							org.seerc.fusion.sr.api.xsd.RemoveRFPFromIndexResponse.MY_QNAME,
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
			org.seerc.fusion.sr.api.xsd.SetupStandardConfigurationResponse param,
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
											org.seerc.fusion.sr.api.xsd.SetupStandardConfigurationResponse.MY_QNAME,
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
			org.seerc.fusion.sr.api.xsd.RefreshIndexResponse param, boolean optimizeContent)
			throws org.apache.axis2.AxisFault
	{
		try
		{
			org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();

			emptyEnvelope.getBody().addChild(
					param.getOMElement(org.seerc.fusion.sr.api.xsd.RefreshIndexResponse.MY_QNAME,
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
			org.seerc.fusion.sr.api.xsd.AddRFPToIndexResponse param, boolean optimizeContent)
			throws org.apache.axis2.AxisFault
	{
		try
		{
			org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();

			emptyEnvelope.getBody().addChild(
					param.getOMElement(org.seerc.fusion.sr.api.xsd.AddRFPToIndexResponse.MY_QNAME,
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
			if (org.seerc.fusion.sr.api.xsd.RemoveRFPFromIndexRequest.class.equals(type))
			{
				return org.seerc.fusion.sr.api.xsd.RemoveRFPFromIndexRequest.Factory.parse(param
						.getXMLStreamReaderWithoutCaching());
			}

			if (org.seerc.fusion.sr.api.xsd.RemoveRFPFromIndexResponse.class.equals(type))
			{
				return org.seerc.fusion.sr.api.xsd.RemoveRFPFromIndexResponse.Factory.parse(param
						.getXMLStreamReaderWithoutCaching());
			}

			if (org.seerc.fusion.sr.api.xsd.SetupStandardConfigurationRequest.class.equals(type))
			{
				return org.seerc.fusion.sr.api.xsd.SetupStandardConfigurationRequest.Factory
						.parse(param.getXMLStreamReaderWithoutCaching());
			}

			if (org.seerc.fusion.sr.api.xsd.SetupStandardConfigurationResponse.class.equals(type))
			{
				return org.seerc.fusion.sr.api.xsd.SetupStandardConfigurationResponse.Factory
						.parse(param.getXMLStreamReaderWithoutCaching());
			}

			if (org.seerc.fusion.sr.api.xsd.RefreshIndexRequest.class.equals(type))
			{
				return org.seerc.fusion.sr.api.xsd.RefreshIndexRequest.Factory.parse(param
						.getXMLStreamReaderWithoutCaching());
			}

			if (org.seerc.fusion.sr.api.xsd.RefreshIndexResponse.class.equals(type))
			{
				return org.seerc.fusion.sr.api.xsd.RefreshIndexResponse.Factory.parse(param
						.getXMLStreamReaderWithoutCaching());
			}

			if (org.seerc.fusion.sr.api.xsd.AddRFPToIndexRequest.class.equals(type))
			{
				return org.seerc.fusion.sr.api.xsd.AddRFPToIndexRequest.Factory.parse(param
						.getXMLStreamReaderWithoutCaching());
			}

			if (org.seerc.fusion.sr.api.xsd.AddRFPToIndexResponse.class.equals(type))
			{
				return org.seerc.fusion.sr.api.xsd.AddRFPToIndexResponse.Factory.parse(param
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
