package org.dllearner.autosparql.client;

import com.extjs.gxt.ui.client.event.EventType;

public class AppEvents {

	public static final EventType Init = new EventType();

	public static final EventType ShowInteractiveMode = new EventType();

	public static final EventType Error = new EventType();

	public static final EventType AddPosExample = new EventType();

	public static final EventType AddNegExample = new EventType();
	
	public static final EventType AddExample = new EventType();

	public static final EventType RemoveExample = new EventType();
	
	public static final EventType UpdateResultTable = new EventType();
}
