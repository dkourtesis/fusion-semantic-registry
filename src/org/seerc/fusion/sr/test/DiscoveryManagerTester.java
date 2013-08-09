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

import org.apache.axis2.AxisFault;
import org.seerc.fusion.sr.api.DiscoveryManagerSkeleton;
import org.seerc.fusion.sr.api.xsd.DoSemanticSearchForServicesRequest;
import org.seerc.fusion.sr.api.xsd.DoSemanticSearchForServicesResponse;


public class DiscoveryManagerTester
{
	
	
	
	public static void main(String[] args) 
	{
		DiscoveryManagerSkeleton mg = new DiscoveryManagerSkeleton();
		
		//String requestedFunctionalProfileURI = "http://www.seerc.org/fusion/PharosOntology.owl#RetrieveCustomerRecordRFP";
		//String requestedFunctionalProfileURI = "http://www.seerc.org/fusion/PharosRomaniaOntology.owl#GetCustomerDataRFP";
		//String requestedFunctionalProfileURI = "http://www.seerc.org/fusion/PharosOntology.owl";
		//String requestedFunctionalProfileURI = "http://www.seerc.org/fusion/PharosOntology.owl#";
		//String requestedFunctionalProfileURI = "http://www.seerc.org/fusion/PharosOntologyXXXXXXXXXXX.owl#RetrieveCustomerRecordRFP";
		//String requestedFunctionalProfileURI = "http://www.seerc.org/fusion/PharosOntology.owl#RetrieveCustomerRecordRFPXXXXXX";
		//String requestedFunctionalProfileURI = "http://www.seerc.org/fusion/PharosOntology.owl#RetrieveCustomerRecordRFP";

		//String requestedFunctionalProfileURI = "file:///C:/FUSION/eclipse/workspace/OWLAPITest/tmp/PharosOntology.owl#RetrieveCustomerRecordRFP";
		//String requestedFunctionalProfileURI = "http://www.seerc.org/fusion/PharosRomaniaOntology.owl#GetCustomerDataRFP";
		//String requestedFunctionalProfileURI = "file:///C:/FUSION/eclipse/workspace/OWLAPITest/tmp/PharosOntology.owl.owl";
		//String requestedFunctionalProfileURI = "file:///C:/FUSION/eclipse/workspace/OWLAPITest/tmp/PharosOntology.owl#";
		//String requestedFunctionalProfileURI = "file:///C:/FUSION/eclipse/workspace/OWLAPITest/tmp/PharosOntologyXXXXXXXXXXX.owl#RetrieveCustomerRecordRFP";
		//String requestedFunctionalProfileURI = "file:///C:/FUSION/eclipse/workspace/OWLAPITest/tmp/PharosOntology.owl#RetrieveCustomerRecordRFPXXXXXX";
		//String requestedFunctionalProfileURI = "file:///C:/FUSION/eclipse/workspace/OWLAPITest/tmp/PharosOntology.owl#RetrieveCustomerRecordRFP";
	
		String requestedFunctionalProfileURI = "http://www.seerc.org/fusion/D43PharosOntology.owl#RetrieveCustomerRecordRFP";
		
		try
		{
			DoSemanticSearchForServicesRequest request = new DoSemanticSearchForServicesRequest();
			request.setRequestFunctionalProfileURI(requestedFunctionalProfileURI);
			DoSemanticSearchForServicesResponse res = mg.doSemanticSearchForServices(request);
			String[] matchingServices = res.getDoSemanticSearchForServicesResponse().getString();
			
			System.out.println("Matching services: ");
			for (int i = 0; i < matchingServices.length; i++)
			{
				System.out.println(matchingServices[i]);
			}
		}
		catch (AxisFault e)
		{
			//System.out.println("Ooops: " + e.getFaultAction());
			//System.out.println("Ooops: " + e.getFaultNode());
			//System.out.println("Ooops: " + e.getFaultRole());
			//System.out.println("Ooops: " + e.getLocalizedMessage());
			//System.out.println("Ooops: " + e.getMessage());
			//System.out.println("Ooops: " + e.getNodeURI());
			//System.out.println("Ooops: " + e.getReason());
			//System.out.println("Ooops: " + e.getCause());
			//System.out.println("Ooops: " + e.getCause().getMessage());
			//System.out.println("Ooops: " + e.getDetail());
			//System.out.println("Ooops: " + e.getDetail().getText());
			//System.out.println("Ooops: " + e.toString());
			System.out.println("Ooops: " + e.getFaultCode().getLocalPart());
			//System.out.println("Ooops: " + e.getFaultCodeElement());
			//System.out.println("Ooops: " + e.getFaultDetailElement());
			//System.out.println("Ooops: " + e.getFaultReasonElement());
		}
	}
	
}
