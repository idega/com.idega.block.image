package com.idega.block.image.presentation;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2000-2001 idega.is All Rights Reserved
 * Company:      idega
  *@author <a href="mailto:aron@idega.is">Aron Birkir</a>
 * @version 1.1
 */

import java.sql.*;
import java.util.*;
import java.io.*;
import com.idega.util.*;
import com.idega.presentation.text.*;
import com.idega.presentation.*;
import com.idega.presentation.ui.*;
import com.idega.block.media.business.ImageBusiness;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWMainApplication;
import com.idega.block.media.data.ImageEntity;
import com.idega.block.media.business.ImageFinder;

public class ImageAttributeSetter extends Block{

  private final static String IW_BUNDLE_IDENTIFIER="com.idega.block.image";
  private int imageId = -1;
  private String sSessionName =null;
  private String sHiddenInputName = null;
  public static final String sSessionParameterName = "im_image_session_name";
  public static String prmImageEntityId = "prmImageEntityId";
  public static String prmAttributeKey = "prmAttributeKey";
  private Map oldMap,newMap;

  public static final String ALIGNMENT = "align",BORDER = "border",VSPACE = "vspace",
                              HSPACE = "hspace",WIDTH = "width",HEIGHT="height";

  private IWBundle iwb;
  private IWResourceBundle iwrb;

  public ImageAttributeSetter(){
    this.sSessionName="image_attributes";
    this.sHiddenInputName = "image_attributes";
  }

  public ImageAttributeSetter(String SessionParameterName) {
    this.sSessionName=SessionParameterName;
    this.sHiddenInputName = SessionParameterName;
  }

  public static Link getLink(PresentationObject linkObject,int iImageEntityId,String sAttributeKey){
    Link L = new Link(linkObject );
    L.setWindowToOpen(ImageAttributeWindow.class);
    L.addParameter(prmAttributeKey,sAttributeKey );
    L.addParameter(prmImageEntityId, iImageEntityId );
    return L;
  }

  public void main(IWContext iwc)throws Exception{
    this.empty();
    iwb = getBundle(iwc);
    iwrb = getResourceBundle(iwc);

    String sAttributeKey = iwc.getParameter(prmAttributeKey );
    String sImageEntityId = iwc.getParameter(prmImageEntityId );
    add(sAttributeKey + " " + sImageEntityId );
    if(sImageEntityId != null && sAttributeKey !=null){
      int iImageEntityId = Integer.parseInt(sImageEntityId );
      String oldAttributes = getImageAttributes(sAttributeKey,iImageEntityId);
      oldMap = getAttributeMap(oldAttributes);
      if(iwc.getParameter("save")!=null){
        String attributeString = processForm(iwc);
        add("<br>"+attributeString);
        saveImageAttributes(sAttributeKey,attributeString,iImageEntityId);
        //saveString( iwc,attributeString);
      }
      add(getForm(oldMap,sAttributeKey ,sImageEntityId ));
      Table T  = new Table();
      try {
        Image image = new Image(iImageEntityId);
        image.setAttributes(oldMap);
        T.add(getText("bla "));
        T.add(image);
        T.add(getText("blu "));

        add(T);
      }
      catch (SQLException ex) {
        add("image error");
        ex.printStackTrace();
      }
    }
    else
      add("no attributekey or image id ");
  }

  public String getText(String bla){
    StringBuffer text = new StringBuffer();
    for (int i = 0; i < 60; i++) {
      text.append(bla);
    }
    return text.toString();
  }

  public PresentationObject getForm(Map map,String sAttributeKey,String sImageEntityId){
    Form form = new Form();
    Table T = new Table();
    T.add(getLayoutTable(map) ,1,1);
    //T.add(getSizeTable(map),2,1);
    SubmitButton save = new SubmitButton("save","Save");
    T.add(save,2,2);
    T.add(new HiddenInput(sHiddenInputName,getAttributeString(map)));
    T.add(new HiddenInput(prmAttributeKey,sAttributeKey));
    T.add(new HiddenInput(prmImageEntityId, sImageEntityId));
    form.add(T);
    return form;
  }

  public String getImageAttributes(String key,int id){
    return ImageFinder.getImageAttributes(key,id);
  }

  public boolean saveImageAttributes(String key,String att, int id){
    return ImageFinder.saveImageAttributes(key,att,id);
  }

  public void saveString(IWContext iwc, String attributeString){
    iwc.setSessionAttribute(sSessionName,attributeString);
  }

  public String processForm(IWContext iwc){
    String alignment = iwc.getParameter(ALIGNMENT);
    String border = iwc.getParameter(BORDER);
    String hspace = iwc.getParameter(HSPACE);
    String vspace = iwc.getParameter(VSPACE);
    String width = iwc.getParameter(WIDTH);
    String height = iwc.getParameter(HEIGHT);
    addAttribute(ALIGNMENT,alignment,oldMap );
    addAttribute(BORDER,border,oldMap  );
    addAttribute(HSPACE,hspace,oldMap );
    addAttribute(VSPACE,vspace,oldMap );
    addAttribute(WIDTH,width,oldMap );
    addAttribute(HEIGHT,height,oldMap );

    return getAttributeString(oldMap);
  }

  private void addAttribute(String key, String value,Map map){
    //System.err.print("addAttribute("+key+","+value+")  ");
    if(value !=null ){
      if(!"".equals(value)){
        map.put(key,value);
        //System.err.print("put("+key+","+value+")");
      }
      else if(map.containsKey(key)){
        map.remove(key);
        //System.err.print("remove("+key+")");
      }
    }
    //System.err.println();
  }

  public PresentationObject getLayoutTable(Map map){
    Table T = new Table();
    String alignment = map.containsKey(ALIGNMENT)?(String)map.get(ALIGNMENT):"";
    String border = map.containsKey(BORDER)?(String)map.get(BORDER):"";
    String hspace = map.containsKey(HSPACE)?(String)map.get(HSPACE):"";
    String vspace = map.containsKey(VSPACE)?(String)map.get(VSPACE):"";
    String width = map.containsKey(WIDTH)?(String)map.get(WIDTH):"";
    String height = map.containsKey(HEIGHT)?(String)map.get(HEIGHT):"";
    T.add(toText( iwrb.getLocalizedString("alignment","Alignment") ) ,1,1 );
    T.add(getAlignmentDropdownMenu(alignment),2,1);
    T.add(toText( iwrb.getLocalizedString("border_thickness","Border thickness") ), 1,2 );
    T.add(getBorderInput(border),2,2);
    T.add(toText( iwrb.getLocalizedString("horizontal_spacing","Horizontal spacing") ), 1,3 );
    T.add(getHSpaceInput(hspace),2,3);
    T.add(toText( iwrb.getLocalizedString("vertical_spacing","Vertical spacing") ), 1,4 );
    T.add(getVSpaceInput(vspace),2,4);
    T.add(toText( iwrb.getLocalizedString("width","Width") ) ,1,5 );
    T.add(getWidthInput(width),2,5);
    T.add(toText( iwrb.getLocalizedString("height","Height") ), 1,6 );
    T.add(getHeightInput(height),2,6);
    return T;
  }

  public PresentationObject getSizeTable(Map map){
    Table T = new Table();
    String width = map.containsKey(WIDTH)?(String)map.get(WIDTH):"";
    String height = map.containsKey(HEIGHT)?(String)map.get(HEIGHT):"";
    T.add(toText( iwrb.getLocalizedString("width","Width") ) ,1,1 );
    T.add(getWidthInput(width),2,1);
    T.add(toText( iwrb.getLocalizedString("height","Height") ), 1,2 );
    T.add(getHeightInput(height),2,2);
    return T;
  }

  public TextInput getBorderInput(String content){
    TextInput border = new TextInput(BORDER);
    border.setAsIntegers();
    border.setLength(4);
    border.setContent(content);
    return border;
  }

  public TextInput getVSpaceInput(String content){
    TextInput vspace = new TextInput(VSPACE);
    vspace.setAsIntegers();
    vspace.setLength(4);
    vspace.setContent(content);
    return vspace ;
  }

  public TextInput getHSpaceInput(String content){
    TextInput hspace = new TextInput(HSPACE);
    hspace.setAsIntegers();
    hspace.setLength(4);
    hspace.setContent(content);
    return hspace;
  }

  public TextInput getHeightInput(String content){
    TextInput height = new TextInput(HEIGHT);
    height.setLength(4);
    height.setContent(content);
    return height;
  }

  public TextInput getWidthInput(String content){
    TextInput width = new TextInput(WIDTH);
    width.setLength(4);
    width.setContent(content);
    return width;
  }

  public DropdownMenu getAlignmentDropdownMenu(String selected){
    DropdownMenu drp = new DropdownMenu(ALIGNMENT);
    drp.addMenuElement("",iwrb.getLocalizedString("default","Default"));
    drp.addMenuElement("left",iwrb.getLocalizedString("left","Left"));
    drp.addMenuElement("right",iwrb.getLocalizedString("right","Right"));
    drp.addMenuElement("top",iwrb.getLocalizedString("top","Top"));
    drp.addMenuElement("texttop",iwrb.getLocalizedString("texttop","Text top"));
    drp.addMenuElement("middle",iwrb.getLocalizedString("middle","Middle"));
    drp.addMenuElement("absmiddle",iwrb.getLocalizedString("absmiddle","Absmiddle"));
    drp.addMenuElement("baseline",iwrb.getLocalizedString("baseline","Baseline"));
    drp.addMenuElement("bottom",iwrb.getLocalizedString("bottom","Bottom"));
    drp.addMenuElement("absbottom",iwrb.getLocalizedString("absbottom","Absbottom"));
    drp.addMenuElement("absbottom",iwrb.getLocalizedString("absbottom","Absbottom"));
    drp.setSelectedElement(selected );
    return drp;
  }

  public Text toText(String text){
    Text t = new Text(text);
    return t;
  }



  public static String getFunction(int id){
    return "setImageId("+id+")";
  }

  public String getImageChangeJSFunction(){
    StringBuffer function = new StringBuffer("");//var imageName = \"rugl\"; \n");
    function.append("function setImageId(imageId) { \n \t");
    function.append("if (document.images) { \n \t\t");
    function.append("document.rugl.src = \"/servlet/MediaServlet/\"+imageId+\"media?media_id=\"+imageId; \n\t ");
    function.append("document.forms[0]."+sHiddenInputName+".value = imageId \n\t}\n }");

    return function.toString();
  }

  public String getBundleIdentifier(){
    return IW_BUNDLE_IDENTIFIER;
  }

}