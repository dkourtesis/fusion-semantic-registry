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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.io.RDFXMLOntologyFormat;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.semanticweb.owl.model.AddAxiom;
import org.semanticweb.owl.model.OWLAnonymousDescription;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.UnknownOWLOntologyException;
import org.semanticweb.owl.model.OWLOntologyChangeException;
import org.semanticweb.owl.model.OWLOntologyCreationException;
import org.semanticweb.owl.model.OWLOntologyStorageException;
import org.semanticweb.owl.util.OWLEntityCollector;
import org.semanticweb.owl.util.OWLEntityRemover;



/**
 * The RequestFunctionalProfileManager is a utility class to be used for creating 
 * and deleting Request Functional Profiles (RFPs) in a specified ontology file.  
 * 
 * @author Dimitrios Kourtesis, South-East European Research Centre (SEERC)
 */
class RequestFunctionalProfileManager
{
	
	/**
	 * Uses the provided information to construct a Request Functional Profile
	 * (RFP) and store it in RDF/XML format. The RFP will be constructed based
	 * on the modelling conventions adopted in FUSION and will be stored in the
	 * ontology file found at the physical URI that the caller of this method
	 * has provided. The caller must also provide the physical URI at which the
	 * Functional Facet ontology can be found and retrieved. In case the two
	 * URIs point to the same file, the ontology is not loaded twice. Before
	 * constructing the RFP the system performs a check in the Functional Facet
	 * ontology to make sure it contains all the classes and object properties
	 * required for constructing an RFP (i.e. to check if the Functional Facet
	 * ontology is well formed). For this purpose, the caller of the method must
	 * also provide the logical URIs of the ontologies in which the FUSION Data
	 * Facet and the FUSION Taxonomy are stored. 
	 * 
	 * @param rfpClassName
	 * @param targetOntologyPhysicalURI
	 * @param functionalFacetOntologyPhysicalURI
	 * @param dataFacetOntologyPhysicalURI
	 * @param taxonomyOntologyPhysicalURI
	 * @param categoryAnnotations
	 * @param inputAnnotations
	 * @param outputAnnotations
	 * 
	 * @throws OWLOntologyCreationException if one of the ontologies could not
	 *             be loaded, or if the Functional Facet ontology failed the
	 *             validation check
	 * @throws OWLOntologyChangeException if the change could not be applied in
	 *             the OWLOntology model
	 * @throws OWLOntologyStorageException if the ontology file could not be
	 *             saved successfully
	 * @throws Exception if an RFP by the same name is already referenced in the
	 *             specified ontology
	 */
	public void createRFP(
		String rfpClassName, 
		URI targetOntologyPhysicalURI, 
		URI functionalFacetOntologyPhysicalURI, 
		URI dataFacetOntologyPhysicalURI, 
		URI taxonomyOntologyPhysicalURI, 
		List<String> categoryAnnotations, 
		List<String> inputAnnotations, 
		List<String> outputAnnotations) 
		
		throws 
		OWLOntologyCreationException, 
		OWLOntologyChangeException, 
		OWLOntologyStorageException, 
		Exception 
	{
		
		// //////////////////////
		// LOAD THE TARGET ONTOLOGY 
		// //////////////////////

		// Load the ontology from its physical URI (OWL file)
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology targetOntology = null;
		try
		{
			targetOntology = manager.loadOntologyFromPhysicalURI(targetOntologyPhysicalURI);
			System.out.println("Ontology loaded from " + targetOntologyPhysicalURI);
		}
		catch (OWLOntologyCreationException e1)
		{
			System.out.println("OWLOntologyCreationException: " + e1.getCause());
			throw new OWLOntologyCreationException("The target ontology could not be loaded from the specified physical URI: " + targetOntologyPhysicalURI);
		}
		
		// This check is redundant 
		if (targetOntology == null) 
			throw new OWLOntologyCreationException("The target ontology could not be loaded from the specified physical URI: " + targetOntologyPhysicalURI);
		
		// Check if the loaded ontology is actually empty
		if ( 	
				targetOntology.getReferencedClasses().isEmpty() && 
				targetOntology.getReferencedDataProperties().isEmpty() &&
				targetOntology.getReferencedObjectProperties().isEmpty() &&
				targetOntology.getReferencedIndividuals().isEmpty()
			)
			System.out.println("The ontology loaded from the specified physical URI (" + targetOntologyPhysicalURI + ") does not contain any referenced classes, properties, or individuals");

		// Get the ontology's data factory to create the various objects
		OWLDataFactory factory = manager.getOWLDataFactory();

		// Get the ontology's logical URI
		URI ontologyLogicalURI = targetOntology.getURI();
		

		// //////////////////////
		// LOAD THE FUNCTIONAL FACET ONTOLOGY
		// //////////////////////

		// Load the FunctionalFacet ontology from its physical URI (OWL file)
		OWLOntologyManager ffOntologymanager = OWLManager.createOWLOntologyManager();
		OWLOntology ffOntology = null;
		try
		{
			// If the two URIs (targetOntologyPhysicalURI and functionalFacetOntologyPhysicalURI) 
			// point to the same ontology there is no need to load it twice
			if (targetOntologyPhysicalURI.normalize().toString().equalsIgnoreCase(functionalFacetOntologyPhysicalURI.normalize().toString()))
			{
				ffOntology = targetOntology;				
			}
			else
			{
				ffOntology = ffOntologymanager.loadOntologyFromPhysicalURI(functionalFacetOntologyPhysicalURI);
				System.out.println("FunctionalFacet ontology loaded from " + functionalFacetOntologyPhysicalURI);
			}
		}
		catch (OWLOntologyCreationException e)
		{
			e.printStackTrace();
			throw new OWLOntologyCreationException("The Functional Facet ontology could not be loaded from the specified physical URI: " + functionalFacetOntologyPhysicalURI);
		}
		
		// This check is redundant 
		if (ffOntology == null) 
			throw new OWLOntologyCreationException("The Functional Facet ontology could not be loaded from the specified physical URI: " + functionalFacetOntologyPhysicalURI);
		
		// Get the FunctionalFacet ontology's data factory to create the various objects
		OWLDataFactory fffactory = ffOntologymanager.getOWLDataFactory();
		
		// Get the FunctionalFacet ontology's logical URI
		URI functionalFacetOntologyLogicalURI = ffOntology.getURI();
		
		
		// //////////////////////
		// LOAD THE DATA FACET ONTOLOGY
		// //////////////////////

		// Load the Data Facet ontology from its physical URI (OWL file)
		OWLOntologyManager dfOntologymanager = OWLManager.createOWLOntologyManager();
		OWLOntology dfOntology = null;
		try
		{
			dfOntology = dfOntologymanager.loadOntologyFromPhysicalURI(dataFacetOntologyPhysicalURI);
			System.out.println("Ontology loaded from " + dataFacetOntologyPhysicalURI);
		}
		catch (OWLOntologyCreationException e)
		{
			e.printStackTrace();
			throw new OWLOntologyCreationException("The Data Facet ontology could not be loaded from the specified physical URI: " + dataFacetOntologyPhysicalURI);
		}
		
		// Get the FunctionalFacet ontology's logical URI
		URI dataFacetOntologyLogicalURI = dfOntology.getURI();
		
		
		// //////////////////////
		// LOAD THE TAXONOMY ONTOLOGY
		// //////////////////////

		// Load the Data Facet ontology from its physical URI (OWL file)
		OWLOntologyManager taxOntologymanager = OWLManager.createOWLOntologyManager();
		OWLOntology taxOntology = null;
		try
		{
			taxOntology = taxOntologymanager.loadOntologyFromPhysicalURI(taxonomyOntologyPhysicalURI);
			System.out.println("Ontology loaded from " + taxonomyOntologyPhysicalURI);
		}
		catch (OWLOntologyCreationException e)
		{
			e.printStackTrace();
			throw new OWLOntologyCreationException("The Taxonomy ontology could not be loaded from the specified physical URI: " + taxonomyOntologyPhysicalURI);
		}
		
		// Get the FunctionalFacet ontology's logical URI
		URI taxonomyOntologyLogicalURI = taxOntology.getURI();
		
		
		
		// Check if the FunctionalFacet ontology contains all 
		// classes and properties necessary for constructing an RFP
		// If not, there is no point in continuing with execution
		try
		{
			checkWellFormedness(
					ffOntology, 
					fffactory, 
					functionalFacetOntologyLogicalURI, 
					dataFacetOntologyLogicalURI, 
					taxonomyOntologyLogicalURI);
		}
		catch (Exception e)
		{
			throw new OWLOntologyCreationException(
					"The Functional Facet ontology that was loaded from "
							+ functionalFacetOntologyPhysicalURI
							+ " did not pass validation as per FUSION modelling conventions: " 
							+ e.getMessage());
		}
		
		
		
		// //////////////////////
		// CREATE A CLASS FOR THE RFP
		// //////////////////////
		
		// Create a reference to an object representing the RFP root class
		OWLClass rfpRootClass = factory.getOWLClass(URI.create(functionalFacetOntologyLogicalURI + "#RequestFunctionalProfile"));
		
		// Create a reference to an object representing the class of the
		// RFP, using the provided class name
		OWLClass rfpClass = factory.getOWLClass(URI.create(ontologyLogicalURI + "#" + rfpClassName));
		
		// Check if a class by the same name (and thus by the same 
		// URI) is already referenced in the ontology. In such an 
		// event there is no point in continuing with execution. 
		if (targetOntology.getReferencedClasses().contains(rfpClass)) 
			throw new Exception("The specified ontology already contains a reference to an RFP with the provided name");

		// Create a subclass axiom for stating that the class of the specific
		// RFP is a subclass of the RequestFunctionalProfile root class
		OWLAxiom axiom = factory.getOWLSubClassAxiom(rfpClass, rfpRootClass);

		// Add the axiom to the ontology by creating an AddAxiom change object
		AddAxiom addAxiomChange = new AddAxiom(targetOntology, axiom);

		// Apply the axiom addition
		manager.applyChange(addAxiomChange);
		
		
		
		// //////////////////////
		// CREATE A CLASS FOR THE RFP INPUTDATASET
		// //////////////////////
		
		// Create a reference to an object representing the InputDataSet root
		// class
		OWLClass inputDataSetRootClass = factory.getOWLClass(URI.create(functionalFacetOntologyLogicalURI + "#InputDataSet"));
		
		// Create a reference to an object representing the class of the
		// RFP's incoming data
		OWLClass rfpIncomingDataClass = factory.getOWLClass(URI.create(ontologyLogicalURI + "#" + rfpClassName + "RFPInputSet" ));
		
		// Create a subclass axiom for stating that the
		// rfpIncomingDataClass is a subclass of the inputDataSetRootClass
		axiom = factory.getOWLSubClassAxiom(rfpIncomingDataClass, inputDataSetRootClass);

		// Add the axiom to the ontology by creating an AddAxiom change object
		addAxiomChange = new AddAxiom(targetOntology, axiom);

		// Apply the axiom addition
		manager.applyChange(addAxiomChange);

		
		
		// //////////////////////
		// CREATE A CLASS FOR THE RFP OUTPUTDATASET
		// //////////////////////
		
		// Create a reference to an object representing the OutputDataSet root
		// class
		OWLClass outputDataSetRootClass = factory.getOWLClass(URI.create(functionalFacetOntologyLogicalURI + "#OutputDataSet"));
		
		// Create a reference to an object representing the class of the
		// RFP's outgoing data
		OWLClass rfpOutgoingDataClass = factory.getOWLClass(URI.create(ontologyLogicalURI + "#" + rfpClassName + "RFPOutputSet" ));
		
		// Create a subclass axiom for stating that the
		// outgoingDataClass is a subclass of the outputDataSetRootClass
		axiom = factory.getOWLSubClassAxiom(rfpOutgoingDataClass, outputDataSetRootClass);

		// Add the axiom to the ontology by creating an AddAxiom change object
		addAxiomChange = new AddAxiom(targetOntology, axiom);

		// Apply the axiom addition
		manager.applyChange(addAxiomChange);
		
		
		
		// //////////////////////
		// CREATE RESTICTIONS FOR THE RFP hasCategory PROPERTY
		// //////////////////////
		
		// Create a reference to an object representing the hasCategory object
		// property
		OWLObjectProperty hasCategory = factory.getOWLObjectProperty(URI.create(functionalFacetOntologyLogicalURI + "#hasCategory"));
		
		// Create a set to hold the restrictions to be created for the above
		// object property
		Set<OWLDescription> rfpCategoryClassRestrictionSet = new HashSet<OWLDescription>();
		
        // Create a restriction to describe the class of individuals that have
		// at least one hasCategory relationship to an individual of class
		// DataProvidedBy_xxxxx, and a closure axiom restriction
		rfpCategoryClassRestrictionSet.addAll(createExistentialAndUniversalRestrictions (factory, hasCategory, categoryAnnotations));
		
		
		
		// //////////////////////
		// CREATE RESTICTIONS FOR THE hasOutput PROPERTY
		// //////////////////////
		
		// Create a reference to an object representing the hasOutput object
		// property
		OWLObjectProperty hasOutput = factory.getOWLObjectProperty(URI.create(functionalFacetOntologyLogicalURI + "#hasOutput"));
		
		// Create a set to hold the restrictions to be created for the above
		// object property
		Set<OWLDescription> rfpOutgoingDataClassRestrictionSet = new HashSet<OWLDescription>();
		
		// Create a list containing only one element: the URI of the above class
		List<String> rfpOutgoingDataClassURI = new ArrayList<String>();
		rfpOutgoingDataClassURI.add(rfpOutgoingDataClass.getURI().toString());
		
        // Create a restriction to describe the class of individuals that have
		// at least one hasOutput relationship to an individual of class
		// DataProvidedBy_xxxxx, and a closure axiom restriction
		rfpOutgoingDataClassRestrictionSet.addAll(createExistentialAndUniversalRestrictions (factory, hasOutput, rfpOutgoingDataClassURI));
				
		
		
		// //////////////////////
		// CREATE RESTICTIONS FOR THE hasInput PROPERTY
		// //////////////////////
		
		// Create a reference to an object representing the hasInput object
		// property
		OWLObjectProperty hasInput = factory.getOWLObjectProperty(URI.create(functionalFacetOntologyLogicalURI + "#hasInput"));
		
		// Create a set to hold the restrictions to be created for the above
		// object property
		Set<OWLDescription> rfpIncomingDataClassRestrictionSet = new HashSet<OWLDescription>();
		
		// Create a list containing only one element: the URI of the above class
		List<String> rfpIncomingDataClassURI = new ArrayList<String>();
		rfpIncomingDataClassURI.add(rfpIncomingDataClass.getURI().toString());
		
        // Create a restriction to describe the class of individuals that have
		// at least one hasInput relationship to an individual of class
		// xxxxxInputSet, and a closure axiom restriction
		rfpIncomingDataClassRestrictionSet.addAll(createExistentialAndUniversalRestrictions (factory, hasInput, rfpIncomingDataClassURI));
				
				
		
		// //////////////////////
		// ADD RESTRICTIONS FOR THE RFP CLASS TO THE ONTOLOGY
		// //////////////////////
		
		// Create a set to hold the restrictions to be created for the RFP class
		Set<OWLDescription> rfpRestrictionSet = new HashSet<OWLDescription>();
		
		// Add all restrictions to the rfpRestrictionSet
		rfpRestrictionSet.addAll(rfpIncomingDataClassRestrictionSet);
		rfpRestrictionSet.addAll(rfpOutgoingDataClassRestrictionSet);
		rfpRestrictionSet.addAll(rfpCategoryClassRestrictionSet);
		
		// Create an anonymous class description containing the
		// intersection of all existential and universal restrictions
		OWLAnonymousDescription anonymousClassDescription = factory.getOWLObjectIntersectionOf(rfpRestrictionSet);
		
		// Create a set containing the rfpClass and the anonymousClassDescription
		Set<OWLDescription> rfpClassPlusAnonymousClass = new HashSet<OWLDescription>();
		rfpClassPlusAnonymousClass.add(rfpClass);
		rfpClassPlusAnonymousClass.add(anonymousClassDescription);
		
        // Create an axiom stating that the rfpClass is equivalent
		// to the anonymous class description that represents the 
		// intersection of existential and universal restrictions
		OWLAxiom equivalentClassesAxiom = factory.getOWLEquivalentClassesAxiom(rfpClassPlusAnonymousClass);
        
        // Apply the axiom addition
        addAxiomChange = new AddAxiom(targetOntology, equivalentClassesAxiom);
        manager.applyChange(addAxiomChange);
		
		
				
		// //////////////////////
		// ADD RESTRICTIONS TO THE hasDataParameter 
		// PROPERTY OF THE RFP INPUTDATASET
		// //////////////////////
		
		// Create a set to hold the restrictions to be created
		Set<OWLDescription> rfpIncomingDataRestrictionSet = new HashSet<OWLDescription>();

		// Create a reference to an object representing the hasDataParameter
		// object property
		OWLObjectProperty hasDataParameter = factory.getOWLObjectProperty(URI.create(functionalFacetOntologyLogicalURI + "#hasDataParameter"));
		
		// For every hasInput annotation: create an existential restriction to
		// describe the class of individuals that have at least one hasInput
		// relationship to an individual of class X (universal restriction
		// should not be created, in order to allow for non-exact match)
		rfpIncomingDataRestrictionSet.addAll(createExistentialAndUniversalRestrictions (factory, hasDataParameter, inputAnnotations));
		
		// Print the number of restrictions created
		//System.out.println("Number of existential and universal restrictions in incomingDataRestrictionSet: " + rfpIncomingDataRestrictionSet.size());
		
		// Create an anonymous class description containing the
		// intersection of all existential and universal restrictions
		OWLAnonymousDescription anonymousRfpIncomingDataClassDescription = factory.getOWLObjectIntersectionOf(rfpIncomingDataRestrictionSet);
		
		// Create a set containing the incomingDataClass and the
		// anonymousIncomingDataClassDescription
		Set<OWLDescription> rfpIncomingDataClassPlusAnonymousDescription = new HashSet<OWLDescription>();
		rfpIncomingDataClassPlusAnonymousDescription.add(rfpIncomingDataClass);
		rfpIncomingDataClassPlusAnonymousDescription.add(anonymousRfpIncomingDataClassDescription);
		
        // Create an axiom stating that the RFP class is equivalent to 
		// the anonymous class description representing the intersection 
		// of existential and universal restrictions
		equivalentClassesAxiom = factory.getOWLEquivalentClassesAxiom(rfpIncomingDataClassPlusAnonymousDescription);
        
        // Apply the axiom addition
        addAxiomChange = new AddAxiom(targetOntology, equivalentClassesAxiom);
        manager.applyChange(addAxiomChange);
        
		
		
		// //////////////////////
		// ADD RESTRICTIONS TO THE hasDataParameter 
		// PROPERTY OF THE RFP's OUTPUTDATASET
		// //////////////////////
		
		// Create a set to hold the restrictions to be created
		Set<OWLDescription> rfpOutgoingDataRestrictionSet = new HashSet<OWLDescription>();
		
		// For every hasOutput annotation: create an existential restriction to
		// describe the class of individuals that have at least one hasOutput
		// relationship to an individual of class X, and also create a universal
		// restriction to serve as a closure axiom
		rfpOutgoingDataRestrictionSet.addAll(createExistentialRestrictions (factory, hasDataParameter, outputAnnotations));
		
		// Print the number of restrictions created
		//System.out.println("Number of existential and universal restrictions in outgoingDataRestrictionSet: " + rfpOutgoingDataRestrictionSet.size());
		
		// Create an anonymous class description containing the
		// intersection of all existential and universal restrictions
		OWLAnonymousDescription anonymousRfpOutgoingDataClassDescription = factory.getOWLObjectIntersectionOf(rfpOutgoingDataRestrictionSet);
		
		// Create a set containing the incomingDataClass and the
		// anonymousIncomingDataClassDescription
		Set<OWLDescription> rfpOutgoingDataClassPlusAnonymousDescription = new HashSet<OWLDescription>();
		rfpOutgoingDataClassPlusAnonymousDescription.add(rfpOutgoingDataClass);
		rfpOutgoingDataClassPlusAnonymousDescription.add(anonymousRfpOutgoingDataClassDescription);
		
        // Create an axiom stating that the RFP is equivalent to 
		// the anonymous class description representing the intersection 
		// of existential and universal restrictions
		equivalentClassesAxiom = factory.getOWLEquivalentClassesAxiom(rfpOutgoingDataClassPlusAnonymousDescription);
        
        // Apply the axiom addition
        addAxiomChange = new AddAxiom(targetOntology, equivalentClassesAxiom);
        manager.applyChange(addAxiomChange);
		
		
		
		// //////////////////////
		// SAVE THE ONTOLOGY
		// //////////////////////
		try
		{
			// Save the ontology in RDF/XML format, disregarding 
			// any original format that the loaded ontology may have had
			manager.saveOntology(targetOntology,  new RDFXMLOntologyFormat());
			System.out.println("RFP added to the ontology");
		}
		catch (OWLOntologyStorageException e)
		{
			e.printStackTrace();
			throw new OWLOntologyStorageException ("The RFP could not be saved to the specified ontology (" + targetOntologyPhysicalURI + ")");
		}
	}// end method
	
	
	
	
	
	
	
	/**
	 * Loads the specified ontology file and removes the following named OWL
	 * classes: (i) the OWLClass of the RFP, (ii) the OWLClass of the
	 * InputDataSet with which the RFP is associated, and (iii), the OWLClass of
	 * the OutputDataSet with which the RFP is associated. The ontology is saved
	 * back in its original format (e.g. RDF/XML or OWL/XML). 
	 * 
	 * @param rfpClassName the name of the RFP class to remove
	 * @param targetOntologyPhysicalURI the URI
	 * 
	 * @throws OWLOntologyCreationException if one of the ontologies could not
	 *             be loaded, or if the Functional Facet ontology failed the
	 *             validation check
	 * @throws OWLOntologyChangeException if the change could not be applied in
	 *             the OWLOntology model
	 * @throws OWLOntologyStorageException if the ontology file could not be
	 *             saved successfully
	 * @throws Exception if an RFP with the provided name does not exist in the
	 *             specified ontology
	 */
	public void deleteRFP(
		String rfpClassName, 
		URI targetOntologyPhysicalURI,
		URI functionalFacetOntologyPhysicalURI) 
		
		throws 
		OWLOntologyCreationException, 
		OWLOntologyChangeException, 
		OWLOntologyStorageException, 
		Exception 
	{
		
		// //////////////////////
		// LOAD THE TARGET ONTOLOGY 
		// //////////////////////
		
		// Load the ontology from its physical URI (OWL file)
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology targetOntology = null;
		try
		{
			targetOntology = manager.loadOntologyFromPhysicalURI(targetOntologyPhysicalURI);
			System.out.println("Ontology loaded from " + targetOntologyPhysicalURI);
		}
		catch (OWLOntologyCreationException e)
		{
			e.printStackTrace();
			throw new OWLOntologyCreationException("The target ontology could not be loaded from the specified physical URI: " + targetOntologyPhysicalURI);
		}

		// Get the ontology's data factory to create the various objects
		OWLDataFactory factory = manager.getOWLDataFactory();

		// Get the ontology's logical URI
		URI ontologyLogicalURI = targetOntology.getURI();
		
		
		// //////////////////////
		// LOAD THE FUNCTIONAL FACET ONTOLOGY
		// //////////////////////

		// Load the FunctionalFacet ontology from its physical URI (OWL file)
		OWLOntologyManager ffOntologymanager = OWLManager.createOWLOntologyManager();
		OWLOntology ffOntology = null;
		try
		{
			// If the two URIs (targetOntologyPhysicalURI and functionalFacetOntologyPhysicalURI) 
			// point to the same ontology there is no need to load it twice
			if (targetOntologyPhysicalURI.normalize().toString().equalsIgnoreCase(functionalFacetOntologyPhysicalURI.normalize().toString()))
			{
				ffOntology = targetOntology;				
			}
			else
			{
				ffOntology = ffOntologymanager.loadOntologyFromPhysicalURI(functionalFacetOntologyPhysicalURI);
				System.out.println("FunctionalFacet ontology loaded from " + functionalFacetOntologyPhysicalURI);
			}
		}
		catch (OWLOntologyCreationException e)
		{
			e.printStackTrace();
			throw new OWLOntologyCreationException("The Functional Facet ontology could not be loaded from the specified physical URI: " + functionalFacetOntologyPhysicalURI);
		}
		
		// This check is redundant 
		if (ffOntology == null) 
			throw new OWLOntologyCreationException("The Functional Facet ontology could not be loaded from the specified physical URI: " + functionalFacetOntologyPhysicalURI);
		
		// Get the FunctionalFacet ontology's logical URI
		URI functionalFacetOntologyLogicalURI = ffOntology.getURI();

		
		// Create a reference to an object representing the class of the
		// RFP, using the provided class name
		OWLClass rfpClass = factory.getOWLClass(URI.create(ontologyLogicalURI + "#" + rfpClassName));
		
		// Check if a class by the same name (and thus by the same 
		// URI) is already referenced in the ontology. In this is 
		// not the case there is no point in continuing with execution.
		if (!targetOntology.getReferencedClasses().contains(rfpClass)) 
			throw new Exception("The specified ontology does not contain any reference to an RFP with the provided name");
		
		// Create an entity remover that will be asked to visit: 
		// 1. the OWLClass of the RFP 
		// 2. the OWLClass of the InputDataSet with which the RFP is associated
		// 3. the OWLClass of the OutputDataSet with which the RFP is associated
		OWLEntityRemover remover = new OWLEntityRemover(manager, Collections.singleton(targetOntology));
				
		// Create a collector to pick up all named classes
		// referenced within the equivalent class axiom of the RFP
		OWLEntityCollector collector = new OWLEntityCollector();

		// Speed things up by asking the collector to collect only named classes
		collector.setCollectClasses(true);
		collector.setCollectDataProperties(false);
		collector.setCollectObjectProperties(false);
		collector.setCollectIndividuals(false);
		collector.setCollectDataTypes(false);
		
		// Get the equivalent class axiom of the RFP class. By convention
		// in the FUSION project, there should be only one equivalent
		// class axiom, so this loop should be executed only once
		for (OWLEquivalentClassesAxiom ax : targetOntology.getEquivalentClassesAxioms(rfpClass))
		{
			// System.out.println("Equivalent class axiom: " + ax.toString());

			// By convention in the FUSION project, there should be two
			// OWLDescription objects in the axiom: 1) the named RFP class,
			// and 2) the anonymous class defined as an intersection of
			// existential and universal restrictions on object properties
			// (i.e. the hasCategory, hasInput, and hasOutput
			// properties)
			for (OWLDescription desc : ax.getDescriptions())
			{
				// Send the collector only to the second component
				// of the axiom (i.e. only to the anonymous class)
				if (desc.isAnonymous())
				{
					// System.out.println("Collecting filler classes from
					// equivalent class axiom...");
					desc.accept(collector);
				}
			}
		}
		
		// Get the filler classes from the collector
		Set<OWLEntity> collectedFillerClasses = collector.getObjects();
					
		// By convention in the FUSION project, there should be three
		// classes left inside the collectedFillerClasses set:
		// 1) the filler class for the hasCategory property restriction,
		// 2) the filler class for the hasInput property restriction, and
		// 3) the filler class for the hasOutput property restriction
		
		// We will need to convert each retrieved OWLEntity to an OWLClass
		OWLClass fillerCls = null;
		
		// Create a reference to an object representing the InputDataSet class
		OWLClass inputDataSetRootClass = factory.getOWLClass(URI.create(functionalFacetOntologyLogicalURI + "#InputDataSet"));
		
		// Create a reference to an object representing the OutputDataSet class
		OWLClass outputDataSetRootClass = factory.getOWLClass(URI.create(functionalFacetOntologyLogicalURI + "#OutputDataSet"));
		
		// Determine the property corresponding to each of the three
		// fillers, by checking if a filler is an asserted subclass of
		// 1) inputDataSetRootClass, 2) outputDataSetRootClass,
		// or 3) none of the two (i.e. a taxonomy annotation)
		for (OWLEntity filler : collectedFillerClasses)
		{
			fillerCls = factory.getOWLClass(filler.getURI().normalize());
			// If the fillerCls is an asserted subclass of #InputDataSet in the given ontology...
			if (inputDataSetRootClass.getSubClasses(targetOntology).contains((OWLDescription)fillerCls))
			{
				System.out.println("Removing " + fillerCls + " (" + rfpClass + " hasInput " + filler.getURI().normalize() + ")" );
				fillerCls.accept(remover);
			}
			// If the fillerCls is an asserted subclass of #OutputDataSet in the given ontology...			
			if (outputDataSetRootClass.getSubClasses(targetOntology).contains((OWLDescription)fillerCls))
			{
				System.out.println("Removing " + fillerCls + " (" + rfpClass + " hasOutput " + filler.getURI().normalize() + ")" );
				fillerCls.accept(remover);
			}
		}
	
		// Finally, ask the RFP class to also accept a visit from the entity remover
		System.out.println("Removing " + rfpClass);
		rfpClass.accept(remover);

		// Get the changes necessary to remove the class from the ontology
		manager.applyChanges(remover.getChanges());

		// Store the updated ontology back to the file
		try
		{
			manager.saveOntology(targetOntology);
			System.out.println("RFP (" + rfpClassName + ") removed successfully from the ontology");
		}
		catch (UnknownOWLOntologyException e)
		{
			throw new Exception("Problem removing RFP (" + rfpClassName + ") from the ontology file (" + targetOntologyPhysicalURI + ")");
		}
		catch (OWLOntologyStorageException e)
		{
			throw new OWLOntologyStorageException("Problem saving the updated ontology (" + targetOntologyPhysicalURI + ")");
		}

	}
	
	
	
	
	
	
	
	/**
	 * Checks if the specified ontology contains all classes and properties
	 * necessary for constructing Functional Profiles
	 * 
	 * These include: 
	 * 1. FunctionalFacetEntity 
	 * 2. AdvertisedFunctionalProfile (as a subclass of FunctionalFacetEntity) 
	 * 3. RequestedFunctionalProfile (as a subclass of FunctionalFacetEntity) 
	 * 4. DataSet class 
	 * 5. InputDataSet (as a subclass of DataSet) 
	 * 6. OutputDataSet (as a subclass of DataSet) 
	 * 7. hasCategory object property (with domain=FunctionalFacetEntity and range=TaxonomyEntity) 
	 * 8. hasInput object property (with domain=FunctionalFacetEntity and range=InputDataSet) 
	 * 9. hasOutput object property (with domain=FunctionalFacetEntity and range=OutputDataSet) 
	 * 10. hasDataParameter object property (with domain=DataSet and range=DataFacetEntity)
	 * 
	 * @param ffOntology
	 * @param fffactory
	 * @param functionalFacetOntologyLogicalURI
	 * @param dataFacetOntologyLogicalURI
	 * @param taxonomyOntologyLogicalURI
	 * 
	 * @return 
	 * 
	 * @throws Exception 
	 */
	private void checkWellFormedness(
		OWLOntology ffOntology, 
		OWLDataFactory fffactory, 
		URI functionalFacetOntologyLogicalURI, 
		URI dataFacetOntologyLogicalURI, 
		URI taxonomyOntologyLogicalURI) 
	
		throws 
		Exception
	{
		// Create references to each of the needed classes
		OWLClass dfe = fffactory.getOWLClass(URI.create(dataFacetOntologyLogicalURI + "#DataFacetEntity"));
		OWLClass tax = fffactory.getOWLClass(URI.create(taxonomyOntologyLogicalURI + "#TaxonomyEntity"));
		OWLClass ffe = fffactory.getOWLClass(URI.create(functionalFacetOntologyLogicalURI + "#FunctionalFacetEntity"));
		OWLClass afp = fffactory.getOWLClass(URI.create(functionalFacetOntologyLogicalURI + "#AdvertisementFunctionalProfile"));
		OWLClass rfp = fffactory.getOWLClass(URI.create(functionalFacetOntologyLogicalURI + "#RequestFunctionalProfile"));
		OWLClass dataSet = fffactory.getOWLClass(URI.create(functionalFacetOntologyLogicalURI + "#DataSet"));
		OWLClass inputDataSet = fffactory.getOWLClass(URI.create(functionalFacetOntologyLogicalURI + "#InputDataSet"));
		OWLClass outputDataSet = fffactory.getOWLClass(URI.create(functionalFacetOntologyLogicalURI + "#OutputDataSet"));
		
		// Create references to each of the needed properties
		OWLObjectProperty hasCategoryProp = fffactory.getOWLObjectProperty(URI.create(functionalFacetOntologyLogicalURI + "#hasCategory"));
		OWLObjectProperty hasInputProp = fffactory.getOWLObjectProperty(URI.create(functionalFacetOntologyLogicalURI + "#hasInput"));
		OWLObjectProperty hasOutputProp = fffactory.getOWLObjectProperty(URI.create(functionalFacetOntologyLogicalURI + "#hasOutput"));
		OWLObjectProperty hasDataParameterProp = fffactory.getOWLObjectProperty(URI.create(functionalFacetOntologyLogicalURI + "#hasDataParameter"));
				
		// Check the classes
		System.out.print("FunctionalFacetEntity class is referenced in the ontology: ");
		if (ffOntology.getReferencedClasses().contains(ffe)) 
			System.out.println("YES");
		else
		{
			System.out.println("NO");
			throw new Exception("FunctionalFacetEntity class is not referenced in the ontology");
		}

		System.out.print("AdvertisementFunctionalProfile class is referenced in the ontology: ");
		if (ffOntology.getReferencedClasses().contains(afp)) 
			System.out.println("YES");
		else
		{
			System.out.println("NO");
			throw new Exception("AdvertisementFunctionalProfile class is not referenced in the ontology");
		}

		System.out.print("AdvertisementFunctionalProfile class is an asserted subclass of FunctionalFacetEntity: ");
		if (ffe.getSubClasses(ffOntology).contains((OWLDescription) afp))
			System.out.println("YES");
		else
		{
			System.out.println("NO");
			throw new Exception("AdvertisementFunctionalProfile class is not an asserted subclass of FunctionalFacetEntity");
		}

		System.out.print("RequestFunctionalProfile class is referenced in the ontology: ");
		if (ffOntology.getReferencedClasses().contains(rfp)) 
			System.out.println("YES");
		else
		{
			System.out.println("NO");
			throw new Exception("RequestFunctionalProfile class is not referenced in the ontology");
		}

		System.out.print("RequestFunctionalProfile class is an asserted subclass of FunctionalFacetEntity: ");
		if (ffe.getSubClasses(ffOntology).contains((OWLDescription) rfp)) 
			System.out.println("YES");
		else
		{
			System.out.println("NO");
			throw new Exception("RequestFunctionalProfile class is not an asserted subclass of FunctionalFacetEntity");
		}

		System.out.print("DataSet class is referenced in the ontology: ");
		if (ffOntology.getReferencedClasses().contains(dataSet)) 
			System.out.println("YES");
		else
		{
			System.out.println("NO");
			throw new Exception("DataSet class is not referenced in the ontology");
		}

		System.out.print("InputDataSet class is referenced in the ontology: ");
		if (ffOntology.getReferencedClasses().contains(inputDataSet)) 
			System.out.println("YES");
		else
		{
			System.out.println("NO");
			throw new Exception("InputDataSet class is not referenced in the ontology");
		}

		System.out.print("InputDataSet class is an asserted subclass of DataSet: ");
		if (dataSet.getSubClasses(ffOntology).contains((OWLDescription) inputDataSet)) 
			System.out.println("YES");
		else
		{
			System.out.println("NO");
			throw new Exception("InputDataSet class is not an asserted subclass of DataSet");
		}

		System.out.print("OutputDataSet class is referenced in the ontology: ");
		if (ffOntology.getReferencedClasses().contains(outputDataSet)) 
			System.out.println("YES");
		else
		{
			System.out.println("NO");
			throw new Exception("OutputDataSet class is not referenced in the ontology");
		}

		System.out.print("OutputDataSet class is an asserted subclass of DataSet: ");
		if (dataSet.getSubClasses(ffOntology).contains((OWLDescription) outputDataSet)) 
			System.out.println("YES");
		else
		{
			System.out.println("NO");
			throw new Exception("OutputDataSet class is not an asserted subclass of DataSet");
		}

		// Check the properties
		System.out.print("hasCategory property is referenced in the ontology: ");
		if (ffOntology.getReferencedObjectProperties().contains(hasCategoryProp)) 
			System.out.println("YES");
		else
		{
			System.out.println("NO");
			throw new Exception("hasCategory property is not referenced in the ontology");
		}

		System.out.print("Domain of hasCategory contains FunctionalFacetEntity: ");
		if (hasCategoryProp.getDomains(ffOntology).contains(ffe)) 
			System.out.println("YES");
		else
		{
			System.out.println("NO");
			throw new Exception("Domain of hasCategory does not contain FunctionalFacetEntity");
		}

		System.out.print("Range of hasCategory contains TaxonomyEntity: ");
		if (hasCategoryProp.getRanges(ffOntology).contains(tax)) 
			System.out.println("YES");
		else
		{
			System.out.println("NO");
			throw new Exception("Range of hasCategory does not contain TaxonomyEntity");
		}

		System.out.print("hasInput property is referenced in the ontology: ");
		if (ffOntology.getReferencedObjectProperties().contains(hasInputProp)) 
			System.out.println("YES");
		else
		{
			System.out.println("NO");
			throw new Exception("hasInput property is referenced in the ontology");
		}

		System.out.print("Domain of hasInput contains FunctionalFacetEntity: ");
		if (hasInputProp.getDomains(ffOntology).contains(ffe)) 
			System.out.println("YES");
		else
		{
			System.out.println("NO");
			throw new Exception("Domain of hasInput does not contain FunctionalFacetEntity");
		}

		System.out.print("Range of hasInput contains InputDataSet: ");
		if (hasInputProp.getRanges(ffOntology).contains(inputDataSet)) 
			System.out.println("YES");
		else
		{
			System.out.println("NO");
			throw new Exception("Range of hasInput does not contain InputDataSet");
		}

		System.out.print("hasOutput property is referenced in the ontology: ");
		if (ffOntology.getReferencedObjectProperties().contains(hasOutputProp)) 
			System.out.println("YES");
		else
		{
			System.out.println("NO");
			throw new Exception("hasOutput property is not referenced in the ontology");
		}

		System.out.print("Domain of hasOutput contains FunctionalFacetEntity: ");
		if (hasOutputProp.getDomains(ffOntology).contains(ffe)) 
			System.out.println("YES");
		else
		{
			System.out.println("NO");
			throw new Exception("Domain of hasOutput does not contain FunctionalFacetEntity");
		}

		System.out.print("Range of hasOutput contains OutputDataSet: ");
		if (hasOutputProp.getRanges(ffOntology).contains(outputDataSet)) 
			System.out.println("YES");
		else
		{
			System.out.println("NO");
			throw new Exception("Range of hasOutput does not contain OutputDataSet");
		}

		System.out.print("hasDataParameter property is referenced in the ontology: ");
		if (ffOntology.getReferencedObjectProperties().contains(hasDataParameterProp)) 
			System.out.println("YES");
		else
		{
			System.out.println("NO");
			throw new Exception("hasDataParameter property is not referenced in the ontology");
		}

		System.out.print("Domain of hasDataParameter contains DataSet: ");
		if (hasDataParameterProp.getDomains(ffOntology).contains(dataSet)) 
			System.out.println("YES");
		else
		{
			System.out.println("NO");
			throw new Exception("Domain of hasDataParameter does not contain DataSet");
		}

		System.out.print("Range of hasDataParameter contains DataFacetEntity: ");
		if (hasDataParameterProp.getRanges(ffOntology).contains(dfe)) 
			System.out.println("YES");
		else
		{
			System.out.println("NO");
			throw new Exception("Range of hasDataParameter does not contain DataFacetEntity");
		}
		
		System.out.println("All checks performed on the Functional Facet ontology were successful");
	}
	
	
	
	
	
	
	
	/**
	 * 
	 * Creates an existential and a universal restriction along the specified
	 * object property for every named OWL class included in the
	 * objectPropertyRangeURIs list, and returns the set of restrictions.
	 * 
	 * @param manager
	 * @param factory
	 * @param classInObjectPropertyDomain
	 * @param objectProperty
	 * @param objectPropertyRangeURIs
	 * 
	 * @return
	 * 
	 * @throws URISyntaxException
	 */
	private Set<OWLDescription> createExistentialAndUniversalRestrictions(
			OWLDataFactory factory,
			OWLObjectProperty objectProperty,
			List<String> objectPropertyRangeURIs) 
	
			throws URISyntaxException
	{
		// Create a set to hold all restrictions (one existential per annotation
		// URI + one universal for all)
		Set<OWLDescription> returnedRestrictionsSet = new HashSet<OWLDescription>();

		// Create a set to hold the existential restrictions (one per annotation
		// URI)
		Set<OWLDescription> existentialRestrictions = new HashSet<OWLDescription>();

		// Create a set to hold the filler classes of all existential
		// restrictions
		Set<OWLDescription> fillers = new HashSet<OWLDescription>();

		// Iterate over the list of annotation URIs
		for (String annotation : objectPropertyRangeURIs)
		{
			//System.out.println("Processing annotation: " + annotation);

			// Construct a URI object from the string carrying the annotation
			// URI
			URI annotationURI = null;
			try
			{
				annotationURI = new URI(annotation);
			}
			catch (URISyntaxException e)
			{
				System.out.println("URISyntaxException: " + e.getCause());
				throw e;
			}

			// Get the name of the class in the annotation URI
			String annotationURIFragment = annotationURI.getFragment();
			//System.out.println("Name of the class in the annotation URI: " + annotationURIFragment);

			// Get the base URI of the annotation URI (i.e. annotation URI
			// without the fragment)
			String annotationURIbase = annotationURI.toString().substring(0,
					annotationURI.toString().indexOf("#"));
			//System.out.println("Base URI in the annotation URI: " + annotationURIbase);

			// Get a reference to an object representing the restriction filler
			// class
			OWLClass fillerClass = factory.getOWLClass(URI.create(annotationURIbase + "#"
					+ annotationURIFragment));

			// Create an existential restriction
			OWLDescription someRestriction = factory.getOWLObjectSomeRestriction(objectProperty,
					fillerClass);

			// Add the restriction to the returned set
			existentialRestrictions.add(someRestriction);

			// Add the filler class to the fillers set
			fillers.add(fillerClass);
		}

		// Add all existential restrictions to the returned set
		returnedRestrictionsSet.addAll(existentialRestrictions);

		// Create a union of the filler classes
		OWLDescription unionOfFillers = factory.getOWLObjectUnionOf(fillers);

		// Create a universal restriction
		OWLDescription allRestriction = factory.getOWLObjectAllRestriction(objectProperty,
				unionOfFillers);

		// Add the restriction to the returned set
		returnedRestrictionsSet.add(allRestriction);

		// Return the set of existential and universal restrictions
		// (one existential restriction per annotation URI + one universal
		// restriction for the set of existential restrictions, i.e. a closure)
		//System.out.println("Number of restrictions on object property " + objectProperty.toString() + ": " + returnedRestrictionsSet.size());
		return returnedRestrictionsSet;
	}// end method
	
	
	
	
	
	
	
	/**
	 * Creates an existential restriction along the specified object property
	 * for every named OWL class included in the objectPropertyRangeURIs list,
	 * and returns the set of restrictions. 
	 * 
	 * @param manager
	 * @param factory
	 * @param classInObjectPropertyDomain
	 * @param objectProperty
	 * @param objectPropertyRangeURIs
	 * 
	 * @return
	 * 
	 * @throws URISyntaxException
	 */
	private Set<OWLDescription> createExistentialRestrictions(
			OWLDataFactory factory,
			OWLObjectProperty objectProperty,
			List<String> objectPropertyRangeURIs) 
	
			throws URISyntaxException
	{
		// For every annotation URI pointing to a class X: create an existential
		// restriction to describe the class of individuals having at least one
		// relationship along the specific property to an individual of class X,
		// and return the resulting set of existential restrictions

		// Create a set to hold all restrictions (one existential per annotation URI)
		Set<OWLDescription> returnedRestrictionsSet = new HashSet<OWLDescription>();

		// Iterate over the list of annotation URIs
		for (String annotation : objectPropertyRangeURIs)
		{
			//System.out.println("Processing annotation: " + annotation);

			// Construct a URI object from the string carrying the annotation URI
			URI annotationURI = null;
			try
			{
				annotationURI = new URI(annotation);
			}
			catch (URISyntaxException e)
			{
				System.out.println("URISyntaxException: " + e.getCause());
				throw e;
			}

			// Get the name of the class in the annotation URI
			String annotationURIFragment = annotationURI.getFragment();
			//System.out.println("Name of the class in the annotation URI: " + annotationURIFragment);

			// Get the base URI of the annotation URI (i.e. annotation URI
			// without the fragment)
			String annotationURIbase = annotationURI.toString().substring(0,
					annotationURI.toString().indexOf("#"));
			//System.out.println("Base URI in the annotation URI: " + annotationURIbase);

			// Get a reference to an object representing the restriction filler class
			OWLClass fillerClass = factory.getOWLClass(URI.create(annotationURIbase + "#"
					+ annotationURIFragment));

			// Create an existential restriction
			OWLDescription someRestriction = factory.getOWLObjectSomeRestriction(objectProperty,
					fillerClass);

			// Add the restriction to the returned set
			returnedRestrictionsSet.add(someRestriction);

		}

		// Return the set of existential restrictions
		//System.out.println("Number of restrictions on object property " + objectProperty.toString() + ": " + returnedRestrictionsSet.size());
		return returnedRestrictionsSet;
	}// end method
	
	
	
	
}// end class
