/*
 * $Id: ImageProcessor.java,v 1.1 2004/09/30 14:48:59 eiki Exp $ Created on Sep 30, 2004
 * 
 * Copyright (C) 2004 Idega Software hf. All Rights Reserved.
 * 
 * This software is the proprietary information of Idega hf. Use is subject to
 * license terms.
 */
package com.idega.block.image.business;

import java.util.HashMap;
import java.util.Map;
import com.idega.idegaweb.IWApplicationContext;

/**
 * 
 * Last modified: $Date: 2004/09/30 14:48:59 $ by $Author: eiki $
 * 
 * 
 * @author <a href="mailto:eiki@idega.com">eiki </a>
 * @version $Revision: 1.1 $
 */
class ImageProcessor implements Runnable {

	static ImageProcessor imageProcessor;

	static final String STORAGE_KEY = "ImageProcessorInstance";

	boolean runThread = true;

	Thread t;

	private boolean doneProcessing = true;

	private boolean isRunning = false;
	private Map unprocessedImages = new HashMap();
	

	/**
	 *  
	 */
	private ImageProcessor() {
		super();
	}

	public static ImageProcessor getInstance(IWApplicationContext iwac) {
		imageProcessor = (ImageProcessor) iwac.getApplicationAttribute(STORAGE_KEY);
		if (imageProcessor == null) {
			imageProcessor = new ImageProcessor();
			iwac.setApplicationAttribute(STORAGE_KEY, imageProcessor);
		}
		return imageProcessor;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		isRunning = true;
		try {
			while (runThread && doneProcessing) {
				processImages();
			}
		}
		catch (Exception e) {
			if (runThread) {
				e.printStackTrace();
			}
		}
		isRunning = false;
	}

	/**
	 * Does the actual image processing and saves the images to the database
	 */
	private synchronized void processImages() {
		
		
		
		
		
	}

	public void start() {
		runThread = true;
		if (t == null) {
			t = new Thread(this, "ImageProcessor Thread");
			t.setPriority(t.MIN_PRIORITY);
			t.start();
		}
		else {
			if (!isRunning) {
				t.run();
			}
		}
	}

	public void stop() {
		if (t != null) {
			runThread = false;
			t.interrupt();
		}
	}

	public boolean hasBeenProcessed() {
		return true;
	}

	public void addToProcessingQueu() {
		doneProcessing = hasBeenProcessed();
	}

	/** Destroy the thread */
	public void destroy() {
		stop();
		t = null;
	}
}