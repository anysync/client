// Copyright (c) 2020, Yanbin (Henry) Zheng <ybzheng@gmail.com>
// All rights reserved.
//
// Use of this source code is governed by a AGPLv3 license that can be
// found in the LICENSE file.
package net.anysync.util;
import java.io.*;
import java.nio.file.Files;

public class FileUtil
{
    public static final String ENCODING = "UTF-8";
    private static org.apache.log4j.Logger _logger = org.apache.log4j.Logger.getLogger(FileUtil .class.getName());

    private FileUtil()
    {
    }

    /**
     * Using UTF-8 encoding to read file
     */
    public static BufferedReader getReader(String fileName) throws FileNotFoundException
    {
        /*
        try
        {
            return new BufferedReader(new InputStreamReader(new FileInputStream(fileName), ENCODING));
        }
        catch(UnsupportedEncodingException e)
        {
        }
        return null;
        */
        return getReader(fileName, null);
    }

    /**
     * Using UTF-8 encoding to read file
     */
    public static BufferedReader getReader(String fileName, String encoding) throws FileNotFoundException
    {
        if(encoding==null)
        {
            encoding=ENCODING;
        }
        try
        {
            return new BufferedReader(new InputStreamReader(new FileInputStream(fileName), encoding));
        }
        catch(UnsupportedEncodingException e)
        {
        }
        return null;
    }

    /**
     * Using default encoding to read file
     */
    public static BufferedInputStream getInputStream(String fileName) throws FileNotFoundException
    {
        return new BufferedInputStream(new FileInputStream(fileName));
    }

    /**
     * Using UTF-8 encoding to write file
     */
    public static BufferedWriter getWriter(String fileName) throws FileNotFoundException
    {
        /*
        try
        {
            return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), ENCODING));
        }
        catch(UnsupportedEncodingException e)
        {
        }
        return null;
        */
        return getWriter(fileName, null);
    }

    public static BufferedWriter getWriter(String fileName, String encoding) throws FileNotFoundException
    {
        if(encoding==null)
        {
            encoding=ENCODING;
        }
        try
        {
            return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), encoding));
        }
        catch(UnsupportedEncodingException e)
        {
        }
        return null;
    }

    /**
     * Using default encoding to write file
     */
    public static BufferedOutputStream getOutputStream(String fileName) throws FileNotFoundException
    {
        return new BufferedOutputStream(new FileOutputStream(fileName));
    }

    /**
     * @return the content of file as byte[], or null if IOException occurred.
     */
    public static byte[] readFileBytes(String fileName)
    {
        byte[] ret = null;
        try
        {
            ret = Files.readAllBytes(new File(fileName).toPath());
        }
        catch(IOException e)
        {
        }
        return ret;
    }
    /**
     * Reads file content into a String, assuming the file is in UTF-8 encoding
     * @return the content of file as String, or null if IOException occurred.
     */
    public static String readFile(String fileName)
    {
        /*
        StringBuffer sbuf = new StringBuffer();
        try
        {
            BufferedReader r = getReader(fileName);
            char[] buf = new char[16 * 1024];
            while(true)
            {
                int num = r.read(buf, 0, buf.length);
                if(num < 0) break;
                sbuf.append(buf, 0, num);
            }
            r.close();
        }
        catch(IOException e)
        {
            return null;
        }
        return sbuf.toString();
        */
        return readFile(fileName, null);
    }

    public static String readFile(String fileName, String encoding)
    {
        return readFile(fileName, encoding, 0);
    }

    public static String readFile(String fileName, String encoding, long offsetChars )
    {
        StringBuffer sbuf = new StringBuffer();
        BufferedReader r=null;
        try
        {
            r= getReader(fileName, encoding);
            if(offsetChars >0)
            {
                r.skip( offsetChars );
            }
            char[] buf = new char[16 * 1024];
            while(true)
            {
                int num = r.read(buf, 0, buf.length);
                if(num < 0) break;
                sbuf.append(buf, 0, num);
            }
        }
        catch(IOException e)
        {
            _logger.error("File reader", e);
            return null;
        }
        finally
        {
            if(r!=null)
            {
                try{r.close();}catch(Exception e){};
            }
        }
        return sbuf.toString();
    }

    /**
     * Write to a file using UTF-8 encoding
     */
    public static void writeFile(String fileName, String content)
        throws IOException
    {
        /*
        BufferedWriter w = getWriter(fileName);
        w.write(content, 0, content.length());
        w.close();
        */
        writeFile(fileName, content, null);
    }

    public static void writeFile(String fileName, String content, String encoding)
        throws IOException
    {
        BufferedWriter w = getWriter(fileName, encoding);
        w.write(content, 0, content.length());
        w.close();
    }

    /**
     * Returns the full path
     * @param directory directory name. It can either end with file separator or not
     */
    public static String getFullPath(String directory, String fileName)
    {
        if(directory.endsWith(File.separator))
        {
            return directory + fileName;
        }
        else
        {
            return directory + File.separator + fileName;
        }
    }

    /**
     * Copy file
     */
    public static void copyFile(File sourceFileName, File destFileName)  throws IOException
    {
        InputStream in = new FileInputStream( sourceFileName);
        OutputStream out = new FileOutputStream( destFileName );
        byte[] buf = new byte[16384];
        int len = 0;
        while ( ( len = in.read( buf ) ) > 0 )
        {
            out.write( buf, 0, len );
        }
        out.close();
        in.close();
    }

    /**
     * Copy file 
     */
    public static void copyFile(String sourceFileName, String destFileName)  throws IOException
    {
        InputStream in = new FileInputStream( sourceFileName );
        OutputStream out = new FileOutputStream( destFileName );
        byte[] buf = new byte[16384];
        int len = 0;
        while ( ( len = in.read( buf ) ) > 0 )
        {
            out.write( buf, 0, len );
        }
        out.close();
        in.close();
    }
}

