package com.idega.block.image.presentation;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;

import com.idega.block.image.business.ImageProvider;
import com.idega.builder.data.IBPage;
import com.idega.business.IBOLookup;
import com.idega.core.data.ICFile;
import com.idega.presentation.Block;
import com.idega.presentation.IWContext;
import com.idega.presentation.PresentationObject;
import com.idega.presentation.Table;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.SubmitButton;

/**
 *  * 
 * 
 * Title:         idegaWeb
 * Description:   ImageGallery is a block to show images that are stored in
 *                a specified folder. A subset of these images is shown in a table.
 *                The sample can be changed by clicking on a forward and a back button.
 *                If there are more than one ImageGallery on a single page 
 *                each gallery works independently of the others. 
 *                  
 * Copyright:     Copyright (c) 2003
 * Company:       idega software
 * @author <a href="mailto:thomas@idega.is">Thomas Hilbig</a>
 * @version 1.0
 */
public class ImageGallery extends Block {
    
	// folder with the images 
  private ICFile imageFileFolder = null;
  // enlarge image to specified height and width
  private boolean enlargeImage = false;
  // heigth of the images
  private int heightOfImages = -1;
  // width of the images
  private int widthOfImages = -1;
  // page where the images are shown when you click on it
  private IBPage viewerPage;
  // show image in a special popup window
  private boolean popUpOriginalImageOnClick = false;
  // show name of image in table
  private boolean showNameOfImage = false;
  // number of new images that is shown per step
  private int numberOfImagesPerStep = 0;
  // flag to show if the image should keep it´s proportion
  private boolean scaleProportional = true;
  // border of all images
  private int borderOfImage = 0;
  
  // table properties...
  private int cellBorderTable = 0;
  private String colorCellBorderTable = null;
  
  private int cellSpacingTable = 0;
  private int cellPaddingTable = 0;
       
  private int rows = 1;
  private int columns = 1;   
    
             
  // corresponding bundle
  private static final String IW_BUNDLE_IDENTIFIER="com.idega.block.image";
  
  // string forward button
  private static final String STRING_FORWARD_BUTTON = ">";
  // string back button
  private static final String STRING_BACK_BUTTON = "<";
    	
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
  
  public void setScaleProportional(boolean scaleProportional) {
    this.scaleProportional = scaleProportional;
  }
        
  public void setRows(int rows) {
    if (rows > 0)
      this.rows = rows;
  }
    
  public void setColumns(int columns)  {
    if (columns > 0)
      this.columns = columns;
  }  

  public void setShowNameOfImage(boolean showNameOfImage)  {
    this.showNameOfImage = showNameOfImage;
  }

  public void setPopUpOriginalImageOnClick(boolean popUpOriginalImageOnClick) {
    this.popUpOriginalImageOnClick = popUpOriginalImageOnClick;
  }

  public void setNumberOfImagesPerStep(int numberOfImagesPerStep) {
    this.numberOfImagesPerStep = numberOfImagesPerStep;
  }
  
  public void setBorderOfImage(int borderOfImage)  {
    this.borderOfImage = borderOfImage;
  }
  
  public void setCellBorderTable(int cellBorderTable) {
    this.cellBorderTable = cellBorderTable;
  }
  
  public void setColorCellBorderTable(String colorCellBorderTable)  {
    this.colorCellBorderTable = colorCellBorderTable;
  }
  
  public void setCellPadding(int cellPaddingTable)  {
    this.cellPaddingTable = cellPaddingTable;
  }
  
  public void setCellSpacing(int cellSpacingTable)  {
    this.cellSpacingTable = cellSpacingTable;
  }

  public void main(IWContext iwc) throws Exception{
    Table mainTable = new Table(1,2); 
    mainTable.add(getImageTable(iwc),1,1);
    mainTable.add(getButtonTable(iwc),1,2);
    add(mainTable);
  }

  private Table getImageTable(IWContext iwc) throws Exception {
    ArrayList images = getImages(iwc);
    // insert rows if names should be shown
    int rowsOfTable = (showNameOfImage)? (rows * 2) : (rows);
    Table galleryTable = new Table(columns,rowsOfTable); 
    if (cellPaddingTable > 0)
      galleryTable.setCellpadding(cellPaddingTable);
    if (cellSpacingTable > 0)
      galleryTable.setCellspacing(cellSpacingTable);
    if (cellBorderTable > 0)
      galleryTable.setBorder(cellBorderTable);
    if (colorCellBorderTable != null)
      galleryTable.setBorderColor(colorCellBorderTable);
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
      // set properties of advanced image
      image.setEnlargeProperty(enlargeImage);
      image.setScaleProportional(scaleProportional);
      if (borderOfImage > 0)
        image.setBorder(borderOfImage);
      int xPositionImage = ((count%columns)+1);
      int yPositionImage;          
      if (showNameOfImage)  {
        yPositionImage = ((count/columns)*2)+1;
        galleryTable.add(image.getName(), xPositionImage, yPositionImage+1); 
      }
      else  {
        yPositionImage = ((count/columns)+1);     
      }  
      PresentationObject pres = null;
      // check if a link to a viewer page should be added
      if (viewerPage != null) {
        Link link;
        link = new Link(image);
        link.setPage(viewerPage);
        link.addParameter(com.idega.block.media.servlet.MediaServlet.PARAMETER_NAME,image.getImageID(iwc));
        pres = (PresentationObject) link;
      }
      // check if a link to a popup window should be added
      else if (popUpOriginalImageOnClick)  {
        image.setLinkToDisplayWindow(iwc);
        pres = (PresentationObject) image;
      }
      // show only the image without a link
      else  {
        pres = (PresentationObject) image;
      }
      // set size of the cell that shows the image
      if (heightOfImages > 0)
        galleryTable.setHeight(xPositionImage, yPositionImage, Integer.toString(heightOfImages));
      if (widthOfImages > 0)
        galleryTable.setWidth(xPositionImage, yPositionImage, Integer.toString(widthOfImages));
      galleryTable.add(pres, xPositionImage, yPositionImage);
    }
    return galleryTable;
  }

  private SubmitButton createButton(String displayText) {
    SubmitButton button = new SubmitButton(Integer.toString(this.getICObjectInstanceID()),displayText);
    button.setToEncloseByForm(true);
    return button;  
  }
    
  private Table getButtonTable(IWContext iwc)  throws Exception{
    SubmitButton backButton = createButton(STRING_BACK_BUTTON);
    SubmitButton forwardButton = createButton(STRING_FORWARD_BUTTON);
    int limit = getImageProvider(iwc).getImageCount(imageFileFolder);
    int startPosition = restoreNumberOfFirstImage(iwc);
    int endPosition;
    if ((endPosition = startPosition + getNumberOfImagePlaces() - 1) >= limit)
      endPosition = limit;
    // special case: If there are not any imgages do not show start position one but zero  
    int displayedStartPosition = (limit == 0) ? 0 : startPosition;
    // create an info text showing the number of the first image and the last image
    // that are currently shown and the total numbers of imgages:
    // for example: 2 - 6 of 9
    StringBuffer infoText = new StringBuffer(); 
    // show: "2 - 6 of 9"
    // special case: Only one image is shown, in this case avoid showing: "2 - 2 of 9"
    if (displayedStartPosition != endPosition)  {
      infoText.append(" ").
      append(displayedStartPosition).
      append("-");
    }
    infoText.append(endPosition).
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
    if (STRING_FORWARD_BUTTON.equals(parameterValue)) 
      newStartPosition = startPosition + step;
    else if (STRING_BACK_BUTTON.equals(parameterValue)) 
      newStartPosition = startPosition - step;
    else 
      newStartPosition = startPosition;
    if (newStartPosition > 0 && newStartPosition <= getImageProvider(iwc).getImageCount(imageFileFolder))
      startPosition = newStartPosition;
    storeNumberOfFirstImage(iwc,startPosition);
    return getImagesFromTo(iwc, startPosition, startPosition + getNumberOfImagePlaces() - 1);
  }
    
  private ArrayList getImagesFromTo(IWContext iwc, int startPosition, int endPosition) throws RemoteException, java.sql.SQLException{
    return getImageProvider(iwc).getImagesFromTo(imageFileFolder, startPosition, endPosition);
  }
    
  private void storeNumberOfFirstImage(IWContext iwc, int firstImageNumber) {
    iwc.setSessionAttribute( getObjectInstanceIdentifierString() , new Integer(firstImageNumber));
  }
    
  private int restoreNumberOfFirstImage(IWContext iwc)  {
    Integer i = (Integer) iwc.getSessionAttribute(getObjectInstanceIdentifierString());
    if (i == null)
      return 1;  
    return i.intValue();           
  }
  
  private String getObjectInstanceIdentifierString()  {
    return Integer.toString(this.getICObjectInstanceID());
  }
    
  private ImageProvider getImageProvider(IWContext iwc)  throws RemoteException{
      return (ImageProvider) IBOLookup.getServiceInstance(iwc,ImageProvider.class);
  }
    
  private int getStep() {
    int totalSumOfImagesInTable = getNumberOfImagePlaces();
    return (numberOfImagesPerStep > 0 && 
            numberOfImagesPerStep < totalSumOfImagesInTable) ?
            numberOfImagesPerStep : totalSumOfImagesInTable;
  }
  
  private int getNumberOfImagePlaces()  {
    // how many images can I show in the current table?
    return rows * columns;
  }
}
     
 