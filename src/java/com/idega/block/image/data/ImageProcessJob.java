/*
 * $Id: ImageProcessJob.java,v 1.1 2004/09/30 17:32:05 thomas Exp $
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
 *  Last modified: $Date: 2004/09/30 17:32:05 $ by $Author: thomas $
 * 
 * @author <a href="mailto:thomas@idega.com">thomas</a>
 * @version $Revision: 1.1 $
 */
public class ImageProcessJob {
	
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
	
	public Cache getCachedImage() {
		return cachedImage;
	}
	public void setCachedImage(Cache cachedImage) {
		this.cachedImage = cachedImage;
	}
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
