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

import java.io.*;

import java.security.*;

import javax.crypto.Cipher;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import static ma.jmbb.AESCryptSharedData.*;

// Derived from AESCrypt.encrypt(int, InputStream, OutputStream)
class AESCryptOutputFilter extends FilterOutputStream {

	private static final String RANDOM_ALG = "SHA1PRNG";

	// Secure Random is Thread safe.
	// => We may use it to intialize a new "local" random for the stream.
	// http://stackoverflow.com/questions/1461568/is-securerandom-thread-
	// 								safe
	private static final SecureRandom seeder = new SecureRandom();

	private final AESCryptSharedData d;
	private final IvParameterSpec    ivSpec1;
	private final SecretKeySpec      aesKey1;
	private final IvParameterSpec    ivSpec2;
	private final SecretKeySpec      aesKey2;

	private final SecureRandom       random;
	private final MessageDigest      digest;

	private byte[] text;
	private int    last;
	private int    bufferContentLength;

	AESCryptOutputFilter(String password, OutputStream to)
							throws Exception {
		super(to); // represented as this.out (cf. FilterOutputStream).
		d = new AESCryptSharedData(password);

		random = SecureRandom.getInstance(RANDOM_ALG);
		digest = MessageDigest.getInstance(DIGEST_ALG);

		ivSpec1 = new IvParameterSpec(generateIv1());
		aesKey1 = new SecretKeySpec(d.generateAESKey1(ivSpec1.getIV(),
							d.password), CRYPT_ALG);
		ivSpec2 = new IvParameterSpec(generateIV2());
		aesKey2 = new SecretKeySpec(generateAESKey2(), CRYPT_ALG);

		out.write(AESCRYPT_HEADER_BYTES);
		out.write(AESCRYPT_VERSION);

		// Reserved extension constants not used with JMBB
		out.write(0); out.write(0); out.write(0);

		out.write(ivSpec1.getIV()); // Initialization Vector.

		text = new byte[BLOCK_SIZE + KEY_SIZE];
		d.cipher.init(Cipher.ENCRYPT_MODE, aesKey1, ivSpec1);
		d.cipher.update(ivSpec2.getIV(), 0, BLOCK_SIZE, text);
		d.cipher.doFinal(aesKey2.getEncoded(), 0, KEY_SIZE, text,
								BLOCK_SIZE);

		out.write(text); // Crypted IV and key.

		d.hmac.init(new SecretKeySpec(aesKey1.getEncoded(), HMAC_ALG));
		text = d.hmac.doFinal(text);
		out.write(text); // HMAC from previous cyphertext.

		try {
			d.cipher.init(Cipher.ENCRYPT_MODE, aesKey2, ivSpec2);
		} catch(InvalidKeyException ex) {
			throw new MBBFailureException(ex);
		}
		d.hmac.init(new SecretKeySpec(aesKey2.getEncoded(), HMAC_ALG));

		text = new byte[BLOCK_SIZE];
		last = 0;
		bufferContentLength = 0;
	}

	private byte[] generateIv1() {
		byte[] iv = new byte[BLOCK_SIZE];
		long time = System.currentTimeMillis();

		// Instead of using the MAC address we add some random bytes
		// from our "global" random AESCryptSharedData.seeder
		byte[] mac = new byte[8];
		seeder.nextBytes(mac);
		
		for (int i = 0; i < 8; i++) {
			iv[i] = (byte) (time >> (i * 8));
		}
		System.arraycopy(mac, 0, iv, 8, mac.length);
		digestRandomBytes(iv, 256);
		return iv;
	}

	private byte[] generateIV2() {
		byte[] iv = generateRandomBytes(BLOCK_SIZE);
		digestRandomBytes(iv, 256);
		return iv;
	}

	private byte[] generateAESKey2() {
		byte[] aesKey = generateRandomBytes(KEY_SIZE);
		digestRandomBytes(aesKey, 32);
		return aesKey;
	}

	// Will be called for all operations, cf. Javadoc FilterOutputStream.
	public void write(int b) throws IOException {
		text[bufferContentLength++] = (byte)b;
		if(bufferContentLength == BLOCK_SIZE) {
			flushBuffer();
		}
	}

	private void flushBuffer() throws IOException {
		try {
			d.cipher.update(text, 0, BLOCK_SIZE, text);
		} catch(ShortBufferException ex) {
			throw new IOException(new MBBFailureException(
				"Unexpected ShortBufferException. " +
				"implementation Error suspected. ", ex
			));
		}
		d.hmac.update(text);
		out.write(text);
		last = bufferContentLength;
		bufferContentLength = 0;
	}

	public void close() throws IOException {
		term();
		super.close();
	}

	private void term() throws IOException {
		if(bufferContentLength != 0) {
			flushBuffer();
		}
		last &= 0x0f;
		out.write(last); // Last block size mod 16.
		out.write(d.hmac.doFinal());
	}

	private void digestRandomBytes(byte[] bytes, int num) {
		digest.reset();
		digest.update(bytes);
		for (int i = 0; i < num; i++) {
			random.nextBytes(bytes);
			digest.update(bytes);
		}
		System.arraycopy(digest.digest(), 0, bytes, 0, bytes.length);
	}

	private byte[] generateRandomBytes(int len) {
		byte[] bytes = new byte[len];
		random.nextBytes(bytes);
		return bytes;
	}

}
