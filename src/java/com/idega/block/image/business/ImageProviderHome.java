/*
 * $Id: ImageProviderHome.java 1.1 Feb 16, 2006 gimmi Exp $
 * Created on Feb 16, 2006
 *
 * Copyright (C) 2006 Idega Software hf. All Rights Reserved.
 *
 * This software is the proprietary information of Idega hf.
 * Use is subject to license terms.
 */
package com.idega.block.image.business;

import com.idega.business.IBOHome;


/**
 * <p>
 * TODO gimmi Describe Type ImageProviderHome
 * </p>
 *  Last modified: $Date: 2004/06/28 09:09:50 $ by $Author: gimmi $
 * 
 * @author <a href="mailto:gimmi@idega.com">gimmi</a>
 * @version $Revision: 1.1 $
 */
public interface ImageProviderHome extends IBOHome {

	public ImageProvider create() throws javax.ejb.CreateException, java.rmi.RemoteException;
}
