package com.idega.block.image.business;

import javax.ejb.*;

public interface ImageProvider extends com.idega.business.IBOService
{
 public java.util.ArrayList getImagesFromTo(com.idega.core.data.ICFile p0,int p1,int p2) throws java.rmi.RemoteException, java.sql.SQLException;
 public int getImageCount(com.idega.core.data.ICFile p0) throws java.rmi.RemoteException;
}
