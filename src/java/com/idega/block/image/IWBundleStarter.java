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

	@Override
	public void start(IWBundle starterBundle) {
		//TODO register auto image resizer
	}

	@Override
	public void stop(IWBundle starterBundle) {
		// nothing to do
	}
}
