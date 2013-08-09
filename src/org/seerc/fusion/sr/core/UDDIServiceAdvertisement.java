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

import org.uddi4j.util.CategoryBag;


/**
 * @author Dimitrios Kourtesis
 *
 */
public class UDDIServiceAdvertisement
{
	//provided by user
	private String serviceProviderUUID;

	//extracted from portType name
	private String serviceName;

	//provided by user
	private String serviceFreeTextDescription;

	//generated based on provided, extracted, and inferred info
	private CategoryBag serviceCategoryBag;

	/**
	 * @param serviceProviderUUID
	 * @param serviceName
	 * @param serviceFreeTextDescription
	 * @param serviceCategoryBag
	 */
	public UDDIServiceAdvertisement(String serviceProviderUUID, String serviceName,
			String serviceFreeTextDescription, CategoryBag serviceCategoryBag)
	{
		this.serviceProviderUUID = serviceProviderUUID;
		this.serviceName = serviceName;
		this.serviceFreeTextDescription = serviceFreeTextDescription;
		this.serviceCategoryBag = serviceCategoryBag;
	}

	/**
	 * @return the serviceProviderUUID
	 */
	public String getServiceProviderUUID()
	{
		return serviceProviderUUID;
	}

	/**
	 * @param serviceProviderUUID the serviceProviderUUID to set
	 */
	public void setServiceProviderUUID(String providerUUID)
	{
		this.serviceProviderUUID = providerUUID;
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
	 * @return the serviceFreeTextDescription
	 */
	public String getServiceFreeTextDescription()
	{
		return serviceFreeTextDescription;
	}

	/**
	 * @param serviceFreeTextDescription the serviceFreeTextDescription to set
	 */
	public void setServiceFreeTextDescription(String serviceDescription)
	{
		this.serviceFreeTextDescription = serviceDescription;
	}

	/**
	 * @return the serviceCategoryBag
	 */
	public CategoryBag getServiceCategoryBag()
	{
		return serviceCategoryBag;
	}

	/**
	 * @param serviceCategoryBag the serviceCategoryBag to set
	 */
	public void setServiceCategoryBag(CategoryBag categoryBag)
	{
		this.serviceCategoryBag = categoryBag;
	}

	
}
