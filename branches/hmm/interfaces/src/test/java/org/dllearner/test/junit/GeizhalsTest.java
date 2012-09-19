package org.dllearner.test.junit;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;
import org.dllearner.learningproblems.EvaluatedDescriptionPosNeg;
import org.dllearner.reasoning.FastInstanceChecker;
import org.dllearner.reasoning.OWLAPIReasoner;
import org.dllearner.server.nke.Geizhals2OWL;
import org.dllearner.server.nke.Learner;
import org.dllearner.utilities.Helper;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Sebastian Hellmann - http://bis.informatik.uni-leipzig.de/SebastianHellmann
 *         Created: 15.06.11
 */
public class GeizhalsTest {


    public static String[] featureDesc = new String[11];
    public static String[] jsonEx = new String[featureDesc.length];
    public static List<String> learningProblem = new ArrayList<String>();

    public static String json1 = "";

    static {

        featureDesc[0] = "Sempron SI-40 2.00GHz • 1024MB • 120GB • DVD+/-RW DL • NVIDIA GeForce 9100M (IGP) max.256MB shared memory • 3x USB 2.0/Modem/LAN/WLAN 802.11bg • ExpressCard/54 Slot • 3in1 Card Reader (SD/MMC/MS) • Webcam (1.3 Megapixel) • 16\\\" WXGA glare TFT (1366x768) • Windows Vista Home Basic • Li-Ionen-Akku (6 Zellen) • 2.70kg • 24 Monate Herstellergarantie";
        featureDesc[1] = "Core 2 Duo 2x 1.86GHz • 2048MB • 128GB Flash • kein optisches Laufwerk • NVIDIA GeForce 320M (IGP) max. 256MB shared memory • 2x USB 2.0/WLAN 802.11n/Bluetooth 2.1 • Mini DisplayPort • Webcam • 13.3\\\" WSXGA glare LED TFT (1440x900) • Mac OS X 10.6 Snow Leopard inkl. iLife • Lithium-Polymer-Akku • 1.32kg • 12 Monate Herstellergarantie";
        featureDesc[2] = "Core i5-2410M 2x 2.30GHz • 4096MB • 500GB • DVD+/-RW DL • NVIDIA GeForce GT520M 1024MB • 3x USB (2x USB 2.0, 1x USB 3.0)/Gb LAN/WLAN 802.11bgn/BLuetooth 3.0 • HDMI • 5in1 Card Reader • Webcam (1.3 Megapixel) • 13.3\\\" WXGA glare LED TFT (1366x768) • Windows 7 Home Premium (64-bit) • Li-Ionen-Akku (6 Zellen) • 2.10kg • 24 Monate Herstellergarantie • Besonderheiten: Nummernblock";
        featureDesc[3] = "Pentium B940 2x 2.00GHz • 4096MB • 500GB • DVD+/-RW DL • Intel GMA HD 3000 (IGP) shared memory • 3x USB (2x USB 2.0, 1x USB 3.0)/Gb LAN/WLAN 802.11bgn • HDMI • 5in1 Card Reader • Webcam (1.3 Megapixel) • 13.3\\\" WXGA glare LED TFT (1366x768) • Windows 7 Home Premium (64-bit) • Li-Ionen-Akku (6 Zellen) • 2.10kg • 24 Monate Herstellergarantie • Besonderheiten: Nummernblock";
        featureDesc[4] = "Core i3-2310M 2x 2.10GHz • 4096MB • 500GB • DVD+/-RW DL • Intel GMA HD 3000 (IGP) shared memory • 3x USB (2x USB 2.0, 1x USB 3.0)/Gb LAN/WLAN 802.11bgn/BLuetooth 3.0 • HDMI • 5in1 Card Reader • 13.3\\\" WXGA glare LED TFT (1366x768) • Windows 7 Home Premium (64-bit) • Li-Ionen-Akku (6 Zellen) • 2.10kg • 24 Monate Herstellergarantie • Besonderheiten: Nummernblock";
        featureDesc[5] = "Core i5-2410M 2x 2.30GHz • 4096MB • 500GB • DVD+/-RW DL • NVIDIA GeForce GT520M 1024MB • 3x USB (2x USB 2.0, 1x USB 3.0)/Gb LAN/WLAN 802.11bgn/BLuetooth 3.0 • HDMI • 5in1 Card Reader • Webcam (1.3 Megapixel) • 13.3\\\" WXGA glare LED TFT (1366x768) • Windows 7 Home Premium (64-bit) • Li-Ionen-Akku (6 Zellen) • 2.10kg • 24 Monate Herstellergarantie • Besonderheiten: Nummernblock";
        featureDesc[6] = "AMD C-50 2x 1.00GHz • 2048MB • 320GB • DVD+/-RW DL • AMD Radeon HD 6250 (IGP) shared memory • 3x USB 2.0/LAN/WLAN 802.11bgn • HDMI • 2in1 Card Reader • 15.6\\\" WXGA glare LED TFT 1366x768) • Windows 7 Home Premium (64-bit) • Li-Ionen-Akku • 2.60kg • 24 Monate Herstellergarantie • Besonderheiten: Nummernblock";
        featureDesc[7] = "Core i3-2310M 2x 2.10GHz • 4096MB • 500GB • DVD+/-RW DL • Intel GMA HD 3000 (IGP) shared memory • 3x USB (2x USB 2.0, 1x USB 3.0)/Gb LAN/WLAN 802.11bgn/BLuetooth 3.0 • HDMI • 5in1 Card Reader • 13.3\\\" WXGA glare LED TFT (1366x768) • Windows 7 Home Premium (64-bit) • Li-Ionen-Akku (6 Zellen) • 2.10kg • 24 Monate Herstellergarantie • Besonderheiten: Nummernblock";
        featureDesc[8] = "Core i3-2310M 2x 2.10GHz • 4096MB • 320GB • DVD+/-RW DL • Intel GMA HD 3000 (IGP) shared memory • 3x USB (2x USB 2.0, 1x USB 3.0)/Gb LAN/WLAN 802.11bgn/BLuetooth 3.0 • HDMI • 5in1 Card Reader • 13.3\\\" WXGA glare LED TFT (1366x768) • Windows 7 Home Premium (64-bit) • Li-Ionen-Akku (6 Zellen) • 2.10kg • 24 Monate Herstellergarantie • Besonderheiten: Nummernblock";
        featureDesc[9] = "Core i3-2310M 2x 2.10GHz • 4096MB • 500GB • DVD+/-RW DL • Intel GMA HD 3000 (IGP) shared memory • 3x USB (2x USB 2.0, 1x USB 3.0)/Gb LAN/WLAN 802.11bgn/BLuetooth 3.0 • HDMI • 5in1 Card Reader • 13.3\\\" WXGA glare LED TFT (1366x768) • Windows 7 Home Premium (64-bit) • Li-Ionen-Akku (6 Zellen) • 2.10kg • 24 Monate Herstellergarantie • Besonderheiten: Nummernblock";
        featureDesc[10] = "Core i3-2310M 2x 2.10GHz • 2048MB • 500GB • DVD+/-RW DL • Intel GMA HD 3000 (IGP) shared memory • 3x USB (2x USB 2.0, 1x USB 3.0)/Gb LAN/WLAN 802.11bgn/BLuetooth 3.0 • HDMI • 5in1 Card Reader • 13.3\\\" WXGA glare LED TFT (1366x768) • Windows 7 Home Premium (64-bit) • Li-Ionen-Akku (6 Zellen) • 2.10kg • 24 Monate Herstellergarantie • Besonderheiten: Nummernblock";

        for (int x = 1; x < featureDesc.length; x++) {
            String pos = " [\"http://test.de/x" + (x - 1) + "\",\"ignore1\", \"" + featureDesc[x - 1] + "\" ] ";
            String neg = " [\"http://test.de/x" + (x) + "\",\"ignore1\", \"" + featureDesc[x] + "\" ] ";
            learningProblem.add("{" + "\"pos\": [  " + pos + "   ] , " + "\"neg\": [   " + neg + "    ] " + "  }");
        }

        String p1 = " [\"http://test.de/x1\",\"ignore1\", \"" + featureDesc[3] + "\" ] ";
        String p2 = " [\"http://test.de/x2\",\"ignore1\", \"" + featureDesc[2] + "\" ] ";
        String n1 = " [\"http://test.de/x3\",\"ignore1\", \"" + featureDesc[1] + "\" ] ";
        String n2 = " [\"http://test.de/x4\",\"ignore1\", \"" + featureDesc[0] + "\" ] ";
       // learningProblem.add("{" + "\"pos\": [  " + p1 + " , " + p2 + "   ] , " + "\"neg\": [   " + n1 + " , " + n2 + "    ] " + "  }");
        //no negatives
       // learningProblem.add("{" + "\"pos\": [  " + p1 + " , " + p2 + "   ] , " + "\"neg\": [      ] " + "  }");

//no positives
       // learningProblem.add("{" + "\"pos\": [    ] , " + "\"neg\": [   " + n1 + " , " + n2 + "     ] " + "  }");

        //json1 = "{" + "\"pos\": [  " + i1 + " ,  " + i2 + "   ] , " + "\"neg\": [   " + i3 + " ,  " + i4 + "    ] " + "  }";*/
    }


    @Test
    public void conversion() throws Exception {
        Geizhals2OWL g = Geizhals2OWL.getInstance();
        BufferedReader bis = new BufferedReader(new InputStreamReader(g.getClass().getClassLoader().getResourceAsStream("nke/material.raw")));
        String line = "";
        while ((line = bis.readLine()) != null) {
            g.convertFeatureString2Classes(line);
        }

    }


    @Test
    public void learn() throws Exception {
//    	Logger.getRootLogger().setLevel(Level.TRACE);
        //System.out.println(json1);
        Geizhals2OWL g = Geizhals2OWL.getInstance();
        for (String json : learningProblem) {
            Monitor mon = MonitorFactory.getTimeMonitor("fic").start();
            Geizhals2OWL.Result result = g.handleJson(json);
            Learner l = new Learner();
            l.reasoner = FastInstanceChecker.class;
            List<EvaluatedDescriptionPosNeg> eds = l.learn(result.pos, result.neg, result.getModel(), 20);

            System.out.println("total time: " + Helper.prettyPrintNanoSeconds((long) mon.stop().getLastValue()));
            System.out.println(eds.get(0).asJSON());
        }

        for (String json : learningProblem) {
            Monitor mon = MonitorFactory.getTimeMonitor("owlapireasoner").start();
            Geizhals2OWL.Result result = g.handleJson(json);
            Learner l = new Learner();
            l.reasoner = OWLAPIReasoner.class;
            List<EvaluatedDescriptionPosNeg> eds = l.learn(result.pos, result.neg, result.getModel(), 20);
            System.out.println("total time: " + Helper.prettyPrintNanoSeconds((long) mon.stop().getLastValue()));
            System.out.println(eds.get(0).asJSON());
        }

        Monitor mon = MonitorFactory.getTimeMonitor("fic");
        System.out.println("FIC: " + mon.toString());
        mon = MonitorFactory.getTimeMonitor("owlapireasoner");
        System.out.println("owlapireasoner: " + mon.toString());
        mon = MonitorFactory.getTimeMonitor("Learner:owlapi");
        System.out.println("Learner:owlapi: " + mon.toString());
        mon = MonitorFactory.getTimeMonitor("Learner:reasoner");
        System.out.println("Learner:reasoner: " + mon.toString());
        mon = MonitorFactory.getTimeMonitor("Learner:learning");
        System.out.println("Learner:learning: " + mon.toString());

    }
}
