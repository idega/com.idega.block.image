package com.idega.block.image.business;

import com.idega.block.media.data.*;
import java.sql.SQLException;
/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2000-2001 idega.is All Rights Reserved
 * Company:      idega
  *@author <a href="mailto:aron@idega.is">Aron Birkir</a>
 * @version 1.1
 */

public class ImageFinder {

  public ImageFinder() {
  }

  public static String getImageAttributes(String key,int iImageId){
    try {
      return new ImageEntity(iImageId).getMetaData(key);
    }
    catch (SQLException ex) {
      ex.printStackTrace();
    }
    return "";
  }

  public static boolean saveImageAttributes(String key,String att,int id){

    try {
      ImageEntity image = new ImageEntity(id);
      image.addMetaData(key,att);
      image.update();
      return true;
    }
    catch (SQLException ex) {
      ex.printStackTrace();
    }
    return false;
  }

}