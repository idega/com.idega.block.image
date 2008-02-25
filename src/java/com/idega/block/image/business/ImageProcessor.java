/*
 * $Id: ImageProcessor.java,v 1.19 2008/02/25 17:50:27 eiki Exp $ Created on
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
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.CreateException;
import javax.media.jai.PlanarImage;

import com.idega.block.image.data.ImageProcessJob;
import com.idega.business.IBOLookup;
import com.idega.graphics.image.business.ImageEncoder;
import com.idega.graphics.image.business.ImageEncoderBean;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.slide.business.IWSlideService;

/**
 * 
 * Last modified: $Date: 2008/02/25 17:50:27 $ by $Author: eiki $
 * 
 * 
 * @author <a href="mailto:eiki@idega.com">eiki </a>
 * @version $Revision: 1.19 $
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
			//we are done with it weather it worked or not
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
		// String imageName = job.getName();
		int widthOfModifiedImage = job.getNewWidth();
		int heightOfModifiedImage = job.getNewHeight();
		String nameOfModifiedImage = job.getJobKey();
		String pathOfModifiedImage = job.getModifiedImageURI();
		
		if (job.getLocationIsURL()) {
			try {						
				IWSlideService ss = (IWSlideService) IBOLookup.getServiceInstance(this.iwac, IWSlideService.class);
			    PlanarImage original = imageEncoder.getPlanarImage(realPathToImage);
			    int originalHeight = original.getHeight();
			    int originalWidth = original.getWidth();
			    float scaleWidth = (float)widthOfModifiedImage;
			    float scaleHeight = (float)heightOfModifiedImage;
			    
			    PlanarImage finalImage;
			    if(job.isSetToOnlyScale()){
			    	 // Scales the original image
				    float scale = (float)(scaleWidth/originalWidth);
				    // Creates a new, scaled image
				    finalImage = imageEncoder.scale(original,scale); 
			    }
			    else{
				    // calculate the biggest even sided box we can fit into the
					// center of the image and then adjust either the width or
					// height depending on the proportions
				    // then crop the image to that size and then scale that to a
					// thumb-nail size
				    float x = 0;
				    float y = 0;
				    float cropWidth;
				    float cropHeight;
	
				    // if ratio is > 1 then width > height
				    // if ratio = 1 then width = height
				    // if ratio < 0 then height > width
				    float widthHeightRatio = (float) (scaleWidth/scaleHeight);
			
				    // first make a box with even sides
				    if(originalWidth > originalHeight){
				    	x = (originalWidth/2)-(originalHeight/2);
				    	y = 0;
				    	cropHeight = originalHeight;
				    	cropWidth = originalHeight;
				    }
				    else if(originalWidth < originalHeight){
				    	x = 0;
				    	y = (originalHeight/2)-(originalWidth/2);
				    	cropHeight = originalWidth;
				    	cropWidth = originalWidth;
				    }
				    else{
				    	x = 0;
				    	y = 0;
				    	cropWidth = originalWidth;
				    	cropHeight = originalHeight;
				    }
				    
				    // then shrink the width or the height if needed
				    //TODO move it a little to center it after shrinking
				    if(widthHeightRatio>1){
				    	// width > height
				    	cropHeight = (float) (cropWidth/widthHeightRatio);
				    }
				    else if(widthHeightRatio<1){
				    	cropWidth = (float) (cropHeight * widthHeightRatio);
				    }
				    
				    PlanarImage cropped = imageEncoder.crop(original, x, y, cropWidth, cropHeight);
		 
				    // Scales the original image
				    float scale = (float)(scaleWidth/cropWidth);
				    // Creates a new, scaled image
				    finalImage = imageEncoder.scale(cropped,scale); 
			    }   
			    
			    InputStream imageStream = imageEncoder.encodePlanarImageToInputStream(finalImage,ImageEncoderBean.JPEG);
				
				ss.uploadFileAndCreateFoldersFromStringAsRoot(pathOfModifiedImage.substring(0,pathOfModifiedImage.lastIndexOf("/")+1) , nameOfModifiedImage, imageStream, mimeType, true);
				imageStream.close();
				imageStream = null;

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
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