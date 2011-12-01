/*
 * This is part of Geomajas, a GIS framework, http://www.geomajas.org/.
 *
 * Copyright 2008-2011 Geosparc nv, http://www.geosparc.com/, Belgium.
 *
 * The program is available in open source according to the GNU Affero
 * General Public License. All contributions in this program are covered
 * by the Geomajas Contributors License Agreement. For full licensing
 * details, see LICENSE.txt in the project root.
 */

package org.geomajas.plugin.staticsecurity.command.staticsecurity;

import java.io.IOException;

/**
 * Encodes and decodes to and from Base64 notation.
 *
 * @author Robert Harder
 * @author rob@iharder.net
 * @version 2.0
 */
public final class Base64 {

/* ********  P U B L I C   F I E L D S  ******** */


	/** No options specified. Value is zero. */
	public static final int NO_OPTIONS = 0;

	/** Specify encoding. */
	public static final int ENCODE = 1;

	/** Specify that data should be gzip-compressed. */
	public static final int GZIP = 2;


	/** Don't break lines when encoding (violates strict Base64 specification). */
	public static final int DONT_BREAK_LINES = 8;


/* ********  P R I V A T E   F I E L D S  ******** */


	/** Maximum line length (76) of Base64 output. */
	private static final int MAX_LINE_LENGTH = 76;


	/** The equals sign (=) as a byte. */
	private static final byte EQUALS_SIGN = (byte) '=';


	/** The new line character (\n) as a byte. */
	private static final byte NEW_LINE = (byte) '\n';


	/** Preferred encoding. */
	private static final String PREFERRED_ENCODING = "UTF-8";


	/** The 64 valid Base64 values. */
	private static final byte[] ALPHABET;
	private static final byte[] NATIVE_ALPHABET = /* May be something funny like EBCDIC */
			{
					(byte) 'A', (byte) 'B', (byte) 'C', (byte) 'D', (byte) 'E', (byte) 'F', (byte) 'G',
					(byte) 'H', (byte) 'I', (byte) 'J', (byte) 'K', (byte) 'L', (byte) 'M', (byte) 'N',
					(byte) 'O', (byte) 'P', (byte) 'Q', (byte) 'R', (byte) 'S', (byte) 'T', (byte) 'U',
					(byte) 'V', (byte) 'W', (byte) 'X', (byte) 'Y', (byte) 'Z',
					(byte) 'a', (byte) 'b', (byte) 'c', (byte) 'd', (byte) 'e', (byte) 'f', (byte) 'g',
					(byte) 'h', (byte) 'i', (byte) 'j', (byte) 'k', (byte) 'l', (byte) 'm', (byte) 'n',
					(byte) 'o', (byte) 'p', (byte) 'q', (byte) 'r', (byte) 's', (byte) 't', (byte) 'u',
					(byte) 'v', (byte) 'w', (byte) 'x', (byte) 'y', (byte) 'z',
					(byte) '0', (byte) '1', (byte) '2', (byte) '3', (byte) '4', (byte) '5',
					(byte) '6', (byte) '7', (byte) '8', (byte) '9', (byte) '+', (byte) '/',
			};

	/** Determine which ALPHABET to use. */
	static {
		byte[] bytes;
		try {
			bytes = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".getBytes(PREFERRED_ENCODING);
		} catch (java.io.UnsupportedEncodingException use) {
			bytes = NATIVE_ALPHABET; // Fall back to native encoding
		}
		ALPHABET = bytes;
	}

	private static final byte WHITE_SPACE_ENC = -5; // Indicates white space in encoding
	private static final byte EQUALS_SIGN_ENC = -1; // Indicates equals sign in encoding

	/**
	 * Translates a Base64 value to either its 6-bit reconstruction value or a negative number indicating some other
	 * meaning.
	 */
	private static final byte[] DECODABET =
			{
					-9, -9, -9, -9, -9, -9, -9, -9, -9, // Decimal  0 -  8
					-5, -5, // Whitespace: Tab and Linefeed
					-9, -9, // Decimal 11 - 12
					-5, // Whitespace: Carriage Return
					-9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, // Decimal 14 - 26
					-9, -9, -9, -9, -9, // Decimal 27 - 31
					-5, // Whitespace: Space
					-9, -9, -9, -9, -9, -9, -9, -9, -9, -9, // Decimal 33 - 42
					62, // Plus sign at decimal 43
					-9, -9, -9, // Decimal 44 - 46
					63, // Slash at decimal 47
					52, 53, 54, 55, 56, 57, 58, 59, 60, 61, // Numbers zero through nine
					-9, -9, -9, // Decimal 58 - 60
					EQUALS_SIGN_ENC, // Equals sign at decimal 61
					-9, -9, -9, // Decimal 62 - 64
					0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, // Letters 'A' through 'N'
					14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, // Letters 'O' through 'Z'
					-9, -9, -9, -9, -9, -9, // Decimal 91 - 96
					26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, // Letters 'a' through 'm'
					39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, // Letters 'n' through 'z'
					-9, -9, -9, -9,								 // Decimal 123 - 126
					/*,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,     // Decimal 127 - 139
								-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,     // Decimal 140 - 152
								-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,     // Decimal 153 - 165
								-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,     // Decimal 166 - 178
								-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,     // Decimal 179 - 191
								-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,     // Decimal 192 - 204
								-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,     // Decimal 205 - 217
								-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,     // Decimal 218 - 230
								-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,     // Decimal 231 - 243
								-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9         // Decimal 244 - 255 */
			};

	/** Defeats instantiation. */
	private Base64() {
	}


/* ********  E N C O D I N G   M E T H O D S  ******** */


	/**
	 * Encodes up to the first three bytes of array <var>threeBytes</var> and returns a four-byte array in Base64
	 * notation. The actual number of significant bytes in your array is given by <var>numSigBytes</var>. The array
	 * <var>threeBytes</var> needs only be as big as <var>numSigBytes</var>. Code can reuse a byte array by passing a
	 * four-byte array as <var>b4</var>.
	 *
	 * @param b4 A reusable byte array to reduce array instantiation
	 * @param threeBytes the array to convert
	 * @param numSigBytes the number of significant bytes in your array
	 * @return four byte array in Base64 notation.
	 */
	private static byte[] encode3to4(byte[] b4, byte[] threeBytes, int numSigBytes) {
		encode3to4(threeBytes, 0, numSigBytes, b4, 0);
		return b4;
	}   


	/**
	 * Encodes up to three bytes of the array <var>source</var> and writes the resulting four Base64 bytes to
	 * <var>destination</var>. The source and destination arrays can be manipulated anywhere along their length by
	 * specifying <var>srcOffset</var> and <var>destOffset</var>. This method does not check to make sure your arrays
	 * are large enough to accomodate <var>srcOffset</var> + 3 for the <var>source</var> array or
	 * <var>destOffset</var> + 4 for the <var>destination</var> array. The actual number of significant bytes in your
	 * array is given by <var>numSigBytes</var>.
	 *
	 * @param source the array to convert
	 * @param srcOffset the index where conversion begins
	 * @param numSigBytes the number of significant bytes in your array
	 * @param destination the array to hold the conversion
	 * @param destOffset the index where output will be put
	 * @return the <var>destination</var> array
	 */
	private static byte[] encode3to4(byte[] source, int srcOffset, int numSigBytes, byte[] destination,
									 int destOffset) {
		//           1         2         3
		// 01234567890123456789012345678901 Bit position
		// --------000000001111111122222222 Array position from threeBytes
		// --------|    ||    ||    ||    | Six bit groups to index ALPHABET
		//          >>18  >>12  >> 6  >> 0  Right shift necessary
		//                0x3f  0x3f  0x3f  Additional AND

		// Create buffer with zero-padding if there are only one or two
		// significant bytes passed in the array.
		// We have to shift left 24 in order to flush out the 1's that appear
		// when Java treats a value as negative that is cast from a byte to an int.
		int inBuff = (numSigBytes > 0 ? ((source[srcOffset] << 24) >>> 8) : 0)
				| (numSigBytes > 1 ? ((source[srcOffset + 1] << 24) >>> 16) : 0)
				| (numSigBytes > 2 ? ((source[srcOffset + 2] << 24) >>> 24) : 0);

		switch (numSigBytes) {
			case 3:
				destination[destOffset] = ALPHABET[(inBuff >>> 18)];
				destination[destOffset + 1] = ALPHABET[(inBuff >>> 12) & 0x3f];
				destination[destOffset + 2] = ALPHABET[(inBuff >>> 6) & 0x3f];
				destination[destOffset + 3] = ALPHABET[(inBuff) & 0x3f];
				return destination;

			case 2:
				destination[destOffset] = ALPHABET[(inBuff >>> 18)];
				destination[destOffset + 1] = ALPHABET[(inBuff >>> 12) & 0x3f];
				destination[destOffset + 2] = ALPHABET[(inBuff >>> 6) & 0x3f];
				destination[destOffset + 3] = EQUALS_SIGN;
				return destination;

			case 1:
				destination[destOffset] = ALPHABET[(inBuff >>> 18)];
				destination[destOffset + 1] = ALPHABET[(inBuff >>> 12) & 0x3f];
				destination[destOffset + 2] = EQUALS_SIGN;
				destination[destOffset + 3] = EQUALS_SIGN;
				return destination;

			default:
				return destination;
		}   // end switch
	}   // end encode3to4


	/**
	 * Encodes a byte array into Base64 notation. Does not GZip-compress data.
	 *
	 * @param source The data to convert
	 * @return bas64 encoded source
	 */
	public static String encodeBytes(byte[] source) {
		return encodeBytes(source, 0, source.length, NO_OPTIONS);
	}   // end encodeBytes

	/**
	 * Encodes a byte array into Base64 notation.
	 * <p/>
	 * Valid options:<pre>
	 *   GZIP: gzip-compresses object before encoding it.
	 *   DONT_BREAK_LINES: don't break lines at 76 characters
	 *     <i>Note: Technically, this makes your encoding non-compliant.</i>
	 * </pre>
	 * <p/>
	 * Example: <code>encodeBytes( myData, Base64.GZIP )</code> or
	 * <p/>
	 * Example: <code>encodeBytes( myData, Base64.GZIP | Base64.DONT_BREAK_LINES )</code>
	 *
	 * @param source The data to convert
	 * @param off Offset in array where conversion should begin
	 * @param len Length of data to convert
	 * @param options Specified options
	 * @return bas64 encoded source
	 * @see Base64#GZIP
	 * @see Base64#DONT_BREAK_LINES
	 */
	public static String encodeBytes(byte[] source, int off, int len, int options) {
		// Isolate options
		boolean breakLines = (options & DONT_BREAK_LINES) == 0;

		// Compress?
		if ((options & GZIP) != 0) {
			return encodeBytesCompressed(source, off, len, breakLines);
		} else {
			return encodeBytesNotCompressed(source, off, len, breakLines);
		}
	}
	
	private static String encodeBytesCompressed(byte[] source, int off, int len, boolean breakLines) {	
			java.io.ByteArrayOutputStream baos = null;
			java.util.zip.GZIPOutputStream gzos = null;
			Base64.OutputStream b64os = null;

			try {
				// GZip -> Base64 -> ByteArray
				baos = new java.io.ByteArrayOutputStream();
				b64os = new Base64.OutputStream(baos, ENCODE | (breakLines ? 0 : DONT_BREAK_LINES));
				gzos = new java.util.zip.GZIPOutputStream(b64os);

				gzos.write(source, off, len);
				gzos.close();
			}  catch (IOException e) {
				e.printStackTrace();
				return null;
			}  finally {
				safeClose(gzos);
				safeClose(b64os);
				safeClose(baos);
			}

			// Return value according to relevant encoding.
			try {
				return new String(baos.toByteArray(), PREFERRED_ENCODING);
			} catch (java.io.UnsupportedEncodingException uue) {
				return new String(baos.toByteArray());
			}   // end catch
		}

	private static void safeClose(java.io.OutputStream stream) {
		try {
			if (stream != null) {
				stream.close();
			}
		} catch (Exception e) {
			// ignore exception
		}
	}
	
	private static String encodeBytesNotCompressed(byte[] source, int off, int len, boolean breakLines) {
	
			int len43 = len * 4 / 3;
			byte[] outBuff = new byte[(len43)					  // Main 4:3
					+ ((len % 3) > 0 ? 4 : 0)	  // Account for padding
					+ (breakLines ? (len43 / MAX_LINE_LENGTH) : 0)]; // New lines
			int d = 0;
			int e = 0;
			int len2 = len - 2;
			int lineLength = 0;
			for ( ; d < len2 ; d += 3, e += 4) {
				encode3to4(source, d + off, 3, outBuff, e);

				lineLength += 4;
				if (breakLines && lineLength == MAX_LINE_LENGTH) {
					outBuff[e + 4] = NEW_LINE;
					e++;
					lineLength = 0;
				}   // end if: end of line
			}   // en dfor: each piece of array

			if (d < len) {
				encode3to4(source, d + off, len - d, outBuff, e);
				e += 4;
			}   // end if: some padding needed


			// Return value according to relevant encoding.
			try {
				return new String(outBuff, 0, e, PREFERRED_ENCODING);
			} catch (java.io.UnsupportedEncodingException uue) {
				return new String(outBuff, 0, e);
			}   // end catch
	}


/* ********  D E C O D I N G   M E T H O D S  ******** */


	/**
	 * Decodes four bytes from array <var>source</var> and writes the resulting bytes (up to three of them) to
	 * <var>destination</var>. The source and destination arrays can be manipulated anywhere along their length by
	 * specifying <var>srcOffset</var> and <var>destOffset</var>. This method does not check to make sure your arrays
	 * are large enough to accomodate <var>srcOffset</var> + 4 for the <var>source</var> array or
	 * <var>destOffset</var> + 3 for the <var>destination</var> array. This method returns the actual number of bytes
	 * that were converted from the Base64 encoding.
	 *
	 * @param source the array to convert
	 * @param srcOffset the index where conversion begins
	 * @param destination the array to hold the conversion
	 * @param destOffset the index where output will be put
	 * @return the number of decoded bytes converted
	 */
	private static int decode4to3(byte[] source, int srcOffset, byte[] destination, int destOffset) {
		// Example: Dk==
		if (source[srcOffset + 2] == EQUALS_SIGN) {
			// Two ways to do the same thing. Don't know which way I like best.
			//int outBuff =   ( ( DECODABET[ source[ srcOffset    ] ] << 24 ) >>>  6 )
			//              | ( ( DECODABET[ source[ srcOffset + 1] ] << 24 ) >>> 12 );
			int outBuff = ((DECODABET[source[srcOffset]] & 0xFF) << 18)
					| ((DECODABET[source[srcOffset + 1]] & 0xFF) << 12);

			destination[destOffset] = (byte) (outBuff >>> 16);
			return 1;
		} else if (source[srcOffset + 3] == EQUALS_SIGN) {
			// Two ways to do the same thing. Don't know which way I like best.
			//int outBuff =   ( ( DECODABET[ source[ srcOffset     ] ] << 24 ) >>>  6 )
			//              | ( ( DECODABET[ source[ srcOffset + 1 ] ] << 24 ) >>> 12 )
			//              | ( ( DECODABET[ source[ srcOffset + 2 ] ] << 24 ) >>> 18 );
			int outBuff = ((DECODABET[source[srcOffset]] & 0xFF) << 18)
					| ((DECODABET[source[srcOffset + 1]] & 0xFF) << 12)
					| ((DECODABET[source[srcOffset + 2]] & 0xFF) << 6);

			destination[destOffset] = (byte) (outBuff >>> 16);
			destination[destOffset + 1] = (byte) (outBuff >>> 8);
			return 2;
		} else {
			try {
				// Two ways to do the same thing. Don't know which way I like best.
				//int outBuff =   ( ( DECODABET[ source[ srcOffset     ] ] << 24 ) >>>  6 )
				//              | ( ( DECODABET[ source[ srcOffset + 1 ] ] << 24 ) >>> 12 )
				//              | ( ( DECODABET[ source[ srcOffset + 2 ] ] << 24 ) >>> 18 )
				//              | ( ( DECODABET[ source[ srcOffset + 3 ] ] << 24 ) >>> 24 );
				int outBuff = ((DECODABET[source[srcOffset]] & 0xFF) << 18)
						| ((DECODABET[source[srcOffset + 1]] & 0xFF) << 12)
						| ((DECODABET[source[srcOffset + 2]] & 0xFF) << 6)
						| ((DECODABET[source[srcOffset + 3]] & 0xFF));


				destination[destOffset] = (byte) (outBuff >> 16);
				destination[destOffset + 1] = (byte) (outBuff >> 8);
				destination[destOffset + 2] = (byte) (outBuff);

				return 3;
			} catch (Exception e) {
				System.out.println("" + source[srcOffset] + ": " + (DECODABET[source[srcOffset]]));
				System.out.println("" + source[srcOffset + 1] + ": " + (DECODABET[source[srcOffset + 1]]));
				System.out.println("" + source[srcOffset + 2] + ": " + (DECODABET[source[srcOffset + 2]]));
				System.out.println("" + source[srcOffset + 3] + ": " + (DECODABET[source[srcOffset + 3]]));
				return -1;
			}   //e nd catch
		}
	}   // end decodeToBytes


	/**
	 * Very low-level access to decoding ASCII characters in the form of a byte array. Does not support automatically
	 * gunzipping or any other "fancy" features.
	 *
	 * @param source The Base64 encoded data
	 * @param off The offset of where to begin decoding
	 * @param len The length of characters to decode
	 * @return decoded data
	public static byte[] decode(byte[] source, int off, int len) {
		int len34 = len * 3 / 4;
		byte[] outBuff = new byte[len34]; // Upper limit on size of output
		int outBuffPosn = 0;

		byte[] b4 = new byte[4];
		int b4Posn = 0;
		int i;
		byte sbiCrop;
		byte sbiDecode;
		for (i = off; i < off + len; i++) {
			sbiCrop = (byte) (source[i] & 0x7f); // Only the low seven bits
			sbiDecode = DECODABET[sbiCrop];

			if (sbiDecode >= WHITE_SPACE_ENC) { // White space, Equals sign or better
				if (sbiDecode >= EQUALS_SIGN_ENC) {
					b4[b4Posn++] = sbiCrop;
					if (b4Posn > 3) {
						outBuffPosn += decode4to3(b4, 0, outBuff, outBuffPosn);
						b4Posn = 0;

						// If that was the equals sign, break out of 'for' loop
						if (sbiCrop == EQUALS_SIGN) {
							break;
						}
					}   // end if: quartet built

				}   // end if: equals sign or better

			} else {
				System.err.println("Bad Base64 input character at " + i + ": " + source[i] + "(decimal)");
				return null;
			}   // end else:
		}   // each input character

		byte[] out = new byte[outBuffPosn];
		System.arraycopy(outBuff, 0, out, 0, outBuffPosn);
		return out;
	}   // end decode
	*/


	/**
	 * Decodes data from Base64 notation, automatically detecting gzip-compressed data and decompressing it.
	 *
	 * @param s the string to decode
	 * @return the decoded data
	public static byte[] decode(String s) {
		byte[] bytes;
		try {
			bytes = s.getBytes(PREFERRED_ENCODING);
		} catch (java.io.UnsupportedEncodingException uee) {
			bytes = s.getBytes();
		}   // end catch
		//</change>

		// Decode
		bytes = decode(bytes, 0, bytes.length);


		// Check to see if it's gzip-compressed
		// GZIP Magic Two-Byte Number: 0x8b1f (35615)
		if (bytes.length >= 2) {

			int head = ((int) bytes[0] & 0xff) | ((bytes[1] << 8) & 0xff00);
			if (bytes.length >= 4 && // Don't want to get ArrayIndexOutOfBounds exception
					java.util.zip.GZIPInputStream.GZIP_MAGIC == head) {
				java.io.ByteArrayInputStream bais = null;
				java.util.zip.GZIPInputStream gzis = null;
				java.io.ByteArrayOutputStream baos = null;
				byte[] buffer = new byte[2048];
				int length;

				try {
					baos = new java.io.ByteArrayOutputStream();
					bais = new java.io.ByteArrayInputStream(bytes);
					gzis = new java.util.zip.GZIPInputStream(bais);

					while ((length = gzis.read(buffer)) >= 0) {
						baos.write(buffer, 0, length);
					}   // end while: reading input

					// No error? Get new bytes.
					bytes = baos.toByteArray();

				} catch (IOException e) {
					// Just return originally-decoded bytes
				} finally {
					try {
						if (baos != null) {
							baos.close();
						}
					} catch (Exception e) {
						// ignore exception
					}
					try {
						if (gzis != null) {
							gzis.close();
						}
					} catch (Exception e) {
						// ignore exception
					}
					try {
						if (bais != null) {
							bais.close();
						}
					} catch (Exception e) {
						// ignore exception
					}
				}   // end finally

			}   // end if: gzipped
		}   // end if: bytes.length >= 2

		return bytes;
	}   // end decode
	*/


	/* ********  I N N E R   C L A S S   O U T P U T S T R E A M  ******** */


	/**
	 * A OutputStream will write data to another {@link java.io.OutputStream}, given in the constructor, and
	 * encode/decode to/from Base64 notation on the fly.
	 *
	 * @see Base64
	 * @see java.io.FilterOutputStream
	 */
	public static class OutputStream extends java.io.FilterOutputStream {
		private boolean encode;
		private int position;
		private byte[] buffer;
		private int bufferLength;
		private int lineLength;
		private boolean breakLines;
		private byte[] b4; // Scratch used in a few places
		private boolean suspendEncoding;

		/**
		 * Constructs a OutputStream in either ENCODE or DECODE mode.
		 * <p/>
		 * Valid options:<pre>
		 *   ENCODE or DECODE: Encode or Decode as data is read.
		 *   DONT_BREAK_LINES: don't break lines at 76 characters
		 *     (only meaningful when encoding)
		 *     <i>Note: Technically, this makes your encoding non-compliant.</i>
		 * </pre>
		 * <p/>
		 * Example: <code>new Base64.OutputStream( out, Base64.ENCODE )</code>
		 *
		 * @param out the {@link java.io.OutputStream} to which data will be written.
		 * @param options Specified options.
		 * @see Base64#ENCODE
		 * @see Base64#DONT_BREAK_LINES
		 */
		public OutputStream(java.io.OutputStream out, int options) {
			super(out);
			this.breakLines = (options & DONT_BREAK_LINES) != DONT_BREAK_LINES;
			this.encode = (options & ENCODE) == ENCODE;
			this.bufferLength = encode ? 3 : 4;
			this.buffer = new byte[bufferLength];
			this.position = 0;
			this.lineLength = 0;
			this.suspendEncoding = false;
			this.b4 = new byte[4];
		}   // end constructor


		/**
		 * Writes the byte to the output stream after converting to/from Base64 notation. When encoding, bytes are
		 * buffered three at a time before the output stream actually gets a write() call. When decoding, bytes are
		 * buffered four at a time.
		 *
		 * @param theByte the byte to write
		 * @throws IOException invalid data in stream
		 */
		public void write(int theByte) throws IOException {
			// Encoding suspended?
			if (suspendEncoding) {
				super.out.write(theByte);
				return;
			}   // end if: suspended

			// Encode?
			if (encode) {
				buffer[position++] = (byte) theByte;
				if (position >= bufferLength) { // Enough to encode.
					out.write(encode3to4(b4, buffer, bufferLength));

					lineLength += 4;
					if (breakLines && lineLength >= MAX_LINE_LENGTH) {
						out.write(NEW_LINE);
						lineLength = 0;
					}   // end if: end of line

					position = 0;
				}   // end if: enough to output
			} else {
				// Meaningful Base64 character?
				if (DECODABET[theByte & 0x7f] > WHITE_SPACE_ENC) {
					buffer[position++] = (byte) theByte;
					if (position >= bufferLength) {
						int len = Base64.decode4to3(buffer, 0, b4, 0);
						out.write(b4, 0, len);
						//out.write( Base64.decode4to3( buffer ) );
						position = 0;
					}   // end if: enough to output
				} else if (DECODABET[theByte & 0x7f] != WHITE_SPACE_ENC) {
					throw new IOException("Invalid character in Base64 data.");
				}   // end else: not white space either
			}   // end else: decoding
		}   // end write


		/**
		 * Calls {@link #write} repeatedly until len bytes are written.
		 *
		 * @param theBytes array from which to read bytes
		 * @param off offset for array
		 * @param len max number of bytes to read into array
		 * @throws IOException invalid data in stream
		 */
		public void write(byte[] theBytes, int off, int len) throws IOException {
			// Encoding suspended?
			if (suspendEncoding) {
				super.out.write(theBytes, off, len);
				return;
			}   // end if: suspended

			for (int i = 0; i < len; i++) {
				write(theBytes[off + i]);
			}   // end for: each byte written

		}   // end write


		/**
		 * Method added by PHIL. [Thanks, PHIL. -Rob] This pads the buffer without closing the stream.
		 *
		 * @throws IOException input not properly padded
		 */
		public void flushBase64() throws IOException {
			if (position > 0) {
				if (encode) {
					out.write(encode3to4(b4, buffer, position));
					position = 0;
				} else {
					throw new IOException("Base64 input not properly padded.");
				} 
			} 

		} 


		/**
		 * Flushes and closes (I think, in the superclass) the stream.
		 *
		 * @throws IOException input not properly padded or cannot close
		 */
		public void close()
				throws IOException {
			// 1. Ensure that pending characters are written
			flushBase64();

			// 2. Actually close the stream
			// Base class both flushes and closes.
			super.close();

			buffer = null;
			out = null;
		}
	}


}
