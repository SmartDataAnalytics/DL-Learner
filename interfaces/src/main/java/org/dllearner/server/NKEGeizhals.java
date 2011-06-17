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
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.security.InvalidParameterException;
import java.util.SortedSet;
import java.util.TreeSet;


public class NKEGeizhals extends HttpServlet {
    private static Logger log = Logger.getLogger(NKEGeizhals.class);


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

        Monitor mon = MonitorFactory.getTimeMonitor("NIFParameters.getInstance").start();
        String result = "";
        try {

            String action = "";
            if (isSet(httpServletRequest, "action")) {
                action = httpServletRequest.getParameter("action");
                //use the function one of
                if (!oneOf(action, "learn", "xml")) {
                    throw new InvalidParameterException("Wrong parameter value for \"action\", must be one of ( learn, xml ) " + getDocumentation(httpServletRequest));
                }
            } else {
                throw new InvalidParameterException("No parameter 'action' found. " + getDocumentation(httpServletRequest));
            }

            if (action.equals("learn")) {
                String json = "";
                if (isSet(httpServletRequest, "data")) {

                    json = httpServletRequest.getParameter("data");
                    Geizhals2OWL.Result r = Geizhals2OWL.getInstance().handleJson(json);
                    EvaluatedDescriptionPosNeg ed = new Learner().learn(r.pos, r.neg, r.getModel(), 20);
                    JSONObject concept = jsonForEd(ed, httpServletRequest.getRequestURL().toString());
                    JSONObject j = new JSONObject();
                    j.put("learned", concept);
                    JSONArray up = new JSONArray();
                    up.add(concept);
                    up.add(concept);
                    j.put("up", up);
                    j.put("down", up);
                    String time = logMonitor(mon.stop());
                    j.put("time", time);

                    PrintWriter pw = httpServletResponse.getWriter();
                    log.debug("Request handled: " + time);
                    pw.print(j.toJSONString());
                    pw.close();
                    return;

                } else {
                    throw new InvalidParameterException("No parameter 'data' found. " + getDocumentation(httpServletRequest));
                }
            }

            //check parameters
            /*String conf = "";
            if (isSet(httpServletRequest, "conf")) {
                conf = httpServletRequest.getParameter("conf");
            } else {
                throw new InvalidParameterException("No parameter 'conf' found. " + getDocumentation(httpServletRequest.getRequestURL().toString()));
            }*/

            PrintWriter pw = httpServletResponse.getWriter();
            log.debug("Request handled: " + logMonitor(mon.stop()));
            pw.print(result);
            pw.close();

        } catch (IllegalArgumentException e) {
            String msg = e.getMessage() + printParameterMap(httpServletRequest);
            log.error(msg);
            httpServletResponse.setContentType("text/plain");
            PrintWriter out = httpServletResponse.getWriter();
            out.println(msg);
            out.close();

        } catch (Exception e) {
            String msg = "An error occured: " + e.getMessage() + printParameterMap(httpServletRequest);
            log.error(msg, e);
            httpServletResponse.setContentType("text/plain");
            PrintWriter out = httpServletResponse.getWriter();
            out.println(msg);
            out.close();

        }

    }


    public static JSONObject jsonForEd(EvaluatedDescriptionPosNeg ed, String requestUrl) {
        SortedSet<NamedClass> namedClasses = getNamedClasses(ed.getDescription(), new TreeSet<NamedClass>());

        String xf = getID(ed.getDescription(), namedClasses);
        String link = "http://geizhals.at/?cat=nb15w&xf="+xf;

        JSONObject j = new JSONObject();
        j.put("link", link);
        j.put("label", getLabel(ed.getDescription(), namedClasses));
        j.put("truePositives", EvaluatedDescriptionPosNeg.getJSONArray(ed.getCoveredPositives()));
        j.put("falsePositives", EvaluatedDescriptionPosNeg.getJSONArray(ed.getNotCoveredPositives()));
        j.put("trueNegatives", EvaluatedDescriptionPosNeg.getJSONArray(ed.getNotCoveredNegatives()));
        j.put("falseNegatives", EvaluatedDescriptionPosNeg.getJSONArray(ed.getCoveredNegatives()));
        j.put("kbsyntax", ed.getDescription().toKBSyntaxString());

        log.info(link);
        log.info(ed.toString());
        return j;
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
    public static String getDocumentation(HttpServletRequest  request) {
        String doc = "";
        try {
            doc = "Requesturl was: "+request.getRequestURL();
            doc += printParameterMap(request);
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
        StringBuffer buf = new StringBuffer();
        for (Object key : httpServletRequest.getParameterMap().keySet()) {
            buf.append("\nParameter: " + key + " Values: ");
            for (String s : httpServletRequest.getParameterValues((String) key)) {
                buf.append(((s.length() > 200) ? s.substring(0, 200) + "..." : s) + " ");
            }
        }
        return buf.toString();
    }

}
