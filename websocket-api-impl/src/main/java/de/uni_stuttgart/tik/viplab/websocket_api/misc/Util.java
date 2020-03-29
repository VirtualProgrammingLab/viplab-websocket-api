package de.uni_stuttgart.tik.viplab.websocket_api.misc;

public class Util {
	private static final char[] HEX_ARRAY = "0123456789abcdef".toCharArray();

	/**
	 * Convert a byte array to hex string with lower.case letters. Useful for
	 * printing a digest as string.
	 * 
	 * @param bytes
	 * @return the hex string
	 */
	public static String bytesToHex(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 2];
		for (int j = 0; j < bytes.length; j++) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = HEX_ARRAY[v >>> 4];
			hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
		}
		return new String(hexChars);
	}
}
