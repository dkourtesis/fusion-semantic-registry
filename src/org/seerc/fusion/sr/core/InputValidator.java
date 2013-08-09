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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.seerc.fusion.sr.utils.StringUtils;

/**
 * @author Dimitrios Kourtesis
 *
 */
public class InputValidator
{
	
	/**
	 * @param param
	 * @return
	 */
	public static Boolean isTextParameterWellFormed(String param)
	{
		if ((param != null) && (param.length() > 0) && (param.length() < 256)) return true;
		else return false;
	}
	
	
	
	
	
	/**
	 * @param key
	 * @return
	 */
	public static Boolean isUUIDKeyWellFormed(String key)
	{
		if ((key == null) || (key.length() == 0)) return false;
		if (key.length() < 36) return false; 
		if (key.length() > 41) return false; 
		if ((key.length() < 41) && (key.length() != 36)) return false; 
		if (key.length() == 36) 
		{			
			if (isCanonicalUUID(key)) return true;
			else return false;
		}
		if (key.length() == 41) 
		{
			if (!key.startsWith("uuid:")) return false;
			else // i.e. if key.startsWith("uuid:") 
			{
				// Get the UUID portion of the string
				String secondPart = key.substring(5);
				if (isCanonicalUUID(secondPart)) return true;
				else return false;
			}
		}
		return false;
	}
	
	
	
	
	
	/**
	 * @param key
	 * @return
	 */
	public static Boolean isAuthenticationTokenWellFormed(String key)
	{
		if ((key == null) || (key.length() == 0)) return false;
		if (key.length() < 36) return false; 
		if (key.length() > 46) return false; 
		if ((key.length() < 46) && (key.length() != 36)) return false; 
		if (key.length() == 36) 
		{			
			if (isCanonicalUUID(key)) return true;
			else return false;
		}
		if (key.length() == 46) 
		{
			if (!key.startsWith("authToken:")) return false;
			else // i.e. if key.startsWith("authToken:") 
			{
				// Get the UUID portion of the string
				String secondPart = key.substring(10);
				if (isCanonicalUUID(secondPart)) return true;
				else return false;
			}
		}
		return false;
	}
	
	
	
	
	

	/**
	 * In its canonical form, a UUID consists of 32 hexadecimal digits,
	 * displayed in 5 groups separated by 4 hyphens, for a total of 36
	 * characters
	 * 
	 * @param key
	 * @return
	 */
	public static Boolean isCanonicalUUID(String key)
	{
		// Regular expression checking for case-insensitive hex (a to f and 0 to 9) characters in a 8-4-4-4-12 series
		String expression = "[a-fA-F0-9]{8,8}-[a-fA-F0-9]{4,4}-[a-fA-F0-9]{4,4}-[a-fA-F0-9]{4,4}-[a-fA-F0-9]{12,12}";
		return key.matches(expression);
	}
	
	
	

	
	/**
	 * @param listOfAnnotationURIs
	 * @return
	 */
	public static boolean isValidListOfAnnotationURIs(String listOfAnnotationURIs)
	{
		String sawsdlURL = null;
		List<String> hasCategoryAnnotations = new ArrayList<String>();
		List<String> hasInputAnnotations = new ArrayList<String>();
		List<String> hasOutputAnnotations = new ArrayList<String>();
		
		// Tokenise the listOfAnnotationURIs and extract the values above
		Set<String> tokens = StringUtils.getTokensAsSet(listOfAnnotationURIs, " ");
		
		String token = null;
		Iterator<String> it = tokens.iterator();
	    while (it.hasNext()) 
	    {
	    	token = it.next();
	    	if (InputValidator.isSAWSDLURLLocation(token)) sawsdlURL = token;
	    	if (InputValidator.isHasCategoryAnnotation(token)) hasCategoryAnnotations.add(token);
	    	if (InputValidator.isHasInputAnnotation(token)) hasInputAnnotations.add(token);
	    	if (InputValidator.isHasOutputAnnotation(token)) hasOutputAnnotations.add(token);
	    }
	    
	    // Make sure that there exists a sufficient number of annotations
	    if (hasCategoryAnnotations.size() == 1 
	    	&& hasInputAnnotations.size() > 0 
	    	&& hasOutputAnnotations.size() > 0 
	    	&& sawsdlURL != null)
	    	return true;
	    else 
	    	return false;
	}
	



	
	/**
	 * @param token
	 * @return
	 */
	public static boolean isHasCategoryAnnotation(String token)
	{
		return isPrefixedURIWellFormed(token, "hasCategory:");
	}
	
	
	
	
	
	/**
	 * @param token
	 * @return
	 */
	public static boolean isHasInputAnnotation(String token)
	{
		return isPrefixedURIWellFormed(token, "hasInput:");	
	}
	
	
	
	
	
	/**
	 * @param token
	 * @return
	 */
	public static boolean isHasOutputAnnotation(String token)
	{
		return isPrefixedURIWellFormed(token, "hasOutput:");
	}
	
	
	
	
	
	
	/**
	 * @param token
	 * @param prefix
	 * @return
	 */
	public static boolean isPrefixedURIWellFormed(String token, String prefix)
	{
		if (token == null || token.length() == 0 || token.length() > 255) return false;
		if (!token.startsWith(prefix)) return false;
		else // i.e. if the token starts with the prefix
		{
			// Get the URI portion of the string
			String secondPart = token.substring(prefix.length());
			
			// Try to create a valid URI from the second part of the token
			try
			{
				URI test = new URI(secondPart);
				return true;
			}
			catch (URISyntaxException e) 
			{
				return false;
			}
		}
	}
	
	
	
	
	
	/**
	 * @param uri
	 * @return
	 */
	public static boolean isURIWellFormed(String uri)
	{
		if (uri == null || uri.length() == 0 || uri.length() > 255) return false;

		// Try to create a valid URI
		URI test = null;
		try
		{
			test = new URI(uri);
			
			if (test.isAbsolute() == false) 
				return false;
			else
				test.toURL();
			
			return true;
		}
		catch (URISyntaxException e)
		{
			return false;
		}
		catch (MalformedURLException e)
		{
			return false;
		}
		
		
	}
	
	
	
	
	
	
	/**
	 * @param uri
	 * @return
	 */
	public static boolean isURIWellformedAndResolvable(String uri)
	{
		if (uri == null || uri.length() == 0 || uri.length() > 255) return false;

		URI test = null;
		// Try to create a valid URI
		try
		{
			test = new URI(uri);
		}
		catch (URISyntaxException e)
		{
			return false;
		}
		
		return isURLWellFormedAndResolvable(test.normalize().toString());
		
	}
	
	
	
	
	
	/**
	 * @param urlAddress
	 * @return
	 */
	public static boolean isURLWellFormed(String urlAddress)
	{
		if ((urlAddress != null) && (urlAddress.length() > 0)) 
		{

			try
			{
				URL url = new URL(urlAddress);
			}
			catch (MalformedURLException e)
			{
				return false;
			}
			return true;

		}
		else return false;

	}
	
	
	
	
	
	/**
	 * @param urlAddress
	 * @return
	 */
	public static Boolean isURLWellFormedAndResolvable(String urlAddress)
	{
		if ((urlAddress != null) && (urlAddress.length() > 0)) 
		{
			try
			{
				URL url = new URL(urlAddress);
				String protocol = url.getProtocol();
				if (protocol.equalsIgnoreCase("http"))
				{
					HttpURLConnection con = ((HttpURLConnection) url.openConnection());
					if (con.getResponseCode() == HttpURLConnection.HTTP_OK) return true;
				}
				if (protocol.equalsIgnoreCase("file"))
				{
					File file = new File(url.getPath());
					if (file.exists()) return true;
				}
				return false;
			}
			catch (MalformedURLException e)
			{
				return false;
			}
			catch (IOException e)
			{
				return false;
			}
		}
		else return false;
	}
	
	
	
	
	
	/**
	 * @param token
	 * @return
	 */
	public static boolean isSAWSDLURLLocation(String token)
	{
		String prefix = "locationURL:";
		if (token == null || token.length() == 0 || token.length() > 255) return false;
		if (!token.startsWith(prefix)) return false;
		else // i.e. if the token starts with the prefix
		{
			// Get the URL portion of the string
			String secondPart = token.substring(prefix.length());
			
			if (isURLWellFormedAndResolvable(secondPart)) return true;
			else return false;
		}
	}
	
	
	
	
	
	/**
	 * @param uriString
	 * @return
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public static boolean isValidURIWithFragmentDefinedInRetrievableDocument(String uriString) throws MalformedURLException, IOException
	{
		String nextLine;
		URI uri = null;
		URL url = null;
		URLConnection urlConn = null;
		InputStreamReader inStream = null;
		BufferedReader buff = null;
		
		// Try to create a URI from the string
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
