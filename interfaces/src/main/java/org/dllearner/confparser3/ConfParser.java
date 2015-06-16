/* ConfParser.java */
/* Generated By:JavaCC: Do not edit this line. ConfParser.java */
package org.dllearner.confparser3;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dllearner.cli.ConfFileOption2;
import org.dllearner.parser.KBParser;

public class ConfParser implements ConfParserConstants {

	// special directives (those without a property name)
	private Map<String, ConfFileOption2> specialOptions = new HashMap<String, ConfFileOption2>();

	// conf file options
	private List<ConfFileOption2> confOptions = new LinkedList<ConfFileOption2>();
	private Map<String, ConfFileOption2> confOptionsByProperty = new HashMap<String, ConfFileOption2>();
	private Map<String, List<ConfFileOption2>> confOptionsByBean = new HashMap<String, List<ConfFileOption2>>();

	private void addConfOption(ConfFileOption2 confOption) {
		confOptions.add(confOption);
		confOptionsByProperty.put(confOption.getPropertyName(), confOption);
		String beanName = confOption.getBeanName();
		if (confOptionsByBean.containsKey(beanName))
			confOptionsByBean.get(beanName).add(confOption);
		else {
			LinkedList<ConfFileOption2> optionList = new LinkedList<ConfFileOption2>();
			optionList.add(confOption);
			confOptionsByBean.put(beanName, optionList);
		}
	}

	public List<ConfFileOption2> getConfOptions() {
		return confOptions;
	}

	public Map<String, ConfFileOption2> getConfOptionsByProperty() {
		return confOptionsByProperty;
	}

	public ConfFileOption2 getConfOptionsByProperty(String propertyName) {
		ConfFileOption2 confOption = confOptionsByProperty.get(propertyName);
		if (confOption == null) {
			confOption = specialOptions.get(propertyName);
		}
		return confOption;
	}

	public Map<String, List<ConfFileOption2>> getConfOptionsByBean() {
		return confOptionsByBean;
	}

	public List<ConfFileOption2> getConfOptionsByBean(String beanName) {
		return confOptionsByBean.get(beanName);
	}

	public static ConfParser parseFile(File filename) throws FileNotFoundException, ParseException,
			UnsupportedEncodingException {
		ConfParser parser = new ConfParser(new InputStreamReader(new FileInputStream(filename), "UTF-8"));
		parser.Start();
		return parser;
	}

  final public void Start() throws ParseException {ConfFileOption2 confOption;
    label_1:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
      case ID:{
        ;
        break;
        }
      default:
        jj_la1[0] = jj_gen;
        break label_1;
      }
      confOption = ConfOption();
if(confOption.getPropertyName() == null) {
                        specialOptions.put(confOption.getBeanName(),confOption);
             } else {
                addConfOption(confOption);
             }
    }
    jj_consume_token(0);
PostProcessor pp = new PostProcessor(confOptions, specialOptions);
    pp.applyAll();
  }

  final public ConfFileOption2 ConfOption() throws ParseException {boolean containsSubOption=false;
        String value="", value1="", value2="", tmp="", tmp2="";
        Set<String> values = new HashSet<String>();
        Map<String,String> tuples = new HashMap<String,String>();
        Map<String,Double> tuplesD = new HashMap<String,Double>();

        ConfFileOption2 option = new ConfFileOption2();
        boolean isBeanRef = false;
        boolean isBeanCollection = false;
        String beanName;
        String propertyName = "";
        String propertyValue = "";
        Class<?> propertyType;
        Object val = null;
        Double d;
    beanName = Id();
    switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
    case COMMAND_END:{
      jj_consume_token(COMMAND_END);
      propertyName = Id();
containsSubOption=true;
      break;
      }
    default:
      jj_la1[1] = jj_gen;
      ;
    }
    jj_consume_token(13);
    switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
    case ID:{
      // two strings separated by a double colon
               // LOOKAHEAD(2) value1=Id() ":" value2=Id() { useColon = true; }
               // simple string
                      propertyValue = Id();
if(propertyValue.equals("true") || propertyValue.equals("false")) {
                   val = Boolean.valueOf(propertyValue); propertyType = Boolean.class;
                } else {
                        val = propertyValue; propertyType = String.class; isBeanRef =  true;
                }
      break;
      }
    case STRING:{
      propertyValue = String();
val = propertyValue; propertyType = String.class;
      break;
      }
    case NUMBER:{
      val = Integer();
propertyValue = val.toString(); propertyType = Integer.class;
      break;
      }
    case DOUBLE:{
      val = Double();
propertyValue = val.toString(); propertyType = Double.class;
      break;
      }
    default:
      jj_la1[2] = jj_gen;
      if (jj_2_5(2147483647)) {
        jj_consume_token(14);
        jj_consume_token(15);
val = new HashSet(); propertyType = Set.class; propertyValue = "{}";
      } else if (jj_2_6(4)) {
        jj_consume_token(14);
        label_2:
        while (true) {
          if (jj_2_1(2)) {
            ;
          } else {
            break label_2;
          }
          tmp = String();
values.add(tmp); propertyValue += "\u005c"" + tmp + "\u005c", ";
          jj_consume_token(16);
        }
        tmp = String();
values.add(tmp); propertyValue += "\u005c"" + tmp + "\u005c"";
        jj_consume_token(15);
propertyType = Set.class; propertyValue = "{"+ propertyValue + "}";; val = values;
      } else {
        switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
        case 17:{
          jj_consume_token(17);
val = new HashSet(); propertyType = Set.class; propertyValue = "-"; isBeanCollection = true;
          break;
          }
        case 14:{
          jj_consume_token(14);
          label_3:
          while (true) {
            if (jj_2_2(4)) {
              ;
            } else {
              break label_3;
            }
            tmp = Id();
values.add(tmp); propertyValue += tmp + ", ";
            jj_consume_token(16);
          }
          tmp = Id();
values.add(tmp); propertyValue += tmp;
          jj_consume_token(15);
val = values; propertyType = Set.class; propertyValue = "{"+ propertyValue + "}"; isBeanCollection = true;
          break;
          }
        default:
          jj_la1[3] = jj_gen;
          if (jj_2_7(2147483647)) {
            jj_consume_token(18);
            jj_consume_token(19);
val = new LinkedList(); propertyType = List.class; propertyValue = "[]";
          } else if (jj_2_8(2147483647)) {
            jj_consume_token(18);
            label_4:
            while (true) {
              if (jj_2_3(6)) {
                ;
              } else {
                break label_4;
              }
              jj_consume_token(20);
              tmp = String();
              jj_consume_token(16);
              tmp2 = String();
              jj_consume_token(21);
tuples.put(tmp,tmp2); propertyValue += "(\u005c""+ tmp + "\u005c",\u005c"" + tmp2 + "\u005c"), ";
              jj_consume_token(16);
            }
            jj_consume_token(20);
            tmp = String();
            jj_consume_token(16);
            tmp2 = String();
            jj_consume_token(21);
tuples.put(tmp,tmp2); propertyValue += "(\u005c""+ tmp + "\u005c",\u005c"" + tmp2 + "\u005c")";
            jj_consume_token(19);
val = tuples; propertyType = List.class; propertyValue = "["+ propertyValue + "]";
          } else {
            switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
            case 18:{
              jj_consume_token(18);
              label_5:
              while (true) {
                if (jj_2_4(6)) {
                  ;
                } else {
                  break label_5;
                }
                jj_consume_token(20);
                tmp = String();
                jj_consume_token(16);
                d = Double();
                jj_consume_token(21);
tuplesD.put(tmp,d); propertyValue += "(\u005c""+ tmp + "\u005c",\u005c"" + d.toString() + "\u005c"), ";
                jj_consume_token(16);
              }
              jj_consume_token(20);
              tmp = String();
              jj_consume_token(16);
              d = Double();
              jj_consume_token(21);
tuplesD.put(tmp,d); propertyValue += "(\u005c""+ tmp + "\u005c",\u005c"" + d.toString() + "\u005c")";
              jj_consume_token(19);
val = tuplesD; propertyType = List.class; propertyValue = "["+ propertyValue + "]";
              break;
              }
            default:
              jj_la1[4] = jj_gen;
              jj_consume_token(-1);
              throw new ParseException();
            }
          }
        }
      }
    }
option.setBeanRef(isBeanRef);
        option.setBeanReferenceCollection(isBeanCollection);
        option.setBeanName(beanName);
        if(containsSubOption) {
                option.setPropertyName(propertyName);
        }
        option.setPropertyType(propertyType);
        option.setPropertyValue(propertyValue);
        option.setValueObject(val);
        {if ("" != null) return option;}
    throw new Error("Missing return statement in function");
  }

  final public String Individual() throws ParseException {String name;
    switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
    case ID:{
      name = Id();
      break;
      }
    case STRING:{
      name = String();
      break;
      }
    default:
      jj_la1[5] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
{if ("" != null) return KBParser.getInternalURI(name);}
    throw new Error("Missing return statement in function");
  }

  final public String ComplexId() throws ParseException {Token t1,t2;
    if (jj_2_9(2)) {
      t1 = jj_consume_token(ID);
      jj_consume_token(22);
      t2 = jj_consume_token(ID);
{if ("" != null) return t1.image + ":" + t2.image;}
    } else {
      switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
      case ID:{
        t1 = jj_consume_token(ID);
{if ("" != null) return t1.image;}
        break;
        }
      default:
        jj_la1[6] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
    }
    throw new Error("Missing return statement in function");
  }

  final public String Id() throws ParseException {Token t;
    t = jj_consume_token(ID);
{if ("" != null) return t.image;}
    throw new Error("Missing return statement in function");
  }

  final public Double Double() throws ParseException {Token t;
    t = jj_consume_token(DOUBLE);
{if ("" != null) return new Double(t.image);}
    throw new Error("Missing return statement in function");
  }

  final public Integer Integer() throws ParseException {Token t;
    t = jj_consume_token(NUMBER);
{if ("" != null) return new Integer(t.image);}
    throw new Error("Missing return statement in function");
  }

  final public String String() throws ParseException {Token t;
  String s;
    t = jj_consume_token(STRING);
// enclosing "" are removed
    s = t.image;
    s = s.substring(1, s.length() - 1);
    {if ("" != null) return s;}
    throw new Error("Missing return statement in function");
  }

  private boolean jj_2_1(int xla)
 {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_1(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(0, xla); }
  }

  private boolean jj_2_2(int xla)
 {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_2(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(1, xla); }
  }

  private boolean jj_2_3(int xla)
 {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_3(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(2, xla); }
  }

  private boolean jj_2_4(int xla)
 {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_4(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(3, xla); }
  }

  private boolean jj_2_5(int xla)
 {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_5(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(4, xla); }
  }

  private boolean jj_2_6(int xla)
 {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_6(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(5, xla); }
  }

  private boolean jj_2_7(int xla)
 {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_7(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(6, xla); }
  }

  private boolean jj_2_8(int xla)
 {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_8(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(7, xla); }
  }

  private boolean jj_2_9(int xla)
 {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_9(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(8, xla); }
  }

  private boolean jj_3_3()
 {
    if (jj_scan_token(20)) return true;
    if (jj_3R_6()) return true;
    if (jj_scan_token(16)) return true;
    if (jj_3R_6()) return true;
    if (jj_scan_token(21)) return true;
    if (jj_scan_token(16)) return true;
    return false;
  }

  private boolean jj_3R_7()
 {
    if (jj_scan_token(ID)) return true;
    return false;
  }

  private boolean jj_3_2()
 {
    if (jj_3R_7()) return true;
    if (jj_scan_token(16)) return true;
    return false;
  }

  private boolean jj_3_5()
 {
    if (jj_scan_token(14)) return true;
    if (jj_scan_token(15)) return true;
    return false;
  }

  private boolean jj_3_9()
 {
    if (jj_scan_token(ID)) return true;
    if (jj_scan_token(22)) return true;
    return false;
  }

  private boolean jj_3R_8()
 {
    if (jj_scan_token(DOUBLE)) return true;
    return false;
  }

  private boolean jj_3R_6()
 {
    if (jj_scan_token(STRING)) return true;
    return false;
  }

  private boolean jj_3_4()
 {
    if (jj_scan_token(20)) return true;
    if (jj_3R_6()) return true;
    if (jj_scan_token(16)) return true;
    if (jj_3R_8()) return true;
    if (jj_scan_token(21)) return true;
    if (jj_scan_token(16)) return true;
    return false;
  }

  private boolean jj_3_8()
 {
    if (jj_scan_token(18)) return true;
    if (jj_scan_token(20)) return true;
    if (jj_3R_6()) return true;
    if (jj_scan_token(16)) return true;
    if (jj_3R_6()) return true;
    return false;
  }

  private boolean jj_3_6()
 {
    if (jj_scan_token(14)) return true;
    Token xsp;
    while (true) {
      xsp = jj_scanpos;
      if (jj_3_1()) { jj_scanpos = xsp; break; }
    }
    if (jj_3R_6()) return true;
    if (jj_scan_token(15)) return true;
    return false;
  }

  private boolean jj_3_1()
 {
    if (jj_3R_6()) return true;
    if (jj_scan_token(16)) return true;
    return false;
  }

  private boolean jj_3_7()
 {
    if (jj_scan_token(18)) return true;
    if (jj_scan_token(19)) return true;
    return false;
  }

  /** Generated Token Manager. */
  public ConfParserTokenManager token_source;
  SimpleCharStream jj_input_stream;
  /** Current token. */
  public Token token;
  /** Next token. */
  public Token jj_nt;
  private int jj_ntk;
  private Token jj_scanpos, jj_lastpos;
  private int jj_la;
  private int jj_gen;
  final private int[] jj_la1 = new int[7];
  static private int[] jj_la1_0;
  static {
      jj_la1_init_0();
   }
   private static void jj_la1_init_0() {
      jj_la1_0 = new int[] {0x200,0x100,0x1e00,0x24000,0x40000,0x1200,0x200,};
   }
  final private JJCalls[] jj_2_rtns = new JJCalls[9];
  private boolean jj_rescan = false;
  private int jj_gc = 0;

  /** Constructor with InputStream. */
  public ConfParser(java.io.InputStream stream) {
     this(stream, null);
  }
  /** Constructor with InputStream and supplied encoding */
  public ConfParser(java.io.InputStream stream, String encoding) {
    try { jj_input_stream = new SimpleCharStream(stream, encoding, 1, 1); } catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }
    token_source = new ConfParserTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 7; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  /** Reinitialise. */
  public void ReInit(java.io.InputStream stream) {
     ReInit(stream, null);
  }
  /** Reinitialise. */
  public void ReInit(java.io.InputStream stream, String encoding) {
    try { jj_input_stream.ReInit(stream, encoding, 1, 1); } catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 7; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  /** Constructor. */
  public ConfParser(java.io.Reader stream) {
    jj_input_stream = new SimpleCharStream(stream, 1, 1);
    token_source = new ConfParserTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 7; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  /** Reinitialise. */
  public void ReInit(java.io.Reader stream) {
    jj_input_stream.ReInit(stream, 1, 1);
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 7; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  /** Constructor with generated Token Manager. */
  public ConfParser(ConfParserTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 7; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  /** Reinitialise. */
  public void ReInit(ConfParserTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 7; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  private Token jj_consume_token(int kind) throws ParseException {
    Token oldToken;
    if ((oldToken = token).next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    if (token.kind == kind) {
      jj_gen++;
      if (++jj_gc > 100) {
        jj_gc = 0;
        for (int i = 0; i < jj_2_rtns.length; i++) {
          JJCalls c = jj_2_rtns[i];
          while (c != null) {
            if (c.gen < jj_gen) c.first = null;
            c = c.next;
          }
        }
      }
      return token;
    }
    token = oldToken;
    jj_kind = kind;
    throw generateParseException();
  }

  @SuppressWarnings("serial")
  static private final class LookaheadSuccess extends java.lang.Error { }
  final private LookaheadSuccess jj_ls = new LookaheadSuccess();
  private boolean jj_scan_token(int kind) {
    if (jj_scanpos == jj_lastpos) {
      jj_la--;
      if (jj_scanpos.next == null) {
        jj_lastpos = jj_scanpos = jj_scanpos.next = token_source.getNextToken();
      } else {
        jj_lastpos = jj_scanpos = jj_scanpos.next;
      }
    } else {
      jj_scanpos = jj_scanpos.next;
    }
    if (jj_rescan) {
      int i = 0; Token tok = token;
      while (tok != null && tok != jj_scanpos) { i++; tok = tok.next; }
      if (tok != null) jj_add_error_token(kind, i);
    }
    if (jj_scanpos.kind != kind) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) throw jj_ls;
    return false;
  }


/** Get the next Token. */
  final public Token getNextToken() {
    if (token.next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    jj_gen++;
    return token;
  }

/** Get the specific Token. */
  final public Token getToken(int index) {
    Token t = token;
    for (int i = 0; i < index; i++) {
      if (t.next != null) t = t.next;
      else t = t.next = token_source.getNextToken();
    }
    return t;
  }

  private int jj_ntk_f() {
    if ((jj_nt=token.next) == null)
      return (jj_ntk = (token.next=token_source.getNextToken()).kind);
    else
      return (jj_ntk = jj_nt.kind);
  }

  private java.util.List<int[]> jj_expentries = new java.util.ArrayList<int[]>();
  private int[] jj_expentry;
  private int jj_kind = -1;
  private int[] jj_lasttokens = new int[100];
  private int jj_endpos;

  private void jj_add_error_token(int kind, int pos) {
    if (pos >= 100) return;
    if (pos == jj_endpos + 1) {
      jj_lasttokens[jj_endpos++] = kind;
    } else if (jj_endpos != 0) {
      jj_expentry = new int[jj_endpos];
      for (int i = 0; i < jj_endpos; i++) {
        jj_expentry[i] = jj_lasttokens[i];
      }
      jj_entries_loop: for (java.util.Iterator<?> it = jj_expentries.iterator(); it.hasNext();) {
        int[] oldentry = (int[])(it.next());
        if (oldentry.length == jj_expentry.length) {
          for (int i = 0; i < jj_expentry.length; i++) {
            if (oldentry[i] != jj_expentry[i]) {
              continue jj_entries_loop;
            }
          }
          jj_expentries.add(jj_expentry);
          break jj_entries_loop;
        }
      }
      if (pos != 0) jj_lasttokens[(jj_endpos = pos) - 1] = kind;
    }
  }

  /** Generate ParseException. */
  public ParseException generateParseException() {
    jj_expentries.clear();
    boolean[] la1tokens = new boolean[23];
    if (jj_kind >= 0) {
      la1tokens[jj_kind] = true;
      jj_kind = -1;
    }
    for (int i = 0; i < 7; i++) {
      if (jj_la1[i] == jj_gen) {
        for (int j = 0; j < 32; j++) {
          if ((jj_la1_0[i] & (1<<j)) != 0) {
            la1tokens[j] = true;
          }
        }
      }
    }
    for (int i = 0; i < 23; i++) {
      if (la1tokens[i]) {
        jj_expentry = new int[1];
        jj_expentry[0] = i;
        jj_expentries.add(jj_expentry);
      }
    }
    jj_endpos = 0;
    jj_rescan_token();
    jj_add_error_token(0, 0);
    int[][] exptokseq = new int[jj_expentries.size()][];
    for (int i = 0; i < jj_expentries.size(); i++) {
      exptokseq[i] = jj_expentries.get(i);
    }
    return new ParseException(token, exptokseq, tokenImage);
  }

  /** Enable tracing. */
  final public void enable_tracing() {
  }

  /** Disable tracing. */
  final public void disable_tracing() {
  }

  private void jj_rescan_token() {
    jj_rescan = true;
    for (int i = 0; i < 9; i++) {
    try {
      JJCalls p = jj_2_rtns[i];
      do {
        if (p.gen > jj_gen) {
          jj_la = p.arg; jj_lastpos = jj_scanpos = p.first;
          switch (i) {
            case 0: jj_3_1(); break;
            case 1: jj_3_2(); break;
            case 2: jj_3_3(); break;
            case 3: jj_3_4(); break;
            case 4: jj_3_5(); break;
            case 5: jj_3_6(); break;
            case 6: jj_3_7(); break;
            case 7: jj_3_8(); break;
            case 8: jj_3_9(); break;
          }
        }
        p = p.next;
      } while (p != null);
      } catch(LookaheadSuccess ls) { }
    }
    jj_rescan = false;
  }

  private void jj_save(int index, int xla) {
    JJCalls p = jj_2_rtns[index];
    while (p.gen > jj_gen) {
      if (p.next == null) { p = p.next = new JJCalls(); break; }
      p = p.next;
    }
    p.gen = jj_gen + xla - jj_la; p.first = token; p.arg = xla;
  }

  static final class JJCalls {
    int gen;
    Token first;
    int arg;
    JJCalls next;
  }

}
