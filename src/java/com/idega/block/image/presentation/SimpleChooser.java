package com.idega.block.image.presentation;

import com.idega.idegaweb.presentation.IWAdminWindow;
import com.idega.block.image.business.SimpleImage;
import com.idega.presentation.*;
import com.idega.presentation.text.*;
import com.idega.presentation.ui.*;
import com.idega.block.image.data.ImageEntity;
import com.idega.util.IWTimestamp;
import com.idega.idegaweb.IWBundle;
import com.idega.block.media.servlet.MediaServlet;


/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2000-2001 idega.is All Rights Reserved
 * Company:      idega
  *@author <a href="mailto:aron@idega.is">Aron Birkir</a>
 * @version 1.1
 */

 public class SimpleChooser extends PresentationObjectContainer implements SimpleImage{

    private String sessImageParameter = "image_id";
    private final static String IW_BUNDLE_IDENTIFIER="com.idega.block.image";
    private boolean includeLinks;
    private boolean usesOld = false;

    public void setToIncludeLinks(boolean includeLinks){
      this.includeLinks = includeLinks;
    }

    public String getBundleIdentifier(){
      return IW_BUNDLE_IDENTIFIER ;
    }

    public static String getSaveImageFunctionName(){
      return "saveImageId()";
    }

    public static String getSaveImageFunction(String imagename){
      StringBuffer function = new StringBuffer("");
      function.append(" var iImageId = -1 ; \n");
      function.append("function "+getSaveImageFunctionName()+" {\n \t");
      function.append("top.window.opener.setImageId(iImageId,'"+imagename+"') ; \n \t");
      function.append("top.window.close(); \n }");
      return function.toString();
    }

    public void  main(IWContext iwc){
      IWBundle iwb = getBundle(iwc);
      checkParameterName(iwc);

      if(iwc.getApplication().getSettings().getProperty(MediaServlet.USES_OLD_TABLES)!=null)
        usesOld = true;

        getParentPage().getAssociatedScript().addFunction("callbim",getSaveImageFunction(sessImageParameter) );


      //add("block.media");
      Table Frame = new Table();
      Frame.setCellpadding(0);
      Frame.setCellspacing(0);
      IFrame ifList = new IFrame(target1,SimpleLister.class);
      IFrame ifViewer = new IFrame(target2, SimpleViewer.class);
      /*
      if(usesOld){
        ifList = new IFrame(target1,com.idega.jmodule.image.presentation.SimpleLister.class);
        ifViewer = new IFrame(target2, com.idega.jmodule.image.presentation.SimpleViewer.class);
      }
      */
      ifList.setWidth(210);
      ifList.setHeight(410);
      ifViewer.setWidth(500);
      ifViewer.setHeight(410);

      ifList.setBorder(1);
      ifViewer.setBorder(1);
      Frame.add(ifList,1,1);
      Frame.add(ifViewer,2,1);
      Frame.setBorderColor("#00FF00");
      if(includeLinks)
        Frame.add(getLinkTable(iwb),2,2);

      add(Frame);
    }

    public void setSessionSaveParameterName(String prmName){
      sessImageParameter = prmName;
    }
    public String getSessionSaveParameterName(){
      return sessImageParameter;
    }
     public void checkParameterName(IWContext iwc){
       if(iwc.getParameter(sessImageParameterName)!=null){
        sessImageParameter = iwc.getParameter(sessImageParameterName);
        //add(sessImageParameter);
        iwc.setSessionAttribute(sessImageParameterName,sessImageParameter);
      }
      else if(iwc.getSessionAttribute(sessImageParameterName)!=null)
        sessImageParameter = (String) iwc.getSessionAttribute(sessImageParameterName);
    }

    public PresentationObject getLinkTable(IWBundle iwb){
      Table T = new Table();

      Text add = new Text("add");
      add.setFontStyle("text-decoration: none");
      add.setFontColor("#FFFFFF");
      add.setBold();
      Link btnAdd = getNewImageLink(add);

      Text del = new Text("delete");
      del.setFontStyle("text-decoration: none");
      del.setFontColor("#FFFFFF");
      del.setBold();
      Link btnDelete = getDeleteLink(del);

      Text save = new Text("use");
      save.setFontStyle("text-decoration: none");
      save.setFontColor("#FFFFFF");
      save.setBold();
      Link btnSave = getSaveLink(save);

      Text reload = new Text("reload");
      reload.setFontStyle("text-decoration: none");
      reload.setFontColor("#FFFFFF");
      reload.setBold();
      Link btnReload = getReloadLink(reload);

      T.add(btnAdd,1,1);
      T.add(btnSave,2,1);
      T.add(btnDelete,3,1);
      T.add(btnReload,4,1);

      return T;
    }

    public Link getNewImageLink(PresentationObject mo){
      //Class C = usesOld ? com.idega.jmodule.image.presentation.SimpleUploaderWindow.class :SimpleUploaderWindow.class;
      Class C = SimpleUploaderWindow.class;
      Link L = new Link(mo,C);
      L.addParameter("action","upload");
      L.addParameter("submit","new");
      L.setTarget(target2);
      return L;
    }

    public Link getSaveLink(PresentationObject mo){
      //Class C = usesOld ? com.idega.jmodule.image.presentation.SimpleViewer.class :SimpleViewer.class;
      Class C = SimpleViewer.class;
      Link L = new Link(mo,C);
      L.addParameter(prmAction,actSave);
      L.setOnClick(getSaveImageFunctionName());
      L.setTarget(target2);
      return L;
    }

    public Link getDeleteLink(PresentationObject mo){
      //Class C = usesOld ? com.idega.jmodule.image.presentation.SimpleViewer.class :SimpleViewer.class;
      Class C = SimpleViewer.class;
      Link L = new Link(mo,C);
      L.addParameter(prmAction,actDelete);
      L.setOnClick("top.setTimeout('top.frames.lister.location.reload()',150)");
      L.setTarget(target2);
      return L;
    }

    public Link getReloadLink(PresentationObject mo){
      //Class C = usesOld ? com.idega.jmodule.image.presentation.SimpleLister.class :SimpleLister.class;
      Class C = SimpleLister.class;
      Link L = new Link(mo,C);
      L.setTarget(target1);
      return L;
    }
}
