package com.idega.block.image.presentation;

import com.idega.presentation.text.*;
import com.idega.presentation.*;
import com.idega.presentation.ui.*;
import com.idega.block.media.presentation.*;

public class ImageEditor extends Block{
private boolean refresh = false;
private boolean showAll = true;

  public void main(IWContext iwc)throws Exception{
    String refreshing = (String) iwc.getSessionAttribute("refresh");
    String sRefresh = iwc.getParameter("refresh");
    ImageBrowser browser = new ImageBrowser();
    browser.setShowAll(showAll);

    if( (sRefresh!=null) || refresh || (refreshing!=null) ) browser.refresh();

    add(browser);
  }

  public void refresh(){
    this.refresh=true;
  }

  public void setShowAll(boolean showAll){
    this.showAll = showAll;
  }
}
