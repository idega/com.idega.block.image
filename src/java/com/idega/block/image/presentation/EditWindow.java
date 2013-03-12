package com.idega.block.image.presentation;

import com.idega.presentation.ui.Window;
import com.idega.presentation.IWContext;
import com.idega.block.image.data.*;
import com.idega.block.image.business.ImageBusiness;
import com.idega.block.media.data.MediaProperties;
import com.idega.presentation.text.*;
import com.idega.presentation.*;
import com.idega.presentation.ui.*;
import com.idega.core.file.data.ICFileCategory;
/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company: idega software
 * @author Eirikur Hrafnsson, eiki@idega.is
 * @version 1.0
 */

public class EditWindow extends Window {
  private String windowColor = "#336699";
  private String language = "IS";
  private Table outerTable = new Table(1,2);
  private Image save;
  private Image cancel;

  public EditWindow(){
    super();
    setTitle("IdegaWeb : Image");
    setWidth(800);
    setHeight(600);
    setAllMargins(0);
    setResizable(true);
  }

  public EditWindow(String name){
    super(name);
  }

  public EditWindow(int width, int heigth) {
    super(width,heigth);
  }

  public EditWindow(String name,int width,int height){
    super(name, width, height);
  }

  public EditWindow(String name,String url){
    super(name,url);
  }

  public EditWindow(String name, int width, int height, String url){
    super(name, width, height, url);
  }

  public void main(IWContext iwc)throws Exception{
    setBackgroundColor(this.windowColor);
    setAllMargins(0);
    setTitle("IdegaWeb : Image");

    this.save = new Image("/pics/jmodules/image/"+this.language+"/save.gif");
    this.cancel = new Image("/pics/jmodules/image/"+this.language+"/cancel.gif");


    this.outerTable.setCellpadding(0);
    this.outerTable.setCellspacing(0);
    this.outerTable.setWidth("100%");
    this.outerTable.setHeight("100%");
    this.outerTable.setHeight(1,1,"25");
    this.outerTable.setBackgroundImage(1,1,new Image("/pics/jmodules/image/myndamodule/topp/topptiler.gif"));
    this.outerTable.setVerticalAlignment(1,2,"top");

    String action = iwc.getParameter("action");
    if("save_text".equalsIgnoreCase(action)){
       ImageBusiness.handleTextSave(iwc);
       close(iwc);
    }
    else if("upload".equalsIgnoreCase(action)){
      this.outerTable.add(getUploadForm(iwc),1,2);
    }
    else if("text".equalsIgnoreCase(action)){
      this.outerTable.add(getEditForm(iwc),1,2);
    }
    else if("save_image".equalsIgnoreCase(action)){
      ImageBusiness.handleSaveImage(iwc);
      close(iwc);
    }
    else{
      uploadAndSaveToCategory(iwc);
    }
    add(this.outerTable);

  }


  private void close(IWContext iwc){
    setParentToReload();
    close();
  }


  private Form getEditForm(IWContext iwc) throws Exception{
    String imageId = iwc.getParameter("image_id");
    Form form = new Form();
    form.add(new HiddenInput("image_id",imageId));
    form.add(new HiddenInput("action","save_text"));

    Table table = new Table(1,5);
    table.setWidth("100%");
    table.setHeight("100%");
    table.setAlignment(1,1,"left");
    table.setAlignment(1,2,"left");
    table.setAlignment(1,3,"left");
    table.setAlignment(1,4,"left");
    table.setVerticalAlignment(1,1,"top");
    table.setVerticalAlignment(1,2,"top");
    table.setVerticalAlignment(1,3,"top");
    table.setVerticalAlignment(1,4,"top");
    table.setAlignment(1,5,"right");
    table.setWidth(1,1,"10%");
    table.setWidth(1,2,"20%");
    table.setWidth(1,3,"10%");
    table.setWidth(1,4,"20%");


    ImageEntity image = ((com.idega.block.image.data.ImageEntityHome)com.idega.data.IDOLookup.getHomeLegacy(ImageEntity.class)).findByPrimaryKeyLegacy(Integer.parseInt(imageId));
    String imageText = image.getDescription();
    String imageLink = image.getLink();

    if( imageText==null ) {
			imageText = "";
		}
    if( imageLink==null ) {
			imageLink= "";
		}


    TextArea input = new TextArea("image_text",imageText);
    TextInput input2 = new TextInput("image_link",imageLink);

    Text texti = new Text("Texti með mynd");
    texti.setFontColor("#FFFFFF");
    texti.setFontSize(2);
    texti.setBold();

    Text texti2 = new Text("Tengill á mynd og texta");
    texti2.setFontColor("#FFFFFF");
    texti2.setFontSize(2);
    texti2.setBold();

    input.setWidth(35);
    input.setWrap(true);

    input.setWidth(35);

    table.add(texti,1,1);
    table.add(input,1,2);
    table.add(texti2,1,3);
    table.add(input2,1,4);
    table.add(new SubmitButton(this.cancel,"submit","cancel"),1,5);
    table.add(new SubmitButton(this.save),1,5);


    form.add(table);
    return form;
  }

  private Form getUploadForm(IWContext iwc) throws Exception{
    Form form = new Form();
    form.setMultiPart();
    //workaround
    String uri = iwc.getRequestURI();
    if(uri.indexOf("Instanciator")==-1){
      form.setAction(uri+"?"+Page.IW_FRAME_CLASS_PARAMETER+"="+com.idega.idegaweb.IWMainApplication.getEncryptedClassName(this.getClass()));
    }
    else{
      form.setAction(uri+"?"+com.idega.idegaweb.IWMainApplication.classToInstanciateParameter+"="+com.idega.idegaweb.IWMainApplication.getEncryptedClassName(this.getClass()));
    }

    Table table = new Table(2,2);
    Text texti = new Text("Veldu mynd með því að ýta á \"Browse\" og smelltu svo á \"Submit\".");
    texti.setFontColor("#FFFFFF");
    texti.setFontSize(2);
    texti.setBold();
    table.mergeCells(1,1,2,1);
    table.add(texti,1,1);
    table.add(Text.getBreak(),1,2);
    table.add(Text.getBreak(),2,2);
    table.add(new FileInput(),1,2);
    table.add(new SubmitButton(),2,2);
    table.setAlignment(1,2,"left");
    table.setAlignment(2,2,"right");
    form.add(table);
    return form;
  }

  private void uploadAndSaveToCategory(IWContext iwc) throws Exception{
    Form form = new Form();
    form.add(new HiddenInput("action","save_image"));
    Table upload = new Table(1,3);
    upload.setWidth("100%");
    upload.setHeight("100%");
    upload.setHeight(1,1,"25");
    upload.setHeight(1,2,"25");
    upload.setAlignment(1,3,"center");
    upload.setVerticalAlignment(1,3,"top");

    try{
      MediaProperties ip = ImageBusiness.doUpload(iwc);
      iwc.setSessionAttribute("im_ip",ip);
      Image imagefile = new Image(ip.getWebPath());

      Text texti = new Text("Veldu nú myndaflokk og hakaðu við þær aukastærðir af myndinni sem þú vilt fá.");
      texti.setFontColor("#FFFFFF");
      texti.setFontSize(2);
      texti.setBold();

      Table toolbar = new Table(6,1);
      toolbar.setWidth("100%");
      toolbar.setAlignment(1,1,"left");
      toolbar.setAlignment(2,1,"left");
      toolbar.setAlignment(3,1,"left");
      toolbar.setAlignment(4,1,"left");
      toolbar.setAlignment(5,1,"right");
      toolbar.setAlignment(6,1,"right");

      toolbar.setWidth(1,1,"12%");
      toolbar.setWidth(2,1,"16%");
      toolbar.setWidth(5,1,"10%");
      toolbar.setWidth(6,1,"10%");

      Text flokkur = new Text("Myndaflokkur ");
      flokkur.setFontColor("#FFFFFF");
      flokkur.setFontSize(2);
      flokkur.setBold();



      ICFileCategory[] imgCat = (ICFileCategory[]) (((com.idega.core.file.data.ICFileCategoryHome)com.idega.data.IDOLookup.getHomeLegacy(ICFileCategory.class)).createLegacy()).findAll();
      DropdownMenu category = new DropdownMenu("category_id");
      for (int i = 0 ; i < imgCat.length ; i++ ) {
        category.addMenuElement(imgCat[i].getID(),imgCat[i].getName());
      }

      upload.add(texti,1,1);

      toolbar.add(flokkur,1,1);
      toolbar.add(category,2,1);
      toolbar.add(new SubmitButton(this.cancel,"submit","cancel"),5,1);
      toolbar.add(new SubmitButton(this.save),6,1);

      upload.add(toolbar);
      upload.add(imagefile,1,3);

      form.addAtBeginning(upload);
      this.outerTable.add(form,1,2);

    }
    catch(Exception e){
    this.outerTable.add("Error while uploading!",1,2);
    e.printStackTrace(System.err);
    }
  }

}
