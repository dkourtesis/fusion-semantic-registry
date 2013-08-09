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
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.wsdl.WSDLException;

import org.seerc.fusion.sr.exceptions.SemanticRegistryMalformedInputException;
import org.seerc.fusion.sr.utils.FileUtils;

import edu.uga.cs.lsdis.sawsdl.*;

/**
 * @author Dimitrios Kourtesis
 *
 */
public class SAWSDLDocumentParser
{
	
	/**
	 * Returns an SAWSDL Definition object by creating a temporary copy of
	 * the WSDL document existing in the specified URL
	 * 
	 * @param sawsdlURL
	 * @return
	 * @throws SemanticRegistryMalformedInputException
	 */
	public edu.uga.cs.lsdis.sawsdl.Definition getSAWSDLDefinition(String sawsdlURL) 
	
	throws SemanticRegistryMalformedInputException
	{
		edu.uga.cs.lsdis.sawsdl.Definition definition = null;
		
		System.setProperty("javax.wsdl.factory.WSDLFactory",
				"edu.uga.cs.lsdis.sawsdl.impl.factory.WSDLFactoryImpl");
		
		File wsdlDoc = null;
		
		try
		{
			// Create a URL referring to the remote WSDL file 
			URL url = new URL(sawsdlURL);
			
			// Download a local/temp copy of the remote WSDL file
			File localTempCopy = FileUtils.downloadTempWSDLFileFromURL(sawsdlURL);
			
			// Get the definition object from the local/temp WSDL file
			definition = edu.uga.cs.lsdis.sawsdl.util.SAWSDLUtility.getDefinitionFromFile(localTempCopy);
		}
		catch (WSDLException e)
		{
			throw new SemanticRegistryMalformedInputException(
					"A problem occured while trying to parse the SAWSDL document retrieved from " + sawsdlURL 
					+ ". One or more SAWSDL annotations in the document are potentially incompatible with the SAWSDL parser.");
		}
		catch (MalformedURLException e)
		{
			throw new SemanticRegistryMalformedInputException(
					"A problem occured while trying to parse the SAWSDL document retrieved from " + sawsdlURL 
					+ ". The URL is malformed.");
		}
		catch (IOException e)
		{
			throw new SemanticRegistryMalformedInputException(
					"A problem occured while trying to parse the SAWSDL document retrieved from " + sawsdlURL 
					+ ". The file could not be loaded.");
		}
		return definition;
	}
	
	
	
	
		

	/**
	 * Extracts annotations from a Definition SAWSDSL object and returns
	 * a SemanticProfileAnnotationCollection bean containing their values 
	 * 
	 * @param definition
	 * @return
	 */
	public SemanticProfileAnnotationCollection extractAnnotationsFromSAWSDLDocument(
			edu.uga.cs.lsdis.sawsdl.Definition definition)
	{
		String serviceName = null;
		List<String> functionalModelReferenceAnnotations = new ArrayList<String>();
		List<String> inputModelReferenceAnnotations = new ArrayList<String>();
		List<String> outputModelReferenceAnnotations = new ArrayList<String>();

		// Get all portTypes in the definition object
		Map portTypes = definition.getPortTypes();

		//System.out.println("Number of PortTypes: " + portTypes.size());

		// // For every portType
		// for (Iterator it = portTypes.entrySet().iterator(); it.hasNext();)
		// {
		//			
		// Map.Entry entry = (Map.Entry) it.next();

		// Assuming only one portType, get the first key-value pair in the Map
		Object[] keyValuePairs = portTypes.entrySet().toArray();
		Map.Entry entry = (Map.Entry) keyValuePairs[0];
			
			Object key = entry.getKey();
			Object value = entry.getValue();
	
			// ...get an SAWSDL portType object
			PortType portType = definition.getSemanticPortType((QName) key);
	
			// Extract the modelRefs from the portType and add them to the list
			List<String> functionalAnnotationsList = extractModelRefsFromPortType(portType);
			
			// Add the annotations to the functionalModelReferenceAnnotations list
			Iterator<String> iterator = functionalAnnotationsList.iterator();
			while (iterator.hasNext())
				functionalModelReferenceAnnotations.add(iterator.next());
	
			// Assuming that there is only one portType per SAWSDL document
			// this assignment shouldn't happen more than once anyway
			serviceName = portType.getQName().getLocalPart();
	
			// Get all operations of the portType
			List<Operation> operationsList = portType.getOperations();
	
			//System.out.println("Number of Operations: " + operationsList.size());
	
			// For every operation
			Iterator allOperations = operationsList.iterator();
			while (allOperations.hasNext())
			{
				Operation op = (Operation) allOperations.next();
	
				//System.out.println("Processing Operation: " + op.getName());
	
				// Get the SAWSDL input message object
				Message inputMessage = definition.getSemanticMessage(op.getInput().getMessage().getQName());
				
				// Get a list of of all modeReference annotations on the input message
				List<String> inputAnnotationsList = extractModelRefsFromMessageParts(inputMessage);
				
				// Add the annotations to the inputModelReferenceAnnotations list
				Iterator<String> allInputAnnotations = inputAnnotationsList.iterator();
				while (allInputAnnotations.hasNext())
					inputModelReferenceAnnotations.add(allInputAnnotations.next());
	
				// Get the SAWSDL output message object
				Message outputMessage = definition.getSemanticMessage(op.getOutput().getMessage().getQName());

				// Get a list of of all modeReference annotations on the output message
				List<String> outputAnnotationsList = extractModelRefsFromMessageParts(outputMessage);
				
				// Add the annotations to the outputModelReferenceAnnotations list
				Iterator<String> allOutputAnnotations = outputAnnotationsList.iterator();
				while (allOutputAnnotations.hasNext())
					outputModelReferenceAnnotations.add(allOutputAnnotations.next());
			
			}// end loop for every operation
		
		// }// end loop for every portType

		System.out.println("Number of extracted functional modelReference URIs: " + functionalModelReferenceAnnotations.size());
		System.out.println("Number of extracted input modelReference URIs: " + inputModelReferenceAnnotations.size());
		System.out.println("Number of extracted output modelReference URIs: " + outputModelReferenceAnnotations.size());

		SemanticProfileAnnotationCollection parseReport = new SemanticProfileAnnotationCollection(
				serviceName,
				functionalModelReferenceAnnotations,
				inputModelReferenceAnnotations, outputModelReferenceAnnotations);

		return parseReport;
	}
	
	
	
	
	

	/**
	 * Extracts the modelReference annotations from a wsdl:portType and returns
	 * their values as a List of Strings
	 * 
	 * @param portType
	 * @return
	 */
	private List<String> extractModelRefsFromPortType(PortType portType)
	{
		List<String> returnedAnnotationsList = new ArrayList<String>();

		//System.out.println("Processing PortType: " + portType.getQName().getLocalPart());

		// Get all extension attributes on that portType
		Map extensionAttributes = portType.getExtensionAttributes();

		// For every extension attribute...
		int mapsize = extensionAttributes.size();
		Object[] keyValuePairs = extensionAttributes.entrySet().toArray();
		
		for (int i = 0; i < mapsize; i++)
		{
			Map.Entry entry = (Map.Entry) keyValuePairs[i];
			Object extKey = entry.getKey();
			Object extValue = entry.getValue();

			// Get the extension attribute as a QName object
			org.apache.xml.utils.QName qualifiedName = org.apache.xml.utils.QName
					.getQNameFromString(extKey.toString());

			// Split the QName into namespace URI and local name
			String localName = qualifiedName.getLocalName();
			String namespaceURI = qualifiedName.getNamespaceURI();

			// If the extension attribute is a modelReference
			if (localName.equalsIgnoreCase("modelReference"))
			{				
				System.out.println("Found a modelReference extension attribute!");
				
				// Get the extension attribute as a QName object
				org.apache.xml.utils.QName qualifiedName2 = org.apache.xml.utils.QName
						.getQNameFromString(extValue.toString());

				// The local name contains a whitespace-separated list of URIs
				// Extract the local name from the QName 
				String localName2 = qualifiedName2.getLocalName();
				
				// And split the string at the whitespace delimiter
				String[] annotations = localName2.split(" ");
				
				// Add each of the URIs to the returnedAnnotationsList
				// (no check for URI well-formedness is made at this point
				for (int j=0; j<annotations.length; j++)
				{
					if ( !InputValidator.isURIWellFormed(annotations[j])  
						&& annotations[j].startsWith("//") )
					{
						annotations[j] = "http:" + annotations[j];
					}
					//System.out.println("Adding modelReference annotation: " + annotations[j]);
					returnedAnnotationsList.add(annotations[j]);
				}
			}
			else
			{
				//System.out.println("No modelReference extension attribute found in the portType.");
			}
		}

		return returnedAnnotationsList;
	}
	
	
	
	
	
	/**
	 * Extracts the modelReference annotations from a wsdl:part and returns
	 * their values as a List of Strings
	 * 
	 * @param semanticMessage
	 * @return
	 */
	public List<String> extractModelRefsFromMessageParts(Message semanticMessage)
	{
		List<String> returnedAnnotationsList = new ArrayList<String>();

		//System.out.println("Processing Message: " + semanticMessage.getQName().getLocalPart());

		// ...get all SAWSDL message parts
		Map parts = semanticMessage.getParts();

		//System.out.println("Number of Message Parts: " + parts.size());

		// For every SAWSDL message part...
		for (Object partKey : parts.keySet())
		{
			// ...get an SAWSDL message part object
			Part semanticPart = semanticMessage
					.getSemanticPart((String) partKey);

			//System.out.println("Processing Message Part: " + semanticPart.getName());

			// Get all extension attributes on that message part
			Map extensionAttributes = semanticPart.getExtensionAttributes();

			// For every extension attribute...
			int mapsize = extensionAttributes.size();
			Object[] keyValuePairs = extensionAttributes.entrySet().toArray();
			for (int i = 0; i < mapsize; i++)
			{
				Map.Entry entry = (Map.Entry) keyValuePairs[i];

				Object extKey = entry.getKey();
				Object extValue = entry.getValue();

				// Get the extension attribute as a QName object
				org.apache.xml.utils.QName qualifiedName = org.apache.xml.utils.QName
						.getQNameFromString(extKey.toString());

				// Split the QName into namespace URI and local name
				String localName = qualifiedName.getLocalName();
				String namespaceURI = qualifiedName.getNamespaceURI();

				// If the extension attribute is a modelReference
				if (localName.equalsIgnoreCase("modelReference"))
				{
					//System.out.println("Found a modelReference extension attribute!");
										
					// Get the extension attribute as a QName object
					org.apache.xml.utils.QName qualifiedName2 = org.apache.xml.utils.QName
							.getQNameFromString(extValue.toString());

					// The local name contains a whitespace-separated list of URIs
					// Extract the local name from the QName 
					String localName2 = qualifiedName2.getLocalName();
					
					// And split the string at the whitespace delimiter
					String[] annotations = localName2.split(" ");
					
					// Add each of the URIs to the returnedAnnotationsList
					// (no check for URI well-formedness is made at this point
					for (int j=0; j<annotations.length; j++)
					{
						if ( !InputValidator.isURIWellFormed(annotations[j])  
							&& annotations[j].startsWith("//") )
						{
							annotations[j] = "http:" + annotations[j];
						}
						//System.out.println("Adding modelReference annotation: " + annotations[j]);
						returnedAnnotationsList.add(annotations[j]);
					}
				}
				else
				{
					//System.out.println("No modelReference extension attribute found.");
				}
			}
		}
		return returnedAnnotationsList;
	}
	
	
	
	
	
	/**
	 * @param semanticMessage
	 * @return
	 */
	public List<String> extractModelRefsFromSchemaElements(Message semanticMessage)
	{
		List<String> returnedAnnotationsList = new ArrayList<String>();
		
		return returnedAnnotationsList;
	}

}
