package org.dllearner.algorithms.parcel;

import java.util.concurrent.ThreadFactory;

/**
 * ParCEL worker factory
 * 
 * @author An C. Tran
 *
 */

public class ParCELWorkerThreadFactory implements ThreadFactory {
	private int count=1;
	String idPrefix = "PDLL worker - ";

	@Override
	public Thread newThread(Runnable r) {
		return new Thread(r, idPrefix + (count++));
	}
}
