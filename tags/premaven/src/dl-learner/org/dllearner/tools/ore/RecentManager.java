package org.dllearner.tools.ore;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.apache.log4j.Logger;

public class RecentManager {

	private static RecentManager instance;
	private List<URI> uriList;

	private static final Logger logger = Logger.getLogger(RecentManager.class);

	public static final String PREFERENCES_KEY = "org.dllearner.tools.ore";

	public static final int MAX_EDITOR_KITS = 10;

	public RecentManager() {
		uriList = new ArrayList<URI>();
	}

	public static synchronized RecentManager getInstance() {
		if (instance == null) {
			instance = new RecentManager();
		}
		return instance;
	}

	public List<URI> getURIs() {
		return uriList;
	}

	@SuppressWarnings("unchecked")
	public void load() {
		Preferences userRoot = Preferences.userRoot();
		byte[] prefsBytes = userRoot.getByteArray(PREFERENCES_KEY, null);
		if (prefsBytes == null) {
			return;
		}
		try {
			ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(prefsBytes));
			uriList = (List<URI>) ois.readObject();
			ois.close();
			pruneInvalidURIs();
		} catch (Exception e) {
			logger.error(e);
		}
	}

	public void save() {
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(bos);
			oos.writeObject(uriList);
			oos.flush();
			oos.close();
			// Store in Java prefs
			Preferences userRoot = Preferences.userRoot();
			userRoot.putByteArray(PREFERENCES_KEY, bos.toByteArray());
			userRoot.flush();
		} catch (IOException e) {
			logger.error(e);
		} catch (BackingStoreException e) {
			logger.error(e);
		}
	}

	public void add(URI uri) {
		for (Iterator<URI> it = uriList.iterator(); it.hasNext();) {
			URI u = it.next();
			if (u.equals(uri)) {
				it.remove();
				break;
			}
		}
		uriList.add(0, uri);
		// Chop any off the end
		for (int i = MAX_EDITOR_KITS - 1; uriList.size() > MAX_EDITOR_KITS;) {
			uriList.remove(i);
		}
	}

	public void clear() {
		uriList.clear();
	}

	private boolean isValidURI(URI uri) {
		if (uri == null || uri.getScheme() == null) {
			return false;
		}
		if (uri.getScheme().equals("file")) {
			File file = new File(uri);
			return file.exists();
		}
		return true;
	}

	public void pruneInvalidURIs() {
		for (Iterator<URI> it = uriList.iterator(); it.hasNext();) {
			if (!isValidURI(it.next())) {
				it.remove();
			}
		}
	}

}
