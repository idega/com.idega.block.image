package com.idega.block.image.business;

import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.ejb.CreateException;
import javax.ejb.TransactionRolledbackLocalException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.apache.commons.httpclient.HttpException;

import com.idega.block.image.data.ImageEntity;
import com.idega.block.image.data.ImageEntityHome;
import com.idega.block.image.presentation.AdvancedImage;
import com.idega.business.IBOLookup;
import com.idega.business.IBOLookupException;
import com.idega.business.IBOServiceBean;
import com.idega.data.IDOLookup;
import com.idega.slide.business.IWSlideService;
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

	public ImageProviderBean() {
	}
	
	public int getImageCount(String imageFolderResourcePath) {
		try {
			IWSlideService service = getIWSlideService();
			return service.getChildCountExcludingFoldersAndHiddenFiles(imageFolderResourcePath);
		}
		catch (IBOLookupException e) {
			e.printStackTrace();
		}
		catch (RemoteException e) {
			e.printStackTrace();
		}

		return 0;
	}
	
	public ArrayList getImagesFromTo(String imageFolderResourcePath, int startPosition, int endPosition) {
		if (imageFolderResourcePath == null) {
			return new ArrayList();
		}
		else{
			try {
				IWSlideService service = getIWSlideService();
				Set result = new TreeSet();
				List imagePaths = service.getChildPathsExcludingFoldersAndHiddenFiles(imageFolderResourcePath);
				Collections.sort(imagePaths);
				
				if(imagePaths!=null){
					int realEnd = Math.min(endPosition, imagePaths.size());
					for (int i = (startPosition -1);i < realEnd; i++) {
						AdvancedImage image = new AdvancedImage(service.getWebdavResourceAuthenticatedAsRoot((String) imagePaths.get(i)));
						result.add(image);
					}
						
					return new ArrayList(result);
				}
			}
			catch (IBOLookupException e) {
				e.printStackTrace();
			}
			catch (RemoteException e) {
				e.printStackTrace();
			}
			catch (HttpException e) {
				e.printStackTrace();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
			
		return new ArrayList();
	}
	
	/**
	 * 
	 * @param folderURI
	 * @return An array of resource paths to images within the supplied folder uri (in slide)
	 * @throws RemoteException 
	 */
	public String[] getAllImagePathsForFolder(String folderURI) throws RemoteException{
		
		IWSlideService service;
		try {
			service = getIWSlideService();
		
			List imagePaths = service.getChildPathsExcludingFoldersAndHiddenFiles(folderURI);
			
			if(imagePaths!=null){
				return (String[]) imagePaths.toArray(new String[0]);
			}
		}
		catch (IBOLookupException e) {
			e.printStackTrace();
		}
	
		return null;
	}

	/**
	 * @return
	 * @throws IBOLookupException
	 */
	public IWSlideService getIWSlideService() throws IBOLookupException {
		IWSlideService service = (IWSlideService) IBOLookup.getServiceInstance(getIWApplicationContext(), IWSlideService.class);
		return service;
	}

	
	public List getContainedFolders(String imageFolderResourcePath) {
		List folders = null;
		if (imageFolderResourcePath == null) {
			return folders;
		}
		else{
			try {
				IWSlideService service = getIWSlideService();
				folders = service.getChildFolderPaths(imageFolderResourcePath);
			}
			catch (IBOLookupException e) {
				e.printStackTrace();
			}
			catch (RemoteException e) {
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

