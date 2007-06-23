/*
 * Portions Copyright 2000-2006 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */

/*
 * @(#)EType.java	1.24 07/05/05
 *
 *  (C) Copyright IBM Corp. 1999 All Rights Reserved.
 *  Copyright 1997 The Open Group Research Institute.  All rights reserved.
 */

package sun.security.krb5.internal.crypto;

import sun.security.krb5.internal.*;
import sun.security.krb5.Config;
import sun.security.krb5.EncryptedData;
import sun.security.krb5.EncryptionKey;
import sun.security.krb5.KrbException;
import sun.security.krb5.Asn1Exception;
import sun.security.krb5.KrbCryptoException;
import javax.crypto.*;
import java.util.List;
import java.util.ArrayList;

//only needed if dataSize() implementation changes back to spec;
//see dataSize() below

public abstract class EType {
    
    private static final boolean DEBUG = Krb5.DEBUG;

    public static EType getInstance  (int eTypeConst)
	throws KdcErrException {
	EType eType = null;
	String eTypeName = null;
	switch (eTypeConst) {
	case EncryptedData.ETYPE_NULL:
	    eType = new NullEType();
	    eTypeName = "sun.security.krb5.internal.crypto.NullEType";
	    break;
	case EncryptedData.ETYPE_DES_CBC_CRC:
	    eType = new DesCbcCrcEType();
	    eTypeName = "sun.security.krb5.internal.crypto.DesCbcCrcEType";
	    break;
	case EncryptedData.ETYPE_DES_CBC_MD5:
	    eType = new DesCbcMd5EType();
	    eTypeName = "sun.security.krb5.internal.crypto.DesCbcMd5EType";
	    break;

	case EncryptedData.ETYPE_DES3_CBC_HMAC_SHA1_KD:
	    eType = new Des3CbcHmacSha1KdEType();
	    eTypeName = 
		"sun.security.krb5.internal.crypto.Des3CbcHmacSha1KdEType";
	    break;

	case EncryptedData.ETYPE_AES128_CTS_HMAC_SHA1_96:
	    eType = new Aes128CtsHmacSha1EType();
	    eTypeName = 
		"sun.security.krb5.internal.crypto.Aes128CtsHmacSha1EType";
	    break;

	case EncryptedData.ETYPE_AES256_CTS_HMAC_SHA1_96:
	    eType = new Aes256CtsHmacSha1EType();
	    eTypeName = 
		"sun.security.krb5.internal.crypto.Aes256CtsHmacSha1EType";
	    break;

	case EncryptedData.ETYPE_ARCFOUR_HMAC:
	    eType = new ArcFourHmacEType();
	    eTypeName = "sun.security.krb5.internal.crypto.ArcFourHmacEType";
	    break;

	default:
	    String msg = "encryption type = " + toString(eTypeConst) 
		+ " ("  + eTypeConst + ")";
	    throw new KdcErrException(Krb5.KDC_ERR_ETYPE_NOSUPP, msg);
	}
	if (DEBUG) {
	    System.out.println(">>> EType: " + eTypeName);
	}
	return eType;
    }

    public abstract int eType();

    public abstract int minimumPadSize();

    public abstract int confounderSize();

    public abstract int checksumType();

    public abstract int checksumSize();

    public abstract int blockSize();

    public abstract int keyType();

    public abstract int keySize();

    public abstract byte[] encrypt(byte[] data, byte[] key, int usage) 
	throws KrbCryptoException;

    public abstract byte[] encrypt(byte[] data, byte[] key, byte[] ivec, 
	int usage) throws KrbCryptoException;

    public abstract byte[] decrypt(byte[] cipher, byte[] key, int usage)
	throws KrbApErrException, KrbCryptoException;

    public abstract byte[] decrypt(byte[] cipher, byte[] key, byte[] ivec, 
	int usage) throws KrbApErrException, KrbCryptoException;

    public int dataSize(byte[] data)
    // throws Asn1Exception
    {
	// EncodeRef ref = new EncodeRef(data, startOfData());
	// return ref.end - startOfData();
	// should be the above according to spec, but in fact
	// implementations include the pad bytes in the data size
	return data.length - startOfData();
    }

    public int padSize(byte[] data) {
	return data.length - confounderSize() - checksumSize() -
	    dataSize(data);
    }

    public int startOfChecksum() {
	return confounderSize();
    }

    public int startOfData() {
	return confounderSize() + checksumSize();
    }

    public int startOfPad(byte[] data) {
	return confounderSize() + checksumSize() + dataSize(data);
    }

    public byte[] decryptedData(byte[] data) {
	int tempSize = dataSize(data);
	byte[] result = new byte[tempSize];
	System.arraycopy(data, startOfData(), result, 0, tempSize);
	return result;
    }

    private static final int[] BUILTIN_ETYPES = new int[] {
	EncryptedData.ETYPE_DES_CBC_MD5,
	EncryptedData.ETYPE_DES_CBC_CRC,
	EncryptedData.ETYPE_ARCFOUR_HMAC,
	EncryptedData.ETYPE_DES3_CBC_HMAC_SHA1_KD,
	EncryptedData.ETYPE_AES128_CTS_HMAC_SHA1_96,
	EncryptedData.ETYPE_AES256_CTS_HMAC_SHA1_96,
    };

    private static final int[] BUILTIN_ETYPES_NOAES256 = new int[] {
	EncryptedData.ETYPE_DES_CBC_MD5,
	EncryptedData.ETYPE_DES_CBC_CRC,
	EncryptedData.ETYPE_ARCFOUR_HMAC,
	EncryptedData.ETYPE_DES3_CBC_HMAC_SHA1_KD,
	EncryptedData.ETYPE_AES128_CTS_HMAC_SHA1_96,
    };


    // used in Config
    public static int[] getBuiltInDefaults() {
	int allowed = 0;
	try {
	    allowed = Cipher.getMaxAllowedKeyLength("AES");
	} catch (Exception e) {
	    // should not happen
	}
        if (allowed < 256) {
	    return BUILTIN_ETYPES_NOAES256;
	}
	return BUILTIN_ETYPES;
    }

    /** 
     * Retrieves the default etypes from the configuration file, or
     * if that's not available, return the built-in list of default etypes.
     */
    // used in KrbAsReq, KeyTab
    public static int[] getDefaults(String configName) {
	try {
	    return Config.getInstance().defaultEtype(configName);
	} catch (KrbException exc) {
	    if (DEBUG) {
		System.out.println("Exception while getting " +
		    configName + exc.getMessage());
		System.out.println("Using defaults " +
		    "des-cbc-md5, des-cbc-crc, des3-cbc-sha1," +
			" aes128cts, aes256cts, rc4-hmac");
	    }
	    return getBuiltInDefaults();
	}
    }

    /**
     * Retrieve the default etypes from the configuration file for
     * those etypes for which there are corresponding keys.
     * Used in scenario we have some keys from a keytab with etypes
     * different from those named in configName. Then, in order
     * to decrypt an AS-REP, we should only ask for etypes for which
     * we have keys.
     */
    public static int[] getDefaults(String configName, EncryptionKey[] keys) 
	throws KrbException {
	int[] answer = getDefaults(configName);
	if (answer == null) {
	    throw new KrbException("No supported encryption types listed in " 
		+ configName);
	}

	List<Integer> list = new ArrayList<Integer> (answer.length);
	for (int i = 0; i < answer.length; i++) {
	    if (EncryptionKey.findKey(answer[i], keys) != null) {
		list.add(answer[i]);
	    }
	}
	int len = list.size();
	if (len <= 0) {
	    StringBuffer keystr = new StringBuffer();
	    for (int i = 0; i < keys.length; i++) {
		keystr.append(toString(keys[i].getEType()));
	        keystr.append(" ");
	    }
	    throw new KrbException(
		"Do not have keys of types listed in " + configName + 
		" available; only have keys of following type: " + 
	        keystr.toString());
	} else {
	    answer = new int[len];
	    for (int i = 0; i < len; i++) {
		answer[i] = list.get(i);
	    }
	    return answer;
        }
    }

    public static boolean isSupported(int eTypeConst, int[] config) {
	for (int i = 0; i < config.length; i++) {
	    if (eTypeConst == config[i]) {
		return true;
	    }
	}
	return false;
    }

    public static boolean isSupported(int eTypeConst) {
	int[] enabledETypes = getBuiltInDefaults();
	return isSupported(eTypeConst, enabledETypes);
    }

    public static String toString(int type) {
        switch (type) {
        case 0:
            return "NULL"; 
        case 1:
            return "DES CBC mode with CRC-32";
        case 2:
            return "DES CBC mode with MD4";
        case 3:
            return "DES CBC mode with MD5";
        case 4:
            return "reserved";
        case 5:
            return "DES3 CBC mode with MD5";
        case 6:
            return "reserved";
        case 7:
            return "DES3 CBC mode with SHA1";
        case 9:
            return "DSA with SHA1- Cms0ID";
        case 10:
            return "MD5 with RSA encryption - Cms0ID";
        case 11:
            return "SHA1 with RSA encryption - Cms0ID";
        case 12:
            return "RC2 CBC mode with Env0ID";
        case 13:
            return "RSA encryption with Env0ID";
        case 14:
            return "RSAES-0AEP-ENV-0ID";
        case 15:
            return "DES-EDE3-CBC-ENV-0ID";
        case 16: 
            return "DES3 CBC mode with SHA1-KD";
        case 17:
            return "AES128 CTS mode with HMAC SHA1-96";
        case 18:
            return "AES256 CTS mode with HMAC SHA1-96";
        case 23:
            return "RC4 with HMAC";
        case 24:
            return "RC4 with HMAC EXP";

        }
        return "Unknown (" + type + ")";
    }
}
