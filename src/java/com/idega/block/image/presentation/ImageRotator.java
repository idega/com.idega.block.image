/*
 * $Id$ Created on Feb 10, 2006
 *
 * Copyright (C) 2006 Idega Software hf. All Rights Reserved.
 *
 * This software is the proprietary information of Idega hf. Use is subject to
 * license terms.
 */
package com.idega.block.image.presentation;

import java.io.IOException;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.jcr.RepositoryException;

import com.idega.business.IBORuntimeException;
import com.idega.presentation.Image;
import com.idega.presentation.PresentationObjectTransitional;

public class ImageRotator extends PresentationObjectTransitional {

	private String iFolderURI;
	private String iWidth;
	private String iHeight;
	private String iAlt;

	/*
	 * (non-Javadoc)
	 *
	 * @see com.idega.presentation.PresentationObjectTransitional#encodeBegin(javax.faces.context.FacesContext)
	 */
	@Override
	public void encodeBegin(FacesContext context) throws IOException {
		super.encodeBegin(context);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.idega.presentation.PresentationObjectTransitional#encodeChildren(javax.faces.context.FacesContext)
	 */
	@Override
	public void encodeChildren(FacesContext context) throws IOException {
		super.encodeChildren(context);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.idega.presentation.PresentationObjectTransitional#encodeEnd(javax.faces.context.FacesContext)
	 */
	@Override
	public void encodeEnd(FacesContext arg0) throws IOException {
		super.encodeEnd(arg0);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.idega.presentation.PresentationObjectTransitional#initializeComponent(javax.faces.context.FacesContext)
	 */
	@Override
	protected void initializeComponent(FacesContext context) {
		List<String> imagePaths = null;
		try {
			imagePaths = getRepositoryService().getChildPathsExcludingFoldersAndHiddenFiles(getFolderURI());
		} catch (RepositoryException e) {
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
			getChildren().add(image);
		}
	}

	private String getRandomURL(List<String> imageURLs) {
		int num = (int) (Math.random() * imageURLs.size());
		return imageURLs.get(num);
	}

	/**
	 * @see javax.faces.component.UIComponentBase#saveState(javax.faces.context.FacesContext)
	 */
	@Override
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
	@Override
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

	@Override
	public String getHeight() {
		return this.iHeight;
	}

	@Override
	public void setHeight(String height) {
		this.iHeight = height;
	}

	@Override
	public String getWidth() {
		return this.iWidth;
	}

	@Override
	public void setWidth(String width) {
		this.iWidth = width;
	}
}