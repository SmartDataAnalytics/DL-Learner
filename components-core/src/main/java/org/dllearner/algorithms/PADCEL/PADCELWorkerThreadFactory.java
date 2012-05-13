package org.dllearner.algorithms.PADCEL;

import java.util.concurrent.ThreadFactory;

/**
 * PADCEL worker factory
 * 
 * @author An C. Tran
 *
 */

public class PADCELWorkerThreadFactory implements ThreadFactory {
	private int count=1;
	String idPrefix = "PDLL worker - ";

	@Override
	public Thread newThread(Runnable r) {
		return new Thread(r, idPrefix + (count++));
	}
}
