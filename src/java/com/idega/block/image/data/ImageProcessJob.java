/*
 * $Id: ImageProcessJob.java,v 1.2 2006/02/17 14:53:10 gimmi Exp $
 * Created on Sep 30, 2004
 *
 * Copyright (C) 2004 Idega Software hf. All Rights Reserved.
 *
 * This software is the proprietary information of Idega hf.
 * Use is subject to license terms.
 */
package com.idega.block.image.data;

import com.idega.util.caching.Cache;


/**
 * 
 *  Last modified: $Date: 2006/02/17 14:53:10 $ by $Author: gimmi $
 * 
 * @author <a href="mailto:thomas@idega.com">thomas</a>
 * @version $Revision: 1.2 $
 */
public class ImageProcessJob {
	
	private ImageEntity entity;
	
	public void setImageEntity(ImageEntity ent) {
		entity = ent;
	}
	
	public ImageEntity getImageEntity() {
		return entity;
	}
	
	private String loc;
	
	public void setImageLocation(String loc) {
		this.loc = loc;
	}
	
	public String getImageLocation() {
		return loc;
	}
	
	private boolean locIsUrl = false;
	
	public void setLocationIsURL(boolean locationIsUrl) {
		this.locIsUrl = locationIsUrl;
	}
	
	public boolean getLocationIsURL() {
		return locIsUrl;
	}
	
	private String id;
	
	public void setID(String id) {
		this.id = id;
	}
	
	public String getID() {
		return id;
	}
	
	private String mimeType;
	
	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}
	
	public String getMimeType() {
		return mimeType;
	}

	private String name;
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	private String jobKey;
	
	public String getJobKey() {
		return jobKey;
	}
	public void setJobKey(String jobKey) {
		this.jobKey = jobKey;
	}
	private Cache cachedImage;
	
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
		return newExtension;
	}
	public void setNewExtension(String newExtension) {
		this.newExtension = newExtension;
	}
	public int getNewHeight() {
		return newHeight;
	}
	public void setNewHeight(int newHeight) {
		this.newHeight = newHeight;
	}
	public int getNewWidth() {
		return newWidth;
	}
	public void setNewWidth(int newWidth) {
		this.newWidth = newWidth;
	}
}
