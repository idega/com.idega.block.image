/*
 * $Id: ImageProcessor.java,v 1.17 2008/02/22 02:28:27 eiki Exp $ Created on
 * Sep 30, 2004
 * 
 * Copyright (C) 2004 Idega Software hf. All Rights Reserved.
 * 
 * This software is the proprietary information of Idega hf. Use is subject to
 * license terms.
 */
package com.idega.block.image.business;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.CreateException;

import com.idega.block.image.data.ImageProcessJob;
import com.idega.business.IBOLookup;
import com.idega.graphics.image.business.ImageEncoder;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.io.MemoryFileBuffer;
import com.idega.io.MemoryInputStream;
import com.idega.io.MemoryOutputStream;
import com.idega.slide.business.IWSlideService;

/**
 * 
 * Last modified: $Date: 2008/02/22 02:28:27 $ by $Author: eiki $
 * 
 * 
 * @author <a href="mailto:eiki@idega.com">eiki </a>
 * @version $Revision: 1.17 $
 */
public class ImageProcessor implements Runnable {

	/** Folder where the modified images are stored */
	public static final String MODIFIED_IMAGES_FOLDER = "modified_images";
	static ImageProcessor imageProcessor;
	static final String STORAGE_KEY = "ImageProcessorInstance";
	boolean runThread = true;
	Thread thread;
	private boolean isRunning = false;
	private Map unprocessedImages = new HashMap();
	private List inProcessOrDone = new ArrayList();
	private IWApplicationContext iwac;

	/**
	 *  
	 */
	private ImageProcessor(IWApplicationContext iwac) {
		super();
		this.iwac = iwac;
	}

	public static ImageProcessor getInstance(IWApplicationContext iwac) {
		imageProcessor = (ImageProcessor) iwac.getApplicationAttribute(STORAGE_KEY);
		if (imageProcessor == null) {
			imageProcessor = new ImageProcessor(iwac);
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
		this.isRunning = true;
		try {
			while (this.runThread && !this.unprocessedImages.isEmpty()) {
				processImages();
			}
		}
		catch (Exception e) {
			if (this.runThread) {
				this.runThread = false;
				e.printStackTrace();
			}
		}
		this.isRunning = false;
	}

	/**
	 * Does the actual image processing and saves the images to the database
	 */
	private void processImages() {
		Object[] jobs = this.unprocessedImages.values().toArray();
		for (int i = 0; i < jobs.length; i++) {
			ImageProcessJob job = (ImageProcessJob) jobs[i];
			//put it in the "busy" list
			this.inProcessOrDone.add(job.getJobKey());
			try {
				processImage(job);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			//we are done with it wether it worked or not
			this.unprocessedImages.remove(job.getJobKey());
		}
	}

	/**
	 * Scales the image to its new dimensions
	 * 
	 * @param job
	 * @param iwc
	 * @throws IOException
	 * @throws RemoteException
	 * @throws CreateException
	 */
	private void processImage(ImageProcessJob job) throws IOException, RemoteException, CreateException {
		// get image encoder
		ImageEncoder imageEncoder = getImageEncoder();
		// get all the job data
		String realPathToImage = job.getImageLocation();
		String mimeType = job.getMimeType();
		//String imageName = job.getName();
		int widthOfModifiedImage = job.getNewWidth();
		int heightOfModifiedImage = job.getNewHeight();
		String nameOfModifiedImage = job.getJobKey();
		String pathOfModifiedImage = job.getModifiedImageURI();
		
		if (job.getLocationIsURL()) {
			try {						
				
				IWSlideService ss = (IWSlideService) IBOLookup.getServiceInstance(this.iwac, IWSlideService.class);
				//pathOfModifiedImage = getModifiedImagePath(widthOfModifiedImage, heightOfModifiedImage, extension,imageName, parentPath);
				
				InputStream input = new URL(realPathToImage).openStream();
				// get output
				MemoryFileBuffer buff = new MemoryFileBuffer();
				OutputStream output = new MemoryOutputStream(buff);
				// encode the image
				try {
					imageEncoder.encode(mimeType, input, output, widthOfModifiedImage, heightOfModifiedImage);
					//set the generated image mime type 
					mimeType = imageEncoder.getResultMimeTypeForInputMimeType(mimeType);
					
					InputStream s = new MemoryInputStream(buff);
					//parentRes.putMethod(pathOfModifiedImage, new URL(realPathToImage).openStream());
					ss.uploadFileAndCreateFoldersFromStringAsRoot(pathOfModifiedImage.substring(0,pathOfModifiedImage.lastIndexOf("/")+1) , nameOfModifiedImage, s, mimeType, true);
					s.close();
					s = null;
				}
				catch (Exception ex) {
					// delete the created file (you can not use the result)
					output.close();
					input.close();
					ex.printStackTrace();
				}
				finally {
					output.close();
					output = null;
					input.close();
					input = null;					
					buff = null;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private String getModifiedImagePath(int width, int height, String extension, String imageName, String parentPath) {
		int slashPos = imageName.lastIndexOf("/");
		if (slashPos > 0) {
			imageName = imageName.substring(slashPos+1);
		}
		int pointPosition = imageName.lastIndexOf('.');
		int length = imageName.length();
		if ((pointPosition > 0) && pointPosition > (length - 5)) {
			imageName = imageName.substring(0, pointPosition);
		}

		StringBuffer buf = new StringBuffer(parentPath);
		buf.append("/resized/").
		append(imageName).
		append("_").append(width).append("x").append(height).append(".").append(extension);		
		return buf.toString();
	}
	
	public void start() {
		this.runThread = true;
		if (this.thread == null || !this.isRunning) {
			//a new thread must be created here because it was null or 
			//we went out of the run() method. When run is finished the thread is considered dead and cannot be restarted
			this.thread = new Thread(this, "ImageProcessor Thread");
			this.thread.setDaemon(true);
			//this is a backround task
			this.thread.setPriority(Thread.NORM_PRIORITY);
			this.thread.start();
		}
	}

	public void stop() {
		if (this.thread != null) {
			this.runThread = false;
			this.thread.interrupt();
		}
	}

	/**
	 * You should use this method to add Image Processing jobs. It automatically
	 * starts the processing thread
	 * 
	 * @param job
	 */
	public void addImageProcessJobToQueu(ImageProcessJob job) {
		String key = job.getJobKey();
		if (!isInProcessOrDone(key)) {
			addToQueu(job);
			//if the processes are not being processed already (run() not
			// running) we start it now.
			this.start();
		}
	}

	private synchronized void addToQueu(ImageProcessJob job) {
		this.unprocessedImages.put(job.getJobKey(), job);
	}

	/**
	 * Tells you if it is already being processed or is in the queu
	 * 
	 * @param key
	 * @return
	 */
	private boolean isInProcessOrDone(String key) {
		return this.unprocessedImages.containsKey(key) || this.inProcessOrDone.contains(key);
	}

	/** Destroy the thread */
	public void destroy() {
		stop();
		this.thread = null;
	}

	private ImageEncoder getImageEncoder() throws RemoteException {
		return (ImageEncoder) IBOLookup.getServiceInstance(this.iwac, ImageEncoder.class);
	}

}