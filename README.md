*******************************************************************************

		   FUSION Semantic Registry v1.1 - README

		http://www.seerc.org/fusion/semanticregistry/

*******************************************************************************

The FUSION Semantic Registry is a system allowing web services to be indexed 
and discovered on the basis of semantic descriptions of their interfaces. It is 
a Java Web Application that can be deployed on a standard servlet container and 
accessed by remote clients through SOAP APIs. 

The system was developed by Dimitrios Kourtesis in the context of EU-funded
research project FUSION (http://www.fusion-strep.eu), which was completed in 
2008. It was originally envisaged as a solution offering improved publication 
and discovery for SOAP web services in a SOA environment. However, it can work 
for indexing/discovering any kind of web API, as long as its description follows 
certain modelling conventions. The overall solution approach builds on the 
SAWSDL and OWL standards by W3C, as well as on the UDDI standard by OASIS. 

The starting point for creating a semantic description of a service is having 
an OWL ontology that serves as "common vocabulary" for describing web services 
in the relevant domain (for example, services in the domain of logistics). The 
ontology will provide the "terminology" to talk about what a web service is 
about (e.g. for calculating cost of shipment), and to describe what are the 
business objects it exchanges as inputs and outputs through its interfaces (for 
instance, a shipment order received as input, a shipment quote produced as 
output). Since every "term" defined in an OWL ontology is a resource identified 
by a URI, every service can be described by a minimal set of such URIs that
correspond to representative terms in one or more ontologies. 

For a service to be added to the registry's index (i.e. for the service to be 
"published"), the associated URIs need to be submitted to the registry for 
indexing. There are two ways to do this. 

One way is to enrich the service's WSDL description document, if one exists, by 
adding these URIs as semantic annotations to it. That is, by adding them as 
SAWSDL modelReferences in designated positions inside the WSDL file. Then, the 
document (along with some additional metadata about the service) should be 
submitted to the registry for parsing and indexing. The second way of submitting 
a service description for indexing is to provide the set of URIs directly, 
without an intermediate SAWSDL document. The FUSION Semantic Registry provides 
separate SOAP APIs for each of the two forms of service "publication". 

During indexing, some of the semantic and the non-semantic metadata about the 
service will be added to the registry's Knowledge Base, while other metadata will 
be stored on the registry's UDDI back-end. Apart from its role as a plain 
metadata store, the UDDI back-end also offers support for "non-semantic" web 
service discovery through legacy UDDI interfaces. 

Once a service description is indexed, the registry will consider it as a 
potential match when answering a service discovery query. The service discovery 
query is also formulated as a set of URIs pointing to terms defined in an OWL 
ontology. Service discovery queries are answered with the help of a semantic 
matchmaking engine that compares all known (already indexed) services to a 
service request, to determine integrability and relevance. The matchmaking engine 
inside the registry employs the subsumption checking service of a Description 
Logics reasoner to perform this analysis. By virtue of the semantic ontology
-based description available for each service, it is possible to have quite 
fine-grained matchmaking between service requests and service "advertisements", 
taking into consideration compatibility of inputs/outputs, and relevance of 
functionality. 

FUSION Semantic Registry v1.1 makes use of several open source software 
libraries (jUDDI, UDDI4J, WSDL4J, SAWSDL4J, OWL API, Pellet) and is itself 
released as open source software under the Apache License, Version 2.0. 

Additional information: http://www.seerc.org/fusion/semanticregistry/

Documentation for installing the FUSION Semantic Registry can be found here:
http://www.seerc.org/fusion/semanticregistry/installation.html

Documentation for using the registry's SOAP API can be found here:
http://www.seerc.org/fusion/semanticregistry/usage.html

A presentation about the FUSION Semantic Registry from the ESWC 2008 conference 
can be found here: http://videolectures.net/eswc08_kourtesis_cs/

Bug reports and enhancement requests should be sent to d.kourtesis@gmail.com or 
dkourtesis@seerc.org.


Dimitrios Kourtesis
9 August 2013