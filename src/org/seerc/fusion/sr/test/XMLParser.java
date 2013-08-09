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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.net.URL;
import java.net.URLConnection;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Document;

public class XMLParser
{
	public static void main(String[] args) 
	{
		getBaseURIFromOntologyDocument("http://www.seerc.org/fusion/PharosOntology.owl");
	}
	
	public static File downloadOntologyDocument(String documentURI, String localFileName)
	{
		File localFileCopy = null;
		OutputStream out = null;
		URLConnection conn = null;
		InputStream in = null;
		try
		{
			URL url = new URL(documentURI);
			out = new BufferedOutputStream(new FileOutputStream(localFileName));
			conn = url.openConnection();
			in = conn.getInputStream();
			byte[] buffer = new byte[1024];
			int numRead;
			long numWritten = 0;
			while ((numRead = in.read(buffer)) != -1)
			{
				out.write(buffer, 0, numRead);
				numWritten += numRead;
			}
			localFileCopy = new File(localFileName);
		}
		catch (Exception exception)
		{
			exception.printStackTrace();
		}
		finally
		{
			try
			{
				if (in != null)
				{
					in.close();
				}
				if (out != null)
				{
					out.close();
				}
			}
			catch (IOException e)
			{
			}
		}
		return localFileCopy;
	}
	
	
	
	public static String getBaseURIFromOntologyDocument(String documentURI)
	{
		File docFile = downloadOntologyDocument(documentURI, "temp.xml");
		String baseURI = null;
		try
		{
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(docFile);
			Element root = doc.getDocumentElement();
			baseURI = root.getBaseURI();
			docFile.delete();
		}
		catch (java.io.IOException e)
		{
		}
		catch (Exception e)
		{
		}
		return baseURI;
	}
}
