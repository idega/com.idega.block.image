package com.idega.block.image;

import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWBundleStartable;
import com.idega.idegaweb.include.GlobalIncludeManager;

/**
 * 
 * 
 *  Last modified: $Date: 2006/03/27 15:07:52 $ by $Author: eiki $
 * 
 * @author <a href="mailto:eiki@idega.com">eiki</a>
 * @version $Revision: 1.2 $
 */
public class IWBundleStarter implements IWBundleStartable {

	public void start(IWBundle starterBundle) {
		GlobalIncludeManager.getInstance().addStyleSheet(starterBundle.getResourcesPath()+"/style/image.css");
		//todo register auto image resizer
//		   try {
//	            IWSlideService service = (IWSlideService) IBOLookup.getServiceInstance(iwac,IWSlideService.class);
//	           
//	            
//	            //add it as a slide change listener for caching purposes
//	            service.addIWSlideChangeListeners(service);
//	            
//	        } catch (IBOLookupException e) {
//	            e.printStackTrace();
//	        } catch (RemoteException e) {
//	            e.printStackTrace();
//	        }
//		
//		
//		
	}
	
	public void stop(IWBundle starterBundle) {
		// nothing to do
	}
}
