package com.idega.block.image.presentation;

import com.idega.block.media.business.MediaBusiness;
import com.idega.presentation.PresentationObjectContainer;
import com.idega.presentation.*;
import com.idega.presentation.text.*;
import com.idega.presentation.ui.*;
import com.idega.block.image.data.ImageEntity;
import com.idega.util.idegaTimestamp;
import java.sql.*;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2000-2001 idega.is All Rights Reserved
 * Company:      idega
  *@author <a href="mailto:aron@idega.is">Aron Birkir</a>
 * @version 1.1
 */

public class SimpleViewer extends PresentationObjectContainer{
    public String prmImageView = "img_view_id";
    public static final String prmAction = "img_view_action";
    public static final String actSave = "save",actDelete = "delete",actConfirmDelete="conf_delete";
    public static final String sessionSaveParameter = "img_id";
    public static final String sessionParameter = "image_id";
    public String sessImageParameterName = "im_image_session_name";
    public String sessImageParameter = "image_id";

    public void  main(IWContext iwc){

      //add("block.media");
      String sImageId = getImageId(iwc);
      String sAction = iwc.getParameter(prmAction);

      if(sImageId != null){
        //saveImageId(iwc,sImageId);
        if(sAction != null){
          if(sAction.equals(actSave)){
            saveImageId(iwc,sImageId);
          }
          else if(sAction.equals(actDelete)){
           ConfirmDeleteImage(sImageId,iwc);
          }
          else if(sAction.equals(actConfirmDelete)){
            deleteImage( sImageId);
            removeFromSession(iwc);
          }
        }
       {
          int id = Integer.parseInt(sImageId);
          try {
            ImageEntity ieImage = ((com.idega.block.image.data.ImageEntityHome)com.idega.data.IDOLookup.getHomeLegacy(ImageEntity.class)).findByPrimaryKeyLegacy(id);
            Table T = new Table();
            T.add(ieImage.getName(),1,1);
            T.add(new Image(id),1,2);
            add(T);

          }
          catch (SQLException ex) {
            add("error");
          }
        }
      }
    }

    public void checkParameterName(IWContext iwc){
     if(iwc.getParameter(sessImageParameterName)!=null){
      sessImageParameter = iwc.getParameter(sessImageParameterName);
      iwc.setSessionAttribute(sessImageParameterName,sessImageParameter);
    }
    else if(iwc.getSessionAttribute(sessImageParameterName)!=null)
      sessImageParameter = (String) iwc.getSessionAttribute(sessImageParameterName);
    }

    public boolean deleteImage(String sImageId){
     /*
      Connection Conn = null;

      try{
        Conn = com.idega.util.database.ConnectionBroker.getConnection();
        ResultSet RS;
        Statement Stmt = Conn.createStatement();
        int r = Stmt.executeUpdate("DELETE FROM IMAGE_IMAGE_CATAGORY WHERE IMAGE_ID = "+sImageId);
        Stmt.close();
      }
      catch(SQLException ex){
        ex.printStackTrace();
      }
      finally{
        if(Conn != null)
          com.idega.util.database.ConnectionBroker.freeConnection(Conn);
      }
*/
      try {
        int iImageId = Integer.parseInt(sImageId);
        ((com.idega.block.image.data.ImageEntityHome)com.idega.data.IDOLookup.getHomeLegacy(ImageEntity.class)).findByPrimaryKeyLegacy(iImageId).delete();
        return true;
      }
      catch (SQLException ex) {
        ex.printStackTrace();
        return false;
      }
      catch (NumberFormatException ex){
        return false;
      }
    }

    public void ConfirmDeleteImage(String sImageId,IWContext iwc){
       Table T = new Table();
       T.setWidth("100%");
       T.setHeight("100%");
       int id = Integer.parseInt(sImageId);
          try {
            ImageEntity ieImage = ((com.idega.block.image.data.ImageEntityHome)com.idega.data.IDOLookup.getHomeLegacy(ImageEntity.class)).findByPrimaryKeyLegacy(id);

            Text warning = new Text("Are you sure ?");
            warning.setFontSize(6);
            warning.setFontColor("FF0000");
            warning.setBold();
            Image image = new Image(id);
            image.setURL(MediaBusiness.getMediaURL(id,iwc.getApplication()));
            T.setBackgroundImage(1,2,image);
            T.add(ieImage.getName(),1,1);
            T.add(warning,1,2);
            T.setHeight(1,2,"100%");
            T.setAlignment(1,2,"center");
            Link confirm = new Link("delete");
            confirm.addParameter(prmAction ,actConfirmDelete);
            confirm.addParameter(sessImageParameter,sImageId);
            T.add(confirm,1,3);
            //T.add(new Image(id),1,2);
            //add(T);
          }
          catch (SQLException ex) {
            T.add("error");
          }
        add(T);
    }

    public String getImageId(IWContext iwc){
      if(iwc.getParameter(sessImageParameterName)!=null)
       sessImageParameter = iwc.getParameter(sessImageParameterName);
      else if(iwc.getSessionAttribute(sessImageParameterName)!=null){
        sessImageParameter = (String) iwc.getSessionAttribute(sessImageParameterName);
      }
      //add(sessImageParameter);
      String s = null;
      if(iwc.getParameter(sessImageParameter)!=null){
        s = iwc.getParameter(sessImageParameter);
        iwc.setSessionAttribute(sessImageParameter+"_2",s);
      }
      else if(iwc.getSessionAttribute(sessImageParameter)!=null)
        s = (String) iwc.getSessionAttribute(sessImageParameter);
      else if(iwc.getSessionAttribute(sessImageParameter+"_2")!=null)
        s = (String) iwc.getSessionAttribute(sessImageParameter+"_2");
      //add(" " +s);
      return s;
    }


    public void removeFromSession(IWContext iwc){
      iwc.removeSessionAttribute(sessImageParameter);
    }

    public void saveImageId(IWContext iwc,String sImageId){
      //System.err.println("SimpleViewer : "+sessImageParameter+" "+ sImageId);
      iwc.setSessionAttribute(sessImageParameter,sImageId);
      iwc.removeSessionAttribute(sessImageParameter+"_2");
      //iwc.setSessionAttribute(sessImageParameter+"2",sImageId);
    }

    public void saveImage(IWContext iwc,String sImageId){
      iwc.setSessionAttribute(sessionSaveParameter,sImageId);
    }

  }
