package com.idega.block.image.business;

public interface BarcodeServices {

	public byte[] getGeneratedBarcode(String type, String message);

}