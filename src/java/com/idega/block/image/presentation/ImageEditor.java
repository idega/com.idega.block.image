package com.idega.block.image.presentation;



import com.idega.presentation.Block;
import com.idega.presentation.IWContext;



public class ImageEditor extends Block{

private boolean refresh = false;

private boolean showAll = true;



  public void main(IWContext iwc)throws Exception{

    String refreshing = (String) iwc.getSessionAttribute("refresh");

    String sRefresh = iwc.getParameter("refresh");

    ImageBrowser browser = new ImageBrowser();

    browser.setShowAll(this.showAll);



    if( (sRefresh!=null) || this.refresh || (refreshing!=null) ) {
			browser.refresh();
		}



    add(browser);

  }



  public void refresh(){

    this.refresh=true;

  }



  public void setShowAll(boolean showAll){

    this.showAll = showAll;

  }

}

