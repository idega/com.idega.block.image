/*
 * $Id$ Created on Feb 10, 2006
 * 
 * Copyright (C) 2006 Idega Software hf. All Rights Reserved.
 * 
 * This software is the proprietary information of Idega hf. Use is subject to
 * license terms.
 */
package com.idega.block.image.presentation;

import java.rmi.RemoteException;
import java.util.List;

import javax.faces.context.FacesContext;

import com.idega.business.IBOLookup;
import com.idega.business.IBORuntimeException;
import com.idega.presentation.IWBaseComponent;
import com.idega.presentation.IWContext;
import com.idega.presentation.Image;
import com.idega.slide.business.IWSlideService;

public class ImageRotator extends IWBaseComponent {

	private String iFolderURI;
	private String iWidth;
	private String iHeight;
	private String iAlt;

	/*
	 * (non-Javadoc)
	 * @see com.idega.presentation.IWBaseComponent#initializeComponent(javax.faces.context.FacesContext)
	 */
	protected void initializeComponent(FacesContext context) {
		IWContext iwc = IWContext.getIWContext(context);
		
		List<?> imagePaths = null;
		try {
			IWSlideService service = (IWSlideService) IBOLookup.getServiceInstance(iwc, IWSlideService.class);
			imagePaths = service.getChildPathsExcludingFoldersAndHiddenFiles(getFolderURI());
		}
		catch (RemoteException e) {
			throw new IBORuntimeException(e);
		}

		if (imagePaths != null) {
			String imageURL = getRandomURL(imagePaths);

			Image image = new Image(imageURL);
			if (getAlt() != null) {
				image.setAlt(getAlt());
			}
			if (getHeight() != null) {
				image.setHeight(getHeight());
			}
			if (getWidth() != null) {
				image.setWidth(getWidth());
			}
			add(image);
		}
	}

	private String getRandomURL(List<?> imageURLs) {
		int num = (int) (Math.random() * imageURLs.size());
		return (String) imageURLs.get(num);
	}

	/**
	 * @see javax.faces.component.UIComponentBase#saveState(javax.faces.context.FacesContext)
	 */
	public Object saveState(FacesContext ctx) {
		Object values[] = new Object[5];
		values[0] = super.saveState(ctx);
		values[1] = getAlt();
		values[2] = getFolderURI();
		values[3] = getHeight();
		values[4] = getWidth();
		return values;
	}

	/**
	 * @see javax.faces.component.UIComponentBase#restoreState(javax.faces.context.FacesContext,
	 *      java.lang.Object)
	 */
	public void restoreState(FacesContext ctx, Object state) {
		Object values[] = (Object[]) state;
		super.restoreState(ctx, values[0]);
		setAlt((String) values[1]);
		setFolderURI((String) values[2]);
		setHeight((String) values[3]);
		setWidth((String) values[4]);
	}

	public String getAlt() {
		return this.iAlt;
	}

	public void setAlt(String alt) {
		this.iAlt = alt;
	}

	public String getFolderURI() {
		return this.iFolderURI;
	}

	public void setFolderURI(String folderURI) {
		this.iFolderURI = folderURI;
	}

	public String getHeight() {
		return this.iHeight;
	}

	public void setHeight(String height) {
		this.iHeight = height;
	}

	public String getWidth() {
		return this.iWidth;
	}

	public void setWidth(String width) {
		this.iWidth = width;
	}
}