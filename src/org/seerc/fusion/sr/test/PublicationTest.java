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

import org.seerc.fusion.sr.core.PublicationHandler;
import org.seerc.fusion.sr.exceptions.SemanticRegistryAuthException;
import org.seerc.fusion.sr.exceptions.SemanticRegistryCommunicationException;
import org.seerc.fusion.sr.exceptions.SemanticRegistryConfigurationException;
import org.seerc.fusion.sr.exceptions.SemanticRegistryException;
import org.seerc.fusion.sr.exceptions.SemanticRegistryMalformedInputException;

public class PublicationTest
{

	/**
	 * @param args
	 * @throws SemanticRegistryCommunicationException 
	 * @throws SemanticRegistryAuthException 
	 * @throws SemanticRegistryMalformedInputException 
	 * @throws SemanticRegistryException 
	 */
	public static void main(String[] args) throws SemanticRegistryAuthException, SemanticRegistryCommunicationException, SemanticRegistryMalformedInputException, SemanticRegistryException
	{
		PublicationHandler handler = null;
		try
		{
			handler = new PublicationHandler();
		}
		catch (SemanticRegistryConfigurationException e)
		{
		}
		
		// Get an authorization token
		System.out.println("\nGet authtoken");
		String token = handler.generateAuthenticationToken("juddi", "password");
		System.out.println("Returned authToken: " + token);

		String ProviderUUIDKey = handler.addServiceProvider(token, "Singular ERP",
						"This is the Singular ERP installation at Germanos Headquarters");

//		String ServiceUUIDKey = handler.addService(token, "Discount Calculation Service",
//						"This service calculates customer discount based on a loyalty scheme",
//						"Haven't done that SAWSDL yet", ProviderUUIDKey);

	}

}
