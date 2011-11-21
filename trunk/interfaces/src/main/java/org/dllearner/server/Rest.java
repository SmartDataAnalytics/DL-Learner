package org.dllearner.server;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;


public class Rest extends HttpServlet {
    private static Logger log = LoggerFactory.getLogger(Rest.class);

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
     * @throws ServletException
     * @throws java.io.IOException
     */
    private void handle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        try {
            String conf = null;
            if (!isSet("conf", httpServletRequest)) {
                throw new IllegalArgumentException("Missing parameter: conf is required. ");
            } else {
                conf = httpServletRequest.getParameter("conf");
            }

            /*todo learn
             Description  d = learn (conf);
             String concept = d.getConceptAsString ();
             String sparql = new SPARLQConverter().convert(d);

            */
            String concept = "hasCar some (ClosedCar and ShortCar)";
            String sparql  = "SELECT ?instances { ?instances :hasCar ?o .  ?o rdf:type :ClosedCar. ?o2 rdf:type  :ShortCar ";

            String result="{\"concept\": \""+concept+"\", \"SPARQL\":\""+sparql+"\"}";
            result+="\n\nconf was:\n"+conf;

            httpServletResponse.setContentType("text/plain");
            PrintWriter out = httpServletResponse.getWriter();
            out.println(result);
            out.close();

        } catch (IllegalArgumentException e) {
            String msg = e.getMessage();// + printParameterMap(httpServletRequest);
            log.error(msg);
            httpServletResponse.setContentType("text/plain");
            PrintWriter out = httpServletResponse.getWriter();
            out.println(msg);
            out.close();

        } catch (Exception e) {
            String msg = "An error occured: " + e.getMessage(); //+ printParameterMap(httpServletRequest);
            log.error(msg, e);
            httpServletResponse.setContentType("text/plain");
            PrintWriter out = httpServletResponse.getWriter();
            out.println(msg);
            out.close();

        }

    }


    public static String requiredParameter(String parameterName, HttpServletRequest hsr) {

        if (!isSet(parameterName, hsr)) {
            throw new IllegalArgumentException("Missing parameter: " + parameterName + " is required. ");
        }
        return hsr.getParameter(parameterName);
    }

    public static String requiredParameter(String parameterName, HttpServletRequest hsr, String... requiredValues) {
        String value = requiredParameter(parameterName, hsr);
        if (!oneOf(value, requiredValues)) {
            throw new InvalidParameterException("Wrong value for parameter " + parameterName + ", value was: " + value + ", but must be one of ( " + StringUtils.join(requiredValues, ", ") + " ) ");
        }
        return value;
    }


    public static String getDocumentation(String serviceUrl) {
        String doc = "";
        try {
            doc = "\nExample1: \n " + serviceUrl + "?input=" + URLEncoder.encode("That's a lot of nuts! That'll be four bucks, baby! You want fries with that? ", "UTF-8") + "&type=text";
            doc += "\nExample2: \n " + serviceUrl + "?input=" + URLEncoder.encode("That's a lot of nuts! That's a lot of nuts! ", "UTF-8") + "&type=text";
        } catch (Exception e) {
            log.error("", e);
        }
        return doc;
    }

    public static boolean oneOf(String value, String... possibleValues) {
        for (String s : possibleValues) {
            if (s.equals(value)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isSet(String parameterName, HttpServletRequest hsr) {
        boolean retVal = hsr.getParameterValues(parameterName) != null && hsr.getParameterValues(parameterName).length == 1 && hsr.getParameter(parameterName).length() > 0;
        if (log.isTraceEnabled()) {
            log.trace("Parameter " + parameterName + " isSet: " + retVal + " with value: " + hsr.getParameter(parameterName) + ")");
        }
        return retVal;
    }

    public static Map<String, String> copyParameterMap(HttpServletRequest httpServletRequest) {
        Map<String, String> ret = new HashMap<String, String>();
        for (Object key : httpServletRequest.getParameterMap().keySet()) {
            ret.put((String) key, httpServletRequest.getParameter((String) key));
        }
        return ret;
    }


}
