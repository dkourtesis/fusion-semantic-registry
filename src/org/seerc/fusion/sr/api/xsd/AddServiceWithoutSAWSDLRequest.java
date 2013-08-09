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
 
 /**
 * AddServiceWithoutSAWSDLRequest.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.3  Built on : Aug 10, 2007 (04:45:58 LKT)
 */
package org.seerc.fusion.sr.api.xsd;


/**
 *  AddServiceWithoutSAWSDLRequest bean class
 */
public class AddServiceWithoutSAWSDLRequest implements org.apache.axis2.databinding.ADBBean {
    public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName("http://api.sr.fusion.seerc.org/xsd",
            "addServiceWithoutSAWSDLRequest", "ns1");

    /**
     * field for AuthenticationToken
     */
    protected java.lang.String localAuthenticationToken;

    /**
     * field for ServiceName
     */
    protected java.lang.String localServiceName;

    /**
     * field for ServiceFreeTextDescription
     */
    protected java.lang.String localServiceFreeTextDescription;

    /**
     * field for ServiceProviderUUID
     */
    protected java.lang.String localServiceProviderUUID;

    /**
     * field for SawsdlURL
     */
    protected java.lang.String localSawsdlURL;

    /**
     * field for HasCategoryAnnotationURI
     */
    protected java.lang.String localHasCategoryAnnotationURI;

    /**
     * field for HasInputAnnotationURIList
     */
    protected org.seerc.fusion.sr.api.xsd.URIListType localHasInputAnnotationURIList;

    /**
     * field for HasOutputAnnotationURIList
     */
    protected org.seerc.fusion.sr.api.xsd.URIListType localHasOutputAnnotationURIList;

    private static java.lang.String generatePrefix(java.lang.String namespace) {
        if (namespace.equals("http://api.sr.fusion.seerc.org/xsd")) {
            return "ns1";
        }

        return org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
    }

    /**
     * Auto generated getter method
     * @return java.lang.String
     */
    public java.lang.String getAuthenticationToken() {
        return localAuthenticationToken;
    }

    /**
     * Auto generated setter method
     * @param param AuthenticationToken
     */
    public void setAuthenticationToken(java.lang.String param) {
        this.localAuthenticationToken = param;
    }

    /**
     * Auto generated getter method
     * @return java.lang.String
     */
    public java.lang.String getServiceName() {
        return localServiceName;
    }

    /**
     * Auto generated setter method
     * @param param ServiceName
     */
    public void setServiceName(java.lang.String param) {
        this.localServiceName = param;
    }

    /**
     * Auto generated getter method
     * @return java.lang.String
     */
    public java.lang.String getServiceFreeTextDescription() {
        return localServiceFreeTextDescription;
    }

    /**
     * Auto generated setter method
     * @param param ServiceFreeTextDescription
     */
    public void setServiceFreeTextDescription(java.lang.String param) {
        this.localServiceFreeTextDescription = param;
    }

    /**
     * Auto generated getter method
     * @return java.lang.String
     */
    public java.lang.String getServiceProviderUUID() {
        return localServiceProviderUUID;
    }

    /**
     * Auto generated setter method
     * @param param ServiceProviderUUID
     */
    public void setServiceProviderUUID(java.lang.String param) {
        this.localServiceProviderUUID = param;
    }

    /**
     * Auto generated getter method
     * @return java.lang.String
     */
    public java.lang.String getSawsdlURL() {
        return localSawsdlURL;
    }

    /**
     * Auto generated setter method
     * @param param SawsdlURL
     */
    public void setSawsdlURL(java.lang.String param) {
        this.localSawsdlURL = param;
    }

    /**
     * Auto generated getter method
     * @return java.lang.String
     */
    public java.lang.String getHasCategoryAnnotationURI() {
        return localHasCategoryAnnotationURI;
    }

    /**
     * Auto generated setter method
     * @param param HasCategoryAnnotationURI
     */
    public void setHasCategoryAnnotationURI(java.lang.String param) {
        this.localHasCategoryAnnotationURI = param;
    }

    /**
     * Auto generated getter method
     * @return org.seerc.fusion.sr.api.xsd.URIListType
     */
    public org.seerc.fusion.sr.api.xsd.URIListType getHasInputAnnotationURIList() {
        return localHasInputAnnotationURIList;
    }

    /**
     * Auto generated setter method
     * @param param HasInputAnnotationURIList
     */
    public void setHasInputAnnotationURIList(
        org.seerc.fusion.sr.api.xsd.URIListType param) {
        this.localHasInputAnnotationURIList = param;
    }

    /**
     * Auto generated getter method
     * @return org.seerc.fusion.sr.api.xsd.URIListType
     */
    public org.seerc.fusion.sr.api.xsd.URIListType getHasOutputAnnotationURIList() {
        return localHasOutputAnnotationURIList;
    }

    /**
     * Auto generated setter method
     * @param param HasOutputAnnotationURIList
     */
    public void setHasOutputAnnotationURIList(
        org.seerc.fusion.sr.api.xsd.URIListType param) {
        this.localHasOutputAnnotationURIList = param;
    }

    /**
     * isReaderMTOMAware
     * @return true if the reader supports MTOM
     */
    public static boolean isReaderMTOMAware(
        javax.xml.stream.XMLStreamReader reader) {
        boolean isReaderMTOMAware = false;

        try {
            isReaderMTOMAware = java.lang.Boolean.TRUE.equals(reader.getProperty(
                        org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE));
        } catch (java.lang.IllegalArgumentException e) {
            isReaderMTOMAware = false;
        }

        return isReaderMTOMAware;
    }

    /**
     *
     * @param parentQName
     * @param factory
     * @return org.apache.axiom.om.OMElement
     */
    public org.apache.axiom.om.OMElement getOMElement(
        final javax.xml.namespace.QName parentQName,
        final org.apache.axiom.om.OMFactory factory)
        throws org.apache.axis2.databinding.ADBException {
        org.apache.axiom.om.OMDataSource dataSource = new org.apache.axis2.databinding.ADBDataSource(this,
                MY_QNAME) {
                public void serialize(
                    org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter)
                    throws javax.xml.stream.XMLStreamException {
                    AddServiceWithoutSAWSDLRequest.this.serialize(MY_QNAME,
                        factory, xmlWriter);
                }
            };

        return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(MY_QNAME,
            factory, dataSource);
    }

    public void serialize(final javax.xml.namespace.QName parentQName,
        final org.apache.axiom.om.OMFactory factory,
        org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter)
        throws javax.xml.stream.XMLStreamException,
            org.apache.axis2.databinding.ADBException {
        java.lang.String prefix = null;
        java.lang.String namespace = null;

        prefix = parentQName.getPrefix();
        namespace = parentQName.getNamespaceURI();

        if (namespace != null) {
            java.lang.String writerPrefix = xmlWriter.getPrefix(namespace);

            if (writerPrefix != null) {
                xmlWriter.writeStartElement(namespace,
                    parentQName.getLocalPart());
            } else {
                if (prefix == null) {
                    prefix = generatePrefix(namespace);
                }

                xmlWriter.writeStartElement(prefix, parentQName.getLocalPart(),
                    namespace);
                xmlWriter.writeNamespace(prefix, namespace);
                xmlWriter.setPrefix(prefix, namespace);
            }
        } else {
            xmlWriter.writeStartElement(parentQName.getLocalPart());
        }

        namespace = "http://api.sr.fusion.seerc.org/xsd";

        if (!namespace.equals("")) {
            prefix = xmlWriter.getPrefix(namespace);

            if (prefix == null) {
                prefix = generatePrefix(namespace);

                xmlWriter.writeStartElement(prefix, "authenticationToken",
                    namespace);
                xmlWriter.writeNamespace(prefix, namespace);
                xmlWriter.setPrefix(prefix, namespace);
            } else {
                xmlWriter.writeStartElement(namespace, "authenticationToken");
            }
        } else {
            xmlWriter.writeStartElement("authenticationToken");
        }

        if (localAuthenticationToken == null) {
            // write the nil attribute
            throw new org.apache.axis2.databinding.ADBException(
                "authenticationToken cannot be null!!");
        } else {
            xmlWriter.writeCharacters(localAuthenticationToken);
        }

        xmlWriter.writeEndElement();

        namespace = "http://api.sr.fusion.seerc.org/xsd";

        if (!namespace.equals("")) {
            prefix = xmlWriter.getPrefix(namespace);

            if (prefix == null) {
                prefix = generatePrefix(namespace);

                xmlWriter.writeStartElement(prefix, "serviceName", namespace);
                xmlWriter.writeNamespace(prefix, namespace);
                xmlWriter.setPrefix(prefix, namespace);
            } else {
                xmlWriter.writeStartElement(namespace, "serviceName");
            }
        } else {
            xmlWriter.writeStartElement("serviceName");
        }

        if (localServiceName == null) {
            // write the nil attribute
            writeAttribute("xsi", "http://www.w3.org/2001/XMLSchema-instance",
                "nil", "1", xmlWriter);
        } else {
            xmlWriter.writeCharacters(localServiceName);
        }

        xmlWriter.writeEndElement();

        namespace = "http://api.sr.fusion.seerc.org/xsd";

        if (!namespace.equals("")) {
            prefix = xmlWriter.getPrefix(namespace);

            if (prefix == null) {
                prefix = generatePrefix(namespace);

                xmlWriter.writeStartElement(prefix,
                    "serviceFreeTextDescription", namespace);
                xmlWriter.writeNamespace(prefix, namespace);
                xmlWriter.setPrefix(prefix, namespace);
            } else {
                xmlWriter.writeStartElement(namespace,
                    "serviceFreeTextDescription");
            }
        } else {
            xmlWriter.writeStartElement("serviceFreeTextDescription");
        }

        if (localServiceFreeTextDescription == null) {
            // write the nil attribute
            writeAttribute("xsi", "http://www.w3.org/2001/XMLSchema-instance",
                "nil", "1", xmlWriter);
        } else {
            xmlWriter.writeCharacters(localServiceFreeTextDescription);
        }

        xmlWriter.writeEndElement();

        namespace = "http://api.sr.fusion.seerc.org/xsd";

        if (!namespace.equals("")) {
            prefix = xmlWriter.getPrefix(namespace);

            if (prefix == null) {
                prefix = generatePrefix(namespace);

                xmlWriter.writeStartElement(prefix, "serviceProviderUUID",
                    namespace);
                xmlWriter.writeNamespace(prefix, namespace);
                xmlWriter.setPrefix(prefix, namespace);
            } else {
                xmlWriter.writeStartElement(namespace, "serviceProviderUUID");
            }
        } else {
            xmlWriter.writeStartElement("serviceProviderUUID");
        }

        if (localServiceProviderUUID == null) {
            // write the nil attribute
            throw new org.apache.axis2.databinding.ADBException(
                "serviceProviderUUID cannot be null!!");
        } else {
            xmlWriter.writeCharacters(localServiceProviderUUID);
        }

        xmlWriter.writeEndElement();

        namespace = "http://api.sr.fusion.seerc.org/xsd";

        if (!namespace.equals("")) {
            prefix = xmlWriter.getPrefix(namespace);

            if (prefix == null) {
                prefix = generatePrefix(namespace);

                xmlWriter.writeStartElement(prefix, "sawsdlURL", namespace);
                xmlWriter.writeNamespace(prefix, namespace);
                xmlWriter.setPrefix(prefix, namespace);
            } else {
                xmlWriter.writeStartElement(namespace, "sawsdlURL");
            }
        } else {
            xmlWriter.writeStartElement("sawsdlURL");
        }

        if (localSawsdlURL == null) {
            // write the nil attribute
            throw new org.apache.axis2.databinding.ADBException(
                "sawsdlURL cannot be null!!");
        } else {
            xmlWriter.writeCharacters(localSawsdlURL);
        }

        xmlWriter.writeEndElement();

        namespace = "http://api.sr.fusion.seerc.org/xsd";

        if (!namespace.equals("")) {
            prefix = xmlWriter.getPrefix(namespace);

            if (prefix == null) {
                prefix = generatePrefix(namespace);

                xmlWriter.writeStartElement(prefix, "hasCategoryAnnotationURI",
                    namespace);
                xmlWriter.writeNamespace(prefix, namespace);
                xmlWriter.setPrefix(prefix, namespace);
            } else {
                xmlWriter.writeStartElement(namespace,
                    "hasCategoryAnnotationURI");
            }
        } else {
            xmlWriter.writeStartElement("hasCategoryAnnotationURI");
        }

        if (localHasCategoryAnnotationURI == null) {
            // write the nil attribute
            throw new org.apache.axis2.databinding.ADBException(
                "hasCategoryAnnotationURI cannot be null!!");
        } else {
            xmlWriter.writeCharacters(localHasCategoryAnnotationURI);
        }

        xmlWriter.writeEndElement();

        if (localHasInputAnnotationURIList == null) {
            throw new org.apache.axis2.databinding.ADBException(
                "hasInputAnnotationURIList cannot be null!!");
        }

        localHasInputAnnotationURIList.serialize(new javax.xml.namespace.QName(
                "http://api.sr.fusion.seerc.org/xsd",
                "hasInputAnnotationURIList"), factory, xmlWriter);

        if (localHasOutputAnnotationURIList == null) {
            throw new org.apache.axis2.databinding.ADBException(
                "hasOutputAnnotationURIList cannot be null!!");
        }

        localHasOutputAnnotationURIList.serialize(new javax.xml.namespace.QName(
                "http://api.sr.fusion.seerc.org/xsd",
                "hasOutputAnnotationURIList"), factory, xmlWriter);

        xmlWriter.writeEndElement();
    }

    /**
     * Util method to write an attribute with the ns prefix
     */
    private void writeAttribute(java.lang.String prefix,
        java.lang.String namespace, java.lang.String attName,
        java.lang.String attValue, javax.xml.stream.XMLStreamWriter xmlWriter)
        throws javax.xml.stream.XMLStreamException {
        if (xmlWriter.getPrefix(namespace) == null) {
            xmlWriter.writeNamespace(prefix, namespace);
            xmlWriter.setPrefix(prefix, namespace);
        }

        xmlWriter.writeAttribute(namespace, attName, attValue);
    }

    /**
     * Util method to write an attribute without the ns prefix
     */
    private void writeAttribute(java.lang.String namespace,
        java.lang.String attName, java.lang.String attValue,
        javax.xml.stream.XMLStreamWriter xmlWriter)
        throws javax.xml.stream.XMLStreamException {
        if (namespace.equals("")) {
            xmlWriter.writeAttribute(attName, attValue);
        } else {
            registerPrefix(xmlWriter, namespace);
            xmlWriter.writeAttribute(namespace, attName, attValue);
        }
    }

    /**
     * Util method to write an attribute without the ns prefix
     */
    private void writeQNameAttribute(java.lang.String namespace,
        java.lang.String attName, javax.xml.namespace.QName qname,
        javax.xml.stream.XMLStreamWriter xmlWriter)
        throws javax.xml.stream.XMLStreamException {
        java.lang.String attributeNamespace = qname.getNamespaceURI();
        java.lang.String attributePrefix = xmlWriter.getPrefix(attributeNamespace);

        if (attributePrefix == null) {
            attributePrefix = registerPrefix(xmlWriter, attributeNamespace);
        }

        java.lang.String attributeValue;

        if (attributePrefix.trim().length() > 0) {
            attributeValue = attributePrefix + ":" + qname.getLocalPart();
        } else {
            attributeValue = qname.getLocalPart();
        }

        if (namespace.equals("")) {
            xmlWriter.writeAttribute(attName, attributeValue);
        } else {
            registerPrefix(xmlWriter, namespace);
            xmlWriter.writeAttribute(namespace, attName, attributeValue);
        }
    }

    /**
     *  method to handle Qnames
     */
    private void writeQName(javax.xml.namespace.QName qname,
        javax.xml.stream.XMLStreamWriter xmlWriter)
        throws javax.xml.stream.XMLStreamException {
        java.lang.String namespaceURI = qname.getNamespaceURI();

        if (namespaceURI != null) {
            java.lang.String prefix = xmlWriter.getPrefix(namespaceURI);

            if (prefix == null) {
                prefix = generatePrefix(namespaceURI);
                xmlWriter.writeNamespace(prefix, namespaceURI);
                xmlWriter.setPrefix(prefix, namespaceURI);
            }

            if (prefix.trim().length() > 0) {
                xmlWriter.writeCharacters(prefix + ":" +
                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                        qname));
            } else {
                // i.e this is the default namespace
                xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                        qname));
            }
        } else {
            xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                    qname));
        }
    }

    private void writeQNames(javax.xml.namespace.QName[] qnames,
        javax.xml.stream.XMLStreamWriter xmlWriter)
        throws javax.xml.stream.XMLStreamException {
        if (qnames != null) {
            // we have to store this data until last moment since it is not possible to write any
            // namespace data after writing the charactor data
            java.lang.StringBuffer stringToWrite = new java.lang.StringBuffer();
            java.lang.String namespaceURI = null;
            java.lang.String prefix = null;

            for (int i = 0; i < qnames.length; i++) {
                if (i > 0) {
                    stringToWrite.append(" ");
                }

                namespaceURI = qnames[i].getNamespaceURI();

                if (namespaceURI != null) {
                    prefix = xmlWriter.getPrefix(namespaceURI);

                    if ((prefix == null) || (prefix.length() == 0)) {
                        prefix = generatePrefix(namespaceURI);
                        xmlWriter.writeNamespace(prefix, namespaceURI);
                        xmlWriter.setPrefix(prefix, namespaceURI);
                    }

                    if (prefix.trim().length() > 0) {
                        stringToWrite.append(prefix).append(":")
                                     .append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                                qnames[i]));
                    } else {
                        stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                                qnames[i]));
                    }
                } else {
                    stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                            qnames[i]));
                }
            }

            xmlWriter.writeCharacters(stringToWrite.toString());
        }
    }

    /**
     * Register a namespace prefix
     */
    private java.lang.String registerPrefix(
        javax.xml.stream.XMLStreamWriter xmlWriter, java.lang.String namespace)
        throws javax.xml.stream.XMLStreamException {
        java.lang.String prefix = xmlWriter.getPrefix(namespace);

        if (prefix == null) {
            prefix = generatePrefix(namespace);

            while (xmlWriter.getNamespaceContext().getNamespaceURI(prefix) != null) {
                prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
            }

            xmlWriter.writeNamespace(prefix, namespace);
            xmlWriter.setPrefix(prefix, namespace);
        }

        return prefix;
    }

    /**
     * databinding method to get an XML representation of this object
     *
     */
    public javax.xml.stream.XMLStreamReader getPullParser(
        javax.xml.namespace.QName qName)
        throws org.apache.axis2.databinding.ADBException {
        java.util.ArrayList elementList = new java.util.ArrayList();
        java.util.ArrayList attribList = new java.util.ArrayList();

        elementList.add(new javax.xml.namespace.QName(
                "http://api.sr.fusion.seerc.org/xsd", "authenticationToken"));

        if (localAuthenticationToken != null) {
            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                    localAuthenticationToken));
        } else {
            throw new org.apache.axis2.databinding.ADBException(
                "authenticationToken cannot be null!!");
        }

        elementList.add(new javax.xml.namespace.QName(
                "http://api.sr.fusion.seerc.org/xsd", "serviceName"));

        elementList.add((localServiceName == null) ? null
                                                   : org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                localServiceName));

        elementList.add(new javax.xml.namespace.QName(
                "http://api.sr.fusion.seerc.org/xsd",
                "serviceFreeTextDescription"));

        elementList.add((localServiceFreeTextDescription == null) ? null
                                                                  : org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                localServiceFreeTextDescription));

        elementList.add(new javax.xml.namespace.QName(
                "http://api.sr.fusion.seerc.org/xsd", "serviceProviderUUID"));

        if (localServiceProviderUUID != null) {
            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                    localServiceProviderUUID));
        } else {
            throw new org.apache.axis2.databinding.ADBException(
                "serviceProviderUUID cannot be null!!");
        }

        elementList.add(new javax.xml.namespace.QName(
                "http://api.sr.fusion.seerc.org/xsd", "sawsdlURL"));

        if (localSawsdlURL != null) {
            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                    localSawsdlURL));
        } else {
            throw new org.apache.axis2.databinding.ADBException(
                "sawsdlURL cannot be null!!");
        }

        elementList.add(new javax.xml.namespace.QName(
                "http://api.sr.fusion.seerc.org/xsd", "hasCategoryAnnotationURI"));

        if (localHasCategoryAnnotationURI != null) {
            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                    localHasCategoryAnnotationURI));
        } else {
            throw new org.apache.axis2.databinding.ADBException(
                "hasCategoryAnnotationURI cannot be null!!");
        }

        elementList.add(new javax.xml.namespace.QName(
                "http://api.sr.fusion.seerc.org/xsd",
                "hasInputAnnotationURIList"));

        if (localHasInputAnnotationURIList == null) {
            throw new org.apache.axis2.databinding.ADBException(
                "hasInputAnnotationURIList cannot be null!!");
        }

        elementList.add(localHasInputAnnotationURIList);

        elementList.add(new javax.xml.namespace.QName(
                "http://api.sr.fusion.seerc.org/xsd",
                "hasOutputAnnotationURIList"));

        if (localHasOutputAnnotationURIList == null) {
            throw new org.apache.axis2.databinding.ADBException(
                "hasOutputAnnotationURIList cannot be null!!");
        }

        elementList.add(localHasOutputAnnotationURIList);

        return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(qName,
            elementList.toArray(), attribList.toArray());
    }

    /**
     *  Factory class that keeps the parse method
     */
    public static class Factory {
        /**
         * static method to create the object
         * Precondition:  If this object is an element, the current or next start element starts this object and any intervening reader events are ignorable
         *                If this object is not an element, it is a complex type and the reader is at the event just after the outer start element
         * Postcondition: If this object is an element, the reader is positioned at its end element
         *                If this object is a complex type, the reader is positioned at the end element of its outer element
         */
        public static AddServiceWithoutSAWSDLRequest parse(
            javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception {
            AddServiceWithoutSAWSDLRequest object = new AddServiceWithoutSAWSDLRequest();

            int event;
            java.lang.String nillableValue = null;
            java.lang.String prefix = "";
            java.lang.String namespaceuri = "";

            try {
                while (!reader.isStartElement() && !reader.isEndElement())
                    reader.next();

                if (reader.getAttributeValue(
                            "http://www.w3.org/2001/XMLSchema-instance", "type") != null) {
                    java.lang.String fullTypeName = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                            "type");

                    if (fullTypeName != null) {
                        java.lang.String nsPrefix = null;

                        if (fullTypeName.indexOf(":") > -1) {
                            nsPrefix = fullTypeName.substring(0,
                                    fullTypeName.indexOf(":"));
                        }

                        nsPrefix = (nsPrefix == null) ? "" : nsPrefix;

                        java.lang.String type = fullTypeName.substring(fullTypeName.indexOf(
                                    ":") + 1);

                        if (!"addServiceWithoutSAWSDLRequest".equals(type)) {
                            //find namespace for the prefix
                            java.lang.String nsUri = reader.getNamespaceContext()
                                                           .getNamespaceURI(nsPrefix);

                            return (AddServiceWithoutSAWSDLRequest) org.seerc.fusion.sr.api.xsd.ExtensionMapper.getTypeObject(nsUri,
                                type, reader);
                        }
                    }
                }

                // Note all attributes that were handled. Used to differ normal attributes
                // from anyAttributes.
                java.util.Vector handledAttributes = new java.util.Vector();

                reader.next();

                while (!reader.isStartElement() && !reader.isEndElement())
                    reader.next();

                if (reader.isStartElement() &&
                        new javax.xml.namespace.QName(
                            "http://api.sr.fusion.seerc.org/xsd",
                            "authenticationToken").equals(reader.getName())) {
                    java.lang.String content = reader.getElementText();

                    object.setAuthenticationToken(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                            content));

                    reader.next();
                } // End of if for expected property start element

                else {
                    // A start element we are not expecting indicates an invalid parameter was passed
                    throw new org.apache.axis2.databinding.ADBException(
                        "Unexpected subelement " + reader.getLocalName());
                }

                while (!reader.isStartElement() && !reader.isEndElement())
                    reader.next();

                if (reader.isStartElement() &&
                        new javax.xml.namespace.QName(
                            "http://api.sr.fusion.seerc.org/xsd", "serviceName").equals(
                            reader.getName())) {
                    nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                            "nil");

                    if (!"true".equals(nillableValue) &&
                            !"1".equals(nillableValue)) {
                        java.lang.String content = reader.getElementText();

                        object.setServiceName(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                                content));
                    } else {
                        reader.getElementText(); // throw away text nodes if any.
                    }

                    reader.next();
                } // End of if for expected property start element

                else {
                    // A start element we are not expecting indicates an invalid parameter was passed
                    throw new org.apache.axis2.databinding.ADBException(
                        "Unexpected subelement " + reader.getLocalName());
                }

                while (!reader.isStartElement() && !reader.isEndElement())
                    reader.next();

                if (reader.isStartElement() &&
                        new javax.xml.namespace.QName(
                            "http://api.sr.fusion.seerc.org/xsd",
                            "serviceFreeTextDescription").equals(
                            reader.getName())) {
                    nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                            "nil");

                    if (!"true".equals(nillableValue) &&
                            !"1".equals(nillableValue)) {
                        java.lang.String content = reader.getElementText();

                        object.setServiceFreeTextDescription(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                                content));
                    } else {
                        reader.getElementText(); // throw away text nodes if any.
                    }

                    reader.next();
                } // End of if for expected property start element

                else {
                    // A start element we are not expecting indicates an invalid parameter was passed
                    throw new org.apache.axis2.databinding.ADBException(
                        "Unexpected subelement " + reader.getLocalName());
                }

                while (!reader.isStartElement() && !reader.isEndElement())
                    reader.next();

                if (reader.isStartElement() &&
                        new javax.xml.namespace.QName(
                            "http://api.sr.fusion.seerc.org/xsd",
                            "serviceProviderUUID").equals(reader.getName())) {
                    java.lang.String content = reader.getElementText();

                    object.setServiceProviderUUID(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                            content));

                    reader.next();
                } // End of if for expected property start element

                else {
                    // A start element we are not expecting indicates an invalid parameter was passed
                    throw new org.apache.axis2.databinding.ADBException(
                        "Unexpected subelement " + reader.getLocalName());
                }

                while (!reader.isStartElement() && !reader.isEndElement())
                    reader.next();

                if (reader.isStartElement() &&
                        new javax.xml.namespace.QName(
                            "http://api.sr.fusion.seerc.org/xsd", "sawsdlURL").equals(
                            reader.getName())) {
                    java.lang.String content = reader.getElementText();

                    object.setSawsdlURL(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                            content));

                    reader.next();
                } // End of if for expected property start element

                else {
                    // A start element we are not expecting indicates an invalid parameter was passed
                    throw new org.apache.axis2.databinding.ADBException(
                        "Unexpected subelement " + reader.getLocalName());
                }

                while (!reader.isStartElement() && !reader.isEndElement())
                    reader.next();

                if (reader.isStartElement() &&
                        new javax.xml.namespace.QName(
                            "http://api.sr.fusion.seerc.org/xsd",
                            "hasCategoryAnnotationURI").equals(reader.getName())) {
                    java.lang.String content = reader.getElementText();

                    object.setHasCategoryAnnotationURI(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                            content));

                    reader.next();
                } // End of if for expected property start element

                else {
                    // A start element we are not expecting indicates an invalid parameter was passed
                    throw new org.apache.axis2.databinding.ADBException(
                        "Unexpected subelement " + reader.getLocalName());
                }

                while (!reader.isStartElement() && !reader.isEndElement())
                    reader.next();

                if (reader.isStartElement() &&
                        new javax.xml.namespace.QName(
                            "http://api.sr.fusion.seerc.org/xsd",
                            "hasInputAnnotationURIList").equals(
                            reader.getName())) {
                    object.setHasInputAnnotationURIList(org.seerc.fusion.sr.api.xsd.URIListType.Factory.parse(
                            reader));

                    reader.next();
                } // End of if for expected property start element

                else {
                    // A start element we are not expecting indicates an invalid parameter was passed
                    throw new org.apache.axis2.databinding.ADBException(
                        "Unexpected subelement " + reader.getLocalName());
                }

                while (!reader.isStartElement() && !reader.isEndElement())
                    reader.next();

                if (reader.isStartElement() &&
                        new javax.xml.namespace.QName(
                            "http://api.sr.fusion.seerc.org/xsd",
                            "hasOutputAnnotationURIList").equals(
                            reader.getName())) {
                    object.setHasOutputAnnotationURIList(org.seerc.fusion.sr.api.xsd.URIListType.Factory.parse(
                            reader));

                    reader.next();
                } // End of if for expected property start element

                else {
                    // A start element we are not expecting indicates an invalid parameter was passed
                    throw new org.apache.axis2.databinding.ADBException(
                        "Unexpected subelement " + reader.getLocalName());
                }

                while (!reader.isStartElement() && !reader.isEndElement())
                    reader.next();

                if (reader.isStartElement()) {
                    // A start element we are not expecting indicates a trailing invalid property
                    throw new org.apache.axis2.databinding.ADBException(
                        "Unexpected subelement " + reader.getLocalName());
                }
            } catch (javax.xml.stream.XMLStreamException e) {
                throw new java.lang.Exception(e);
            }

            return object;
        }
    } //end of factory class
}
