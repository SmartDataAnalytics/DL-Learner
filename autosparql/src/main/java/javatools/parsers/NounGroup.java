package javatools.parsers;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javatools.administrative.D;
import javatools.datatypes.FinalSet;

/** 
This class is part of the Java Tools (see http://mpii.de/yago-naga/javatools).
It is licensed under the Creative Commons Attribution License 
(see http://creativecommons.org/licenses/by/3.0) by 
the YAGO-NAGA team (see http://mpii.de/yago-naga).
  

  
 

The class NounGroup splits a noun group (given by a String) into its
modifiers and its head.<BR>
Example:
<PRE>
     System.out.println(new NounGroup("the United States of America").description());
     ->
      NounGroup:
        Original: the_United_States_of_America
        Determiner: the
        Head: State
        Plural: true
        preModifiers: United
        Adjective: 
        Preposition: of
        postModifier:
          NounGroup:
            Original: America
            Determiner:
            Head: America
            Plural: false
            preModifiers:
            Preposition:
            postModifier:
</PRE>
*/
public class NounGroup {

  /** Defines just one function from a String to a boolean */
  public interface String2Boolean {
    /** Function from a String to a boolean */
    boolean apply(String s);
  }

  /** Tells whether a word is an adjective (currently by a simple heuristics */
  public static String2Boolean isAdjective=new String2Boolean() {
     public boolean apply(String s) {
       return(s.length()>0 && Character.isLowerCase(s.charAt(0)) &&
              (s.endsWith("al") || s.endsWith("ed") || s.endsWith("ing")));
     }
  };

  /** Contains determiners*/
  public static final Set<String> determiners=new FinalSet<String>(
        "the",
        "a",
        "an",
        "this",
        "these",
        "those"
        );

  /** Holds prepositions (like "of" etc.) */
  public static final FinalSet<String> prepositions=new FinalSet<String>(
        ",",
        "at",
        "about",
        "and",
        "by",
        "for",
        "from",
        "in",
        "of",
        "on",
        "to",
        "with",
        "who",
        "-",
        "\u2248"
  );

  /** Holds the original noun group */
  protected String original;

  /** Holds the adjective */
  protected String adjective;

  /** Holds the preposition */
  protected String preposition;

  /** Holds the noun group after the preposition */
  protected NounGroup postModifier;

  /** Holds the head of the noun group */
  protected String head;

  /** Holds the modifiers before the head  */
  protected String preModifier;

  /** Holds the determiner (if any) */
  protected String determiner;

  /** Returns the adjective. */
  public String adjective() {
    return adjective;
  }

  /**Returns the determiner. */
  public String determiner() {
    return determiner;
  }

  /** Returns the head (lowercased singular). */
  public String head() {
    return head;
  }

  /**Returns the original. */
  public String original() {
    return original;
  }

  /** Returns the postModifier. */
  public NounGroup postModifier() {
    return postModifier;
  }

  /** Returns the preModifier.  */
  public String preModifier() {
    return preModifier;
  }

  /** Returns the preposition.*/
  public String preposition() {
    return preposition;
  }

  /** Returns the full name with the head word stemmed */
  public String stemmed() {
    StringBuilder full=new StringBuilder();
    if(preModifier!=null) full.append(preModifier).append(' ');
    full.append(PlingStemmer.stem(head.toLowerCase()));
    if(adjective!=null) full.append(' ').append(adjective);
    if(preposition!=null) full.append(' ').append(preposition);
    if(postModifier!=null) full.append(' ').append(postModifier.original());
    return(full.toString());
  }
  
  /** Stems the head. TRUE if this had any effect */
  public boolean stemHead() {
    String stemmed=PlingStemmer.stem(head);
    boolean result=!stemmed.equals(head);
    head=stemmed;
    return(result);
  }
  /** Constructs a noun group from a String */
  public NounGroup(String s) {
    this(Arrays.asList(s.split("[\\s_]+")));
  }  

  /** Constructs a noun group from a list of words */
  public NounGroup(List<String> words) { 
    // Assemble the original
    original=words.toString().replace(", ", " ");
    original=original.substring(1,original.length()-1);
    
    // Cut away preceding determiners
    if(words.size()>0 && determiners.contains(words.get(0).toLowerCase())) {
      determiner=words.get(0).toLowerCase();
      words=words.subList(1, words.size());
    }
    
    // Locate prepositions (but not in first or last position)
    int prepPos;
    for(prepPos=1;prepPos<words.size()-1;prepPos++) {
      if(prepositions.contains(words.get(prepPos))) {
        preposition=words.get(prepPos);
        break;
      }
    }
    
    // Locate "-ing"-adjectives before prepositions (but not at pos 0)
    int ingPos;
    for(ingPos=1;ingPos<prepPos;ingPos++) {
      if(words.get(ingPos).endsWith("ing")) {
        adjective=words.get(ingPos);
        break;
      }
    }

    // Cut off postmodifier in "Blubs blubbing in blah"    
    if(preposition!=null && adjective!=null && ingPos==prepPos-1) {
      postModifier=new NounGroup(words.subList(prepPos+1, words.size()));
      words=words.subList(0, ingPos);
    }
    // Cut off postmodifier in "Blubs blubbing blah"
    else if(adjective!=null) {
      postModifier=new NounGroup(words.subList(ingPos+1, words.size()));
      words=words.subList(0, ingPos);      
    }
    // Cut off postmodifier in "Blubs in blah"
    else if(preposition!=null) {
      postModifier=new NounGroup(words.subList(prepPos+1, words.size()));
      if(prepPos>1 && isAdjective.apply(words.get(prepPos-1))) {
        adjective=words.get(prepPos-1);        
        words=words.subList(0, prepPos-1);      
      } else {
        words=words.subList(0, prepPos);      
      }  
    }

    if(words.size()==0) return;

    head=words.get(words.size()-1);
    if(words.size()>1) {
      preModifier=words.subList(0, words.size()-1).toString().replace(", ", "_");
      preModifier=preModifier.substring(1, preModifier.length()-1);
    }
  }
  

  /** Checks if the originals match */
  public boolean equals(Object o) {
    return(o instanceof NounGroup && ((NounGroup)o).original.equals(original));
  }

  /** Returns the original */
  public String toString() {
    return(original);
  }

  /** Returns all fields in a String */
  public String description() {
    return("NounGroup:\n"+
           "  Original: "+original+"\n"+
           "  Stemmed: "+stemmed()+"\n"+
           "  Determiner: "+determiner+"\n"+
           "  preModifiers: "+preModifier+"\n"+
           "  Head: "+head+"\n"+
           "  Adjective: "+adjective+"\n"+
           "  Preposition: "+preposition+"\n"+
           "  postModifier: \n"+(postModifier==null?"":postModifier.description()));
  }

  /** Test method   */
  public static void main(String[] args) throws Exception {
    D.p("Enter a noun group and press ENTER. Press CTRL+C to abort");
    while(true) {
      D.p(new NounGroup(D.r()).description());
    }
  }

}
