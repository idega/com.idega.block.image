package com.idega.block.image.presentation;

import java.sql.SQLException;

import com.idega.block.image.business.ImageBusiness;
import com.idega.block.image.business.SimpleImage;
import com.idega.block.media.data.MediaProperties;
import com.idega.presentation.IWContext;
import com.idega.presentation.Image;
import com.idega.presentation.PresentationObject;
import com.idega.presentation.Table;
import com.idega.presentation.ui.FileInput;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.SubmitButton;
import com.idega.presentation.ui.Window;
/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2000-2001 idega.is All Rights Reserved
 * Company:      idega
  *@author <a href="mailto:aron@idega.is">Aron Birkir</a>
 * @version 1.1
 */

public class SimpleUploaderWindow extends Window implements SimpleImage{

    String dataBaseType;
    private String sessImageParameter = "image_id";

    public SimpleUploaderWindow(){

    }

    public void setSessionSaveParameterName(String prmName){
      this.sessImageParameter = prmName;
    }
    public String getSessionSaveParameterName(){
      return this.sessImageParameter;
    }
     public void checkParameterName(IWContext iwc){
       if(iwc.getParameter(sessImageParameterName)!=null){
        this.sessImageParameter = iwc.getParameter(sessImageParameterName);
        iwc.setSessionAttribute(sessImageParameterName,this.sessImageParameter);
      }
      else if(iwc.getSessionAttribute(sessImageParameterName)!=null) {
				this.sessImageParameter = (String) iwc.getSessionAttribute(sessImageParameterName);
			}
    }

    @Override
	public void main(IWContext iwc){
      checkParameterName(iwc);
      this.setBackgroundColor("white");
      this.setTitle("Idega Uploader");
      control(iwc);
    }

    public void control(IWContext iwc){
      //add("block.media");
      //add(sessImageParameter);
      String sContentType = iwc.getRequest().getContentType();
      if(sContentType !=null && sContentType.indexOf("multipart")!=-1){
       // add(sContentType);
        add(parse(iwc));
      }
      else{
        if(iwc.getParameter("save")!=null){
          save(iwc);
        }
				else {
					add(getMultiForm(iwc));
				}
      }


    }
    public Form getMultiForm(IWContext iwc){
      Form f = new Form();
      f.setMultiPart();
      String s = iwc.getRequestURI()+"?"+com.idega.idegaweb.IWMainApplication.classToInstanciateParameter+"="+com.idega.idegaweb.IWMainApplication.getEncryptedClassName(this.getClass());
      f.setAction(s);
      //add(s);
      f.add(new FileInput());
      f.add(new SubmitButton());
      return f;
    }

    public PresentationObject parse(IWContext iwc){
      MediaProperties ip = null;
      try {
        ip = ImageBusiness.doUpload(iwc);
        iwc.setSessionAttribute("image_props",ip);
      }
      catch (Exception ex) {
        ex.printStackTrace();
      }

      if(ip!=null){
        Form form = new Form();
        Table T = new Table();
        T.add(new Image(ip.getWebPath()),1,1);
        SubmitButton save = new SubmitButton("save","Save");
        save.setOnClick("top.setTimeout('top.frames.lister.location.reload()',150)");
        T.add(save,1,2);
        T.add(new SubmitButton("newimage","New"),1,2);
        form.add(T);
        return form;
      }
      else{
        return getMultiForm(iwc);
      }

    }

    public void save(IWContext iwc){
      MediaProperties ip = null;
      if(iwc.getSessionAttribute("image_props")!=null){
        ip = (MediaProperties) iwc.getSessionAttribute("image_props");
        iwc.removeSessionAttribute("image_props");
      }
      if(ip !=null){
        int i = ImageBusiness.SaveImage(ip);
        iwc.setSessionAttribute(this.sessImageParameter,String.valueOf(i));
        try {
          add(new Image(i));
        }
        catch (SQLException ex) {

        }
      }
    }
}
