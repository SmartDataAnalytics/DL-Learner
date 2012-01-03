package org.dllearner.scripts.evaluation;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

/**
 * A simple helper class to extract links from a given HTML page.
 * @author lorenz
 *
 */
public class LinkExtractor {

	public static void main(String[] args) throws Exception {

		if(args.length == 1){
			URL url = new URL(args[0]);
			URLConnection con = url.openConnection();

			BufferedReader br = new BufferedReader(new InputStreamReader(con
					.getInputStream()));

			HTMLEditorKit editorKit = new HTMLEditorKit();
			HTMLDocument htmlDoc = new HTMLDocument();
			htmlDoc.putProperty("IgnoreCharsetDirective", Boolean.TRUE);
			editorKit.read(br, htmlDoc, 0);

			HTMLDocument.Iterator iter = htmlDoc.getIterator(HTML.Tag.A);
			while (iter.isValid()) {
				Object href = iter.getAttributes().getAttribute(
						HTML.Attribute.HREF);
				if(href != null){
					System.out.println(href);
				}
				
				iter.next();
			}
		} else {
			System.out.println("Wrong number of arguments. A valid path to a HTML page is needed.");
		}
		

	}
}
