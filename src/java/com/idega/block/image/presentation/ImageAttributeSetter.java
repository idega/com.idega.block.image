package com.idega.block.image.presentation;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2000-2001 idega.is All Rights Reserved
 * Company:      idega
  *@author <a href="mailto:aron@idega.is">Aron Birkir</a>
 * @version 1.1
 */

import com.idega.idegaweb.*;
import com.idega.builder.presentation.IBPageChooser;
import com.idega.block.media.presentation.ImageInserter;
import com.idega.block.text.business.TextFormatter;
import java.sql.*;
import java.util.*;
import java.io.*;
import com.idega.util.*;
import com.idega.presentation.text.*;
import com.idega.presentation.*;
import com.idega.presentation.ui.*;
import com.idega.block.image.business.ImageBusiness;
import com.idega.block.image.data.ImageEntity;
import com.idega.block.image.business.ImageFinder;

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
  public static final String ZOOMIMAGE = "zoom_image", ZOOMHEIGHT = "zoom_height",
			      ZOOMWIDTH = "zoom_width", ZOOMPAGE = "zoom_page";

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
    iwb = getBundle(iwc);
    iwrb = getResourceBundle(iwc);

    String sAttributeKey = iwc.getParameter(prmAttributeKey );
    String sImageEntityId = iwc.getParameter(prmImageEntityId );
    if(sImageEntityId != null && sAttributeKey !=null){
      int iImageEntityId = Integer.parseInt(sImageEntityId );
      String oldAttributes = getImageAttributes(sAttributeKey,iImageEntityId);
      oldMap = getAttributeMap(oldAttributes);
      if(iwc.isParameterSet("save")){
	String attributeString = processForm(iwc);
	saveImageAttributes(sAttributeKey,attributeString,iImageEntityId);
      }

      add(getForm(oldMap,sAttributeKey ,sImageEntityId ));
    }
    else
      add("no attributekey or image id ");
  }

  public PresentationObject getForm(Map map,String sAttributeKey,String sImageEntityId){
    Form form = new Form();
      form.add(new HiddenInput(sHiddenInputName,getAttributeString(map)));
      form.add(new HiddenInput(prmAttributeKey,sAttributeKey));
      form.add(new HiddenInput(prmImageEntityId, sImageEntityId));

    Table T = new Table(1,2);
      T.setWidth(Table.HUNDRED_PERCENT);
    T.add(getLayoutTable(map) ,1,1);

    SubmitButton save = new SubmitButton(iwrb.getLocalizedImageButton("save","Save"),"save");
    CloseButton close = new CloseButton(iwrb.getLocalizedImageButton("close","Close"));
    T.add(close,1,2);
    T.add(Text.getNonBrakingSpace(),1,2);
    T.add(save,1,2);

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
    String zwidth = iwc.getParameter(ZOOMWIDTH);
    String zheight = iwc.getParameter(ZOOMHEIGHT);
    String zimage = iwc.getParameter(ZOOMIMAGE);
    String zpage = iwc.getParameter(ZOOMPAGE);
    addAttribute(ALIGNMENT,alignment,oldMap );
    addAttribute(BORDER,border,oldMap  );
    addAttribute(HSPACE,hspace,oldMap );
    addAttribute(VSPACE,vspace,oldMap );
    addAttribute(WIDTH,width,oldMap );
    addAttribute(HEIGHT,height,oldMap );
    addAttribute(ZOOMWIDTH,zwidth,oldMap );
    addAttribute(ZOOMHEIGHT,zheight,oldMap );
    addAttribute(ZOOMIMAGE,zimage,oldMap );
    if ( zpage != null )
      addAttribute(ZOOMPAGE,zpage,oldMap );

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
    Table T = new Table(2,10);
      T.setColumnAlignment(1,Table.HORIZONTAL_ALIGN_RIGHT);
      T.setColumnVerticalAlignment(1,Table.VERTICAL_ALIGN_TOP);
      T.setVerticalAlignment(1,8,Table.VERTICAL_ALIGN_MIDDLE);
    String alignment = map.containsKey(ALIGNMENT)?(String)map.get(ALIGNMENT):"";
    String border = map.containsKey(BORDER)?(String)map.get(BORDER):"";
    String hspace = map.containsKey(HSPACE)?(String)map.get(HSPACE):"";
    String vspace = map.containsKey(VSPACE)?(String)map.get(VSPACE):"";
    String width = map.containsKey(WIDTH)?(String)map.get(WIDTH):"";
    String height = map.containsKey(HEIGHT)?(String)map.get(HEIGHT):"";
    String zoomImage = map.containsKey(ZOOMIMAGE)?(String)map.get(ZOOMIMAGE):null;
    String zoomPage = map.containsKey(ZOOMPAGE)?(String)map.get(ZOOMPAGE):null;
    String zoomHeight = map.containsKey(ZOOMHEIGHT)?(String)map.get(ZOOMHEIGHT):"";
    String zoomWidth = map.containsKey(ZOOMWIDTH)?(String)map.get(ZOOMWIDTH):"";

    T.add(toText( iwrb.getLocalizedString("alignment","Alignment")+":" ) ,1,1 );
    T.add(getAlignmentDropdownMenu(alignment),2,1);
    T.add(toText( iwrb.getLocalizedString("border_thickness","Border thickness")+":" ), 1,2 );
    T.add(getBorderInput(border),2,2);
    T.add(toText( iwrb.getLocalizedString("horizontal_spacing","Horizontal spacing")+":" ), 1,3 );
    T.add(getHSpaceInput(hspace),2,3);
    T.add(toText( iwrb.getLocalizedString("vertical_spacing","Vertical spacing")+":" ), 1,4 );
    T.add(getVSpaceInput(vspace),2,4);
    T.add(toText( iwrb.getLocalizedString("width","Width")+":" ) ,1,5 );
    T.add(getHeightAndWidthInput(width,WIDTH),2,5);
    T.add(toText( iwrb.getLocalizedString("height","Height")+":" ), 1,6 );
    T.add(getHeightAndWidthInput(height,HEIGHT),2,6);

    ImageInserter insert = new ImageInserter(ZOOMIMAGE);
      insert.setHasUseBox(false);
      insert.setImageHeight(40);
      insert.setMaxImageWidth(50);
      insert.setImageWidth(50);
    if ( zoomImage != null )
      insert.setImageId(Integer.parseInt(zoomImage));

    IBPageChooser file = new IBPageChooser(ZOOMPAGE,IWConstants.BUILDER_FONT_STYLE_INTERFACE_SMALL);
    if ( zoomPage != null )
      file.setSelectedPage(Integer.parseInt(zoomPage),"Page");

    T.add(toText(iwrb.getLocalizedString("zoom_image","Zoom image")+":"),1,7);
    T.add(toText(iwrb.getLocalizedString("zoom_page","Zoom page")+":"),1,8);
    T.add(toText(iwrb.getLocalizedString("zoom_width","Zoom image width")+":"),1,9);
    T.add(toText(iwrb.getLocalizedString("zoom_height","Zoom image height")+":"),1,10);

    T.add(insert,2,7);
    T.add(file,2,8);
    T.add(getHeightAndWidthInput(zoomWidth,ZOOMWIDTH),2,9);
    T.add(getHeightAndWidthInput(zoomHeight,ZOOMHEIGHT),2,10);

    return T;
  }

  public PresentationObject getSizeTable(Map map){
    Table T = new Table(2,2);
    String width = map.containsKey(WIDTH)?(String)map.get(WIDTH):"";
    String height = map.containsKey(HEIGHT)?(String)map.get(HEIGHT):"";
    T.add(toText( iwrb.getLocalizedString("width","Width") ) ,1,1 );
    T.add(getWidthInput(width),2,1);
    T.add(toText( iwrb.getLocalizedString("height","Height") ), 1,2 );
    T.add(getHeightAndWidthInput(height,HEIGHT),2,2);
    return T;
  }

  public TextInput getBorderInput(String content){
    TextInput border = new TextInput(BORDER);
    border.setAsIntegers();
    border.setLength(4);
    border.setContent(content);
    border.setStyleAttribute(IWConstants.BUILDER_FONT_STYLE_INTERFACE_SMALL);
    return border;
  }

  public TextInput getVSpaceInput(String content){
    TextInput vspace = new TextInput(VSPACE);
    vspace.setAsIntegers();
    vspace.setLength(4);
    vspace.setContent(content);
    vspace.setStyleAttribute(IWConstants.BUILDER_FONT_STYLE_INTERFACE_SMALL);
    return vspace ;
  }

  public TextInput getHSpaceInput(String content){
    TextInput hspace = new TextInput(HSPACE);
    hspace.setAsIntegers();
    hspace.setLength(4);
    hspace.setContent(content);
    hspace.setStyleAttribute(IWConstants.BUILDER_FONT_STYLE_INTERFACE_SMALL);
    return hspace;
  }

  public TextInput getHeightAndWidthInput(String content,String name){
    TextInput height = new TextInput(name);
    height.setLength(4);
    height.setContent(content);
    height.setStyleAttribute(IWConstants.BUILDER_FONT_STYLE_INTERFACE_SMALL);
    return height;
  }

  public TextInput getWidthInput(String content){
    TextInput width = new TextInput(WIDTH);
    width.setLength(4);
    width.setContent(content);
    width.setStyleAttribute(IWConstants.BUILDER_FONT_STYLE_INTERFACE_SMALL);
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
    drp.setSelectedElement(selected );
    drp.setStyleAttribute(IWConstants.BUILDER_FONT_STYLE_INTERFACE_SMALL);
    return drp;
  }

  public Text toText(String text){
    Text t = new Text(text);
      t.setFontStyle(IWConstants.BUILDER_FONT_STYLE_LARGE);
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
