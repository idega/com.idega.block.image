package com.idega.block.image.presentation;

import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.List;

import com.idega.block.image.business.ImageProvider;
import com.idega.business.IBOLookup;
import com.idega.core.builder.data.ICPage;
import com.idega.idegaweb.IWBundle;
import com.idega.presentation.Block;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.util.PresentationUtil;

/**
 * MultiImageGallery is a block to show multiple ImageGallery blocks at once.
 * It scans the supplied repository folder for image folders and adds one ImageGallery block for each of them.
 *
 * Copyright: Copyright (c) 2006 Company: idega software
 *
 * @author <a href="mailto:eiki@idega.is">Eirikur Hrafnsson</a>
 * @version 1.0
 */
public class MultiImageGallery extends Block {

	private String resourceFilePath = null;
	// enlarge image to specified height and width
	private boolean enlargeImage = false;
	// heigth of the images
	private int heightOfImages = -1;
	// width of the images
	private int widthOfImages = -1;
	// page where the images are shown when you click on it
	private ICPage viewerPage;
	// show image in a special popup window
	private boolean popUpOriginalImageOnClick = false;
	private boolean scaleProportional = true;


	// corresponding bundle
	private static final String IW_BUNDLE_IDENTIFIER = "com.idega.block.image";

	private String styleClassName = "multiImageGallery";

	public MultiImageGallery() {
	}

	@Override
	public String getBundleIdentifier() {
		return IW_BUNDLE_IDENTIFIER;
	}

	/**
	 * Path to a folder of folders of images
	 * @param resourcePath
	 */
	public void setFolderResourcePath(String resourcePath) {
		this.resourceFilePath = resourcePath;
	}

	public void setHeightOfImages(int heightOfImages) {
		this.heightOfImages = heightOfImages;
	}

	public void setWidthOfImages(int widthOfImages) {
		this.widthOfImages = widthOfImages;
	}

	public void setEnlargeImage(boolean enlargeImage) {
		this.enlargeImage = enlargeImage;
	}

	public void setViewerPage(ICPage viewerPage) {
		this.viewerPage = viewerPage;
	}

	public void setScaleProportional(boolean scaleProportional) {
		this.scaleProportional = scaleProportional;
	}

	public void setPopUpOriginalImageOnClick(boolean popUpOriginalImageOnClick) {
		this.popUpOriginalImageOnClick = popUpOriginalImageOnClick;
	}

	@Override
	public void setStyleClass(String className) {
		this.styleClassName = className;
	}

	@Override
	public void main(IWContext iwc) throws Exception {
		IWBundle iwb = getBundle(iwc);
		PresentationUtil.addStyleSheetToHeader(iwc, iwb.getVirtualPathWithFileNameString("style/image.css"));

		Layer multiImageGalleryLayer = new Layer(Layer.DIV);
		multiImageGalleryLayer.setStyleClass(this.styleClassName);
		add(multiImageGalleryLayer);

		addGalleries(iwc, multiImageGalleryLayer);
	}


	protected void addGalleries(IWContext iwc, Layer multiImageGalleryLayer) throws Exception {
		List folders = getFolders(iwc);

		for (Iterator iter = folders.iterator(); iter.hasNext();) {
			String folderPath = (String) iter.next();
			ImageGallery gallery = new ImageGallery();
			gallery.setFolderResourcePath(folderPath);
			gallery.setColumns(1);
			gallery.setRows(1);
			gallery.setToShowButtons(false);
			gallery.setScaleProportional(this.scaleProportional);
			gallery.setHeightOfImages(this.heightOfImages);
			gallery.setWidthOfImages(this.widthOfImages);
			gallery.setEnlargeImage(this.enlargeImage);
			gallery.setViewerPage(this.viewerPage);
			gallery.setPopUpOriginalImageOnClick(this.popUpOriginalImageOnClick);

			multiImageGalleryLayer.add(gallery);
		}

	}

	/**
	 * Gets a List of Folders of images
	 * @param iwc
	 * @return
	 * @throws Exception
	 */
	protected List getFolders(IWContext iwc) throws Exception {
		return getImageProvider(iwc).getContainedFolders(this.resourceFilePath);
	}

	protected ImageProvider getImageProvider(IWContext iwc) throws RemoteException {
		return (ImageProvider) IBOLookup.getServiceInstance(iwc, ImageProvider.class);
	}
}
