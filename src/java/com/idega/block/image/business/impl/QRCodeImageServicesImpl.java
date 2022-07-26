package com.idega.block.image.business.impl;

import java.io.ByteArrayOutputStream;
import java.util.logging.Level;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.idega.block.image.business.QRCodeImageServices;
import com.idega.core.business.DefaultSpringBean;
import com.idega.util.CoreConstants;
import com.idega.util.IOUtil;
import com.idega.util.StringUtil;

@Service
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class QRCodeImageServicesImpl extends DefaultSpringBean implements QRCodeImageServices {

	@Override
	public byte[] getGeneratedQRCodeImage(String text, int width, int height) {
		if (StringUtil.isEmpty(text)) {
			return null;
		}

		ByteArrayOutputStream output = null;
		try {
			width = width <= 0 ? 200 : width;
			height = height <= 0 ? 200 : height;
			BitMatrix matrix = new MultiFormatWriter().encode(
					new String(text.getBytes(CoreConstants.ENCODING_UTF8), CoreConstants.ENCODING_UTF8),
					BarcodeFormat.QR_CODE,
					width,
					height
			);

			output = new ByteArrayOutputStream();
			MatrixToImageWriter.writeToStream(matrix, "PNG", output);
			return output.toByteArray();
		} catch (Exception e) {
			getLogger().log(Level.WARNING, "Error generating QR code image. Text: " + text, e);
		} finally {
			IOUtil.close(output);
		}

		return null;
	}

}