package com.idega.block.image.business;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.ejb.CreateException;
import javax.ejb.TransactionRolledbackLocalException;
import javax.media.jai.PlanarImage;
import javax.media.jai.RegistryMode;
import javax.media.jai.OperationRegistry;
import javax.media.jai.*;
import javax.transaction.SystemException;
import javax.transaction.TransactionRolledbackException;
import javax.transaction.UserTransaction;


import com.idega.block.image.data.ImageEntity;
import com.idega.block.image.data.ImageEntityHome;
import com.idega.block.image.presentation.AdvancedImage;
import com.idega.business.IBOServiceBean;
import com.idega.core.data.ICFile;
import com.idega.presentation.Image;
import com.sun.media.jai.codec.MemoryCacheSeekableStream;
/**
 *  * 
 * 
 * Title:         idegaWeb
 * Description:   This class is mainly used by the ImageGallery class to 
 *                get, to store and to change stored images.
 *                  
 * Copyright:     Copyright (c) 2003
 * Company:       idega software
 * @author <a href="mailto:thomas@idega.is">Thomas Hilbig</a>
 * @version 1.0
 */
public class ImageProviderBean extends IBOServiceBean implements ImageProvider {

  
  public ImageProviderBean(){}
    

  public int getImageCount(ICFile imageFolder) {
  	if (imageFolder == null)
  		return 0;
    return imageFolder.getChildCount();
  }
    
  
  public ArrayList getImagesFromTo(ICFile imageFolder, int startPosition, int endPosition) throws SQLException {
    if (imageFolder == null || (startPosition < 1) || (startPosition > endPosition))
      return new ArrayList();
    Iterator iterator = imageFolder.getChildren();  
    ArrayList result = new ArrayList(endPosition - startPosition + 1);
    int position = 0;
    AdvancedImage child;
      while (iterator.hasNext() && position < endPosition)  {
      position++;
      if (position < startPosition)  {
        iterator.next();
      }
      else  {
        ICFile imageFile = ((ICFile) iterator.next());
        child = new AdvancedImage(imageFile.getID(),imageFile.getName());
        result.add(child);
      }
    }
    return result;  
  }
  
  public int uploadImage(InputStream inputStream, String mimeType, String name, int width, int height, ImageEntity parent) 
    throws CreateException  {
    
    UserTransaction trans=null;
    int id = -1;
    try
    {
      trans = this.getSessionContext().getUserTransaction();
      trans.begin();
      // create new image entity
      ImageEntityHome imageEntityHome = (ImageEntityHome)com.idega.data.IDOLookup.getHomeLegacy(ImageEntity.class);
      ImageEntity imageEntity;
      // create throws CreateException
      imageEntity = imageEntityHome.create();
      // store value of File
      imageEntity.setFileValue(inputStream);
      imageEntity.setMimeType(mimeType);
      imageEntity.setName(name);
      imageEntity.setWidth(Integer.toString(width));
      imageEntity.setHeight(Integer.toString(height));
      // store throws RuntimeException
      imageEntity.store();
      // set new entity as a child 
      // addChild throws SQLException
      parent.addChild(imageEntity);
      id = imageEntity.getID();
      trans.commit();
    }
    catch (Exception e) {
      System.err.println(e.getMessage());
//      e.printStackTrace();
      if(trans!=null) {
        try {
          trans.rollback();
        }
        catch (SystemException se) {
          se.printStackTrace();
        } 
      }
      throw new CreateException("There was an error storing the modified image. Message was: "+e.getMessage());
    }
    return id;
  } 
  
  
  public void setHeightAndWidthOfOriginalImageToEntity(int width, int height, ImageEntity imageEntity) 
    throws TransactionRolledbackLocalException {
    UserTransaction trans = null;    
    try {
      trans = this.getSessionContext().getUserTransaction();
      trans.begin();  
      imageEntity.setWidth(Integer.toString(width));
      imageEntity.setHeight(Integer.toString(height));
      imageEntity.store();
      trans.commit();
    }
    catch (Exception e) {
      System.err.println(e.getMessage());
//      e.printStackTrace();
      if(trans!=null) {
        try {
          trans.rollback();
        }
        catch (SystemException se) {
          se.printStackTrace();
        } 
      }
      throw new TransactionRolledbackLocalException("There was an error storing height and width of the image. Message was: "+ e.getMessage());
    }
  } 
    
}
  
  
 

