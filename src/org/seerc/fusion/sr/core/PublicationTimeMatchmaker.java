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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.seerc.fusion.sr.exceptions.SemanticRegistryConfigurationException;
import org.seerc.fusion.sr.exceptions.SemanticRegistryMatchmakingException;
import org.seerc.fusion.sr.utils.Chronometer;
import org.semanticweb.owl.inference.OWLReasoner;
import org.semanticweb.owl.inference.OWLReasonerException;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.AddAxiom;
import org.semanticweb.owl.model.OWLAnonymousDescription;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyChangeException;
import org.semanticweb.owl.model.OWLOntologyCreationException;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.semanticweb.owl.model.OWLOntologyStorageException;
import org.semanticweb.owl.util.OWLEntityCollector;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * @author Dimitrios Kourtesis
 *
 */
public class PublicationTimeMatchmaker
{

	/**
	 * Uses the extracted information to construct a service Advertisement
	 * Functional Profile (AFP), store it in the OWL KB, classify the OWL KB,
	 * and get the URIs of all service Request Functional Profiles (RFPs) that
	 * the AFP matches
	 * 
	 * @param srkbOntologyPhysicalURI
	 * @param annotations
	 * @param advertisementUUIDKey
	 * @return
	 * @throws SemanticRegistryMatchmakingException
	 * @throws SemanticRegistryConfigurationException
	 */
	public SemanticProfileURICollection createAFPAndGetMatchingRFPs(
			URI srkbOntologyPhysicalURI, 
			SemanticProfileAnnotationCollection annotations, 
			String advertisementUUIDKey) 
	
	throws SemanticRegistryMatchmakingException, 
	SemanticRegistryConfigurationException
	
	{
		// Get the parsed modelReference annotations
		List<String> functionalModelReferenceAnnotations = annotations.getFunctionalModelReferenceAnnotationURIs();
		List<String> inputModelReferenceAnnotations = annotations.getInputModelReferenceAnnotationURIs();
		List<String> outputModelReferenceAnnotations = annotations.getOutputModelReferenceAnnotationURIs();

		// Detect the implied message exchange pattern of the AFP
		String afpMEP = null;
		if (inputModelReferenceAnnotations.size() > 0 && outputModelReferenceAnnotations.size() == 0) afpMEP = "in-only";
		if (inputModelReferenceAnnotations.size() == 0 && outputModelReferenceAnnotations.size() > 0) afpMEP = "out-only";
		if (inputModelReferenceAnnotations.size() > 0 && outputModelReferenceAnnotations.size() > 0) afpMEP = "in-out";
		System.out.println("The message exchange pattern of the service is: " + afpMEP);
		
		
		
		
		// //////////////////////
		// LOAD THE SEMANTIC REGISTRY KB ONTOLOGY
		// //////////////////////
		
		// Load the SRKB ontology
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

		// Load the SRKB ontology from its physical URI (OWL file)
		OWLOntology srkbOntology = null;
		try
		{
			srkbOntology = manager.loadOntologyFromPhysicalURI(srkbOntologyPhysicalURI);
			System.out.println("SRKB ontology loaded from " + srkbOntologyPhysicalURI);
		}
		catch (OWLOntologyCreationException e1)
		{
			System.out.println("OWLOntologyCreationException: " + e1.getCause());
			throw new SemanticRegistryMatchmakingException("An error occured while trying to load the SRKB Ontology from its physical URI (" + srkbOntologyPhysicalURI + ")");
		}

		// Get the ontology's data factory to create the various objects
		OWLDataFactory factory = manager.getOWLDataFactory();

		// Get the SemanticRegistryKB ontology's logical URI
		URI srkbLogicalURI = srkbOntology.getURI();
		
		// Obtain and load the imports closure of the semRegKBontology
        Set<OWLOntology> importsClosure = manager.getImportsClosure(srkbOntology);
        
		System.out.println("Ontologies contained in the SRKB ontology's import closure:");
		for (OWLOntology ont : importsClosure)
		{
			System.out.println(ont.getURI());
		}
        
        URI taxonomyOntologyLogicalURI = getDefiningOntology("#TaxonomyEntity", importsClosure, factory).getURI();
		URI dataFacetOntologyLogicalURI = getDefiningOntology("#DataFacetEntity", importsClosure, factory).getURI();
		URI functionalFacetOntologyLogicalURI = getDefiningOntology("#FunctionalFacetEntity", importsClosure, factory).getURI();
		OWLOntology rfpOntology = getDefiningOntology(functionalFacetOntologyLogicalURI+"#RequestFunctionalProfile", importsClosure, factory);
				
		
		
		
		
		// //////////////////////
		// CREATE A CLASS FOR THE AFP
		// //////////////////////
		
		// Create a reference to an object representing the AFP root class
		OWLClass advertisementFunctionalProfileRootClass = factory.getOWLClass(URI.create(functionalFacetOntologyLogicalURI + "#AdvertisementFunctionalProfile"));
		
		// Create a reference to an object representing the class of the
		// Advertisement, using the advertisement's UUID as the class name
		OWLClass afpClass = factory.getOWLClass(URI.create(srkbLogicalURI + "#" + advertisementUUIDKey));
		
		// Create a subclass axiom for stating that the class of the specific
		// Advertisement is a subclass of the AdvertisementFunctionalProfile root class
		OWLAxiom axiom = factory.getOWLSubClassAxiom(afpClass, advertisementFunctionalProfileRootClass);

		// Add the axiom to the ontology by creating an AddAxiom change object
		AddAxiom addAxiomChange = new AddAxiom(srkbOntology, axiom);

		// Apply the axiom addition
		try
		{
			manager.applyChange(addAxiomChange);
		}
		catch (OWLOntologyChangeException e)
		{
			System.out.println("OWLOntologyChangeException: " + e.getCause());
			throw new SemanticRegistryMatchmakingException("An error occured while constructing an AFP in memory");
		}
		
				
		
		
		
		// //////////////////////
		// CREATE A CLASS FOR THE AFP's INCOMING DATACOLLECTION
		// AND RESTICTIONS FOR THE hasInput PROPERTY
		// //////////////////////
		
		// Create a reference to an object representing the InputDataSet root class
		OWLClass inputDataSetRootClass = factory.getOWLClass(URI.create(functionalFacetOntologyLogicalURI + "#InputDataSet"));

		// Create a reference to an object representing the class of the Advertisement's incoming data
		OWLClass afpIncomingDataClass = factory.getOWLClass(URI.create(srkbLogicalURI + "#" + advertisementUUIDKey + "InputDataSet"));
	
		// Create a reference to an object representing the hasInput object property
		OWLObjectProperty hasInput = factory.getOWLObjectProperty(URI.create(functionalFacetOntologyLogicalURI + "#hasInput"));
		
		// Create a set to hold the restrictions to be created for the above object property
		Set<OWLDescription> afpIncomingDataClassRestrictionSet = new HashSet<OWLDescription>();
		
		// No point in continuing if the service doesn't have any inputs 
		if ( afpMEP.equalsIgnoreCase("in-only") || afpMEP.equalsIgnoreCase("in-out") )
		{			
			// Create a subclass axiom for stating that the
			// afpIncomingDataClass is a subclass of the inputDataSetRootClass
			axiom = factory.getOWLSubClassAxiom(afpIncomingDataClass, inputDataSetRootClass);
	
			// Add the axiom to the ontology by creating an AddAxiom change object
			addAxiomChange = new AddAxiom(srkbOntology, axiom);
	
			// Apply the axiom addition
			try
			{
				manager.applyChange(addAxiomChange);
			}
			catch (OWLOntologyChangeException e)
			{
				System.out.println("OWLOntologyChangeException: " + e.getCause());
				throw new SemanticRegistryMatchmakingException("An error occured while constructing an AFP in memory");
			}
			
			// Create a list containing only one element: the URI of the above class
			List<String> afpIncomingDataClassURI = new ArrayList<String>();
			afpIncomingDataClassURI.add(afpIncomingDataClass.getURI().toString());
			
	        // Create a restriction to describe the class of individuals that have
			// at least one hasInput relationship to an individual of class
			// xxxxxInputDataSet, and a closure axiom restriction
			afpIncomingDataClassRestrictionSet.addAll(createExistentialAndUniversalRestrictions (manager, srkbOntology, factory, afpClass, hasInput, afpIncomingDataClassURI));		
		}
		
				
		
		
		
		// //////////////////////
		// CREATE A CLASS FOR THE AFP's OUTGOING DATACOLLECTION
		// AND RESTICTIONS FOR THE hasOutput PROPERTY
		// //////////////////////
		
		// Create a reference to an object representing the OutputDataSet root class
		OWLClass outputDataSetRootClass = factory.getOWLClass(URI.create(functionalFacetOntologyLogicalURI + "#OutputDataSet"));
		
		// Create a reference to an object representing the class of the Advertisement's outgoing data
		OWLClass afpOutgoingDataClass = factory.getOWLClass(URI.create(srkbLogicalURI + "#" + advertisementUUIDKey + "OutputDataSet" ));
		
		// Create a reference to an object representing the hasOutput object property
		OWLObjectProperty hasOutput = factory.getOWLObjectProperty(URI.create(functionalFacetOntologyLogicalURI + "#hasOutput"));
		
		// Create a set to hold the restrictions to be created for the above object property
		Set<OWLDescription> afpOutgoingDataClassRestrictionSet = new HashSet<OWLDescription>();
		
		// No point in continuing if the service doesn't have any outputs
		if ( afpMEP.equalsIgnoreCase("out-only") || afpMEP.equalsIgnoreCase("in-out") )
		{
			// Create a subclass axiom for stating that the
			// outgoingDataClass is a subclass of the outputDataSetRootClass
			axiom = factory.getOWLSubClassAxiom(afpOutgoingDataClass, outputDataSetRootClass);
	
			// Add the axiom to the ontology by creating an AddAxiom change object
			addAxiomChange = new AddAxiom(srkbOntology, axiom);
	
			// Apply the axiom addition
			try
			{
				manager.applyChange(addAxiomChange);
			}
			catch (OWLOntologyChangeException e)
			{
				System.out.println("OWLOntologyChangeException: " + e.getCause());
				throw new SemanticRegistryMatchmakingException("An error occured while constructing an AFP in memory");
			}
			
			// Create a list containing only one element: the URI of the above class
			List<String> afpOutgoingDataClassURI = new ArrayList<String>();
			afpOutgoingDataClassURI.add(afpOutgoingDataClass.getURI().toString());
			
	        // Create a restriction to describe the class of individuals that have
			// at least one hasOutput relationship to an individual of class
			// DataProvidedBy_xxxxx, and a closure axiom restriction
			afpOutgoingDataClassRestrictionSet.addAll(createExistentialAndUniversalRestrictions (manager, srkbOntology, factory, afpClass, hasOutput, afpOutgoingDataClassURI));
		}
		
				
		
		
		
		// //////////////////////
		// CREATE RESTICTIONS FOR THE hasCategory PROPERTY
		// //////////////////////
		
		// Create a reference to an object representing the hasCategory object
		// property
		OWLObjectProperty hasCategory = factory.getOWLObjectProperty(URI.create(functionalFacetOntologyLogicalURI + "#hasCategory"));
		
		// Create a set to hold the restrictions to be created for the above
		// object property
		Set<OWLDescription> afpCategoryClassRestrictionSet = new HashSet<OWLDescription>();
		
        // Create a restriction to describe the class of individuals that have
		// at least one hasCategory relationship to an individual of class
		// DataProvidedBy_xxxxx, and a closure axiom restriction
		afpCategoryClassRestrictionSet.addAll(createExistentialAndUniversalRestrictions (manager, srkbOntology, factory, afpClass, hasCategory, functionalModelReferenceAnnotations));
		
		
		
		
		
		// //////////////////////
		// ADD RESTRICTIONS FOR THE AFP CLASS TO THE ONTOLOGY
		// //////////////////////
		
		// Create a set to hold the restrictions to be created for the AFP class
		Set<OWLDescription> afpRestrictionSet = new HashSet<OWLDescription>();
		
		// Add all restrictions to the afpRestrictionSet
		afpRestrictionSet.addAll(afpIncomingDataClassRestrictionSet);
		afpRestrictionSet.addAll(afpOutgoingDataClassRestrictionSet);
		afpRestrictionSet.addAll(afpCategoryClassRestrictionSet);
		
		// Create an anonymous class description containing the
		// intersection of all existential and universal restrictions
		OWLAnonymousDescription anonymousClassDescription = factory.getOWLObjectIntersectionOf(afpRestrictionSet);
		
		// Create a set containing the afpClass and the anonymousClassDescription
		Set<OWLDescription> afpClassPlusAnonymousClass = new HashSet<OWLDescription>();
		afpClassPlusAnonymousClass.add(afpClass);
		afpClassPlusAnonymousClass.add(anonymousClassDescription);
		
        // Create an axiom stating that the afpClass is equivalent
		// to the anonymous class description that represents the 
		// intersection of existential and universal restrictions
		OWLAxiom equivalentClassesAxiom = factory.getOWLEquivalentClassesAxiom(afpClassPlusAnonymousClass);
        
        // Apply the axiom addition
        addAxiomChange = new AddAxiom(srkbOntology, equivalentClassesAxiom);
        try
		{
			manager.applyChange(addAxiomChange);
		}
		catch (OWLOntologyChangeException e)
		{
			System.out.println("OWLOntologyChangeException: " + e.getCause());
			throw new SemanticRegistryMatchmakingException("An error occured while constructing an AFP in memory");
		}
		
		
		
		
		// //////////////////////
		// ADD RESTRICTIONS TO THE hasDataParameter PROPERTY OF THE AFP's
		// INCOMING DATACOLLECTION
		// //////////////////////
		
		// Create a reference to an object representing the hasDataParameter object property
		OWLObjectProperty hasDataParameter = factory.getOWLObjectProperty(URI.create(functionalFacetOntologyLogicalURI + "#hasDataParameter"));		
		
		// No point in continuing if the service doesn't have any inputs
		if ( afpMEP.equalsIgnoreCase("in-only") || afpMEP.equalsIgnoreCase("in-out") )
		{		
			// Create a set to hold the restrictions to be created
			Set<OWLDescription> afpIncomingDataRestrictionSet = new HashSet<OWLDescription>();
				
			// For every hasInput annotation: create an existential restriction to
			// describe the class of individuals that have at least one hasInput
			// relationship to an individual of class X (universal restriction
			// should not be created, in order to allow for non-exact match)
			afpIncomingDataRestrictionSet.addAll(createExistentialRestrictions (manager, srkbOntology, factory, afpIncomingDataClass, hasDataParameter, inputModelReferenceAnnotations));
			
			// Print the number of restrictions created
			//System.out.println("Number of existential and universal restrictions in incomingDataRestrictionSet: " + afpIncomingDataRestrictionSet.size());
			
			// Create an anonymous class description containing the
			// intersection of all existential and universal restrictions
			OWLAnonymousDescription anonymousAfpIncomingDataClassDescription = factory.getOWLObjectIntersectionOf(afpIncomingDataRestrictionSet);
			
			// Create a set containing the incomingDataClass and the
			// anonymousIncomingDataClassDescription
			Set<OWLDescription> afpIncomingDataClassPlusAnonymousDescription = new HashSet<OWLDescription>();
			afpIncomingDataClassPlusAnonymousDescription.add(afpIncomingDataClass);
			afpIncomingDataClassPlusAnonymousDescription.add(anonymousAfpIncomingDataClassDescription);
			
	        // Create an axiom stating that the advertisedServiceClass is equivalent
			// to the anonymous class description representing the intersection of
			// existential and universal restrictions
			equivalentClassesAxiom = factory.getOWLEquivalentClassesAxiom(afpIncomingDataClassPlusAnonymousDescription);
	        
	        // Apply the axiom addition
	        addAxiomChange = new AddAxiom(srkbOntology, equivalentClassesAxiom);
	        try
			{
				manager.applyChange(addAxiomChange);
			}
			catch (OWLOntologyChangeException e)
			{
				System.out.println("OWLOntologyChangeException: " + e.getCause());
				throw new SemanticRegistryMatchmakingException("An error occured while constructing an AFP in memory");
			}
		}
		
		
		
		
		// //////////////////////
		// ADD RESTRICTIONS TO THE hasDataParameter PROPERTY OF THE AFP's
		// OUTGOING DATACOLLECTION
		// //////////////////////
		
		// No point in continuing if the service doesn't have any outputs
		if ( afpMEP.equalsIgnoreCase("out-only") || afpMEP.equalsIgnoreCase("in-out") )
		{
			// Create a set to hold the restrictions to be created
			Set<OWLDescription> afpOutgoingDataRestrictionSet = new HashSet<OWLDescription>();
			
			// For every hasOutput annotation: create an existential restriction to
			// describe the class of individuals that have at least one hasOutput
			// relationship to an individual of class X, and also create a universal
			// restriction to serve as a closure axiom
			afpOutgoingDataRestrictionSet.addAll(createExistentialAndUniversalRestrictions (manager, srkbOntology, factory, afpOutgoingDataClass, hasDataParameter, outputModelReferenceAnnotations));
			
			// Print the number of restrictions created
			//System.out.println("Number of existential and universal restrictions in outgoingDataRestrictionSet: " + afpOutgoingDataRestrictionSet.size());
			
			// Create an anonymous class description containing the
			// intersection of all existential and universal restrictions
			OWLAnonymousDescription anonymousAfpOutgoingDataClassDescription = factory.getOWLObjectIntersectionOf(afpOutgoingDataRestrictionSet);
			
			// Create a set containing the incomingDataClass and the
			// anonymousIncomingDataClassDescription
			Set<OWLDescription> afpOutgoingDataClassPlusAnonymousDescription = new HashSet<OWLDescription>();
			afpOutgoingDataClassPlusAnonymousDescription.add(afpOutgoingDataClass);
			afpOutgoingDataClassPlusAnonymousDescription.add(anonymousAfpOutgoingDataClassDescription);
			
	        // Create an axiom stating that the advertisedServiceClass is equivalent
			// to the anonymous class description representing the intersection of
			// existential and universal restrictions
			equivalentClassesAxiom = factory.getOWLEquivalentClassesAxiom(afpOutgoingDataClassPlusAnonymousDescription);
	        
	        // Apply the axiom addition
	        addAxiomChange = new AddAxiom(srkbOntology, equivalentClassesAxiom);
	        try
			{
				manager.applyChange(addAxiomChange);
			}
			catch (OWLOntologyChangeException e)
			{
				System.out.println("OWLOntologyChangeException: " + e.getCause());
				throw new SemanticRegistryMatchmakingException("An error occured while constructing an AFP in memory");
			}
		}
		
		
		
	
		// //////////////////////
		// SAVE THE SEMANTIC REGISTRY KB ONTOLOGY
		// //////////////////////
		try
		{
			manager.saveOntology(srkbOntology);
			System.out.println("AFP added to the SRKB");
		}
		catch (OWLOntologyStorageException e)
		{
			System.out.println("OWLOntologyStorageException: " + e.getCause());
			throw new SemanticRegistryMatchmakingException("An error occured while trying to save the AFP to the SRKB ontology (" + srkbOntology + ")");
		}
		
				
		
		
		// //////////////////////
		// CLASSIFY THE SEMANTIC REGISTRY KB ONTOLOGY
		// //////////////////////
		
		// Create a Pellet reasoner instance
		OWLReasoner reasoner = createReasoner(manager);

		// Get the logger instance for pellet.taxonomy.Taxonomy
		Logger logger = Logger.getLogger("log4j.logger.org.mindswap.pellet.taxonomy.Taxonomy");
		logger.setLevel(Level.OFF);
		
		// Get the logger instance for pellet.KnowledgeBase
		logger = Logger.getLogger("log4j.logger.org.mindswap.pellet.KnowledgeBase");
		logger.setLevel(Level.OFF);
		
		// Get the logger instance for pellet.ABox
		logger = Logger.getLogger("log4j.logger.org.mindswap.pellet.ABox");
		logger.setLevel(Level.OFF);
		
		// Get the logger instance for pellet
		logger = Logger.getLogger("log4j.logger.org.mindswap.pellet");
		logger.setLevel(Level.OFF);
		
		// Create a chronometer to measure lapsed time
		Chronometer chron = new Chronometer();
		chron.start();
		
        // Load the ontologies to Pellet
        try
		{
			reasoner.loadOntologies(importsClosure);
		}
		catch (OWLReasonerException e)
		{
			System.out.println("OWLReasonerException: " + e.getCause());
			throw new SemanticRegistryMatchmakingException("An error occured while trying to load the ontology import closure to the DL reasoner");
		}
		
		chron.stop();
		System.out.println("The SRKB ontology and all directly or indirectly imported ontologies where loaded to the DL reasoner in " + chron);
		
		
        // Classify the SRKB ontology and determine if it is inconsistent
		try
		{
			reasoner.classify();
		}
		catch (OWLReasonerException e)
		{
			System.out.println("An error occured while trying to classify the SRKB ontology");
			throw new SemanticRegistryMatchmakingException("An error occured while trying to classify the SRKB ontology");
		}
		catch (Exception e)
		{
			try
			{
				if (reasoner.isConsistent(srkbOntology)) 
				{
					System.out.println("An error occured while trying to determine if the SRKB ontology is consistent. Message: " + e.getMessage());
					throw new SemanticRegistryMatchmakingException("An error occured while trying to determine if the SRKB ontology is consistent");
				}
				else
				{
					System.out.println("The SRKB ontology is inconsistent. Cannot proceed with matchmaking between the newly created AFP and the RFPs in the EAI Ontology.");
					throw new SemanticRegistryMatchmakingException("The SRKB ontology is inconsistent. Cannot proceed with matchmaking between the newly created AFP and the RFPs in the EAI Ontology.");					
				}
			}
			catch (OWLReasonerException e1)
			{
				System.out.println("An error occured while trying to determine if the SRKB ontology is consistent");
				throw new SemanticRegistryMatchmakingException("An error occured while trying to determine if the SRKB ontology is consistent");
			}
		}
		
		
		
		

		
		// //////////////////////
		// PREPROCESSING - CREATE CATEGORY MATCHING CHECKLIST
		// //////////////////////
		
		// Create a reference to the filler class of the AFP's hasCategory property
		OWLClass afpHasCategoryClass = factory.getOWLClass(URI.create(functionalModelReferenceAnnotations.get(0)));
		
		// Get the set of sets of classes that subsume the afpHasCategoryClass
		Set<Set<OWLClass>> subsumersOfAFPHasCategoryClassSetOfSets = new HashSet<Set<OWLClass>>();
		try
		{
			subsumersOfAFPHasCategoryClassSetOfSets = reasoner.getAncestorClasses(afpHasCategoryClass);
		}
		catch (OWLReasonerException e)
		{
			System.out.println("OWLReasonerException: " + e.getCause());
			throw new SemanticRegistryMatchmakingException();
		}
		
		// Flatten the set of sets into one set...
		Set<OWLClass> categoryCheckList = new HashSet<OWLClass>();
		
		// First of all, add the category class itself
		categoryCheckList.add(afpHasCategoryClass);
		
		// ... and from each set...
		for (Set<OWLClass> subsumersOfRFPHasCategoryClassSet : subsumersOfAFPHasCategoryClassSetOfSets)
		{
			// ...add every class apart from owl:Nothing 
			for (OWLClass tmp : subsumersOfRFPHasCategoryClassSet)
			{
				// We could check tmp.isOWLThing() to avoid adding owl:Thing in the 
				// checkList. However we don't, because we would like to keep it. 
				// The reason for keeping it is to allow someone to bypass the 
				// category-based matchmaking for a specific RFP by specifying this 
				// RFP as having a category equal to #Thing. This is essentially read 
				// as "any category will do for this request to be satisfied". On
				// the other hand, the presence of owl:Nothing would signal an 
				// inconsistent ontology and as this information would be irrelevant 
				// to indexing, we would prefer to avoid it.
				if ( !tmp.isOWLNothing() ) 
				{
					categoryCheckList.add(tmp);
				}
			}
		}
		
		// Note that the checkList might contain classes that are not necessarily asserted subclasses of #TaxonomyEntity
		System.out.println("The taxonomy class of the AFP (" + afpHasCategoryClass.getURI() + ") is subsumed by " + categoryCheckList.size() + " classes (including itself)");
		
		
		
		
		
		// //////////////////////
		// PREPROCESSING - CREATE INPUT MATCHING CHECKLIST 
		// //////////////////////
		
		// A list of all matching InputDataSet classes
		Set<OWLClass> inputCheckList = new HashSet<OWLClass>();
		
		// No point in preparing an input checklist if the service doesn't have any inputs 
		if ( afpMEP.equalsIgnoreCase("in-only") || afpMEP.equalsIgnoreCase("in-out") )
		{			
			// Get the set of sets of classes subsumed by afpIncomingDataClass
			// (this includes all classes that represent collections of data, 
			// regardless of whether they represent input or output data, and 
			// whether they "belong" to RFPs or AFPs 
			Set<Set<OWLClass>> subsumedRFPInputDataSetClassSetOfSets = new HashSet<Set<OWLClass>>();
			try
			{
				subsumedRFPInputDataSetClassSetOfSets = reasoner.getDescendantClasses(afpIncomingDataClass);
			}
			catch (OWLReasonerException e)
			{
				System.out.println("OWLReasonerException: " + e.getCause());
				throw new SemanticRegistryMatchmakingException();
			}
			
			// First of all, add the InputDataSet class itself
			inputCheckList.add(afpIncomingDataClass);
			
			// ... and from each set...
			for (Set<OWLClass> subsumedRFPInputDataSetClassSet : subsumedRFPInputDataSetClassSetOfSets)
			{
				// ...pick only those classes that represent inputs (of AFPs or RFPs)
				for (OWLClass subsumedRFPInputDataSetClass : subsumedRFPInputDataSetClassSet)
				{
					// if the descendantClass is not the owl:Nothing class (the presence of owl:Nothing would signal an inconsistent ontology), AND
					// if the descendantClass is an asserted subclass of the #InputDataSet root class (i.e. it represents input, not output)
					if ( !subsumedRFPInputDataSetClass.isOWLNothing() 
							&& (isAssertedSubClassOf(subsumedRFPInputDataSetClass, inputDataSetRootClass, rfpOntology) ) ) 
					{
						// ...add the class to the subsumedInputClasses set, 
						inputCheckList.add(subsumedRFPInputDataSetClass);
					}
				}
			}
			
			// At this point the subsumedInputClasses set contains classes 
			// representing not only input data of RFPs, but also of AFPs
			System.out.println("The input data set class of the AFP " + afpIncomingDataClass.getURI() + " subsumes " + inputCheckList.size() + " classes (including itself)");
			
		}
		
		
		
		
		
		
		// //////////////////////
		// PREPROCESSING - CREATE OUPUT MATCHING CHECKLIST
		// //////////////////////
		
		// A list of all matching OutputDataSet classes
		Set<OWLClass> outputCheckList = new HashSet<OWLClass>();
		
		// No point in preparing an output checklist if the service doesn't have any outputs 
		if ( afpMEP.equalsIgnoreCase("out-only") || afpMEP.equalsIgnoreCase("in-out") )
		{	
			// Get the set of sets of classes that subsume the afpOutgoingDataClass
			// (this includes all classes that represent collections of data, 
			// regardless of whether they represent input or output data, and 
			// whether they "belong" to RFPs or AFPs 
			Set<Set<OWLClass>> subsumerClassSetOfSets = new HashSet<Set<OWLClass>>();
			try
			{
				subsumerClassSetOfSets = reasoner.getAncestorClasses(afpOutgoingDataClass);
			}
			catch (OWLReasonerException e)
			{
				System.out.println("OWLReasonerException: " + e.getCause());
				throw new SemanticRegistryMatchmakingException();
			}
			
			// First of all, add the OutputDataSet class itself
			outputCheckList.add(afpOutgoingDataClass);
			
			// ... and from each set...
			for (Set<OWLClass> tmpClassSet : subsumerClassSetOfSets)
			{
				// ...pick only those classes that represent outputs (of AFPs or RFPs)
				for (OWLClass tmpClass : tmpClassSet)
				{
					// if the class is not the owl:Thing class, AND
					// if the class is an asserted subclass of the #OutputDataSet root class (i.e. it represents output, not input)
					if ( !tmpClass.isOWLThing() 
							&& (isAssertedSubClassOf(tmpClass, outputDataSetRootClass, rfpOntology) ) ) 
					{
						// ...add the class to the set of subsumer classes 
						outputCheckList.add(tmpClass);
					}
				}
			}
			
			// At this point the set of subsumer classes contains classes 
			// representing not only output data of RFPs, but also of AFPs
			System.out.println("The output data set class of the AFP " + afpOutgoingDataClass.getURI() + " is subsumed by " + outputCheckList.size() + " classes (including itself)");
		}
			
			
		
		
		
		// //////////////////////
		// PREPROCESSING 
		// //////////////////////
		
		// Create a list to store the URIs of all RFPs that match the AFP 
		List<String> returnedMatchingRFPs = new ArrayList<String>();
		
		// Create a set to hold all RFPs that pass the category-based matching 
		// check (#TaxonomyEntity subclasses that subsume the taxonomy class
		// that the AFP is associated with via the hasCategory property). The
		// RFPs in the set should be also checked for one of the three cases:
		// 1) input match, if the MEP is in-only
		// 2) output match, if the MEP is out-only
		// 3) input and output match, if the MEP is in-out
		//Set<OWLDescription> candidateRFPs = new HashSet<OWLDescription>();
		
		// Create a reference to an object representing the TaxonomyEntity root class
		OWLClass taxonomyRootClass = factory.getOWLClass(URI.create(taxonomyOntologyLogicalURI + "#TaxonomyEntity"));
		
		// Create a reference to the Requested Functional Profile root class
		OWLClass requestFunctionalProfileRootClass = factory.getOWLClass(URI.create(functionalFacetOntologyLogicalURI + "#RequestFunctionalProfile"));
		
		// Get a set of all AFPs in the ontology
		Set<OWLDescription> allAFPs = advertisementFunctionalProfileRootClass.getSubClasses(srkbOntology);
		
		// Get a set of all the RFPs asserted in the ontology 
		Set<OWLDescription> allRFPs = requestFunctionalProfileRootClass.getSubClasses(rfpOntology);
		
		System.out.println(allRFPs.size() + " RFPs found in the ontology");
		
		// Create a collector to pick up all named classes
		// referenced within the equivalent class axiom of the RFP
		OWLEntityCollector collector = new OWLEntityCollector();

		// Speed things up by asking the collector to collect only named classes
		collector.setCollectClasses(true);
		collector.setCollectDataProperties(false);
		collector.setCollectObjectProperties(false);
		collector.setCollectIndividuals(false);
		collector.setCollectDataTypes(false);
		

		
		
		
		// //////////////////////
		// ITERATING OVER ALL RFPs 
		// //////////////////////
		
   	    // Iterate over all references to RFP classes
		for (OWLDescription rfp : allRFPs)
		{
			System.out.println("\nProcessing RFP: " + rfp.asOWLClass().getURI().normalize().toString());

			// Get the equivalent class axiom of the RFP class. By convention
			// in the FUSION project, there should be only one equivalent
			// class axiom, so this loop should be executed only once
			for (OWLEquivalentClassesAxiom ax : rfpOntology.getEquivalentClassesAxioms(rfp.asOWLClass()))
			{
				//System.out.println("Equivalent class axiom: " + ax.toString());

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
						//System.out.println("Collecting filler classes from equivalent class axiom...");
						desc.accept(collector);
					}
				}
			}

			// Get the filler classes from the collector
			Set<OWLEntity> collectedFillerClasses = collector.getObjects();
			//System.out.println(collectedFillerClasses.size() + " filler classes collected from " + rfp);
					
			// By convention in the FUSION project, there should be a maximum 
			// of three classes inside the collectedFillerClasses set:
			// 1) the filler class for the hasCategory property restriction (mandatory)
			// 2) the filler class for the hasInput property restriction (for in-only and in-out services)
			// 3) the filler class for the hasOutput property restriction (for out-only and in-out services)
			
			// Keep a flag to speed things up a bit more
			boolean categoryMatchFound = false;
			boolean mepIsCompatible = false;
			
			// //////////////////////
			// 
			// //////////////////////
			
			// In-only AFPs should be checked for matchmaking only against in-only RFPs, 
			// out-only AFPs checked for matchmaking only against out-only RFPs, and in-out
			// AFPs checked for matchmaking only against in-out RFPs. 
			// If collectedFillerClasses.size() == 2 then the RFP represents an in-only 
			// or an out-only service (we can't yet know which of the two). 
			// If collectedFillerClasses.size() == 3 then the RFP represents an in-out service.
			if ( 
					( (afpMEP.equalsIgnoreCase("in-out") && collectedFillerClasses.size() == 3) ) 
					|| 
					( ( (afpMEP.equalsIgnoreCase("in-only") || afpMEP.equalsIgnoreCase("out-only") ) && collectedFillerClasses.size() == 2) )
				)
			{
				// Keep a flag for each type of filler class expected
				boolean hasCategoryFillerFound = false;
				boolean hasInputFillerFound = false;
				boolean hasOutputFillerFound = false;
				
				// Keep the filler classes
				OWLClass hasCategoryFillerClass = null;
				OWLClass hasInputFillerClass = null;
				OWLClass hasOutputFillerClass = null;
				
				// We will need to convert each retrieved OWLEntity to an OWLClass
				OWLClass fillerCls = null;
				
				// Determine the property corresponding to each filler, 
				// by checking if the filler is an asserted subclass of 
				// 1) inputDataSetRootClass, 2) outputDataSetRootClass, 
				// or 3) none of the two (i.e. a taxonomy annotation)
				for (OWLEntity filler : collectedFillerClasses)
				{
					fillerCls = factory.getOWLClass(filler.getURI().normalize());
					if (!hasInputFillerFound && 
							isAssertedSubClassOf(fillerCls, inputDataSetRootClass, rfpOntology))
					{
						//System.out.println(rfp + " hasInput " + filler.getURI().normalize());
						hasInputFillerFound = true;
						hasInputFillerClass = fillerCls;
					}
					else if (!hasOutputFillerFound && 
							isAssertedSubClassOf(fillerCls, outputDataSetRootClass, rfpOntology))
					{
						//System.out.println(rfp + " hasOutput " + filler.getURI().normalize());
						hasOutputFillerFound = true;
						hasOutputFillerClass = fillerCls;
					}
					else // i.e. if it's the filler class for hasCategory 
					{
						//System.out.println(rfp + " hasCategory " + filler.getURI().normalize());
						hasCategoryFillerFound = true;
						hasCategoryFillerClass = fillerCls;
					}
				}
				
				// Detect the implied message exchange pattern of the RFP
				String rfpMEP = null;
				if (hasCategoryFillerFound && hasInputFillerFound && !hasOutputFillerFound) rfpMEP = "in-only";
				if (hasCategoryFillerFound && !hasInputFillerFound && hasOutputFillerFound) rfpMEP = "out-only";
				if (hasCategoryFillerFound && hasInputFillerFound && hasOutputFillerFound) rfpMEP = "in-out";				
				
				
				// //////////////////////
				// CATEGORY MATCHMAKING 
				// //////////////////////
				
				// If the MEPs of the AFP and the RFP match, check if 
				// the RFP's hasCategory filler class is in the checklist
				
				if ( afpMEP.equalsIgnoreCase(rfpMEP) )
				{
					mepIsCompatible = true;
					System.out.println("The message exchange pattern of the RFP (" + rfpMEP + ") matches the MEP of the AFP");
					
					for (OWLClass tmpClass : categoryCheckList)
					{
						if (!categoryMatchFound && 
								hasCategoryFillerClass.getURI().normalize().toString().equalsIgnoreCase(
										tmpClass.getURI().normalize().toString()))
						{
							//System.out.println("Category Match found!");
							categoryMatchFound = true;
						}
					}
				}
				else
				{
					mepIsCompatible = false;
					System.out.println("The message exchange pattern of the RFP (" + rfpMEP + ") does not match the MEP of the AFP (" + afpMEP + ")");
				}
				
				// If this RFP matches with the AFP at the category level, 
				// proceed to matchmaking at the input level (if MEP is in-only)
				// or output-level (if MEP is out-only)
				if (categoryMatchFound)
				{
					
					// //////////////////////
					// IN-ONLY MATCHMAKING 
					// //////////////////////
					
					// if both AFP and RFP represent services with an in-only MEP
					if (afpMEP.equalsIgnoreCase("in-only") && rfpMEP.equalsIgnoreCase("in-only"))
					{
						System.out.println("Category match detected for RFP " +  rfp + ", proceeding to input matching for this RFP");
						if (inputCheckList.contains(hasInputFillerClass))
						{
							System.out.println("Input match detected for RFP " +  rfp + ", adding it to the list of matching RFPs");
							returnedMatchingRFPs.add(rfp.asOWLClass().getURI().normalize().toString());
						}
						else
							System.out.println("Input match was not detected for RFP " +  rfp);
					}
					
					
					// //////////////////////
					// OUT-ONLY MATCHMAKING 
					// //////////////////////
					
					// if both AFP and RFP represent services with an out-only MEP
					if (afpMEP.equalsIgnoreCase("out-only") && rfpMEP.equalsIgnoreCase("out-only"))
					{
						System.out.println("Category match detected for RFP " +  rfp + ", proceeding to output matching for this RFP");
						if (outputCheckList.contains(hasOutputFillerClass))
						{
							System.out.println("Output match detected for RFP " +  rfp + ", adding it to the list of matching RFPs");
							returnedMatchingRFPs.add(rfp.asOWLClass().getURI().normalize().toString());
						}
						else
							System.out.println("Output match was not detected for RFP " +  rfp);
					}
					
					
					// //////////////////////
					// IN-OUT MATCHMAKING 
					// //////////////////////
					
					// if both AFP and RFP represent services with an in-out MEP
					if (afpMEP.equalsIgnoreCase("in-out") && rfpMEP.equalsIgnoreCase("in-out"))
					{
						System.out.println("Category match detected for RFP " +  rfp + ", proceeding to input matching for this RFP");
						if (inputCheckList.contains(hasInputFillerClass))
						{
							System.out.println("Input match detected for RFP " +  rfp + ", proceeding to output matching");
							if (outputCheckList.contains(hasOutputFillerClass))
							{
								System.out.println("Output match detected for RFP " +  rfp + ", adding it to the list of matching RFPs");
								returnedMatchingRFPs.add(rfp.asOWLClass().getURI().normalize().toString());
							}
							else
								System.out.println("Output match was not detected for RFP " +  rfp);
						}
						else
							System.out.println("Input match was not detected for RFP " +  rfp);
					}
					
					
				} // end-if this RFP matches with the AFP at the category level
				else
				{	
					if (mepIsCompatible && !categoryMatchFound)
					{
						System.out.println("Category match was not detected for RFP " +  rfp);
					}
				}
				
				
			}// end if both AFP and RFP represent in-only, out-only, or in-out services
			else
			{
				System.out.println("The message exchange pattern of the RFP does not match the MEP of the AFP (" + afpMEP + ")");		
			}
			
			// Reset the collector to reuse it in the next iteration
			collector.reset();
			
			
		}// end for every RFP
		
		// 
		if (returnedMatchingRFPs.size() == 0)
			System.out.println("No RFP was detected to match the AFP. ");
		else
			System.out.println(returnedMatchingRFPs.size() + " RFPs were detected as matching the AFP. ");
		
		// Return the (potentially empty) list of matching RFPs
		return new SemanticProfileURICollection(returnedMatchingRFPs);
				
	}



	
	
	
	
	/**
	 * @param subsumedClass
	 * @param rootClass
	 * @param reasoner
	 * @return
	 * @throws SemanticRegistryMatchmakingException
	 */
	private boolean is…nferredSubClassOf(OWLClass subsumedClass,
			OWLClass rootClass, OWLReasoner reasoner) throws SemanticRegistryMatchmakingException
	{

		Set<Set<OWLClass>> inferredSubClassesSetOfSets = new HashSet<Set<OWLClass>>();
		try
		{
			inferredSubClassesSetOfSets = reasoner.getDescendantClasses(rootClass);
		}
		catch (OWLReasonerException e)
		{
			System.out.println("OWLReasonerException: " + e.getCause());
			throw new SemanticRegistryMatchmakingException("????????????");
		}
		
		for (Set<OWLClass> inferredSubClassesSet : inferredSubClassesSetOfSets)
		{
			// iterate over all classes...
			for (OWLClass subClass : inferredSubClassesSet)
			{
				// ... and return true as soon as the URIs of the two class match 
				if (subClass.getURI().toString().equalsIgnoreCase(subsumedClass.getURI().toString())) 
				{
					return true;
				}
			}
		}
		return false;
	}







	/**
	 * @param classToCheck
	 * @param rootClass
	 * @param ontology
	 * @return
	 */
	private boolean isAssertedSubClassOf(OWLClass classToCheck, OWLClass rootClass, OWLOntology ontology)
	{
		// Get a set of all classes in the ontology
		Set<OWLDescription> allAssertedSubclassesOfRootClass = rootClass.getSubClasses(ontology);
		
		//System.out.println("allAssertedSubclassesOfRootClass.size(): " + allAssertedSubclassesOfRootClass.size());
		
		//System.out.print("Checking (IN MODE A) if " + classToCheck + " is subclass of " + rootClass + ": ");
		if (allAssertedSubclassesOfRootClass.contains((OWLDescription)classToCheck)) 
		{
			//System.out.println("YES");
			return true;
		}

		return false;
	}
	
	
	
	
	
	
	
	/**
	 * @param manager
	 * @param semRegKBontology
	 * @param factory
	 * @param classInObjectPropertyDomain
	 * @param objectProperty
	 * @param objectPropertyRangeURIs
	 * @return
	 * @throws SemanticRegistryMatchmakingException
	 */
	Set<OWLDescription> createExistentialAndUniversalRestrictions(OWLOntologyManager manager,
			OWLOntology semRegKBontology, OWLDataFactory factory,
			OWLClass classInObjectPropertyDomain, OWLObjectProperty objectProperty,
			List<String> objectPropertyRangeURIs) throws SemanticRegistryMatchmakingException
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
				throw new SemanticRegistryMatchmakingException("???????????????");
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
	}
	
	
	
	
	
	
	
	/**
	 * @param manager
	 * @param semRegKBontology
	 * @param factory
	 * @param classInObjectPropertyDomain
	 * @param objectProperty
	 * @param objectPropertyRangeURIs
	 * @return
	 * @throws SemanticRegistryMatchmakingException
	 */
	Set<OWLDescription> createExistentialRestrictions(OWLOntologyManager manager,
			OWLOntology semRegKBontology, OWLDataFactory factory,
			OWLClass classInObjectPropertyDomain, OWLObjectProperty objectProperty,
			List<String> objectPropertyRangeURIs) throws SemanticRegistryMatchmakingException
	{
		// For every annotation URI pointing to a class X: create an existential
		// restriction to describe the class of individuals having at least one
		// relationship along the specific property to an individual of class X,
		// and return the resulting set of existential restrictions

		// Create a set to hold all restrictions (one existential per annotation
		// URI)
		Set<OWLDescription> returnedRestrictionsSet = new HashSet<OWLDescription>();

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
				throw new SemanticRegistryMatchmakingException("?????????????");
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
			returnedRestrictionsSet.add(someRestriction);

		}

		// Return the set of existential restrictions
		//System.out.println("Number of restrictions on object property " + objectProperty.toString() + ": " + returnedRestrictionsSet.size());
		return returnedRestrictionsSet;
	}
	
	
	
	
	
	
	
	/**
	 * @param targetClassName
	 * @param importsClosure
	 * @param factory
	 * @return
	 * @throws SemanticRegistryConfigurationException
	 */
	private static OWLOntology getDefiningOntology(
			String targetClassName, 
			Set<OWLOntology> importsClosure, 
			OWLDataFactory factory) 
	
	throws SemanticRegistryConfigurationException
	{
		// Determine which of the ontologies imported by the SRKB 
		// ontology (directly or indirectly) is the ontology in which 
		// the targetClassName is defined, and retrieve its logical URI
		System.out.println("Scanning for " + targetClassName + " in the import closure of the SRKB ontology:");
		
		OWLClass tmpClass = null; 
		OWLOntology returnedOntology = null;
		
		// Create a map to store search result tuples (ontology logical URI, number of subclasses detected)
		Map<OWLOntology, Integer> ontologySearchResults = new HashMap<OWLOntology, Integer>();
		
		URI targetClassURI = URI.create(targetClassName);
		
		// Iterate over every ontology in the imports closure and check  
		// if it contains any asserted subclasses of targetClassName
		for (OWLOntology ont : importsClosure)
		{
			if (targetClassURI.isAbsolute())
			{
				tmpClass = factory.getOWLClass(targetClassURI);
			}
			else
			{
				tmpClass = factory.getOWLClass(URI.create(ont.getURI() + targetClassName));
			}
			
			//System.out.println("Looking for subclasses of " + tmpClass.getURI() + " in " + ont.getURI());
			Set<OWLDescription> foundSubClasses = tmpClass.getSubClasses(ont);
			
			// The containsEntityDeclaration method offers the best technique for 
			// the specific purpose but cannot be used as it is still not implemented 
			// in the latest binary release of the OWL-API (v2.1.1 - 16th August 2007)
			//if (ont.containsEntityDeclaration(target)) 
			//System.out.println("A declaration for concept " + tmpClass.getURI() + " was found in " + ont.getURI());
			
			// Since a concept with the specified name (targetClassName) may be defined in more 
			// than one ontologies in the closure, judging which is the correct ontology that we 
			// are after can only be based on a heuristic (the number of concepts in the ontology 
			// that are defined as asserted subclasses of the target class - the more the better)
			
			// If at least one asserted subclass of the target class has been found
			if (foundSubClasses.size() > 0)
			{				
				// Add it to the search results map
				ontologySearchResults.put(ont, foundSubClasses.size());
			}
		}
		
		// If none of the ontologies in the import closure of the SRKB contains a 
		// subclass of the target class, there is a configuration problem
		if (ontologySearchResults.size() == 0)
		{
			throw new SemanticRegistryConfigurationException(
					"Configuration problem detected: None of the ontologies in the import closure " +
					"of the SRKB ontology contains subclasses of the " + targetClassName + " concept. " +
					"Make sure that the SRKB ontology imports (directly or indirectly) some ontology " +
					"in which a " + targetClassName + " concept as well as several subclass concepts are defined.");
		}
		
		// Standard case: only one ontology from the SRKB's imports closure 
		// contains asserted subclasses of the target concept 
		if (ontologySearchResults.size() == 1)
		{
			returnedOntology = ontologySearchResults.keySet().iterator().next();
			//int subclasses = ontologySearchResults.get(targetOntologyLogicalURI);
			//System.out.println("FOUND " + subclasses + " subclasses of #TaxonomyEntity in " + taxonomyOntologyLogicalURI);
		}
		
		// Exceptional case: more than one ontology from the SRKB's imports closure 
		// contain asserted subclasses of the target class. Therefore Judging which 
		// is the correct ontology can only be based on a heuristic (we choose the number of 
		// is the correct asserted subclasses that were detected - the more being the better) 
		if (ontologySearchResults.size() > 1)
		{
			int maxSubClassesFound = 0 ;
			OWLOntology ontContainingMaxSubClasses = null;
		    
			// Get the keys (URIs) and values (number of subclasses) for every entry in the map
		    for (Iterator it = ontologySearchResults.entrySet().iterator(); it.hasNext(); ) 
		    {
		        Map.Entry entry = (Map.Entry)it.next();
		        OWLOntology onto = (OWLOntology) entry.getKey();
		        Integer numOfSubclasses = (Integer) entry.getValue();
		        
		        if (numOfSubclasses.intValue() > maxSubClassesFound)
		        {
		        	maxSubClassesFound = numOfSubclasses.intValue();
		        	ontContainingMaxSubClasses = onto;
		        }
		    }
		    returnedOntology = ontContainingMaxSubClasses;
		}
		
		System.out.println("Subclasses of the " + tmpClass + " concept are defined in " + returnedOntology.getURI());
		return returnedOntology;
	}
	
	
	
	
	
	
	
	/**
	 * @param man
	 * @return
	 */
	private static OWLReasoner createReasoner(OWLOntologyManager man)
	{
		try
		{
			// The following code is a little overly complicated. The reason for
			// using reflection to create an instance of pellet is so that there 
			// is no compile time dependency (since the pellet libraries aren't 
			// contained in the OWL API repository). Normally, one would simply 
			// create an instance using the following incantation:
			//
			// OWLReasoner reasoner = new Reasoner()
			//
			// Where the full class name for Reasoner is
			// org.mindswap.pellet.owlapi.Reasoner
			//
			// Pellet requires the Pellet libraries (pellet.jar,
			// aterm-java-x.x.jar) and the
			// XSD libraries that are bundled with pellet: xsdlib.jar and
			// relaxngDatatype.jar

			String reasonerClassName = "org.mindswap.pellet.owlapi.Reasoner";
			Class reasonerClass = Class.forName(reasonerClassName);
			Constructor<OWLReasoner> con = reasonerClass.getConstructor(OWLOntologyManager.class);

			return con.newInstance(man);
		}
		catch (ClassNotFoundException e)
		{
			throw new RuntimeException(e);
		}
		catch (IllegalAccessException e)
		{
			throw new RuntimeException(e);
		}
		catch (NoSuchMethodException e)
		{
			throw new RuntimeException(e);
		}
		catch (InvocationTargetException e)
		{
			throw new RuntimeException(e);
		}
		catch (InstantiationException e)
		{
			throw new RuntimeException(e);
		}
	}

}
