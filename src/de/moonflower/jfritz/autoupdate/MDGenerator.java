package de.moonflower.jfritz.autoupdate;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;

public class MDGenerator {

	public static byte[] messageDigest(String file, String algo) throws Exception {
		MessageDigest messagedigest = MessageDigest.getInstance(algo);
		byte[] md = new byte[8192];
		InputStream in = new FileInputStream(file);
		for (int n = 0; (n = in.read(md)) > -1;)
			messagedigest.update(md, 0, n);
		return messagedigest.digest();
	}

	private static void digestDemo(String file, String algo) throws Exception {
		byte[] digest = messageDigest(file, algo);
		Logger.msg(algo + ", Schlüssellänge: " + digest.length * 8
				+ " Bit");
		for (int i = 0; i < digest.length; i++) {
			Logger.msg(Integer.toHexString(digest[i] & 0xFF));
		}
		Logger.msg("\n");
	}

	public static void main(String[] args) throws Exception {
		digestDemo("MDGenerator.java", "SHA");
		digestDemo("MDGenerator.java", "MD5");
	}

}
