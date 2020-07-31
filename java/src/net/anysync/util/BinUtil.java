// Copyright (c) 2020, Yanbin (Henry) Zheng <ybzheng@gmail.com>
// All rights reserved.
//
// Use of this source code is governed by a AGPLv3 license that can be
// found in the LICENSE file.
package net.anysync.util;

import com.google.protobuf.InvalidProtocolBufferException;
import net.anysync.protoc.FileAttribs;
import org.apache.log4j.*;;

import org.bouncycastle.crypto.generators.SCrypt;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.Security;
import java.util.*;

public class BinUtil implements java.io.Serializable
{
    final static Logger log = LogManager.getLogger(BinUtil .class);

    public static ArrayList<IndexBinRow> readBinFile(String folderHash, String fileName, boolean skipFirstRow) throws IOException
    {
        ArrayList<IndexBinRow> rows = new ArrayList<>();
        FileInputStream fis = new FileInputStream(fileName);
        byte[] buf = new byte[IndexBinRow.FILE_INFO_BYTE_COUNT * 512 ];
        boolean isFirstRow = true;
        ArrayList<String>keys = new ArrayList<>();
        HashMap<String, IndexBinRow> map = new HashMap<>();
        while(true)
        {
            int readSize = fis.read(buf);
            if(readSize < 0) break;
            int start = 0;
            while(start < readSize)
            {
                IndexBinRow row = new IndexBinRow();
                row.readBytes(buf, start);
                start += IndexBinRow.FILE_INFO_BYTE_COUNT;
                if(isFirstRow)
                {
                    isFirstRow = false;
                    if(skipFirstRow) continue;
                }
                if(row.isDeleted()) continue;
                keys.add(row.fileNameKey);
                map.put(row.fileNameKey, row);
                rows.add(row);
            }

        }


        Properties kvs = NetUtil.getFileNames(folderHash, keys);
//        HashMap<String, String> kvs = DbUtil.getValues(keys);

        for(Map.Entry entry: kvs.entrySet())
        {
            IndexBinRow row = map.get(entry.getKey());
            if(row == null) continue;
            String text = (String) entry.getValue();
            row.name = getDisplayFileName(text);
        }
        return rows;
    }

    public static Properties getRepositories(boolean includeShared)
    {
        Properties r = new Properties();
        String fileName = AppUtil.getBinFileName(AppUtil.NULL_HASH);
        try
        {
            ArrayList<IndexBinRow> rows = readBinFile(AppUtil.NULL_HASH, fileName, true);
            for(IndexBinRow row : rows)
            {
                String hash = row.getHashString();
                if(!includeShared && AppUtil.SHARED_HASH.equals(hash))
                {
                    continue;
                }
                r.setProperty(row.name, hash);
            }
        }
        catch(IOException e)
        {
            log.error(e);
        }
        return r;
    }

    private static String getDisplayFileName(String fileName)
    {
        if(!fileName.startsWith("/")) return fileName;
        fileName = fileName.substring(1);
        byte[] decodedBytes = Base64.getDecoder().decode(fileName);
        try
        {
            FileAttribs fattr = FileAttribs.parseFrom(decodedBytes);
            String prefix = "";
            fileName = "";
            if(fattr.containsAttribs("x"))
            {
                prefix = fattr.getAttribsOrThrow("x").toStringUtf8();
            }
            byte[] encrypted = fattr.getAttribsOrThrow("0").toByteArray();
            boolean isRSA = fattr.containsAttribs("0p");
            if(isRSA)
            {
                //use private key to decrypt
            }
            else
            {
                byte[] bs = decryptUsingMasterKey(encrypted);
                fileName = new String(bs, StandardCharsets.UTF_8);
                return prefix + fileName;
            }
        }
        catch(InvalidProtocolBufferException e)
        {
            log.error(e);
        }
        return fileName;
    }

    private static byte[] decryptUsingMasterKey(byte[] bs)
    {
        byte[] key = getClientMasterEncKey();
        return decrypt(bs, key);
    }

    private static byte[] clientEncKey;
    private static byte [] getClientMasterEncKey()
    {
        if(clientEncKey == null )
        {
            byte[] accessToken = getAccessToken();
            if(accessToken == null) return null;
            String keyFile = AppUtil.getFolder("data") + "access.keys";
            Map<String,byte[]>keys = decryptMasterKeyBytes(accessToken, FileUtil.readFileBytes(keyFile));
            clientEncKey = keys.get("enc");
        }
        return clientEncKey;
    }

    private  static byte[] currentAccessToken ;
    private static byte[] getAccessToken()
    {
        if(currentAccessToken != null) return currentAccessToken;
        String dataFile = AppUtil.getFolder("data") + "session.dat";
        currentAccessToken = FileUtil.readFileBytes(dataFile);
        return currentAccessToken;
    }

    static
    {
        Security.addProvider(new BouncyCastleProvider());
    }

    private static Map<String, byte[]>decryptMasterKeyBytes(byte[] password, byte[] bytes)
    {
        Map<String,byte[]>ret = new HashMap<>();
        FileAttribs fattr = null;
        try
        {
            fattr = FileAttribs.parseFrom(bytes);
        }
        catch(InvalidProtocolBufferException e)
        {
            return null;
        }
        byte[] salt = null;
        if(fattr.containsAttribs("salt")) salt = fattr.getAttribsOrThrow("salt").toByteArray();
        byte[] key = new byte[32];
        if(salt == null || salt.length == 0)
        {
            System.arraycopy(password, 0, key, 0, 32);
        }
        else
        {
            /*
            public static byte[] generate(byte[] P, byte[] S, int N, int r, int p, int dkLen)

Generate a key using the scrypt key derivation function.

Parameters:
    P - the bytes of the pass phrase.
    S - the salt to use for this invocation.
    N - CPU/Memory cost parameter. Must be larger than 1, a power of 2 and less than 2^(128 * r / 8).
    r - the block size, must be >= 1.
    p - Parallelization parameter. Must be a positive integer less than or equal to Integer.MAX_VALUE / (128 * r * 8).
    dkLen - the length of the key to generate.

    type Params struct {
	N       int // CPU/memory cost parameter (logN)
	R       int // block size parameter (octets)
	P       int // parallelisation parameter (positive int)
	SaltLen int // bytes to use as salt (octets)
	DKLen   int // length of the derived key (octets)
}     Params{N: 16384, R: 8, P: 1, SaltLen: 16, DKLen: 32}
             */

            //byte[] resBytes = SCrypt.generate(p.getBytes(), salt.getBytes(), cost, BSize, par, len);;
            //var DefaultParams = Params{N: 16384, R: 8, P: 1, SaltLen: 16, DKLen: 32}
            byte[] k = SCrypt.generate(password, salt, 16384 * 2, 8, 1, 32);
            System.arraycopy(k, 0, key, 0, 32);
        }
        byte [] decrypted = decrypt(fattr.getAttribsOrThrow("enc").toByteArray(), key);
        if(decrypted != null) ret.put("enc", decrypted);

        decrypted = decrypt(fattr.getAttribsOrThrow("auth").toByteArray(), key);
        if(decrypted != null) ret.put("auth", decrypted);
        decrypted = decrypt(fattr.getAttribsOrThrow("acc").toByteArray(), key);
        if(decrypted != null) ret.put("acc", decrypted);
        decrypted = decrypt(fattr.getAttribsOrThrow("pub").toByteArray(), key);
        if(decrypted != null) ret.put("pub", decrypted);
        decrypted = decrypt(fattr.getAttribsOrThrow("priv").toByteArray(), key);
        if(decrypted != null) ret.put("priv", decrypted);
        return ret;
    }



    public static final int GCM_TAG_LENGTH = 16;
    public static byte[] decrypt(byte[] cText, byte[] key)
    {
        try
        {
            SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            byte[] nounce = new byte[12];

            System.arraycopy(cText, 0, nounce, 0, 12);
            byte[] bs = new byte[cText.length - 12];
            System.arraycopy(cText, 12, bs, 0, cText.length - 12);

            cipher.init(Cipher.DECRYPT_MODE, keySpec, new GCMParameterSpec(GCM_TAG_LENGTH * 8, nounce));
            byte[] plainText = cipher.doFinal(bs);
            return plainText;
        }
        catch(Exception e)
        {
            log.error(e);
        }
        return null;
    }

    // 16 bytes IV
    public static byte[] getRandomNonce()
    {
        byte[] nonce = new byte[16];
        new SecureRandom().nextBytes(nonce);
        return nonce;
    }
}

