package com.idega.block.image.business;


public interface ImageProviderHome extends com.idega.business.IBOHome
{
 public ImageProvider create() throws javax.ejb.CreateException, java.rmi.RemoteException;

}