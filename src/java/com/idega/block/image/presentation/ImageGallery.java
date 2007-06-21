package com.idega.block.image.presentation;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;

import com.idega.block.image.business.ImageProvider;
import com.idega.block.web2.business.Web2Business;
import com.idega.business.IBOLookup;
import com.idega.business.SpringBeanLookup;
import com.idega.core.builder.data.ICPage;
import com.idega.core.idgenerator.business.UUIDGenerator;
import com.idega.presentation.Block;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.presentation.Page;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Paragraph;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.SubmitButton;

/**
 * *
 * 
 * Title: idegaWeb Description: ImageGallery is a block to show images that are
 * stored in a specified folder. A subset of these images is shown in a table.
 * The sample can be changed by clicking on a forward and a back button. If
 * there are more than one ImageGallery on a single page each gallery works
 * independently of the others.
 * 
 * Copyright: Copyright (c) 2003 Company: idega software
 * 
 * @author <a href="mailto:thomas@idega.is">Thomas Hilbig </a>
 * @version 1.0
 */
public class ImageGallery extends Block {

	public static final String STYLE_CLASS_GALLERY_IMAGE_TITLE = "galleryImageTitle";
	public static final String STYLE_CLASS_LAST_IN_ROW = "lastInRow";
	public static final String STYLE_CLASS_FIRST_IN_ROW = "firstInRow";
	private static final String STYLE_CLASS_GALLERY_BUTTONS = "galleryButtons";

	// slide path to resource folder
	private String resourceFilePath = null;
	// enlarge image to specified height and width
	private boolean enlargeImage = false;
	// heigth of the images
	private int heightOfImages = -1;
	// width of the images
	private int widthOfImages = -1;
	// page where the images are shown when you click on it
	/* @deprecated, now we use a lightbox */
	private ICPage viewerPage;
	// show image in a special popup window
	/* @deprecated, now we use a lightbox */
	private boolean popUpOriginalImageOnClick = false;
	// show name of image in table
	private boolean showNameOfImage = false;
	// number of new images that is shown per step
	private int numberOfImagesPerStep = 0;
	// flag to show if the image should keep it�s proportion
	private boolean scaleProportional = true;
	private String heightOfGallery = null;
	private String widthOfGallery = null;
	private int rows = 1;
	private int columns = 1;
	// corresponding bundle
	private static final String IW_BUNDLE_IDENTIFIER = "com.idega.block.image";
	// string forward button
	private static final String STRING_FORWARD_BUTTON = ">";
	// string back button
	private static final String STRING_BACK_BUTTON = "<";
	public static final int BUTTON_POSITON_BOTTOM = 0;
	public static final int BUTTON_POSITON_TOP = 1;

	private int _posButton = BUTTON_POSITON_BOTTOM;
	private String styleClassName = "imageGallery";
	private String imageStyleClassName = "galleryImage";
	// deprecated stuff
	// border of all images
	private int paddingOfImage = 0;
	private boolean showButtons = true;

	// end of deprecated stuff
	public ImageGallery() {
	}

	public String getBundleIdentifier() {
		return ImageGallery.IW_BUNDLE_IDENTIFIER;
	}

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

	/**
	 * 
	 * @param viewerPage
	 * @deprecated
	 */
	public void setViewerPage(ICPage viewerPage) {
		this.viewerPage = viewerPage;
	}

	public void setScaleProportional(boolean scaleProportional) {
		this.scaleProportional = scaleProportional;
	}

	public void setRows(int rows) {
		if (rows > 0) {
			this.rows = rows;
		}
	}

	public void setColumns(int columns) {
		if (columns > 0) {
			this.columns = columns;
		}
	}

	public void setShowNameOfImage(boolean showNameOfImage) {
		this.showNameOfImage = showNameOfImage;
	}

	/**
	 * 
	 * @param viewerPage
	 * @deprecated
	 */
	public void setPopUpOriginalImageOnClick(boolean popUpOriginalImageOnClick) {
		this.popUpOriginalImageOnClick = popUpOriginalImageOnClick;
	}

	public void setNumberOfImagesPerStep(int numberOfImagesPerStep) {
		this.numberOfImagesPerStep = numberOfImagesPerStep;
	}

	/**
	 * @deprecated use CSS style instead (default is galleryImage) , this sets
	 *             padding to each image now via inline style
	 * @param cellPaddingTable
	 */
	public void setCellPadding(int cellPaddingTable) {
		this.paddingOfImage = cellPaddingTable;
	}

	public void setStyleClass(String className) {
		this.styleClassName = className;
	}

	public void setImageLayerStyleClass(String className) {
		this.imageStyleClassName = className;
	}

	public void main(IWContext iwc) throws Exception {

		Web2Business web2 = (Web2Business) SpringBeanLookup.getInstance().getSpringBean(iwc, Web2Business.class);

		Page parentPage = this.getParentPage();
		if(parentPage!=null){
			
			parentPage.addStyleSheetURL(web2.getThickboxStyleFilePath());
			
			//parentPage.addStyleSheetURL(web2.getLightboxStyleFilePath());
			
			//Script script = parentPage.getAssociatedScript();
	
//			script.addScriptSource(web2.getBundleURIToPrototypeLib());
//			script.addScriptSource(web2.getBundleURIToScriptaculousLib()+"?load=effects");
//			script.addScriptSource(web2.getLightboxScriptFilePath());
			
			parentPage.addScriptSource(web2.getBundleURIToJQueryLib());
			parentPage.addScriptSource(web2.getThickboxScriptFilePath());
				
		}

		Layer imageGalleryLayer = new Layer(Layer.DIV);
		//imageGalleryLayer.setStyleClass("album-wrapper "+this.styleClassName);
		imageGalleryLayer.setStyleClass(this.styleClassName);
		
		
		add(imageGalleryLayer);

		switch (this._posButton) {
		case BUTTON_POSITON_TOP:
			if(this.showButtons){
				addButtons(iwc, imageGalleryLayer);
			}
			addImages(iwc, imageGalleryLayer);
			break;
		default:
			addImages(iwc, imageGalleryLayer);
		if(this.showButtons){
			addButtons(iwc, imageGalleryLayer);
		}
		break;
		}


		/* backward compatability */
		if (this.heightOfGallery != null) {
			imageGalleryLayer.setHeight(this.heightOfGallery);
		}
		if (this.widthOfGallery != null) {
			imageGalleryLayer.setWidth(this.widthOfGallery);
		}
		/* backward compatability ends */
	}

	/**
	 * Adds images and text if needed to the imageGalleryLayer and sets various
	 * style class too the items
	 * 
	 * @param iwc
	 * @param imageGalleryLayer
	 * @throws Exception
	 */
	protected void addImages(IWContext iwc, Layer imageGalleryLayer) throws Exception {

		ArrayList images = getImages(iwc);

		Paragraph name = new Paragraph();
		name.setStyleClass("thumbnail-caption " + STYLE_CLASS_GALLERY_IMAGE_TITLE);
		
		Layer imageAndText = new Layer(Layer.DIV);
		//imageAndText.setStyleClass("thumbnail-wrap" +this.imageStyleClassName);
		imageAndText.setStyleClass(this.imageStyleClassName);
		
		Layer imageLayer = new Layer(Layer.DIV);
		imageLayer.setStyleClass("thumbnail-frame " +this.imageStyleClassName);
		
		AdvancedImage image;
		int count = -1;
		Iterator iterator = images.iterator();
		int imageNumber = restoreNumberOfFirstImage(iwc);
		while (iterator.hasNext()) {
			count++;
			image = (AdvancedImage) iterator.next();
			Layer wrapper = (Layer) imageAndText.clone();
			wrapper.setId(UUIDGenerator.getInstance().generateId());
			wrapper.setStyleAttribute("width", ""+this.widthOfImages);
			//yes height the same on purpose
			wrapper.setStyleAttribute("height", ""+this.widthOfImages);
			
			imageGalleryLayer.add(wrapper);
			
//			Layer imageAndTitle = (Layer) imageLayer.clone();
//			imageAndTitle.setId(UUIDGenerator.getInstance().generateId());
//			imageAndTitle.setStyleAttribute("width", ""+this.widthOfImages);
//			
//			wrapper.add(imageAndTitle);
			
			// todo have a set method
			if (this.heightOfImages > 0) {
				image.setHeight(this.heightOfImages);
			}
			if (this.widthOfImages > 0) {
				image.setWidth(this.widthOfImages);
			}
			// set properties of advanced image
			image.setEnlargeProperty(this.enlargeImage);
			image.setScaleProportional(this.scaleProportional);
			// deprecated backward compatability stuff
			if (this.paddingOfImage > 0) {
				image.setPadding(this.paddingOfImage);
			}
			
			// check if a link to a viewer page should be added
//			if (this.viewerPage != null) {
			Link link = new Link(image);
			String resourceURI = image.getResourceURI();
			link.setToolTip(image.getName());
			link.setURL(resourceURI);
			link.setStyleClass("thickbox");
//			link.setMarkupAttribute("rel", "thickbox-"+this.getId());
			link.setMarkupAttribute("rel", "thickboxes");

			imageNumber++;
			int xPositionImage = ((count % this.columns) + 1);
			// why clone?
			//imageAndTitle.add(link);
			wrapper.add(link);
			
			// add extra style classes for first and last elements of each row
			// for styling purposes
			if (xPositionImage == 1) {
				wrapper.setStyleClass(STYLE_CLASS_FIRST_IN_ROW);
				//imageAndTitle.setStyleClass(STYLE_CLASS_FIRST_IN_ROW);
				
			}
			else if (xPositionImage == this.columns) {
				wrapper.setStyleClass(STYLE_CLASS_LAST_IN_ROW);
//				imageAndTitle.setStyleClass(STYLE_CLASS_LAST_IN_ROW);
			}
			
			if (this.showNameOfImage) {
				Paragraph theName = (Paragraph) name.clone();
				theName.add(image.getName());
				
			//	imageAndTitle.add(theName);
				wrapper.add(theName);
			}
			
			//Older deprecated stuff
//			link.setPage(this.viewerPage);
//			if (resourceURI != null) {
//				link.addParameter(Image.PARAM_IMAGE_URL, resourceURI);
//			}
//			else {
//				link.addParameter(Image.PARAM_IMAGE_ID, image.getImageID(iwc));
//			}

//			}
//			else if (this.popUpOriginalImageOnClick) {
//			// check if a link to a popup window should be added
//			image.setLinkToDisplayWindow(imageNumber);

//			pres = image;
//			}
//			else {
//			// show only the image without a link
//			pres = image;
//			}
		}
	}

	private SubmitButton createButton(String displayText) {
		SubmitButton button = new SubmitButton(Integer.toString(this.getICObjectInstanceID()), displayText);
		button.setToEncloseByForm(true);
		return button;
	}

	private void addButtons(IWContext iwc, Layer imageGalleryLayer) throws Exception {
		SubmitButton backButton = createButton(STRING_BACK_BUTTON);
		SubmitButton forwardButton = createButton(STRING_FORWARD_BUTTON);
		Layer buttonsLayer = new Layer(Layer.DIV);
		buttonsLayer.setStyleClass(STYLE_CLASS_GALLERY_BUTTONS);
//		add everything
		imageGalleryLayer.add(buttonsLayer);


		int limit = 0;
		if (this.resourceFilePath != null) {
			limit = getImageProvider(iwc).getImageCount(this.resourceFilePath);
		}
		int startPosition = restoreNumberOfFirstImage(iwc);
		int endPosition;
		int imageSpotsAvailable = getNumberOfImagePlaces();
		StringBuffer infoText = new StringBuffer();

		if(limit<=imageSpotsAvailable && startPosition==1){
			//all images can be shown if we are at the start and there are more spots available than number of all images
			return;
		}

		if ((endPosition = startPosition + imageSpotsAvailable - 1) >= limit){
			endPosition = limit;
		}
		// special case: If there are not any images do not show start position
		// one but zero
		int displayedStartPosition = (limit == 0) ? 0 : startPosition;
		// create an info text showing the number of the first image and thelast image
		// that are currently shown and the total numbers of images:
		// for example: 2 - 6 of 9

		// show: "2 - 6 of 9"
		// special case: Only one image is shown, in this case avoid showing: "2 - 2 of 9"
		if (displayedStartPosition != endPosition) {
			infoText.append(" ").append(displayedStartPosition).append("-");
		}
		infoText.append(endPosition).append(" ").append(this.getResourceBundle(iwc).getLocalizedString("of", "of")).append(" ").append(limit);
		// possibly disable buttons
		if (startPosition == 1){
			backButton.setDisabled(true);
		}

		if (endPosition == limit){
			forwardButton.setDisabled(true);
		}
		buttonsLayer.add(backButton);
		buttonsLayer.add(new Text(infoText.toString()));
		buttonsLayer.add(forwardButton);		
	}

	private String getParameter(IWContext iwc) throws Exception {
		return iwc.getParameter(getObjectInstanceIdentifierString());
	}

	/**
	 * Gets a List of AdvancedImage objects
	 * @param iwc
	 * @return
	 * @throws Exception
	 */
	protected ArrayList getImages(IWContext iwc) throws Exception {
		int step = getNumberOfImagesInStep();
		int startPosition = restoreNumberOfFirstImage(iwc);
		int newStartPosition;
		int limit = 0;

		if (this.resourceFilePath != null) {
			limit = getImageProvider(iwc).getImageCount(this.resourceFilePath);
		}		

		String parameterValue = getParameter(iwc);
		if (STRING_FORWARD_BUTTON.equals(parameterValue)) {
			newStartPosition = startPosition + step;
		}
		else if (STRING_BACK_BUTTON.equals(parameterValue)) {
			newStartPosition = startPosition - step;
		}
		else {
			newStartPosition = startPosition;
		}
		if (newStartPosition > 0 && newStartPosition <= limit) {
			startPosition = newStartPosition;
		}
		storeNumberOfFirstImage(iwc, startPosition);
		return getImagesFromTo(iwc, startPosition, startPosition + getNumberOfImagePlaces() - 1);
	}

	protected ArrayList getImagesFromTo(IWContext iwc, int startPosition, int endPosition) throws RemoteException {
		//todo optimize calls to imageprovider, this is almost the same as before
		return getImageProvider(iwc).getImagesFromTo(this.resourceFilePath, startPosition, endPosition);
	}

	private void storeNumberOfFirstImage(IWContext iwc, int firstImageNumber) {
		iwc.setSessionAttribute(getObjectInstanceIdentifierString(), new Integer(firstImageNumber));
	}

	private int restoreNumberOfFirstImage(IWContext iwc) {
		Integer i = (Integer) iwc.getSessionAttribute(getObjectInstanceIdentifierString());
		if (i == null) {
			return 1;
		}
		return i.intValue();
	}

	private String getObjectInstanceIdentifierString() {
		return Integer.toString(this.getICObjectInstanceID());
	}

	protected ImageProvider getImageProvider(IWContext iwc) throws RemoteException {
		return (ImageProvider) IBOLookup.getServiceInstance(iwc, ImageProvider.class);
	}

	private int getNumberOfImagesInStep() {
		int totalSumOfImagesInTable = getNumberOfImagePlaces();
		return (this.numberOfImagesPerStep > 0 && this.numberOfImagesPerStep < totalSumOfImagesInTable) ? this.numberOfImagesPerStep: totalSumOfImagesInTable;
	}

	protected int getNumberOfImagePlaces() {
		// how many images can I show in the current table?
		return this.rows * this.columns;
	}

	public void setToShowButtons(boolean showButtons){
		this.showButtons  = showButtons;		
	}

	/**
	 * @return
	 */
	public int getButtonPosition() {
		return this._posButton;
	}

	/**
	 * @param posConst,
	 *            one of the BOTTON_POSITION_... constants
	 */
	public void setButtonPosition(int posConst) {
		this._posButton = posConst;
	}

	/**
	 * @param height
	 * @deprecated use CSS styles instead
	 */
	public void setHeightOfGallery(String height) {
		this.heightOfGallery = height;
	}

	/**
	 * @param width
	 * @deprecated use CSS styles instead
	 */
	public void setWidthOfGallery(String width) {
		this.widthOfGallery = width;
	}
}
