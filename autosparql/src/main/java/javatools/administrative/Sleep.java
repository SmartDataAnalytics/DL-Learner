package javatools.administrative;
import javatools.parsers.NumberFormatter;
import javatools.parsers.NumberParser;

/** 
This class is part of the Java Tools (see http://mpii.de/yago-naga/javatools).
It is licensed under the Creative Commons Attribution License 
(see http://creativecommons.org/licenses/by/3.0) by 
the YAGO-NAGA team (see http://mpii.de/yago-naga).

Sleeps for a given time
*/

public class Sleep {

  /** Sleeps for a given time*/
  public static void sleep(String time) throws Exception {
    sleep((long)Double.parseDouble(NumberParser.getNumber(NumberParser.normalize(time))));
  }

  /** Sleeps for a given time*/
  public static void sleep(long seconds) throws Exception {
    Announce.progressStart("Sleeping "+NumberFormatter.formatMS(seconds*1000),seconds);
    for(long slept=0;slept<seconds;slept++) {
      Announce.progressAt(slept);    
      Thread.sleep(1000);
    } 
    Announce.progressDone();
  }
  
  /** Sleeps for a given time */
  public static void main(String[] args) throws Exception {
    if(args==null || args.length==0) {
      Announce.help("Sleep for a given time. E.g. 'sleep 3h 5min'");
    }
    String time="";
    for(int i=0;i<args.length;i++) time+=" "+args[i];
    sleep(time);
  }

}
