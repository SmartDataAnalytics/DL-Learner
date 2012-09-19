package org.dllearner.algorithm.tbsl.nlp;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** 
This class is part of the Java Tools (see http://mpii.de/yago-naga/javatools).
It is licensed under the Creative Commons Attribution License 
(see http://creativecommons.org/licenses/by/3.0) by 
the YAGO-NAGA team (see http://mpii.de/yago-naga).
  

  
 

  The PlingStemmer stems an English noun (plural or singular) to its singular
  form. It deals with "firemen"->"fireman", it knows Greek stuff like
  "appendices"->"appendix" and yes, it was a lot of work to compile these exceptions.
  Examples:
  <PRE>
      System.out.println(PlingStemmer.stem("boy"));
      ----> boy
      System.out.println(PlingStemmer.stem("boys"));
      ----> boy
      System.out.println(PlingStemmer.stem("biophysics"));
      ---->  biophysics
      System.out.println(PlingStemmer.stem("automata"));
      ----> automaton
      System.out.println(PlingStemmer.stem("genus"));
      ----> genus
      System.out.println(PlingStemmer.stem("emus"));
      ----> emu
  </PRE><P>

  There are a number of word forms that can either be plural or singular.
  Examples include "physics" (the science or the plural of "physic" (the
  medicine)), "quarters" (the housing or the plural of "quarter" (1/4))
  or "people" (the singular of "peoples" or the plural of "person"). In
  these cases, the stemmer assumes the word is a plural form and returns
  the singular form. The methods isPlural, isSingular and isPluralAndSingular
  can be used to differentiate the cases.<P>

  It cannot be guaranteed that the stemmer correctly stems a plural word
  or correctly ignores a singular word -- let alone that it treats an
  ambiguous word form in the way expected by the user.<P>
  
  The PlingStemmer uses material from <A HREF=http://wordnet.princeton.edu/>WordNet</A>.<P>
  It requires the class FinalSet from the <A HREF=http://www.mpii.mpg.de/~suchanek/downloads/javatools>
  Java Tools</A>.
*/
public class PlingStemmer {

  /** Tells whether a word form is plural. This method just checks whether the
   * stem method alters the word */
  public static boolean isPlural(String s) {
    return(!s.equals(stem(s)));
  }

  /** Tells whether a word form is singular. Note that a word can be both plural and singular */
  public static boolean isSingular(String s) {
    return(singAndPlur.contains(s.toLowerCase()) || !isPlural(s));
  }  

  /** Tells whether a word form is the singular form of one word and at
   * the same time the plural form of another.*/
  public static boolean isSingularAndPlural(String s) {
    return(singAndPlur.contains(s.toLowerCase()));
  }  
  
  /** Cuts a suffix from a string (that is the number of chars given by the suffix) */
  public static String cut(String s, String suffix) {
    return(s.substring(0,s.length()-suffix.length()));
  }

  /** Returns true if a word is probably not Latin */
  public static boolean noLatin(String s) {
    return(s.indexOf('h')>0 || s.indexOf('j')>0 || s.indexOf('k')>0 ||
           s.indexOf('w')>0 || s.indexOf('y')>0 || s.indexOf('z')>0 ||
           s.indexOf("ou")>0 || s.indexOf("sh")>0 || s.indexOf("ch")>0 ||
           s.endsWith("aus"));
  }

  /** Returns true if a word is probably Greek */
  private static boolean greek(String s) {
    return(s.indexOf("ph")>0 || s.indexOf('y')>0 && s.endsWith("nges"));
  }

  /** Stems an English noun */
  public static String stem(String s) {
    String stem = s;

     // Handle irregular ones
     String irreg=irregular.get(s);
     if(irreg!=null) return(stem=irreg);

     // -on to -a
     if(categoryON_A.contains(s)) return(stem=cut(s,"a")+"on");

     // -um to -a
     if(categoryUM_A.contains(s)) return(stem=cut(s,"a")+"um");

     // -x to -ices
     if(categoryIX_ICES.contains(s)) return(stem=cut(s,"ices")+"ix");

     // -o to -i
     if(categoryO_I.contains(s)) return(stem=cut(s,"i")+"o");

     // -se to ses
     if(categorySE_SES.contains(s)) return(stem=cut(s,"s"));

     // -is to -es
     if(categoryIS_ES.contains(s) || s.endsWith("theses")) return(stem=cut(s,"es")+"is");

     // -us to -i
     if(categoryUS_I.contains(s)) return(stem=cut(s,"i")+"us");
     //Wrong plural
     if(s.endsWith("uses") && (categoryUS_I.contains(cut(s,"uses")+"i") ||
                               s.equals("genuses") || s.equals("corpuses"))) return(stem=cut(s,"es"));

     // -ex to -ices
     if(categoryEX_ICES.contains(s)) return(stem=cut(s,"ices")+"ex");

     // Words that do not inflect in the plural
     if(s.endsWith("ois") || s.endsWith("itis") || category00.contains(s) || categoryICS.contains(s)) return(stem=s);

     // -en to -ina
     // No other common words end in -ina
     if(s.endsWith("ina")) return(stem=cut(s,"en"));

     // -a to -ae
     // No other common words end in -ae
     if(s.endsWith("ae")) return(stem=cut(s,"e"));

     // -a to -ata
     // No other common words end in -ata
     if(s.endsWith("ata")) return(stem=cut(s,"ta"));

     // trix to -trices
     // No common word ends with -trice(s)
     if(s.endsWith("trices")) return(stem=cut(s,"trices")+"trix");

     // -us to -us
     //No other common word ends in -us, except for false plurals of French words
     //Catch words that are not latin or known to end in -u
     if(s.endsWith("us") && !s.endsWith("eaus") && !s.endsWith("ieus") && !noLatin(s)
        && !categoryU_US.contains(s)) return(stem=s);

     // -tooth to -teeth
     // -goose to -geese
     // -foot to -feet
     // -zoon to -zoa
     //No other common words end with the indicated suffixes
     if(s.endsWith("teeth")) return(stem=cut(s,"teeth")+"tooth");
     if(s.endsWith("geese")) return(stem=cut(s,"geese")+"goose");
     if(s.endsWith("feet")) return(stem=cut(s,"feet")+"foot");
     if(s.endsWith("zoa")) return(stem=cut(s,"zoa")+"zoon");

     // -eau to -eaux
     //No other common words end in eaux
     if(s.endsWith("eaux")) return(stem=cut(s,"x"));

     // -ieu to -ieux
     //No other common words end in ieux
     if(s.endsWith("ieux")) return(stem=cut(s,"x"));

     // -nx to -nges
     // Pay attention not to kill words ending in -nge with plural -nges
     // Take only Greek words (works fine, only a handfull of exceptions)
     if(s.endsWith("nges") && greek(s)) return(stem=cut(s,"nges")+"nx");

     // -[sc]h to -[sc]hes
     //No other common word ends with "shes", "ches" or "she(s)"
     //Quite a lot end with "che(s)", filter them out
     if(s.endsWith("shes") || s.endsWith("ches") && !categoryCHE_CHES.contains(s)) return(stem=cut(s,"es"));

     // -ss to -sses
     // No other common singular word ends with "sses"
     // Filter out those ending in "sse(s)"
     if(s.endsWith("sses") && !categorySSE_SSES.contains(s) && !s.endsWith("mousses")) return(stem=cut(s,"es"));

     // -x to -xes
     // No other common word ends with "xe(s)" except for "axe"
     if(s.endsWith("xes") && !s.equals("axes")) return(stem=cut(s,"es"));

     // -[nlw]ife to -[nlw]ives
     //No other common word ends with "[nlw]ive(s)" except for olive
     if(s.endsWith("nives") || s.endsWith("lives") && !s.endsWith("olives") ||
        s.endsWith("wives")) return(stem=cut(s,"ves")+"fe");

     // -[aeo]lf to -ves  exceptions: valve, solve
     // -[^d]eaf to -ves  exceptions: heave, weave
     // -arf to -ves      no exception
     if(s.endsWith("alves") && !s.endsWith("valves") ||
        s.endsWith("olves") && !s.endsWith("solves") ||
        s.endsWith("eaves") && !s.endsWith("heaves") && !s.endsWith("weaves") ||
        s.endsWith("arves") ) return(stem=cut(s,"ves")+"f");

     // -y to -ies
     // -ies is very uncommon as a singular suffix
     // but -ie is quite common, filter them out
     if(s.endsWith("ies") && !categoryIE_IES.contains(s)) return(stem=cut(s,"ies")+"y");

     // -o to -oes
     // Some words end with -oe, so don't kill the "e"
     if(s.endsWith("oes") && !categoryOE_OES.contains(s)) return(stem=cut(s,"es"));

     // -s to -ses
     // -z to -zes
     // no words end with "-ses" or "-zes" in singular
     if(s.endsWith("ses") || s.endsWith("zes") ) return(stem=cut(s,"es"));

     // - to -s
     if(s.endsWith("s") && !s.endsWith("ss") && !s.endsWith("is")) return(stem=cut(s,"s"));

     return stem;
  }

  /** Words that end in "-se" in their plural forms (like "nurse" etc.)*/
  public static Set<String> categorySE_SES= new HashSet<String>();
  static {
      
      categorySE_SES.add("nurses");
      categorySE_SES.add("cruises");
      categorySE_SES.add("premises");
      categorySE_SES.add("houses");
  }

  /** Words that do not have a distinct plural form (like "atlas" etc.)*/
  public static Set<String> category00 = new HashSet<String>();
  static { 
      category00.add("alias");
      category00.add("asbestos");
      category00.add("atlas");
      category00.add("barracks");
      category00.add("bathos");
      category00.add("bias");
      category00.add("breeches");
      category00.add("britches");
      category00.add("canvas");
      category00.add("chaos");
      category00.add("clippers");
      category00.add("contretemps");
      category00.add("corps");
      category00.add("cosmos");
      category00.add("crossroads");
      category00.add("diabetes");
      category00.add("ethos");
      category00.add("gallows");
      category00.add("gas");
      category00.add("graffiti");
      category00.add("headquarters");
      category00.add("herpes");
      category00.add("high-jinks");
      category00.add("innings");
      category00.add("jackanapes");
      category00.add("lens");
      category00.add("means");
      category00.add("measles");
      category00.add("mews");
      category00.add("mumps");
      category00.add("news");
      category00.add("pathos");
      category00.add("pincers");
      category00.add("pliers");   
      category00.add("proceedings");
      category00.add("rabies");
      category00.add("rhinoceros");
      category00.add("sassafras");
      category00.add("scissors");
      category00.add("series");
      category00.add("shears");
      category00.add("species");
      category00.add("tuna");
    }

  /** Words that change from "-um" to "-a" (like "curriculum" etc.), listed in their plural forms*/
  public static Set<String> categoryUM_A=new HashSet<String>();
  static {
       categoryUM_A.add("addenda");
       categoryUM_A.add("agenda");
       categoryUM_A.add("aquaria");
       categoryUM_A.add("bacteria");
       categoryUM_A.add("candelabra");
       categoryUM_A.add("compendia");
       categoryUM_A.add("consortia");
       categoryUM_A.add("crania");
       categoryUM_A.add("curricula");
       categoryUM_A.add("data");
       categoryUM_A.add("desiderata");
       categoryUM_A.add("dicta");
       categoryUM_A.add("emporia");
       categoryUM_A.add("enconia");
       categoryUM_A.add("errata");
       categoryUM_A.add("extrema");
       categoryUM_A.add("gymnasia");
       categoryUM_A.add("honoraria");
       categoryUM_A.add("interregna");
       categoryUM_A.add("lustra");
       categoryUM_A.add("maxima");
       categoryUM_A.add("media");
       categoryUM_A.add("memoranda");
       categoryUM_A.add("millenia");
       categoryUM_A.add("minima");
       categoryUM_A.add("momenta");
       categoryUM_A.add("optima");
       categoryUM_A.add("ova");
       categoryUM_A.add("phyla");
       categoryUM_A.add("quanta");
       categoryUM_A.add("rostra");
       categoryUM_A.add("spectra");
       categoryUM_A.add("specula");
       categoryUM_A.add("stadia");
       categoryUM_A.add("strata");
       categoryUM_A.add("symposia");
       categoryUM_A.add("trapezia");
       categoryUM_A.add("ultimata");
       categoryUM_A.add("vacua");
       categoryUM_A.add("vela");
}

  /** Words that change from "-on" to "-a" (like "phenomenon" etc.), listed in their plural forms*/
  public static Set<String> categoryON_A=new HashSet<String>();
  static {
      categoryON_A.add("aphelia");
      categoryON_A.add("asyndeta");
      categoryON_A.add("automata");
      categoryON_A.add("criteria");
      categoryON_A.add("hyperbata");
      categoryON_A.add("noumena");
      categoryON_A.add("organa");
       categoryON_A.add("perihelia");
       categoryON_A.add("phenomena");
       categoryON_A.add("prolegomena");
  }

  /** Words that change from "-o" to "-i" (like "libretto" etc.), listed in their plural forms*/
  public static Set<String> categoryO_I=new HashSet<String>();
   static {       categoryO_I.add("alti");
          categoryO_I.add("bassi");
          categoryO_I.add("canti");
          categoryO_I.add("contralti");
          categoryO_I.add("crescendi");
          categoryO_I.add("libretti");
          categoryO_I.add("soli");
          categoryO_I.add("soprani");
          categoryO_I.add("tempi");
          categoryO_I.add("virtuosi");
   }

  /** Words that change from "-us" to "-i" (like "fungus" etc.), listed in their plural forms*/
  public static Set<String> categoryUS_I=new HashSet<String>();
   static {
       categoryUS_I.add("alumni");
      categoryUS_I.add("bacilli");
      categoryUS_I.add("cacti");
      categoryUS_I.add("foci");
      categoryUS_I.add("fungi");
      categoryUS_I.add("genii");
      categoryUS_I.add("hippopotami");
      categoryUS_I.add("incubi");
      categoryUS_I.add("nimbi");
      categoryUS_I.add("nuclei");
      categoryUS_I.add("nucleoli");
      categoryUS_I.add("octopi");
      categoryUS_I.add("radii");
      categoryUS_I.add("stimuli");
      categoryUS_I.add("styli");
      categoryUS_I.add("succubi");
      categoryUS_I.add("syllabi");
      categoryUS_I.add("termini");
      categoryUS_I.add("tori");
      categoryUS_I.add("umbilici");
      categoryUS_I.add("uteri");
}

  /** Words that change from "-ix" to "-ices" (like "appendix" etc.), listed in their plural forms*/
  public static Set<String> categoryIX_ICES=new HashSet<String>();
static {
    categoryIX_ICES.add("appendices");
    categoryIX_ICES.add("cervices");
}

  /** Words that change from "-is" to "-es" (like "axis" etc.), listed in their plural forms*/
  public static Set<String> categoryIS_ES=new HashSet<String>();
  static {
    // plus everybody ending in theses
       category00.add("analyses");
       category00.add("axes");
       category00.add("bases");
       category00.add("crises");
       category00.add("diagnoses");
       category00.add("ellipses");
       category00.add("emphases");
       category00.add("neuroses");
       category00.add("oases");
       category00.add("paralyses");
       category00.add("synopses");
  }

  /** Words that change from "-oe" to "-oes" (like "toe" etc.), listed in their plural forms*/
  public static Set<String> categoryOE_OES=new HashSet<String>();
  static {
       categoryOE_OES.add("aloes");
       categoryOE_OES.add("backhoes");
       categoryOE_OES.add("beroes");
       categoryOE_OES.add("canoes");
       categoryOE_OES.add("chigoes");
       categoryOE_OES.add("cohoes");
       categoryOE_OES.add("does");
       categoryOE_OES.add("felloes");
       categoryOE_OES.add("floes");
       categoryOE_OES.add("foes");
       categoryOE_OES.add("gumshoes");
       categoryOE_OES.add("hammertoes");
       categoryOE_OES.add("hoes");
       categoryOE_OES.add("hoopoes");
       categoryOE_OES.add("horseshoes");
       categoryOE_OES.add("leucothoes");
       categoryOE_OES.add("mahoes");
       categoryOE_OES.add("mistletoes");
       categoryOE_OES.add("oboes");
       categoryOE_OES.add("overshoes");
       categoryOE_OES.add("pahoehoes");
       categoryOE_OES.add("pekoes");
       categoryOE_OES.add("roes");
       categoryOE_OES.add("shoes");
       categoryOE_OES.add("sloes");
       categoryOE_OES.add("snowshoes");
       categoryOE_OES.add("throes");
       categoryOE_OES.add("tic-tac-toes");
       categoryOE_OES.add("tick-tack-toes");
       categoryOE_OES.add("ticktacktoes");
       categoryOE_OES.add("tiptoes");
       categoryOE_OES.add("tit-tat-toes");
       categoryOE_OES.add("toes");
       categoryOE_OES.add("toetoes");
       categoryOE_OES.add("tuckahoes");
       categoryOE_OES.add("woes");
  }

  /** Words that change from "-ex" to "-ices" (like "index" etc.), listed in their plural forms*/
  public static Set<String> categoryEX_ICES=new HashSet<String>();
  static {
      categoryEX_ICES.add("apices");
      categoryEX_ICES.add("codices");
      categoryEX_ICES.add("cortices");
      categoryEX_ICES.add("indices");
      categoryEX_ICES.add("latices");
      categoryEX_ICES.add("murices");
      categoryEX_ICES.add("pontifices");
      categoryEX_ICES.add("silices");
      categoryEX_ICES.add("simplices");
      categoryEX_ICES.add("vertices");
      categoryEX_ICES.add("vortices");
  }

  /** Words that change from "-u" to "-us" (like "emu" etc.), listed in their plural forms*/
  public static Set<String> categoryU_US=new HashSet<String>();
  static {
      categoryU_US.add("apercus");
      categoryU_US.add("barbus");
      categoryU_US.add("cornus");
      categoryU_US.add("ecrus");
      categoryU_US.add("emus");
      categoryU_US.add("fondus");
      categoryU_US.add("gnus");
      categoryU_US.add("iglus");
      categoryU_US.add("mus");
      categoryU_US.add("nandus");
      categoryU_US.add("napus");
      categoryU_US.add("poilus");
      categoryU_US.add("quipus");
      categoryU_US.add("snafus");
      categoryU_US.add("tabus");
      categoryU_US.add("tamandus");
      categoryU_US.add("tatus");
      categoryU_US.add("timucus");
      categoryU_US.add("tiramisus");
      categoryU_US.add("tofus");
      categoryU_US.add("tutus");
  }

  /** Words that change from "-sse" to "-sses" (like "finesse" etc.), listed in their plural forms*/
  public static Set<String> categorySSE_SSES=new HashSet<String>();
  static {  //plus those ending in mousse
       categorySSE_SSES.add("bouillabaisses");
       categorySSE_SSES.add("coulisses");
       categorySSE_SSES.add("crevasses");
       categorySSE_SSES.add("crosses");
       categorySSE_SSES.add("cuisses");
       categorySSE_SSES.add("demitasses");
       categorySSE_SSES.add("ecrevisses");
       categorySSE_SSES.add("fesses");
       categorySSE_SSES.add("finesses");
       categorySSE_SSES.add("fosses");
       categorySSE_SSES.add("impasses");
       categorySSE_SSES.add("lacrosses");
       categorySSE_SSES.add("largesses");
       categorySSE_SSES.add("masses");
       categorySSE_SSES.add("noblesses");
       categorySSE_SSES.add("palliasses");
       categorySSE_SSES.add("pelisses");
       categorySSE_SSES.add("politesses");
       categorySSE_SSES.add("posses");
       categorySSE_SSES.add("tasses");
       categorySSE_SSES.add("wrasses");
  }

  /** Words that change from "-che" to "-ches" (like "brioche" etc.), listed in their plural forms*/
  public static Set<String> categoryCHE_CHES=new HashSet<String>();
  static {
       categoryCHE_CHES.add("adrenarches");
       categoryCHE_CHES.add("attaches");
       categoryCHE_CHES.add("avalanches");
       categoryCHE_CHES.add("barouches");
       categoryCHE_CHES.add("brioches");
       categoryCHE_CHES.add("caches");
       categoryCHE_CHES.add("caleches");
       categoryCHE_CHES.add("caroches");
       categoryCHE_CHES.add("cartouches");
       categoryCHE_CHES.add("cliches");
       categoryCHE_CHES.add("cloches");
       categoryCHE_CHES.add("creches");
       categoryCHE_CHES.add("demarches");
       categoryCHE_CHES.add("douches");
       categoryCHE_CHES.add("gouaches");
       categoryCHE_CHES.add("guilloches");
       categoryCHE_CHES.add("headaches");
       categoryCHE_CHES.add("heartaches");
       categoryCHE_CHES.add("huaraches");
       categoryCHE_CHES.add("menarches");
       categoryCHE_CHES.add("microfiches");
       categoryCHE_CHES.add("moustaches");
       categoryCHE_CHES.add("mustaches");
       categoryCHE_CHES.add("niches");
       categoryCHE_CHES.add("panaches");
       categoryCHE_CHES.add("panoches");
       categoryCHE_CHES.add("pastiches");
       categoryCHE_CHES.add("penuches");
       categoryCHE_CHES.add("pinches");
       categoryCHE_CHES.add("postiches");
       categoryCHE_CHES.add("psyches");
       categoryCHE_CHES.add("quiches");
       categoryCHE_CHES.add("schottisches");
       categoryCHE_CHES.add("seiches");
       categoryCHE_CHES.add("soutaches");
       categoryCHE_CHES.add("synecdoches");
       categoryCHE_CHES.add("thelarches");
       categoryCHE_CHES.add("troches");
  }

  /** Words that end with "-ics" and do not exist as nouns without the 's' (like "aerobics" etc.)*/
  public static Set<String> categoryICS=new HashSet<String>();
  static {
       categoryICS.add("aerobatics");
       categoryICS.add("aerobics");
       categoryICS.add("aerodynamics");
       categoryICS.add("aeromechanics");
       categoryICS.add("aeronautics");
       categoryICS.add("alphanumerics");
       categoryICS.add("animatronics");
       categoryICS.add("apologetics");
       categoryICS.add("architectonics");
       categoryICS.add("astrodynamics");
       categoryICS.add("astronautics");
       categoryICS.add("astrophysics");
       categoryICS.add("athletics");
       categoryICS.add("atmospherics");
       categoryICS.add("autogenics");
       categoryICS.add("avionics");
       categoryICS.add("ballistics");
       categoryICS.add("bibliotics");
       categoryICS.add("bioethics");
       categoryICS.add("biometrics");
       categoryICS.add("bionics");
       categoryICS.add("bionomics");
       categoryICS.add("biophysics");
       categoryICS.add("biosystematics");
       categoryICS.add("cacogenics");
       categoryICS.add("calisthenics");
       categoryICS.add("callisthenics");
       categoryICS.add("catoptrics");
       categoryICS.add("civics");
       categoryICS.add("cladistics");
       categoryICS.add("cryogenics");
       categoryICS.add("cryonics");
       categoryICS.add("cryptanalytics");
       categoryICS.add("cybernetics");
       categoryICS.add("cytoarchitectonics");
       categoryICS.add("cytogenetics");
       categoryICS.add("diagnostics");
       categoryICS.add("dietetics");
       categoryICS.add("dramatics");
       categoryICS.add("dysgenics");
       categoryICS.add("econometrics");
       categoryICS.add("economics");
       categoryICS.add("electromagnetics");
       categoryICS.add("electronics");
       categoryICS.add("electrostatics");
       categoryICS.add("endodontics");
       categoryICS.add("enterics");
       categoryICS.add("ergonomics");
       categoryICS.add("eugenics");
       categoryICS.add("eurhythmics");
       categoryICS.add("eurythmics");
       categoryICS.add("exodontics");
       categoryICS.add("fibreoptics");
       categoryICS.add("futuristics");
       categoryICS.add("genetics");
       categoryICS.add("genomics");
       categoryICS.add("geographics");
       categoryICS.add("geophysics");
       categoryICS.add("geopolitics");
       categoryICS.add("geriatrics");
       categoryICS.add("glyptics");
       categoryICS.add("graphics");
       categoryICS.add("gymnastics");
       categoryICS.add("hermeneutics");
       categoryICS.add("histrionics");
       categoryICS.add("homiletics");
       categoryICS.add("hydraulics");
       categoryICS.add("hydrodynamics");
       categoryICS.add("hydrokinetics");
       categoryICS.add("hydroponics");
       categoryICS.add("hydrostatics");
       categoryICS.add("hygienics");
       categoryICS.add("informatics");
       categoryICS.add("kinematics");
       categoryICS.add("kinesthetics");
       categoryICS.add("kinetics");
       categoryICS.add("lexicostatistics");
       categoryICS.add("linguistics");
       categoryICS.add("lithoglyptics");
       categoryICS.add("liturgics");
       categoryICS.add("logistics");
       categoryICS.add("macrobiotics");
       categoryICS.add("macroeconomics");
       categoryICS.add("magnetics");
       categoryICS.add("magnetohydrodynamics");
       categoryICS.add("mathematics");
       categoryICS.add("metamathematics");
       categoryICS.add("metaphysics");
       categoryICS.add("microeconomics");
       categoryICS.add("microelectronics");
       categoryICS.add("mnemonics");
       categoryICS.add("morphophonemics");
       categoryICS.add("neuroethics");
       categoryICS.add("neurolinguistics");
       categoryICS.add("nucleonics");
       categoryICS.add("numismatics");
       categoryICS.add("obstetrics");
       categoryICS.add("onomastics");
       categoryICS.add("orthodontics");
       categoryICS.add("orthopaedics");
       categoryICS.add("orthopedics");
       categoryICS.add("orthoptics");
       categoryICS.add("paediatrics");
       categoryICS.add("patristics");
       categoryICS.add("patristics");
       categoryICS.add("pedagogics");
       categoryICS.add("pediatrics");
       categoryICS.add("periodontics");
       categoryICS.add("pharmaceutics");
       categoryICS.add("pharmacogenetics");
       categoryICS.add("pharmacokinetics");
       categoryICS.add("phonemics");
       categoryICS.add("phonetics");
       categoryICS.add("phonics");
       categoryICS.add("photomechanics");
       categoryICS.add("physiatrics");
       categoryICS.add("pneumatics");
       categoryICS.add("poetics");
       categoryICS.add("politics");
       categoryICS.add("pragmatics");
       categoryICS.add("prosthetics");
       categoryICS.add("prosthodontics");
       categoryICS.add("proteomics");
       categoryICS.add("proxemics");
       categoryICS.add("psycholinguistics");
       categoryICS.add("psychometrics");
       categoryICS.add("psychonomics");
       categoryICS.add("psychophysics");
       categoryICS.add("psychotherapeutics");
       categoryICS.add("robotics");
       categoryICS.add("semantics");
       categoryICS.add("semiotics");
       categoryICS.add("semitropics");
       categoryICS.add("sociolinguistics");
       categoryICS.add("stemmatics");
       categoryICS.add("strategics");
       categoryICS.add("subtropics");
       categoryICS.add("systematics");
       categoryICS.add("tectonics");
       categoryICS.add("telerobotics");
       categoryICS.add("therapeutics");
       categoryICS.add("thermionics");
       categoryICS.add("thermodynamics");
       categoryICS.add("thermostatics");
  }

  /** Words that change from "-ie" to "-ies" (like "auntie" etc.), listed in their plural forms*/
  public static Set<String> categoryIE_IES=new HashSet<String>();
  static {
       categoryIE_IES.add("aeries");
       categoryIE_IES.add("anomies");
       categoryIE_IES.add("aunties");
       categoryIE_IES.add("baddies");
       categoryIE_IES.add("beanies");
       categoryIE_IES.add("birdies");
       categoryIE_IES.add("boccies");
       categoryIE_IES.add("bogies");
       categoryIE_IES.add("bolshies");
       categoryIE_IES.add("bombies");
       categoryIE_IES.add("bonhomies");
       categoryIE_IES.add("bonxies");
       categoryIE_IES.add("booboisies");
       categoryIE_IES.add("boogies");
       categoryIE_IES.add("boogie-woogies");
       categoryIE_IES.add("bookies");
       categoryIE_IES.add("booties");
       categoryIE_IES.add("bosies");
       categoryIE_IES.add("bourgeoisies");
       categoryIE_IES.add("brasseries");
       categoryIE_IES.add("brassies");
       categoryIE_IES.add("brownies");
       categoryIE_IES.add("budgies");
       categoryIE_IES.add("byrnies");
       categoryIE_IES.add("caddies");
       categoryIE_IES.add("calories");
       categoryIE_IES.add("camaraderies");
       categoryIE_IES.add("capercaillies");
       categoryIE_IES.add("capercailzies");
       categoryIE_IES.add("cassies");
       categoryIE_IES.add("catties");
       categoryIE_IES.add("causeries");
       categoryIE_IES.add("charcuteries");
       categoryIE_IES.add("chinoiseries");
       categoryIE_IES.add("collies");
       categoryIE_IES.add("commies");
       categoryIE_IES.add("cookies");
       categoryIE_IES.add("coolies");
       categoryIE_IES.add("coonties");
       categoryIE_IES.add("cooties");
       categoryIE_IES.add("corries");
       categoryIE_IES.add("coteries");
       categoryIE_IES.add("cowpies");
       categoryIE_IES.add("cowries");
       categoryIE_IES.add("cozies");
       categoryIE_IES.add("crappies");
       categoryIE_IES.add("crossties");
       categoryIE_IES.add("curies");
       categoryIE_IES.add("dachsies");
       categoryIE_IES.add("darkies");
       categoryIE_IES.add("dassies");
       categoryIE_IES.add("dearies");
       categoryIE_IES.add("dickies");
       categoryIE_IES.add("dies");
       categoryIE_IES.add("dixies");
       categoryIE_IES.add("doggies");
       categoryIE_IES.add("dogies");
       categoryIE_IES.add("dominies");
       categoryIE_IES.add("dovekies");
       categoryIE_IES.add("eyries");
       categoryIE_IES.add("faeries");
       categoryIE_IES.add("falsies");
       categoryIE_IES.add("floozies");
       categoryIE_IES.add("folies");
       categoryIE_IES.add("foodies");
       categoryIE_IES.add("freebies");
       categoryIE_IES.add("gaucheries");
       categoryIE_IES.add("gendarmeries");
       categoryIE_IES.add("genies");
       categoryIE_IES.add("ghillies");
       categoryIE_IES.add("gillies");
       categoryIE_IES.add("goalies");
       categoryIE_IES.add("goonies");
       categoryIE_IES.add("grannies");
       categoryIE_IES.add("grotesqueries");
       categoryIE_IES.add("groupies");
       categoryIE_IES.add("hankies");
       categoryIE_IES.add("hippies");
       categoryIE_IES.add("hoagies");
       categoryIE_IES.add("honkies");
       categoryIE_IES.add("hymies");
       categoryIE_IES.add("indies");
       categoryIE_IES.add("junkies");
       categoryIE_IES.add("kelpies");
       categoryIE_IES.add("kilocalories");
       categoryIE_IES.add("knobkerries");
       categoryIE_IES.add("koppies");
       categoryIE_IES.add("kylies");
       categoryIE_IES.add("laddies");
       categoryIE_IES.add("lassies");
       categoryIE_IES.add("lies");
       categoryIE_IES.add("lingeries");
       categoryIE_IES.add("magpies");
       categoryIE_IES.add("magpies");
       categoryIE_IES.add("marqueteries");
       categoryIE_IES.add("mashies");
       categoryIE_IES.add("mealies");
       categoryIE_IES.add("meanies");
       categoryIE_IES.add("menageries");
       categoryIE_IES.add("millicuries");
       categoryIE_IES.add("mollies");
       categoryIE_IES.add("facts1");
       categoryIE_IES.add("moxies");
       categoryIE_IES.add("neckties");
       categoryIE_IES.add("newbies");
       categoryIE_IES.add("nighties");
       categoryIE_IES.add("nookies");
       categoryIE_IES.add("oldies");
       categoryIE_IES.add("organdies");
       categoryIE_IES.add("panties");
       categoryIE_IES.add("parqueteries");
       categoryIE_IES.add("passementeries");
       categoryIE_IES.add("patisseries");
       categoryIE_IES.add("pies");
       categoryIE_IES.add("pinkies");
       categoryIE_IES.add("pixies");
       categoryIE_IES.add("porkpies");
       categoryIE_IES.add("potpies");
       categoryIE_IES.add("prairies");
       categoryIE_IES.add("preemies");
       categoryIE_IES.add("premies");
       categoryIE_IES.add("punkies");
       categoryIE_IES.add("pyxies");
       categoryIE_IES.add("quickies");
       categoryIE_IES.add("ramies");
       categoryIE_IES.add("reveries");
       categoryIE_IES.add("rookies");
       categoryIE_IES.add("rotisseries");
       categoryIE_IES.add("scrapies");
       categoryIE_IES.add("sharpies");
       categoryIE_IES.add("smoothies");
       categoryIE_IES.add("softies");
       categoryIE_IES.add("stoolies");
       categoryIE_IES.add("stymies");
       categoryIE_IES.add("swaggies");
       categoryIE_IES.add("sweeties");
       categoryIE_IES.add("talkies");
       categoryIE_IES.add("techies");
       categoryIE_IES.add("ties");
       categoryIE_IES.add("tooshies");
       categoryIE_IES.add("toughies");
       categoryIE_IES.add("townies");
       categoryIE_IES.add("veggies");
       categoryIE_IES.add("walkie-talkies");
       categoryIE_IES.add("wedgies");
       categoryIE_IES.add("weenies");
       categoryIE_IES.add("weirdies");
       categoryIE_IES.add("yardies");
       categoryIE_IES.add("yuppies");
       categoryIE_IES.add("zombies");
  }

  /** Maps irregular Germanic English plural nouns to their singular form */
  public static Map<String,String> irregular=new HashMap<String,String>();
  static {
       irregular.put("beefs","beef");
       irregular.put("beeves","beef");
       irregular.put("brethren","brother");
       irregular.put("busses","bus");
       irregular.put("cattle","cattlebeast");
       irregular.put("children","child");
       irregular.put("corpora","corpus");
       irregular.put("ephemerides","ephemeris");
       irregular.put("firemen","fireman");
       irregular.put("genera","genus");
       irregular.put("genies","genie");
       irregular.put("genii","genie");
       irregular.put("kine","cow");
       irregular.put("lice","louse");
       irregular.put("men","man");
       irregular.put("mice","mouse");
       irregular.put("mongooses","mongoose");
       irregular.put("monies","money");
       irregular.put("mythoi","mythos");
       irregular.put("octopodes","octopus");
       irregular.put("octopuses","octopus");
       irregular.put("oxen","ox");
       irregular.put("people","person");
       irregular.put("soliloquies","soliloquy");
       irregular.put("throes","throes");
       irregular.put("trilbys","trilby");
       irregular.put("women","woman");
  }

  /** Contains word forms that can either be plural or singular */
  public static Set<String> singAndPlur=new HashSet<String>();
  static {
           singAndPlur.add("acoustics");
           singAndPlur.add("aestetics");
           singAndPlur.add("aquatics");
           singAndPlur.add("basics");
           singAndPlur.add("ceramics");
           singAndPlur.add("classics");
           singAndPlur.add("cosmetics");
           singAndPlur.add("dermatoglyphics");
           singAndPlur.add("dialectics");
           singAndPlur.add("dynamics");
           singAndPlur.add("esthetics");
           singAndPlur.add("ethics");
           singAndPlur.add("harmonics");
           singAndPlur.add("heroics");
           singAndPlur.add("isometrics");
           singAndPlur.add("mechanics");
           singAndPlur.add("metrics");
           singAndPlur.add("statistics");
           singAndPlur.add("optic");
           singAndPlur.add("people");
           singAndPlur.add("physics");
           singAndPlur.add("polemics");
           singAndPlur.add("premises");
           singAndPlur.add("propaedeutics");
           singAndPlur.add("pyrotechnics");
           singAndPlur.add("quadratics");
           singAndPlur.add("quarters");
           singAndPlur.add("statistics");
           singAndPlur.add("tactics");
           singAndPlur.add("tropics");
  }
  
  /** Test routine */
  public static void main(String[] argv) throws Exception {    
    System.out.println("Enter an English word in plural form and press ENTER");
    BufferedReader in=new BufferedReader(new InputStreamReader(System.in));
    while(true) {
      String w=in.readLine();
      if(w.length()==0) break;
      if(isPlural(w)) System.out.println("This word is plural");
      if(isSingular(w)) System.out.println("This word is singular");
      System.out.println("Stemmed to singular: "+stem(w));
    }
  }
}
