package com.idega.block.image.presentation;

import java.util.*;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.text.*;

import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;


import com.idega.block.image.business.ImageProvider;
import com.idega.block.image.business.ImageProviderBean;
import com.idega.builder.data.IBPage;
import com.idega.business.IBOLookup;
import com.idega.core.data.ICFile;
import com.idega.presentation.IWContext;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.SubmitButton;

import com.idega.presentation.Block;
import com.idega.presentation.PresentationObject;
import com.idega.presentation.Table;
import com.idega.presentation.Image;
import com.sun.image.codec.jpeg.*;

/**
 * @author Thomas
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class ImageGallery extends Block {
    
	// folder with the images 
  private ICFile imageFileFolder = null;
  // enlarge image to given height and width
  private boolean enlargeImage = false;
  // heigth of the images
  private int heightOfImages = -1;
  // width of the images
  private int widthOfImages = -1;
  // page where the images are shown when you click on it
  private IBPage viewerPage;
    
  private int rows = 1;
  private int columns = 2;   
    
             
  // corresponding bundle
  private static final String IW_BUNDLE_IDENTIFIER="com.idega.block.image";

  private int step;
  	
  public ImageGallery() {
        }

  public String getBundleIdentifier(){
    return this.IW_BUNDLE_IDENTIFIER;
  }

  public void setFilesFolder(ICFile imageFileFolder){
    this.imageFileFolder=imageFileFolder;
  }

  public void setHeightOfImages(int heightOfImages){
    this.heightOfImages = heightOfImages;
  }
    
  public void setWidthOfImages(int widthOfImages){
    this.widthOfImages = widthOfImages;
  }

  public void setEnlargeImage(boolean enlargeImage) {
    this.enlargeImage = enlargeImage;
  }

  public void setViewerPage(IBPage viewerPage) {
    this.viewerPage = viewerPage;
  }
        
  public void setRows(int rows) {
    this.rows = rows;
  }
    
  public void setColumns(int columns)  {
    this.columns = columns;
  }  

  public void main(IWContext iwc) throws Exception{
    Table mainTable = new Table(1,2); 
    mainTable.add(getImageTable(iwc),1,1);
    mainTable.add(getButtonTable(iwc),1,2);
    add(mainTable);
  }

  private Table getImageTable(IWContext iwc) throws Exception {
    ArrayList images = getImages(iwc);
    Table galleryTable = new Table(columns,rows); 
    AdvancedImage image;              
    int count = -1;
    Iterator iterator = images.iterator();
    while  (iterator.hasNext()) {
      count++;
      image = (AdvancedImage) iterator.next();
      /* table starts with coordinates 1,1 !
      fill the table: start at the left corner, fill the first row, 
      then go to the second row and so on  */
      if (widthOfImages > 0)
        image.setHeight(heightOfImages);
      if (heightOfImages > 0)
        image.setWidth(widthOfImages);
      image.setEnlargeProperty(enlargeImage);
      galleryTable.add(image, ((count%columns)+1), ((count/columns)+1));
    }
    return galleryTable;
  }

//  private PresentationObject getCheckedAndModifiedImage(AdvancedImage image) {
//
//    try {
//		  adjustSizeOfImage(image);
//    } 
//    catch (Exception ex)  {
//      return new Text("Can't adjust image");
//    }
//
//    /*Image modifiedImage;
//    try {
//      modifiedImage = image.getModifiedImage();
//    }
//    catch (Exception ex)  {
//      return new Text("Can't retrieve image");
//    }*/
//    Link link;
//    try {
//      link = new Link(image);
//    } 
//    catch (Exception ex)  {
//      return new Text("Can't build Link");
//    }
//      
//    link.setPage(viewerPage);
//    //link.addParameter(com.idega.block.media.servlet.MediaServlet.PARAMETER_NAME,image.getIDOfFile() );
//    return link; 
//  }

//	private void adjustSizeOfImage(AdvancedImage image) throws Exception {
//		/* modify height, if
//		+ desired height is defined
//		+ image should be enlarged (if it is smaller than the desired height)
//		+ imgage is too large for the desired heigth
//		*/
//		int newHeight = 0;
//		int newWidth = 0;
//		boolean imageMustBeModified = false;
//    
//		if ( heightOfImages > 0 && 
//		      ( enlargeImage ||
//		        (newHeight = Integer.decode(image.getHeight()).intValue()) > heightOfImages)) {
//		    newHeight = heightOfImages;
//		    imageMustBeModified = true;
//    }
//            
//		/* modify width, if...
//		see explanation above 
//		*/
//		if ( widthOfImages > 0 && 
//		    ( enlargeImage ||
//		        (newWidth = Integer.decode(image.getWidth()).intValue()) > widthOfImages))  {
//		    newWidth = widthOfImages;
//		    imageMustBeModified = true;
//    }
//    // now adjust size of image if necessary
//		if  (imageMustBeModified) {
//		  if (newHeight == 0)
//		    newHeight = Integer.decode(image.getHeight()).intValue();
//		  if (newWidth == 0)
//		    newWidth = Integer.decode(image.getWidth()).intValue();
//		  image.scaleImage(newWidth,newHeight);
//		}
//	}

  private SubmitButton createButton(String displayText) {
    SubmitButton button = new SubmitButton(Integer.toString(this.getICObjectInstanceID()),displayText);
    button.setToEncloseByForm(true);
    return button;  
  }
    
  private Table getButtonTable(IWContext iwc)  throws Exception{
    SubmitButton backButton = createButton("<<");
    SubmitButton forwardButton = createButton(">>");
    int limit = getImageProvider(iwc).getImageCount(imageFileFolder);
    int startPosition = restoreNumberOfFirstImage(iwc);
    int endPosition;
    if ((endPosition = startPosition + getStep() - 1) >= limit)
      endPosition = limit;
    StringBuffer infoText = new StringBuffer(); 
      infoText.
      append(" ").
      append(startPosition).
      append("-").
      append(endPosition).
      append(" ").
      append(this.getResourceBundle(iwc).getLocalizedString("of","of")).
      append(" ").
      append(limit);
    // possibly disable buttons  
    if (startPosition == 1)
      backButton.setDisabled(true);
    if (endPosition == limit)
      forwardButton.setDisabled(true);
    // arrange  table
    // three columns and one row
    Table buttonTable = new Table(3,1);
    buttonTable.add(backButton,1,1);
    buttonTable.add(new Text(infoText.toString()),2,1);
    buttonTable.add(forwardButton,3,1);
    return buttonTable;
  }
      

  private String getParameter(IWContext iwc)  throws Exception{
    return iwc.getParameter(getObjectInstanceIdentifierString());      
  }  
  
  private ArrayList getImages(IWContext iwc) throws Exception{
    int step = getStep();
    int startPosition = restoreNumberOfFirstImage(iwc);
    int newStartPosition;
    String parameterValue =  getParameter(iwc);
    if (">>".equals(parameterValue)) 
      newStartPosition = startPosition + step;
    else if ("<<".equals(parameterValue)) 
      newStartPosition = startPosition - step;
    else 
      newStartPosition = startPosition;
    if (newStartPosition > 0 && newStartPosition <= getImageProvider(iwc).getImageCount(imageFileFolder))
      startPosition = newStartPosition;
    storeNumberOfFirstImage(iwc,startPosition);
    return getImagesFromTo(iwc, startPosition, startPosition + step - 1);
  }

    
  private ArrayList getImagesFromTo(IWContext iwc, int startPosition, int endPosition) throws RemoteException, java.sql.SQLException{
    return getImageProvider(iwc).getImagesFromTo(imageFileFolder, startPosition, endPosition);
  }
    
  private void storeNumberOfFirstImage(IWContext iwc, int firstImageNumber) {
    System.out.println("ObjectInstance: "+ getObjectInstanceIdentifierString());
    System.out.println("Stored Number: "+ new Integer(firstImageNumber));
    iwc.setSessionAttribute( getObjectInstanceIdentifierString() , new Integer(firstImageNumber));
  }
    
  private int restoreNumberOfFirstImage(IWContext iwc)  {
    Integer i = (Integer) iwc.getSessionAttribute(getObjectInstanceIdentifierString());
    System.out.println("ObjectInstance: "+ getObjectInstanceIdentifierString());
    if (i ==null)
      return 1;  
    System.out.println("Restored: "+ i.toString());
    return i.intValue();           
  }
  
  private String getObjectInstanceIdentifierString()  {
    return Integer.toString(this.getICObjectInstanceID());
  }
    
  private ImageProvider getImageProvider(IWContext iwc)  throws RemoteException{
      return (ImageProvider) IBOLookup.getServiceInstance(iwc,ImageProvider.class);
  }
    
  private int getStep() {
    return rows * columns;
  }
}
     
 