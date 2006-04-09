package com.idega.block.image.presentation;

import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.idegaweb.presentation.IWAdminWindow;
import com.idega.presentation.IWContext;

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
    private IWResourceBundle iwrb;
    private String IW_BUNDLE_IDENTIFIER="com.idega.block.image";

    public ImageAttributeWindow(){
      super();
      setResizable(true);
      setWidth(260);
      setHeight(320);
    }

    public String getBundleIdentifier(){
      return this.IW_BUNDLE_IDENTIFIER;
    }

    public void  main(IWContext iwc) throws Exception{
      this.iwb = getBundle(iwc);
      this.iwrb = this.iwb.getResourceBundle(iwc);

      ImageAttributeSetter SC = new ImageAttributeSetter();
      add(SC);
      String title = this.iwrb.getLocalizedString("im_image_attributes","Image Attributes");
      setTitle(title);
      addTitle(title);

      //setParentToReload();
    }
}
