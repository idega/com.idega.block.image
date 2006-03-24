package com.idega.block.image;

import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWBundleStartable;
import com.idega.idegaweb.include.GlobalIncludeManager;

/**
 * 
 * 
 *  Last modified: $Date: 2006/03/24 16:46:18 $ by $Author: eiki $
 * 
 * @author <a href="mailto:eiki@idega.com">eiki</a>
 * @version $Revision: 1.1 $
 */
public class IWBundleStarter implements IWBundleStartable {

	public void start(IWBundle starterBundle) {
		GlobalIncludeManager.getInstance().addStyleSheet(starterBundle.getResourcesPath()+"/style/image.css");
	}
	
	public void stop(IWBundle starterBundle) {
		// nothing to do
	}
}
