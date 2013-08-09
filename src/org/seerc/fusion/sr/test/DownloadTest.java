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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;

public class DownloadTest
{
	public static void main(String[] args) throws IOException 
	{
		//isValidURIWithFragmentDefinedInRetrievableDocument("http://www.seerc.org/fusion/PharosOntology.owl");
		//isValidURIWithFragmentDefinedInRetrievableDocument("http://www.seerc.org/fusion/PharosOntology.owl#");
		//isValidURIWithFragmentDefinedInRetrievableDocument("http://www.seerc.org/fusion/PharosOntologyXXXXXXXXXXX.owl#RetrieveCustomerRecordRFP");
		//isValidURIWithFragmentDefinedInRetrievableDocument("http://www.seerc.org/fusion/PharosOntology.owl#RetrieveCustomerRecordRFPXXXXXX");
		isValidURIWithFragmentDefinedInRetrievableDocument("http://www.seerc.org/fusion/PharosOntology.owl#RetrieveCustomerRecordRFP");
	}
	
	public static boolean isValidURIWithFragmentDefinedInRetrievableDocument(String uriString) throws MalformedURLException, IOException
	{
		String nextLine;
		URI uri = null;
		URL url = null;
		URLConnection urlConn = null;
		InputStreamReader inStream = null;
		BufferedReader buff = null;
		
		try
		{
			uri = new URI(uriString);
		}
		catch (URISyntaxException e)
		{
			throw new MalformedURLException("The provided URI (" + uriString + ") is not well-formed");
		}

		// Get the fragment identifier
		String fragment = uri.getFragment();
		
		// No point in continuing if the URI does not include a fragment component
		if (fragment == null || fragment.length() == 0) 
			throw new MalformedURLException("The provided URI (" + uriString + ") does not include a does not contain an ontology concept identifier (no fragment component present)");
		
		try
		{			
			// Try to create a URL object pointing to the file
			url = uri.toURL();
			
			// Try to connect 
			urlConn = url.openConnection();
			
			// Try to read the document
			inStream = new InputStreamReader(urlConn.getInputStream());
			buff = new BufferedReader(inStream);

			// Read the lines from the file
			while (true)
			{
				nextLine = buff.readLine();
				if (nextLine != null)
				{
					// Check if the fragment identifier is mentioned 
					// in any of the appropriate RDF declarations
					//System.out.println(nextLine);
					if ( nextLine.contains("rdf:about=\"#" + fragment + "\"")
						|| nextLine.contains("rdf:ID=\"" + fragment + "\"")
						|| nextLine.contains("rdf:about=\"" + uriString + "\"") )
					{
						return true;
					}
				}
				else
				{
					break;
				}
			}
		}
		catch (MalformedURLException e)
		{
			throw new MalformedURLException("The provided URI (" + uriString + ") cannot be converted to a valid URL");
		}
		catch (IOException e)
		{
			throw new IOException("A document cannot be retrieved from the path defined in the provided URI (" + uriString.substring(0, uriString.indexOf("#")) + ") ");
		}
		return false;
	}
}
