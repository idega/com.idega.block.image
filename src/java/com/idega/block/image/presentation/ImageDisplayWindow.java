package com.idega.block.image.presentation;

import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.ejb.FinderException;

import com.idega.block.image.business.ImageProvider;
import com.idega.business.IBOLookup;
import com.idega.business.IBOLookupException;
import com.idega.business.IBORuntimeException;
import com.idega.core.file.data.ICFile;
import com.idega.core.file.data.ICFileHome;
import com.idega.data.IDOLookup;
import com.idega.data.IDOLookupException;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.IWContext;
import com.idega.presentation.Image;
import com.idega.presentation.Script;
import com.idega.presentation.Table;
import com.idega.presentation.text.Link;
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

	private static final String IW_BUNDLE_IDENTIFIER = "com.idega.block.image";

	public static final String PARAMETER_IMAGE_ID = "parameter_image_id";
  public static final String PARAMETER_IMAGE_NUMBER = "parameter_image_number";
  public static final String PARAMETER_INFO = "paramter_info";
  public static final String PARAMETER_WIDTH = "parameter_width";
  public static final String PARAMETER_HEIGHT = "parameter_height";
  public static final String PARAMETER_BORDER = "parameter_border";
  public static final String PARAMETER_TITLE = "parameter_title";
  

	private String widthString;
	private String heightString;
	private int border;

  public ImageDisplayWindow() {
    setResizable(true);
    //(Unsolved problem: why do the gifs images stop when this window pop up?)
  }

	public String getBundleIdentifier() {
		return this.IW_BUNDLE_IDENTIFIER;
	}


  public void main(IWContext iwc){
    IWResourceBundle iwrb = getResourceBundle(iwc);
    
    Image image = null;
    String info = null;
    String title = "";
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
    int id = -1;
    if(iwc.isParameterSet(PARAMETER_IMAGE_ID)){
      try {
        id = Integer.parseInt(iwc.getParameter(PARAMETER_IMAGE_ID));
        image = new Image(id);
      }
      catch (Exception ex) {
        info = "We are sorry: Image is not available";
      }
    }
    
    int imageNumber = -1;
    if (iwc.isParameterSet(PARAMETER_IMAGE_NUMBER)) {
    		imageNumber = Integer.parseInt(iwc.getParameter(PARAMETER_IMAGE_NUMBER));
    }
    AdvancedImage previousImage = null;
    AdvancedImage nextImage = null;
    if (id != -1) {
			try {
				ICFile imageFile = ((ICFileHome) IDOLookup.getHome(ICFile.class)).findByPrimaryKey(new Integer(-1));
				ICFile imageFolder = (ICFile) imageFile.getParentEntity();
				
				previousImage = getPreviousImage(iwc, imageFolder, imageNumber);
				nextImage = getNextImage(iwc, imageFolder, imageNumber);
			}
			catch (IDOLookupException ile) {
				log(ile);
			}
			catch (FinderException fe) {
				log(fe);
			}
    }    
    // define frameTable to force to show the image in the center
    // of the window
		Table frameTable = setFrameTable();
    
    // define table with the image and the info
    Table table = new Table(1,3);
    // set aligments
    table.setAlignment(1,1,Table.HORIZONTAL_ALIGN_CENTER);
    table.setAlignment(1,2,Table.HORIZONTAL_ALIGN_CENTER);
    table.setAlignment(1,3,Table.HORIZONTAL_ALIGN_CENTER);
    // add image if available
    if(image !=null){
      table.add(image,1,1);
    // add info if necessary
      if(info!=null)
        table.add(info,1,2);
    }
    
    Table buttonTable = new Table(3, 1);
    buttonTable.setWidth(Table.HUNDRED_PERCENT);
    buttonTable.setWidth(1, "33%");
    buttonTable.setWidth(2, "33%");
    buttonTable.setWidth(3, "33%");
    buttonTable.setAlignment(2, 1, Table.HORIZONTAL_ALIGN_CENTER);
    buttonTable.setAlignment(3, 1, Table.HORIZONTAL_ALIGN_RIGHT);
    
    Link close = new Link(iwrb.getLocalizedString("close", "Close"));
    close.setAsCloseLink();
    buttonTable.add(close, 2, 1);
    
    if (previousImage != null) {
    		try {
    			Link previous = getImageLink(iwc, iwrb.getLocalizedString("previous", "Previous"), previousImage, imageNumber - 1);
    			buttonTable.add(previous, 1, 1);
    		}
    		catch (Exception e) {
    			log(e);
    		}
    }
    if (nextImage != null) {
	  		try {
	  			Link next = getImageLink(iwc, iwrb.getLocalizedString("next", "Next"), nextImage, imageNumber + 1);
	  			buttonTable.add(next, 3, 1);
	  		}
	  		catch (Exception e) {
	  			log(e);
	  		}
    }

    table.add(buttonTable,1,3);
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
		border = 0;
		Script script = this.getAssociatedScript();
		if (script == null)
		  script = new Script();
		  
		//script.addFunction("resizeWindow", "function resizeWindow(width,height) { if (parseInt(navigator.appVersion)>3 && navigator.appName==\"Netscape\") {    top.outerWidth=width;    top.outerHeight=height;   }   else top.resizeTo(width,height); }");
		script.addFunction("resizeWindow", "function resizeWindow(width,height) {\n\ttop.resizeTo(width,height);\n}");
		
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

	private ImageProvider getImageProvider(IWContext iwc) {
		try {
			return (ImageProvider) IBOLookup.getServiceInstance(iwc, ImageProvider.class);
		}
		catch (IBOLookupException ile) {
			throw new IBORuntimeException(ile);
		}
	}
	
	private AdvancedImage getPreviousImage(IWContext iwc, ICFile imageFolder, int imageNumber) {
		if (imageNumber > 0) {
			try {
				ArrayList list = getImageProvider(iwc).getImagesFromTo(imageFolder, imageNumber, imageNumber-1);
				if (list != null) {
					Iterator iter = list.iterator();
					while (iter.hasNext()) {
						return (AdvancedImage) iter.next();
					}
				}
			}
			catch (SQLException sql) {
				log(sql);
			}
			catch (RemoteException re) {
				log(re);
			}
		}
		
		return null;
	}
	
	private AdvancedImage getNextImage(IWContext iwc, ICFile imageFolder, int imageNumber) {
		try {
			ArrayList list = getImageProvider(iwc).getImagesFromTo(imageFolder, imageNumber, imageNumber+1);
			if (list != null) {
				Iterator iter = list.iterator();
				while (iter.hasNext()) {
					return (AdvancedImage) iter.next();
				}
			}
		}
		catch (SQLException sql) {
			log(sql);
		}
		catch (RemoteException re) {
			log(re);
		}
		
		return null;
	}
	
	private Link getImageLink(IWContext iwc, String displayString, AdvancedImage image, int imageNumber) throws Exception {
    Link link = new Link(displayString);
    link.setParameter(ImageDisplayWindow.PARAMETER_BORDER, Integer.toString(border));
    link.setParameter(ImageDisplayWindow.PARAMETER_WIDTH, Integer.toString(image.getWidthOfOriginalImage(iwc)));
    link.setParameter(ImageDisplayWindow.PARAMETER_HEIGHT, Integer.toString(image.getHeightOfOriginalImage(iwc)));
    link.setParameter(ImageDisplayWindow.PARAMETER_IMAGE_NUMBER, String.valueOf(imageNumber));
		return link;
	}
}