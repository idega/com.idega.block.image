package com.idega.block.image.data;


public class ImageEntityHomeImpl extends com.idega.data.IDOFactory implements ImageEntityHome
{
 protected Class getEntityInterfaceClass(){
  return ImageEntity.class;
 }

 public ImageEntity create() throws javax.ejb.CreateException{
  return (ImageEntity) super.idoCreate();
 }

 public ImageEntity createLegacy(){
	try{
		return create();
	}
	catch(javax.ejb.CreateException ce){
		throw new RuntimeException("CreateException:"+ce.getMessage());
	}

 }

 public ImageEntity findByPrimaryKey(int id) throws javax.ejb.FinderException{
  return (ImageEntity) super.idoFindByPrimaryKey(id);
 }

 public ImageEntity findByPrimaryKey(Object pk) throws javax.ejb.FinderException{
  return (ImageEntity) super.idoFindByPrimaryKey(pk);
 }

 public ImageEntity findByPrimaryKeyLegacy(int id) throws java.sql.SQLException{
	try{
		return findByPrimaryKey(id);
	}
	catch(javax.ejb.FinderException fe){
		throw new java.sql.SQLException(fe.getMessage());
	}

 }


}