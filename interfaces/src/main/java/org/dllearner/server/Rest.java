package org.dllearner.server;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.dllearner.configuration.IConfiguration;
import org.dllearner.configuration.spring.ApplicationContextBuilder;
import org.dllearner.configuration.spring.DefaultApplicationContextBuilder;
import org.dllearner.confparser3.ConfParserConfiguration;
import org.dllearner.core.ClassExpressionLearningAlgorithm;
import org.dllearner.core.LearningAlgorithm;
import org.dllearner.kb.sparql.SparqlQueryDescriptionConvertVisitor;
import org.dllearner.learningproblems.EvaluatedDescriptionPosNeg;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.ArrayList;
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
        JSONObject result = new JSONObject();
        JSONObject learningResult = new JSONObject();
        try {
            String conf = null;
            int limit = 5;
            if (!isSet("conf", httpServletRequest)) {
                throw new IllegalArgumentException("Missing parameter: conf is required. ");
            } else {
                conf = httpServletRequest.getParameter("conf");
                if (isSet("limit", httpServletRequest)) {
                    limit = Integer.parseInt(httpServletRequest.getParameter("limit"));
                }
            }

            if (isSet("debug", httpServletRequest) && httpServletRequest.getParameter("debug").equalsIgnoreCase("true")) {


                String manchester = "author some (Artist and Writer)";
                String sparql = "prefix dbo: <http://dbpedia.org/ontology/>\n" +
                        "SELECT ?instances WHERE {\n" +
                        "?instances dbo:author ?o . ?o a dbo:Artist . ?o a dbo:Writer .\n" +
                        "} ";

                learningResult.put("success", "1");
                learningResult.put("manchester", manchester);
                learningResult.put("kbsyntax", "other syntax");
                learningResult.put("sparql", sparql);
                learningResult.put("accuracy", 1.0);
                learningResult.put("truePositives", "uri1, uri2");
                learningResult.put("truePositives", "uri1, uri2");
                learningResult.put("trueNegatives", "uri1, uri2");
                learningResult.put("falseNegatives", "uri1, uri2");
            } else {

                EvaluatedDescriptionPosNeg ed = learn(conf);

                SparqlQueryDescriptionConvertVisitor sqd = new SparqlQueryDescriptionConvertVisitor();
                sqd.setLimit(limit);

                learningResult.put("success", "1");
                learningResult.put("manchester", ed.getDescription().toManchesterSyntaxString(null, null));
                learningResult.put("kbsyntax", ed.getDescription().toKBSyntaxString());
                learningResult.put("sparql", sqd.getSparqlQuery(ed.getDescription()));
                learningResult.put("accuracy", ed.getAccuracy());
                learningResult.put("truePositives", EvaluatedDescriptionPosNeg.getJSONArray(ed.getCoveredPositives()));
                learningResult.put("falsePositives", EvaluatedDescriptionPosNeg.getJSONArray(ed.getNotCoveredPositives()));
                learningResult.put("trueNegatives", EvaluatedDescriptionPosNeg.getJSONArray(ed.getNotCoveredNegatives()));
                learningResult.put("falseNegatives", EvaluatedDescriptionPosNeg.getJSONArray(ed.getCoveredNegatives()));
            }


        } catch (IllegalArgumentException e) {
            String msg = e.getMessage();// + printParameterMap(httpServletRequest);
            log.error("", ExceptionUtils.getRootCause(e));
            learningResult.put("success", "0");
            learningResult.put("error", msg);
            learningResult.put("stacktrace", ExceptionUtils.getRootCause(e));

        } catch (Exception e) {
            String msg = "An error occured: " + e.getMessage(); //+ printParameterMap(httpServletRequest);
            log.error("", ExceptionUtils.getRootCause(e));
            learningResult.put("success", "0");
            learningResult.put("error", msg);
            learningResult.put("stacktrace", ExceptionUtils.getRootCause(e));
        }

        result.put("learningresult", learningResult);
        httpServletResponse.setContentType("text/plain");
        PrintWriter out = httpServletResponse.getWriter();
        out.println(result.toJSONString());
        out.close();

    }

    /**
     * TODO
     * This function takes the config string as in a conf file and the returns an EvaluatedDescription
     *
     * @param conf the content of a conf file
     * @return
     */
    public EvaluatedDescriptionPosNeg learn(String conf) throws Exception {
        Resource confFile = new InputStreamResource(new ByteArrayInputStream(conf.getBytes()));

        IConfiguration configuration = new ConfParserConfiguration(confFile);

        ApplicationContextBuilder builder = new DefaultApplicationContextBuilder();
        ApplicationContext context = builder.buildApplicationContext(configuration, new ArrayList<Resource>());

        LearningAlgorithm algorithm = context.getBean(LearningAlgorithm.class);
        algorithm.start();
        if (algorithm instanceof ClassExpressionLearningAlgorithm) {
            return (EvaluatedDescriptionPosNeg) ((ClassExpressionLearningAlgorithm) algorithm).getCurrentlyBestEvaluatedDescriptions(1).iterator().next();
        }
        throw new Exception("only ClassExpressionLearningAlgorithm implemented currently");
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

    public static void main(String[] args) throws Exception {
        String filePath = "../examples/father.conf";
        byte[] buffer = new byte[(int) new File(filePath).length()];
        BufferedInputStream f = null;
        try {
            f = new BufferedInputStream(new FileInputStream(filePath));
            f.read(buffer);
        } finally {
            if (f != null) try {
                f.close();
            } catch (IOException ignored) {
            }
        }
        String confString = new String(buffer);

        Resource confFile = new InputStreamResource(new ByteArrayInputStream(confString.getBytes()));

        IConfiguration configuration = new ConfParserConfiguration(confFile);

        ApplicationContextBuilder builder = new DefaultApplicationContextBuilder();
        ApplicationContext context = builder.buildApplicationContext(configuration, new ArrayList<Resource>());

        LearningAlgorithm algorithm = context.getBean(LearningAlgorithm.class);
        algorithm.start();
    }

}
