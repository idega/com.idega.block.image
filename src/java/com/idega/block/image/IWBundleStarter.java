package com.idega.block.image;

import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWBundleStartable;

/**
 * 
 * 
 *  Last modified: $Date: 2008/10/20 11:54:30 $ by $Author: laddi $
 * 
 * @author <a href="mailto:eiki@idega.com">eiki</a>
 * @version $Revision: 1.3 $
 */
public class IWBundleStarter implements IWBundleStartable {

	public void start(IWBundle starterBundle) {
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
