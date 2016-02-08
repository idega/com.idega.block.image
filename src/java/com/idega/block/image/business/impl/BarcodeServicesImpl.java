package com.idega.block.image.business.impl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.logging.Level;

import org.krysalis.barcode4j.ant.BarcodeTask;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.idega.block.image.business.BarcodeServices;
import com.idega.core.business.DefaultSpringBean;
import com.idega.util.FileUtil;
import com.idega.util.IOUtil;
import com.idega.util.StringHandler;
import com.idega.util.StringUtil;

@Service
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class BarcodeServicesImpl extends DefaultSpringBean implements BarcodeServices {

	@Override
	public byte[] getGeneratedBarcode(String type, String message) {
		if (StringUtil.isEmpty(type)) {
			getLogger().warning("Type is not provided");
			return null;
		}
		if (StringUtil.isEmpty(message)) {
			getLogger().warning("Message to encode in a barcode is not provided");
			return null;
		}

		File tmp = null;
		InputStream input = null;
		ByteArrayOutputStream output = null;
		try {
			tmp = File.createTempFile("barcode_" + StringHandler.stripNonRomanCharacters(message) + System.currentTimeMillis(), ".tmp");
			BarcodeTask task = new BarcodeTask();
			task.setSymbol(type);
			task.setMessage(message);
			task.setOutput(tmp);
			task.setFormat("png");
			task.setBw(false);
			task.setDpi(700);
			task.execute();

			input = new FileInputStream(tmp);
			output = new ByteArrayOutputStream();
			FileUtil.streamToOutputStream(input, output);
			return output.toByteArray();
		} catch (Exception e) {
			getLogger().log(Level.WARNING, "Error generating barcode. Message: " + message + ", type: " + type, e);
		} finally {
			IOUtil.close(input);
			IOUtil.close(output);
			if (tmp != null && tmp.exists() && tmp.canWrite()) {
				tmp.delete();
			}
		}

		return null;
	}

}