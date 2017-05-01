/*
 * =====================================================================
 *  This is a modified version of the original source of AESCrypt.java.
 *  The modification was made 2013 by the Ma_Sys.ma.
 *  For further information send an e-mail to Ma_Sys.ma@web.de.
 * =====================================================================
 *
 * The original copyright block of the original AESCrypt.java is included
 * between the dashed lines made of minus-signs.
 *
 * --------------------------------------------------------------------------
 *  Copyright 2008 Vócali Sistemas Inteligentes
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * --------------------------------------------------------------------------
 */
package ma.jmbb;

import java.nio.charset.*;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.Mac;

// This class contains all parts of AESCrypt which are shared between encryption
// and decryption. It has mainly been copied from the Java AESCrypt sourcecode.
class AESCryptSharedData {

	// Units are bits here
	static final String DIGEST_ALG          = "SHA-256";
	static final String HMAC_ALG            = "HmacSHA256";
	static final String CRYPT_ALG           = "AES";
	private static final String CRYPT_TRANS = "AES/CBC/NoPadding";

	// Units are bytes here.
	static final int KEY_SIZE   = 32;
	static final int BLOCK_SIZE = 16;
	static final int SHA_SIZE   = 32;

	// Documentation constants
	private static final Charset AESCRYPT_PASSWORD_ENCODING =
						StandardCharsets.UTF_16LE;
	private static final String AESCRYPT_HEADER_STRING = "AES";
	static final byte[] AESCRYPT_HEADER_BYTES =
			AESCRYPT_HEADER_STRING.getBytes(StandardCharsets.UTF_8);
	// Version 1 is NOT supported by the JMBB Implementation to reduce code
	// size.
	static final int AESCRYPT_VERSION = 2;

	final byte[] password;
	final Mac    hmac;
	final Cipher cipher;

	AESCryptSharedData(String password) throws MBBFailureException {
		super();
		this.password = password.getBytes(AESCRYPT_PASSWORD_ENCODING);
		try {
			cipher = Cipher.getInstance(CRYPT_TRANS);
			hmac   = Mac.getInstance(HMAC_ALG);
		} catch(Exception ex) {
			throw new MBBFailureException(ex);
		}
	}

	byte[] generateAESKey1(byte[] iv, byte[] password)
					throws NoSuchAlgorithmException {
		MessageDigest digest = MessageDigest.getInstance(DIGEST_ALG);

		byte[] aesKey = new byte[KEY_SIZE];
		System.arraycopy(iv, 0, aesKey, 0, iv.length);
		for (int i = 0; i < 8192; i++) {
			digest.reset();
			digest.update(aesKey);
			digest.update(password);
			aesKey = digest.digest();
		}
		return aesKey;
	}

}
