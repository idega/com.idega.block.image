/**
 * @(#)ImagesService.java    1.0.0 10:02:28
 *
 * Idega Software hf. Source Code Licence Agreement x
 *
 * This agreement, made this 10th of February 2006 by and between 
 * Idega Software hf., a business formed and operating under laws 
 * of Iceland, having its principal place of business in Reykjavik, 
 * Iceland, hereinafter after referred to as "Manufacturer" and Agura 
 * IT hereinafter referred to as "Licensee".
 * 1.  License Grant: Upon completion of this agreement, the source 
 *     code that may be made available according to the documentation for 
 *     a particular software product (Software) from Manufacturer 
 *     (Source Code) shall be provided to Licensee, provided that 
 *     (1) funds have been received for payment of the License for Software and 
 *     (2) the appropriate License has been purchased as stated in the 
 *     documentation for Software. As used in this License Agreement, 
 *     Licensee shall also mean the individual using or installing 
 *     the source code together with any individual or entity, including 
 *     but not limited to your employer, on whose behalf you are acting 
 *     in using or installing the Source Code. By completing this agreement, 
 *     Licensee agrees to be bound by the terms and conditions of this Source 
 *     Code License Agreement. This Source Code License Agreement shall 
 *     be an extension of the Software License Agreement for the associated 
 *     product. No additional amendment or modification shall be made 
 *     to this Agreement except in writing signed by Licensee and 
 *     Manufacturer. This Agreement is effective indefinitely and once
 *     completed, cannot be terminated. Manufacturer hereby grants to 
 *     Licensee a non-transferable, worldwide license during the term of 
 *     this Agreement to use the Source Code for the associated product 
 *     purchased. In the event the Software License Agreement to the 
 *     associated product is terminated; (1) Licensee's rights to use 
 *     the Source Code are revoked and (2) Licensee shall destroy all 
 *     copies of the Source Code including any Source Code used in 
 *     Licensee's applications.
 * 2.  License Limitations
 *     2.1 Licensee may not resell, rent, lease or distribute the 
 *         Source Code alone, it shall only be distributed as a 
 *         compiled component of an application.
 *     2.2 Licensee shall protect and keep secure all Source Code 
 *         provided by this this Source Code License Agreement. 
 *         All Source Code provided by this Agreement that is used 
 *         with an application that is distributed or accessible outside
 *         Licensee's organization (including use from the Internet), 
 *         must be protected to the extent that it cannot be easily 
 *         extracted or decompiled.
 *     2.3 The Licensee shall not resell, rent, lease or distribute 
 *         the products created from the Source Code in any way that 
 *         would compete with Idega Software.
 *     2.4 Manufacturer's copyright notices may not be removed from 
 *         the Source Code.
 *     2.5 All modifications on the source code by Licencee must 
 *         be submitted to or provided to Manufacturer.
 * 3.  Copyright: Manufacturer's source code is copyrighted and contains 
 *     proprietary information. Licensee shall not distribute or 
 *     reveal the Source Code to anyone other than the software 
 *     developers of Licensee's organization. Licensee may be held 
 *     legally responsible for any infringement of intellectual property 
 *     rights that is caused or encouraged by Licensee's failure to abide 
 *     by the terms of this Agreement. Licensee may make copies of the 
 *     Source Code provided the copyright and trademark notices are 
 *     reproduced in their entirety on the copy. Manufacturer reserves 
 *     all rights not specifically granted to Licensee.
 *
 * 4.  Warranty & Risks: Although efforts have been made to assure that the 
 *     Source Code is correct, reliable, date compliant, and technically 
 *     accurate, the Source Code is licensed to Licensee as is and without 
 *     warranties as to performance of merchantability, fitness for a 
 *     particular purpose or use, or any other warranties whether 
 *     expressed or implied. Licensee's organization and all users 
 *     of the source code assume all risks when using it. The manufacturers, 
 *     distributors and resellers of the Source Code shall not be liable 
 *     for any consequential, incidental, punitive or special damages 
 *     arising out of the use of or inability to use the source code or 
 *     the provision of or failure to provide support services, even if we 
 *     have been advised of the possibility of such damages. In any case, 
 *     the entire liability under any provision of this agreement shall be 
 *     limited to the greater of the amount actually paid by Licensee for the 
 *     Software or 5.00 USD. No returns will be provided for the associated 
 *     License that was purchased to become eligible to receive the Source 
 *     Code after Licensee receives the source code. 
 */
package com.idega.block.image.presentation.bean.service;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.flickr4java.flickr.Flickr;
import com.flickr4java.flickr.FlickrException;
import com.flickr4java.flickr.REST;
import com.flickr4java.flickr.photos.Photo;
import com.flickr4java.flickr.photos.PhotoList;
import com.flickr4java.flickr.photos.Size;
import com.idega.block.image.presentation.bean.data.ImageBean;
import com.idega.idegaweb.IWMainApplication;
import com.idega.idegaweb.IWMainApplicationSettings;
import com.idega.util.ListUtil;


/**
 * <p>JSF managed bean for providing images</p>
 * <p>You can report about problems to: 
 * <a href="mailto:martynas@idega.is">Martynas Stakė</a></p>
 *
 * @version 1.0.0 2016 rugs. 2
 * @author <a href="mailto:martynas@idega.is">Martynas Stakė</a>
 */
public class ImagesService {
	
	private Flickr flickrAPI;

	private Flickr getFlickrAPI() {
		if (this.flickrAPI == null) {
			String apiKey = getProperty("flickr_api_key", "948bf0459a085e83887d4896ffbaff9c");
			String sharedSecret = getProperty("flickr_api_secret", "23939948ecf77994");
			this.flickrAPI = new Flickr(apiKey, sharedSecret, new REST());
		}

		return this.flickrAPI;
	}

	private IWMainApplicationSettings getSettings() {
		IWMainApplication application = IWMainApplication.getDefaultIWMainApplication();
		if (application != null) {
			return application.getSettings();
		}

		return null;
	}

	private String getProperty(String key, String value) {
		IWMainApplicationSettings settings = getSettings();
		if (settings != null) {
			return settings.getProperty(key, value);
		}

		return null;
	}

	public BufferedImage getBufferedImage(Photo photo) {
		BufferedImage image = null;
		if (photo != null) {
			try {
				image = getFlickrAPI().getPhotosInterface().getImage(photo.getLargeUrl());
			} catch (FlickrException e) {
				Logger.getLogger(getClass().getName()).log(Level.WARNING, 
						"Failed to get photo by id: " + photo.getId(), e);
			}
		}

		return image;
	}

	private Set<String> getExtraInfoTags() {
		HashSet<String> tags = new HashSet<String>();
		tags.add("description");
		tags.add("url_l");
		return tags;
	}

	private String getMSISportFlickrID() {
		return getProperty("flickr_user_id", "146599774@N04");
	}

	public ArrayList<ImageBean> getFlickrImages() {
		ArrayList<ImageBean> images = new ArrayList<ImageBean>();

		PhotoList<Photo> photos = null;
		try {

			photos = getFlickrAPI().getPeopleInterface().getPhotos(getMSISportFlickrID(), 
					null, null, null, null, null, null, null, getExtraInfoTags(), 0, 0);
		} catch (FlickrException e) {
			Logger.getLogger(getClass().getName()).log(Level.WARNING, 
					"Failed to get photos from Flickr, cause of:", e);
		}

		if (!ListUtil.isEmpty(photos)) {
			for (Photo photo : photos) {
				ImageBean bean = new ImageBean();
				bean.setLink(photo.getLargeUrl());
				bean.setThumbnailLink(photo.getSmallUrl());
				bean.setName(photo.getTitle());

				long currentTime = System.currentTimeMillis();
				Size size = photo.getLargeSize();
				if (size != null) {
					bean.setHeight(size.getHeight());
					bean.setWidth(size.getWidth());
				}

				Logger.getLogger(getClass().getName()).log(Level.WARNING, "Fetching phot info took:" + (System.currentTimeMillis() - currentTime) + "ms");
				images.add(bean);
			}
		}

		return images;
	}
}
