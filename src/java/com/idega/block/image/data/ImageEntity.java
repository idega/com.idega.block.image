package com.idega.block.image.data;

import com.idega.data.IDOLegacyEntity;


public interface ImageEntity extends com.idega.core.file.data.ICFile,IDOLegacyEntity
{
 public java.lang.String getHeight();
 public java.lang.String getImageLink();
 public java.lang.String getImageLinkOwner();
 public java.lang.String getLink();
 public int getParentId();
 public java.lang.String getWidth();
 public void setDefaultValues();
 public void setHeight(java.lang.String p0);
 public void setImageLink(java.lang.String p0);
 public void setImageLinkOwner(java.lang.String p0);
 public void setWidth(java.lang.String p0);
}
