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
import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;

import com.idega.block.image.business.ImageEncoder;
import com.idega.block.image.data.ImageEntity;
import com.idega.block.image.data.ImageEntityHome;
import com.idega.block.media.business.MediaBusiness;
import com.idega.block.media.servlet.MediaServlet;
import com.idega.business.IBOLookup;
import com.idega.core.data.ICFile;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.idegaweb.IWCacheManager;
import com.idega.idegaweb.IWMainApplication;
import com.idega.presentation.IWContext;
import com.idega.presentation.Image;
import com.idega.presentation.PresentationObject;
import com.idega.util.FileUtil;
import com.idega.util.caching.Cache;
import com.sun.media.jai.codec.ImageCodec;

import com.sun.media.jai.codec.JPEGEncodeParam;
import com.sun.media.jai.codec.MemoryCacheSeekableStream;

/**
 * @author Thomas
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class AdvancedImage extends Image {
  

  /** Folder where the modified images are stored */  
  public static final String MODIFIED_IMAGES_FOLDER = "modified_images";
  
  /** cached Value not an attribute */
  private ImageEntity imageEntity;

  /** cached Value not an attribute */
  private PlanarImage originalImage;
  
  /** cached Value not an attribute */
  private String realPathToImage;
  
  /** id of the original image. The image id of this instance 
   * changed to -1 if the image is scaled. -1 means that this instance  
   * points to an URL (a stored image in a folder). 
   * See print() method of the super class.
   */
  private int originalImageId;

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
  private int widthOfModifiedImage;
  
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
  private int heightOfModifiedImage;
  
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


  public void setEnlargeProperty(boolean enlargeIfNecessary)  {
    this.enlargeIfNecessary = enlargeIfNecessary;
  }

 

    
  private void scaleImage(IWContext iwc) {
    
    if (checkAndCalculateNewWidthAndHeight(iwc)) {
        
      // Does the desired image already exist?
      // If so then there is nothing to do.
    
      String pathOfModifiedImage; // = getPathOfModifiedImage(heightOfModifiedImage,widthOfModifiedImage,iwc);
    
      
      //if (! new File(pathOfModifiedImage).canRead()) {
        // create and store the new image*/
        try {
          pathOfModifiedImage = createAndStoreImage(widthOfModifiedImage,heightOfModifiedImage,iwc);
          setURL(pathOfModifiedImage);
          setImageID(-1);
          
        }
        catch (Exception ex)  {
          System.out.println("weser");
        }    
      

    }
    removeAttribute(HEIGHT);
    removeAttribute(WIDTH);
  }
 
 
 
  private boolean checkAndCalculateNewWidthAndHeight(IWContext iwc) {
    
    heightOfModifiedImage = 0;
    widthOfModifiedImage = 0;
    
    String heightString = getHeight();
    String widthString = getWidth();

    if (heightString == null || widthString == null) 
      return false;
    
    int setHeight = Integer.parseInt(heightString);
    int setWidth = Integer.parseInt(widthString);
    
    if ((setHeight <= 0) && (setWidth <= 0))
      return false;

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
 

  private int getHeightOfOriginalImage(IWContext iwc)  {
    int height = 0;
    try {
      height = getOriginalImage(iwc).getHeight();
    } catch (Exception ex)  {}
    return height;
  }
   
  private int getWidthOfOriginalImage(IWContext iwc)  {
    int width = 0;
    try {
      width = getOriginalImage(iwc).getWidth();
    } catch (Exception ex)  {}
    return width;
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
      new BufferedInputStream(imageEntity.getFileValue()));
    originalImage = JAI.create("stream", stream);
    stream.close();

    return originalImage;
  }



	private Cache getCachedImage(IWContext iwc, int imageId) {
		// this method is very similar to the private getImage() method of the super class Image
		IWMainApplication iwma = iwc.getApplication(); 
		String mmProp = iwma.getSettings().getProperty(MediaServlet.USES_OLD_TABLES);
		
		boolean usesOldImageTables = (mmProp!=null);
		
		Cache cachedImage;
		
		if( usesOldImageTables ){
		  cachedImage = (Cache) IWCacheManager.getInstance(iwma).getCachedBlobObject(com.idega.jmodule.image.data.ImageEntity.class.getName(),imageId ,iwma);
		}
		else{
		  cachedImage = (Cache) IWCacheManager.getInstance(iwma).getCachedBlobObject(com.idega.block.image.data.ImageEntity.class.getName(),imageId,iwma);
		}
		return cachedImage;
	}

 
  private String createAndStoreImage(int width, int height, IWContext iwc) throws Exception {
    
    
      // get image encoder
      ImageEncoder imageEncoder = getImageEncoder(iwc);
      
       // get mimetype
      ImageEntity imageEntity = getImageEntity(iwc);
      String mimeType = imageEntity.getMimeType();
            
      // get path of the (sometimes not yet created) modified image
      String extension = imageEncoder.getResultFileExtensionForInputMimeType(mimeType);
      if (ImageEncoder.INVALID_FILE_EXTENSION.equals(extension))
        throw new IOException("ImageEncoder do not known this mime type:"+mimeType); 
      
      String pathOfModifiedImage = 
        getPathOfModifiedImage(widthOfModifiedImage, heightOfModifiedImage, extension,iwc);
      
      // Does the image already exist? Then there is nothing to do.
      if (new File(pathOfModifiedImage).canRead()) 
        return pathOfModifiedImage;
      
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
    uploadImage(inputStream, mimeType, name, iwc);
    inputStream.close();
    return pathOfModifiedImage;
  }
 
 
  
  
  private ImageEncoder getImageEncoder(IWContext iwc)  throws RemoteException{
      return (ImageEncoder) IBOLookup.getServiceInstance(iwc,ImageEncoder.class);
  }  
  
  private void uploadImage(FileInputStream inputStream, String mimeType, String name, IWContext iwc)  {
    // create new image entity
    ImageEntityHome imageEntityHome = (ImageEntityHome)com.idega.data.IDOLookup.getHomeLegacy(ImageEntity.class);
    ImageEntity imageEntity;
		try {
			imageEntity = imageEntityHome.create();
		} catch (CreateException e) {
      return;
		}
    // store value of File
    imageEntity.setFileValue(inputStream);
    imageEntity.setMimeType(mimeType);
    
    
    
    imageEntity.setName(name);
    imageEntity.store();
    try {
			getImageEntity(iwc).addChild(imageEntity);
		} catch (SQLException e) {
      return;
		}
  }
  
  
  
  private String getPathOfModifiedImage(int width, int height, String extension, IWContext iwc) throws IOException {
    
    String separator = FileUtil.getFileSeparator();
    
    StringBuffer nameOfImage = new StringBuffer();
    
    IWMainApplication mainApp = iwc.getApplication();
    nameOfImage.append(mainApp.getApplicationRealPath());
    
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
    nameOfImage.append(width).append("_").append(height).append("_")
      .append(originalImageId).append("_").append(name)
      .append(".").append(extension);
    
    return nameOfImage.toString();
  }
    
    
  
  
/*  
  private String getAbsolutePathToCache(IWContext iwc) {
    IWMainApplication mainApp = iwc.getApplication();
    return (mainApp.getApplicationRealPath()) 
        + (mainApp.getIWCacheManager().IW_ROOT_CACHE_DIRECTORY);
  }
*/
}