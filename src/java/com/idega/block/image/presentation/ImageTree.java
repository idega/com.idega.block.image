package com.idega.block.image.presentation;



import java.sql.*;

import java.util.*;

import com.idega.util.*;

import com.idega.presentation.text.*;

import	com.idega.presentation.*;

import	com.idega.presentation.ui.*;

import	com.idega.block.image.data.*;

import	com.idega.data.*;

import com.idega.util.text.*;

import com.idega.core.data.ICFileCategory;







public class ImageTree extends Block{



private String width = "100%";

private boolean showAll = false;

private boolean refresh = false;



public Table getTreeTable(IWContext iwc) throws SQLException {



    ICFileCategory[] catagory = (ICFileCategory[]) (((com.idega.core.data.ICFileCategoryHome)com.idega.data.IDOLookup.getHomeLegacy(ICFileCategory.class)).createLegacy()).findAll();

    ImageEntity[] images;

    Vector items = null;



    items = (Vector) iwc.getServletContext().getAttribute("image_tree_vector");



    Integer[] intArr = new Integer[3];

    int pos;



    Table returnTable = new Table();



    if (items == null) {

      items = new Vector();

      if ( catagory != null) {

        if (catagory.length > 0) {

          for (int i = 0 ; i < catagory.length ; i++ ) {

            findNodes(items,catagory[i].getID(),1,com.idega.data.GenericEntity.getStaticInstance("com.idega.core.data.ICFileCategory"),1);



            if ( showAll ) {

              images = (ImageEntity[])catagory[i].findRelated( com.idega.data.GenericEntity.getStaticInstance("com.idega.block.media.data.ImageEntity") );



              if (images != null) {

                if (images.length > 0 ) {

                    intArr = (Integer[])(items.lastElement());

                    pos = intArr[1].intValue()+1;

                  for (int j = 0 ; j < images.length ; j++) {

                    if (images[j].getParentId()== -1 ) {

                      findNodes(items,images[j].getID(),pos,com.idega.data.GenericEntity.getStaticInstance("com.idega.block.media.data.ImageEntity"),2);

                    }

                  }

                }

              }

            }



          iwc.getServletContext().setAttribute("image_tree_vector",items);

          }

        }

      }

    }





    if (items.size() > 0) {

      String openCat = iwc.getParameter("open_catagory_id");



      if (openCat == null) { openCat = "-3";}

        Table isTable = (Table) iwc.getServletContext().getAttribute("image_tree_table"+openCat);



        if (isTable != null) {

          returnTable = isTable;

        }

        else {

          returnTable = writeTable(items,iwc);

        }

    }



    return returnTable;

}



public String getWidth(){

  return this.width;

}



public void setWidth(String width){

  this.width =  width;

}



public void setShowAll(boolean showAll){

  this.showAll =  showAll;

}



public Table writeTable(Vector items,IWContext iwc) throws SQLException {

  Table table = new Table();

    table.setBorder(0);

    table.setWidth(getWidth());

    table.setCellpadding(2);

    table.setCellspacing(0);

    table.setAlignment("left");



  Text more = new Text("+");

    more.setFontColor("#FFFFFF");

  String imageId = iwc.getParameter("image_id");

  String openCat = iwc.getParameter("open_catagory_id");

    if (openCat == null) { openCat = "-3";}

  String openImg = iwc.getParameter("open_image_id");

    if (openImg == null) { openImg = "-3";}



  Link openLink;

  Link idLink;

  String color0 = "/pics/jmodules/image/myndamodule/menubar/yfirfl1.gif";

  String color1 = "/pics/jmodules/image/myndamodule/menubar/undirfl1.gif";

  String color2 = "/pics/jmodules/image/myndamodule/menubar/undirfl2.gif";

  int depth = 10;



  Text text;



  ICFileCategory catagory;

  ImageEntity image;

  Integer[] intArr = new Integer[3];

  int pos = 1;

  int id;

  int spe;

  int row = 0;

  int preCatId = -1;



  for (int i = 0 ; i < items.size() ; i++) {

      intArr = (Integer[]) items.elementAt(i);

      id = intArr[0].intValue();

      pos= intArr[1].intValue();

      spe= intArr[2].intValue();

      if (spe == 1) {

        ++row;

        catagory = ((com.idega.core.data.ICFileCategoryHome)com.idega.data.IDOLookup.getHomeLegacy(ICFileCategory.class)).findByPrimaryKeyLegacy(id);

        preCatId = id;



        table.mergeCells(1,row,depth,row);



        text = new Text(catagory.getName());

          text.setFontColor("#FFFFFF");



        openLink = new Link(more);

        openLink.setFontColor("#FFFFFF");

        openLink.setAttribute("style","text-decoration:none");



        idLink = new Link(text);

        idLink.setFontColor("#FFFFFF");

        idLink.setBold();

        idLink.setAttribute("style","text-decoration:none");



        if (!openCat.equals(Integer.toString(id))) {

          openLink.addParameter("open_catagory_id",""+id);



        }

        else {

          idLink.addParameter("open_catagory_id",""+id);

        }



          idLink.addParameter("image_catagory_id",""+id);

        table.setHeight(row,"25");

        if ( showAll ) {

          table.add(openLink,pos,row);

        }

        table.addText("&nbsp;",pos,row);

        table.add(idLink,pos,row);

        table.setBackgroundImage(pos,row,new Image(color0));

      }



      if (openCat.equals(Integer.toString(preCatId)))

      if (spe == 2) {

        ++row;

        image = ((com.idega.block.image.data.ImageEntityHome)com.idega.data.IDOLookup.getHomeLegacy(ImageEntity.class)).findByPrimaryKeyLegacy(id);



        StringBuffer extrainfo = new StringBuffer("");

        extrainfo.append("&nbsp;");

        extrainfo.append(image.getName());



        if ( ( image.getWidth()!=null)&& ( image.getHeight()!=null) ){

          extrainfo.append(" (");

          extrainfo.append(image.getWidth());

          extrainfo.append("*");

          extrainfo.append(image.getHeight());

          extrainfo.append(")");

        }



        text = new Text(extrainfo.toString());

        text.setFontSize(1);



        idLink = new Link(text);

        idLink.setFontColor("#FFFFFF");

        idLink.setAttribute("style","text-decoration:none");

        if (preCatId != -1 ) {

          idLink.addParameter("open_catagory_id",""+preCatId);

        }



        table.mergeCells(pos,row,depth,row);

        table.setHeight(row,"21");



        if ( pos == 2 ) {

          table.setBackgroundImage(pos,row,new Image(color1));

          table.setBackgroundImage(1,row,new Image(color1));

          table.addText("",1,row);

        }

        else {

          table.setBackgroundImage(pos,row,new Image(color2));

          for ( int a = 1; a < pos; a++ ) {

            table.setBackgroundImage(a,row,new Image(color2));

            table.addText("",a,row);

          }

        }



          idLink.addParameter("image_id",""+id);



        table.add(idLink, pos,row);

      }





  }



  iwc.getServletContext().setAttribute("image_tree_table"+openCat,table);



  return table;

//  add(table);

}







    private void findNodes(Vector vector,int id, int position,IDOLegacyEntity entity, IDOLegacyEntity[] options, int specialValue) throws SQLException{

        Integer[] intArray = new Integer[3];

          intArray[0] = new Integer(id);

          intArray[1] = new Integer(position);

          intArray[2] = new Integer(specialValue);



        vector.addElement(intArray);



       options = (IDOLegacyEntity[]) (entity).findAllByColumn("parent_id",""+id);

        int i = 0;



        if (options != null ) {

          if (options.length > 0) {

            ++position;

            for (i = 0 ; i < options.length ; i++) {

              findNodes(vector,options[i].getID(), position,entity,options, specialValue);

            }

          }

        }





    }



    private void findNodes(Vector vector,int id, int position,IDOLegacyEntity entity) throws SQLException{

        findNodes(vector,id,position,entity,new IDOLegacyEntity[1],0);

    }



    private void findNodes(Vector vector,int id, int position,IDOLegacyEntity entity, int specialValue) throws SQLException{

        findNodes(vector,id,position,entity,new IDOLegacyEntity[1], specialValue);

    }





private void refresh(IWContext iwc) throws SQLException{

    Table table;

    Vector vector;

    String test;



    table = (Table) iwc.getServletContext().getAttribute("image_tree_table-3");

    vector = (Vector) iwc.getServletContext().getAttribute("image_tree_vector");

    if (table != null) {

      iwc.getServletContext().removeAttribute("image_tree_table-3");

    }

    if (vector != null) {

      iwc.getServletContext().removeAttribute("image_tree_vector");

    }



    test = (String) iwc.getSessionAttribute("image_tree_catagory_id");

    if (test != null) {

      iwc.removeSessionAttribute("image_tree_catagory_id");

    }

    test = (String) iwc.getSessionAttribute("image_tree_image_id");

    if (test != null) {

      iwc.removeSessionAttribute("image_tree_image_id");

    }





        ICFileCategory[] catagories = (ICFileCategory[])(((com.idega.core.data.ICFileCategoryHome)com.idega.data.IDOLookup.getHomeLegacy(ICFileCategory.class)).createLegacy()).findAll();



        if (catagories != null) {

            if (catagories.length > 0 ) {

                for (int i = 0 ; i < catagories.length ; i++ ) {

                    table = (Table) iwc.getServletContext().getAttribute("image_tree_table"+catagories[i].getID());

                    if (table != null) {

                        iwc.getServletContext().removeAttribute("image_tree_table"+catagories[i].getID());

                    }

                }

            }

        }

    }





public void refresh(){

  this.refresh=true;

}



public void main(IWContext iwc)throws Exception{

  //this.isAdmin=this.isAdministrator(iwc);

  //setSpokenLanguage(iwc);



  if(refresh) refresh(iwc);



  String tempImageId = iwc.getParameter("image_id");

  String tempCatagoryId = iwc.getParameter("catagory_id");

  String imageId = null;

  String catagoryId = null;



  if (tempImageId != null) {

     iwc.setSessionAttribute("image_tree_image_id",tempImageId);

     iwc.removeSessionAttribute("image_tree_catagory_id");

  }

  if (tempCatagoryId != null) {

     iwc.setSessionAttribute("image_tree_catagory_id",tempCatagoryId);

     iwc.removeSessionAttribute("image_tree_image_id");

  }

     imageId = (String) iwc.getSessionAttribute("image_tree_image_id");

     catagoryId = (String) iwc.getSessionAttribute("image_tree_catagory_id");



  add(getTreeTable(iwc));



  }



}

