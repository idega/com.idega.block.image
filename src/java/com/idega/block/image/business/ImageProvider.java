package com.idega.block.image.business;


public interface ImageProvider extends com.idega.business.IBOService
{
 public int getImageCount(com.idega.core.file.data.ICFile p0) throws java.rmi.RemoteException;
 public java.util.ArrayList getImagesFromTo(com.idega.core.file.data.ICFile p0,int p1,int p2)throws java.sql.SQLException, java.rmi.RemoteException;
 public int uploadImage(java.io.InputStream p0,java.lang.String p1, java.lang.String p2,int width, int height, com.idega.block.image.data.ImageEntity p3)throws javax.ejb.CreateException, java.rmi.RemoteException;
 public void setHeightAndWidthOfOriginalImageToEntity(int width, int height, com.idega.block.image.data.ImageEntity imageEntity) throws javax.ejb.TransactionRolledbackLocalException;
}
