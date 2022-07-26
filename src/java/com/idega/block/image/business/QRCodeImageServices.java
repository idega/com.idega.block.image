package com.idega.block.image.business;

public interface QRCodeImageServices {

	public byte[] getGeneratedQRCodeImage(String text, int width, int height);

}