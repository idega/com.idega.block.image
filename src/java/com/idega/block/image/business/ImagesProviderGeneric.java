package com.idega.block.image.business;

import java.util.List;

import com.idega.block.image.presentation.AdvancedImage;

/**
 * this is more generic provider interface than ImageProvider
 * 
 * @author <a href="mailto:civilis@idega.com">Vytautas Čivilis</a>
 * @version $Revision: 1.1 $ Last modified: $Date: 2009/03/30 13:15:35 $ by $Author: civilis $
 */
public interface ImagesProviderGeneric {
	
	public abstract int getImageCount();
	
	public abstract List<AdvancedImage> getImagesFromTo(int startPosition,
	        int endPosition);
}