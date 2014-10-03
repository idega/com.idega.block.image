package com.idega.block.image.presentation;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.idega.block.image.business.ImageProvider;
import com.idega.block.image.business.ImagesProviderGeneric;
import com.idega.block.web2.business.Web2Business;
import com.idega.business.IBOLookup;
import com.idega.business.IBORuntimeException;
import com.idega.core.builder.data.ICPage;
import com.idega.core.idgenerator.business.UUIDGenerator;
import com.idega.idegaweb.IWBundle;
import com.idega.presentation.Block;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Paragraph;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.SubmitButton;
import com.idega.util.PresentationUtil;
import com.idega.util.expression.ELUtil;

/**
 *
 * Title: idegaWeb Description: ImageGallery is a block to show images that are
 * stored in a specified folder. A subset of these images is shown at a time.
 * The sample can be changed by clicking on a forward and a back button. If
 * there are more than one ImageGallery on a single page each gallery works
 * independently of the others.
 *
 * Copyright: Copyright (c) 2003 Company: idega software
 *
 * @author <a href="mailto:eiki@idega.is">Eirikur S. Hrafnsson</a>
 * @version 2.0
 */
public class ImageGallery extends Block {

	private static final String STYLE_CLASS_GALLERY_BUTTONS = "galleryButtons";

	// repository path to resource folder
	private String resourceFilePath;
	// enlarge image to specified height and width
	private boolean enlargeImage = false;
	// height of the images
	private int heightOfImages = -1;
	// width of the images
	private int widthOfImages = -1;

	// show name of image
	private boolean showNameOfImage = false;
	// number of new images that is shown per step
	private int numberOfImagesPerStep = 0;
	// flag to show if the image should keep its proportion
	private boolean scaleProportional = true;
	private String heightOfGallery = null;
	private String widthOfGallery = null;
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
	private int totalCountOfImages = -1;
	// end of deprecated stuff


	public ImageGallery() {
	}

	@Override
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
	 * @deprecated does nothing since image gallery uses slimbox (lightbox)
	 */
	@Deprecated
	public void setViewerPage(ICPage viewerPage) {
	}

	public void setScaleProportional(boolean scaleProportional) {
		this.scaleProportional = scaleProportional;
	}

	/**
	 * @deprecated use useNumberOfImagePerStep
	 * @param rows
	 */
	@Deprecated
	public void setRows(int rows) {
	}

	/**
	 * @deprecated use useNumberOfImagePerStep
	 * @param columns
	 */
	@Deprecated
	public void setColumns(int columns) {
	}

	public void setShowNameOfImage(boolean showNameOfImage) {
		this.showNameOfImage = showNameOfImage;
	}

	/**
	 * @deprecated now uses a lightbox
	 */
	@Deprecated
	public void setPopUpOriginalImageOnClick(boolean popUpOriginalImageOnClick) {
		//does nothing
	}

	public void setNumberOfImagesPerStep(int numberOfImagesPerStep) {
		this.numberOfImagesPerStep = numberOfImagesPerStep;
	}

	public int getNumberOfImagesPerStep() {
		return this.numberOfImagesPerStep;
	}

	/**
	 * @deprecated use CSS style instead (default is galleryImage) , this sets
	 *             padding to each image now via inline style
	 * @param cellPaddingTable
	 */
	@Deprecated
	public void setCellPadding(int cellPaddingTable) {
		this.paddingOfImage = cellPaddingTable;
	}

	@Override
	public void setStyleClass(String className) {
		this.styleClassName = className;
	}

	public void setImageLayerStyleClass(String className) {
		this.imageStyleClassName = className;
	}

	@Override
	public void main(IWContext iwc) throws Exception {
		Web2Business web2 = ELUtil.getInstance().getBean(Web2Business.class);
		IWBundle iwb = getBundle(iwc);

		List<String> scriptsUris = new ArrayList<String>();
		scriptsUris.add(web2.getBundleURIToMootoolsLib());
		scriptsUris.add(web2.getSlimboxScriptFilePath());
		PresentationUtil.addJavaScriptSourcesLinesToHeader(iwc, scriptsUris);			//	JavaScript
		PresentationUtil.addStyleSheetToHeader(iwc, web2.getSlimboxStyleFilePath());	//	CSS
		PresentationUtil.addStyleSheetToHeader(iwc, iwb.getVirtualPathWithFileNameString("style/image.css"));

		Layer imageGalleryLayer = new Layer(Layer.DIV);
		imageGalleryLayer.setStyleClass("album-wrapper "+this.styleClassName);
		//imageGalleryLayer.setStyleClass(this.styleClassName);

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


		/* backward compatibility */
		if (this.heightOfGallery != null) {
			imageGalleryLayer.setHeight(this.heightOfGallery);
		}
		if (this.widthOfGallery != null) {
			imageGalleryLayer.setWidth(this.widthOfGallery);
		}
		/* backward compatibility ends */
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

		List<AdvancedImage> images = getImages(iwc);
		String idOfGallery = this.getId();

		Paragraph name = new Paragraph();
		name.setStyleClass("thumbnail-caption");

		Layer imageAndText = new Layer(Layer.DIV);
		imageAndText.setStyleClass("thumbnail-wrap " +this.imageStyleClassName);

		Layer imageLayer = new Layer(Layer.DIV);
		imageLayer.setStyleClass("thumbnail-frame");

		AdvancedImage image;
		Iterator<AdvancedImage> iterator = images.iterator();
		while (iterator.hasNext()) {
			image = iterator.next();
			Layer wrapper = (Layer) imageAndText.clone();
			wrapper.setId(UUIDGenerator.getInstance().generateId());
			wrapper.setStyleAttribute("width", ""+this.widthOfImages);
			if(this.heightOfImages==-1){
				//yes height the same on purpose
				wrapper.setStyleAttribute("height", ""+this.widthOfImages);
			}

			imageGalleryLayer.add(wrapper);

			if (this.heightOfImages > 0) {
				image.setHeight(this.heightOfImages);
			}
			if (this.widthOfImages > 0) {
				image.setWidth(this.widthOfImages);
			}
			// set properties of advanced image
			image.setEnlargeProperty(this.enlargeImage);
			image.setScaleProportional(this.scaleProportional);

			//todo reflect as an option?
			//image.setStyleClass("reflect rheight10");

			Link link = new Link(image);
			String resourceURI = image.getResourceURI();
			link.setToolTip(image.getName());
			link.setURL(resourceURI);
			link.setMarkupAttribute("rel", "lightbox["+idOfGallery+"]");

			wrapper.add(link);

			if (this.showNameOfImage) {
				Paragraph theName = (Paragraph) name.clone();
				theName.add(image.getName());
				wrapper.add(theName);
			}

			// deprecated backward compatability stuff
			if (this.paddingOfImage > 0) {
				image.setPadding(this.paddingOfImage);
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
//		add everything
		imageGalleryLayer.add(buttonsLayer);


		int limit = getTotalImageCount(iwc);
//		TODO: cleanup
//		if (this.resourceFilePath != null) {
//			limit = getTotalImageCount(iwc);
//		}
		int startPosition = restoreNumberOfFirstImage(iwc);
		int endPosition;
		int imageSpotsAvailable = getNumberOfImagesPerStep();
		if(imageSpotsAvailable==0){
			imageSpotsAvailable = limit;
		}

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

	protected int getTotalImageCount(IWContext iwc) {

		if(totalCountOfImages == -1) {
			totalCountOfImages = getImagesProvider(iwc).getImageCount();
		}

		return totalCountOfImages;
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
	protected List<AdvancedImage> getImages(IWContext iwc) throws Exception {
		int step = getNumberOfImagesPerStep();
		int startPosition = restoreNumberOfFirstImage(iwc);
		int newStartPosition;
		int limit = getTotalImageCount(iwc);

		if(step == 0) {
			step = limit;
		}

//		TODO: cleanup
//		if (this.resourceFilePath != null) {
//			limit = getTotalImageCount(iwc);
//			if(step==0){
//				step = limit;
//			}
//		}

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

		return getImagesFromTo(iwc, startPosition, startPosition + step - 1);
	}

	protected List<AdvancedImage> getImagesFromTo(IWContext iwc, int startPosition, int endPosition) {
		//todo optimize calls to imageprovider, this is almost the same as before
		return getImagesProvider(iwc).getImagesFromTo(startPosition, endPosition);
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

	protected ImageProvider getImageProvider(IWContext iwc) {

		try {
			return IBOLookup.getServiceInstance(iwc, ImageProvider.class);

        } catch (RemoteException e) {
	        throw new IBORuntimeException(e);
        }
	}

	protected ImagesProviderGeneric getImagesProvider(IWContext iwc) {

		ImagesProviderGeneric imagesProvider = getExpressionValue(iwc, "imagesProvider");

		if(imagesProvider == null) {

			final ImageProvider imageProvider = getImageProvider(iwc);
			final String resourceFilePath = this.resourceFilePath;

//			TODO: we could cache this either on resourceFilePath, or at least as a property in ImageGallery (saving 1 object for request)
			imagesProvider = new ImagesProviderGeneric() {

				@Override
				public int getImageCount() {
					try {
						return resourceFilePath != null ? imageProvider.getImageCount(resourceFilePath) : 0;

                    } catch (RemoteException e) {
                    	throw new IBORuntimeException(e);
                    }
                }

				@Override
				public List<AdvancedImage> getImagesFromTo(int startPosition,
                        int endPosition) {
					try {
						List<AdvancedImage> images = imageProvider.getImagesFromTo(resourceFilePath, startPosition, endPosition);
						return images;

                    } catch (RemoteException e) {
                    	throw new IBORuntimeException(e);
                    }
                }
			};
		}

		return imagesProvider;
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
	@Deprecated
	public void setHeightOfGallery(String height) {
		this.heightOfGallery = height;
	}

	/**
	 * @param width
	 * @deprecated use CSS styles instead
	 */
	@Deprecated
	public void setWidthOfGallery(String width) {
		this.widthOfGallery = width;
	}

}
