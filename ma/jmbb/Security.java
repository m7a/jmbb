package ma.jmbb;

import java.io.*;

class Security {

	static final boolean isChecksumEqual(byte[] a, byte[] b) {
		if(a.length != b.length) {
			throw new RuntimeException(new MBBFailureException(
				"This program will never compare checksums " +
				"of different length => This is likely to be " +
				"an implementation error."
			));
		}

		for(int i = 0; i < a.length; i++) {
			if(a[i] != b[i]) {
				return false;
			}
		}

		return true;
	}

	static OutputStream newAESOutputFilter(String password, OutputStream to)
						throws MBBFailureException {
		try {
			return new AESCryptOutputFilter(password, to);
		} catch(MBBFailureException ex) {
			throw ex;
		} catch(Exception ex) {
			throw new MBBFailureException(ex);
		}
	}

	static InputStream newAESInputFilter(String password,
				InputStream from) throws MBBFailureException {
		try {
			return new AESCryptInputFilter(password, from);
		} catch(MBBFailureException ex) {
			throw ex;
		} catch(Exception ex) {
			throw new MBBFailureException(ex);
		}	
	}

	/**
	 * Do not use this function for sizes above a few bytes. It is slow.
	 */
	static byte[] encrypt(String password, byte[] data)
						throws MBBFailureException {
		try {
			return encryptS(password, data);
		} catch(MBBFailureException ex) {
			throw ex;
		} catch(Exception ex) {
			throw new MBBFailureException(ex);
		}
	}

	private static byte[] encryptS(String password, byte[] data)
							throws Exception {
		ByteArrayOutputStream out = new ByteArrayOutputStream(512);
		OutputStream aes = newAESOutputFilter(password, out);
		try {
			aes.write(data);
		} finally {
			aes.close();
		}
		return out.toByteArray();
	}

	/**
	 * Do not use this function for sizes above a few bytes. It is slow.
	 */
	static byte[] decrypt(String password, byte[] encryptedData)
						throws MBBFailureException {
		try {
			return decryptS(password, encryptedData);
		} catch(MBBFailureException ex) {	
			throw ex;
		} catch(Exception ex) {
			throw new MBBFailureException(ex);
		}
	}

	private static byte[] decryptS(String pass, byte[] enc)
							throws Exception {
		InputStream aes = newAESInputFilter(pass,
						new ByteArrayInputStream(enc));
		ByteArrayOutputStream result = new ByteArrayOutputStream(256);
		try {
			StreamUtility.copy(aes, null, result);
		} finally {
			result.close();
		}
		return result.toByteArray();
	}

}
