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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class WSDLUtils
{
	
	/**
	 * Downloads a WSDL file from a URL, stores it to a temp file, and returns
	 * it
	 * 
	 * @param url
	 * @return
	 * @throws IOException
	 */
	public static File downloadTempWSDLFileFromURL(URL url) throws IOException 
	{
		StringBuffer buffer = new StringBuffer();
		String line = null;
		
        // Create a file in the default location for temp files 
        File localFileCopy = File.createTempFile("fusion-sr-", ".wsdl");
        
		// Setup a connection to the specified URL
		URLConnection connection = url.openConnection();
		//connection.setDoOutput(true);
		
		// Get output writer and input reader streams from the connection
		PrintWriter outStream = new PrintWriter(connection.getOutputStream());
		BufferedReader inStream = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		
		// Is this necessary?
		outStream.flush();

		// Read line by line and append to the string buffer until EOF
		while ((line = inStream.readLine()) != null)
		{
			buffer.append(line);
			buffer.append("\r\n");
		}
		        
        // Write to temp file
        BufferedWriter fileWriter = new BufferedWriter(new FileWriter(localFileCopy));
        fileWriter.write(buffer.toString());
        fileWriter.close();
        
        // Close I/O streams
        inStream.close();
		outStream.close();
        
        return localFileCopy;
	}

}
