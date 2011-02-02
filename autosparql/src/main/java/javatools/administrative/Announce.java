package javatools.administrative;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Set;

import javatools.datatypes.FinalSet;
import javatools.parsers.NumberFormatter;

/**
 This class is part of the Java Tools (see http://mpii.de/yago-naga/javatools).
 It is licensed under the Creative Commons Attribution License
 (see http://creativecommons.org/licenses/by/3.0) by
 the YAGO-NAGA team (see http://mpii.de/yago-naga).





 This class can make progress announcements. The announcements are handled by an object,
 but static methods exist to simplify the calls.<BR>
 Example:
 <PRE>
 Announce.doing("Testing 1");
 Announce.doing("Testing 2");
 Announce.message("Now testing", 3);
 Announce.warning(1,2,3);
 Announce.debug(1,2,3);
 Announce.doing("Testing 3a");
 Announce.doneDoing("Testing 3b");
 Announce.done();
 Announce.progressStart("Testing 3c",5); // 5 steps
 D.waitMS(1000);
 Announce.progressAt(1); // We're at 1 (of 5)
 D.waitMS(3000);
 Announce.progressAt(4); // We're at 4 (of 5)
 D.waitMS(1000);
 Announce.progressDone();
 Announce.done();
 Announce.done();
 Announce.done(); // This is one too much, but it works nevertheless
 -->
 Testing 1...
 Testing 2...
 Now testing 3
 Warning:1 2 3
 Announce.main(243): 1 2 3
 Testing 3a... done
 Testing 3b... done
 Testing 3c...........(4.00 s to go)................................ done (5.00 s)
 done
 done
 </PRE>
 The progress bar always walks to MAXDOTS dots. The data is written to Announce.out
 (by default System.err). There are different levels of announcements that can be switched on
 and off.
 */
public class Announce {

  /** Log level*/
  public enum Level {
    MUTE, ERROR, WARNING, STATE, MESSAGES, DEBUG
  };

  /** Current log level*/
  protected static Level level = Level.MESSAGES;

  /** Maximal number of dots */
  public static int MAXDOTS = 40;

  /** Where to write to (default: System.err) */
  protected static Writer out = new BufferedWriter(new OutputStreamWriter(System.out));

  /** Indentation level */
  protected static int doingLevel = 0;

  /** Are we at the beginning of a line?*/
  protected static boolean cursorAtPos1 = true;

  /** Memorizes the maximal value for progressAt(...) */
  protected static double progressEnd = 0;

  /** Memorizes the number of printed dots */
  protected static int progressDots = 0;

  /** Memorizes the process start time */
  protected static long progressStart = 0;

  /** Internal counter for progresses*/
  protected static double progressCounter = 0;

  /** Did we print the estimated time? */
  protected static boolean printedEstimatedTime;

  /** Memorizes the timer */
  protected static long timer;

  /** TRUE if debugging is on*/
  protected static boolean debug;

  /** Starts the timer */
  public static void startTimer() {
    timer = System.currentTimeMillis();
  }

  /** Retrieves the time */
  public static long getTime() {
    return (System.currentTimeMillis() - timer);
  }

  /** Closes the writer */
  public static void close() throws IOException {
    out.close();
  }

  /** Switches announcing on or off */
  public static void setLevel(Level l) {
    level = l;
  }

  /** Blanks*/
  public static final String blanks="                                                                  ";

  /** Returns blanks*/
  public static String blanks(int n) {
   if(n<=0) return("");
    if(n>=blanks.length()) return(blanks);
    return(blanks.substring(0, n));
  }

  /** Returns blanks*/
  protected static String blanks() {
    return(blanks(doingLevel*2));
  }
  /** Internal printer */
  protected static void print(Object... o) {
    try {
      if (cursorAtPos1) out.write(blanks());
      out.write(D.toString(o).replace("\n", "\n"+blanks()));
      out.flush();
    } catch (IOException e) {
    }
    cursorAtPos1 = false;
  }

  /** Internal printer for new line */
  protected static void newLine() {
    if (cursorAtPos1) return;
    try {
      out.write("\n");
      out.flush();
    } catch (IOException e) {
    }
    cursorAtPos1 = true;
  }

  /** Prints an (indented) message */
  public static void message(Object... o) {
    if (D.smaller(level, Level.MESSAGES)) return;
    newLine();
    print(o);
    newLine();
  }

  /** Prints a debug message with the class and method name preceeding */
  public static void debug(Object... o) {
    if (D.smaller(level, Level.DEBUG)) return;
    newLine();
    print(CallStack.toString(new CallStack().ret().top()) + ": ");
    print(o);
    newLine();
  }

  /** Prints a debug message */
  public static void debugMsg(Object... o) {
    if (D.smaller(level, Level.DEBUG)) return;
    newLine();
    print(o);
    newLine();
  }

  /** Prints an error message and aborts (aborts even if log level is mute)*/
  public static void error(Object... o) {
    if (D.smaller(level, Level.ERROR)) System.exit(255);
    while(doingLevel>0) failed();
    newLine();
    print("Error: ");
    print(o);
    newLine();
    System.exit(255);
  }

  /** Prints an exception and aborts (aborts even if log level is mute)*/
  public static void error(Exception e) {
    if (D.smaller(level, Level.ERROR)) System.exit(255);
    e.printStackTrace(new PrintWriter(out));
    System.exit(255);
  }

  /** Prints a warning*/
  public static void warning(Object... o) {
    if (D.smaller(level, Level.WARNING)) return;
    newLine();
    print("Warning:  ");
    doingLevel+=5;
    print(o);
    doingLevel-=5;
    newLine();
  }

  /** Sets the writer the data is written to */
  public static void setWriter(Writer w) {
    out = w;
  }

  /** Sets the writer the data is written to */
  public static void setWriter(OutputStream s) {
    out = new BufferedWriter(new OutputStreamWriter(s));
  }

  /** Writes "s..."*/
  public static void doing(Object... o) {
    if (D.smaller(level, Level.STATE)) return;
    newLine();
    print(o);
    print("... ");
    doingLevel++;
  }

  /** Writes "failed NEWLINE" */
  public static void failed() {
    if (doingLevel > 0) {
      doingLevel--;
      if (D.smaller(level, Level.STATE)) return;
      print("failed");
      newLine();
    }
  }

  /** Writes "done NEWLINE"*/
  public static void done() {
    if (doingLevel > 0) {
      doingLevel--;
      if (D.smaller(level, Level.STATE)) return;
      print("done");
      newLine();
    }
  }

  /** Writes "done with problems NEWLINE"*/
  public static void doneWithProbs() {
    if (doingLevel > 0) {
      doingLevel--;
      if (D.smaller(level, Level.STATE)) return;
      print("done with problems");
      newLine();
    }
  }

  /** Calls done() and doing(...)*/
  public static void doneDoing(Object... s) {
    done();
    doing(s);
  }

  /** Writes s, prepares to make progress up to max */
  public static void progressStart(String s, double max) {
    progressEnd = max;
    progressDots = 0;
    progressStart = System.currentTimeMillis();
    printedEstimatedTime = false;
    progressCounter=0;
    if (!D.smaller(level, Level.STATE)) {
      newLine();
      print(s,"...");
    }
    doingLevel++;
  }

  /** Notes that the progress is at d, prints dots if necessary,
   * calculates and displays the estimated time after 20sec of the progress */
  public static void progressAt(double d) {
    if (d > progressEnd || d * MAXDOTS / progressEnd <= progressDots) return;
    StringBuilder b = new StringBuilder();
    while (d * MAXDOTS / progressEnd > progressDots) {
      progressDots++;
      b.append(".");
    }
    if (!printedEstimatedTime && System.currentTimeMillis() - progressStart > 20000) {
      b.append('(').append(NumberFormatter.formatMS((long) ((System.currentTimeMillis() - progressStart) * (progressEnd - d) / d))).append(" to go)");
      printedEstimatedTime = true;
    }
    if (!D.smaller(level, Level.STATE)) print(b);
  }

  /** One progress step (use alternatively to progressAt) */
  public static void progressStep() {
    progressAt(progressCounter++);
  }

  /** Fills missing dots and writes "done NEWLINE"*/
  public static void progressDone() {
    progressAt(progressEnd);
    doingLevel--;
    if (!D.smaller(level, Level.STATE)) {
      print(" done (" + NumberFormatter.formatMS(System.currentTimeMillis() - progressStart) + ")");
      newLine();
    }
  }

  /** Writes "failed NEWLINE"*/
  public static void progressFailed() {
    failed();
  }

  /** Writes a help text and exits */
  public static void help(Object... o) {
    if (D.smaller(level, Level.ERROR)) System.exit(63);
    newLine();
    for (Object s : o) {
      print(s);
      newLine();
    }
    System.exit(63);
  }

  /** Retrieves the time */
  public static void printTime() {
    message("Time:", NumberFormatter.formatMS(getTime()));
  }

  /** Command line arguments that ask for help*/
  protected static final Set<String> helpCommands = new FinalSet<String>("-help", "--help", "-h", "--h", "-?", "/?", "/help");

  /** Says whether a command line argument asks for help*/
  public static boolean isHelp(String arg) {
    return (helpCommands.contains(arg.toLowerCase()));
  }

  /** Test routine */
  public static void main(String[] args) {
    Announce.startTimer();
    Announce.doing("Testing 1");
    Announce.doing("Testing 2");
    Announce.message("Now testing", 3);
    Announce.warning(1, 2, 3);
    Announce.debug(1, 2, 3);
    Announce.doing("Testing 3a");
    Announce.doneDoing("Testing 3b");
    Announce.done();
    Announce.progressStart("Testing 3c", 5); // 5 steps
    D.waitMS(1000);
    Announce.progressAt(1); // We're at 1 (of 5)
    D.waitMS(3000);
    Announce.progressAt(4); // We're at 4 (of 5)
    D.waitMS(1000);
    Announce.progressDone();
    Announce.done();
    Announce.done();
    Announce.done(); // This is one too much, but it works nevertheless
    Announce.printTime();
  }
}