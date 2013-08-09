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
import org.seerc.fusion.sr.api.PublicationManagerSkeleton;
import org.seerc.fusion.sr.api.xsd.AddServiceRequest;
import org.seerc.fusion.sr.api.xsd.InitiatePublicationSessionRequest;
import org.seerc.fusion.sr.api.xsd.InitiatePublicationSessionResponse;

public class PublicationManagerTester
{
	
	public static void main(String[] args) 
	{
		PublicationManagerSkeleton mg = new PublicationManagerSkeleton();
		InitiatePublicationSessionRequest request = new InitiatePublicationSessionRequest();
		request.setUsername("juddi");
		request.setPassword("password");
		String token = null;
		try
		{
			InitiatePublicationSessionResponse res = mg.initiatePublicationSession(request);
			token = res.getAuthenticationToken();
		}
		catch (AxisFault e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try
		{
			AddServiceRequest request2 = new AddServiceRequest();
			request2.setAuthenticationToken(token);
			request2.setSawsdlURL("http://www.seerc.org/projects/fusion/test.wsdl");
			request2.setServiceFreeTextDescription("Bleh bloh");
			request2.setServiceName("08.01.08 test service");
			request2.setServiceProviderUUID("A53F2160-BE3E-11DC-A160-E3B377BC0847");
			
			mg.addService(request2 );
		}
		catch (AxisFault e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
