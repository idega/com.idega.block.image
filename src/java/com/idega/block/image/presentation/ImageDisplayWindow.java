package com.idega.block.image.presentation;

import com.idega.presentation.IWContext;
import com.idega.presentation.Image;
import com.idega.presentation.Script;
import com.idega.presentation.Table;
import com.idega.presentation.ui.Window;

/**
 * Title:
 * Description:  A popup window for showing images. 
 *               Similar to the 
 *               com.idega.block.news.presentation.ImageWindow class
 *               
 * Copyright:    Copyright (c) 2003
 * Company:
 * @author <br><a href="mailto:thomas@idega.is">Thomas Hilbig</a><br>
 * @version 1.0
 */

public class ImageDisplayWindow extends Window{

  public static final String PARAMETER_IMAGE_ID = "parameter_image_id";
  public static final String PARAMETER_INFO = "paramter_info";
  public static final String PARAMETER_WIDTH = "parameter_width";
  public static final String PARAMETER_HEIGHT = "parameter_height";
  public static final String PARAMETER_BORDER = "parameter_border";
  public static final String PARAMETER_TITLE = "parameter_title";
  


	private String widthString;

	private String heightString;

  public ImageDisplayWindow() {
    setResizable(true);
    
  }


  public void main(IWContext iwc){
    
    Image image = null;
    String info = null;
    String title = "";
    int width = 0;
    int height = 0;
    int border = 0;
    this.setAllMargins(0);
    // set size
		setSize(iwc);
    // get title
    if (iwc.isParameterSet(PARAMETER_TITLE))
      title = iwc.getParameter(PARAMETER_TITLE);
    // set title
    setTitle(title);
     // get info text 
    if(iwc.isParameterSet(PARAMETER_INFO))
      info = iwc.getParameter(PARAMETER_INFO);
    // get image  
    if(iwc.isParameterSet(PARAMETER_IMAGE_ID)){
      try {
        int id = Integer.parseInt(iwc.getParameter(PARAMETER_IMAGE_ID));
        image = new Image(id);
      }
      catch (Exception ex) {
        info = "We are sorry: Image is not available";
      }
    }
    // define frameTable to force to show the image in the center
    // of the window
		Table frameTable = setFrameTable();
    
    // define table with the image and the info
    Table table = new Table(1,2);
    if(image !=null){
      table.add(image,1,1);
      if(info!=null)
        table.add(info,1,2);
    }
    frameTable.add(table);
    add(frameTable);


  }


	private Table setFrameTable() {
		Table frameTable = new Table(1,1);
		
		frameTable.setAlignment(1,1,Table.HORIZONTAL_ALIGN_CENTER);
		frameTable.setCellpadding(0);
		frameTable.setCellspacing(0);
		frameTable.setHeight(Table.HUNDRED_PERCENT);
		frameTable.setWidth(Table.HUNDRED_PERCENT);
		return frameTable;
	}


	private void setSize(IWContext iwc) {
		int width;
		int height;
		int border = 0;
		Script script = this.getAssociatedScript();
		if (script == null)
		  script = new Script();
		  
		script.addFunction("resizeWindow", "function resizeWindow(width,height) { if (parseInt(navigator.appVersion)>3) {   if (navigator.appName==\"Netscape\") {    top.outerWidth=width;    top.outerHeight=height;   }   else top.resizeTo(width,height); }}");
		
		// fetch desired border 
		if (iwc.isParameterSet(PARAMETER_BORDER)) {
		  String borderString = iwc.getParameter(PARAMETER_BORDER);  
		  try {
		    border = Integer.parseInt(borderString);
		  }  
		  catch (NumberFormatException ex) {
		    System.err.println("Parameter "+
		      PARAMETER_BORDER+ " was not an integer. Message was: "+ex.getMessage());
		    border = 0;
		  }
		}    
		if (iwc.isParameterSet(PARAMETER_WIDTH) && iwc.isParameterSet(PARAMETER_HEIGHT))  {
		  String widthString = iwc.getParameter(PARAMETER_WIDTH);
		  String heightString = iwc.getParameter(PARAMETER_HEIGHT);
		  try {
		    width = Integer.parseInt(widthString);
		    height = Integer.parseInt(heightString);
		    if (height > 0 && width > 0 ) {
		      height += border;
		      width += border;
		      this.setOnLoad("resizeWindow("+width+","+height+")");
		    }
		  }
		  catch (NumberFormatException ex){
		  System.err.println("Parameter "+
		    PARAMETER_HEIGHT + " or "+
		    PARAMETER_WIDTH+ " was not an integer. Message was: "+ex.getMessage());
		  // do nothing
		  // default size of window is defined in super class
		  }
		}      
	}

}

  
  
  
  
  
  
  
  
  
  
  
  
  

