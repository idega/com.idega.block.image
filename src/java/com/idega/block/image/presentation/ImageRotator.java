/*
 * $Id$
 * Created on Feb 10, 2006
 *
 * Copyright (C) 2006 Idega Software hf. All Rights Reserved.
 *
 * This software is the proprietary information of Idega hf.
 * Use is subject to license terms.
 */
package com.idega.block.image.presentation;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import javax.faces.context.FacesContext;
import org.apache.commons.httpclient.HttpException;
import org.apache.webdav.lib.WebdavResource;
import org.apache.webdav.lib.WebdavResources;
import com.idega.business.IBOLookup;
import com.idega.business.IBORuntimeException;
import com.idega.presentation.IWContext;
import com.idega.presentation.Image;
import com.idega.presentation.PresentationObjectTransitional;
import com.idega.slide.business.IWSlideSession;
import com.idega.slide.util.WebdavExtendedResource;


public class ImageRotator extends PresentationObjectTransitional {

	private static final String ATTRIBUTE_ROTATOR = "image_rotator_";
	
	private String iFolderURI;
	private String iWidth;
	private String iHeight;
	private String iAlt;
	
	/* (non-Javadoc)
	 * @see com.idega.presentation.PresentationObjectTransitional#encodeBegin(javax.faces.context.FacesContext)
	 */
	public void encodeBegin(FacesContext context) throws IOException {
		super.encodeBegin(context);
	}

	/* (non-Javadoc)
	 * @see com.idega.presentation.PresentationObjectTransitional#encodeChildren(javax.faces.context.FacesContext)
	 */
	public void encodeChildren(FacesContext context) throws IOException {
		super.encodeChildren(context);
	}

	/* (non-Javadoc)
	 * @see com.idega.presentation.PresentationObjectTransitional#encodeEnd(javax.faces.context.FacesContext)
	 */
	public void encodeEnd(FacesContext arg0) throws IOException {
		super.encodeEnd(arg0);
	}
	
	/* (non-Javadoc)
	 * @see com.idega.presentation.PresentationObjectTransitional#initializeComponent(javax.faces.context.FacesContext)
	 */
	protected void initializeComponent(FacesContext context) {
		IWContext iwc = IWContext.getIWContext(context);

		List imageURLs = (List) iwc.getApplicationAttribute(ATTRIBUTE_ROTATOR + getICObjectID());
		
		if (imageURLs == null) {
			try {
				IWSlideSession session = (IWSlideSession) IBOLookup.getSessionInstance(iwc, IWSlideSession.class);
				WebdavExtendedResource resource = session.getWebdavResource(getFolderURI());
				if (resource.isCollection()) {
					WebdavResources children = resource.getChildResources();
					WebdavResource[] resources = children.listResources();
					imageURLs = getImageURLs(resources);
					iwc.setApplicationAttribute(ATTRIBUTE_ROTATOR + getICObjectID(), imageURLs);
				}
			}
			catch (HttpException e) {
				e.printStackTrace();
			}
			catch (RemoteException e) {
				throw new IBORuntimeException(e);
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		if (imageURLs != null) {
			String imageURL = getRandomURL(imageURLs);
			
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
	
	private List getImageURLs(WebdavResource[] resources) {
		List list = new ArrayList();
		for (int i = 0; i < resources.length; i++) {
			WebdavResource resource = resources[i];
			if (!resource.getDisplayName().startsWith(".")) {
				list.add(resource.getPath());
			}
		}
		
		return list;
	}
	
	private String getRandomURL(List imageURLs) {
    int num = (int) (Math.random() * imageURLs.size());
    return (String) imageURLs.get(num - 1);
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
		return iAlt;
	}
	
	public void setAlt(String alt) {
		iAlt = alt;
	}

	public String getFolderURI() {
		return iFolderURI;
	}
	
	public void setFolderURI(String folderURI) {
		iFolderURI = folderURI;
	}
	
	public String getHeight() {
		return iHeight;
	}
	
	public void setHeight(String height) {
		iHeight = height;
	}
	
	public String getWidth() {
		return iWidth;
	}
	
	public void setWidth(String width) {
		iWidth = width;
	}
}