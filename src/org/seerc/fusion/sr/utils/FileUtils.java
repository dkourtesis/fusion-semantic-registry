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
 
 package org.seerc.fusion.sr.utils;

import java.io.*;
import java.net.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class FileUtils
{
	/**
	 * Downloads a file from a URL and saves it to a local file. (author: Marco Schmidt)
	 * 
	 * @param address
	 * @param localFileName
	 * @return
	 */
	public static File download(String address, String localFileName)
	{
		File localFileCopy = null;
		OutputStream out = null;
		URLConnection conn = null;
		InputStream in = null;
		try
		{
			URL url = new URL(address);
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
			//System.out.println(localFileName + "\t" + numWritten);
			
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
			catch (IOException ioe)
			{
			}
		}
		return localFileCopy;
	}
	
	
	
	
	
	/**
	 * @param address
	 * @return
	 */
	public static File downloadTempWSDLFileFromURL(String address)
	{
		File localFileCopy = null;
		int lastSlashIndex = address.lastIndexOf('/');
		if (lastSlashIndex >= 0 && lastSlashIndex < address.length() - 1)
		{
			localFileCopy = download(address, address.substring(lastSlashIndex + 1));
		}
		else
		{
			System.err.println("Could not figure out local file name for " + address);
		}
		return localFileCopy;
	}
	
	
	
	
	
	/**
	 * @param documentURI
	 * @return
	 */
	public static String getBaseURIFromOntologyDocument(String documentURI)
	{
		File docFile = download(documentURI, "temp.xml");
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
	
	
	
	
	
	public static void main(String[] args) throws IOException
	{
		File tmp = downloadTempWSDLFileFromURL("http://www.seerc.org/projects/fusion/test.wsdl");
		if (tmp == null) System.err.println("Could not download the file");
		else System.out.println(tmp.getCanonicalPath());
	}
}
