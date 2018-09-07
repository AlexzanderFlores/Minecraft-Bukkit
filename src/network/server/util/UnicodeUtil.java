package network.server.util;

public class UnicodeUtil {
	public static String getUnicode(int value) {
		return String.valueOf((char) Integer.parseInt(String.valueOf(value), 16));
	}
	
	public static String getUnicode(String value) {
		return String.valueOf((char) Integer.parseInt(String.valueOf(value), 16));
	}
	
	public static String getHeart() {
		return getUnicode(2764);
	}
}
