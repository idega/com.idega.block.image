package com.idega.block.image.presentation;

import java.awt.image.renderable.ParameterBlock;
import java.awt.image.renderable.RenderableImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.Vector;

import javax.ejb.CreateException;
import javax.ejb.FinderException;
import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;
import javax.transaction.UserTransaction;

import com.idega.block.image.business.ImageEncoder;
import com.idega.block.image.business.ImageProvider;
import com.idega.block.image.data.ImageEntity;
import com.idega.block.image.data.ImageEntityHome;
import com.idega.block.media.business.MediaBusiness;
import com.idega.block.media.servlet.MediaServlet;
import com.idega.business.IBOLookup;
import com.idega.core.data.ICFile;
import com.idega.core.data.ICFileHome;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.idegaweb.IWCacheManager;
import com.idega.idegaweb.IWMainApplication;
import com.idega.presentation.IWContext;
import com.idega.presentation.Image;
import com.idega.presentation.PresentationObject;
import com.idega.presentation.text.Link;
import com.idega.util.FileUtil;
import com.idega.util.caching.Cache;
import com.sun.media.jai.codec.ImageCodec;

import com.sun.media.jai.codec.JPEGEncodeParam;
import com.sun.media.jai.codec.MemoryCacheSeekableStream;

/**
 *   
 * 
 * Title:         idegaWeb
 * Description:   AdvancedImage represents a image and extend the image class.
 *                In contrast to the Image class changes of the size by the methods
 *                setHeight and setWith are not performed by adding corresponding values
 *                and commands to the print method but by creating a new image 
 *                with the desired size. 
 *                The ImageEncoder service bean is used to create a new image. 
 *                The new created image is uploaded into the database 
 *                into a branch of the original image. The type of the new image is not
 *                necessary equal to the type of the original image, that depends how the
 *                ImageEncoder works (e.g. bitmap is transformed to jpeg).
 *                An instance of this class represents the original image and all derived
 *                images: Depending on the values of the height and the width value the
 *                corresponding image is used by the print method. A new image is only 
 *                created if that size was never created before otherwise the desired
 *                image is fetched from the database or cache.
 *                To print the original image the AdvancedImageWrapper can be used:
 *                Even if the height and the width is set the original image is
 *                printed.
 *                
 *                  
 * Copyright:     Copyright (c) 2003
 * Company:       idega software
 * @author <a href="mailto:thomas@idega.is">Thomas Hilbig</a>
 * @version 1.0
 */

public class AdvancedImage extends Image {
  

  /** Folder where the modified images are stored */  
  public static final String MODIFIED_IMAGES_FOLDER = "modified_images";
  
  /** border around the image in the popup window */
  private static final String BORDER = "70";
  
  /** cached value not an attribute */
  private ImageEntity imageEntity;

  /** cached value not an attribute */
  private PlanarImage originalImage;
  
  /** cached value not an attribute */
  private String realPathToImage;
  
  /** id of the original image. The image id of this instance 
   * changed to -1 if the image is scaled. -1 means that this instance  
   * points to an URL (a stored image in a folder). 
   * See print() method of the super class.
   */
  private int originalImageId;
  
  private int modifiedImageId = -1;
   

  /** desired width of the image.
   *  This is neither the width of the original image 
   *  nor necessary the result of the modification.
   *  This value is used to calculate the width of the modified image.
   *  If enlargeNecessary is set to true then 
   *  the width (of the result) equals the desired width. 
   *  If enlargeNecessary is set to false then
   *  the width (of the result) is the minimum of the 
   *  width of the original image and the desired width.
   */
  private int widthOfModifiedImage = -1;
  
  /** desired height of the image.
   *  This is neither the height of the original image 
   *  nor necessary the result of the modification.
   *  This value is used to calculate the height of the modified image.
   *  If enlargeNecessary is set to true then 
   *  the height (of the result image) equals the desired height. 
   *  If enlargeNecessary is set to false then
   *  the width (of the result) is the minimum of the 
   *  height of the original image and the desired height.
   */
  private int heightOfModifiedImage = -1;
  
  /** flag to show if the image should be enlarged or not
   */
  private boolean enlargeIfNecessary = false;


    
  public AdvancedImage(int imageId) throws SQLException{
    super(imageId);
    originalImageId = imageId; 
  }
  
  public AdvancedImage(int imageId, String name) throws SQLException{
    super(imageId, name);
    originalImageId = imageId;
  }  
  

  public void main(IWContext iwc) {
    super.main(iwc);
    scaleImage(iwc);
  }
  
  public void print(IWContext iwc) throws Exception  {
    if (modifiedImageId > -1) {
      setImageID(modifiedImageId);
      super.print(iwc);
      setImageID(originalImageId);
    }
    else
      printOriginalImage(iwc);
  }
   
  public void printOriginalImage(IWContext iwc) throws Exception {
    super.print(iwc);
  }    



  public void setEnlargeProperty(boolean enlargeIfNecessary)  {
    this.enlargeIfNecessary = enlargeIfNecessary;
  }


/*  public void setImageToOpenInPopUp(IWContext iwc) {
    try {
      String width = Integer.toString(getWidthOfOriginalImage(iwc));
      String height = Integer.toString(getHeightOfOriginalImage(iwc));
      this.setOnClick("img_wnd=window.open('"+getMediaURL()+"','','width="+width+",height="+height+",left='+((screen.width/2)-50)+',top='+((screen.height/2)-50)+',resizable=yes,scrollbars=no'); doopen('"+getMediaURL()+"'); return true;");
    }
    catch (Exception ex)  {
      System.err.println(ex.getMessage());
    }
  }
*/

 

    
  private void scaleImage(IWContext iwc) {
    try {
    	if (checkAndCalculateNewWidthAndHeight(iwc)) {
        
      	// Does the desired image already exist?
      	// If so then there is nothing to do.
        modifiedImageId  = createAndStoreImage(widthOfModifiedImage,heightOfModifiedImage,iwc);

   		}
   		// remove these attributes to prevent scaling by the browser client 
    	removeAttribute(HEIGHT);
    	removeAttribute(WIDTH);
    }
    catch (Exception ex)  {
      // set modified image id back
      modifiedImageId = -1;
      System.err.println("Image could not be modified. Message was: "+ ex.getMessage());
    }      
  }
 
 
 
  private boolean checkAndCalculateNewWidthAndHeight(IWContext iwc) throws Exception {
       
    String heightString = getHeight();
    String widthString = getWidth();

    // if the image was scaled before return true!  
    // (that is: use the modified image!)  
    boolean returnValue = (heightOfModifiedImage > -1 && heightOfModifiedImage > -1);

    if (heightString == null || widthString == null) 
      return returnValue;
    
    int setHeight = Integer.parseInt(heightString);
    int setWidth = Integer.parseInt(widthString);
    
    if ((setHeight <= 0) && (setWidth <= 0))
      return returnValue;

    heightOfModifiedImage = 0;
    widthOfModifiedImage = 0;

    boolean imageMustBeModified = false;
    
    heightOfModifiedImage = getHeightOfOriginalImage(iwc);
    widthOfModifiedImage = getWidthOfOriginalImage(iwc);
    
    /* modify height, if
    + desired height is defined
    + image should be enlarged (if it is smaller than the desired height)
    + imgage is too large for the desired heigth
    */
        
    if ((heightOfModifiedImage < setHeight && enlargeIfNecessary) ||
        (heightOfModifiedImage > setHeight))  {
      heightOfModifiedImage = setHeight;
      imageMustBeModified = true;
    }
    
    /* modify width, if
    + desired width is defined
    + image should be enlarged (if it is smaller than the desired width)
    + imgage is too large for the desired width
    */
    
    if ((widthOfModifiedImage < setWidth && enlargeIfNecessary) ||
        (widthOfModifiedImage > setWidth))  {
      widthOfModifiedImage = setWidth;
      imageMustBeModified = true;
    }
    return imageMustBeModified;
  }
 

  public int getHeightOfOriginalImage(IWContext iwc) throws Exception{
    String heightOfOriginalImage = getImageEntity(iwc).getHeight();
    if (heightOfOriginalImage == null)  {
      PlanarImage image = getOriginalImage(iwc);
      int height = image.getHeight();
      int width = image.getWidth();
      setHeightAndWidthOfOriginalImageAtEntity(width, height, iwc);
      return height;
    }
    return Integer.parseInt(heightOfOriginalImage);
  }


  public int getWidthOfOriginalImage(IWContext iwc) throws Exception {
   String widthOfOriginalImage = getImageEntity(iwc).getWidth();
    if (widthOfOriginalImage == null)  {
      PlanarImage image = getOriginalImage(iwc);
      int height = image.getHeight();
      int width = image.getWidth();
      setHeightAndWidthOfOriginalImageAtEntity(width, height, iwc);
      return width;
    }
    return Integer.parseInt(widthOfOriginalImage);
  }


  public void addLinkToDisplayWindow(IWContext iwc)  {
    Link link = new Link();
    String imageID = Integer.toString(originalImageId);
    String widthString;
    String heightString;
    try {
      widthString = Integer.toString(getWidthOfOriginalImage(iwc));
      heightString = Integer.toString(getHeightOfOriginalImage(iwc));
      // if an exception occurs the width and height parameters are not set.
      // In this case default values are used.
      link.setParameter(ImageDisplayWindow.PARAMETER_BORDER, BORDER);
      link.setParameter(ImageDisplayWindow.PARAMETER_WIDTH, widthString);
      link.setParameter(ImageDisplayWindow.PARAMETER_HEIGHT, heightString);
    }
    catch (Exception ex)  { 
      // do nothing
      // default values of height and width will be used
    }
    String title = getResourceBundle(iwc).getLocalizedString("image","Image");
    link.setParameter(ImageDisplayWindow.PARAMETER_TITLE, title);
    link.setParameter(ImageDisplayWindow.PARAMETER_IMAGE_ID,imageID);
    link.setParameter(ImageDisplayWindow.PARAMETER_INFO, getName());
    link.setWindowToOpen(ImageDisplayWindow.class);
    setImageZoomLink(link);
    setImageLinkZoomView();
  }







	/**
	 * Method setHeightAndWidthOfOriginalImageAtEntity.
	 * @param width
	 * @param height
	 */
	private void setHeightAndWidthOfOriginalImageAtEntity(int width, int height, IWContext iwc) 
    throws Exception {
     ImageProvider imageProvider = getImageProvider(iwc);
     ImageEntity entity = getImageEntity(iwc);
     imageProvider.setHeightAndWidthOfOriginalImageToEntity(width, height, entity);     
	}



  private ImageEntity getImageEntity(IWContext iwc) {
    if (imageEntity == null)
      setRealPathToImageAndImageEntity(iwc);
    return imageEntity;
  }  


  private String getRealPathToImage(IWContext iwc) {
    if (realPathToImage == null)
      setRealPathToImageAndImageEntity(iwc);
    return realPathToImage;
  }

  
  private void setRealPathToImageAndImageEntity(IWContext iwc)  {  
    Cache cachedImage = getCachedImage(iwc, originalImageId);
    realPathToImage = cachedImage.getRealPathToFile();       
    imageEntity = (ImageEntity) cachedImage.getEntity();
  }
  
      
 
  private PlanarImage getOriginalImage(IWContext iwc) throws Exception {
    
    if (originalImage != null) 
      return originalImage;
    MemoryCacheSeekableStream stream = new MemoryCacheSeekableStream(
      new BufferedInputStream(new FileInputStream(getRealPathToImage(iwc))));
    originalImage = JAI.create("stream", stream);
    stream.close();

    return originalImage;
  }



	private Cache getCachedImage(IWContext iwc, int imageId) {
		// this method is similar to the private getImage() method of the super class Image
		IWMainApplication iwma = iwc.getApplication(); 
	
	  return (Cache) IWCacheManager.getInstance(iwma).getCachedBlobObject(com.idega.block.image.data.ImageEntity.class.getName(),imageId,iwma);
	}

 
  private int createAndStoreImage(int width, int height, IWContext iwc) throws Exception {
    
    
      // get image encoder
      ImageEncoder imageEncoder = getImageEncoder(iwc);
      
       // get mimetype
      ImageEntity imageEntity = getImageEntity(iwc);
      String mimeType = imageEntity.getMimeType();
            
      // look up the file extension of the result file the image encoder returns 
      // for this mime type
      String extension = imageEncoder.getResultFileExtensionForInputMimeType(mimeType);
      if (ImageEncoder.INVALID_FILE_EXTENSION.equals(extension))
        throw new IOException("ImageEncoder do not known this mime type:"+mimeType); 
    
      String nameOfModifiedImage = getNameOfModifiedImageWithExtension(width, height ,extension);
      
       // Does the image already exist? Then there is nothing to do.
      int imageID = getImageIDByName(nameOfModifiedImage);
      if ( imageID > -1)
      	return imageID;
      
      // get real path and virtual path to modified image
      // (this does not mean that the modified image already exists)
      IWMainApplication mainApp = iwc.getApplication();
            
      String virtualPathOfModifiedImage = 
        getVirtualPathOfModifiedImage(widthOfModifiedImage, heightOfModifiedImage, extension, mainApp);
      
      String pathOfModifiedImage = 
        mainApp.getApplicationRealPath() + virtualPathOfModifiedImage;
      
      // now create the new image...  
      
      // get input
        // get fileFileValue() causes End-Of-File Exception when JAI tries to read the file fully 
        // InputStream input = imageEntity.getFileValue();
      FileInputStream input = new FileInputStream(getRealPathToImage(iwc));
     
      // get output
      OutputStream output = new FileOutputStream(pathOfModifiedImage);
      // encode     
    try {  
      imageEncoder.encode(mimeType, input ,output,width, height);
    }
    catch (Exception ex)  {
      // delete the created file (you can not use the result)
      output.close();
      input.close();
      (new File(pathOfModifiedImage)).delete();
      throw ex;
    }
    output.close();
    input.close();
    FileInputStream inputStream = new FileInputStream(pathOfModifiedImage);  
    String name = getNameOfModifiedImageWithExtension(widthOfModifiedImage, heightOfModifiedImage, extension);
    
    ImageEntity motherImage = getImageEntity(iwc);
    ImageProvider imageProvider = getImageProvider(iwc);
    int modifiedImageId = imageProvider.uploadImage(inputStream, mimeType, name, width, height,motherImage);
    inputStream.close();
    return modifiedImageId;
  }
 
 
	private int getImageIDByName(String name) {
  	ICFileHome icFileHome = (ICFileHome) com.idega.data.IDOLookup.getHomeLegacy(ICFile.class);
    ICFile icFile;
  	try {
			icFile = (ICFile) icFileHome.findByFileName(name);
		}
		catch (FinderException e) {
			return -1;
		}
    return icFile.getID();
	}
  
  private ImageEncoder getImageEncoder(IWContext iwc)  throws RemoteException{
      return (ImageEncoder) IBOLookup.getServiceInstance(iwc,ImageEncoder.class);
  }  
  
  
  private ImageProvider getImageProvider(IWContext iwc) throws RemoteException {
    return (ImageProvider) IBOLookup.getServiceInstance(iwc, ImageProvider.class);
  }
  
 

  
  
  private String getVirtualPathOfModifiedImage(int width, int height, String extension, IWMainApplication mainApp) throws IOException {
    
    String separator = FileUtil.getFileSeparator();
    
    StringBuffer nameOfImage = new StringBuffer();
           
    nameOfImage.append(mainApp.getIWCacheManager().IW_ROOT_CACHE_DIRECTORY);
    nameOfImage.append(separator);
    nameOfImage.append(MODIFIED_IMAGES_FOLDER);
    
    // check if this folder exist create it if necessary
    FileUtil.createFolder(nameOfImage.toString());
    
    nameOfImage.append(separator);  
        
    nameOfImage.append(getNameOfModifiedImageWithExtension(width, height, extension));
    
    return nameOfImage.toString();
  }

  private String getNameOfModifiedImageWithExtension(int width, int height, String extension)  {
    String name = getName();
    int pointPosition = name.lastIndexOf('.');
    int length = name.length();
    // cut extension (name.a  name.ab name.abc but not name.abcd)
    if ( (pointPosition > 0) && pointPosition > (length - 5))  
      name = name.substring(0,pointPosition);        
    StringBuffer nameOfImage = new StringBuffer();
    // add new extension
    nameOfImage.append(width).append("_").append(height)
      .append("_").append(name)
      .append(".").append(extension);
    
    return nameOfImage.toString();
  }
}