package com.idega.block.image.util;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import com.idega.util.CoreConstants;
import com.idega.util.IOUtil;

public class ImageUtil {

	private static final Logger LOGGER = Logger.getLogger(ImageUtil.class.getName());

	private static final ImageUtil INSTANCE = new ImageUtil();

	private static final String ONE = "1",
								ZERO = "0";

	private ImageUtil() {}

	public static final ImageUtil getIntance() {
		return INSTANCE;
	}

	private BufferedImage getResizedImage(BufferedImage originalImage, int width, int height) {
		Image resizedImage = originalImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
		BufferedImage bufferedResized = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

		Graphics2D g2d = bufferedResized.createGraphics();
		g2d.drawImage(resizedImage, 0, 0, null);
		g2d.dispose();
		return bufferedResized;
	}

	private String getCalculatedHash(BufferedImage img) {
		int width = 8;
		int height = 8;

		BufferedImage resizedImg = getResizedImage(img, width, height);

		// Convert to grayscale and calculate average pixel value
		long totalPixelValue = 0;
		int[] pixels = new int[width * height];
		int index = 0;

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int pixel = resizedImg.getRGB(x, y) & 0xFF;  // Grayscale intensity
				pixels[index++] = pixel;
				totalPixelValue += pixel;
			}
		}

		int avgPixelValue = (int) (totalPixelValue / (width * height));

		// Generate hash based on whether each pixel is above or below the average
		StringBuilder hash = new StringBuilder();
		for (int pixel : pixels) {
			hash.append(pixel >= avgPixelValue ? ONE : ZERO);
		}
		return hash.toString();
	}

	private boolean areImagesSimilar(String hash1, String hash2, int threshold) {
		// Hamming distance threshold - can be adjusted based on tolerance level
		int hammingDistance = 0;
		for (int i = 0; i < hash1.length(); i++) {
			if (hash1.charAt(i) != hash2.charAt(i)) {
				hammingDistance++;
			}
		}

		boolean similar = hammingDistance <= threshold;
		LOGGER.info("Images are" + (similar ? CoreConstants.EMPTY : " not") + " similar. Threshold: " + threshold + ", hamming distance: " + hammingDistance);
		return similar;
	}

	/**
	 * Threshold around 10-15: Good balance for slightly modified images (e.g., cropped, resized, or lightly compressed).
	 * Threshold above 15: Use with caution, as it can lead to very different images being marked as similar.
	 * Threshold below 10: Use when you need high accuracy and want only identical or nearly identical images to match.
	 * @param image1
	 * @param image2
	 * @param threshold
	 * @return true if similar
	 */
	public boolean areImagesSimilar(byte[] image1, byte[] image2, int threshold) {
		if (image1 == null || image2 == null) {
			return false;
		}

		InputStream stream = null;
		try {
			stream = new ByteArrayInputStream(image1);
			BufferedImage img1 = ImageIO.read(stream);
			IOUtil.close(stream);

			stream = new ByteArrayInputStream(image2);
			BufferedImage img2 = ImageIO.read(stream);
			IOUtil.close(stream);

			String hash1 = getCalculatedHash(img1);
			String hash2 = getCalculatedHash(img2);

			return areImagesSimilar(hash1, hash2, threshold);
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, "Error checking if images are similar. Threshold: " + threshold, e);
		} finally {
			IOUtil.close(stream);
		}

		return false;
	}

}