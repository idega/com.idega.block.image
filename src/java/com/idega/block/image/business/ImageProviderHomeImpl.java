package com.idega.block.image.business;


public class ImageProviderHomeImpl extends com.idega.business.IBOHomeImpl implements ImageProviderHome
{
 protected Class getBeanInterfaceClass(){
  return ImageProvider.class;
 }


 public ImageProvider create() throws javax.ejb.CreateException{
  return (ImageProvider) super.createIBO();
 }



}