/*
 * $Id: ImageProcessor.java,v 1.11 2006/02/20 11:04:52 laddi Exp $ Created on
 * Sep 30, 2004
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
import org.apache.commons.httpclient.HttpURL;
import org.apache.webdav.lib.WebdavResource;
import com.idega.block.image.data.ImageEntity;
import com.idega.block.image.data.ImageProcessJob;
import com.idega.business.IBOLookup;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.idegaweb.IWCacheManager;
import com.idega.idegaweb.IWMainApplication;
import com.idega.io.MemoryFileBuffer;
import com.idega.io.MemoryInputStream;
import com.idega.io.MemoryOutputStream;
import com.idega.presentation.IWContext;
import com.idega.slide.business.IWSlideService;
import com.idega.slide.util.WebdavExtendedResource;
import com.idega.util.FileUtil;
import com.idega.util.caching.Cache;

/**
 * 
 * Last modified: $Date: 2006/02/20 11:04:52 $ by $Author: laddi $
 * 
 * 
 * @author <a href="mailto:eiki@idega.com">eiki </a>
 * @version $Revision: 1.11 $
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
		isRunning = true;
		try {
			while (runThread && !unprocessedImages.isEmpty()) {
				processImages();
			}
		}
		catch (Exception e) {
			if (runThread) {
				runThread = false;
				e.printStackTrace();
			}
		}
		isRunning = false;
	}

	/**
	 * Does the actual image processing and saves the images to the database
	 */
	private void processImages() {
		Object[] jobs = unprocessedImages.values().toArray();
		for (int i = 0; i < jobs.length; i++) {
			ImageProcessJob job = (ImageProcessJob) jobs[i];
			//put it in the "busy" list
			inProcessOrDone.add(job.getJobKey());
			try {
				processImage(job);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			//we are done with it wether it worked or not
			unprocessedImages.remove(job.getJobKey());
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
		String imageName = job.getName();
		String originalImageID = job.getID();
		int widthOfModifiedImage = job.getNewWidth();
		int heightOfModifiedImage = job.getNewHeight();
		String extension = job.getNewExtension();
		String nameOfModifiedImage = job.getJobKey();
		String pathOfModifiedImage = null;
		if (job.getLocationIsURL()) {
			try {

				WebdavExtendedResource resource = new WebdavExtendedResource(new HttpURL(realPathToImage));
				String parentPath = resource.getParentPath();
				IWSlideService ss = (IWSlideService) IBOLookup.getServiceInstance(iwac, IWSlideService.class);
				WebdavExtendedResource parentRes = ss.getWebdavExtendedResource(parentPath,ss.getRootUserCredentials());
				WebdavResource root = ss.getWebdavRootResource(ss.getRootUserCredentials());
				String complete = parentPath+"/thumbnails";
				boolean exists = ss.getExistence(complete);
				// create thumbnails folder
				if (!exists) {
					root.mkcolMethod(complete);
				}
				pathOfModifiedImage = getModifiedImagePath(widthOfModifiedImage, heightOfModifiedImage, extension,
						imageName, parentPath);

				InputStream input = new URL(realPathToImage).openStream();
				// get output
				MemoryFileBuffer buff = new MemoryFileBuffer();
				OutputStream output = new MemoryOutputStream(buff);
				// encode the image
				try {
					imageEncoder.encode(mimeType, input, output, widthOfModifiedImage, heightOfModifiedImage);
					InputStream s = new MemoryInputStream(buff);
					//parentRes.putMethod(pathOfModifiedImage, new URL(realPathToImage).openStream());
					parentRes.putMethod(pathOfModifiedImage, s);
					s.close();
				}
				catch (Exception ex) {
					// delete the created file (you can not use the result)
					output.close();
					input.close();
					ex.printStackTrace();
				}
				finally {
					output.close();
					input.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {

			pathOfModifiedImage = getRealPathOfModifiedImage(widthOfModifiedImage, heightOfModifiedImage, extension,
					imageName, originalImageID);

			// now create the new image...
			// get input from the cached file instead of from the database because
			// get imageEntity.getFileValue() causes End-Of-File Exception when JAI
			// tries to read the file fully
			FileInputStream input = new FileInputStream(realPathToImage);
			// get output
			OutputStream output = new FileOutputStream(pathOfModifiedImage);
			// encode the image
			try {
				imageEncoder.encode(mimeType, input, output, widthOfModifiedImage, heightOfModifiedImage);
			}
			catch (Exception ex) {
				// delete the created file (you can not use the result)
				output.close();
				input.close();
				(new File(pathOfModifiedImage)).delete();
				ex.printStackTrace();
			}
			finally {
				output.close();
				input.close();
			}
			
			//Save the image to the database
			FileInputStream inputStream = new FileInputStream(pathOfModifiedImage);
			ImageEntity motherImage = job.getImageEntity();
			ImageProvider imageProvider = getImageProvider();
			imageProvider.uploadImage(inputStream, mimeType, nameOfModifiedImage,
					widthOfModifiedImage, heightOfModifiedImage, motherImage);
			inputStream.close();
		}
	}

	private String getModifiedImagePath(int width, int height, String extension, String imageName, String parentPath) {
		int slashPos = imageName.lastIndexOf("/");
		if (slashPos > 0) {
			imageName = imageName.substring(slashPos+1);
		}
		int pointPosition = imageName.lastIndexOf('.');
		int length = imageName.length();
		if ((pointPosition > 0) && pointPosition > (length - 5))
			imageName = imageName.substring(0, pointPosition);

		StringBuffer buf = new StringBuffer(parentPath);
		buf.append("/thumbnails/").
		append(imageName).
		append("_").append(width).append("x").append(height).append(".").append(extension);		
		return buf.toString();
	}
	
	private String getRealPathOfModifiedImage(int width, int height, String extension, String imageName,
			String originalImageID) {
		String separator = FileUtil.getFileSeparator();
		StringBuffer path = new StringBuffer(iwac.getIWMainApplication().getApplicationRealPath());
		path.append(IWCacheManager.IW_ROOT_CACHE_DIRECTORY).append(separator).append(MODIFIED_IMAGES_FOLDER);
		// check if the folder exists create it if necessary
		// usually the folder should be already be there.
		// the folder is never deleted by this class
		FileUtil.createFolder(path.toString());
		path.append(separator).append(
				getNameOfModifiedImageWithExtension(width, height, extension, imageName, originalImageID));
		return path.toString();
	}

	private String getNameOfModifiedImageWithExtension(int width, int height, String extension, String imageName,
			String originalImageID) {
		int pointPosition = imageName.lastIndexOf('.');
		int length = imageName.length();
		// cut extension (imageName.a imageName.ab imageName.abc but not
		// imageName.abcd)
		if ((pointPosition > 0) && pointPosition > (length - 5))
			imageName = imageName.substring(0, pointPosition);
		StringBuffer nameOfImage = new StringBuffer();
		// add new extension
		nameOfImage.append(originalImageID);
		nameOfImage.append(width).append("_").append(height).append("_").append(imageName).append(".").append(extension);
		return nameOfImage.toString();
	}

	private Cache getCachedImage(IWContext iwc, int imageId) {
		// this method is similar to the private getImage() method of the super
		// class Image
		IWMainApplication iwma = iwc.getIWMainApplication();
		return IWCacheManager.getInstance(iwma).getCachedBlobObject(
				com.idega.block.image.data.ImageEntity.class.getName(), imageId, iwma);
	}

	private String getRealPathToImageAndImageEntity(IWContext iwc, int originalImageID) {
		Cache cachedImage = getCachedImage(iwc, originalImageID);
		return cachedImage.getRealPathToFile();
	}

	private ImageEncoder getImageEncoder(IWContext iwc) throws RemoteException {
		return (ImageEncoder) IBOLookup.getServiceInstance(iwc, ImageEncoder.class);
	}

	private ImageProvider getImageProvider(IWContext iwc) throws RemoteException {
		return (ImageProvider) IBOLookup.getServiceInstance(iwc, ImageProvider.class);
	}

	public void start() {
		runThread = true;
		if (thread == null || !isRunning) {
			//a new thread must be created here because it was null or 
			//we went out of the run() method. When run is finished the thread is considered dead and cannot be restarted
			thread = new Thread(this, "ImageProcessor Thread");
			//this is a backround task
			thread.setPriority(Thread.NORM_PRIORITY);
			thread.start();
		}
	}

	public void stop() {
		if (thread != null) {
			runThread = false;
			thread.interrupt();
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
		unprocessedImages.put(job.getJobKey(), job);
	}

	/**
	 * Tells you if it is already being processed or is in the queu
	 * 
	 * @param key
	 * @return
	 */
	private boolean isInProcessOrDone(String key) {
		return unprocessedImages.containsKey(key) || inProcessOrDone.contains(key);
	}

	/** Destroy the thread */
	public void destroy() {
		stop();
		thread = null;
	}

	private ImageEncoder getImageEncoder() throws RemoteException {
		return (ImageEncoder) IBOLookup.getServiceInstance(iwac, ImageEncoder.class);
	}

	private ImageProvider getImageProvider() throws RemoteException {
		return (ImageProvider) IBOLookup.getServiceInstance(iwac, ImageProvider.class);
	}
}