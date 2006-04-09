package com.idega.block.image.presentation;

import com.idega.presentation.IWContext;
import com.idega.presentation.PresentationObject;

/**
 *  
 * Title:         idegaWeb
 * Description:   To print the original image of an AdvancedImage this wrapper can be used.
 *                  
 * Copyright:     Copyright (c) 2003
 * Company:       idega software
 * @author <a href="mailto:thomas@idega.is">Thomas Hilbig</a>
 * @version 1.0
 */
public class AdvancedImageWrapper extends PresentationObject  {
  
  private AdvancedImage image;
  
  public AdvancedImageWrapper(AdvancedImage image)  {
    this.image = image;
  }  
  
  public void main(IWContext iwc) {
    this.image.main(iwc);
  }
  
  public void _main(IWContext iwc) throws Exception {
    this.image._main(iwc);
  }
  
  public void print(IWContext iwc)throws Exception{
    this.image.printOriginalImage(iwc);
  }
  
  public void _print(IWContext iwc) throws Exception {
    this.image._print(iwc);
  }
}
