package com.idega.block.image.business;


public interface ImageEncoderHome extends com.idega.business.IBOHome
{
 public ImageEncoder create() throws javax.ejb.CreateException, java.rmi.RemoteException;

}