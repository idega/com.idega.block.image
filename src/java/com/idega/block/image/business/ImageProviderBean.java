package com.idega.block.image.business;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.media.jai.PlanarImage;
import javax.media.jai.RegistryMode;
import javax.media.jai.OperationRegistry;
import javax.media.jai.*;


import com.idega.block.image.data.ImageEntity;
import com.idega.block.image.presentation.AdvancedImage;
import com.idega.business.IBOServiceBean;
import com.idega.core.data.ICFile;
import com.idega.presentation.Image;
import com.sun.media.jai.codec.MemoryCacheSeekableStream;
/*
/**
 * @author Thomas
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class ImageProviderBean extends IBOServiceBean implements ImageProvider{

  
  public ImageProviderBean(){}
    

  public int getImageCount(ICFile imageFolder) {
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
}
  
 

