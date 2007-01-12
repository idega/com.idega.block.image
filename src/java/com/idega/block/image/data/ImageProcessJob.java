/*
 * $Id: ImageProcessJob.java,v 1.1.2.1 2007/01/12 19:32:36 idegaweb Exp $
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
 *  Last modified: $Date: 2007/01/12 19:32:36 $ by $Author: idegaweb $
 * 
 * @author <a href="mailto:thomas@idega.com">thomas</a>
 * @version $Revision: 1.1.2.1 $
 */
public class ImageProcessJob {
	
	private String jobKey;
	
	public String getJobKey() {
		return this.jobKey;
	}
	public void setJobKey(String jobKey) {
		this.jobKey = jobKey;
	}
	private Cache cachedImage;
	
	private int newWidth;
	
	private int newHeight;
	
	String newExtension;
	
	public Cache getCachedImage() {
		return this.cachedImage;
	}
	public void setCachedImage(Cache cachedImage) {
		this.cachedImage = cachedImage;
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
}
