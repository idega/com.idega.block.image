package com.idega.block.image.presentation;

import com.idega.idegaweb.presentation.IWAdminWindow;
import com.idega.block.media.business.SimpleImage;
import com.idega.presentation.*;
import com.idega.presentation.text.*;
import com.idega.presentation.ui.*;
import com.idega.block.media.data.ImageEntity;
import com.idega.util.idegaTimestamp;
import com.idega.idegaweb.IWBundle;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2000-2001 idega.is All Rights Reserved
 * Company:      idega
  *@author <a href="mailto:aron@idega.is">Aron Birkir</a>
 * @version 1.1
 */

 public class ImageAttributeWindow extends IWAdminWindow {
    private IWBundle iwb;
    private String IW_BUNDLE_IDENTIFIER="com.idega.block.image";

    public ImageAttributeWindow(){
      super();
      setResizable(true);
      setWidth(300);
      setHeight(300);
    }

    public String getBundleIdentifier(){
      return IW_BUNDLE_IDENTIFIER;
    }

    public void  main(IWContext iwc) throws Exception{
      iwb = getBundle(iwc);
      ImageAttributeSetter SC = new ImageAttributeSetter();
      add(SC);
      setTitle("Image Attributes");
      addTitle("Image Attributes" );

      //setParentToReload();
    }
}