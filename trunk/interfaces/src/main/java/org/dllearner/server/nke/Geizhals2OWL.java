package org.dllearner.server.nke;


import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.vocabulary.OWL;
import org.aksw.commons.jena.ClassIndexer;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.File;
import java.security.InvalidParameterException;
import java.util.*;

/**
 * @author Sebastian Hellmann - http://bis.informatik.uni-leipzig.de/SebastianHellmann
 *         Created: 15.06.11
 */
public class Geizhals2OWL {
    private static Logger log = Logger.getLogger(Geizhals2OWL.class);
    public static final OntModel labels = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM, ModelFactory.createDefaultModel());
    public static final ClassIndexer index = new ClassIndexer();
    public static final Geizhals2OWL geizhals2OWL = new Geizhals2OWL();
    public static final LogicalRelationStrategy lrs = new LogicalRelationStrategy(new File("store.owl").toURI().toString());



    public static final Map<String, String> ramMap = new HashMap<String, String>();
    public static final Map<String, String> harddriveMap = new HashMap<String, String>();
    public static final Map<String, String> optischesLaufwerkMap = new HashMap<String, String>();
    public static final String prefix = "http://nke.aksw.org/geizhals/_";
    public static final String prefixSave = "http://nke.aksw.org/geizhals/user_generated/";

    public static final String retrievalIdPrefix =  "?cat=nb15w&xf=";

    //public static final String prefix = "http://geizhals.at/?cat=nb15w&xf=";

    static {

        labels.read(Geizhals2OWL.class.getClassLoader().getResourceAsStream("nke/geizhals.owl"), "");
        index.index(labels);

        ramMap.put("512MB", "12_512");
        ramMap.put("1024MB", "12_1024");
        ramMap.put("2048MB", "12_2048");
        ramMap.put("3072MB", "12_3072");
        ramMap.put("4096MB", "12_4096");
        ramMap.put("6144MB", "12_6144");
        ramMap.put("8192MB", "12_8192");
        ramMap.put("unbekannt", "12_unbekannt");

        harddriveMap.put("32GB", "11_32");
        harddriveMap.put("80GB", "11_80");
        harddriveMap.put("120GB", "11_120");
        harddriveMap.put("128GB", "11_128");
        harddriveMap.put("160GB", "11_160");
        harddriveMap.put("250GB", "11_250");
        harddriveMap.put("256GB", "11_256");
        harddriveMap.put("320GB", "11_320");
        harddriveMap.put("500GB", "11_500");
        harddriveMap.put("640GB", "11_640");
        harddriveMap.put("1000GB", "11_1000");
        harddriveMap.put("sonstige", "11_sonstige");
        harddriveMap.put("unbekannt", "11_unbekannt");

        optischesLaufwerkMap.put("kein optisches Laufwerk", "84_ohne");
        optischesLaufwerkMap.put("DVD/CD-RW Combo", "84_DVD%2FCD-RW+Combo");
        optischesLaufwerkMap.put("DVD+/-RW DL", "84_DVD%2B%2F-RW");
        optischesLaufwerkMap.put("DVD /-RW DL", "84_DVD%2B%2F-RW");
        optischesLaufwerkMap.put("DVD+/-RW", "84_DVD%2B%2F-RW");
        optischesLaufwerkMap.put("Blu-ray (BD-ROM) und DVD+/-RW DL", "84_Blu-ray+(BD-ROM)");

        //$subs[] = array("84_Blu-ray+(BD-R%2FRE)","84_Blu-ray+(BD-R%2FRE%2FRW)","84_Blu-ray+(BD-ROM)","84_DVD%2B%2F-RW","84_DVD-ROM","84_DVD%2FCD-RW+Combo","84_ohne","84_unbekannt","");
    }


    public static Geizhals2OWL getInstance() {
        return geizhals2OWL;
    }

    public static LogicalRelationStrategy getLRS() {
        return lrs;
    }

    public List<String> convertFeatureString2Classes(String productdesc) {
        List<String> classes = new ArrayList<String>();
        String[] features = productdesc.split(" • ");
        String cpu = features[0].trim();

        /*
        * RAM
        * */
        String ram = features[1].trim();
        ram = ram.substring(0, ram.indexOf("MB") + 2);
        add(classes, ramMap, ram);

        /*
        * Hard drive
        * */
        try {
            String hd = features[2].trim();
            if (hd.contains("Flash")) {
                classes.add(prefix + "82_Flash");
            } else if (hd.contains("SSD")) {
                classes.add(prefix + "82_SSD");
            } else {
                classes.add(prefix + "82_HDD");
            }
            hd = hd.substring(0, hd.indexOf("GB") + 2);
            add(classes, harddriveMap, hd);
        } catch (Exception e) {
            log.warn("Handling hd failed: " + features[2].trim());
        }
        String disc = features[3].trim();
        add(classes, optischesLaufwerkMap, disc);

        //4 =Intel GMA X4500HD (IGP) max.384MB shared memory
        //5 =3x USB 2.0/FireWire/Modem/Gb LAN/WLAN 802.11agn/Bluetooth

        /*
        * this is skipping some optional values
        * */
        int x = 6;
        while (!features[x].contains("\"")) {
            x++;
        }

        /*
        * DISPLAY
        * */
        // 13.3\" WXGA glare LED TFT (1366x768) " +
        String[] display = features[x++].split(" ");
        classes.add(prefix + "85_" + display[0].replace("\"", ""));
        //display[display.length-1];

        /*
       * Operating System
       * */
        // Windows 7 Home Premium (64-bit)
        //String next = features[x];
        //System.out.println(next);
        //String next1 = features[x+1];
        //System.out.println(next1);

        return classes;
    }


    public static void add(List<String> classes, Map<String, String> map, String key) {
        String val = null;
        if ((val = map.get(key)) == null) {
            log.warn("No value found for: |" + key + "|");
            return;
        } else {
            //log.info("Adding " + val);
            classes.add(prefix + val);
        }

    }

    public class Result {
        public Set<String> pos = new HashSet<String>();
        public Set<String> neg = new HashSet<String>();
        private OntModel model;

        public Result(OntModel model) {
            this.model = model;
        }

        public OntModel getModel() {
            return model;
        }
    }


    public Result handleJson(String json) {
        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM, ModelFactory.createDefaultModel());
        Result r = new Result(model);
        try {
            JSONObject j = (JSONObject) JSONValue.parseWithException(json);
            JSONArray pos = (JSONArray) j.get("pos");
            JSONArray neg = (JSONArray) j.get("neg");

            if (pos != null) {
                fill(pos, r.pos, model);
            }
            if (neg != null && !neg.isEmpty()) {
                fill(neg, r.neg, model);
            } else {
                String negative = "http://negative.org/fake";
                r.neg.add(negative);
                model.createIndividual(negative, OWL.Thing);
            }
        } catch (org.json.simple.parser.ParseException e) {
            int position = e.getPosition();
            String msg = "Parsing the JSON string failed\n" + e.toString() + "\n";
            String before = (position >= 30) ? json.substring(position - 30, position) : json.substring(0, position);
            String after = (position + 30 < json.length()) ? json.substring(position, position + 29) : json.substring(position);
            msg += "String before position " + position + ": " + before + "\n" + "String after position " + position + ": " + after + "\n" + "JSON was:\n" + json;
            log.error(msg, e);
            throw new InvalidParameterException(msg);
        }
        return r;

    }

    private void fill(JSONArray arr, Set<String> l, OntModel model) {
        for (Object o : arr) {
            JSONArray one = (JSONArray) o;
            String uri = one.get(0).toString();
            String title = one.get(1).toString();
            String featureText = one.get(2).toString();

            l.add(uri);

            List<String> classes = convertFeatureString2Classes(featureText);
            for (String c : classes) {
                model.createIndividual(uri, model.createClass(c));
                Model m = index.getHierarchyForClassURI(c);
                if (m == null) {
                    log.warn("received null for " + c);
                } else {
                    model.add(m);
                }
            }
        }
    }

}
