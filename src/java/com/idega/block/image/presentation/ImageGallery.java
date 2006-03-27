package com.idega.block.image.presentation;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import com.idega.block.image.business.ImageProvider;
import com.idega.business.IBOLookup;
import com.idega.core.builder.data.ICPage;
import com.idega.core.file.data.ICFile;
import com.idega.presentation.Block;
import com.idega.presentation.IWContext;
import com.idega.presentation.Image;
import com.idega.presentation.Layer;
import com.idega.presentation.PresentationObject;
import com.idega.presentation.text.Link;
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
	
	// folder with the images
	private ICFile imageFileFolder = null;
	// slide path to resource folder
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
	// show name of image in table
	private boolean showNameOfImage = false;
	// number of new images that is shown per step
	private int numberOfImagesPerStep = 0;
	// flag to show if the image should keep it´s proportion
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

	public void setFilesFolder(ICFile imageFileFolder) {
		this.imageFileFolder = imageFileFolder;
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

	public void setViewerPage(ICPage viewerPage) {
		this.viewerPage = viewerPage;
	}

	public void setScaleProportional(boolean scaleProportional) {
		this.scaleProportional = scaleProportional;
	}

	public void setRows(int rows) {
		if (rows > 0)
			this.rows = rows;
	}

	public void setColumns(int columns) {
		if (columns > 0)
			this.columns = columns;
	}

	public void setShowNameOfImage(boolean showNameOfImage) {
		this.showNameOfImage = showNameOfImage;
	}

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
		Layer imageGalleryLayer = new Layer(Layer.DIV);
		imageGalleryLayer.setStyleClass(styleClassName);
		add(imageGalleryLayer);

		switch (_posButton) {
			case BUTTON_POSITON_TOP:
				if(showButtons){
					addButtons(iwc, imageGalleryLayer);
				}
				addImages(iwc, imageGalleryLayer);
				break;
			default:
				addImages(iwc, imageGalleryLayer);
				if(showButtons){
					addButtons(iwc, imageGalleryLayer);
				}
				break;
		}
		
		
		/* backward compatability */
		if (heightOfGallery != null) {
			imageGalleryLayer.setHeight(heightOfGallery);
		}
		if (widthOfGallery != null) {
			imageGalleryLayer.setWidth(widthOfGallery);
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
		
		Text name = new Text("");
		name.setStyle(STYLE_CLASS_GALLERY_IMAGE_TITLE);
		Layer imageLayer = new Layer(Layer.DIV);
		imageLayer.setStyleClass(imageStyleClassName);
		AdvancedImage image;
		int count = -1;
		Iterator iterator = images.iterator();
		int imageNumber = restoreNumberOfFirstImage(iwc);
		while (iterator.hasNext()) {
			count++;
			image = (AdvancedImage) iterator.next();
			Layer imageAndTextLayer = (Layer) imageLayer.clone();
			imageGalleryLayer.add(imageAndTextLayer);
			// todo have a set method
			if (widthOfImages > 0) {
				image.setHeight(heightOfImages);
			}
			if (heightOfImages > 0) {
				image.setWidth(widthOfImages);
			}
			// set properties of advanced image
			image.setEnlargeProperty(enlargeImage);
			image.setScaleProportional(scaleProportional);
			// deprecated backward compatability stuff
			if (paddingOfImage > 0) {
				image.setPadding(paddingOfImage);
			}
			PresentationObject pres = null;
			// check if a link to a viewer page should be added
			if (viewerPage != null) {
				Link link;
				link = new Link(image);
				link.setPage(viewerPage);
				String resourceURI = image.getResourceURI();
				if (resourceURI != null) {
					link.addParameter(Image.PARAM_IMAGE_URL, resourceURI);
				}
				else {
					link.addParameter(Image.PARAM_IMAGE_ID, image.getImageID(iwc));
				}
				pres = link;
			}
			else if (popUpOriginalImageOnClick) {
				// check if a link to a popup window should be added
				image.setLinkToDisplayWindow(imageNumber);
				pres = image;
			}
			else {
				// show only the image without a link
				pres = image;
			}
			imageNumber++;
			int xPositionImage = ((count % columns) + 1);
			// why clone?
			PresentationObject tmp = (PresentationObject) pres.clone();
			imageAndTextLayer.add(tmp);
			// add extra style classes for first and last elements of each row
			// for styling purposes
			if (xPositionImage == 1) {
				imageAndTextLayer.setStyleClass(STYLE_CLASS_FIRST_IN_ROW);
			}
			else if (xPositionImage == columns) {
				imageAndTextLayer.setStyleClass(STYLE_CLASS_LAST_IN_ROW);
			}
			if (showNameOfImage) {
				Text theName = (Text) name.clone();
				theName.setText(image.getName());
				Layer spacer = new Layer(Layer.DIV);
				spacer.setStyleClass("spacer");
				imageAndTextLayer.add(spacer);
				imageAndTextLayer.add(theName);
			}
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
		
		
		int limit = 0;
		if (imageFileFolder != null) {
			limit = getImageProvider(iwc).getImageCount(imageFileFolder);
		}
		else if (resourceFilePath != null) {
			limit = getImageProvider(iwc).getImageCount(resourceFilePath);
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
		if (startPosition == 1)
			backButton.setDisabled(true);
		if (endPosition == limit)
			forwardButton.setDisabled(true);
		//add everything
		imageGalleryLayer.add(buttonsLayer);
		
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
		
		if (imageFileFolder != null) {
			limit = getImageProvider(iwc).getImageCount(imageFileFolder);
		}
		else if (resourceFilePath != null) {
			limit = getImageProvider(iwc).getImageCount(resourceFilePath);
		}		
		
		String parameterValue = getParameter(iwc);
		if (STRING_FORWARD_BUTTON.equals(parameterValue))
			newStartPosition = startPosition + step;
		else if (STRING_BACK_BUTTON.equals(parameterValue))
			newStartPosition = startPosition - step;
		else
			newStartPosition = startPosition;
		if (newStartPosition > 0 && newStartPosition <= limit)
			startPosition = newStartPosition;
		storeNumberOfFirstImage(iwc, startPosition);
		return getImagesFromTo(iwc, startPosition, startPosition + getNumberOfImagePlaces() - 1);
	}

	private ArrayList getImagesFromTo(IWContext iwc, int startPosition, int endPosition) throws RemoteException,
			java.sql.SQLException {
		//todo optimize calls to imageprovider, this is almost the same as before
		if (imageFileFolder != null) {
			return getImageProvider(iwc).getImagesFromTo(imageFileFolder, startPosition, endPosition);
		}
		else {
			return getImageProvider(iwc).getImagesFromTo(resourceFilePath, startPosition, endPosition);
		}
	}

	private void storeNumberOfFirstImage(IWContext iwc, int firstImageNumber) {
		iwc.setSessionAttribute(getObjectInstanceIdentifierString(), new Integer(firstImageNumber));
	}

	private int restoreNumberOfFirstImage(IWContext iwc) {
		Integer i = (Integer) iwc.getSessionAttribute(getObjectInstanceIdentifierString());
		if (i == null)
			return 1;
		return i.intValue();
	}

	private String getObjectInstanceIdentifierString() {
		return Integer.toString(this.getICObjectInstanceID());
	}

	private ImageProvider getImageProvider(IWContext iwc) throws RemoteException {
		return (ImageProvider) IBOLookup.getServiceInstance(iwc, ImageProvider.class);
	}

	private int getNumberOfImagesInStep() {
		int totalSumOfImagesInTable = getNumberOfImagePlaces();
		return (numberOfImagesPerStep > 0 && numberOfImagesPerStep < totalSumOfImagesInTable) ? numberOfImagesPerStep: totalSumOfImagesInTable;
	}

	protected int getNumberOfImagePlaces() {
		// how many images can I show in the current table?
		return rows * columns;
	}
	
	public void setToShowButtons(boolean showButtons){
		this.showButtons  = showButtons;		
	}

	/**
	 * @return
	 */
	public int getButtonPosition() {
		return _posButton;
	}

	/**
	 * @param posConst,
	 *            one of the BOTTON_POSITION_... constants
	 */
	public void setButtonPosition(int posConst) {
		_posButton = posConst;
	}

	/**
	 * @param height
	 * @deprecated use CSS styles instead
	 */
	public void setHeightOfGallery(String height) {
		heightOfGallery = height;
	}

	/**
	 * @param width
	 * @deprecated use CSS styles instead
	 */
	public void setWidthOfGallery(String width) {
		widthOfGallery = width;
	}
}
