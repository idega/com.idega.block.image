package com.idega.block.image.business;


public class ImageEncoderHomeImpl extends com.idega.business.IBOHomeImpl implements ImageEncoderHome
{
 protected Class getBeanInterfaceClass(){
  return ImageEncoder.class;
 }


 public ImageEncoder create() throws javax.ejb.CreateException{
  return (ImageEncoder) super.createIBO();
 }



}