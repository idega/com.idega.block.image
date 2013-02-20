package com.idega.block.image.business;

import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.ejb.CreateException;
import javax.ejb.TransactionRolledbackLocalException;
import javax.jcr.RepositoryException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import com.idega.block.image.data.ImageEntity;
import com.idega.block.image.data.ImageEntityHome;
import com.idega.block.image.presentation.AdvancedImage;
import com.idega.business.IBOServiceBean;
import com.idega.data.IDOLookup;
import com.idega.util.ListUtil;

/**
 * *
 *
 * Title: idegaWeb Description: This class is mainly used by the ImageGallery
 * class to get, to store and to change stored images.
 *
 * Copyright: Copyright (c) 2003-2006 Company: idega software
 *
 * @author <a href="mailto:thomas@idega.is">Thomas Hilbig</a>,<a href="mailto:eiki@idega.is">Eirikur S. Hrafnsson</a>
 * @version 2.0
 */
public class ImageProviderBean extends IBOServiceBean implements ImageProvider {

	private static final long serialVersionUID = 8916867832320355561L;

	public ImageProviderBean() {
	}

	@Override
	public int getImageCount(String imageFolderResourcePath) {
		try {
			return getRepositoryService().getChildCountExcludingFoldersAndHiddenFiles(imageFolderResourcePath);
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return 0;
	}

	@Override
	public List <AdvancedImage>getImagesFromTo(String imageFolderResourcePath, int startPosition, int endPosition) {
		List<AdvancedImage> results = new ArrayList<AdvancedImage>();
		if (imageFolderResourcePath == null) {
			return results;
		} else {
			try {
				List<String> imagePaths = getRepositoryService().getChildPathsExcludingFoldersAndHiddenFiles(imageFolderResourcePath);
				Collections.sort(imagePaths);

				if(imagePaths!=null){
					int realEnd = Math.min(endPosition, imagePaths.size());
					for (int i = (startPosition -1);i < realEnd; i++) {
						AdvancedImage image = new AdvancedImage(getRepositoryService().getRepositoryItemAsRootUser(imagePaths.get(i)));
						results.add(image);
					}

					return results;
				}
			} catch (RepositoryException e) {
				e.printStackTrace();
			}
		}

		return results;
	}

	/**
	 *
	 * @param folderURI
	 * @return An array of resource paths to images within the supplied folder uri (in repository)
	 * @throws RemoteException
	 */
	@Override
	public String[] getAllImagePathsForFolder(String folderURI) throws RemoteException{
		try {
			List<String> imagePaths = getRepositoryService().getChildPathsExcludingFoldersAndHiddenFiles(folderURI);

			if(imagePaths!=null){
				return imagePaths.toArray(new String[0]);
			}
		}
		catch (RepositoryException e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override
	public List<String> getContainedFolders(String imageFolderResourcePath) {
		List<String> folders = null;
		if (imageFolderResourcePath == null) {
			return folders;
		}
		else{
			try {
				folders = getRepositoryService().getChildFolderPaths(imageFolderResourcePath);
			} catch (RepositoryException e) {
				e.printStackTrace();
			}
		}

		if(folders!=null){
			return folders;
		}
		else{
			return ListUtil.getEmptyList();
		}
	}

	@Override
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

	@Override
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

