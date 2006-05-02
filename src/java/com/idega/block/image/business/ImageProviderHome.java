/*
 * $Id: ImageProviderHome.java 1.1 May 2, 2006 eiki Exp $
 * Created on May 2, 2006
 *
 * Copyright (C) 2006 Idega Software hf. All Rights Reserved.
 *
 * This software is the proprietary information of Idega hf.
 * Use is subject to license terms.
 */
package com.idega.block.image.business;

import com.idega.business.IBOHome;


/**
 * 
 *  Last modified: $Date: 2004/06/28 09:09:50 $ by $Author: eiki $
 * 
 * @author <a href="mailto:eiki@idega.com">eiki</a>
 * @version $Revision: 1.1 $
 */
public interface ImageProviderHome extends IBOHome {

	public ImageProvider create() throws javax.ejb.CreateException, java.rmi.RemoteException;
}
