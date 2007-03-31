package com.idega.block.image.presentation;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.Enumeration;
import java.util.Hashtable;

import org.apache.webdav.lib.WebdavResource;

import com.idega.block.image.business.ImageProcessor;
import com.idega.block.image.business.ImageProvider;
import com.idega.block.image.data.ImageProcessJob;
import com.idega.business.IBOLookup;
import com.idega.graphics.ImageInfo;
import com.idega.graphics.image.business.ImageEncoder;
import com.idega.presentation.IWContext;
import com.idega.presentation.Image;
import com.idega.presentation.text.Link;
import com.idega.slide.business.IWSlideService;
import com.idega.slide.util.IWSlideConstants;

/**
 * 
 * 
 * Title: idegaWeb Description: AdvancedImage represents a image and extends
 * {@link com.idega.presentation.Image Image}.
 * 
 * In contrast to the Image class changes of the size by the methods setHeight
 * and setWith are not performed by adding corresponding values and commands to
 * the print method but by creating a new image with the desired size. The new
 * image with the desired size is sent to the client. Therefore the image is not
 * resized on the client side but on the server side.
 * 
 * The height and the width of the image can be set by using the setHeight and
 * setWidth methods of the Image class.
 * 
 * The ImageEncoder service bean is used to create the new image. The new
 * created image is uploaded into the file repository (db or slide) into a branch of the original
 * image(db) or under "/resized" in the images parent folder. The type of the new image is not necessary equal to the type of the
 * original image. The ImageEncoder is responsible for changing the type. (e.g.
 * bitmap is transformed to jpeg).
 * 
 * An instance of this class represents therefore the original image and all
 * derived images: Depending on the values of the height and the width value the
 * corresponding image is used by the print method. A new image is only created
 * if that size was never created before otherwise the desired image is fetched
 * from the database or cache. This means that an access to all modified images
 * and especially to the original image is always possible.
 * 
 * To print the original image when this image is set to a different size the
 * {@link com.idega.block.image.presentation.AdvancedImageWrapper AdvancedImageWrapper}
 * can be used. Use the constructor AdvancedImageWrapper(this) and the wrapper
 * represents the original image, especially the original image is printed if
 * the wrapper is printed.
 * 
 * 
 * Copyright: Copyright (c) 2003 Company: idega software
 * 
 * @author <a href="mailto:thomas@idega.is">Thomas Hilbig</a>
 * @version 1.0
 */
public class AdvancedImage extends Image {

	/** Folder where the modified images are stored */
	public static final String MODIFIED_IMAGES_FOLDER = "modified_images";
	/** border around the image in the popup window */
	private static final String BORDER = "90";

	/** cached value not an attribute */
	private String realPathToImage;

	private int widthOfModifiedImage = 0;
	private int heightOfModifiedImage = 0;
	private int widthOfOriginalImage = 0;
	private int heightOfOriginalImage = 0;
	/**
	 * flag to show if the image should be enlarged or not
	 */
	private boolean enlargeIfNecessary = false;
	/**
	 * flag to show if the image should keep it�s proportion
	 */
	private boolean scaleProportional = true;
	private WebdavResource resource = null;
	private boolean shouldBeModified = false;
	private String nameOfModifiedImage;
	private String fullModifiedImageURL;
	private String originalImageName;
	private String resourceURI;

	public AdvancedImage(WebdavResource webdavResource) {
		super(webdavResource.getPath());

		this.resourceURI = getURL();
		this.resource = webdavResource;
		this.realPathToImage = this.resource.getHttpURL().toString();
		this.setToolTip(getName());
	}

	public void main(IWContext iwc) {
		super.main(iwc);
		if (this.resource != null) {
			try {
				String name = this.getName();
				String extension = name.substring(name.lastIndexOf(".") + 1);
				//todo don't get original width and height unless absolutely necessery e.g. not if both height and width are set and scale proportionally is false
				this.shouldBeModified = checkAndCalculateNewWidthAndHeight(iwc);

				if(shouldBeModified){
					this.nameOfModifiedImage = getNameOfModifiedImageWithExtension(this.widthOfModifiedImage, this.heightOfModifiedImage,extension);

					IWSlideService ss = getImageProvider(iwc).getIWSlideService();

					this.fullModifiedImageURL = ss.getParentPath(this.getResourceURI()) + "/resized/" + this.nameOfModifiedImage;

					if (ss.getExistence(this.fullModifiedImageURL)) {
						//why was this set before??- eiki
						//resource = ss.getWebdavResourceAuthenticatedAsRoot(temp);
						setURL(this.fullModifiedImageURL);
					}
					else {
						scaleImage(iwc);
					}

				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void setEnlargeProperty(boolean enlargeIfNecessary) {
		this.enlargeIfNecessary = enlargeIfNecessary;
	}

	public void setScaleProportional(boolean scaleProportional) {
		this.scaleProportional = scaleProportional;
	}

	/*
	 * Scales the image with an encoder in a seperate thread
	 */
	private void scaleImage(IWContext iwc) {
		try {
			createAndStoreImage(iwc);
			// commented because we want the browser to know the width and
			// height (faster browser rendering)
			// even when the image has not been scaled (otherwise the layout is
			// destroyed)...
			// remove these attributes to prevent scaling by the browser client
			// removeMarkupAttribute(HEIGHT);
			// removeMarkupAttribute(WIDTH);
			//    
		}
		catch (Exception ex) {
			ex.printStackTrace();
			System.err.println("Image could not be modified. Message was: " + ex.getMessage());
		}
	}

	private boolean checkAndCalculateNewWidthAndHeight(IWContext iwc) throws Exception {
		String heightString = getHeight();
		String widthString = getWidth();
		if (heightString == null && widthString == null){
			// image must be not modified
			return false;
		}

		int setHeight = (heightString==null)? 0: Integer.parseInt(heightString);
		int setWidth =  (widthString==null)? 0: Integer.parseInt(widthString);

		// ...there are new settings for the height and the width
		// calculate now the new values for the modified image....
		// first assumption: image must not be modified:
		this.heightOfModifiedImage = 0;
		this.widthOfModifiedImage = 0;
		boolean imageMustBeModified = false;
		// get values of the original image
		this.heightOfOriginalImage = getHeightOfOriginalImage();
		this.widthOfOriginalImage = getWidthOfOriginalImage();


		/*
		 * modify height, if + desired height is defined + image should be
		 * enlarged (if it is smaller than the desired height) + imgage is too
		 * large for the desired heigth
		 */
		if ((this.heightOfOriginalImage < setHeight && this.enlargeIfNecessary) || (this.heightOfOriginalImage > setHeight)) {
			this.heightOfModifiedImage = setHeight;
			imageMustBeModified = true;
		}
		else {
			this.heightOfModifiedImage = this.heightOfOriginalImage;
		}
		/*
		 * modify width, if + desired width is defined + image should be
		 * enlarged (if it is smaller than the desired width) + imgage is too
		 * large for the desired width
		 */
		if ((this.widthOfOriginalImage < setWidth && this.enlargeIfNecessary) || (this.widthOfOriginalImage > setWidth)) {
			this.widthOfModifiedImage = setWidth;
			imageMustBeModified = true;
		}
		else {
			this.widthOfModifiedImage = this.widthOfOriginalImage;
		}
		// resize the image proportional if desired
		if (imageMustBeModified && this.scaleProportional) {

			//proportional calculations
//			width = (orgWidth*height)/orgHeight;
//			height = orgheight* width / orgwidth

			BigInteger widthDesired = BigInteger.valueOf(setWidth);
			BigInteger heightDesired = BigInteger.valueOf(setHeight);
			BigInteger widthOriginal = BigInteger.valueOf(this.widthOfOriginalImage);
			BigInteger heightOriginal = BigInteger.valueOf(this.heightOfOriginalImage);

			// start calculation with big integers
			BigInteger widthFinding = widthOriginal.multiply(heightDesired); //orgWidth*height
			BigInteger heightFinding = heightOriginal.multiply(widthDesired); //orgheight* width

			if (setHeight == 0) {
				// set height to the calculated propertional height
				this.widthOfModifiedImage = setWidth;
				this.heightOfModifiedImage = heightFinding.divide(widthOriginal).intValue();
			}
			else if(setWidth == 0) {
				// set width to the calculated propertional width
				this.heightOfModifiedImage = setHeight;
				this.widthOfModifiedImage = widthFinding.divide(heightOriginal).intValue();
			}
			else{
				this.widthOfModifiedImage = setWidth;
				this.heightOfModifiedImage = setHeight;
			}
		}
		else{
			if(setWidth>0){
				this.widthOfModifiedImage = setWidth;
			}
			if(setHeight>0){
				this.heightOfModifiedImage = setHeight;
			}
		}


//		sometimes the new values equal to the original values:
		// in this case do not modify the image
		if (this.widthOfModifiedImage == this.widthOfOriginalImage && this.heightOfModifiedImage == this.heightOfOriginalImage) {
			// do not modify the image
			this.widthOfModifiedImage = 0;
			this.heightOfModifiedImage = 0;
			return false;
		}


		// end of calculation of the values width and height of the modified
		// image
		return imageMustBeModified;
	}

	/**
	 * Gets height of original image.
	 */
	public int getHeightOfOriginalImage() throws Exception {
		if (this.heightOfOriginalImage <= 0) {		
			// same for both db and slide
			if (this.heightOfOriginalImage <= 0) {
				// this actually sets both the width and height
				readWidthAndHeightFromOriginalImage();
			}
		}
		return this.heightOfOriginalImage;
	}

	/**
	 * Gets width of the original image
	 */
	public int getWidthOfOriginalImage() throws Exception {
		if (this.widthOfOriginalImage <= 0) {
			// same for both db and slide
			if (this.widthOfOriginalImage <= 0) {
				// this actually sets both the width and height
				readWidthAndHeightFromOriginalImage();
			}
		}
		return this.widthOfOriginalImage;
	}

	public String getHeight() {
		String height = super.getHeight();
		// height is set?
		// In this case get the height that was set by the setHeight method of
		// the super class!
		if (height != null) {
			return height;
		}
		// has the height been set before?
		// remember: to prevent that the image is resized on the client side
		// the height attribute was deleted after creating the modified image
		// but the value is stored in the
		// heightOfModifiedImage variable.
		// see: scale() method of this class
		// Therefore:
		if (this.heightOfModifiedImage > 0) {
			return Integer.toString(this.heightOfModifiedImage);
		}
		return null;
	}

	public String getWidth() {
		String width = super.getWidth();
		// width is set?
		// In this case get the width that was set by the setWidth method of the
		// super class!
		if (width != null) {
			return width;
		}
		// has the width been set before?
		// remember: to prevent that the image is resized on the client side
		// the width attribute was deleted after creating the modified image
		// but the value is stored in the
		// widthOfModifiedImage variable.
		// see: scale() method of this class
		// Therefore:
		if (this.widthOfModifiedImage > 0) {
			return Integer.toString(this.widthOfModifiedImage);
		}
		return null;
	}

	/**
	 * Adds a link to this image to a popup window that the original version of
	 * this image shows
	 */
	public void setLinkToDisplayWindow(int imageNumber) {
		Link popUp = getPopUpReadyLink();
		if (this.resource != null) {
			popUp.addParameter(ImageDisplayWindow.PARAMETER_IMAGE_URI, getResourceURI());
		}

		popUp.addParameter(ImageDisplayWindow.PARAMETER_IMAGE_NUMBER, imageNumber);
		setImageZoomLink(popUp);
		setImageLinkZoomView();
	}

	/**
	 * 
	 * @return resource.getPath() if resource is not null (image from slide)
	 */
	public String getResourceURI() {
		return resourceURI;
	}

	public String getName(){
		if(this.originalImageName!=null){
			return this.originalImageName;
		}
		else if(this.resource!=null){
			String path = getResourceURI();
			this.originalImageName = path.substring(path.lastIndexOf("/")+1);
			return this.originalImageName;
		}
		else{
			return super.getName();
		}
	}

	/**
	 * 
	 * @return A link with almost all neccessary parameters set for the
	 *         ImageDisplayWindow
	 */
	public Link getPopUpReadyLink() {
		//TODO remove or implement ImageDisplayWindow for slide, not used in ImageGallery any more.
		Link link = new Link();
		try {
			int width = getWidthOfOriginalImage();
			int height = getHeightOfOriginalImage();
			if (width > 0) {
				link.addParameter(ImageDisplayWindow.PARAMETER_WIDTH, width);
				link.addParameter(ImageDisplayWindow.PARAMETER_HEIGHT, height);
			}
		}
		catch (Exception ex) {
			// do nothing
			// default values of height and width (regarding a pop up window)
			// will be used
		}
		String title = getName();
		link.addParameter(ImageDisplayWindow.PARAMETER_BORDER, BORDER);
		link.addParameter(ImageDisplayWindow.PARAMETER_TITLE, title );
		link.addParameter(ImageDisplayWindow.PARAMETER_INFO, title );
		link.setPublicWindowToOpen(ImageDisplayWindow.class);
		return link;
	}

	private String getRealPathToImage() {
		return this.realPathToImage;
	}


	/**
	 * Read width and height from the original image via slide properties or ImageInfo
	 * 
	 * @return
	 * @throws Exception
	 */
	protected void readWidthAndHeightFromOriginalImage() throws Exception {
		IWContext iwc = IWContext.getInstance();
		String realOrURLPath = getRealPathToImage();

		//Todo use slide local api/webdavlocal resource to get properties
		String widthAndHeight = null;
		Enumeration enumWidthAndHeight = this.resource.propfindMethod(IWSlideConstants.PROPERTYNAME_WIDTH_AND_HEIGHT_PROPERTY);
		if(enumWidthAndHeight!=null && enumWidthAndHeight.hasMoreElements() && !"".equals((widthAndHeight = (String) enumWidthAndHeight.nextElement())) ){
			this.widthOfOriginalImage = Integer.parseInt(widthAndHeight.substring(0,widthAndHeight.indexOf("x")));
			this.heightOfOriginalImage = Integer.parseInt(widthAndHeight.substring(widthAndHeight.indexOf("x")+1));	
		}
		else{
			//TODO move to ImageProvider
			URL url = new URL(realOrURLPath);
			ImageInfo ii = new ImageInfo();
			InputStream stream = url.openStream();
			ii.setInput(stream);
			if (ii.check()) {
				this.widthOfOriginalImage = ii.getWidth();
				this.heightOfOriginalImage = ii.getHeight();
				final int bitsPerPixel = ii.getBitsPerPixel();
				final int widthDpi = ii.getPhysicalWidthDpi();

				Hashtable props = new Hashtable();
				props.put(IWSlideConstants.PROPERTY_HEIGHT, String.valueOf(this.heightOfOriginalImage));
				props.put(IWSlideConstants.PROPERTY_WIDTH, String.valueOf(this.widthOfOriginalImage));

				props.put(IWSlideConstants.PROPERTY_WIDTH_AND_HEIGHT, String.valueOf(this.widthOfOriginalImage)+"x"+String.valueOf(this.heightOfOriginalImage));
				props.put(IWSlideConstants.PROPERTY_BITS_PER_PIXEL, String.valueOf(bitsPerPixel));
				if (widthDpi != -1) {
					props.put(IWSlideConstants.PROPERTY_DPI, String.valueOf(widthDpi));
				}

				//close before to avoid thread lock
				stream.close();
				stream = null;
				ii = null;
				this.resource.proppatchMethod(props, true);

			}
			//must close
			if(stream!=null){
				stream.close();
				stream = null;
				ii = null;
			}


		}

		// so we can have access to that in javascript...very handy
		//only set for slide stuff now...
		String path = getResourceURI();
		setMarkupAttribute("orgIMGPath",path);
		setMarkupAttribute("orgIMGParentPath", getImageProvider(iwc).getIWSlideService().getParentPath(path));


	}

	private void createAndStoreImage(IWContext iwc) throws Exception {

//		set the xml width and height
		setHeight(this.heightOfModifiedImage);
		setWidth(this.widthOfModifiedImage);

		// get image encoder
		ImageEncoder imageEncoder = getImageEncoder(iwc);
		// get mime type
		String mimeType = this.resource.getGetContentType();

		// look up the file extension of the result file the image encoder
		// returns
		// for this mime type
		String extension = imageEncoder.getResultFileExtensionForInputMimeType(mimeType);
		if (ImageEncoder.INVALID_FILE_EXTENSION.equals(extension)) {
			throw new IOException("ImageEncoder do not known this mime type:" + mimeType);
		}

		// -----------------------------------------------------------------------------
		// ------------------ image has to be processed---------------------------------
		// get the processer
		// create a image process job
		ImageProcessJob job = new ImageProcessJob();
		job.setImageLocation(this.realPathToImage);
		job.setLocationIsURL(true);
		job.setMimeType(mimeType);
		job.setName(this.getName());
		job.setID("");

		job.setNewExtension(extension);
		job.setNewWidth(this.widthOfModifiedImage);
		job.setNewHeight(this.heightOfModifiedImage);
		job.setJobKey(nameOfModifiedImage);
		ImageProcessor processor = ImageProcessor.getInstance(iwc);
		//add to the image processing engine
		processor.addImageProcessJobToQueu(job);
	}


	private ImageEncoder getImageEncoder(IWContext iwc) throws RemoteException {
		return (ImageEncoder) IBOLookup.getServiceInstance(iwc, ImageEncoder.class);
	}

	private ImageProvider getImageProvider(IWContext iwc) throws RemoteException {
		return (ImageProvider) IBOLookup.getServiceInstance(iwc, ImageProvider.class);
	}

	private String getNameOfModifiedImageWithExtension(int width, int height, String extension) {
		String name = getName();

		int pointPosition = name.lastIndexOf('.');
		int length = name.length();
		if ((pointPosition > 0) && pointPosition > (length - 5)) {
			name = name.substring(0, pointPosition);
		}
		StringBuffer nameOfImage = new StringBuffer();
		// add new extension	
		nameOfImage.append(name).append("_").append(width).append("x").append(height).append(".").append(extension);
		return nameOfImage.toString();
	}
}