package javatools.database;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javatools.administrative.Announce;
import javatools.administrative.D;

/** 
 This class is part of the Java Tools (see http://mpii.de/yago-naga/javatools).
 It is licensed under the Creative Commons Attribution License 
 (see http://creativecommons.org/licenses/by/3.0) by 
 the YAGO-NAGA team (see http://mpii.de/yago-naga).
 
 
 


 This class provides a wrapping for
 <A HREF=http://wordnet.princeton.edu/>WordNet</A>.
 Each instance of this class can wrap one relation.
 For example, an instance can wrap Mereonymy, another one can wrap Hyponymy etc..
 To create an instance for the relation X, you need the files <TT>wn_s.pl</TT> and <TT>wn_<I>X</I>.pl</TT>.
 For example, for Hyponymy, you need the files <TT>wn_s.pl</TT> and <TT>wn_hyp.pl</TT>.
 These files can be downloaded from WordNet's site.
 They are part of the Prolog version of WordNet.
 All strings are being normalized (see Char.java).<BR>
 Example:
 <PRE>
   // WordNet synset definitions
   File synsetDef=new File("wn_s.pl");
   // Choose hyponomy
   File relationDef=new File("wn_hyp.pl");
   // Choose to store only nouns and verbs
   EnumSet<WordNet.WordType> types
   =EnumSet.of(WordNet.WordType.NOUN,WordNet.WordType.VERB);
   // Choose to store only two senses per word
   int sensesPerWord=2;
   WordNet w=new WordNet(synsetDef,relationDef,types,sensesPerWord);
   for(Synset s : w.synsetsFor("mouse")) {
   D.p(s);              // Print synset
   D.p(" "+s.getUps()); // Print direct supersynsets
   }
 -->
   Synset #102244530 (NOUN): [mouse, ]
   [Synset #102243671 (NOUN): [rodent, gnawer, gnawing_animal, ]]
   Synset #103651364 (NOUN): [mouse, computer_mouse, ]
   [Synset #103158939 (NOUN): [electronic_device, ]]
   Synset #201175362 (VERB): [mouse, ]
   [Synset #201174946 (VERB): [manipulate, ]]
   Synset #201856050 (VERB): [sneak, mouse, creep, pussyfoot, ]
   [Synset #201849285 (VERB): [walk, ]]
 </PRE>
 Note that if you load only n senses per word, there may be synsets in the WordNet instance
 that do not have words!
 */

public class WordNet implements Serializable {

  /** Types of words in Wordnet */
  public enum WordType {
    NOUN, VERB, ADJECTIVE, ADVERB
  };

  /** Pattern for synset definitions */
  //              s  (00001740,1111,'enti',n,111111,11).
  public static Pattern SYNSETPATTERN = Pattern.compile("s\\((\\d{9}),\\d*,'(.*)',(.),(\\d*),.*");

  public static final int IDGROUP = 1;

  public static final int WORDGROUP = 2;

  public static final int CLASSGROUP = 3;

  public static final int SENSENUMGROUP = 4;

  /** Pattern for relation definitions */
  //                hyp    (00001740,00001740).
  public static Pattern RELATIONPATTERN = Pattern.compile("\\w*\\((\\d{9}),(\\d{9})\\)\\.");

  public static final int DOWNGROUP = 1;

  public static final int UPGROUP = 2;

  /** Holds the current source synset for NCA calculi */
  protected Synset source;

  /** Represents a WordNet synset */
  public static class Synset implements Serializable, Comparable<Synset> {

    /** Id as given in the WordNet Prolog files */
    protected int id;

    /** Words of this synset */
    protected List<String> words = new ArrayList<String>(8);

    /** Upward connected synsets of this synset */
    protected List<Synset> ups = new ArrayList<Synset>(0);

    /** Downward connected synsets of this synset */
    protected List<Synset> downs = new ArrayList<Synset>(0);

    /** Public marker */
    public Object marker = null;

    /** Public marker */
    public int intMarker = 0;

    /** Constructs a synset from an id */
    public Synset(int idnum) {
      id = idnum;
    }

    /** Returns the id */
    public int hashCode() {
      return (id);
    }

    /** Tells wether the ids are the same */
    public boolean equals(Object o) {
      return (o != null && o instanceof Synset && ((Synset) o).id == id);
    }

    /** Sorts synsets by id */
    public int compareTo(Synset o) {
      return (o.id == id ? 0 : o.id < id ? 1 : -1);
    }

    /** Returns the id and the words of this synset */
    public final String toString() {
      StringBuilder result = new StringBuilder("Synset #").append(id).append(" (").append(getWordType()).append("): [");
      for (String s : words)
        result.append(s).append(", ");
      return (result.append("]").toString());
    }

    /** Returns the downs. */
    public List<Synset> getDowns() {
      return downs;
    }

    /** Returns the id. */
    public int getId() {
      return id;
    }

    /** Returns the ups. */
    public List<Synset> getUps() {
      return ups;
    }

    /** Returns the words. */
    public List<String> getWords() {
      return words;
    }

    /** Returns the most frequent word for this and the parent */
    public String toSmallString() {
      if (words.size() == 0) return ("#" + id);
      if (ups.size() == 0 || ups.get(0).words.size() == 0) return (words.get(0));
      return (words.get(0) + " (" + ups.get(0).words.get(0) + ")");
    }

    /** Returns WordType */
    public WordType getWordType() {
      return (WordType.values()[(id / 100000000) - 1]);
    }

    /** Returns the set of ancestors */
    public Set<Synset> ancestors() {
      Set<Synset> set = new TreeSet<Synset>();
      ancestors(set);
      return (set);
    }

    /** Returns the set of ancestors of a Synset*/
    protected void ancestors(Set<Synset> set) {
      for (Synset s : this.ups) {
        if (set.add(s)) s.ancestors(set);
      }
    }

    /** Returns the set of descendants */
    public Set<Synset> descendants() {
      Set<Synset> set = new TreeSet<Synset>();
      descendants(set);
      return (set);
    }

    /** Returns the set of ancestors of a Synset*/
    protected void descendants(Set<Synset> set) {
      for (Synset s : this.downs) {
        if (set.add(s)) s.descendants(set);
      }
    }

  }

  /** Maps words to synsets */
  public Map<String, List<Synset>> word2synsets = new HashMap<String, List<Synset>>(114700);

  /** Maps ids to synsets (compiled on demand)*/
  protected Map<Integer, Synset> id2synset = null;

  /** Returns the map from ids to synsets */
  public Map<Integer, Synset> getId2SynsetMap() {
    return (id2synset);
  }

  /** Returns a synset for a given id */
  public Synset getSynset(int id) {
    return (id2synset.get(id));
  }

  /** Compiles the set of all Synsets */
  public Collection<Synset> getSynsets() {
    return (id2synset.values());
  }

  /** Returns the list of synsets that contain a word*/
  public List<Synset> synsetsFor(String s) {
    return (word2synsets.get(s.replace('_', ' ')));
  }

  /** Returns the number of synsets */
  public int numSynsets() {
    return (id2synset.size());
  }

  /** Returns the synset that contain two words*/
  public Synset synsetFor(String word, String otherWord) {
    for (Synset s : word2synsets.get(word)) {
      if (s.words.contains(otherWord)) return (s);
    }
    return (null);
  }

  /** Constructor with no relation (only synsets) */
  public WordNet(File wn_s, EnumSet<WordType> lextypes, int sensesPerWord) throws IOException {
    this(wn_s, null, lextypes, sensesPerWord);
  }

  /** Constructor with no relation (only synsets) */
  public WordNet(File wn_s, WordType lextype, int sensesPerWord) throws IOException {
    this(wn_s, null, EnumSet.of(lextype), sensesPerWord);
  }

  /** Constructor */
  public WordNet(File wn_s, File relation, WordType lextype, int sensesPerWord) throws IOException {
    this(wn_s, relation, EnumSet.of(lextype), sensesPerWord);
  }

  /** Constructor (main constructor)*/
  public WordNet(File wn_s, File relation, EnumSet<WordType> lextypes, int sensesPerWord) throws IOException {
    int maxId = 0;
    for (WordType w : lextypes)
      if (w.ordinal() + 1 > maxId) maxId = w.ordinal() + 1;
    id2synset = new HashMap<Integer, Synset>();
    BufferedReader in = new BufferedReader(new FileReader(wn_s));
    String s = in.readLine();
    if (!SYNSETPATTERN.matcher(s).matches()) throw new IOException(wn_s + " does not contain WordNet synsets");
    while (s != null) {
      // Scan to next desired group
      int type = s.charAt(2) - 48;
      if (type > maxId) break;
      if (!lextypes.contains(WordType.values()[type - 1])) {
        s = in.readLine();
        continue;
      }
      // Fill up synsets
      while (s != null && s.charAt(2) - 48 == type) {
        Matcher m = SYNSETPATTERN.matcher(s);
        if (!m.matches()) continue; // happens in a few cases
        int id = Integer.parseInt(m.group(IDGROUP));
        Synset currentSS = id2synset.get(id);
        if (currentSS == null) {
          currentSS = new Synset(id);
          id2synset.put(id, currentSS);
        }
        int senseNumber = Integer.parseInt(m.group(SENSENUMGROUP));
        if (senseNumber <= sensesPerWord) {
          String word = m.group(WORDGROUP).replace("''", "'");     
          currentSS.words.add(word);
          List<Synset> synsets = word2synsets.get(word);
          if (synsets == null) {
            synsets = new ArrayList<Synset>(sensesPerWord);
            word2synsets.put(word, synsets);
          }
          while (synsets.size() < senseNumber)
            synsets.add(null);
          synsets.set(senseNumber - 1, currentSS);
        }
        s = in.readLine();
      }
    }
    in.close();
    for (List<Synset> l : word2synsets.values()) {
      for (int i = 0; i < l.size(); i++) {
        if (l.get(i) == null) l.remove(i--);
      }
    }
    if (relation == null) return;
    in = new BufferedReader(new FileReader(relation));
    s = in.readLine();
    if (!RELATIONPATTERN.matcher(s).matches()) throw new IOException(relation + " does not contain a WordNet relation");
    while (s != null) {
      // Scan to next desired group
      int type = s.charAt(s.indexOf('(') + 1) - 48;
      if (type > maxId) break;
      if (!lextypes.contains(WordType.values()[type - 1])) {
        s = in.readLine();
        continue;
      }
      // Fill relation
      while (s != null) {
        Matcher m = RELATIONPATTERN.matcher(s);
        if (m.matches()) {
          if (m.group(UPGROUP).charAt(0) - 48 != type) break;
          Synset down = id2synset.get(Integer.parseInt(m.group(DOWNGROUP)));
          Synset up = id2synset.get(Integer.parseInt(m.group(UPGROUP)));
          if (up != null && down != null) {
            down.ups.add(up);
            up.downs.add(down);
          }
        }
        s = in.readLine();
      }
    }
    in.close();
  }

  /** Returns a short String description */
  public String toString() {
    return ("WordNet: " + word2synsets.size() + " synsets");
  }

  /** Labels all up's of a synset with their distance from start */
  protected void setSource(Synset start, int dist) {
    // Have we been here with a better path?
    if (start.marker == source && start.intMarker <= dist) dist = start.intMarker;
    start.marker = source;
    start.intMarker = dist;
    // Even if we've been here before, we need to go up to the root,
    // because something on this path might have been destroyed
    for (Synset s : start.ups) {
      setSource(s, dist + 1);
    }
  }

  /** Sets the source for an NCA or distance calculus */
  protected void setSource(Synset start) {
    if (source == start) return;
    source = start;
    setSource(start, 0);
  }

  /** Finds the NCA by help of labels */
  protected Synset nca(Synset destination, int[] dist, int d) {
    if (destination.marker == source) {
      dist[0] = d + destination.intMarker;
      return (destination);
    }
    if (destination.marker == dist) {
      System.err.println("Found a loop in WordNet with " + destination);
      return (null);
    }
    destination.marker = dist;
    Synset bestSS = null;
    dist[0] = Integer.MAX_VALUE;
    for (Synset s : destination.ups) {
      int[] dist2 = new int[1];
      Synset c = nca(s, dist2, d + 1);
      if (dist2[0] < dist[0]) {
        dist[0] = dist2[0];
        bestSS = c;
      }
    }
    return (bestSS);
  }

  /** Returns the nearest common ancestor of two synsets. 
   * The NCA (nearest common ancestor) is the ancestor node for both synsets that has the
   * smallest distance (number of edges) to them. This need not be the lowest common ancestor!
   * Returns the distance source->NCA in dist1[0] and the distance destination->NCA in dist2[0].
   * In case of failure, null is returned and dist1[0]=dist2[0]=-1. */
  public Synset nca(Synset source, Synset destination, int[] dist1, int[] dist2) {
    if (source == destination) {
      dist1[0] = dist2[0] = 0;
      return (source);
    }
    setSource(source);
    Synset nca = nca(destination, dist2, 0);
    if (nca == null) {
      dist1[0] = -1;
      dist2[0] = -1;
    } else {
      dist1[0] = nca.intMarker;
    }
    return (nca);
  }

  /** Returns the nearest common ancestor of two synsets.
   * The NCA (nearest common ancestor) is the ancestor node for both synsets that has the
   * smallest distance (number of edges) to them. This need not be the lowest common ancestor! */
  public Synset nca(Synset s1, Synset s2) {
    int[] dist1 = new int[1];
    int[] dist2 = new int[2];
    return (nca(s1, s2, dist1, dist2));
  }

  /** Returns the length of s1->NCA->s2, -1 in case of failure */
  public int distance(Synset s1, Synset s2) {
    int d1[] = new int[1];
    int d2[] = new int[1];
    if (nca(s1, s2, d1, d2) != null) return (d1[0] + d2[0]);
    else return (-1);
  }

  /** Returns the distance in the hierarchy upwards from the first node to the second, -1 in case of failure */
  public int ancestor(Synset s1, Synset s2) {
    setSource(s1);
    if (s2.marker != s1) return (-1);
    return (s2.intMarker);
  }

  /** Removes a synset*/
  public void remove(Synset s) {
    id2synset.remove(s.id);
    for(String w : s.getWords()) {
      Collection<Synset> synsets=word2synsets.get(w);
      if(synsets!=null) {
        synsets.remove(s);      
        if(synsets.size()==0) word2synsets.remove(w);
      }
    }
    for(Synset up : s.getUps()) {
      up.downs.remove(s);
    }
    for(Synset down : s.getDowns()) {
      down.ups.remove(s);
    }
  }
  /** Test routine, requires the Prolog version of WordNet and the paths adjusted. */
  public static void main(String[] argv) throws Exception {
    Announce.doing("Loading");
    WordNet wordNet = new WordNet(new File("c:\\Program Files\\WordNet\\2.1\\Prolog\\wn_s.pl"), new File(
        "c:\\Program Files\\WordNet\\2.1\\Prolog\\wn_hyp.pl"), WordType.NOUN, 13);
    Announce.done();
    while (true) {
      D.p("Enter an English and press ENTER (CTRL+C to abort)");
      List<Synset> targetSynsets = wordNet.synsetsFor(D.r());
      if (targetSynsets == null) continue;
      for (Synset s : targetSynsets)
        D.p(s.toSmallString());
    }
  }

}
