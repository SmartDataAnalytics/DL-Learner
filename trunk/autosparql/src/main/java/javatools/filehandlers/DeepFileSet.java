package javatools.filehandlers;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Stack;
import java.util.regex.Pattern;

import javatools.administrative.D;
import javatools.datatypes.PeekIterator;

/** 
This class is part of the Java Tools (see http://mpii.de/yago-naga/javatools).
It is licensed under the Creative Commons Attribution License 
(see http://creativecommons.org/licenses/by/3.0) by 
the YAGO-NAGA team (see http://mpii.de/yago-naga).
  

  
 

 This class represents a set of files as given by a wildcard string.
 It can also recurse into subfolders.
 It does not return folders and is not case-sensitive.
 The class can be used as an Iterator or Iterable (e.g. in a for-each-loop).<BR>
 Example:
 <PRE>
 for(File f : new DeepFileSet("c:\\myfiles","*.jaVa"))
 System.out.println(f);
 -->
 c:\myfiles\FileSet.java
 c:\myfiles\HTMLReader.java
 c:\myfiles\mysubfolder\OtherFile.java
 ...
 </PRE>

 */
public class DeepFileSet extends PeekIterator<File> {

  protected final Stack<File> paths = new Stack<File>();

  protected final Pattern wildcard;

  protected Iterator<File> currentIterator;

  public Pattern patternForWildcard(String wildcard) {
    return (Pattern.compile("(?i)" + wildcard.replace(".", "\\.").replace("*",".*").replace('?', '.')));
  }

  /** Constructs a DeepFileSet from a path that ends in a wildcard*/
  public DeepFileSet(File folderPlusWildcard) {
    this(folderPlusWildcard.getParentFile()==null?
        new File("."):folderPlusWildcard.getParentFile(),folderPlusWildcard.getName());
  }

  /** Constructs a DeepFileSet from a path that ends in a wildcard*/
  public DeepFileSet(String folderPlusWildcard) {
    this(new File(folderPlusWildcard));
  }

  /** Constructs a DeepFileSet from path and wildcard */
  public DeepFileSet(File folder, String wildcard) {
    File path = folder;
    paths.push(path);
    this.wildcard = patternForWildcard(wildcard);
    setIterator();
  }

  /** Pops a path, sets the iterator to the files in the path*/
  protected boolean setIterator() {
    if (paths.size() == 0) return (false);
    File folder = paths.pop();
    currentIterator = Arrays.asList(folder.listFiles(new FileFilter() {

      public boolean accept(File pathname) {
        if (pathname.isDirectory()) paths.push(pathname);
        return (wildcard.matcher(pathname.getName()).matches());
      }
    })).iterator();
    return (true);
  }

  @Override
  protected File internalNext() throws Exception {
    while (!currentIterator.hasNext()) {
      if (!setIterator()) return (null);
    }
    return (currentIterator.next());
  }

  /** Returns the current state of this DeepFileSet */
  public String toString() {
    return ("DeepFileSet at " + paths);
  }

  /** Test routine */
  public static void main(String argv[]) {
    D.p("Enter a filename with wildcards and hit ENTER. Press CTRL+C to abort");
    while (true) {
      for (File f : new DeepFileSet(D.r()))
        D.p(f);
    }
  }
}
