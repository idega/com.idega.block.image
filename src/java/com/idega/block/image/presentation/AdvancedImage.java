package com.idega.block.image.presentation;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URL;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Hashtable;
import javax.ejb.FinderException;
import org.apache.webdav.lib.WebdavResource;
import com.idega.block.image.business.ImageEncoder;
import com.idega.block.image.business.ImageProcessor;
import com.idega.block.image.business.ImageProvider;
import com.idega.block.image.data.ImageEntity;
import com.idega.block.image.data.ImageProcessJob;
import com.idega.business.IBOLookup;
import com.idega.core.file.data.ICFile;
import com.idega.core.file.data.ICFileHome;
import com.idega.graphics.ImageInfo;
import com.idega.idegaweb.IWCacheManager;
import com.idega.idegaweb.IWMainApplication;
import com.idega.presentation.IWContext;
import com.idega.presentation.Image;
import com.idega.presentation.text.Link;
import com.idega.slide.business.IWSlideService;
import com.idega.slide.util.IWSlideConstants;
import com.idega.util.caching.Cache;

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
	private ImageEntity imageEntity;
	/** cached value not an attribute */
	private String realPathToImage;
	/**
	 * id of the original image. The id is stored because during the execution
	 * of the print method the id variable of the super class must be set to the
	 * id of the modified image when a modified image is printed. See print
	 * method of this class.
	 */
	private int originalImageId = -1;
	private int modifiedImageId = -1;
	private int widthOfModifiedImage = 0;
	private int heightOfModifiedImage = 0;
	private int widthOfOriginalImage = 0;
	private int heightOfOriginalImage = 0;
	/**
	 * flag to show if the image should be enlarged or not
	 */
	private boolean enlargeIfNecessary = false;
	/**
	 * flag to show if the image should keep it´s proportion
	 */
	private boolean scaleProportional = true;
	private WebdavResource resource = null;

	public AdvancedImage(WebdavResource webdavResource) {
		super(webdavResource.getPath());
		this.resource = webdavResource;
		this.realPathToImage = this.resource.getHttpURL().toString();
	}
	
	public AdvancedImage(int imageId) throws SQLException {
		super(imageId);
		this.originalImageId = imageId;
	}

	public AdvancedImage(int imageId, String name) throws SQLException {
		super(imageId, name);
		this.originalImageId = imageId;
	}

	public void main(IWContext iwc) {
		super.main(iwc);
		if (this.resource != null) {
			try {
				String name = this.resource.getName();
				String extension = name.substring(name.lastIndexOf(".") + 1);
				//todo don't get original width and height unless absolutely necessery e.g. not if both height and width are set and scale proportionally is false
				checkAndCalculateNewWidthAndHeight(iwc);
				String newName = getNameOfModifiedImageWithExtension(this.widthOfModifiedImage, this.heightOfModifiedImage,extension, null);
				
				IWSlideService ss = (IWSlideService) IBOLookup.getServiceInstance(iwc, IWSlideService.class);
				
				
				String temp = ss.getParentPath(this.resource) + "/resized/" + newName;
				if (ss.getExistence(temp)) {
					//why was this set before??- eiki
					//resource = ss.getWebdavResourceAuthenticatedAsRoot(temp);
					setURL(temp);
				}
				else {
					scaleImage(iwc);
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		else {
			scaleImage(iwc);
		}
	}

	public void print(IWContext iwc) throws Exception {
		// TODO Eiki show a message that the image is being prepared if the id
		// is -1
		// or have it as a setting
		// set id temporary to id of the modified image
		if (this.modifiedImageId > -1) {
			setImageID(this.modifiedImageId);
			super.print(iwc);
			// set old value
			setImageID(this.originalImageId);
		}
		else {
			printOriginalImage(iwc);
		}
	}

	public void printOriginalImage(IWContext iwc) throws Exception {
		super.print(iwc);
	}

	public void setEnlargeProperty(boolean enlargeIfNecessary) {
		this.enlargeIfNecessary = enlargeIfNecessary;
	}

	public void setScaleProportional(boolean scaleProportional) {
		this.scaleProportional = scaleProportional;
	}

	/*
	 * public void setImageToOpenInPopUp(IWContext iwc) { try { String width =
	 * Integer.toString(getWidthOfOriginalImage(iwc)); String height =
	 * Integer.toString(getHeightOfOriginalImage(iwc));
	 * this.setOnClick("img_wnd=window.open('"+getMediaURL()+"','','width="+width+",height="+height+",left='+((screen.width/2)-50)+',top='+((screen.height/2)-50)+',resizable=yes,scrollbars=no');
	 * doopen('"+getMediaURL()+"'); return true;"); } catch (Exception ex) {
	 * System.err.println(ex.getMessage()); } }
	 */
	private void scaleImage(IWContext iwc) {
		try {
			this.modifiedImageId = createAndStoreImage(iwc);
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
			// set modified image id back
			ex.printStackTrace();
			this.modifiedImageId = -1;
			System.err.println("Image could not be modified. Message was: " + ex.getMessage());
		}
	}

	private boolean checkAndCalculateNewWidthAndHeight(IWContext iwc) throws Exception {
		String heightString = getHeight();
		String widthString = getWidth();
		if (heightString == null || widthString == null){
			// image must be not modified
			return false;
		}
		
		int setHeight = Integer.parseInt(heightString);
		int setWidth = Integer.parseInt(widthString);
		if ((setHeight <= 0) && (setWidth <= 0)){
			// image must not be modified
			return false;
		}
		
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
			BigInteger wTable = BigInteger.valueOf(setWidth);
			BigInteger hTable = BigInteger.valueOf(setHeight);
			BigInteger wImage = BigInteger.valueOf(this.widthOfOriginalImage);
			BigInteger hImage = BigInteger.valueOf(this.heightOfOriginalImage);
			// start calculation with big integers
			BigInteger wImagehTable = wImage.multiply(hTable);
			BigInteger hImagewTable = hImage.multiply(wTable);
			if (hImagewTable.compareTo(wImagehTable) > 0) {
				// set height of modified image to height of the cell
				this.heightOfModifiedImage = setHeight;
				this.widthOfModifiedImage = wImagehTable.divide(hImage).intValue();
			}
			else {
				// set width of modified image to width of the cell
				this.widthOfModifiedImage = setWidth;
				this.heightOfModifiedImage = hImagewTable.divide(wImage).intValue();
			}
			// sometimes the new values equal to the original values:
			// in this case do not modify the image
			if (this.widthOfModifiedImage == this.widthOfOriginalImage && this.heightOfModifiedImage == this.heightOfOriginalImage) {
				// do not modify the image
				this.widthOfModifiedImage = 0;
				this.heightOfModifiedImage = 0;
				return false;
			}
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
			// is not in Slide
			if (this.resource == null) {
				String tempHeight = getImageEntity(IWContext.getInstance()).getHeight();
				if (tempHeight != null) {
					this.heightOfOriginalImage = Integer.parseInt(tempHeight);
					return this.heightOfOriginalImage;
				}
			}
			// same for both db and slide
			if (this.heightOfOriginalImage <= 0) {
				// this actually sets both the width and height
				readWidthAndHeightFromOriginalImage();
				if (this.resource == null) {
					setWidthAndHeightInImageEntity(this.widthOfOriginalImage, this.heightOfOriginalImage);
				}
			}
		}
		return this.heightOfOriginalImage;
	}

	/**
	 * Gets width of the original image
	 */
	public int getWidthOfOriginalImage() throws Exception {
		if (this.widthOfOriginalImage <= 0) {
			// is not in Slide
			if (this.resource == null) {
				String tempWidth = getImageEntity(IWContext.getInstance()).getWidth();
				if (tempWidth != null) {
					this.widthOfOriginalImage = Integer.parseInt(tempWidth);
				}
			}
			// same for both db and slide
			if (this.widthOfOriginalImage <= 0) {
				// this actually sets both the width and height
				readWidthAndHeightFromOriginalImage();
				if (this.resource == null) {
					setWidthAndHeightInImageEntity(this.widthOfOriginalImage, this.heightOfOriginalImage);
				}
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
		if (this.originalImageId != -1) {
			// set imageID of the original image
			popUp.addParameter(ImageDisplayWindow.PARAMETER_IMAGE_ID, this.originalImageId);
		}
		else if (this.resource != null) {
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
		if (this.resource != null) {
			return this.resource.getPath();
		}
		else {
			return null;
		}
	}
	
	public String getName(){
		if(this.resource!=null){
			String path = getResourceURI();
			return path.substring(path.lastIndexOf("/")+1);
		}

		return super.getName();
	}

	/**
	 * 
	 * @return A link with almost all neccessary parameters set for the
	 *         ImageDisplayWindow
	 */
	public Link getPopUpReadyLink() {
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

	/**
	 * Gets the imageEntity and changes and stores it
	 * 
	 * @param width
	 * @param height
	 */
	private void setWidthAndHeightInImageEntity(int width, int height) throws Exception {
		// only if we are using the database storage
		if (this.resource == null) {
			IWContext iwc = IWContext.getInstance();
			ImageProvider imageProvider = getImageProvider(iwc);
			ImageEntity entity = getImageEntity(iwc);
			imageProvider.setHeightAndWidthOfOriginalImageToEntity(width, height, entity);
		}
	}

	private ImageEntity getImageEntity(IWContext iwc) {
		if (this.imageEntity == null) {
			setRealPathToImageAndImageEntity(iwc);
		}
		return this.imageEntity;
	}

	private String getRealPathToImage(IWContext iwc) {
		if (this.realPathToImage == null) {
			setRealPathToImageAndImageEntity(iwc);
		}
		return this.realPathToImage;
	}

	private void setRealPathToImageAndImageEntity(IWContext iwc) {
		Cache cachedImage = getCachedImage(iwc, this.originalImageId);
		this.realPathToImage = cachedImage.getRealPathToFile();
		this.imageEntity = (ImageEntity) cachedImage.getEntity();
	}

	/**
	 * Read width and height from the original image via slide properties or ImageInfo
	 * 
	 * @return
	 * @throws Exception
	 */
	protected void readWidthAndHeightFromOriginalImage() throws Exception {
		String realOrURLPath = getRealPathToImage(IWContext.getInstance());
		
		// if in database
		if (this.resource == null) {
			ImageInfo ii = new ImageInfo();
			InputStream stream = new FileInputStream(realOrURLPath);
			ii.setInput(stream);
			if (ii.check()) {
				this.widthOfOriginalImage = ii.getWidth();
				this.heightOfOriginalImage = ii.getHeight();
			}
			stream.close();
			stream = null;
			
		}
		else {
			// in Slide
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
			setMarkupAttribute("orgIMGPath", this.resource.getPath());
		}
		
	}

	private Cache getCachedImage(IWContext iwc, int imageId) {
		// this method is similar to the private getImage() method of the super
		// class Image
		IWMainApplication iwma = iwc.getIWMainApplication();
		IWCacheManager cacheManager = IWCacheManager.getInstance(iwma);
		if (imageId > 0) {
			return cacheManager.getCachedBlobObject(com.idega.block.image.data.ImageEntity.class.getName(), imageId,
					iwma);
		}
		else if (this.resource != null) {
			// TODO store this cache object
			return new Cache(getResourceURI(), getResourceURI());
		}
		return null;
	}

	private int createAndStoreImage(IWContext iwc) throws Exception {
		// get image encoder
		ImageEncoder imageEncoder = getImageEncoder(iwc);
		// get mime type
		String mimeType = null;
		Cache cachedImage = null;
		if (this.resource == null) {
			cachedImage = getCachedImage(iwc, this.originalImageId);
			ImageEntity imageEntity = (ImageEntity) cachedImage.getEntity();
			mimeType = imageEntity.getMimeType();
		}
		else {
			cachedImage = new Cache(getResourceURI(), getResourceURI());
			mimeType = this.resource.getGetContentType();
		}
		// is it necessary to convert the image?
		if (!checkAndCalculateNewWidthAndHeight(iwc)) {
			// okay: the desired width and height is the same as the original
			// image
			// check if the image would be converted to another type
			if (imageEncoder.isInputTypeEqualToResultType(mimeType)) {
				// do nothing, use the original image -----
				return -1;
			}
			else if (this.resource != null) {
				// convert the original image using the same size
				// e.g. bitmap to jpeg conversion because of load size
				this.heightOfModifiedImage = getHeightOfOriginalImage();
				this.widthOfModifiedImage = getWidthOfOriginalImage();
			}
		}
		// set the xml width and height
		setHeight(this.heightOfModifiedImage);
		setWidth(this.widthOfModifiedImage);
		// look up the file extension of the result file the image encoder
		// returns
		// for this mime type
		String extension = imageEncoder.getResultFileExtensionForInputMimeType(mimeType);
		if (ImageEncoder.INVALID_FILE_EXTENSION.equals(extension)) {
			throw new IOException("ImageEncoder do not known this mime type:" + mimeType);
		}
		String nameOfModifiedImage = getNameOfModifiedImageWithExtension(this.widthOfModifiedImage, this.heightOfModifiedImage,
				extension, this.imageEntity);
		// TODO if the image exists on HARD DISK then set the Image url to that
		// path
		// otherwise get the id from the database.
		// setURL(path);
		// check if the image already exists and return the value if found
		int imageID = getImageIDByName(nameOfModifiedImage);
		if (imageID > -1) {
			// nothing to do, use the already existing modified image ---
			return imageID;
		}
		else {
			// -----------------------------------------------------------------------------
			// ------------------ image has to be processed---------------------------------
			// get the processer
			// create a image process job
			ImageProcessJob job = new ImageProcessJob();
			if (this.resource != null) {
				job.setImageLocation(this.resource.getHttpURL().toString());
				job.setLocationIsURL(true);
				job.setMimeType(this.resource.getGetContentType());
				job.setName(this.resource.getName());
				job.setID("");
			}
			else {
				job.setImageEntity((ImageEntity) cachedImage.getEntity());
				job.setImageLocation(cachedImage.getRealPathToFile());
				job.setLocationIsURL(false);
				job.setMimeType(job.getImageEntity().getMimeType());
				job.setName(job.getImageEntity().getName());
				job.setID(job.getImageEntity().getPrimaryKey().toString());
			}
			// job.setCachedImage(cachedImage);
			job.setNewExtension(extension);
			job.setNewWidth(this.widthOfModifiedImage);
			job.setNewHeight(this.heightOfModifiedImage);
			job.setJobKey(nameOfModifiedImage);
			ImageProcessor processor = ImageProcessor.getInstance(iwc);
			processor.addImageProcessJobToQueu(job);
			// the scaled image is not ready yet
			return -1;
		}
	}

	private int getImageIDByName(String name) {
		ICFileHome icFileHome = (ICFileHome) com.idega.data.IDOLookup.getHomeLegacy(ICFile.class);
		ICFile icFile;
		try {
			icFile = icFileHome.findByFileName(name);
		}
		catch (FinderException e) {
			return -1;
		}
		return ((Integer) icFile.getPrimaryKey()).intValue();
	}

	private ImageEncoder getImageEncoder(IWContext iwc) throws RemoteException {
		return (ImageEncoder) IBOLookup.getServiceInstance(iwc, ImageEncoder.class);
	}

	private ImageProvider getImageProvider(IWContext iwc) throws RemoteException {
		return (ImageProvider) IBOLookup.getServiceInstance(iwc, ImageProvider.class);
	}

	private String getNameOfModifiedImageWithExtension(int width, int height, String extension, ImageEntity entity) {
		String name = getName();
		if (this.resource != null) {
			String imageName = this.resource.getName();
			int slashPos = imageName.lastIndexOf("/");
			if (slashPos > 0) {
				imageName = imageName.substring(slashPos + 1);
			}
			int pointPosition = imageName.lastIndexOf('.');
			int length = imageName.length();
			if ((pointPosition > 0) && pointPosition > (length - 5)) {
				imageName = imageName.substring(0, pointPosition);
			}
			name = imageName;
		}
		StringBuffer nameOfImage = new StringBuffer();
		// add new extension
		if (entity != null) {
			nameOfImage.append(entity.getPrimaryKey());
		}
		nameOfImage.append(name).append("_").append(width).append("x").append(height).append(".").append(extension);
		return nameOfImage.toString();
	}
}