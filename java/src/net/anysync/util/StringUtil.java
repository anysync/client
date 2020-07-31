// Copyright (c) 2020, Yanbin (Henry) Zheng <ybzheng@gmail.com>
// All rights reserved.
//
// Use of this source code is governed by a AGPLv3 license that can be
// found in the LICENSE file.
package net.anysync.util;
import java.io.*;

/**
 * This class is a collection of helper methods for string manipulation
 *
 * @version 1.0
 */
public class StringUtil
{
    /**
     * Returns true if passed string is null or empty
     */
    public static boolean isEmpty(String s)
    {
        return s == null || s.length() == 0;
    }

    public static final boolean equals(String s1, String s2)
    {
        if(s1 == s2) return true;
        if(s1 != null)
        {
            if(s1.equals(s2)) return true;
        }
        if(s2 != null)
        {
            if(s2.equals(s1)) return true;
        }
        return false;
    }

    public static final boolean containsIgnoreCase(String s1, String s2)
    {
        return s1.toLowerCase().indexOf(s2.toLowerCase()) >= 0;
    }

    public static final boolean equalsIgnoreCase(String s1, String s2)
    {
        if(s1 == s2) return true;
        if(s1 != null)
        {
            if(s1.equalsIgnoreCase(s2)) return true;
        }
        if(s2 != null)
        {
            if(s2.equalsIgnoreCase(s1)) return true;
        }
        return false;
    }
    
    public static final boolean equals(String[] s1, String[] s2)
    {
        if(s1 == s2) return true;
        if(s1 == null || s2 == null) return false;
        if(s1.length != s2.length) return false;

        for (int i = 0; i < s1.length ; i++) 
        {
            if( ! equals(s1[i], s2[i])) return false;
        }

        return true;
    }
    
    public static final boolean equalsIgnoreCase(String[] s1, String[] s2)
    {
        if(s1 == s2) return true;
        if(s1 == null || s2 == null) return false;
        if(s1.length != s2.length) return false;

        for (int i = 0; i < s1.length ; i++) 
        {
            if( ! equalsIgnoreCase(s1[i], s2[i])) return false;
        }

        return true;
    }
    
    /**
     * Check if the passed-in char c is one of white space, tab, CR, LF.
     * @return true if the passed-in char c is one of white space, tab, CR, LF.
     */
    public static final boolean isSpace(char c)
    {
        if( c == ' ' || c == '\t' || c == '\r' || c == '\n' )
        {
            return true;
        }
        return false;
    }
    
    /**
     * Check if the passed-in byte c is one of white space, tab, CR, LF.
     * @return true if the passed-in byte c is one of white space, tab, CR, LF.
     */
    public static final boolean isSpace(byte c)
    {
        if( c == ' ' || c == '\t' || c == '\r' || c == '\n' )
        {
            return true;
        }
        return false;
    }

    /** Remove line feed and carriage return */
    public static String removeLFCR(String s)
    {
        if(s == null ) return null;
        s = s.replace('\r', ' ');
        s = s.replace('\n', ' ');
        return s;
    }
    
    /**
     * Remove all the trailing null elements from the passed-in array
     * @return a String with all null byte removed from the passed-in byte array.
     */
    public static String removeTrailingNull(byte buf[])
    {
        StringBuffer stringbuffer = new StringBuffer(512);
        int len;
        for (len = buf.length - 1; len >= 0  ; len--) 
        {
            if(buf[len] != 0) break;
        }
        len ++;
        for(int i = 0; i < len; i++)
        {
            stringbuffer.append((char)buf[i]);
        }
        return stringbuffer.toString();
    }

    /**
     * Remove all the null elements from the passed-in array
     * @return a String with all null byte removed from the passed-in byte array.
     */
    public static String removeNull(byte buf[])
    {
        StringBuffer stringbuffer = new StringBuffer(4096);
        for(int i = 0; i < buf.length; i++)
        {
            if(buf[i] != 0)
            {
                stringbuffer.append((char)buf[i]);
            }
        }
        return stringbuffer.toString();
    }

    public static boolean isHex(String text)
    {
        if(text == null) return false;

        int len = text.length ();

        for (int i = 0; i < len; i++)
        {
            char c = text.charAt (i);
            if (! Character.isDigit (c))
            {
                if(  (c <= 'F' && c >= 'A') ||
                     (c <= 'f' && c >= 'a'))
                {
                    continue;
                }
                return false;
            }
        }

        return true;
    }
    
    /**
     * Determines if the specified string is digit
     * @return true if all chars in the passed-in text are between '0'-'9'.
     */
    public static boolean isDigit (String text)
    {
        if(text == null || text.length() == 0) return false;

        int len = text.length ();

        for (int i = 0; i < len; i++)
        {
            if (! Character.isDigit (text.charAt (i)))
            {
                return false;
            }
        }

        return true;
    }

    /**
     * Determines if the specified string is digit or '.'
     * @return true if all chars in the passed-in text are between '0'-'9' or '.'
     */
    public static boolean isNumeric(String text)
    {
        if(text == null) return false;

        int len = text.length ();

        for (int i = 0; i < len; i++)
        {
            char c = text.charAt (i);
            if (! Character.isDigit (c) && c != '.')
            {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Compute text contains how many token. e.g. "12345123" contains 2 "12", so it returns 2.
     */
    public static int numOfTokens(String text, String token)
    {
        int pos1, pos2, count;
        pos1 = -1;
        count = 0;
        while(true)
        {
            pos2 = text.indexOf(token, pos1 + 1);
            if(pos2 >= 0)
            {
                count ++;
                pos1 = pos2;
            }
            else
            {
                break;
            }
        }
        return count;
    }


    /**
     * Remove the specified character from string. If it occurs
     * multiple times, all occurrences will be removed.
     * @param text The source string.
     * @param c The char to be removed.
     * @return String a new string with all 'c' char's removed.
     */
    public static String remove (String text,
                                 char   c)
    {
        String result = text;

        if (text != null && text.length () > 0)
        {
            StringBuffer sb    = new StringBuffer (text.length ());
            int          index = 0;
            int          pos;

            for (;;)
            {
                pos = text.indexOf (c, index);

                if (pos < 0)
                {
                    sb.append (text.substring (index));
                    break;
                }

                sb.append(text.substring (index, pos));

                index = pos + 1;

                if (index == text.length ())
                {
                    break;
                }
            }

            result = sb.toString ();
        }

        return result;
    }


    /**
     * Removes all occurrences of 'substring' from the source string.
     * @param substring  The string to be removed.
     * @param text  The source string.
     * @return a string with all substring removed.
     */
    public static String remove (String text,
                                 String substring)
    {
        String result = text;

        if ("".equals (substring) || substring == null)
        {
            return result;
        }

        if (text != null && text.length () > 0)
        {
            int          lenSub = substring.length ();
            StringBuffer sb     = new StringBuffer (text.length ());
            int          index  = 0;
            int          pos;

            while (true)
            {
                pos = text.indexOf (substring, index);

                if (pos < 0)
                {
                    sb.append (text.substring (index));
                    break;
                }

                sb.append (text.substring (index, pos));
                index = pos + lenSub;

                if (index == text.length ())
                {
                    break;
                }
            }

            result = sb.toString ();
        }

        return result;
    }


    /**
     * This method will find 'replaceFrom' in 'original', and replace
     * it verbatim with 'replaceTo'. For example, calling this method
     * as follows:
     * <P>replaceString ("helloworld", "world", "")
     * <P>should return "hello".
     *
     * @param original  The source string.
     * @param replaceFrom  The substring to find.
     * @param replaceTo  The substring to replace 'replaceFrom' with.
     * @return String a new string with all occurrences replaced.
     */
    public static String replaceString (String original,
                                        String replaceFrom,
                                        String replaceTo)
    {
        int index = 0;

        if ("".equals (replaceFrom) ||
            replaceFrom == null ||
            original == null)
        {
           return original; 
        }

        StringBuffer buf = new StringBuffer (original.length ());

        while (true)
        {
            int pos = original.indexOf (replaceFrom, index);

            if (pos == -1)
            {
                buf.append (original.substring (index));
                break;
            }

            buf.append (original.substring (index, pos));
            buf.append (replaceTo);

            index = pos + replaceFrom.length ();

            if (index == original.length ())
            {
                break;
            }
        }

        return buf.toString ();
    }
    
    /**
     * Gets the first token, such as "ab" if calling getFirstToken("ab,c,d", ","),
     * or "ab" if calling getFirstToken("ab", ",");
     */
    public static String getFirstToken(String text, String delimiter)
    {
        int pos = text.indexOf(delimiter);
        if(pos < 0) return text;
        return text.substring(0, pos);
    }
    
    /**
     * Gets the last token, such as "d" if calling getFirstToken("ab,c,d", ","),
     * or "ab" if calling getFirstToken("ab", ",");
     */
    public static String getLastToken(String text, String delimiter)
    {
        int pos = text.lastIndexOf(delimiter);
        if(pos < 0) return text;
        return text.substring(pos + 1);
    }

    /**
     * Checks if string s is matched by pattern.
     * Supports "?", "*" each of which may be escaped with "\";
     * Not yet supported: internationalization; "\" inside brackets.<P>
     * Wildcard matching routine by Karl Heuer.  Public Domain.<P>
     * <p><pre>
     * Example:
     * 
     * wildcardMatch("abcde", "ab*") true 
     * wildcardMatch("abcde", "b*") false
     * wildcardMatch("abcde", "abc?") false
     * wildcardMatch("abcde", "abcd?") true
     * </pre>
     */
    public static boolean wildcardMatch( String s, String pattern )
    {
        char c;
        int si = 0, pi = 0;
        int slen = s.length();
        int plen = pattern.length();

        while ( pi < plen )
        {            // While still string
            c = pattern.charAt( pi++ );
            if ( c == '?' )
            {
                if ( ++si > slen )
                    return false;
            }
            else if ( c == '*' )
            {        // Wildcard
                if ( pi >= plen )
                    return true;
                do
                {
                    if ( wildcardMatch( s.substring( si ), pattern.substring( pi ) ) )
                        return true;
                }
                while ( ++si < slen );
                return false;
            }
            else
            {
                if ( si >= slen || c != s.charAt( si++ ) )
                    return false;
            }
        }
        return ( si == slen );
    }
    
    public static int findOneOf (String text, String searchString)
    {
        return findOneOf (text, searchString, 0);
    }


    /**
     * Check if 'text' contains any of the characters in searchString.
     * <BR>
     * <BR>
     * If 'text' or 'searchString' are null or empty, or 'start' is
     * an invalid index, -1 is returned. -1 is also returned if there
     * are no valid character matches.
     *
     * @param text (String) The source string.
     * @param searchString (String) The string containing the search char's.
     * @param start (int) The starting index in source string.
     * @return String The index of first occurrence of any character
     *         in searchString, or -1 if not found
     */
    public static int findOneOf (String text, String searchString, int start)
    {
        if (text == null) return -1;
        int  len = text.length ();
        char c;

        for (int i = start; i < len; i++)
        {
            c = text.charAt (i);

            if (searchString.indexOf (c) >= 0)
            {
                return (i);
            }
        }
        return -1;
    }
    
    public static String capitalizeFirstChar(String text)
    {
        char c = text.charAt(0);
        c = Character.toUpperCase(c);
        return "" + c + text.substring(1);
    }
    
    public static String lowerFirstChar(String text)
    {
        char c = text.charAt(0);
        c = Character.toLowerCase(c);
        return "" + c + text.substring(1);
    }
    
    public static String cloneString(String s)
    {
        if(s == null) return null;
        return new String(s);
    }
    
    /**
     * For null String, return "", otherwise return the passed String 
     */
    public static String convertNull(String s)
    {
        if(s == null) return "";
        return s;
    }
    
    public static byte[] toBytes(String s)
    {
        try
        {
            return s.getBytes("ISO-8859-1");
        }
        catch(UnsupportedEncodingException e)
        {
        }
        return null;
    }
    
    /** 
     * Converts byte array to String
     */
    public static String byteToString(byte[] bs)
    {
        try
        {
            return new String(bs, "ISO-8859-1");  
        }
        catch(UnsupportedEncodingException e)
        {
        }
        return null;
    }
 
    /**
     * If passed val starts with "n" or "N", "f" or "F", it return false;
     * otherwise returns true
     * @return false if passed val is null or empty;
     */
    public static boolean getBool(String val)
    {
        if(val == null || val.length() == 0)
        {
            return false;
        }
        char c = val.charAt(0);
        if(c == 'N' || c == 'n' || c == 'f' || c == 'F')
        {
            return false;
        }
        else
        {
            return true;
        }
    }
 
    public static String newString(byte [] bs)
    {
        try
        {
            return new String(bs, "ISO-8859-1");
        }
        catch(Exception e)
        {
        }
        return null;
    }
    
    /**
     * For instance, if raw="abc12de", then it should return "12" .
     */
    public static String extractNumber( String raw )
    {
        StringBuffer digit = new StringBuffer();
        if ( raw == null )
        {
            return digit.toString();
        }
        char[] array = raw.toCharArray();
        for ( int i = 0, size = array.length; i < size; i++ )
        {
            if ( Character.isDigit( array[i] ) )
            {
                digit.append( array[i] );
            }
            else if ( digit.length() > 0 )
            {
                break;
            }
        }
        return digit.toString();
    }

    public static String removeNonPrintable(String s)
    {
        if(s == null || s.length() == 0) return "";
        int length = s.length();
        char[] oldChars = new char[length];
        s.getChars(0, length, oldChars, 0);
        int newLen = 0;
        for(int j = 0; j < length; j++)
        {
            char ch = oldChars[j];
            if(ch >= ' ' || ch == '\r' || ch == '\n' || ch == '\t')
            {
                oldChars[newLen] = ch;
                newLen++;
            }
        }
        return new String(oldChars, 0, newLen);
    }

    public static String removeHtmlTags(String text)
    {
        return text.replaceAll("\\<.*?\\>", "");
    }

    public static String removeNewLines(String text)
    {
        if(text == null) return "";
        return text.replaceAll("\\r|\\n", " ");
    }

    public static void main(String[] args)
    {
         // System.out.println( "getFirstToken " + getFirstToken("ab,c, d", ","));
         // System.out.println( "getLastToken " + getLastToken("ab,c, d", ","));
         // System.out.println( "wildcardMatch " + wildcardMatch("abcde", "abc?"));
         // System.out.println( "capitalizeFirstChar =" + capitalizeFirstChar("abc") );
        String s = "test CDATA\t[!\u0002]\r\nabc";
        System.out.println(removeNonPrintable(s));

        s = "<html><font >SNMPv1 TRAPs</font>end";
        System.out.println(removeHtmlTags(s));
        s = "test \r test2 \r\ntest3\ntest4";

        System.out.println(removeNewLines(s));

        System.out.println("wildrettrue:" + wildcardMatch("abcde", "ab*") );

        System.out.println("retfalse:" + wildcardMatch("abcde", "b*") );
        System.out.println("return false:" + wildcardMatch("abcde", "abc?") );
        System.out.println("return true:" + wildcardMatch("abcde", "abcd?") );

    }
}


