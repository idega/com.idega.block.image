/*
 * $Id: ImageProcessJob.java,v 1.4 2006/04/09 11:38:40 laddi Exp $
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
 *  Last modified: $Date: 2006/04/09 11:38:40 $ by $Author: laddi $
 * 
 * @author <a href="mailto:thomas@idega.com">thomas</a>
 * @version $Revision: 1.4 $
 */
public class ImageProcessJob {
	
	private ImageEntity entity;
	
	public void setImageEntity(ImageEntity ent) {
		this.entity = ent;
	}
	
	public ImageEntity getImageEntity() {
		return this.entity;
	}
	
	private String loc;
	
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
	
	private String id;
	
	public void setID(String id) {
		this.id = id;
	}
	
	public String getID() {
		return this.id;
	}
	
	private String mimeType;
	
	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}
	
	public String getMimeType() {
		return this.mimeType;
	}

	private String name;
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return this.name;
	}
	
	private String jobKey;
	
	public String getJobKey() {
		return this.jobKey;
	}
	public void setJobKey(String jobKey) {
		this.jobKey = jobKey;
	}
	private int newWidth;
	
	private int newHeight;
	
	String newExtension;
	
//	public Cache getCachedImage() {
//		return cachedImage;
//	}
//	public void setCachedImage(Cache cachedImage) {
//		this.cachedImage = cachedImage;
//	}
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
}
