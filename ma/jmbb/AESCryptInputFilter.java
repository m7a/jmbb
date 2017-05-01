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

import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import ma.tools2.util.NotImplementedException;

import static ma.jmbb.AESCryptSharedData.*;

public class AESCryptInputFilter extends FilterInputStream {

	private final AESCryptSharedData d;

	private byte[] decryptedData;
	private int bufferSize;
	private int bufferPos;

	public AESCryptInputFilter(String password, InputStream from)
							throws Exception {
		// Represented as this.in (cf. FilterInputStream)
		// 1 additional byte for the last block's size modulo 16.
		super(new ArrayCachedInputStream(from, SHA_SIZE + 1));

		d = new AESCryptSharedData(password);

		verifyHeader();
		checkVersion();
		skipExtensions();

		byte[] text = new byte[BLOCK_SIZE];
		readEncrypted(text); // Initialization vector.
		IvParameterSpec ivSpec1 = new IvParameterSpec(text);
		SecretKeySpec aesKey1 = new SecretKeySpec(
				d.generateAESKey1(ivSpec1.getIV(), d.password),
								CRYPT_ALG);
		
		d.cipher.init(Cipher.DECRYPT_MODE, aesKey1, ivSpec1);
		byte[] backup = new byte[BLOCK_SIZE + KEY_SIZE];
		// IV and key to decrypt file contents.
		readEncrypted(backup);
		text = d.cipher.doFinal(backup);
		IvParameterSpec ivSpec2 = new IvParameterSpec(text, 0,
								BLOCK_SIZE);
		SecretKeySpec aesKey2 = new SecretKeySpec(text, BLOCK_SIZE,
							KEY_SIZE, CRYPT_ALG);

		d.hmac.init(new SecretKeySpec(aesKey1.getEncoded(), HMAC_ALG));
		backup = d.hmac.doFinal(backup);
		text = new byte[SHA_SIZE];
		readEncrypted(text); // HMAC and authenticity test.
		if(!Arrays.equals(backup, text)) {
			throw new MBBFailureException("Message has been " +
					"altered or password incorrect.");
		}

		// Warning: The original AESCrypt implementation used the file
		//          size to perform additional integrity checks by
		//          checking if the given filesize matches the
		//          blocksize. This check has been removed to be able to
		//          operate as a normal stream.
		// Warning: This implementation does not check if a given
		//          encrypted file is empty.

		d.cipher.init(Cipher.DECRYPT_MODE, aesKey2, ivSpec2);
		d.hmac.init(new SecretKeySpec(aesKey2.getEncoded(), HMAC_ALG));

		decryptedData = new byte[BLOCK_SIZE];
		bufferPos = 0;
		bufferSize = 0;
	}

	private void verifyHeader() throws MBBFailureException, IOException {
		byte[] text = new byte[AESCRYPT_HEADER_BYTES.length];
		readEncrypted(text);
		if(!Arrays.equals(text, AESCRYPT_HEADER_BYTES)) {
			throw new AESCryptHeaderCheckFailureException();
		}
	}

	private void readEncrypted(byte[] bytes) throws IOException {
		if(in.read(bytes) != bytes.length) {
			throw new IOException("Unexpected end of file");
		}
	}

	private void checkVersion() throws MBBFailureException, IOException {
		int version = in.read();
		if(version != 2) { // Check version (We only support V2.)
			throw new MBBFailureException(
				"Unsupported AESCrypt file format version: " +
				version + ". Remember that JMBB only " +
				"implements V2."
			);
		}

		in.read(); // Skip reserved.
	}

	private void skipExtensions() throws MBBFailureException, IOException {
		byte[] text = new byte[2];
		int len;
		do {
			readEncrypted(text);
			len = ((0xff & (int)text[0]) << 8) |
							(0xff & (int)text[1]);
			if(in.skip(len) != len) {
				throw new MBBFailureException(
					"Unexpected end of extension. With " +
					"JMBB this should be empty anyway..."
				);
			}
		} while(len != 0);
	}

	@Override
	public int read() throws IOException {
		if(bufferPos == bufferSize) {
			if(!fillBuffer()) {
				return -1;
			}
		}
		return decryptedData[bufferPos++] & 0xff;
	}

	private boolean fillBuffer() throws IOException {
		if(((ArrayCachedInputStream)in).isEndOfData()) {
			return false;
		}

		byte[] raw = readRawInput();

		try {
			d.cipher.update(raw, 0, BLOCK_SIZE, decryptedData);
		} catch(ShortBufferException ex) {
			throw new IOException(new MBBFailureException(
				"Implementation error or data corruption.", ex
			));
		}
		d.hmac.update(raw, 0, BLOCK_SIZE);

		try {
			byte[] footer;
			if((footer = getFooterOnEndData()) != null) {
				cacheRestData();
				verifyHMAC(footer);
			}
		} catch(MBBFailureException ex) {
			throw new IOException(ex);
		}

		return true;
	}

	private byte[] readRawInput() throws IOException {
		byte[] raw = new byte[decryptedData.length];
		if(in.read(raw, 0, raw.length) != BLOCK_SIZE) {
			throw new IOException(new MBBFailureException(
				"Unexpected EOF: Partial Block."
			));
		}
		bufferPos = 0;
		bufferSize = BLOCK_SIZE;
		return raw;
	}

	private byte[] getFooterOnEndData()
				throws IOException, MBBFailureException {
		byte[] footer = null;
		if(((ArrayCachedInputStream)in).isEndOfData()) {
			footer = ((ArrayCachedInputStream)in).getFooter();

			// Hack to get proper unsigned int.
			int lastBlockSizeMod16 = footer[0] & 0xff;

			if(lastBlockSizeMod16 > 0) {
				bufferSize = lastBlockSizeMod16;
			} else {
				bufferSize = BLOCK_SIZE;
			}
		}
		return footer;
	}

	private void cacheRestData() throws MBBFailureException {
		byte[] rest;
		try {
			rest = d.cipher.doFinal();
		} catch(Exception ex) {
			throw new MBBFailureException(ex);
		}
		byte[] decryptedNew = new byte[bufferSize + rest.length];
		System.arraycopy(decryptedData, 0, decryptedNew, 0, bufferSize);
		System.arraycopy(rest, 0, decryptedNew, bufferSize,
								rest.length);
		decryptedData = decryptedNew;
		bufferSize = decryptedNew.length;
	}

	private void verifyHMAC(byte[] footer) throws MBBFailureException {
		byte[] hmac = d.hmac.doFinal();

		if(hmac.length != footer.length - 1) {
			throw new MBBFailureException(
				"Different checksum lengths. Might be a " +
				"different method or corrupted file. " +
				hmac.length + " != " + (footer.length - 1)
			);
		}

		for(int i = 0; i < hmac.length; i++) {
			if(hmac[i] != footer[i + 1]) {
				throw new MBBFailureException(
					"Message has been altered or " +
					"password incorrect."
				);
			}
		}
	}

	// Not very efficient but it is not simply possible to read arrays of
	// arbitrary size from the encrypted input data.
	public int read(byte[] b, int off, int len) throws IOException {
		int i;
		for(i = 0; i < len; i++) {
			int si = read();
			if(si == -1) {
				break;
			}
			b[off + i] = (byte)si;
		}
		// End of stream => return -1
		if(i == 0) {
			return -1;
		} else {
			return i;
		}
	}

	public int read(byte[] b) throws IOException {
		return read(b, 0, b.length);
	}

	public long skip(long n) throws IOException {
		for(long i = 0; i < n;) {
			if(read() == -1) {
				return i;
			}
		}
		return n;
	}

	public boolean markSupported() {
		return false;
	}

}
