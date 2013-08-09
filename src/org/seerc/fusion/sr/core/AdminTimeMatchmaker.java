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
import org.seerc.fusion.sr.utils.FileUtils;
import org.semanticweb.owl.inference.OWLReasoner;
import org.semanticweb.owl.inference.OWLReasonerException;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyCreationException;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.semanticweb.owl.util.OWLEntityCollector;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * @author Dimitrios Kourtesis
 *
 */
public class AdminTimeMatchmaker
{

	
	/**
	 * Retrieves the URIs of all Advertisement Functional 
	 * Profiles (RFPs) that the RFP matches with
	 * 
	 * @param srkbOntologyPhysicalURI
	 * @param eaiOntologyPhysicalURI
	 * @param requestFunctionalProfileURI
	 * @return
	 * @throws SemanticRegistryMatchmakingException
	 * @throws SemanticRegistryConfigurationException
	 */
	public SemanticProfileURICollection getMatchingAFPsForRFP(
			URI srkbOntologyPhysicalURI,
			URI eaiOntologyPhysicalURI,
			URI requestFunctionalProfileURI)

	throws SemanticRegistryMatchmakingException,
		SemanticRegistryConfigurationException
	{

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
		URI functionalFacetOntologyLogicalURI = getDefiningOntology("#FunctionalFacetEntity", importsClosure, factory).getURI();

		
		
		// Get the base URI of the RFP URI (remove the fragment)
		String rfpBaseURI = requestFunctionalProfileURI.toString().substring(0,requestFunctionalProfileURI.toString().indexOf("#"));
//		System.out.println("Physical URI of ontology where RFP is defined: " + requestFunctionalProfileURI.toString().substring(0,requestFunctionalProfileURI.toString().indexOf("#")));
//		System.out.println("Base/Logical URI of ontology where RFP is defined: " + rfpBaseURI);
		
		// Get the base URI of the SRKB ontology
		String srkbOntologyBaseURI = FileUtils.getBaseURIFromOntologyDocument(srkbOntologyPhysicalURI.normalize().toString());
//		System.out.println("Physical URI of srkbOntology: " + srkbOntologyPhysicalURI);
//		System.out.println("Base/Logical URI of srkbOntology: " + srkbOntologyBaseURI);
		
		// Get the base URI of the EAI ontology
		String eaiOntologyBaseURI = FileUtils.getBaseURIFromOntologyDocument(eaiOntologyPhysicalURI.normalize().toString());
//		System.out.println("Physical URI of eaiOntology: " + eaiOntologyPhysicalURI);
//		System.out.println("Base/Logical URI of eaiOntology: " + eaiOntologyBaseURI);
		
		
		// Determine which is the ontology in which the RFP is stored
		OWLOntology rfpOntology = null;
		
		// Check if the baseURI of the URI of the requested Functional Profile
		// coincides with the base/logical URI of the SRKB 
		if (!rfpBaseURI.equalsIgnoreCase(srkbOntologyBaseURI))
		{
			// //////////////////////
			// LOAD THE EAI ONTOLOGY
			// //////////////////////

			// Load the EAI ontology
			OWLOntologyManager eaiOntologyManager = OWLManager.createOWLOntologyManager();

			// Load the EAI ontology from its physical URI (OWL file)
			OWLOntology eaiOntology = null;
			
			
			
			// If the RFP is defined in the EAI ontology (domain ontology),
			// load the EAI ontology from the predefined eaiOntologyPhysicalURI
			if (rfpBaseURI.equalsIgnoreCase(eaiOntologyBaseURI))
			{	
				try
				{
					// Assign the role of the EAI ontology to the predefined EAI ontology
					System.out.println("Trying to load EAI ontology from : " + eaiOntologyPhysicalURI);
					eaiOntology = eaiOntologyManager.loadOntologyFromPhysicalURI(eaiOntologyPhysicalURI);
				}
				catch (OWLOntologyCreationException e1)
				{
					System.out.println("OWLOntologyCreationException: " + e1.getCause());
					throw new SemanticRegistryMatchmakingException("OWLOntologyCreationException");
				}
			}
			// If the RFP is defined in a foreign ontology (that extends the EAI/domain ontology)
			// load the EAI ontology from the base URI of the RFP (which is also a physical URI) 
			else 
			{
				try
				{
					// Assign the role of the EAI ontology to the home ontology in the given RFP URI
					System.out.println("Trying to load EAI ontology from : " + rfpBaseURI);
					eaiOntology = eaiOntologyManager.loadOntologyFromPhysicalURI(URI.create(rfpBaseURI));
				}
				catch (OWLOntologyCreationException e)
				{
					System.out.println("OWLOntologyCreationException: " + e.getCause());
					throw new SemanticRegistryMatchmakingException("OWLOntologyCreationException");
				}
			}

			System.out.println("EAI ontology loaded from " + eaiOntologyPhysicalURI);
			
			// Assign the role of the EAI ontology to the SRKB ontology
			rfpOntology = eaiOntology;
		}
		// If the RFP is defined in the SRKB ontology
		else
		{
			rfpOntology = srkbOntology; 
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
		
		
		
		
		
        // Check if the RFP Ontology is included in the imports closure
        boolean included = false;

        for (OWLOntology ont : importsClosure)
        {
        	if (ont.getURI().normalize().toString().equalsIgnoreCase(rfpOntology.getURI().normalize().toString()))
        		included = true;
        }

		// If it was not included, load it explicitly
    	if (included)
    		System.out.println("RFP Ontology included in the SRKB imports closure");
    	else
    	{
    		System.out.println("RFP Ontology not included in the SRKB imports closure -will be loaded to the reasoner separately-");

    		// Load the RFP "home" ontology to Pellet
    		try
			{
    			Set<OWLOntology> tmp = new HashSet<OWLOntology>();
    			tmp.add(rfpOntology);
				reasoner.loadOntologies(tmp);
			}
			catch (OWLReasonerException e)
			{
				System.out.println("OWLReasonerException: " + e.getCause());
				throw new SemanticRegistryMatchmakingException("Problem occured while loading ontologies to the reasoner");
			}

    	}


        // Load the rest of the ontologies from the imports closure to Pellet
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
		// PREPROCESSING - PREPARE RFP
		// //////////////////////

		// Create a reference to the Request Functional Profile (RFP) class we need to classify against
		OWLClass rfpClassToClassifyAgainst = factory.getOWLClass(requestFunctionalProfileURI);

		// Create a reference to the Request Functional Profile (RFP) root class
		OWLClass requestFunctionalProfileRootClass = factory.getOWLClass(URI.create(functionalFacetOntologyLogicalURI + "#RequestFunctionalProfile"));

		// Create a reference to the Advertisement Functional Profile (AFP) root class
		OWLClass advertisementFunctionalProfileRootClass = factory.getOWLClass(URI.create(functionalFacetOntologyLogicalURI + "#AdvertisementFunctionalProfile"));

		// Create a reference to an object representing the InputDataSet root class
		OWLClass inputDataSetRootClass = factory.getOWLClass(URI.create(functionalFacetOntologyLogicalURI + "#InputDataSet"));

		// Create a reference to an object representing the OutputDataSet root class
		OWLClass outputDataSetRootClass = factory.getOWLClass(URI.create(functionalFacetOntologyLogicalURI + "#OutputDataSet"));

		// Get a set of all the RFPs asserted in the ontology
		Set<OWLDescription> allRFPs = requestFunctionalProfileRootClass.getSubClasses(rfpOntology);

		// Get a set of all AFPs in the ontology
		Set<OWLDescription> allAFPs = advertisementFunctionalProfileRootClass.getSubClasses(srkbOntology);

		// Filler classes corresponding to the hasInput, hasOutput, and hasCategory classes associated with the RFP
		// They are subclasses of #InputDataSet, #OutputDataSet, and #TaxonomyEntity, respectively
   	    OWLClass rfpHasCategoryPropertyFillerClass = null;
   	    OWLClass rfpHasInputPropertyFillerClass = null;
   	    OWLClass rfpHasOutputPropertyFillerClass = null;

		// Create a collector to pick up all named classes
		// referenced within the equivalent class axiom of the RFP
		OWLEntityCollector collector = new OWLEntityCollector();

		// Speed things up by asking the collector to collect only named classes
		collector.setCollectClasses(true);
		collector.setCollectDataProperties(false);
		collector.setCollectObjectProperties(false);
		collector.setCollectIndividuals(false);
		collector.setCollectDataTypes(false);

		System.out.println(allRFPs.size() + " RFPs contained in " + rfpOntology.getURI());
		System.out.println(allAFPs.size() + " AFPs contained in " + srkbOntology.getURI());

		// No point in continuing if the RFP ontology does not contain any RFPs
		if (allRFPs.size() == 0)
			throw new SemanticRegistryMatchmakingException(
					"The ontology at " + rfpOntology.getURI() + " does not contain any RFP concepts. " +
							"Matchmaking among AFPs and RFPs is not possible.");

		// No point in continuing if the SRKB ontology does not contain any AFPs
		if (allAFPs.size() == 0)
			throw new SemanticRegistryMatchmakingException(
					"The ontology at " + srkbOntology.getURI() + " does not contain any AFP concepts. " +
							"Matchmaking among AFPs and RFPs is not possible.");

		// Keep a flag for each type of filler class expected
		boolean hasCategoryFillerFound = false;
		boolean hasInputFillerFound = false;
		boolean hasOutputFillerFound = false;

		// Keep a flag for each type of filler class expected
		hasCategoryFillerFound = false;
		hasInputFillerFound = false;
		hasOutputFillerFound = false;

		// Get the equivalent class axiom of the RFP class. By convention
		// in the FUSION project, there should be only one equivalent
		// class axiom, so this loop should be executed only once
		for (OWLEquivalentClassesAxiom ax : rfpOntology
				.getEquivalentClassesAxioms(rfpClassToClassifyAgainst))
		{
			// System.out.println("Equivalent class axiom: " + ax.toString());

			// By convention in the FUSION project, there should be two OWLDescription objects in the axiom:
			// 1) the named RFP class, and
			// 2) the anonymous class defined as an intersection of existential and universal restrictions
			// on object properties (i.e. the hasCategory, hasInput, and hasOutput properties)
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

		System.out.println(" ");
		System.out.println(rfpClassToClassifyAgainst + ": " + collectedFillerClasses.size() + " filler classes ");

		// By convention in the FUSION project, there should be three
		// classes left inside the collectedFillerClasses set:
		// 1) the filler class for the hasCategory property restriction,
		// 2) the filler class for the hasInput property restriction, and
		// 3) the filler class for the hasOutput property restriction

		// We will need to convert each retrieved OWLEntity to an OWLClass
		OWLClass fillerCls = null;

		// Determine the property corresponding to each of the three
		// fillers, by checking if a filler is an asserted subclass of
		// 1) inputDataSetRootClass, 2) outputDataSetRootClass,
		// or 3) none of the two (i.e. a taxonomy annotation)
		for (OWLEntity filler : collectedFillerClasses)
		{
			fillerCls = factory.getOWLClass(filler.getURI().normalize());
			if (isAssertedSubClassOf(fillerCls, inputDataSetRootClass, rfpOntology))
			{
				rfpHasInputPropertyFillerClass = fillerCls;
				hasInputFillerFound = true;
				System.out.println(rfpClassToClassifyAgainst + " hasInput: "
						+ rfpHasInputPropertyFillerClass.getURI());
			}
			else if (isAssertedSubClassOf(fillerCls, outputDataSetRootClass, rfpOntology))
			{
				rfpHasOutputPropertyFillerClass = fillerCls;
				hasOutputFillerFound = true;
				System.out.println(rfpClassToClassifyAgainst + " hasOutput: "
						+ rfpHasOutputPropertyFillerClass.getURI());
			}
			else
			{
				rfpHasCategoryPropertyFillerClass = fillerCls;
				hasCategoryFillerFound = true;
				System.out.println(rfpClassToClassifyAgainst + " hasCategory: "
						+ rfpHasCategoryPropertyFillerClass.getURI());
			}
		}

		// Reset the collector to reuse it later
		collector.reset();

		// No point in continuing if the filler classes of the
		// property restrictions of the RFP could not be collected
		if (rfpHasCategoryPropertyFillerClass == null)
			throw new SemanticRegistryMatchmakingException("The RFP could not be reconstructed from the ontology in which it is defined");

		// Detect the implied message exchange pattern of the RFP
		String rfpMEP = null;
		if (hasCategoryFillerFound && hasInputFillerFound && !hasOutputFillerFound) rfpMEP = "in-only";
		if (hasCategoryFillerFound && !hasInputFillerFound && hasOutputFillerFound) rfpMEP = "out-only";
		if (hasCategoryFillerFound && hasInputFillerFound && hasOutputFillerFound) rfpMEP = "in-out";
		
		
		
		
		
		// //////////////////////
		// PREPROCESSING - CREATE CATEGORY MATCHING CHECKLIST
		// //////////////////////

		// Get the set of sets of classes that are subsumed by the rfpHasOutputPropertyFillerClass
		Set<Set<OWLClass>> subsumedByAFPHasCategoryClassSetOfSets = new HashSet<Set<OWLClass>>();
		try
		{
			// Get services having a category that is the same or more specific than what the request specifies
			subsumedByAFPHasCategoryClassSetOfSets = reasoner.getDescendantClasses(rfpHasCategoryPropertyFillerClass);
		}
		catch (OWLReasonerException e)
		{
			System.out.println("OWLReasonerException: " + e.getCause());
			throw new SemanticRegistryMatchmakingException();
		}

		// Flatten the set of sets into one set...
		Set<OWLClass> categoryCheckList = new HashSet<OWLClass>();

		// First of all, add the category class itself
		categoryCheckList.add(rfpHasCategoryPropertyFillerClass);

		// ... and from each set...
		for (Set<OWLClass> subsumedByRFPHasCategoryClassSet : subsumedByAFPHasCategoryClassSetOfSets)
		{
			// ...add every class apart from owl:Nothing
			for (OWLClass tmp : subsumedByRFPHasCategoryClassSet)
			{
				categoryCheckList.add(tmp);
			}
		}

		// Note that the categoryCheckList might contain classes that
		// are not necessarily asserted subclasses of #TaxonomyEntity
		System.out.println("The taxonomy class of the RFP (" + rfpHasCategoryPropertyFillerClass.getURI() + ") matches " + categoryCheckList.size() + " classes (including itself)");





		// //////////////////////
		// PREPROCESSING - CREATE INPUT MATCHING CHECKLIST
		// //////////////////////

		// A list of all matching InputDataSet classes
		Set<OWLClass> inputCheckList = new HashSet<OWLClass>();

		// No point in preparing an input checklist if the RFP doesn't have any inputs
		if ( rfpHasInputPropertyFillerClass != null )
		{
			// Get the set of sets of classes that subsume the rfpHasInputPropertyFillerClass
			// (this includes all classes that represent collections of data, regardless of
			// whether they represent input or output data, and whether they "belong" to RFPs or AFPs
			Set<Set<OWLClass>> subsumersOfRFPInputDataSetClassSetOfSets = new HashSet<Set<OWLClass>>();
			try
			{
				// Get services that require the same or less inputs than what the request specifies
				subsumersOfRFPInputDataSetClassSetOfSets = reasoner.getAncestorClasses(rfpHasInputPropertyFillerClass);
			}
			catch (OWLReasonerException e)
			{
				System.out.println("OWLReasonerException: " + e.getCause());
				throw new SemanticRegistryMatchmakingException();
			}

			// First of all, add the InputDataSet class itself
			inputCheckList.add(rfpHasInputPropertyFillerClass);

			// ... and from each set...
			for (Set<OWLClass> tmpClassSet : subsumersOfRFPInputDataSetClassSetOfSets)
			{
				// ...pick only those classes that represent inputs (of AFPs or RFPs)
				for (OWLClass tmpClass : tmpClassSet)
				{
					inputCheckList.add(tmpClass);
				}
			}

			// At this point the inputCheckList set contains classes
			// representing not only input data of AFPs, but also of RFPs
			System.out.println("The input data set class of the RFP (" + rfpHasInputPropertyFillerClass.getURI() + ") matches " + inputCheckList.size() + " classes (including itself)");

		}





		// //////////////////////
		// PREPROCESSING - CREATE OUPUT MATCHING CHECKLIST
		// //////////////////////

		// A list of all matching OutputDataSet classes
		Set<OWLClass> outputCheckList = new HashSet<OWLClass>();

		// No point in preparing an output checklist if the service doesn't have any outputs
		if (  rfpHasOutputPropertyFillerClass != null  )
		{
			// Get the set of sets of classes that are subsumed by the rfpHasOutputPropertyFillerClass
			// (this includes all classes that represent collections of data, regardless of whether
			// they represent input or output data, and whether they "belong" to RFPs or AFPs
			Set<Set<OWLClass>> subsumedClassSetOfSets = new HashSet<Set<OWLClass>>();
			try
			{
				// Get services that provide the same or more outputs than what the request specifies
				subsumedClassSetOfSets = reasoner.getDescendantClasses(rfpHasOutputPropertyFillerClass);
			}
			catch (OWLReasonerException e)
			{
				System.out.println("OWLReasonerException: " + e.getCause());
				throw new SemanticRegistryMatchmakingException();
			}

			// First of all, add the OutputDataSet class itself
			outputCheckList.add(rfpHasOutputPropertyFillerClass);

			// ... and from each set...
			for (Set<OWLClass> tmpClassSet : subsumedClassSetOfSets)
			{
				// ...pick only those classes that represent outputs (of AFPs or RFPs)
				for (OWLClass tmpClass : tmpClassSet)
				{
					outputCheckList.add(tmpClass);
				}
			}

			// At this point the output check list contains classes
			// representing not only output data of AFPs, but also of RFPs
			System.out.println("The output data set class of the RFP (" + rfpHasOutputPropertyFillerClass.getURI() + ") matches " + outputCheckList.size() + " classes (including itself)");
		}





		// //////////////////////
		// MATCHMAKING WITH ALL AFPs
		// //////////////////////

		// Create a list to store the URIs of all AFPs that match the RFP
		List<String> returnedMatchingAFPs = new ArrayList<String>();

		// Create a reference to an object representing the TaxonomyEntity root class
		OWLClass taxonomyRootClass = factory.getOWLClass(URI.create(taxonomyOntologyLogicalURI + "#TaxonomyEntity"));

   	    // Iterate over all references to AFP classes
		for (OWLDescription afp : allAFPs)
		{
			System.out.println("\nProcessing AFP: " + afp.asOWLClass().getURI().normalize().toString());

			// Get the equivalent class axiom of the AFP class. By convention
			// in the FUSION project, there should be only one equivalent
			// class axiom, so this loop should be executed only once
			for (OWLEquivalentClassesAxiom ax : srkbOntology.getEquivalentClassesAxioms(afp.asOWLClass()))
			{
				//System.out.println("Equivalent class axiom: " + ax.toString());

				// By convention in the FUSION project, there should be two
				// OWLDescription objects in the axiom: 1) the named AFP class,
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
			collectedFillerClasses = collector.getObjects();
			//System.out.println(collectedFillerClasses.size() + " filler classes collected from " + rfp);

			// By convention in the FUSION project, there should be a maximum
			// of three classes inside the collectedFillerClasses set:
			// 1) the filler class for the hasCategory property restriction (mandatory)
			// 2) the filler class for the hasInput property restriction (for in-only and in-out services)
			// 3) the filler class for the hasOutput property restriction (for out-only and in-out services)

			// Keep a flag to speed things up a bit more
			boolean categoryMatchFound = false;
			boolean mepIsCompatible = false;

				// Keep a flag for each type of filler class expected
			boolean hasAFPCategoryFillerFound = false;
			boolean hasAFPInputFillerFound = false;
			boolean hasAFPOutputFillerFound = false;

			// Keep the filler classes
			OWLClass hasAFPCategoryFillerClass = null;
			OWLClass hasAFPInputFillerClass = null;
			OWLClass hasAFPOutputFillerClass = null;

			// We will need to convert each retrieved OWLEntity to an OWLClass
			fillerCls = null;

			// Determine the property corresponding to each filler,
			// by checking if the filler is an asserted subclass of
			// 1) inputDataSetRootClass, 2) outputDataSetRootClass,
			// or 3) none of the two (i.e. a taxonomy annotation)
			for (OWLEntity filler : collectedFillerClasses)
			{
				fillerCls = factory.getOWLClass(filler.getURI().normalize());
				if (!hasAFPInputFillerFound
						&& isAssertedSubClassOf(fillerCls, inputDataSetRootClass, srkbOntology))
				{
					hasAFPInputFillerFound = true;
					hasAFPInputFillerClass = fillerCls;
					//System.out.println(afp + " hasInput: " + fillerCls.getURI());
				}
				else if (!hasAFPOutputFillerFound
						&& isAssertedSubClassOf(fillerCls, outputDataSetRootClass, srkbOntology))
				{
					hasAFPOutputFillerFound = true;
					hasAFPOutputFillerClass = fillerCls;
					//System.out.println(afp + " hasOutput: " + fillerCls.getURI());
				}
				else // i.e. if it's the filler class for hasCategory
				{
					hasAFPCategoryFillerFound = true;
					hasAFPCategoryFillerClass = fillerCls;
					//System.out.println(afp + " hasCategory: " + fillerCls.getURI());
				}
			}

			// Detect the implied message exchange pattern of the AFP
			String afpMEP = null;
			if (hasAFPCategoryFillerFound && hasAFPInputFillerFound && !hasAFPOutputFillerFound) afpMEP = "in-only";
			if (hasAFPCategoryFillerFound && !hasAFPInputFillerFound && hasAFPOutputFillerFound) afpMEP = "out-only";
			if (hasAFPCategoryFillerFound && hasAFPInputFillerFound && hasAFPOutputFillerFound) afpMEP = "in-out";

			if (afpMEP == null) throw new SemanticRegistryConfigurationException(
					"Problem encountered while processing AFPs for matchmaking");





			// //////////////////////
			// CATEGORY MATCHMAKING
			// //////////////////////

			// If the MEPs of the AFP and the RFP match, check if
			// the AFP's hasCategory filler class is in the checklist

			if (afpMEP.equalsIgnoreCase(rfpMEP))
			{
				mepIsCompatible = true;
				System.out.println("The message exchange pattern of the AFP (" + afpMEP + ") matches the MEP of the RFP");

				for (OWLClass tmpClass : categoryCheckList)
				{
					if (!categoryMatchFound
							&& hasAFPCategoryFillerClass.getURI().normalize().toString()
									.equalsIgnoreCase(tmpClass.getURI().normalize().toString()))
					{
						// System.out.println("Category Match found!");
						categoryMatchFound = true;
					}
				}
			}
			else
			{
				mepIsCompatible = false;
				System.out.println("The message exchange pattern of the AFP (" + afpMEP + ") does not match the MEP of the RFP (" + rfpMEP + ")");
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
					System.out.println("Category match detected for AFP " + afp + ", proceeding to input matching for this AFP");
					if (inputCheckList.contains(hasAFPInputFillerClass))
					{
						System.out.println("Input match detected for AFP " + afp + ", adding it to the list of matching AFPs");
						returnedMatchingAFPs.add(afp.asOWLClass().getURI().normalize().toString());
					}
					else
						System.out.println("Input match was not detected for AFP " + afp);
				}


				// //////////////////////
				// OUT-ONLY MATCHMAKING
				// //////////////////////

				// if both AFP and RFP represent services with an out-only MEP
				if (afpMEP.equalsIgnoreCase("out-only") && rfpMEP.equalsIgnoreCase("out-only"))
				{
					System.out.println("Category match detected for AFP " + afp + ", proceeding to output matching for this AFP");
					if (outputCheckList.contains(hasAFPOutputFillerClass))
					{
						System.out.println("Output match detected for AFP " + afp + ", adding it to the list of matching AFPs");
						returnedMatchingAFPs.add(afp.asOWLClass().getURI().normalize().toString());
					}
					else
						System.out.println("Output match was not detected for AFP " + afp);
				}


				// //////////////////////
				// IN-OUT MATCHMAKING
				// //////////////////////

				// if both AFP and RFP represent services with an in-out MEP
				if (afpMEP.equalsIgnoreCase("in-out") && rfpMEP.equalsIgnoreCase("in-out"))
				{
					System.out.println("Category match detected for AFP " + afp + ", proceeding to input matching for this AFP");
					if (inputCheckList.contains(hasAFPInputFillerClass))
					{
						System.out.println("Input match detected for AFP " + afp + ", proceeding to output matching");
						if (outputCheckList.contains(hasAFPOutputFillerClass))
						{
							System.out.println("Output match detected for AFP " + afp + ", adding it to the list of matching AFPs");
							returnedMatchingAFPs.add(afp.asOWLClass().getURI().normalize()
									.toString());
						}
						else
							System.out.println("Output match was not detected for AFP " + afp);
					}
					else
						System.out.println("Input match was not detected for AFP " + afp);
				}

			} // end-if this AFP matches with the RFP at the category level
			else
			{
				if (mepIsCompatible && !categoryMatchFound)
				{
					System.out.println("Category match was not detected for RFP " + afp);
				}
			}

			// Reset the collector to reuse it in the next iteration
			collector.reset();


		}// end for every RFP





		// //////////////////////
		// WRAP-UP
		// //////////////////////

		// Create an inferredInfo list
		SemanticProfileURICollection inferredInfo = new SemanticProfileURICollection(returnedMatchingAFPs);
		return inferredInfo;

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
			throw new SemanticRegistryMatchmakingException("OWLReasonerException");
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

		// One way to check
		//System.out.print("Checking (IN MODE A) if " + classToCheck + " is subclass of " + rootClass + ": ");
		if (allAssertedSubclassesOfRootClass.contains((OWLDescription)classToCheck))
		{
			return true;
		}
		return false;
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
