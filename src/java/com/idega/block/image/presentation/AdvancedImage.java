package com.idega.block.image.presentation;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.rmi.RemoteException;
import java.sql.SQLException;
import javax.ejb.FinderException;
import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;
import com.idega.block.image.business.ImageEncoder;
import com.idega.block.image.business.ImageProcessor;
import com.idega.block.image.business.ImageProvider;
import com.idega.block.image.data.ImageEntity;
import com.idega.block.image.data.ImageProcessJob;
import com.idega.business.IBOLookup;
import com.idega.core.file.data.ICFile;
import com.idega.core.file.data.ICFileHome;
import com.idega.idegaweb.IWCacheManager;
import com.idega.idegaweb.IWMainApplication;
import com.idega.presentation.IWContext;
import com.idega.presentation.Image;
import com.idega.presentation.text.Link;
import com.idega.util.caching.Cache;
import com.sun.media.jai.codec.MemoryCacheSeekableStream;

/**
 *   
 * 
 * Title:         idegaWeb
 * Description:   AdvancedImage represents a image and extends 
 *                {@link com.idega.presentation.Image Image}.
 *                 
 *                In contrast to the Image class changes of the size by the methods
 *                setHeight and setWith are not performed by adding corresponding values
 *                and commands to the print method but by creating a new image 
 *                with the desired size. The new image with the desired size is sent to the client.
 *                Therefore the image is not resized on the client side but
 *                on the server side. 
 * 
 *                The height and the width of the image can be set by using 
 *                the setHeight and setWidth methods of the Image class.
 * 
 *                The ImageEncoder service bean is used to create the new image. 
 *                The new created image is uploaded into the database 
 *                into a branch of the original image. The type of the new image is not
 *                necessary equal to the type of the original image. The 
 *                ImageEncoder is responsible for changing the type. 
 *                (e.g. bitmap is transformed to jpeg).
 * 
 *                An instance of this class represents therefore the original image and all derived
 *                images: Depending on the values of the height and the width value the
 *                corresponding image is used by the print method. A new image is only 
 *                created if that size was never created before otherwise the desired
 *                image is fetched from the database or cache. 
 *                This means that an access to
 *                all modified images and especially to the original image is always possible.
 *                
 *                To print the original image when this image is set to a different size the 
 *                {@link com.idega.block.image.presentation.AdvancedImageWrapper AdvancedImageWrapper} 
 *                can be used. Use the constructor AdvancedImageWrapper(this) and the wrapper represents
 *                the original image, especially the original image is printed if the wrapper is printed.
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
  private static final String BORDER = "90";
  
  /** cached value not an attribute */
  private ImageEntity imageEntity;

  /** cached value not an attribute */
  private PlanarImage originalImage;
  
  /** cached value not an attribute */
  private String realPathToImage;
  
  /** id of the original image. 
   * The id is stored because during the execution of the print method
   * the id variable of the super class must be set to the id of the modified image
   * when a modified image is printed. See print method of this class.
   */
  private int originalImageId;
  
  private int modifiedImageId = -1;

  private int widthOfModifiedImage = 0;
  
  private int heightOfModifiedImage = 0;
  
  /** flag to show if the image should be enlarged or not
   */
  private boolean enlargeIfNecessary = false;
  
  /** flag to show if the image should keep it´s proportion
   */
  private boolean scaleProportional = true;


    
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
  	//TODO Eiki show a message that the image is being prepared if the id is -1
  	//or have it as a setting
  	
    // set id temporary to id of the modified image
    if (modifiedImageId > -1) {
      setImageID(modifiedImageId);
      super.print(iwc);
      // set old value
      setImageID(originalImageId);
    }
    else{
      printOriginalImage(iwc);
    }
  }
   
  public void printOriginalImage(IWContext iwc) throws Exception {
    super.print(iwc);
  }    



  public void setEnlargeProperty(boolean enlargeIfNecessary)  {
    this.enlargeIfNecessary = enlargeIfNecessary;
  }

  public void setScaleProportional(boolean scaleProportional) {
    this.scaleProportional = scaleProportional;
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
      modifiedImageId  = createAndStoreImage(iwc);
      // remove these attributes to prevent scaling by the browser client 
      removeMarkupAttribute(HEIGHT);
      removeMarkupAttribute(WIDTH);
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
    
    if (heightString == null || widthString == null) 
      // image must be not modified
      return false;
    
    int setHeight = Integer.parseInt(heightString);
    int setWidth = Integer.parseInt(widthString);
    
    if ((setHeight <= 0) && (setWidth <= 0))
      // image must not be modified
      return false;

    // ...there are new settings for the height and the width
    // calculate now the new values for the modified image....
    // first assumption: image must not be modified:
    heightOfModifiedImage = 0;
    widthOfModifiedImage = 0;
    boolean imageMustBeModified = false;  
  
    // get values of the original image
    int heightOfOriginalImage = getHeightOfOriginalImage(iwc);
    int widthOfOriginalImage = getWidthOfOriginalImage(iwc);
   
        
    /* modify height, if
    + desired height is defined
    + image should be enlarged (if it is smaller than the desired height)
    + imgage is too large for the desired heigth
    */
        
    if ((heightOfOriginalImage < setHeight && enlargeIfNecessary) ||
        (heightOfOriginalImage > setHeight))  {
      heightOfModifiedImage = setHeight;
      imageMustBeModified = true;
    }
    else
      heightOfModifiedImage = heightOfOriginalImage;
    
    /* modify width, if
    + desired width is defined
    + image should be enlarged (if it is smaller than the desired width)
    + imgage is too large for the desired width
    */
    
    if ((widthOfOriginalImage < setWidth && enlargeIfNecessary) ||
        (widthOfOriginalImage > setWidth))  {
      widthOfModifiedImage = setWidth;
      imageMustBeModified = true;
    }
    else
      widthOfModifiedImage = widthOfOriginalImage;
      
    
   // resize the image proportional if desired
    if (imageMustBeModified && scaleProportional) {
      BigInteger wTable = BigInteger.valueOf(setWidth);
      BigInteger hTable = BigInteger.valueOf(setHeight);
      BigInteger wImage = BigInteger.valueOf(widthOfOriginalImage);
      BigInteger hImage = BigInteger.valueOf(heightOfOriginalImage);
      // start calculation with big integers
      BigInteger wImagehTable = wImage.multiply(hTable);
      BigInteger hImagewTable = hImage.multiply(wTable);
      if (hImagewTable.compareTo(wImagehTable) > 0)  {
        // set height of modified image to height of the cell
        heightOfModifiedImage = setHeight;
        widthOfModifiedImage = wImagehTable.divide(hImage).intValue();
      }
      else { 
        // set width of modified image to width of the cell
        widthOfModifiedImage = setWidth;
        heightOfModifiedImage = hImagewTable.divide(wImage).intValue();
      }
      // sometimes the new values equal to the original values:
      // in this case do not modify the image 
      if (widthOfModifiedImage == widthOfOriginalImage &&
          heightOfModifiedImage == heightOfOriginalImage) {
        // do not modify the image    
        widthOfModifiedImage = 0;
        heightOfModifiedImage = 0;
        return false;
      }
    }
    // end of calculation of the values width and height of the modified image   
    return imageMustBeModified;
  }
 
 /**  Gets height of original image. 
  */
  public synchronized  int getHeightOfOriginalImage(IWContext iwc) throws Exception{
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

  /** Gets width of the original image
   */
  public synchronized int getWidthOfOriginalImage(IWContext iwc) throws Exception {
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
  
  
  public String getHeight()  {
    String height = super.getHeight();
    // height is set?
    // In this case get the height that was set by the setHeight method of the super class!
    if (height != null) 
      return height;
    // has the height been set before?
    // remember: to prevent that the image is resized on the client side
    // the height attribute was deleted after creating the modified image
    // but the value is stored in the
    // heightOfModifiedImage variable. 
    // see: scale() method of this class
    // Therefore:
    if (heightOfModifiedImage > 0)
      return Integer.toString(heightOfModifiedImage);
    return null;
  }


  public String getWidth()  {
    String width = super.getWidth();
    // width is set?
    // In this case get the width that was set by the setWidth method of the super class!
    if (width != null) 
      return width;
    // has the width been set before?
    // remember: to prevent that the image is resized on the client side
    // the width attribute was deleted after creating the modified image
    // but the value is stored in the
    // widthOfModifiedImage variable. 
    // see: scale() method of this class
    // Therefore:
    if (widthOfModifiedImage > 0)
      return Integer.toString(widthOfModifiedImage);
    return null;
  }




      

  /** Adds a link to this image to a popup window that the original version of this image shows
   */
  public void setLinkToDisplayWindow(IWContext iwc, int imageNumber)  {
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
      link.setParameter(ImageDisplayWindow.PARAMETER_IMAGE_NUMBER, String.valueOf(imageNumber));
    }
    catch (Exception ex)  { 
      // do nothing
      // default values of height and width (regarding a pop up window) will be used
    }
    String title = getResourceBundle(iwc).getLocalizedString("image","Image");
    link.setParameter(ImageDisplayWindow.PARAMETER_TITLE, title);
    // set imageID of the original image
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
    IWMainApplication iwma = iwc.getIWMainApplication(); 
  
    return (Cache) IWCacheManager.getInstance(iwma).getCachedBlobObject(com.idega.block.image.data.ImageEntity.class.getName(),imageId,iwma);
  }

 
  private int createAndStoreImage(IWContext iwc) throws Exception {
    
    // get image encoder
    ImageEncoder imageEncoder = getImageEncoder(iwc);
    
    // get mime type
    Cache cachedImage = getCachedImage(iwc, originalImageId);
    ImageEntity imageEntity = (ImageEntity) cachedImage.getEntity();
    String mimeType = imageEntity.getMimeType();
    
    // is it necessary to convert the image?  
    if (!checkAndCalculateNewWidthAndHeight(iwc)){
      // okay: the desired width and height is the same as the original image
      // check if the image would be converted to another type
      if (imageEncoder.isInputTypeEqualToResultType(mimeType)) {
        // do nothing, use the original image -----
        return -1;
      }else{
      	// convert the original image using the same size
    		heightOfModifiedImage = getHeightOfOriginalImage(iwc);
    		widthOfModifiedImage = getWidthOfOriginalImage(iwc);  
      }
    }
    
           
    // look up the file extension of the result file the image encoder returns 
    // for this mime type
    String extension = imageEncoder.getResultFileExtensionForInputMimeType(mimeType);
    if (ImageEncoder.INVALID_FILE_EXTENSION.equals(extension)){
      throw new IOException("ImageEncoder do not known this mime type:"+mimeType); 
    }
    
    String nameOfModifiedImage = getNameOfModifiedImageWithExtension(widthOfModifiedImage, heightOfModifiedImage ,extension, imageEntity);
      
    //TODO if the image exists on HARD DISK then set the Image url to that path
    //otherwise get the id from the database.
    // setURL(path);
    // check if the image already exists and return the value if found
    int imageID = getImageIDByName(nameOfModifiedImage);
    if ( imageID > -1) {
    	// nothing to do, use the already existing modified image ---
    		return imageID;
    }
    else{
	    //------------------------------------------------------------------------------------//
	    // ------------------ image has to be processed ---------------------------------------
	    // get the processer
	    // create a image process job
	    ImageProcessJob job = new ImageProcessJob();
	    job.setCachedImage(cachedImage);
	    job.setNewExtension(extension);
	    job.setNewWidth(widthOfModifiedImage);
	    job.setNewHeight(heightOfModifiedImage);
	    job.setJobKey(nameOfModifiedImage);
	        
	    ImageProcessor processor = ImageProcessor.getInstance(iwc);
	    processor.addImageProcessJobToQueu(job);
	    
	    //the scaled image is not ready yet
	    return -1;
    }
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
    return ((Integer)icFile.getPrimaryKey()).intValue();
  }
  
  private ImageEncoder getImageEncoder(IWContext iwc)  throws RemoteException{
      return (ImageEncoder) IBOLookup.getServiceInstance(iwc,ImageEncoder.class);
  }  
  
  
  private ImageProvider getImageProvider(IWContext iwc) throws RemoteException {
    return (ImageProvider) IBOLookup.getServiceInstance(iwc, ImageProvider.class);
  }
  
  



  private String getNameOfModifiedImageWithExtension(int width, int height, String extension, ImageEntity entity)  {
    String name = getName();
    
    int pointPosition = name.lastIndexOf('.');
    int length = name.length();
    // cut extension (name.a  name.ab name.abc but not name.abcd)
    if ( (pointPosition > 0) && pointPosition > (length - 5))  
      name = name.substring(0,pointPosition);        
    StringBuffer nameOfImage = new StringBuffer();
    // add new extension
    nameOfImage.append(entity.getPrimaryKey());
    nameOfImage.append(width).append("_").append(height)
      .append("_").append(name)
      .append(".").append(extension);
    
    return nameOfImage.toString();
  }
}