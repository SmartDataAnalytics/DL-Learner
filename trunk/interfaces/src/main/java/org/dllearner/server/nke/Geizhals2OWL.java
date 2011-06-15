package org.dllearner.server.nke;


import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import org.aksw.commons.jena.ClassIndexer;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.util.*;

/**
 * @author Sebastian Hellmann - http://bis.informatik.uni-leipzig.de/SebastianHellmann
 *         Created: 15.06.11
 */
public class Geizhals2OWL {
    private static Logger log = Logger.getLogger(Geizhals2OWL.class);
    public static ClassIndexer index = new ClassIndexer();
    public static Geizhals2OWL geizhals2OWL = new Geizhals2OWL();

    public static Map<String, String> ramMap = new HashMap<String, String>();
    public static Map<String, String> hdMap = new HashMap<String, String>();
    public static Map<String, String> discMap = new HashMap<String, String>();
    public static String prefix = "http://nke.aksw.org/_";

    static {

        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM, ModelFactory.createDefaultModel());
        model.read(Geizhals2OWL.class.getClassLoader().getResourceAsStream("nke/geizhals.owl"), "");
        index.index(model);

        ramMap.put("512MB", "12_512");
        ramMap.put("1024MB", "12_1024");
        ramMap.put("2048MB", "12_2048");
        ramMap.put("3072MB", "12_3072");
        ramMap.put("4096MB", "12_4096");
        ramMap.put("6144MB", "12_6144");
        ramMap.put("8192MB", "12_8192");
        ramMap.put("unbekannt", "12_unbekannt");

        hdMap.put("32GB", "11_32");
        hdMap.put("80GB", "11_80");
        hdMap.put("120GB", "11_120");
        hdMap.put("128GB", "11_128");
        hdMap.put("160GB", "11_160");
        hdMap.put("250GB", "11_250");
        hdMap.put("256GB", "11_256");
        hdMap.put("320GB", "11_320");
        hdMap.put("500GB", "11_500");
        hdMap.put("640GB", "11_640");
        hdMap.put("1000GB", "11_1000");
        hdMap.put("sonstige", "11_sonstige");
        hdMap.put("unbekannt", "11_unbekannt");

        discMap.put("kein optisches Laufwerk", "84_ohne");
        discMap.put("DVD/CD-RW Combo", "84_DVD%2FCD-RW+Combo");
        discMap.put("DVD+/-RW DL", "84_DVD%2B%2F-RW");
        discMap.put("DVD+/-RW", "84_DVD%2B%2F-RW");
        discMap.put("Blu-ray (BD-ROM) und DVD+/-RW DL", "84_Blu-ray+(BD-ROM)");

        //$subs[] = array("84_Blu-ray+(BD-R%2FRE)","84_Blu-ray+(BD-R%2FRE%2FRW)","84_Blu-ray+(BD-ROM)","84_DVD%2B%2F-RW","84_DVD-ROM","84_DVD%2FCD-RW+Combo","84_ohne","84_unbekannt","");
    }


    public static Geizhals2OWL getInstance() {
        return geizhals2OWL;
    }

    public List<String> convertFeatureString2Classes(String productdesc) {
        List<String> classes = new ArrayList<String>();
        String[] features = productdesc.split(" • ");
        String cpu = features[0].trim();

        String ram = features[1].trim();
        ram = ram.substring(0, ram.indexOf("MB") + 2);
        add(classes, ramMap, ram);

        try {
            String hd = features[2].trim();
            if (hd.contains("Flash")) {
                classes.add(prefix+"82_Flash");
            } else if (hd.contains("SSD")) {
                classes.add(prefix+"82_SSD");
            } else {
                classes.add(prefix+"82_HDD");
            }
            hd = hd.substring(0, hd.indexOf("GB") + 2);
            add(classes, hdMap, hd);
        } catch (Exception e) {
            log.warn("Handling hd failed: " + features[2].trim());
        }
        String disc = features[3].trim();
        add(classes, discMap, disc);

        //4 =Intel GMA X4500HD (IGP) max.384MB shared memory
        //5 =3x USB 2.0/FireWire/Modem/Gb LAN/WLAN 802.11agn/Bluetooth

        int x = 6;
        while (!features[x].contains("\"")) {
            x++;
        }

        //String next = features[x];
        //System.out.println(next);
        //String next1 = features[x+1];
        //System.out.println(next1);

        return classes;
    }


    public static void add(List<String> classes, Map<String, String> map, String key) {
        String val = null;
        if ((val = map.get(key)) == null) {
            log.warn("No value found for: " + key);
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

        JSONObject j = (JSONObject) JSONValue.parse(json);
        JSONArray pos = (JSONArray) j.get("pos");
        JSONArray neg = (JSONArray) j.get("neg");

        System.out.println(j);

        fill(pos, r.pos, model);
        fill(neg, r.neg, model);
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
                    log.warn("recieved null for " + c);
                }else{
                    model.add(m);
                }
            }
        }
    }

}
