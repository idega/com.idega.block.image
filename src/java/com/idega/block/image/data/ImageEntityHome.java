package com.idega.block.image.data;


public interface ImageEntityHome extends com.idega.data.IDOHome
{
 public ImageEntity create() throws javax.ejb.CreateException;
 public ImageEntity createLegacy();
 public ImageEntity findByPrimaryKey(int id) throws javax.ejb.FinderException;
 public ImageEntity findByPrimaryKey(Object pk) throws javax.ejb.FinderException;
 public ImageEntity findByPrimaryKeyLegacy(int id) throws java.sql.SQLException;

}