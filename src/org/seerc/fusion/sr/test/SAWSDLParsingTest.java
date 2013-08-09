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
 
 package org.seerc.fusion.sr.test;

import edu.uga.cs.lsdis.sawsdl.extensions.sawsdl.AttrExtensions;
import edu.uga.cs.lsdis.sawsdl.util.SAWSDLUtility;
import edu.uga.cs.lsdis.sawsdl.*;

import java.io.File;
import java.util.Iterator;
import java.util.Map;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;

/**
 * A complete example that demonstrates the use of the
 * SAWSDL API
 */
/**
 * @author DK
 *
 */
public class SAWSDLParsingTest
{
	/**
	 * The main method - assumes that there is only one argument
	 * passed
	 * Note - Needs Java 5 or higher
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception
	{
		// get the definition
		System.setProperty("javax.wsdl.factory.WSDLFactory",
				"edu.uga.cs.lsdis.sawsdl.impl.factory.WSDLFactoryImpl");

		//File wsdlURI = new File("D:\\GoogleSearch.wsdl");
		File wsdlURI = new File("D:\\SAWSDL normative examples\\15-part-annotation.wsdl");
		Definition definition = SAWSDLUtility.getDefinitionFromFile(wsdlURI);

		assert (definition != null);
		System.out.println("Definition created Successfully!");

		extractModelRefsFromMessageParts(definition);
		
		//get and print the porttypes and their relevant operations
		Map portTypes = definition.getPortTypes();
		for (Object key : portTypes.keySet())
		{
			// Get the portType object through its name (the key is the name)
			PortType semanticPortType = definition.getSemanticPortType((QName) key);
			
			// Get the name of the portType 
			System.out.println("Porttype QName ->"
					+ semanticPortType.getQName());
			System.out.println("Model References ->"
					+ semanticPortType.getModelReferences());
			
			Set annotations = semanticPortType.getModelReferences();
			if (annotations.isEmpty()) System.out.println("============");
			Iterator it2 = annotations.iterator();
			while (it2.hasNext())
			{
				ModelReference m = (ModelReference) it2.next();
				System.out.println(m.getURI().toString());
			}
			

			List operations = semanticPortType.getOperations();

			for (Object operation : operations)
			{
				System.out.println("Operation ->"
						+ ((Operation) operation).getName());
			}

		}

		//get and print the messages
		Map messages = definition.getMessages();
		for (Object key : messages.keySet())
		{
			Message semanticMessage = definition.getSemanticMessage((QName) key);
					
			System.out.println("Message QName ->" + semanticMessage.getQName());

			Map parts = semanticMessage.getParts();

			for (Object partKey : parts.keySet())
			{
				Part semanticPart = semanticMessage.getSemanticPart((String) partKey);
				System.out.println("part ->" + semanticPart);
				System.out.println("part model references ->"
						+ semanticPart.getModelReferences());
				Set annotations = semanticPart.getModelReferences();
				if (annotations.isEmpty()) System.out.println("============");
				
				Map extensionAttributes = semanticPart.getExtensionAttributes();
				for (Object extKey : extensionAttributes.keySet())
				{
					System.out.println("Extension attribute key: " + extKey.toString());
					org.apache.xml.utils.QName qualifiedName = org.apache.xml.utils.QName.getQNameFromString(extKey.toString());
					System.out.println("Extension attribute local name: " + qualifiedName.getLocalName());
					System.out.println("Extension attribute namespace part: " + qualifiedName.getNamespaceURI());
				}
				
				
				for (Object extEntry : extensionAttributes.entrySet())
				{
					System.out.println("Extension attribute entry: " + extEntry.toString());
				}
				
				for (Object extValue : extensionAttributes.values())
				{
					System.out.println("Extension attribute value: " + extValue.toString());
				}
			}

		}

	}
	
	public static void extractModelRefsFromMessageParts(Definition definition)
	{
		// Get all messages in the definition object
		Map messages = definition.getMessages();
		
		// For every message... 
		for (Object key : messages.keySet())
		{
			// ...get an SAWSDL message object
			Message semanticMessage = definition.getSemanticMessage((QName) key);

			System.out.println("Message QName ->" + semanticMessage.getQName());

			// ...get all SAWSDL message parts
			Map parts = semanticMessage.getParts();
			
			// For every SAWSDL message part... 
			for (Object partKey : parts.keySet())
			{
				// ...get an SAWSDL message part object
				Part semanticPart = semanticMessage.getSemanticPart((String) partKey);
				
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
					org.apache.xml.utils.QName qualifiedName = org.apache.xml.utils.QName.getQNameFromString(extKey.toString());
					
					// Split the QName into namespace URI and local name
					String localName = qualifiedName.getLocalName();
					String namespaceURI = qualifiedName.getNamespaceURI();
					
					// If the extension attribute is a modelReference
					if (localName.equalsIgnoreCase("modelReference"))
					{
						System.out.println("Extension attribute is a modelReference!");
						// Keep the value
						System.out.println("i am keeping: " + extValue.toString());
						
						//http://www.w3.org/2002/ws/sawsdl/spec/ontology/purchaseorder#OrderRequest
					}	
				}
			}
		}
	}
}
