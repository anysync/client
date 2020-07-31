// Copyright (c) 2020, Yanbin (Henry) Zheng <ybzheng@gmail.com>
// All rights reserved.
//
// Use of this source code is governed by a AGPLv3 license that can be
// found in the LICENSE file.
package net.anysync.util;

import java.nio.ByteBuffer;

public class IndexBinRow implements java.io.Serializable
{
    public final static int FILE_NAME_KEY_BYTE_COUNT =28 ;

    //Index.4|FileNameKey.16|Offset.4|CreateTime.4|FileMode.4|Timestamp.4|LastModified.4|size.8|opMode.1|user.4|Hash.28
    public final static int FILE_INFO_BYTE_COUNT =93;  // 65 + (FILE_NAME_KEY_BYTE_COUNT - 4)

    public final static int FILE_OFFSET_INDEX =4+ FILE_NAME_KEY_BYTE_COUNT ;        //Index.4|FileNameKey.16|Offset.4|CreateTime.4|FileMode.4
    public final static int FILE_MODE_INDEX = 4 + FILE_NAME_KEY_BYTE_COUNT + 4 + 4 ;//Index.4|FileNameKey.16|Offset.4|CreateTime.4|FileMode.4

    public final static int FILE_INFO_BYTE_HEADER_COUNT =16+(FILE_NAME_KEY_BYTE_COUNT -4) ;//40: Index, FileNameKey, Offset and CreateTime fields

    public final static int FILE_OPMODE_POS =FILE_INFO_BYTE_HEADER_COUNT +20 ;// + FileMode.4 Timestamp.4|LastModified.4|sizeAndOpMode.8
    public final static int HASH_BYTE_COUNT = 28;
    public final static int DEFAULT_OPERATION_MODE =255;
    public final static long TYPE_MASK  = 0xFFF00000L;
    public final static long TYPE_DIRECTORY  = 0x40000000;
    public final static long TYPE_DELETED = 0x00000000;


    public long index;
    public String fileNameKey;
    public long timestamp;
    public long createTime;
    public long fileMode;
    public long lastModified;
    public short operationMode;
    public long user;
    public long fileSize;
    public long offset;
    public byte[] hash;
    public String name;
//    byte[] raw;

    public void readBytes(byte[] b, int start)
    {
        index = toUnsignedInt(b, start);
        start += 4;
        byte[]bs = new byte[FILE_NAME_KEY_BYTE_COUNT];
        System.arraycopy(b,start, bs, 0, FILE_NAME_KEY_BYTE_COUNT);
        fileNameKey = byteArrayToHex(bs);
        start += FILE_NAME_KEY_BYTE_COUNT;
        offset = toUnsignedInt(b, start);
        start += 4;
        createTime = toUnsignedInt(b, start);

        start += 4;
        fileMode = toUnsignedInt(b, start);
        start += 4;
        timestamp = toUnsignedInt(b, start);
        start += 4;
        lastModified = toUnsignedInt(b, start);
        start += 4;
        fileSize = toLong(b, start);
        start+=8;
        operationMode = (short) (b[start] & 0xFF);
        start ++;
        user = toUnsignedInt(b,start);
        start += 4;
        hash = new byte[HASH_BYTE_COUNT];
        System.arraycopy(b, start, hash, 0, HASH_BYTE_COUNT);

//        name = fileNameKey.substring(0,10);
    }

    public String getHashString()
    {
        return byteArrayToHex(hash);
    }

    private static final char[] HEX_ARRAY = "0123456789abcdef".toCharArray();
    public static String byteArrayToHex(byte[] bytes)
    {
        char[] hexChars = new char[bytes.length * 2];
        for(int j = 0; j < bytes.length; j++)
        {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static long toUnsignedInt(byte[] bytes, int offset)
    {
        ByteBuffer buffer = ByteBuffer.allocate(4).put(bytes,  offset, 4);
        buffer.position(0);
        return buffer.getInt() & 0xFFFFFFFFL;
    }

    public static long toLong(byte[] bytes, int offset)
    {
        ByteBuffer buffer = ByteBuffer.allocate(8).put(bytes, offset, 8);
        buffer.position(0);
        return buffer.getLong() & 0xFFFFFFFFL;
    }

    public boolean isFileModeDirectory()
    {
        return (fileMode & TYPE_MASK) == TYPE_DIRECTORY  ;
    }

    public boolean isDeleted()
    {
            return (fileMode & TYPE_MASK) == TYPE_DELETED;
    }
}
