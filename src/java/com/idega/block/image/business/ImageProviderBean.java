package com.idega.block.image.business;

import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.ejb.CreateException;
import javax.ejb.FinderException;
import javax.ejb.TransactionRolledbackLocalException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import com.idega.block.image.data.ImageEntity;
import com.idega.block.image.data.ImageEntityHome;
import com.idega.block.image.presentation.AdvancedImage;
import com.idega.business.IBOServiceBean;
import com.idega.core.file.data.ICFile;
import com.idega.core.file.data.ICFileHome;
import com.idega.data.IDOLookup;
import com.idega.data.IDOLookupException;

/**
 * *
 * 
 * Title: idegaWeb Description: This class is mainly used by the ImageGallery
 * class to get, to store and to change stored images.
 * 
 * Copyright: Copyright (c) 2003 Company: idega software
 * 
 * @author <a href="mailto:thomas@idega.is">Thomas Hilbig </a>
 * @version 1.0
 */
public class ImageProviderBean extends IBOServiceBean implements ImageProvider {

	public ImageProviderBean() {
	}

	public int getImageCount(ICFile imageFolder) {
		if (imageFolder == null) {
			return 0;
		}
		return imageFolder.getChildCount();
	}

	public ArrayList getImagesFromTo(ICFile imageFolder, int startPosition, int endPosition) throws SQLException {
		if (imageFolder == null || (startPosition < 1) || (startPosition > endPosition)) {
			return new ArrayList();
		}

		int length = endPosition - startPosition + 1;
		ArrayList result = new ArrayList(length);

		try {
			ICFileHome fileHome = (ICFileHome) IDOLookup.getHome(ICFile.class);
			Collection images = fileHome.findChildren(imageFolder, null, null, "name", startPosition-1, length);
			Iterator iterator = images.iterator();
			AdvancedImage child;
			while (iterator.hasNext()) {
				ICFile imageFile = ((ICFile) iterator.next());
				child = new AdvancedImage(((Integer) imageFile.getPrimaryKey()).intValue(), imageFile.getName());
				result.add(child);
			}
		}
		catch (IDOLookupException ile) {
			throw new SQLException(ile.getMessage());
		}
		catch (FinderException fe) {
			throw new SQLException(fe.getMessage());
		}
		return result;
	}

	public int uploadImage(InputStream inputStream, String mimeType, String name, int width, int height,
			ImageEntity parent) throws CreateException {

		UserTransaction trans = null;
		int id = -1;
		try {
			trans = this.getSessionContext().getUserTransaction();
			trans.begin();
			// create new image entity
			ImageEntityHome imageEntityHome = (ImageEntityHome) IDOLookup.getHome(ImageEntity.class);
			ImageEntity imageEntity;
			// create throws CreateException
			imageEntity = imageEntityHome.create();
			// store value of File
			imageEntity.setFileValue(inputStream);
			imageEntity.setMimeType(mimeType);
			imageEntity.setName(name);
			imageEntity.setWidth(Integer.toString(width));
			imageEntity.setHeight(Integer.toString(height));
			// store throws RuntimeException
			imageEntity.store();
			// set new entity as a child
			// addChild throws SQLException
			parent.addChild(imageEntity);
			id = ((Integer) imageEntity.getPrimaryKey()).intValue();
			trans.commit();
		}
		catch (Exception e) {
			System.err.println(e.getMessage());
			//      e.printStackTrace();
			if (trans != null) {
				try {
					trans.rollback();
				}
				catch (SystemException se) {
					se.printStackTrace();
				}
			}
			throw new CreateException("There was an error storing the modified image. Message was: " + e.getMessage());
		}
		return id;
	}

	public synchronized void setHeightAndWidthOfOriginalImageToEntity(int width, int height, ImageEntity imageEntity)
			throws TransactionRolledbackLocalException {
		UserTransaction trans = null;
		try {
			trans = this.getSessionContext().getUserTransaction();
			trans.begin();
			imageEntity.setWidth(Integer.toString(width));
			imageEntity.setHeight(Integer.toString(height));
			imageEntity.store();
			trans.commit();
		}
		catch (Exception e) {
			System.err.println(e.getMessage());
			//      e.printStackTrace();
			if (trans != null) {
				try {
					trans.rollback();
				}
				catch (SystemException se) {
					se.printStackTrace();
				}
			}
			throw new TransactionRolledbackLocalException(
					"There was an error storing height and width of the image. Message was: " + e.getMessage());
		}
	}

}

