package org.dllearner.algorithm.tbsl.exploration.Utils;

import java.util.HashMap;

public class ElementStorage {
private static HashMap<String,HashMap<String,String>> storage_classes = new HashMap<String,HashMap<String,String>> ();
private static HashMap<String,HashMap<String,String>> storage_resource_right = new HashMap<String,HashMap<String,String>> ();
private static HashMap<String,HashMap<String,String>> storage_resource_left = new HashMap<String,HashMap<String,String>> ();
private static HashMap<String,String> storage_property= new HashMap<String,String> ();

/*
 * First String contains URI
 */
public static HashMap<String,HashMap<String,String>> getStorage_classes() {
	return storage_classes;
}

public static void setStorage_classes(HashMap<String,HashMap<String,String>> storage_classes) {
	ElementStorage.storage_classes = storage_classes;
}

public static void addStorage_classes(String key, HashMap<String,String> value) {
	ElementStorage.storage_classes.put(key, value);
}

public static HashMap<String,HashMap<String,String>> getStorage_resource_right() {
	return storage_resource_right;
}

public static void setStorage_resource_right(HashMap<String,HashMap<String,String>> storage_resource_right) {
	ElementStorage.storage_resource_right = storage_resource_right;
}

public static void addStorage_resource_right(String key, HashMap<String,String> value) {
	ElementStorage.storage_resource_right.put(key, value);
}

public static HashMap<String,HashMap<String,String>> getStorage_resource_left() {
	return storage_resource_left;
}

public static void setStorage_resource_left(HashMap<String,HashMap<String,String>> storage_resource_left) {
	ElementStorage.storage_resource_left = storage_resource_left;
}

public static void addStorage_resource_left(String key, HashMap<String,String> value) {
	ElementStorage.storage_resource_left.put(key, value);
}

public static HashMap<String,String> getStorage_property() {
	return storage_property;
}

public static void setStorage_property(HashMap<String,String> storage_property) {
	ElementStorage.storage_property = storage_property;
}

public static void addStorage_property(String key, String value) {
	ElementStorage.storage_property.put(key, value);
}


}
