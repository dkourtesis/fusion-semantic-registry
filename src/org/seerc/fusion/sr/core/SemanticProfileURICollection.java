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

import java.util.List;

/**
 * @author Dimitrios Kourtesis
 *
 */
public class SemanticProfileURICollection
{
	private List<String> matchingFunctionalProfileURIs;
	
	public SemanticProfileURICollection(List<String> profileURIs)
	{
		this.matchingFunctionalProfileURIs = profileURIs;	
	}

	/**
	 * @return the URIs of the profiles 
	 */
	public List<String> getProfileURIs()
	{
		return matchingFunctionalProfileURIs;
	}

	/**
	 * @param the URIs to add to the list
	 */
	public void setProfileURIs(List<String> profileURIs)
	{
		this.matchingFunctionalProfileURIs = profileURIs;
	}
	
}
