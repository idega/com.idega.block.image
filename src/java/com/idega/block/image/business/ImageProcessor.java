/*
 * $Id: ImageProcessor.java,v 1.2 2004/09/30 16:08:45 thomas Exp $ Created on Sep 30, 2004
 * 
 * Copyright (C) 2004 Idega Software hf. All Rights Reserved.
 * 
 * This software is the proprietary information of Idega hf. Use is subject to
 * license terms.
 */
package com.idega.block.image.business;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import com.idega.block.image.data.ImageEntity;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.idegaweb.IWCacheManager;
import com.idega.idegaweb.IWMainApplication;
import com.idega.presentation.IWContext;
import com.idega.util.FileUtil;
import com.idega.util.caching.Cache;

/**
 * 
 * Last modified: $Date: 2004/09/30 16:08:45 $ by $Author: thomas $
 * 
 * 
 * @author <a href="mailto:eiki@idega.com">eiki </a>
 * @version $Revision: 1.2 $
 */
class ImageProcessor implements Runnable {
	
	  /** Folder where the modified images are stored */  
	public static final String MODIFIED_IMAGES_FOLDER = "modified_images";

	static ImageProcessor imageProcessor;

	static final String STORAGE_KEY = "ImageProcessorInstance";

	boolean runThread = true;

	Thread thread;

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
	
    // get image encoder
    ImageEncoder imageEncoder = getImageEncoder(iwc);
    
	
	
	private void processImage() {
    	IWMainApplication mainApp = iwc.getIWMainApplication();
    
		String pathOfModifiedImage = 
			getRealPathOfModifiedImage(widthOfModifiedImage, heightOfModifiedImage, extension, mainApp, imageName, originalImageID);

		// now create the new image...  
		
		// get input
		// get fileFileValue() causes End-Of-File Exception when JAI tries to read the file fully 
		// InputStream input = imageEntity.getFileValue();
		FileInputStream input = new FileInputStream(getRealPathToImage(iwc));
		
		// get output
		OutputStream output = new FileOutputStream(pathOfModifiedImage);
		// encode     
		try {  
		imageEncoder.encode(mimeType, input ,output,widthOfModifiedImage, heightOfModifiedImage);
		}
		catch (Exception ex)  {
		// delete the created file (you can not use the result)
		output.close();
		input.close();
		(new File(pathOfModifiedImage)).delete();
		throw ex;
		}
		output.close();
		input.close();
		FileInputStream inputStream = new FileInputStream(pathOfModifiedImage);  
		
		ImageEntity motherImage = getImageEntity(iwc);
		ImageProvider imageProvider = getImageProvider(iwc);
		int modifiedImageId = imageProvider.uploadImage(inputStream, mimeType, nameOfModifiedImage, widthOfModifiedImage, heightOfModifiedImage ,motherImage);
		inputStream.close();
		return modifiedImageId;
	}
		

	private String getRealPathOfModifiedImage(int width, int height, String extension,IWMainApplication mainApp, String imageName, int originalImageID) {	    
	    String separator = FileUtil.getFileSeparator();
	    
	    StringBuffer path = new StringBuffer(mainApp.getApplicationRealPath());
	           
	    path.append(IWCacheManager.IW_ROOT_CACHE_DIRECTORY)
	      .append(separator)
	      .append(MODIFIED_IMAGES_FOLDER);
	    
	    // check if the folder exists create it if necessary
	    // usually the folder should be already be there.
	    // the folder is never deleted by this class
	    FileUtil.createFolder(path.toString());
	    path.append(separator) 
	        .append(getNameOfModifiedImageWithExtension(width, height, extension, imageName, originalImageID));
	    return path.toString();
	  }
  
	private String getNameOfModifiedImageWithExtension(int width, int height, String extension, String imageName, int originalImageID)  {
	    
	    int pointPosition = imageName.lastIndexOf('.');
	    int length = imageName.length();
	    // cut extension (imageName.a  imageName.ab imageName.abc but not imageName.abcd)
	    if ( (pointPosition > 0) && pointPosition > (length - 5))  
	      imageName = imageName.substring(0,pointPosition);        
	    StringBuffer nameOfImage = new StringBuffer();
	    // add new extension
	    nameOfImage.append(originalImageID);
	    nameOfImage.append(width).append("_").append(height)
	      .append("_").append(imageName)
	      .append(".").append(extension);
	    
	    return nameOfImage.toString();
	}
		
	private Cache getCachedImage(IWContext iwc, int imageId) {
	    // this method is similar to the private getImage() method of the super class Image
	    IWMainApplication iwma = iwc.getIWMainApplication(); 
	  
	    return IWCacheManager.getInstance(iwma).getCachedBlobObject(com.idega.block.image.data.ImageEntity.class.getName(),imageId,iwma);
	  }

	private String getRealPathToImageAndImageEntity(IWContext iwc, int originalImageID)  {  
	    Cache cachedImage = getCachedImage(iwc, originalImageID);
	    return cachedImage.getRealPathToFile();
	}
		


	public void start() {
		runThread = true;
		if (thread == null) {
			thread = new Thread(this, "ImageProcessor Thread");
			thread.setPriority(thread.MIN_PRIORITY);
			thread.start();
		}
		else {
			if (!isRunning) {
				thread.run();
			}
		}
	}

	public void stop() {
		if (thread != null) {
			runThread = false;
			thread.interrupt();
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
		thread = null;
	}
}