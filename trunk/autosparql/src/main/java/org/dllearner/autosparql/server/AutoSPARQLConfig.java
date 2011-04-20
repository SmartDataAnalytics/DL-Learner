package org.dllearner.autosparql.server;

import org.dllearner.autosparql.server.store.Store;

public class AutoSPARQLConfig {
	
	private Store store;
	
	public void setStore(Store store){
		this.store = store;
	}
	
	public Store getStore(){
		return store;
	}

}
