package com.idega.block.image.business;

import java.io.File;
import java.io.FileInputStream;
import java.sql.SQLException;
import java.util.List;

import com.idega.block.image.data.ImageEntity;
import com.idega.block.media.data.MediaProperties;
import com.idega.core.data.ICFileCategory;
import com.idega.data.DatastoreInterface;
import com.idega.data.EntityFinder;
import com.idega.data.IDOLegacyEntity;
import com.idega.presentation.IWContext;

/**
 * Title: ImageBusiness
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company: idega
 * @author Eirikur Hrafnsson
 * @version 1.2
 *
 */


public class ImageBusiness  {

  public static int IM_BROWSER_WIDTH = 800;
  public static int IM_BROWSER_HEIGHT = 600;
  public static int IM_MAX_WIDTH = 140;


/*
public static void saveImageToCategories(int imageId, String[] categoryId)throws SQLException {
  ImageEntity image = ((com.idega.block.image.data.ImageEntityHome)com.idega.data.IDOLookup.getHomeLegacy(ImageEntity.class)).findByPrimaryKeyLegacy(imageId);
  image.setParentId(-1);//only top level images saved to categories
  image.update();

  for (int i = 0; i < categoryId.length; i++) {
    try{
      int category = Integer.parseInt(categoryId[i]);
      ImageCategory cat = new ImageCategory(category);
      cat.addTo(image);
    }
    catch(NumberFormatException e){
      System.err.println("ImageBusiness : categoryId is not a number");
    }
  }
}

*/

public static void handleEvent(IWContext iwc,ImageHandler handler) throws Exception{

  String action = iwc.getParameter("action");
  String scaling = iwc.getParameter("scale.x");
  String imageId2 = iwc.getParameter("image_id");

  int imageId = (handler!=null)? handler.getOriginalImageId() : Integer.parseInt(imageId2);

  if ( action != null){
        if ( action.equalsIgnoreCase("Grayscale") ) handler.convertModifiedImageToGrayscale();
        else if ( action.equalsIgnoreCase("Emboss") ) handler.embossModifiedImage();
        else if ( action.equalsIgnoreCase("Invert") ) handler.invertModifiedImage();
        else if ( action.equalsIgnoreCase("Sharpen") ) handler.sharpenModifiedImage();
        else if( action.equalsIgnoreCase("Save") ){
          //System.out.println("ImageBusiness: Saving");
          handler.writeModifiedImageToDatabase(true);
        }
        else if( action.equalsIgnoreCase("Savenew") ){
          //System.out.println("ImageBusiness: Saving new image");
          handler.writeModifiedImageToDatabase(false);
        }
        else if( action.equalsIgnoreCase("Undo") || action.equalsIgnoreCase("Revert") ){
          handler.setModifiedImageAsOriginal();
        }
        else if( action.equalsIgnoreCase("delete") ){
          try{

            ImageEntity image = ((com.idega.block.image.data.ImageEntityHome)com.idega.data.IDOLookup.getHomeLegacy(ImageEntity.class)).findByPrimaryKeyLegacy( imageId );
            image.removeFrom(((com.idega.core.data.ICFileCategoryHome)com.idega.data.IDOLookup.getHomeLegacy(ICFileCategory.class)).createLegacy());
            image.delete();
            iwc.removeSessionAttribute("image_in_session");
            iwc.removeSessionAttribute("handler");

            /*ImageEntity parent = (ImageEntity) this.getParentNode();
            Iterator iter = (ImageEntity[]) image.getChildren();

            //brake childs from parent
            while (iter.hasNext()) {
              ImageEntity item = (ImageEntity) iter.next();
              image.addChild();
              if( parent != null ){
                parent.addChild(item);
              }                if( (catagories!=null) && (catagories.length>0) ){
                  for (int k = 0; k < catagories.length; k++) {
                    catagories[k].addTo(childs[i]);
                  }
                }
            }


            ICFileCategory[] catagories = (ImageCategory[]) image.findReverseRelated(com.idega.data.GenericEntity.getStaticInstance("com.idega.block.media.data.ImageCategory"));


            image.removeFrom(com.idega.data.GenericEntity.getStaticInstance("com.idega.block.media.data.ImageCategory"));

            image.delete();
*/


          }
          catch(Exception e){
            e.printStackTrace(System.err);
            System.out.println(e.getMessage());
          }
        }
  }

  if( scaling!=null ){
    if(!scaling.equalsIgnoreCase("0")){//didn't push the button

      String height = iwc.getRequest().getParameter("height");
      String width = iwc.getRequest().getParameter("width");
      String constraint = iwc.getRequest().getParameter("constraint");

      if( constraint!=null ) {

          handler.keepProportions(true);

          if( (height!=null) &&(height!="") && !(height.equalsIgnoreCase("")) ) {
                  if ( Integer.parseInt(height) != handler.getModifiedHeight() ){
                          handler.setModifiedHeight(Integer.parseInt(height));
                  }
                  else handler.setModifiedHeight(-1);
          }

          if( (width!=null) &&(width!="") && !(width.equalsIgnoreCase("")) ) {
                  if ( Integer.parseInt(width) != handler.getModifiedWidth() ){
                          handler.setModifiedWidth(Integer.parseInt(width));
                  }
                  else handler.setModifiedWidth(-1);
          }

       }
       else{

        if( (height!=null) &&(height!="") && !(height.equalsIgnoreCase("")) ) { handler.setModifiedHeight(Integer.parseInt(height)); }
        if( (width!=null) &&(width!="") && !(width.equalsIgnoreCase("")) ) { handler.setModifiedWidth(Integer.parseInt(width)); }

       }

        handler.resizeImage();
      }
    }
}


public static void makeDefaultSizes(IWContext iwc){
  try{
    /**
    *@todo : get the image bundle and make these default image sizes
    **/
  }
  catch(Exception ex){}
}


    public static List getImageCategories(){
      try {
        return EntityFinder.findAll(((com.idega.core.data.ICFileCategoryHome)com.idega.data.IDOLookup.getHomeLegacy(ICFileCategory.class)).createLegacy());
      }
      catch (Exception ex) {
        return null;
      }

    }


    public static String getDatastoreType(IDOLegacyEntity entity){
      return DatastoreInterface.getDatastoreType(entity.getDatasource());
    }



    public static void storeEditForm(IWContext iwc){
        String catagoriTextInputName = "category";  // same as in ImageViewer getEditForm
        String deleteTextInputName = "delete";      // same as in ImageViewer getEditForm
        String idees = "ids";      // same as in ImageViewer getEditForm

        String[] categoryName = iwc.getParameterValues(catagoriTextInputName);
        String[] deleteValue = iwc.getParameterValues(deleteTextInputName);
        String[] ids = iwc.getParameterValues(idees);

        ICFileCategory category = ((com.idega.core.data.ICFileCategoryHome)com.idega.data.IDOLookup.getHomeLegacy(ICFileCategory.class)).createLegacy();

        //change
  //      if(categoryName != null && categoryName.length > 0){
  //        for (int i = 0; i < categoryName.length; i++) {
  //          String tempName = categoryName[i];
  //          category = new ImageCategory(deleteValue[i]);
  //        }
  //
  //      }

         //debug this is experimental code NOT failsafe!
        try {
          int k = ids.length;
          ICFileCategory temp;
          for (int i = 0; i < categoryName.length; i++) {
            if (categoryName[i] != null && !"".equals(categoryName[i]) ) {
              String tempName = categoryName[i];

              if( i >= k ){//insert
                temp = ((com.idega.core.data.ICFileCategoryHome)com.idega.data.IDOLookup.getHomeLegacy(ICFileCategory.class)).createLegacy();
                temp.setName(tempName);
                temp.insert();
              }
              else{//updates
                temp = ((com.idega.core.data.ICFileCategoryHome)com.idega.data.IDOLookup.getHomeLegacy(ICFileCategory.class)).findByPrimaryKeyLegacy(Integer.parseInt(ids[i]));
                if( !temp.getName().equalsIgnoreCase(tempName) ){
                   temp.setName(tempName);
                   temp.update();
                }
              }

            }
          }
        }
        catch (Exception ex) {
          ex.printStackTrace(System.err);
          System.err.println("ImageBusiness : error in storeEditForm");
        }


        //delete
        try {
          if(deleteValue != null){
            for(int i = 0; i < deleteValue.length; i++){
              ICFileCategory cat = ((com.idega.core.data.ICFileCategoryHome)com.idega.data.IDOLookup.getHomeLegacy(ICFileCategory.class)).findByPrimaryKeyLegacy( Integer.parseInt(deleteValue[i]) );
              cat.removeFrom(com.idega.data.GenericEntity.getStaticInstance("com.idega.block.media.data.ImageEntity"));
              cat.delete();
            }
          }
        }
        catch (Exception ex) {
          ex.printStackTrace(System.err);
          System.err.println("ImageBusiness : error in storeEditForm");
        }



//}
    }

  public static int SaveImage(MediaProperties ip){
    int id = -1;
  //  Connection Conn = null;

    try{
      FileInputStream input = new FileInputStream(ip.getRealPath());
      System.out.println("ImageBusiness FileSize:"+input.available());
      ImageEntity image = ((com.idega.block.image.data.ImageEntityHome)com.idega.data.IDOLookup.getHomeLegacy(ImageEntity.class)).createLegacy();
      image.setName(ip.getName());
      /**@todo make this non image specific*/


      image.setMimeType(ip.getMimeType());

      System.out.println("ImageBusiness mimetype:"+ip.getMimeType());

      image.setFileValue(input);
      image.setFileSize((int)ip.getSize());
      image.insert();

      id = image.getID();


/*      String dataBaseType = "";
      Conn = com.idega.data.GenericEntity.getStaticInstance("com.idega.block.media.data.ImageEntity").getConnection();

      if (Conn!=null) dataBaseType = com.idega.data.DatastoreInterface.getDataStoreType(Conn);
      else dataBaseType="oracle";

      if( dataBaseType.equals("oracle") ) {
        id = ImageSave.saveImageToOracleDB(-1,-1,input,ip.getContentType(),ip.getName(),"-1","-1", true);
      }//other databases
      else {
        id = ImageSave.saveImageToDB(-1,-1,input,ip.getContentType(),ip.getName(),"-1","-1", true);
      }

      */
    }
    catch(Exception e){
      e.printStackTrace(System.err);
      ip.setId(-1);
      return -1;
    }
    finally{
     // if(Conn != null ) com.idega.data.GenericEntity.getStaticInstance("com.idega.block.media.data.ImageEntity").freeConnection(Conn);
    }

    return id;
  }

  public static MediaProperties doUpload(IWContext iwc) throws Exception{

    System.err.println("com.idega.block.image.business.ImageBusiness.doUpload(IWContext iwc)");
    System.err.println("Warning: MediaProperties is constructed but with parameterMap = null");
    return new MediaProperties(iwc.getUploadedFile());

//    String sep = FileUtil.getFileSeparator();
//    StringBuffer pathToFile = new StringBuffer();
//    pathToFile.append(iwc.getApplication().getApplicationRealPath());
//    //pathToFile.append(sep);
//    pathToFile.append(IWCacheManager.IW_ROOT_CACHE_DIRECTORY);
//    pathToFile.append(sep);
//
//    FileUtil.createFolder(pathToFile.toString());
//
//
//    MediaProperties  ip = null;
//
//    MultipartParser mp = new MultipartParser(iwc.getRequest(), 10*1024*1024); // 10MB
//    Part part;
//    File dir = null;
//    String value = null;
//    while ((part = mp.readNextPart()) != null) {
//      String name = part.getName();
//      if(part.isParam()){
//        ParamPart paramPart = (ParamPart) part;
//        value = paramPart.getStringValue();
//        //debug
//        System.out.println("Image Business"+name+" : "+value+Text.getBreak());
//      }
//      else if (part.isFile()) {
//        // it's a file part
//        FilePart filePart = (FilePart) part;
//        String fileName = filePart.getFileName();
//
//        if (fileName != null) {
//          pathToFile.append(fileName);
//          String filePath = pathToFile.toString();
//          StringBuffer webPath = new StringBuffer();
//          webPath.append('/');
//          webPath.append(IWCacheManager.IW_ROOT_CACHE_DIRECTORY);
//          webPath.append('/');
//          webPath.append(fileName);
//
//          File file = new File(filePath);
//          int size = (int) filePart.writeTo(file);
//                  //debug
//
//        String mimetype = filePart.getContentType();
//        if(mimetype!=null){
//          StringTokenizer tokenizer = new StringTokenizer(mimetype," ;:");
//          if(tokenizer.hasMoreTokens())
//            mimetype = tokenizer.nextToken();
//        }
//        System.out.println("ImageBusiness : File size"+size);
//        System.out.println("ImageBusiness : File filePath"+filePath);
//        System.out.println("ImageBusiness : File webPath"+webPath.toString());
//        System.out.println("ImageBusiness : File getContentType"+mimetype);
//        System.out.println("ImageBusiness : File fileName"+fileName);
//
//          ip = new MediaProperties(fileName,mimetype,filePath,webPath.toString(),size);
//        }
//      }
//    }
//
//    return ip;
}

public static boolean deleteImageFile(String pathToImage){
    File file = new File(pathToImage);
    return file.delete();
}

public static void setImageDimensions(MediaProperties ip) {
  try{
    /**@todo optimize for memory
     *
     */
    ImageHandler handler =  new ImageHandler(ip.getId());
    handler.updateOriginalInfo();
  }
  catch(Exception e){
   e.printStackTrace(System.err);
   System.err.println("ImageBusiness : setImageDimensions failed!");
  }

}

  public static void handleSaveImage(IWContext iwc){
    MediaProperties ip = (MediaProperties) iwc.getSessionAttribute("im_ip");
    String submit = iwc.getParameter("submit");
    String categoryId = iwc.getParameter("category_id");

    if( (ip!=null) && !("cancel".equalsIgnoreCase(submit)) ){
      int imageId = SaveImage(ip);
      ip.setId(imageId);

      setImageDimensions(ip);//adds width height and size in bytes to database
      makeDefaultSizes(iwc);

      try{
        ImageEntity image = ((com.idega.block.image.data.ImageEntityHome)com.idega.data.IDOLookup.getHomeLegacy(ImageEntity.class)).findByPrimaryKeyLegacy(imageId);
        ICFileCategory cat = ((com.idega.core.data.ICFileCategoryHome)com.idega.data.IDOLookup.getHomeLegacy(ICFileCategory.class)).findByPrimaryKeyLegacy(Integer.parseInt(categoryId));
        cat.addTo(image);
      }
      catch(SQLException e){
        e.printStackTrace(System.err);
        System.err.println("ImageBusiness : failed to add to image_image_category");
      }

      iwc.setSessionAttribute("im_image_id",Integer.toString(imageId));
      deleteImageFile(ip.getRealPath());
      iwc.removeSessionAttribute("im_ip");
      iwc.setSessionAttribute("refresh",new String("true"));

    }
    else {
      System.err.println("Image save failed or was cancelled!");
    }
  }

  public static void handleTextSave(IWContext iwc) throws Exception{
    String submit = iwc.getParameter("submit");
    if( !"cancel".equalsIgnoreCase(submit) ){
      boolean update = true;
      String imageId = iwc.getParameter("image_id");
      String imageText = iwc.getParameter("image_text");
      String imageLink = iwc.getParameter("image_link");
      ImageEntity image = ((com.idega.block.image.data.ImageEntityHome)com.idega.data.IDOLookup.getHomeLegacy(ImageEntity.class)).findByPrimaryKeyLegacy(Integer.parseInt(imageId));

      if( imageText!=null ) image.setDescription(imageText);
      else update = false;

      if( (imageLink!=null) && !"".equals(imageLink) ){
        image.setImageLink(imageLink);
        image.setImageLinkOwner("both");
      }
      else update = false;

      if(update){
        image.update();
        iwc.setSessionAttribute("im_refresh",new String("true"));
      }
    }
  }
}//end of class

