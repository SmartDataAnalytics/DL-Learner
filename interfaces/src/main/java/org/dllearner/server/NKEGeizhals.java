package org.dllearner.server;

import com.hp.hpl.jena.ontology.OntClass;
import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;
import org.apache.log4j.Logger;
import org.dllearner.core.ComponentManager;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.learningproblems.EvaluatedDescriptionPosNeg;
import org.dllearner.server.nke.Geizhals2OWL;
import org.dllearner.server.nke.Learner;
import org.dllearner.server.nke.LogicalRelationStrategy;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.InvalidParameterException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;


public class NKEGeizhals extends HttpServlet {
    private static Logger log = Logger.getLogger(NKEGeizhals.class);

    int requestcount = 0;


    @Override
    protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        handle(httpServletRequest, httpServletResponse);
    }

    @Override
    protected void doPost(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        handle(httpServletRequest, httpServletResponse);
    }


    /**
     * *
     *
     * @param httpServletRequest
     * @param httpServletResponse
     * @throws javax.servlet.ServletException
     * @throws java.io.IOException
     */
    private void handle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        ComponentManager cm = ComponentManager.getInstance();
        cm.freeAllComponents();

        Monitor mon = MonitorFactory.getTimeMonitor(this.getClass().getSimpleName()).start();

        JSONObject result = new JSONObject();

        try {

            String action = "";
            if (isSet(httpServletRequest, "action")) {
                action = httpServletRequest.getParameter("action");
                //use the function one of
                if (!oneOf(action, "learn", "feedback", "mostpopular")) {
                    throw new InvalidParameterException("Wrong parameter value for \"action\", must be one of ( learn, feedback, mostpopular ) " + getDocumentation(httpServletRequest));
                }
            } else {
                throw new InvalidParameterException("No parameter 'action' found. " + getDocumentation(httpServletRequest));
            }

            if (action.equalsIgnoreCase("learn")) {

                if (isSet(httpServletRequest, "debug")) {
                    String debugResult = "{\"time\":\"needed: 2896.0 ms. (2896.0 total)\",\"learned\":{\"kbsyntax\":\"(\\\"http:\\/\\/nke.aksw.org\\/geizhals\\/_84_DVD%2B%2F-RW\\\" AND \\\"http:\\/\\/nke.aksw.org\\/geizhals\\/_85_12.5~85_13~85_13.3~85_13.3~85_13.4\\\")\",\"link\":\"http:\\/\\/geizhals.at\\/?cat=nb15w&xf=84_DVD%2B%2F-RW~85_12.5~85_13~85_13.3~85_13.3~85_13.4\",\"trueNegatives\":[\"http://test.de/n1\",\"http://test.de/n2\"],\"truePositives\":[\"http://test.de/p1\",\"http://test.de/p2\"],\"label\":\"(DVD+\\/-RW and 12.5&quot; till 13.4&quot;)\",\"falsePositives\":[],\"falseNegatives\":[]},\"related\":[{\"kbsyntax\":\"(\\\"http:\\/\\/nke.aksw.org\\/geizhals\\/_84_DVD%2B%2F-RW\\\" AND \\\"http:\\/\\/nke.aksw.org\\/geizhals\\/_85_12.5~85_13~85_13.3~85_13.3~85_13.4\\\")\",\"link\":\"http:\\/\\/geizhals.at\\/?cat=nb15w&xf=84_DVD%2B%2F-RW~85_12.5~85_13~85_13.3~85_13.3~85_13.4\",\"trueNegatives\":[\"http://test.de/n1\",\"http://test.de/n2\"],\"truePositives\":[\"http://test.de/p1\",\"http://test.de/p2\"],\"label\":\"(DVD+\\/-RW and 12.5&quot; till 13.4&quot;)\",\"falsePositives\":[],\"falseNegatives\":[]},{\"kbsyntax\":\"(\\\"http:\\/\\/nke.aksw.org\\/geizhals\\/_84_DVD%2B%2F-RW\\\" AND \\\"http:\\/\\/nke.aksw.org\\/geizhals\\/_85_12.5~85_13~85_13.3~85_13.3~85_13.4\\\")\",\"link\":\"http:\\/\\/geizhals.at\\/?cat=nb15w&xf=84_DVD%2B%2F-RW~85_12.5~85_13~85_13.3~85_13.3~85_13.4\",\"trueNegatives\":[\"http://test.de/n1\",\"http://test.de/n2\"],\"truePositives\":[\"http://test.de/p1\",\"http://test.de/p2\"],\"label\":\"(DVD+\\/-RW and 12.5&quot; till 13.4&quot;)\",\"falsePositives\":[],\"falseNegatives\":[]}],\"success\":true,\"up\":[{\"kbsyntax\":\"(\\\"http:\\/\\/nke.aksw.org\\/geizhals\\/_84_DVD%2B%2F-RW\\\" AND \\\"http:\\/\\/nke.aksw.org\\/geizhals\\/_85_12.5~85_13~85_13.3~85_13.3~85_13.4\\\")\",\"link\":\"http:\\/\\/geizhals.at\\/?cat=nb15w&xf=84_DVD%2B%2F-RW~85_12.5~85_13~85_13.3~85_13.3~85_13.4\",\"trueNegatives\":[\"http://test.de/n1\",\"http://test.de/n2\"],\"truePositives\":[\"http://test.de/p1\",\"http://test.de/p2\"],\"label\":\"(DVD+\\/-RW and 12.5&quot; till 13.4&quot;)\",\"falsePositives\":[],\"falseNegatives\":[]},{\"kbsyntax\":\"(\\\"http:\\/\\/nke.aksw.org\\/geizhals\\/_84_DVD%2B%2F-RW\\\" AND \\\"http:\\/\\/nke.aksw.org\\/geizhals\\/_85_12.5~85_13~85_13.3~85_13.3~85_13.4\\\")\",\"link\":\"?cat=nb15w&xf=84_DVD%2B%2F-RW~85_12.5~85_13~85_13.3~85_13.3~85_13.4\",\"trueNegatives\":[\"http://test.de/n1\",\"http://test.de/n2\"],\"truePositives\":[\"http://test.de/p1\",\"http://test.de/p2\"],\"label\":\"(DVD+\\/-RW and 12.5&quot; till 13.4&quot;)\",\"falsePositives\":[],\"falseNegatives\":[]}]}";
                    result = (JSONObject) JSONValue.parseWithException(debugResult);
                } else if (isSet(httpServletRequest, "data")) {
                    String json = httpServletRequest.getParameter("data");
                    actionLearn(json, result);
                } else {
                    throw new InvalidParameterException("No parameter 'data' found. " + getDocumentation(httpServletRequest));
                }
            }

            if (action.equalsIgnoreCase("feedback")) {

                if (isSet(httpServletRequest, "data")) {

                    String json = "";
                    if (isSet(httpServletRequest, "debug")) {
                        if (new Random().nextBoolean()) {
                            json = "{\"kbsyntax\":\"(\\\"http:\\/\\/nke.aksw.org\\/geizhals\\/_84_DVD%2B%2F-RW\\\" AND \\\"http:\\/\\/nke.aksw.org\\/geizhals\\/_85_12.5~85_13~85_13.3~85_13.3~85_13.4\\\")\",\"link\":\"?cat=nb15w&xf=84_DVD%2B%2F-RW~85_12.5~85_13~85_13.3~85_13.3~85_13.4\",\"label\":\"(DVD+\\/-RW and 12.5&quot; till 13.4&quot;)\"}";
                        } else {
                            json = "{\"kbsyntax\":\"(\\\"http:\\/\\/nke.aksw.org\\/geizhals\\/_11_500\\\" AND \\\"http:\\/\\/nke.aksw.org\\/geizhals\\/_85_12.5~85_-+16.4\\\")\",\"link\":\"?cat=nb15w&xf=11_500~85_12.5~85_-+16.4\",\"label\":\"(ab 500GB and 12.5&quot; till 16.4&quot;)\"}";
                        }
                    } else {
                        json = httpServletRequest.getParameter("data");
                    }

                    try {
                        JSONObject j = (JSONObject) JSONValue.parseWithException(json);

                        String kbsyntax = (String) j.get("kbsyntax");

                        if (kbsyntax.contains(" AND ") || kbsyntax.contains(" OR ")) {
                            //save the concept
                            Geizhals2OWL.getLRS().increasePopularity(Geizhals2OWL.prefixSave + j.get("link"), kbsyntax, (String) j.get("link"), (String) j.get("label"));
                            log.info("saved: " + Geizhals2OWL.prefixSave + j.get("link") + " | " + kbsyntax + " | " + (String) j.get("label"));
                        }

                    } catch (org.json.simple.parser.ParseException e) {
                        //TODO this code was copy pasted and could be a function
                        int position = e.getPosition();
                        String msg = "Parsing the JSON string failed\n" + e.toString() + "\n";
                        String before = (position >= 30) ? json.substring(position - 30, position) : json.substring(0, position);
                        String after = (position + 30 < json.length()) ? json.substring(position, position + 29) : json.substring(position);
                        msg += "String before position " + position + ": " + before + "\n" + "String after position " + position + ": " + after + "\n" + "JSON was:\n" + json;
                        log.error(msg, e);
                        throw new InvalidParameterException(msg);
                    }

                } else {
                    throw new InvalidParameterException("No parameter 'data'  found. " + getDocumentation(httpServletRequest));
                }
            }

            if (action.equalsIgnoreCase("mostPopular")) {

                if (isSet(httpServletRequest, "debug")) {
                    String debugResult = "[" + " {\"kbsyntax\":\"(\\\"http:\\/\\/nke.aksw.org\\/geizhals\\/_84_DVD%2B%2F-RW\\\" AND \\\"http:\\/\\/nke.aksw.org\\/geizhals\\/_85_12.5~85_13~85_13.3~85_13.3~85_13.4\\\")\",\"link\":\"?cat=nb15w&xf=84_DVD%2B%2F-RW~85_12.5~85_13~85_13.3~85_13.3~85_13.4\",\"trueNegatives\":[\"http://test.de/n1\",\"http://test.de/n2\"],\"truePositives\":[\"http://test.de/p1\",\"http://test.de/p2\"],\"label\":\"(DVD+\\/-RW and 12.5&quot; till 13.4&quot;)\",\"popularity\": 10,\"falsePositives\":[],\"falseNegatives\":[]} " + ", " + " {\"kbsyntax\":\"(\\\"http:\\/\\/nke.aksw.org\\/geizhals\\/_84_DVD%2B%2F-RW\\\" AND \\\"http:\\/\\/nke.aksw.org\\/geizhals\\/_85_12.5~85_13~85_13.3~85_13.3~85_13.4\\\")\",\"popularity\": 10,\"link\":\"?cat=nb15w&xf=84_DVD%2B%2F-RW~85_12.5~85_13~85_13.3~85_13.3~85_13.4\",\"trueNegatives\":[\"http://test.de/n1\",\"http://test.de/n2\"],\"truePositives\":[\"http://test.de/p1\",\"http://test.de/p2\"],\"label\":\"(DVD+\\/-RW and 12.5&quot; till 13.4&quot;)\",\"falsePositives\":[],\"falseNegatives\":[]} " + ", " + " {\"kbsyntax\":\"(\\\"http:\\/\\/nke.aksw.org\\/geizhals\\/_84_DVD%2B%2F-RW\\\" AND \\\"http:\\/\\/nke.aksw.org\\/geizhals\\/_85_12.5~85_13~85_13.3~85_13.3~85_13.4\\\")\",\"popularity\": 10,\"link\":\"http:\\/\\/geizhals.at\\/?cat=nb15w&xf=84_DVD%2B%2F-RW~85_12.5~85_13~85_13.3~85_13.3~85_13.4\",\"trueNegatives\":[\"http://test.de/n1\",\"http://test.de/n2\"],\"truePositives\":[\"http://test.de/p1\",\"http://test.de/p2\"],\"label\":\"(DVD+\\/-RW and 12.5&quot; till 13.4&quot;)\",\"falsePositives\":[],\"falseNegatives\":[]} " + ", " + " {\"kbsyntax\":\"(\\\"http:\\/\\/nke.aksw.org\\/geizhals\\/_84_DVD%2B%2F-RW\\\" AND \\\"http:\\/\\/nke.aksw.org\\/geizhals\\/_85_12.5~85_13~85_13.3~85_13.3~85_13.4\\\")\",\"link\":\"?cat=nb15w&xf=84_DVD%2B%2F-RW~85_12.5~85_13~85_13.3~85_13.3~85_13.4\",\"trueNegatives\":[\"http://test.de/n1\",\"http://test.de/n2\"],\"truePositives\":[\"http://test.de/p1\",\"http://test.de/p2\"],\"label\":\"(DVD+\\/-RW and 12.5&quot; till 13.4&quot;)\",\"falsePositives\":[],\"falseNegatives\":[]} " + "] ";
                    debugResult = "{\"popular\":" + debugResult + " }";
                    result = (JSONObject) JSONValue.parseWithException(debugResult);
                } else {

                    JSONArray ja = new JSONArray();
                    for (LogicalRelationStrategy.Concept c : Geizhals2OWL.getLRS().getMostPopular(6)) {
                        ja.add(c.getJSON());
                    }

                    result.put("popular", ja);
                }
            }

            //check parameters
            /*String conf = "";
            if (isSet(httpServletRequest, "conf")) {
                conf = httpServletRequest.getParameter("conf");
            } else {
                throw new InvalidParameterException("No parameter 'conf' found. " + getDocumentation(httpServletRequest.getRequestURL().toString()));
            }*/

            result.put("success", true);
        } catch (IllegalArgumentException e) {
            String msg = e.getMessage() + printParameterMap(httpServletRequest);
            log.error(msg, e);
            result.put("success", false);
            result.put("error", msg);

        } catch (Exception e) {
            String msg = "An error occured: " + e.getMessage() + printParameterMap(httpServletRequest);
            log.error(msg, e);
            result.put("success", false);
            result.put("error", msg);

        }

        String time = logMonitor(mon.stop());
        result.put("time", time);

        PrintWriter pw = httpServletResponse.getWriter();
        log.debug("Request handled: " + time);
        pw.print(result.toJSONString());
        pw.close();

        //every 10 requests, write the jamonlog
        if (requestcount++ > 10) {
            requestcount = 0;
            try {
                FileWriter fw = new FileWriter("log/jamon"+getDateTime()+".html");
                fw.write(MonitorFactory.getReport());
                fw.flush();
            } catch (Exception e) {
                log.error("", e);
            }
        }

    }

     private String getDateTime() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();
        return dateFormat.format(date);
    }


    public void actionLearn(String json, JSONObject result) throws Exception {

        Geizhals2OWL.Result r = Geizhals2OWL.getInstance().handleJson(json);
        if (r.pos.isEmpty()) {
            throw new InvalidParameterException("No positive examples were chosen. Press a plus.");
        }

        Set<String> tp = new HashSet<String>(r.pos);
        Set<String> tn = new HashSet<String>(r.neg);
        tp.retainAll(tn);
        tn.retainAll(r.pos);

        if (tp.size() > 0 || tn.size() > 0) {
            throw new InvalidParameterException("The pos and neg examples are not disjoint. \npos: " + r.pos + "\nneg: " + r.neg);
        }

        List<EvaluatedDescriptionPosNeg> eds = new Learner().learn(r.pos, r.neg, r.getModel(), 20);

        EvaluatedDescriptionPosNeg ed = selectDescription(eds);

        List<LogicalRelationStrategy.Concept> related = Geizhals2OWL.getLRS().getRelatedConcepts(ed);

        JSONObject concept = jsonForEd(ed);
        result.put("learned", concept);
        JSONArray up = new JSONArray();
        for (LogicalRelationStrategy.Concept c : related) {

            up.add(c.getJSON());
        }
        result.put("related", up);
    }


    public static JSONObject jsonForEd(EvaluatedDescriptionPosNeg ed) {

        SortedSet<NamedClass> namedClasses = getNamedClasses(ed.getDescription(), new TreeSet<NamedClass>());

        String link = "";
        String label = "";

        if (ed.getDescription().toKBSyntaxString().equals("TOP")) {
            label = "No suggestions for current selection (click to show all)";
            link = "?cat=nb15w";
        } else {
            link = Geizhals2OWL.retrievalIdPrefix + getID(ed.getDescription(), namedClasses);
            label = getLabel(ed.getDescription(), namedClasses);
        }

        JSONObject j = new JSONObject();
        j.put("link", link);
        j.put("label", label);
        j.put("truePositives", EvaluatedDescriptionPosNeg.getJSONArray(ed.getCoveredPositives()));
        j.put("falsePositives", EvaluatedDescriptionPosNeg.getJSONArray(ed.getNotCoveredPositives()));
        j.put("trueNegatives", EvaluatedDescriptionPosNeg.getJSONArray(ed.getNotCoveredNegatives()));
        j.put("falseNegatives", EvaluatedDescriptionPosNeg.getJSONArray(ed.getCoveredNegatives()));
        j.put("kbsyntax", ed.getDescription().toKBSyntaxString());

        log.info(link);
        log.info(ed.toString());
        return j;
    }

    public static EvaluatedDescriptionPosNeg selectDescription(List<EvaluatedDescriptionPosNeg> eds) {
        EvaluatedDescriptionPosNeg ret = eds.get(0);
        int maxNamedClasses = 0;
        int current = 0;
        for (EvaluatedDescriptionPosNeg ed : eds) {
            if (ed.getAccuracy() < 1.0) {
                break;
            } else {
                current = getNamedClasses(ed.getDescription(), new TreeSet<NamedClass>()).size();
                if (current > maxNamedClasses) {
                    maxNamedClasses = current;
                    ret = ed;
                }

            }
        }
        return ret;

    }


    public static String getID(Description d, SortedSet<NamedClass> namedClasses) {

        //prepare retrieval string
        StringBuilder sb = new StringBuilder();
        int x = 0;
        for (NamedClass nc : namedClasses) {
            sb.append(nc.getName().replace(Geizhals2OWL.prefix, ""));
            if (x < (namedClasses.size() - 1)) {
                sb.append("~");
            }
            x++;
        }
        return sb.toString();
    }

    public static String getLabel(Description d, SortedSet<NamedClass> namedClasses) {

        String mos = d.toManchesterSyntaxString(null, null);
        for (NamedClass nc : namedClasses) {
            String label = null;
            OntClass c = null;
            if ((c = Geizhals2OWL.labels.getOntClass(nc.getName())) != null && (label = c.getLabel(null)) != null) {
                mos = mos.replace(nc.getName(), label);
            } else {
                mos = mos.replace(nc.getName(), nc.getName().replace(Geizhals2OWL.prefix, ""));
            }
        }
        return mos;

    }

    public static SortedSet<NamedClass> getNamedClasses(Description d, SortedSet<NamedClass> ret) {
        if (d instanceof NamedClass) {
            ret.add((NamedClass) d);
        }
        for (Description ch : d.getChildren()) {
            getNamedClasses(ch, ret);
        }
        return ret;

    }


    /**
     * Examples are from NIF
     *
     * @return
     */
    public static String getDocumentation(HttpServletRequest request) {
        String doc = "";
        try {
            doc = "Request Url was: " + request.getRequestURL() + "\n";
            //doc = "\nExample1: \n " + serviceUrl + "?input=" + URLEncoder.encode("That's a lot of nuts! That'll be four bucks, baby! You want fries with that? ", "UTF-8") + "&type=text";
            //doc += "\nExample2: \n " + serviceUrl + "?input=" + URLEncoder.encode("That's a lot of nuts! That's a lot of nuts! ", "UTF-8") + "&type=text";
        } catch (Exception e) {
            log.error("", e);
        }
        return doc;
    }

    public static boolean isSet(HttpServletRequest httpServletRequest, String name) {
        log.trace("isSet(" + name + ")");
        log.trace(httpServletRequest.getParameterValues(name) + "");
        return httpServletRequest.getParameterValues(name) != null && httpServletRequest.getParameterValues(name).length == 1 && httpServletRequest.getParameter(name).length() > 0;
    }

    public static boolean oneOf(String value, String... possibleValues) {
        for (String s : possibleValues) {
            if (s.equals(value)) {
                return true;
            }
        }
        return false;
    }

    protected static String logMonitor(Monitor m) {
        return "needed: " + m.getLastValue() + " ms. (" + m.getTotal() + " total)";
    }


    public static String printParameterMap(HttpServletRequest httpServletRequest) {
        StringBuilder buf = new StringBuilder();
        if (httpServletRequest.getParameterMap().keySet().isEmpty()) {
            return "Empty parameters: there were neither post nor get parameters";
        } else {
            buf.append("\nReceived " + httpServletRequest.getParameterMap().size() + " parameters");
        }
        for (Object key : httpServletRequest.getParameterMap().keySet()) {
            buf.append("\nParameter: " + key + " Values: ");
            for (String s : httpServletRequest.getParameterValues((String) key)) {
                //buf.append(((s.length() > 200) ? s.substring(0, 200) + "..." : s) + " ");
                buf.append(s);
            }
        }
        return buf.toString();
    }

}
