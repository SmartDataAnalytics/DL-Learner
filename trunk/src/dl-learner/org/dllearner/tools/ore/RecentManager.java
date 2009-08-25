package org.dllearner.tools.ore;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;


public class RecentManager {
	
	private static RecentManager instance;
	private List<URI> uriList;
	private File file;
	
	public RecentManager(){
		uriList = new ArrayList<URI>();
		file = new File("src/dl-learner/org/dllearner/tools/ore/recent.txt");
	}

	public static synchronized RecentManager getInstance() {
		if (instance == null) {
			instance = new RecentManager();
		}
		return instance;
	}
	
	public void addURI(URI uri){
		if(!uriList.contains(uri)){
			uriList.add(uri);
		}
		
	}
	
	public List<URI> getURIs(){
		uriList.clear();
		deserialize();
		return uriList;
	}
	
	public void serialize() {
		try {
			FileOutputStream fileStream = new FileOutputStream(file);
			ObjectOutputStream outputStream = new ObjectOutputStream(
					new BufferedOutputStream(fileStream));
			try {
				outputStream.writeObject(uriList);
			} finally {
				outputStream.close();
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	@SuppressWarnings("unchecked")
	private void deserialize() {
		try {
			FileInputStream fileStream = new FileInputStream(file);
			ObjectInputStream inputStream = new ObjectInputStream(
					new BufferedInputStream(fileStream));

			try {
				List<URI> list = (List<URI>) inputStream.readObject();
				uriList.addAll(list);
			} finally {
				inputStream.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

	}
	
	
}
