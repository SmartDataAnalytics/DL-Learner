package org.dllearner.algorithm.tbsl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Set;

import org.dllearner.algorithm.tbsl.sparql.Template;
import org.dllearner.algorithm.tbsl.templator.Templator;

public class TemplateSerializationTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		Templator templateGenerator = new Templator();
		templateGenerator.setUNTAGGED_INPUT(true);
		
		//generate templates
		Set<Template> templates = templateGenerator.buildTemplates("Give me all soccer clubs in Premier League.");
		for(Template t : templates){
			System.out.println(t);
		}
		
		//serialize
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File("templates.out")));
		oos.writeObject(templates);
		
		//deserialize
		ObjectInputStream ois = new ObjectInputStream(new FileInputStream(new File("templates.out")));
		templates = (Set<Template>) ois.readObject();
		
		for(Template t : templates){
			System.out.println(t);
		}
	}

}
