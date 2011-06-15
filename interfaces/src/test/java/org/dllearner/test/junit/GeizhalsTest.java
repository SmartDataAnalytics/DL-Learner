package org.dllearner.test.junit;

import org.dllearner.server.nke.Geizhals2OWL;
import org.dllearner.server.nke.Learner;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * @author Sebastian Hellmann - http://bis.informatik.uni-leipzig.de/SebastianHellmann
 *         Created: 15.06.11
 */
public class GeizhalsTest {


    public static String n1 = "Sempron SI-40 2.00GHz • 1024MB • 120GB • DVD+/-RW DL • NVIDIA GeForce 9100M (IGP) max.256MB shared memory • 3x USB 2.0/Modem/LAN/WLAN 802.11bg • ExpressCard/54 Slot • 3in1 Card Reader (SD/MMC/MS) • Webcam (1.3 Megapixel) • 16\" WXGA glare TFT (1366x768) • Windows Vista Home Basic • Li-Ionen-Akku (6 Zellen) • 2.70kg • 24 Monate Herstellergarantie";
    public static String n2 = "Core 2 Duo 2x 1.86GHz • 2048MB • 128GB Flash • kein optisches Laufwerk • NVIDIA GeForce 320M (IGP) max. 256MB shared memory • 2x USB 2.0/WLAN 802.11n/Bluetooth 2.1 • Mini DisplayPort • Webcam • 13.3\" WSXGA glare LED TFT (1440x900) • Mac OS X 10.6 Snow Leopard inkl. iLife • Lithium-Polymer-Akku • 1.32kg • 12 Monate Herstellergarantie";
    public static String p1 = "Core i5-2410M 2x 2.30GHz • 4096MB • 500GB • DVD+/-RW DL • NVIDIA GeForce GT520M 1024MB • 3x USB (2x USB 2.0, 1x USB 3.0)/Gb LAN/WLAN 802.11bgn/BLuetooth 3.0 • HDMI • 5in1 Card Reader • Webcam (1.3 Megapixel) • 13.3\" WXGA glare LED TFT (1366x768) • Windows 7 Home Premium (64-bit) • Li-Ionen-Akku (6 Zellen) • 2.10kg • 24 Monate Herstellergarantie • Besonderheiten: Nummernblock";
    public static String p2 = "Pentium B940 2x 2.00GHz • 4096MB • 500GB • DVD+/-RW DL • Intel GMA HD 3000 (IGP) shared memory • 3x USB (2x USB 2.0, 1x USB 3.0)/Gb LAN/WLAN 802.11bgn • HDMI • 5in1 Card Reader • Webcam (1.3 Megapixel) • 13.3\" WXGA glare LED TFT (1366x768) • Windows 7 Home Premium (64-bit) • Li-Ionen-Akku (6 Zellen) • 2.10kg • 24 Monate Herstellergarantie • Besonderheiten: Nummernblock";
    public static String json = "";

    static {
        p1 = " [\"http://test.de/p1\",\"ignore1\", \"" + p1 + "\" ] ";
        p2 = " [\"http://test.de/p2\",\"ignore1\", \"" + p2 + "\" ] ";
        n1 = " [\"http://test.de/n1\",\"ignore1\", \"" + n1 + "\" ] ";
        n2 = " [\"http://test.de/n2\",\"ignore1\", \"" + n2 + "\" ] ";
        json = "{" + "\"pos\": [  " + p1 + " ,  " + p2 + "   ], " + "\"neg\": [   \"" + n1 + "\" ,  \"" + n2 + "\"    ] " + "  }";
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
    public void learn() throws Exception{

        Geizhals2OWL g = Geizhals2OWL.getInstance();
        Geizhals2OWL.Result result = g.handleJson(GeizhalsTest.json);
        Learner l = new Learner();
        Learner.LearningResult lr = l.learn(result.pos, result.neg, result.getModel(), 20);
        System.out.println(lr);
    }
}
