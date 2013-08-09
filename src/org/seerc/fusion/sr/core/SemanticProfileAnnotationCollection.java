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
public class SemanticProfileAnnotationCollection
{
	private String serviceName;
	private List<String> functionalModelReferenceAnnotationURIs;
	private List<String> inputModelReferenceAnnotationURIs;
	private List<String> outputModelReferenceAnnotationURIs;
	
	
	/**
	 * @param serviceName
	 * @param functionalModelReferenceAnnotationURIs
	 * @param inputModelReferenceAnnotationURIs
	 * @param outputModelReferenceAnnotationURIs
	 */
	public SemanticProfileAnnotationCollection(String serviceName,
			List<String> functionalModelReferenceAnnotationURIs,
			List<String> inputModelReferenceAnnotationURIs,
			List<String> outputModelReferenceAnnotationURIs)
	{
		this.serviceName = serviceName;
		this.functionalModelReferenceAnnotationURIs = functionalModelReferenceAnnotationURIs;
		this.inputModelReferenceAnnotationURIs = inputModelReferenceAnnotationURIs;
		this.outputModelReferenceAnnotationURIs = outputModelReferenceAnnotationURIs;
	}

	/**
	 * @return the serviceName
	 */
	public String getServiceName()
	{
		return serviceName;
	}

	/**
	 * @param serviceName the serviceName to set
	 */
	public void setServiceName(String serviceName)
	{
		this.serviceName = serviceName;
	}

	/**
	 * @return the functionalModelReferenceAnnotationURIs
	 */
	public List<String> getFunctionalModelReferenceAnnotationURIs()
	{
		return functionalModelReferenceAnnotationURIs;
	}

	/**
	 * @param functionalModelReferenceAnnotationURIs the functionalModelReferenceAnnotationURIs to set
	 */
	public void setFunctionalModelReferenceAnnotationURIs(
			List<String> functionalModelReferenceAnnotationURIs)
	{
		this.functionalModelReferenceAnnotationURIs = functionalModelReferenceAnnotationURIs;
	}

	/**
	 * @return the inputModelReferenceAnnotationURIs
	 */
	public List<String> getInputModelReferenceAnnotationURIs()
	{
		return inputModelReferenceAnnotationURIs;
	}

	/**
	 * @param inputModelReferenceAnnotationURIs the inputModelReferenceAnnotationURIs to set
	 */
	public void setInputModelReferenceAnnotationURIs(
			List<String> inputModelReferenceAnnotationURIs)
	{
		this.inputModelReferenceAnnotationURIs = inputModelReferenceAnnotationURIs;
	}

	/**
	 * @return the outputModelReferenceAnnotationURIs
	 */
	public List<String> getOutputModelReferenceAnnotationURIs()
	{
		return outputModelReferenceAnnotationURIs;
	}

	/**
	 * @param outputModelReferenceAnnotationURIs the outputModelReferenceAnnotationURIs to set
	 */
	public void setOutputModelReferenceAnnotationURIs(
			List<String> outputModelReferenceAnnotationURIs)
	{
		this.outputModelReferenceAnnotationURIs = outputModelReferenceAnnotationURIs;
	}



	
}
