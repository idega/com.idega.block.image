/*
 * $Id: ImageProcessJob.java,v 1.5 2007/04/11 12:54:40 eiki Exp $
 * Created on Sep 30, 2004
 *
 * Copyright (C) 2004 Idega Software hf. All Rights Reserved.
 *
 * This software is the proprietary information of Idega hf.
 * Use is subject to license terms.
 */
package com.idega.block.image.data;



/**
 * 
 *  Last modified: $Date: 2007/04/11 12:54:40 $ by $Author: eiki $
 * 
 * @author <a href="mailto:thomas@idega.com">thomas</a>
 * @version $Revision: 1.5 $
 */
public class ImageProcessJob {
		
	private String loc;
	private int newWidth;
	private int newHeight;
	private String newExtension;
	private String mimeType;
	private String jobKey;
	private String modifiedImageURI;

	public void setImageLocation(String loc) {
		this.loc = loc;
	}
	
	public String getImageLocation() {
		return this.loc;
	}
	
	private boolean locIsUrl = false;
	
	public void setLocationIsURL(boolean locationIsUrl) {
		this.locIsUrl = locationIsUrl;
	}
	
	public boolean getLocationIsURL() {
		return this.locIsUrl;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}
	
	public String getMimeType() {
		return this.mimeType;
	}

	/**
	 * 
	 * @return The path to the modified image
	 */
	public String getJobKey() {
		return this.jobKey;
	}
	
	/*
	 * A unique key, the path to the modified image
	 */
	public void setJobKey(String jobKey) {
		this.jobKey = jobKey;
	}

	public String getNewExtension() {
		return this.newExtension;
	}
	public void setNewExtension(String newExtension) {
		this.newExtension = newExtension;
	}
	public int getNewHeight() {
		return this.newHeight;
	}
	public void setNewHeight(int newHeight) {
		this.newHeight = newHeight;
	}
	public int getNewWidth() {
		return this.newWidth;
	}
	public void setNewWidth(int newWidth) {
		this.newWidth = newWidth;
	}

	public String getModifiedImageURI() {
		return modifiedImageURI;
	}

	public void setModifiedImageURI(String modifiedImageURI) {
		this.modifiedImageURI = modifiedImageURI;
	}

}
