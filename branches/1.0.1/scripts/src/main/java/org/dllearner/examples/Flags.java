/**
 * 
 */
package org.dllearner.examples;

import org.dllearner.core.OntologyFormat;
import org.dllearner.core.owl.BooleanDatatypePropertyAssertion;
import org.dllearner.core.owl.ClassAssertionAxiom;
import org.dllearner.core.owl.Datatype;
import org.dllearner.core.owl.DatatypeProperty;
import org.dllearner.core.owl.DatatypePropertyDomainAxiom;
import org.dllearner.core.owl.DatatypePropertyRangeAxiom;
import org.dllearner.core.owl.DoubleDatatypePropertyAssertion;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.KB;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.core.owl.ObjectPropertyAssertion;
import org.dllearner.core.owl.ObjectPropertyDomainAxiom;
import org.dllearner.core.owl.ObjectPropertyRangeAxiom;
import org.dllearner.core.owl.StringDatatypePropertyAssertion;
import org.semanticweb.owlapi.model.*;
	

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author flo
 *
 */
public class Flags {

	private static final String flagDataPath = "E:/temp/flag.data";
	//private static final String flagNamesPath = "D:/Dokumente/Uni/Semantic Web Praktikum 10/flag.names";
	private static final String kbOutputPath = "E:/temp/flag.owl";
	private static final String dataSeparator = ",";
	private static final String dbPediaIri = "http://DBpedia.org/resource";
	private static final String dbPediaOntoIri = "http://DBpedia.org/ontology";
	//private static String ontoIri = "http://www.semanticweb.org/owlapi/ontologies/uniLpz/semWeb";
	
	/*private static final String ontoFlagClassName = "Flag";
	private static final String ontoCountryClassName = "Country";	
	private static String ontoLandmassClassName = "Landmass";
	private static String ontoHemisphereClassName = "Hemisphere";
	private static String ontoLanguageClassName = "Language";
	private static String ontoLanguageGroupClassName = "LanguageGroup"; 
	private static String ontoReligionClassName = "Religion";
	private static String ontoReligionGroupClassName = "ReligionGroup";
	private static String ontoColorClassName = "Color";*/	
	private static final String ontoFlagClassName = "Flag";
	private static final String ontoCountryClassName = "";
	private static String ontoLandmassClassName = "";
	private static String ontoHemisphereClassName = "";
	private static String ontoLanguageClassName = "";
	private static String ontoLanguageGroupClassName = ""; 
	private static String ontoReligionClassName = "";
	private static String ontoReligionGroupClassName = "";
	private static String ontoColorClassName = "";
	
	private static final String iriSeperator = "#";

	//private static OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	//private static OWLDataFactory factory = manager.getOWLDataFactory();
	
	//OWLClass flagClass = factory.getOWLClass(IRI.create(ontoIri + iriSeperator + ontoFlagClassName));
	
	private static final KB kb = new KB();
	private static ArrayList<Individual> flags = new ArrayList<Individual>();
	private static ArrayList<Individual> countries = new ArrayList<Individual>();
	
	
	/*private static NamedClass Flag 							= new NamedClass(getIRI(ontoFlagClassName));		
	private static NamedClass Country 						= new NamedClass(getIRI(ontoCountryClassName));
	private static NamedClass Landmass 						= new NamedClass(getIRI(ontoLandmassClassName));
	private static NamedClass Hemisphere 					= new NamedClass(getIRI(ontoHemisphereClassName));
	private static NamedClass Language 						= new NamedClass(getIRI(ontoLanguageClassName));
	private static NamedClass LanguageGroup 				= new NamedClass(getIRI(ontoLanguageGroupClassName));
	private static NamedClass Religion 						= new NamedClass(getIRI(ontoReligionClassName));
	private static NamedClass ReligionGroup 				= new NamedClass(getIRI(ontoReligionGroupClassName));
	private static NamedClass Color 						= new NamedClass(getIRI(ontoColorClassName));*/
	private static NamedClass Flag 							= new NamedClass(getIRI("Flag",dbPediaIri,"/"));		
	private static NamedClass Country 						= new NamedClass(getIRI("Country",dbPediaIri,"/"));
	private static NamedClass Landmass 						= new NamedClass(getIRI("Continent",dbPediaOntoIri,"/"));
	private static NamedClass Hemisphere 					= new NamedClass(getIRI("Hemisphere"));
	private static NamedClass Language 						= new NamedClass(getIRI("Language",dbPediaIri,"/"));
	private static NamedClass LanguageGroup 				= new NamedClass(getIRI("LanguageGroup"));
	private static NamedClass Religion 						= new NamedClass(getIRI("Religion",dbPediaIri,"/"));
	private static NamedClass ReligionGroup 				= new NamedClass(getIRI("ReligionGroup"));
	private static NamedClass Color 						= new NamedClass(getIRI("Color",dbPediaIri,"/"));
	
	// Objektbeziehungen
	private static ObjectProperty isFlagOf 					= new ObjectProperty(getIRI("isFlagOf"));
	private static ObjectProperty isOnLandmass 				= new ObjectProperty(getIRI("isOnLandmass"));
	private static ObjectProperty isInZone 					= new ObjectProperty(getIRI("isInZone"));	
	private static ObjectProperty spokenLanguage 			= new ObjectProperty(getIRI("spokenLanguage"));
	private static ObjectProperty spokenLanguageGroup 		= new ObjectProperty(getIRI("spokenLanguageGroup"));
	private static ObjectProperty hasReligion 				= new ObjectProperty(getIRI("hasReligion"));
	private static ObjectProperty hasReligionGroup 			= new ObjectProperty(getIRI("hasReligionGroup"));
	private static ObjectProperty hasColor 					= new ObjectProperty(getIRI("hasColor"));
	private static ObjectProperty hasPredominationColor 	= new ObjectProperty(getIRI("hasPredominationColor"));
	private static ObjectProperty hasTopleftColor 			= new ObjectProperty(getIRI("hasTopleftColor"));
	private static ObjectProperty hasBottomRightColor 		= new ObjectProperty(getIRI("hasBottomRightColor"));
	
	// Objekteigenschaften		
	private static DatatypeProperty hasName 				= new DatatypeProperty(getIRI("hasName"));
	private static DatatypeProperty hasArea 				= new DatatypeProperty(getIRI("hasArea"));
	private static DatatypeProperty hasPopulation 			= new DatatypeProperty(getIRI("hasPopulation"));
	private static DatatypeProperty numberOfVericalBars 	= new DatatypeProperty(getIRI("numberOfVericalBars"));
	private static DatatypeProperty numberOfHorizontalStripes = new DatatypeProperty(getIRI("numberOfHorizontalStripes"));
	private static DatatypeProperty numberOfDifferentColors = new DatatypeProperty(getIRI("numberOfVericalBars"));
	private static DatatypeProperty hasColorRed 			= new DatatypeProperty(getIRI("hasColorRed"));
	private static DatatypeProperty hasColorGreen 			= new DatatypeProperty(getIRI("hasColorGreen"));
	private static DatatypeProperty hasColorBlue 			= new DatatypeProperty(getIRI("hasColorBlue"));
	private static DatatypeProperty hasColorGold 			= new DatatypeProperty(getIRI("hasColorGold"));
	private static DatatypeProperty hasColorWhite 			= new DatatypeProperty(getIRI("hasColorWhite"));
	private static DatatypeProperty hasColorBlack 			= new DatatypeProperty(getIRI("hasColorBlack"));
	private static DatatypeProperty hasColorOrange 			= new DatatypeProperty(getIRI("hasColorOrange"));
	private static DatatypeProperty numberOfCircles 		= new DatatypeProperty(getIRI("numberOfCircles"));
	private static DatatypeProperty numberOfUprightCrosses 	= new DatatypeProperty(getIRI("numberOfUprightCrosses"));
	private static DatatypeProperty numberOfDiagonalCrosses = new DatatypeProperty(getIRI("numberOfDiagonalCrosses"));
	private static DatatypeProperty numberOfQuarterSections = new DatatypeProperty(getIRI("numberOfQuarterSections"));
	private static DatatypeProperty numberOfQuarteredSections = new DatatypeProperty(getIRI("numberOfQuarteredSections"));
	private static DatatypeProperty numberOfSunOrStarSymbols = new DatatypeProperty(getIRI("numberOfSunOrStarSymbols"));
	private static DatatypeProperty hasCrescentMoonSymbol 	= new DatatypeProperty(getIRI("hasCrescentMoonSymbol"));
	private static DatatypeProperty hasTriangle 			= new DatatypeProperty(getIRI("hasTriangle"));
	private static DatatypeProperty hasImageInanimate 		= new DatatypeProperty(getIRI("hasImageInanimate"));
	private static DatatypeProperty hasImageAnimate 		= new DatatypeProperty(getIRI("hasImageAnimate"));
	private static DatatypeProperty hasText 				= new DatatypeProperty(getIRI("hasText"));

	private static Individual landmassNorthAmerica 			= new Individual(getIRI(ontoLandmassClassName + "North_America",dbPediaIri,"/"));
	private static Individual landmassSouthAmerica 			= new Individual(getIRI(ontoLandmassClassName + "South_America",dbPediaIri,"/"));
	private static Individual landmassEurope 				= new Individual(getIRI(ontoLandmassClassName + "Europe",dbPediaIri,"/"));
	private static Individual landmassAfrica 				= new Individual(getIRI(ontoLandmassClassName + "Africa",dbPediaIri,"/"));
	private static Individual landmassAsia					= new Individual(getIRI(ontoLandmassClassName + "Asia",dbPediaIri,"/"));
	private static Individual landmassOceania 				= new Individual(getIRI(ontoLandmassClassName + "Australia_(continent)",dbPediaIri,"/"));
	
	private static Individual hemisphereNorthEast			= new Individual(getIRI(ontoHemisphereClassName + "NorthEast"));
	private static Individual hemisphereSouthEast			= new Individual(getIRI(ontoHemisphereClassName + "SouthEast"));
	private static Individual hemisphereSouthWest			= new Individual(getIRI(ontoHemisphereClassName + "SouthWest"));
	private static Individual hemisphereNorthWest			= new Individual(getIRI(ontoHemisphereClassName + "NorthWest"));
	
	private static Individual languageGroupEnglish			= new Individual(getIRI(ontoLanguageGroupClassName + "English"));
	private static Individual languageGroupSpanish			= new Individual(getIRI(ontoLanguageGroupClassName + "Spanish"));
	private static Individual languageGroupFrench			= new Individual(getIRI(ontoLanguageGroupClassName + "French"));
	private static Individual languageGroupGerman			= new Individual(getIRI(ontoLanguageGroupClassName + "German"));
	private static Individual languageGroupSlavic			= new Individual(getIRI(ontoLanguageGroupClassName + "Slavic"));
	private static Individual languageGroupIndoEuropean		= new Individual(getIRI(ontoLanguageGroupClassName + "IndoEuropean"));
	private static Individual languageGroupChinese			= new Individual(getIRI(ontoLanguageGroupClassName + "Chinese"));
	private static Individual languageGroupArabic			= new Individual(getIRI(ontoLanguageGroupClassName + "Arabic"));
	private static Individual languageGroupJapTurkFinnMag	= new Individual(getIRI(ontoLanguageGroupClassName + "JapaneseTurkishFinnishMagyar"));
	private static Individual languageGroupOthers			= new Individual(getIRI(ontoLanguageGroupClassName + "Others"));
	
	private static Individual religionGroupCatholic			= new Individual(getIRI(ontoReligionGroupClassName + "Catholic"));
	private static Individual religionGroupOtherChristian	= new Individual(getIRI(ontoReligionGroupClassName + "OtherChristian"));
	private static Individual religionGroupMuslim			= new Individual(getIRI(ontoReligionGroupClassName + "Muslim"));
	private static Individual religionGroupBuddhist			= new Individual(getIRI(ontoReligionGroupClassName + "Buddhist"));
	private static Individual religionGroupHindu			= new Individual(getIRI(ontoReligionGroupClassName + "Hindu"));
	private static Individual religionGroupEthnic			= new Individual(getIRI(ontoReligionGroupClassName + "Ethnic"));
	private static Individual religionGroupMarxist			= new Individual(getIRI(ontoReligionGroupClassName + "Marxist"));
	private static Individual religionGroupOthers			= new Individual(getIRI(ontoReligionGroupClassName + "Others"));
	
	private static Individual colorRed						= new Individual(getIRI(ontoColorClassName + "red"));
	private static Individual colorGreen					= new Individual(getIRI(ontoColorClassName + "green"));
	private static Individual colorBlue						= new Individual(getIRI(ontoColorClassName + "blue"));
	private static Individual colorGold						= new Individual(getIRI(ontoColorClassName + "gold"));
	private static Individual colorYellow					= new Individual(getIRI(ontoColorClassName + "yellow"));
	private static Individual colorWhite					= new Individual(getIRI(ontoColorClassName + "white"));
	private static Individual colorBlack					= new Individual(getIRI(ontoColorClassName + "black"));
	private static Individual colorOrange					= new Individual(getIRI(ontoColorClassName + "orange"));
	private static Individual colorBrown					= new Individual(getIRI(ontoColorClassName + "brown"));
	private static HashMap<String, Individual> newColors			= new HashMap<String, Individual>();
	
	/**
	 * @param args
	 */

		
		// Klassen
//		NamedClass Flag = new NamedClass(getIRI(ontoFlagClassName));		
//		NamedClass Country = new NamedClass(getIRI(ontoCountryClassName));
//		NamedClass Landmass = new NamedClass(getIRI(ontoLandmassClassName));
//		NamedClass Hemisphere = new NamedClass(getIRI(ontoHemisphereClassName));
//		NamedClass Language = new NamedClass(getIRI(ontoLanguageClassName));
//		NamedClass LanguageGroup = new NamedClass(getIRI(ontoLanguageGroupClassName));
//		NamedClass Religion = new NamedClass(getIRI(ontoReligionClassName));
//		NamedClass ReligionGroup = new NamedClass(getIRI(ontoReligionGroupClassName));
//		NamedClass Color = new NamedClass(getIRI(ontoColorClassName));
		
	public static boolean createKB()
	{
		
		// Objektbeziehungen
//		ObjectProperty isFlagOf = new ObjectProperty(getIRI("isFlagOf"));
		kb.addAxiom(new ObjectPropertyDomainAxiom(isFlagOf, Flag));
		kb.addAxiom(new ObjectPropertyRangeAxiom(isFlagOf, Country));
		
//		ObjectProperty isOnLandmass = new ObjectProperty(getIRI("isOnLandmass"));
		kb.addAxiom(new ObjectPropertyDomainAxiom(isOnLandmass, Country));
		kb.addAxiom(new ObjectPropertyRangeAxiom(isOnLandmass, Landmass));
		
//		ObjectProperty isInZone = new ObjectProperty(getIRI("isInZone"));
		kb.addAxiom(new ObjectPropertyDomainAxiom(isInZone, Country));
		kb.addAxiom(new ObjectPropertyRangeAxiom(isInZone, Hemisphere));
		
//		ObjectProperty spokenLanguage = new ObjectProperty(getIRI("spokenLanguage"));
		kb.addAxiom(new ObjectPropertyDomainAxiom(spokenLanguage, Country));
		kb.addAxiom(new ObjectPropertyRangeAxiom(spokenLanguage, LanguageGroup));
		kb.addAxiom(new ObjectPropertyRangeAxiom(spokenLanguage, Language));
		
//		ObjectProperty spokenLanguageGroup = new ObjectProperty(getIRI("spokenLanguageGroup"));
		kb.addAxiom(new ObjectPropertyDomainAxiom(spokenLanguageGroup, Country));
		kb.addAxiom(new ObjectPropertyRangeAxiom(spokenLanguageGroup, LanguageGroup));
		
//		ObjectProperty hasReligion = new ObjectProperty(getIRI("hasReligion"));
		kb.addAxiom(new ObjectPropertyDomainAxiom(hasReligion, Country));
		kb.addAxiom(new ObjectPropertyDomainAxiom(hasReligion, ReligionGroup));
		kb.addAxiom(new ObjectPropertyRangeAxiom(hasReligion, Religion));
		
//		ObjectProperty hasReligionGroup = new ObjectProperty(getIRI("hasReligionGroup"));
		kb.addAxiom(new ObjectPropertyDomainAxiom(hasReligionGroup, Country));
		kb.addAxiom(new ObjectPropertyRangeAxiom(hasReligionGroup, ReligionGroup));
		
//		ObjectProperty hasColor = new ObjectProperty(getIRI("hasColor"));
		kb.addAxiom(new ObjectPropertyDomainAxiom(hasColor, Flag));
		kb.addAxiom(new ObjectPropertyRangeAxiom(hasColor, Color));
		
//		ObjectProperty hasPredominationColor = new ObjectProperty(getIRI("hasPredominationColor"));
		kb.addAxiom(new ObjectPropertyDomainAxiom(hasPredominationColor, Flag));
		kb.addAxiom(new ObjectPropertyRangeAxiom(hasPredominationColor, Color));
		
//		ObjectProperty hasTopleftColor = new ObjectProperty(getIRI("hasTopleftColor"));
		kb.addAxiom(new ObjectPropertyDomainAxiom(hasTopleftColor, Flag));
		kb.addAxiom(new ObjectPropertyRangeAxiom(hasTopleftColor, Color));
		
//		ObjectProperty hasBottomRightColor = new ObjectProperty(getIRI("hasBottomRightColor"));
		kb.addAxiom(new ObjectPropertyDomainAxiom(hasBottomRightColor, Flag));
		kb.addAxiom(new ObjectPropertyRangeAxiom(hasBottomRightColor, Color));
		
		// Objekteigenschaften			
//		private static DatatypeProperty hasName 				= new DatatypeProperty(getIRI("hasName"));	
		kb.addAxiom(new DatatypePropertyDomainAxiom(hasName, Country));
		kb.addAxiom(new DatatypePropertyDomainAxiom(hasName, Landmass));
		kb.addAxiom(new DatatypePropertyDomainAxiom(hasName, Hemisphere));
		kb.addAxiom(new DatatypePropertyDomainAxiom(hasName, ReligionGroup));
		kb.addAxiom(new DatatypePropertyDomainAxiom(hasName, LanguageGroup));
		kb.addAxiom(new DatatypePropertyDomainAxiom(hasName, Religion));
		kb.addAxiom(new DatatypePropertyDomainAxiom(hasName, Language));
		kb.addAxiom(new DatatypePropertyDomainAxiom(hasName, Color));
		kb.addAxiom(new DatatypePropertyDomainAxiom(hasName, Flag));
		kb.addAxiom(new DatatypePropertyRangeAxiom(hasName, Datatype.STRING));
		
//		DatatypeProperty hasArea = new DatatypeProperty(getIRI("hasArea"));
		kb.addAxiom(new DatatypePropertyDomainAxiom(hasArea, Country));
		kb.addAxiom(new DatatypePropertyDomainAxiom(hasArea, Landmass));
		kb.addAxiom(new DatatypePropertyDomainAxiom(hasArea, Hemisphere));		
		kb.addAxiom(new DatatypePropertyRangeAxiom(hasArea, Datatype.DOUBLE));
		
//		DatatypeProperty hasPopulation = new DatatypeProperty(getIRI("hasPopulation"));
		kb.addAxiom(new DatatypePropertyDomainAxiom(hasPopulation, Country));
		kb.addAxiom(new DatatypePropertyDomainAxiom(hasPopulation, Landmass));
		kb.addAxiom(new DatatypePropertyDomainAxiom(hasPopulation, Hemisphere));
		kb.addAxiom(new DatatypePropertyRangeAxiom(hasPopulation, Datatype.DOUBLE));
		
//		DatatypeProperty numberOfVericalBars = new DatatypeProperty(getIRI("numberOfVericalBars"));
		kb.addAxiom(new DatatypePropertyDomainAxiom(numberOfVericalBars, Flag));
		kb.addAxiom(new DatatypePropertyRangeAxiom(numberOfVericalBars, Datatype.DOUBLE));
		
//		DatatypeProperty numberOfHorizontalStripes = new DatatypeProperty(getIRI("numberOfHorizontalStripes"));
		kb.addAxiom(new DatatypePropertyDomainAxiom(numberOfHorizontalStripes, Flag));
		kb.addAxiom(new DatatypePropertyRangeAxiom(numberOfHorizontalStripes, Datatype.DOUBLE));
		
//		DatatypeProperty numberOfDifferentColors = new DatatypeProperty(getIRI("numberOfVericalBars"));
		kb.addAxiom(new DatatypePropertyDomainAxiom(numberOfDifferentColors, Flag));
		kb.addAxiom(new DatatypePropertyRangeAxiom(numberOfDifferentColors, Datatype.DOUBLE));
		
//		DatatypeProperty hasColorRed = new DatatypeProperty(getIRI("hasColorRed"));
		kb.addAxiom(new DatatypePropertyDomainAxiom(hasColorRed, Flag));
		kb.addAxiom(new DatatypePropertyRangeAxiom(hasColorRed, Datatype.BOOLEAN));
		
//		DatatypeProperty hasColorGreen = new DatatypeProperty(getIRI("hasColorGreen"));
		kb.addAxiom(new DatatypePropertyDomainAxiom(hasColorGreen, Flag));
		kb.addAxiom(new DatatypePropertyRangeAxiom(hasColorGreen, Datatype.BOOLEAN));
		
//		DatatypeProperty hasColorBlue = new DatatypeProperty(getIRI("hasColorBlue"));
		kb.addAxiom(new DatatypePropertyDomainAxiom(hasColorBlue, Flag));
		kb.addAxiom(new DatatypePropertyRangeAxiom(hasColorBlue, Datatype.BOOLEAN));
		
//		DatatypeProperty hasColorGold = new DatatypeProperty(getIRI("hasColorGold"));
		kb.addAxiom(new DatatypePropertyDomainAxiom(hasColorGold, Flag));
		kb.addAxiom(new DatatypePropertyRangeAxiom(hasColorGold, Datatype.BOOLEAN));
		
//		DatatypeProperty hasColorWhite = new DatatypeProperty(getIRI("hasColorWhite"));
		kb.addAxiom(new DatatypePropertyDomainAxiom(hasColorWhite, Flag));
		kb.addAxiom(new DatatypePropertyRangeAxiom(hasColorWhite, Datatype.BOOLEAN));
		
//		DatatypeProperty hasColorBlack = new DatatypeProperty(getIRI("hasColorBlack"));
		kb.addAxiom(new DatatypePropertyDomainAxiom(hasColorBlack, Flag));
		kb.addAxiom(new DatatypePropertyRangeAxiom(hasColorBlack, Datatype.BOOLEAN));
		
//		DatatypeProperty hasColorOrange = new DatatypeProperty(getIRI("hasColorOrange"));
		kb.addAxiom(new DatatypePropertyDomainAxiom(hasColorOrange, Flag));
		kb.addAxiom(new DatatypePropertyRangeAxiom(hasColorOrange, Datatype.BOOLEAN));
		
//		DatatypeProperty numberOfCircles = new DatatypeProperty(getIRI("numberOfCircles"));
		kb.addAxiom(new DatatypePropertyDomainAxiom(numberOfCircles, Flag));
		kb.addAxiom(new DatatypePropertyRangeAxiom(numberOfCircles, Datatype.DOUBLE));
		
//		DatatypeProperty numberOfUprightCrosses = new DatatypeProperty(getIRI("numberOfUprightCrosses"));
		kb.addAxiom(new DatatypePropertyDomainAxiom(numberOfUprightCrosses, Flag));
		kb.addAxiom(new DatatypePropertyRangeAxiom(numberOfUprightCrosses, Datatype.DOUBLE));
		
//		DatatypeProperty numberOfDiagonalCrosses = new DatatypeProperty(getIRI("numberOfDiagonalCrosses"));
		kb.addAxiom(new DatatypePropertyDomainAxiom(numberOfDiagonalCrosses, Flag));
		kb.addAxiom(new DatatypePropertyRangeAxiom(numberOfDiagonalCrosses, Datatype.DOUBLE));
		
//		DatatypeProperty numberOfQuarterSections = new DatatypeProperty(getIRI("numberOfQuarterSections"));
		kb.addAxiom(new DatatypePropertyDomainAxiom(numberOfQuarterSections, Flag));
		kb.addAxiom(new DatatypePropertyRangeAxiom(numberOfQuarterSections, Datatype.DOUBLE));
		
//		DatatypeProperty numberOfQuarteredSections = new DatatypeProperty(getIRI("numberOfQuarteredSections"));
		kb.addAxiom(new DatatypePropertyDomainAxiom(numberOfQuarteredSections, Flag));
		kb.addAxiom(new DatatypePropertyRangeAxiom(numberOfQuarteredSections, Datatype.DOUBLE));
		
//		DatatypeProperty numberOfSunOrStarSymbols = new DatatypeProperty(getIRI("numberOfSunOrStarSymbols"));
		kb.addAxiom(new DatatypePropertyDomainAxiom(numberOfSunOrStarSymbols, Flag));
		kb.addAxiom(new DatatypePropertyRangeAxiom(numberOfSunOrStarSymbols, Datatype.DOUBLE));
		
//		DatatypeProperty hasCrescentMoonSymbol = new DatatypeProperty(getIRI("hasCrescentMoonSymbol"));
		kb.addAxiom(new DatatypePropertyDomainAxiom(hasCrescentMoonSymbol, Flag));
		kb.addAxiom(new DatatypePropertyRangeAxiom(hasCrescentMoonSymbol, Datatype.BOOLEAN));
		
//		DatatypeProperty hasTriangle = new DatatypeProperty(getIRI("hasTriangle"));
		kb.addAxiom(new DatatypePropertyDomainAxiom(hasTriangle, Flag));
		kb.addAxiom(new DatatypePropertyRangeAxiom(hasTriangle, Datatype.BOOLEAN));
		
//		DatatypeProperty hasImageInanimate = new DatatypeProperty(getIRI("hasImageInanimate"));
		kb.addAxiom(new DatatypePropertyDomainAxiom(hasImageInanimate, Flag));
		kb.addAxiom(new DatatypePropertyRangeAxiom(hasImageInanimate, Datatype.BOOLEAN));
		
//		DatatypeProperty hasImageAnimate = new DatatypeProperty(getIRI("hasImageAnimate"));
		kb.addAxiom(new DatatypePropertyDomainAxiom(hasImageAnimate, Flag));
		kb.addAxiom(new DatatypePropertyRangeAxiom(hasImageAnimate, Datatype.BOOLEAN));
		
//		DatatypeProperty hasText = new DatatypeProperty(getIRI("hasText"));
		kb.addAxiom(new DatatypePropertyDomainAxiom(hasText, Flag));
		kb.addAxiom(new DatatypePropertyRangeAxiom(hasText, Datatype.BOOLEAN));


		
//		Individual landmassNorthAmerica 		= new Individual(getIRI(ontoLandmassClassName + "NorthAmerica"));
		kb.addAxiom(new ClassAssertionAxiom(Landmass,landmassNorthAmerica));
		kb.addAxiom(new StringDatatypePropertyAssertion(hasName, landmassNorthAmerica, "North America"));
//		Individual landmassSouthAmerica 		= new Individual(getIRI(ontoLandmassClassName + "SouthAmerica"));
		kb.addAxiom(new ClassAssertionAxiom(Landmass,landmassSouthAmerica));
		kb.addAxiom(new StringDatatypePropertyAssertion(hasName, landmassSouthAmerica, "South America"));
//		Individual landmassEurope 				= new Individual(getIRI(ontoLandmassClassName + "Europe"));
		kb.addAxiom(new ClassAssertionAxiom(Landmass,landmassEurope));
		kb.addAxiom(new StringDatatypePropertyAssertion(hasName, landmassEurope, "Europe"));
//		Individual landmassAfrica 				= new Individual(getIRI(ontoLandmassClassName + "Africa"));
		kb.addAxiom(new ClassAssertionAxiom(Landmass,landmassAfrica));
		kb.addAxiom(new StringDatatypePropertyAssertion(hasName, landmassAfrica, "Africa"));
//		Individual landmassAsia					= new Individual(getIRI(ontoLandmassClassName + "Asia"));
		kb.addAxiom(new ClassAssertionAxiom(Landmass,landmassAsia));
		kb.addAxiom(new StringDatatypePropertyAssertion(hasName, landmassAsia, "Asia"));
//		Individual landmassOceania 				= new Individual(getIRI(ontoLandmassClassName + "Oceania"));
		kb.addAxiom(new ClassAssertionAxiom(Landmass,landmassOceania));
		kb.addAxiom(new StringDatatypePropertyAssertion(hasName, landmassOceania, "Oceania"));
		
		
//		Individual hemisphereNorthEast			= new Individual(getIRI(ontoHemisphereClassName + "NorthEast"));
		kb.addAxiom(new ClassAssertionAxiom(Hemisphere,hemisphereNorthEast));
//		Individual hemisphereSouthEast			= new Individual(getIRI(ontoHemisphereClassName + "SouthEast"));
		kb.addAxiom(new ClassAssertionAxiom(Hemisphere,hemisphereSouthEast));
//		Individual hemisphereSouthWest			= new Individual(getIRI(ontoHemisphereClassName + "SouthWest"));
		kb.addAxiom(new ClassAssertionAxiom(Hemisphere,hemisphereSouthWest));
//		Individual hemisphereNorthWest			= new Individual(getIRI(ontoHemisphereClassName + "NorthWest"));
		kb.addAxiom(new ClassAssertionAxiom(Hemisphere,hemisphereNorthWest));
		
//		Individual languageGroupEnglish			= new Individual(getIRI(ontoLanguageGroupClassName + "English"));
		kb.addAxiom(new ClassAssertionAxiom(LanguageGroup,languageGroupEnglish));
//		Individual languageGroupSpanish			= new Individual(getIRI(ontoLanguageGroupClassName + "Spanish"));
		kb.addAxiom(new ClassAssertionAxiom(LanguageGroup,languageGroupSpanish));
//		Individual languageGroupFrench			= new Individual(getIRI(ontoLanguageGroupClassName + "French"));
		kb.addAxiom(new ClassAssertionAxiom(LanguageGroup,languageGroupFrench));
//		Individual languageGroupGerman			= new Individual(getIRI(ontoLanguageGroupClassName + "German"));
		kb.addAxiom(new ClassAssertionAxiom(LanguageGroup,languageGroupGerman));
//		Individual languageGroupSlavic			= new Individual(getIRI(ontoLanguageGroupClassName + "Slavic"));
		kb.addAxiom(new ClassAssertionAxiom(LanguageGroup,languageGroupSlavic));
//		Individual languageGroupIndoEuropean	= new Individual(getIRI(ontoLanguageGroupClassName + "IndoEuropean"));
		kb.addAxiom(new ClassAssertionAxiom(LanguageGroup,languageGroupIndoEuropean));
//		Individual languageGroupChinese			= new Individual(getIRI(ontoLanguageGroupClassName + "Chinese"));
		kb.addAxiom(new ClassAssertionAxiom(LanguageGroup,languageGroupChinese));
//		Individual languageGroupArabic			= new Individual(getIRI(ontoLanguageGroupClassName + "Arabic"));
		kb.addAxiom(new ClassAssertionAxiom(LanguageGroup,languageGroupArabic));
//		Individual languageGroupJapTurkFinnMag	= new Individual(getIRI(ontoLanguageGroupClassName + "JapaneseTurkishFinnishMagyar"));
		kb.addAxiom(new ClassAssertionAxiom(LanguageGroup,languageGroupJapTurkFinnMag));
//		Individual languageGroupOthers			= new Individual(getIRI(ontoLanguageGroupClassName + "Others"));
		kb.addAxiom(new ClassAssertionAxiom(LanguageGroup,languageGroupOthers));
		
//		Individual religionGroupCatholic		= new Individual(getIRI(ontoReligionGroupClassName + "Catholic"));
		kb.addAxiom(new ClassAssertionAxiom(ReligionGroup,religionGroupCatholic));
//		Individual religionGroupOtherChristian	= new Individual(getIRI(ontoReligionGroupClassName + "OtherChristian"));
		kb.addAxiom(new ClassAssertionAxiom(ReligionGroup,religionGroupOtherChristian));
//		Individual religionGroupMuslim			= new Individual(getIRI(ontoReligionGroupClassName + "Muslim"));
		kb.addAxiom(new ClassAssertionAxiom(ReligionGroup,religionGroupMuslim));
//		Individual religionGroupBuddhist		= new Individual(getIRI(ontoReligionGroupClassName + "Buddhist"));
		kb.addAxiom(new ClassAssertionAxiom(ReligionGroup,religionGroupBuddhist));
//		Individual religionGroupHindu			= new Individual(getIRI(ontoReligionGroupClassName + "Hindu"));
		kb.addAxiom(new ClassAssertionAxiom(ReligionGroup,religionGroupHindu));
//		Individual religionGroupEthnic			= new Individual(getIRI(ontoReligionGroupClassName + "Ethnic"));
		kb.addAxiom(new ClassAssertionAxiom(ReligionGroup,religionGroupEthnic));
//		Individual religionGroupMarxist			= new Individual(getIRI(ontoReligionGroupClassName + "Marxist"));
		kb.addAxiom(new ClassAssertionAxiom(ReligionGroup,religionGroupMarxist));
//		Individual religionGroupOthers			= new Individual(getIRI(ontoReligionGroupClassName + "Others"));
		kb.addAxiom(new ClassAssertionAxiom(ReligionGroup,religionGroupOthers));
		
//		Individual colorRed						= new Individual(getIRI(ontoColorClassName + "Red"));
		kb.addAxiom(new ClassAssertionAxiom(Color,colorRed));
//		Individual colorGreen					= new Individual(getIRI(ontoColorClassName + "Green"));
		kb.addAxiom(new ClassAssertionAxiom(Color,colorGreen));
//		Individual colorBlue					= new Individual(getIRI(ontoColorClassName + "Blue"));
		kb.addAxiom(new ClassAssertionAxiom(Color,colorBlue));
//		Individual colorGold					= new Individual(getIRI(ontoColorClassName + "Gold"));
		kb.addAxiom(new ClassAssertionAxiom(Color,colorGold));
//		Individual colorYellow					= new Individual(getIRI(ontoColorClassName + "Yellow"));
		kb.addAxiom(new ClassAssertionAxiom(Color,colorYellow));
//		Individual colorWhite					= new Individual(getIRI(ontoColorClassName + "White"));
		kb.addAxiom(new ClassAssertionAxiom(Color,colorWhite));
//		Individual colorBlack					= new Individual(getIRI(ontoColorClassName + "Black"));
		kb.addAxiom(new ClassAssertionAxiom(Color,colorBlack));
//		Individual colorOrange					= new Individual(getIRI(ontoColorClassName + "Orange"));
		kb.addAxiom(new ClassAssertionAxiom(Color,colorOrange));
//		Individual colorBrown					= new Individual(getIRI(ontoColorClassName + "Brown"));
		kb.addAxiom(new ClassAssertionAxiom(Color,colorBrown));
		
		
		/* 1. name: Name of the country concerned
		# 2. landmass: 1=N.America, 2=S.America, 3=Europe, 4=Africa, 4=Asia, 6=Oceania
		# 3. zone: Geographic quadrant, based on Greenwich and the Equator; 1=NE, 2=SE, 3=SW, 4=NW
		# 4. area: in thousands of square km
		# 5. population: in round millions
		# 6. language: 1=English, 2=Spanish, 3=French, 4=German, 5=Slavic, 6=Other Indo-European, 7=Chinese, 8=Arabic, 9=Japanese/Turkish/Finnish/Magyar, 10=Others
		# 7. religion: 0=Catholic, 1=Other Christian, 2=Muslim, 3=Buddhist, 4=Hindu, 5=Ethnic, 6=Marxist, 7=Others
		# 8. bars: Number of vertical bars in the flag
		# 9. stripes: Number of horizontal stripes in the flag
		# 10. colours: Number of different colours in the flag
		# 11. red: 0 if red absent, 1 if red present in the flag
		# 12. green: same for green
		# 13. blue: same for blue
		# 14. gold: same for gold (also yellow)
		# 15. white: same for white
		# 16. black: same for black
		# 17. orange: same for orange (also brown)
		# 18. mainhue: predominant colour in the flag (tie-breaks decided by taking the topmost hue, if that fails then the most central hue, and if that fails the leftmost hue)
		# 19. circles: Number of circles in the flag
		# 20. crosses: Number of (upright) crosses
		# 21. saltires: Number of diagonal crosses
		# 22. quarters: Number of quartered sections
		# 23. sunstars: Number of sun or star symbols
		# 24. crescent: 1 if a crescent moon symbol present, else 0
		# 25. triangle: 1 if any triangles present, 0 otherwise
		# 26. icon: 1 if an inanimate image present (e.g., a boat), otherwise 0
		# 27. animate: 1 if an animate image (e.g., an eagle, a tree, a human hand) present, 0 otherwise
		# 28. text: 1 if any letters or writing on the flag (e.g., a motto or slogan), 0 otherwise
		# 29. topleft: colour in the top-left corner (moving right to decide tie-breaks)
		# 30. botright: Colour in the bottom-left corner (moving left to decide tie-breaks)
		 */
		
		
		readData(flagDataPath);
		
		kb.export(new File(kbOutputPath), OntologyFormat.RDF_XML);
		
		return true;
	}
	
	public static boolean newFlag(String[] inputParts, int count)
	{
		// Flag
		flags.add(new Individual(getIRI(ontoFlagClassName + "Of" + inputParts[0])));
		kb.addAxiom(new ClassAssertionAxiom(Flag, flags.get(count)));
		// Country + name
		countries.add(new Individual(getIRI(ontoCountryClassName + inputParts[0])));
		kb.addAxiom(new ClassAssertionAxiom(Country, countries.get(count)));
		kb.addAxiom(new ObjectPropertyAssertion(isFlagOf, flags.get(count), countries.get(count)));		
		kb.addAxiom(new StringDatatypePropertyAssertion(hasName,countries.get(count),inputParts[0]));
		// landmass		
		kb.addAxiom(new ObjectPropertyAssertion(isOnLandmass, countries.get(count), getLandmass(Integer.valueOf(inputParts[1]))));		
		// zone
		kb.addAxiom(new ObjectPropertyAssertion(isInZone, countries.get(count), getZone(Integer.valueOf(inputParts[2]))));
		// area
		kb.addAxiom(new DoubleDatatypePropertyAssertion(hasArea, countries.get(count), Double.valueOf(inputParts[3])));
		// population
		kb.addAxiom(new DoubleDatatypePropertyAssertion(hasPopulation, countries.get(count), Double.valueOf(inputParts[4])));		
		// language
		kb.addAxiom(new ObjectPropertyAssertion(spokenLanguageGroup, countries.get(count), getLanguageGroup(Integer.valueOf(inputParts[5]))));
		// religion
		kb.addAxiom(new ObjectPropertyAssertion(hasReligionGroup, countries.get(count), getReligionGroup(Integer.valueOf(inputParts[6]))));
		// verticals
		kb.addAxiom(new DoubleDatatypePropertyAssertion(numberOfVericalBars, flags.get(count), Double.valueOf(inputParts[7])));
		// horizontals
		kb.addAxiom(new DoubleDatatypePropertyAssertion(numberOfHorizontalStripes, flags.get(count), Double.valueOf(inputParts[8])));
		// colors
		kb.addAxiom(new DoubleDatatypePropertyAssertion(numberOfDifferentColors, flags.get(count), Double.valueOf(inputParts[9])));
		// red
		kb.addAxiom(new BooleanDatatypePropertyAssertion(hasColorRed, flags.get(count), Boolean.valueOf(inputParts[10])));
		if(Boolean.valueOf(inputParts[10]))
			kb.addAxiom(new ObjectPropertyAssertion(hasColor, flags.get(count), colorRed));
		// green
		kb.addAxiom(new BooleanDatatypePropertyAssertion(hasColorGreen, flags.get(count), Boolean.valueOf(inputParts[11])));
		if(Boolean.valueOf(inputParts[11]))
			kb.addAxiom(new ObjectPropertyAssertion(hasColor, flags.get(count), colorGreen));
		// blue
		kb.addAxiom(new BooleanDatatypePropertyAssertion(hasColorBlue, flags.get(count), Boolean.valueOf(inputParts[12])));
		if(Boolean.valueOf(inputParts[12]))
			kb.addAxiom(new ObjectPropertyAssertion(hasColor, flags.get(count), colorBlue));
		// gold
		kb.addAxiom(new BooleanDatatypePropertyAssertion(hasColorGold, flags.get(count), Boolean.valueOf(inputParts[13])));
		if(Boolean.valueOf(inputParts[13]))
			kb.addAxiom(new ObjectPropertyAssertion(hasColor, flags.get(count), colorGold));
		// white
		kb.addAxiom(new BooleanDatatypePropertyAssertion(hasColorWhite, flags.get(count), Boolean.valueOf(inputParts[14])));
		if(Boolean.valueOf(inputParts[14]))
			kb.addAxiom(new ObjectPropertyAssertion(hasColor, flags.get(count), colorWhite));
		// black
		kb.addAxiom(new BooleanDatatypePropertyAssertion(hasColorBlack, flags.get(count), Boolean.valueOf(inputParts[15])));
		if(Boolean.valueOf(inputParts[15]))
			kb.addAxiom(new ObjectPropertyAssertion(hasColor, flags.get(count), colorBlack));
		// orange
		kb.addAxiom(new BooleanDatatypePropertyAssertion(hasColorOrange, flags.get(count), Boolean.valueOf(inputParts[16])));
		if(Boolean.valueOf(inputParts[16]))
			kb.addAxiom(new ObjectPropertyAssertion(hasColor, flags.get(count), colorOrange));
		// mainhue
		String newColorString = inputParts[17];
		if(newColors.containsKey(newColorString))
		{
			kb.addAxiom(new ObjectPropertyAssertion(hasPredominationColor, flags.get(count), newColors.get(newColorString)));
		}
		else
		{
			Individual newColor	= new Individual(getIRI(ontoColorClassName + newColorString));
			kb.addAxiom(new ClassAssertionAxiom(Color,newColor));
			newColors.put(newColorString, newColor);
			kb.addAxiom(new ObjectPropertyAssertion(hasPredominationColor, flags.get(count), newColor));
		}
		// circles
		kb.addAxiom(new DoubleDatatypePropertyAssertion(numberOfCircles, flags.get(count), Double.valueOf(inputParts[18])));
		// crosses
		kb.addAxiom(new DoubleDatatypePropertyAssertion(numberOfUprightCrosses, flags.get(count), Double.valueOf(inputParts[19])));
		// diagonal crosses
		kb.addAxiom(new DoubleDatatypePropertyAssertion(numberOfDiagonalCrosses, flags.get(count), Double.valueOf(inputParts[20])));
		// quarters
		kb.addAxiom(new DoubleDatatypePropertyAssertion(numberOfQuarteredSections, flags.get(count), Double.valueOf(inputParts[21])));
		// sunstars
		kb.addAxiom(new DoubleDatatypePropertyAssertion(numberOfSunOrStarSymbols, flags.get(count), Double.valueOf(inputParts[22])));
		// crescent
		kb.addAxiom(new BooleanDatatypePropertyAssertion(hasCrescentMoonSymbol, flags.get(count), Boolean.valueOf(inputParts[23])));
		// triangle
		kb.addAxiom(new BooleanDatatypePropertyAssertion(hasTriangle, flags.get(count), Boolean.valueOf(inputParts[24])));
		// icon
		kb.addAxiom(new BooleanDatatypePropertyAssertion(hasImageInanimate, flags.get(count), Boolean.valueOf(inputParts[25])));
		// animate
		kb.addAxiom(new BooleanDatatypePropertyAssertion(hasImageAnimate, flags.get(count), Boolean.valueOf(inputParts[26])));
		// text
		kb.addAxiom(new BooleanDatatypePropertyAssertion(hasText, flags.get(count), Boolean.valueOf(inputParts[27])));
		// topleft
		newColorString = inputParts[28];
		if(newColors.containsKey(newColorString))
		{
			kb.addAxiom(new ObjectPropertyAssertion(hasTopleftColor, flags.get(count), newColors.get(newColorString)));
		}
		else
		{
			Individual newColor	= new Individual(getIRI(ontoColorClassName + newColorString));
			kb.addAxiom(new ClassAssertionAxiom(Color,newColor));
			newColors.put(newColorString, newColor);
			kb.addAxiom(new ObjectPropertyAssertion(hasTopleftColor, flags.get(count), newColor));
		}
		// botright
		newColorString = inputParts[29];
		if(newColors.containsKey(newColorString))
		{
			kb.addAxiom(new ObjectPropertyAssertion(hasBottomRightColor, flags.get(count), newColors.get(newColorString)));
		}
		else
		{
			Individual newColor	= new Individual(getIRI(ontoColorClassName + newColorString));
			kb.addAxiom(new ClassAssertionAxiom(Color,newColor));
			newColors.put(newColorString, newColor);
			kb.addAxiom(new ObjectPropertyAssertion(hasBottomRightColor, flags.get(count), newColor));
		}
		
		
		/*;
		kb.addAxiom(new ClassAssertionAxiom(ReligionGroup,religionGroupOthers));
		kb.addAxiom(axiom)
		name.setName(inputParts[0]);
		landmass.setName(inputParts[1]);
		zone.setName(inputParts[2]);
		name.setArea(Double.valueOf(inputParts[3]));
		name.setPopulation(Double.valueOf(inputParts[4]));
		language.setName(inputParts[5]);
		religion.setName(inputParts[6]);
		bars 		= Double.valueOf(inputParts[7]);
		stripes 	= Double.valueOf(inputParts[8]);
		colours 	= Double.valueOf(inputParts[9]);
		red 		= inputParts[10].contains("1");
		green 		= inputParts[11].contains("1");
		blue 		= inputParts[12].contains("1");
		gold 		= inputParts[13].contains("1");
		white 		= inputParts[14].contains("1");
		black 		= inputParts[15].contains("1");
		orange 		= inputParts[16].contains("1");
		mainhue 	= Color.getColor(inputParts[17]);
		circles 	= Double.valueOf(inputParts[18]);
		crosses 	= Double.valueOf(inputParts[19]);
		diagCrosses = Double.valueOf(inputParts[20]);
		quarters 	= Double.valueOf(inputParts[21]);
		sunstars 	= Double.valueOf(inputParts[22]);
		crescent 	= inputParts[23].contains("1");
		triangle 	= inputParts[24].contains("1");
		icon 		= inputParts[25].contains("1");
		animate 	= inputParts[26].contains("1");
		text 		= inputParts[27].contains("1");
		topleft 	= Color.getColor(inputParts[28]);
		botright 	= Color.getColor(inputParts[29]);*/
		return true;
	}
	
	public static Individual getLandmass(int nrOfLandmass)
	{
		switch(nrOfLandmass)
		{
			case 1: return landmassNorthAmerica;
			case 2: return landmassSouthAmerica;
			case 3: return landmassEurope;
			case 4: return landmassAfrica;
			case 5: return landmassAsia;
			case 6: return landmassOceania;
		}	
		return null;
	}
	
	public static Individual getZone(int nrOfZone)
	{
		switch(nrOfZone)
		{
			case 1: return hemisphereNorthEast;
			case 2: return hemisphereSouthEast;
			case 3: return hemisphereSouthWest;
			case 4: return hemisphereNorthWest;
		}	
		return null;
	}
	
	public static Individual getLanguageGroup(int nrOfLG)
	{
	
		switch(nrOfLG)
		{
			case 1:  return languageGroupEnglish;
			case 2:  return languageGroupSpanish;
			case 3:  return languageGroupFrench;
			case 4:  return languageGroupGerman;
			case 5:  return languageGroupSlavic;
			case 6:  return languageGroupIndoEuropean;
			case 7:  return languageGroupChinese;
			case 8:  return languageGroupArabic;
			case 9:  return languageGroupJapTurkFinnMag;
			case 10: return languageGroupOthers;
			default: return languageGroupOthers;
		}	
	}
	
	public static Individual getReligionGroup(int nrOfRG)
	{
		switch(nrOfRG)
		{
			case 0: return religionGroupCatholic;
			case 1: return religionGroupOtherChristian;
			case 2: return religionGroupMuslim;
			case 3: return religionGroupBuddhist;
			case 4: return religionGroupHindu;
			case 5: return religionGroupEthnic;
			case 6: return religionGroupMarxist;
			case 7: return religionGroupOthers;
			default: return religionGroupOthers;
		}	
	}
	
	public static boolean readData(String filePath)
	{
		/*ArrayList<Flag> flagAL = null;
		Flag inputFlag;*/
		File file;
		FileReader fileReader;
		BufferedReader inputReader;
		String input;
		String[] inputParts;
		int count = 0;
		flags = new ArrayList<Individual>();
		
		try {
			file = new File(filePath);
			fileReader = new FileReader(file);	
			inputReader = new BufferedReader(fileReader);
			
			input = inputReader.readLine();
			while(input != null)
			{
				inputParts = input.split(dataSeparator);
				newFlag(inputParts, count);
				/*inputFlag = new Flag();
				inputFlag.convertFromStringArray(inputParts);*/
				count++;
				
				input = inputReader.readLine();
			}
		
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return true;
	}
	
	private static String getIRI(String name) {		
		return /*ontoIri*/"http://www.semanticweb.org/owlapi/ontologies/uniLpz/semWeb" + iriSeperator + name;
	}
	
	private static String getIRI(String name, String prefix) {		
		return prefix + iriSeperator + name;
	}
	
	private static String getIRI(String name, String prefix, String seperator) {		
		return prefix + seperator + name;
	}
	
	public static void main(String[] args) {
		Flags.createKB();
		System.out.println("done.");
	}	

}
