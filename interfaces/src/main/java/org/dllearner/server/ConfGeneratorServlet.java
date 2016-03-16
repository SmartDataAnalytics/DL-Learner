/**
 * 
 */
package org.dllearner.server;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.util.*;

/**
 * @author didier
 * 
 */
public class ConfGeneratorServlet extends HttpServlet {
    
    private Logger logger = LoggerFactory.getLogger(ConfGeneratorServlet.class);
    
    private String template;
    
    public ConfGeneratorServlet() {
        BufferedReader input = new BufferedReader(new InputStreamReader(ConfGeneratorServlet.class
                .getClassLoader().getResourceAsStream("config.template")));
        StringBuilder builder = new StringBuilder();
        try {
            while (input.ready()) {
                builder.append(input.readLine());
                builder.append("\n");
            }
        } catch (IOException e) {
            logger.error("", ExceptionUtils.getRootCause(e));
        } finally {
            try {
                input.close();
            } catch (IOException e) {
                logger.error("", ExceptionUtils.getRootCause(e));
            }
        }
        template = builder.toString();
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest
     * , javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
            IOException {
        handle(req, resp);
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest
     * , javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        handle(req, resp);
    }
    
    /**
     * @param req
     * @param resp
     */
    private void handle(HttpServletRequest req, HttpServletResponse resp) {
        String tmp = template;
        try {
            String[] pos, neg = null;
            if (!Rest.isSet("pos", req)) {
                System.out.println("blub");
                req.getRequestDispatcher("/WEB-INF/sparqr.html").forward(req, resp);
                return;
            } else {
                pos = URLDecoder.decode(req.getParameter("pos"), "UTF-8").split(",");
                if (Rest.isSet("neg", req)) {
                    neg = URLDecoder.decode(req.getParameter("neg"), "UTF-8").split(",");
                }
            }
            StringBuilder posStr = new StringBuilder();
            StringBuilder negStr = new StringBuilder();
            StringBuilder instances = new StringBuilder();
            
            if (neg != null) {
                tmp=tmp.replace("<LPTYPE>", "\"posNegStandard\"");
                for (int i = 0; i < neg.length; i++) {
                    if (i > 0) {
                        negStr.append(",\n");
                        instances.append(",\n");
                    }
                        negStr.append("\"");
                        negStr.append(neg[i].replaceAll("\"|\n|\\s", ""));
                        negStr.append("\"");
                        instances.append("\"");
                        instances.append(neg[i].replaceAll("\"|\n|\\s", ""));
                        instances.append("\"");
                }
            } else {
                tmp=tmp.replace("<LPTYPE>", "\"posOnlyLP\"");
                tmp=tmp.replace("lp.negativeExamples = {\n<NEGATIVES>\n} ", "");
            }
            
            for (int i = 0; i < pos.length; i++) {
                if (i > 0) {
                    posStr.append(",\n");
                }
                if (instances.length() > 0) {
                    instances.append(",\n");
                }
                    posStr.append("\"");
                    posStr.append(pos[i].replaceAll("\"|\n|\\s", ""));
                    posStr.append("\"");
                    instances.append("\"");
                    instances.append(pos[i].replaceAll("\"|\n|\\s", ""));
                    instances.append("\"");
            }
            tmp=tmp.replace("<INSTANCES>", instances.toString());
            tmp=tmp.replace("<POSITIVES>", posStr.toString());
            tmp=tmp.replace("<NEGATIVES>", negStr.toString());
            Map<String, String[]> additionalParams = new HashMap<>();
            additionalParams.put("conf", new String[]{tmp});
            System.out.println(tmp);
            ModifiableWrappedRequest request = new ModifiableWrappedRequest(req, additionalParams);
            request.getRequestDispatcher("/rest").forward(request, resp);
        } catch (ServletException | IOException e) {
            logger.error("", ExceptionUtils.getRootCause(e));
            try {
                resp.sendError(500, ExceptionUtils.getRootCause(e).toString());
            } catch (IOException e1) {
                logger.error("", ExceptionUtils.getRootCause(e1));
            }
        }
    }
    
    public class ModifiableWrappedRequest extends HttpServletRequestWrapper
    {
        private final Map<String, String[]> modifiableParameters;
        private Map<String, String[]> allParameters = null;

        /**
         * Create a new request wrapper that will merge additional parameters into
         * the request object without prematurely reading parameters from the
         * original request.
         * 
         * @param request
         * @param additionalParams
         */
        public ModifiableWrappedRequest(final HttpServletRequest request, 
                                                        final Map<String, String[]> additionalParams)
        {
            super(request);
            modifiableParameters = new TreeMap<>();
            modifiableParameters.putAll(additionalParams);
        }

        @Override
        public String getParameter(final String name)
        {
            String[] strings = getParameterMap().get(name);
            if (strings != null)
            {
                return strings[0];
            }
            return super.getParameter(name);
        }

        @Override
        public Map<String, String[]> getParameterMap()
        {
            if (allParameters == null)
            {
                allParameters = new TreeMap<>();
                allParameters.putAll(super.getParameterMap());
                allParameters.putAll(modifiableParameters);
            }
            //Return an unmodifiable collection because we need to uphold the interface contract.
            return Collections.unmodifiableMap(allParameters);
        }

        @Override
        public Enumeration<String> getParameterNames()
        {
            return Collections.enumeration(getParameterMap().keySet());
        }

        @Override
        public String[] getParameterValues(final String name)
        {
            return getParameterMap().get(name);
        }
    }

}
