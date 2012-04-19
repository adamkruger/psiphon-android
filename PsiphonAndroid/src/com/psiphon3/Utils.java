package com.psiphon3;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.util.IllegalFormatException;
import java.util.Random;

import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;


public class Utils {

    private static SecureRandom s_secureRandom = new SecureRandom();
    static byte[] generateSecureRandomBytes(int byteCount)
    {
        byte bytes[] = new byte[byteCount];
        s_secureRandom.nextBytes(bytes);
        return bytes;
    }

    private static Random s_insecureRandom = new Random();
    static byte[] generateInsecureRandomBytes(int byteCount)
    {
        byte bytes[] = new byte[byteCount];
        s_insecureRandom.nextBytes(bytes);
        return bytes;
    }

    // from:
    // http://stackoverflow.com/questions/140131/convert-a-string-representation-of-a-hex-dump-to-a-byte-array-using-java
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character
                    .digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    // from:
    // http://stackoverflow.com/questions/332079/in-java-how-do-i-convert-a-byte-array-to-a-string-of-hex-digits-while-keeping-l
    public static String byteArrayToHexString(byte[] bytes) 
    {
        char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
        char[] hexChars = new char[bytes.length * 2];
        int v;
        for ( int j = 0; j < bytes.length; j++ ) 
        {
            v = bytes[j] & 0xFF;
            hexChars[j*2] = hexArray[v/16];
            hexChars[j*2 + 1] = hexArray[v%16];
        }
        return new String(hexChars);
    }

    /***************************************************************
     * Copyright (c) 1998, 1999 Nate Sammons <nate@protomatter.com> This library
     * is free software; you can redistribute it and/or modify it under the
     * terms of the GNU Library General Public License as published by the Free
     * Software Foundation; either version 2 of the License, or (at your option)
     * any later version.
     * 
     * This library is distributed in the hope that it will be useful, but
     * WITHOUT ANY WARRANTY; without even the implied warranty of
     * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library
     * General Public License for more details.
     * 
     * You should have received a copy of the GNU Library General Public License
     * along with this library; if not, write to the Free Software Foundation,
     * Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
     * 
     * Contact support@protomatter.com with your questions, comments, gripes,
     * praise, etc...
     ***************************************************************/

    /***************************************************************
     * - moved to the net.matuschek.util tree by Daniel Matuschek - replaced
     * deprecated getBytes() method in method decode - added String
     * encode(String) method to encode a String to base64
     ***************************************************************/

    /**
     * Base64 encoder/decoder. Does not stream, so be careful with using large
     * amounts of data
     * 
     * @author Nate Sammons
     * @author Daniel Matuschek
     * @version $Id: Base64.java,v 1.4 2001/04/17 10:09:27 matuschd Exp $
     */
    public static class Base64 {

        private Base64() {
            super();
        }

        /**
         * Encode some data and return a String.
         */
        public final static String encode(byte[] d) {
            if (d == null)
                return null;
            byte data[] = new byte[d.length + 2];
            System.arraycopy(d, 0, data, 0, d.length);
            byte dest[] = new byte[(data.length / 3) * 4];

            // 3-byte to 4-byte conversion
            for (int sidx = 0, didx = 0; sidx < d.length; sidx += 3, didx += 4) {
                dest[didx] = (byte) ((data[sidx] >>> 2) & 077);
                dest[didx + 1] = (byte) ((data[sidx + 1] >>> 4) & 017 | (data[sidx] << 4) & 077);
                dest[didx + 2] = (byte) ((data[sidx + 2] >>> 6) & 003 | (data[sidx + 1] << 2) & 077);
                dest[didx + 3] = (byte) (data[sidx + 2] & 077);
            }

            // 0-63 to ascii printable conversion
            for (int idx = 0; idx < dest.length; idx++) {
                if (dest[idx] < 26)
                    dest[idx] = (byte) (dest[idx] + 'A');
                else if (dest[idx] < 52)
                    dest[idx] = (byte) (dest[idx] + 'a' - 26);
                else if (dest[idx] < 62)
                    dest[idx] = (byte) (dest[idx] + '0' - 52);
                else if (dest[idx] < 63)
                    dest[idx] = (byte) '+';
                else
                    dest[idx] = (byte) '/';
            }

            // add padding
            for (int idx = dest.length - 1; idx > (d.length * 4) / 3; idx--) {
                dest[idx] = (byte) '=';
            }
            return new String(dest);
        }

        /**
         * Encode a String using Base64 using the default platform encoding
         **/
        public final static String encode(String s) {
            return encode(s.getBytes());
        }

        /**
         * Decode data and return bytes.
         */
        public final static byte[] decode(String str) {
            if (str == null)
                return null;
            byte data[] = str.getBytes();
            return decode(data);
        }

        /**
         * Decode data and return bytes. Assumes that the data passed in is
         * ASCII text.
         */
        public final static byte[] decode(byte[] data) {
            int tail = data.length;
            while (data[tail - 1] == '=')
                tail--;
            byte dest[] = new byte[tail - data.length / 4];

            // ascii printable to 0-63 conversion
            for (int idx = 0; idx < data.length; idx++) {
                if (data[idx] == '=')
                    data[idx] = 0;
                else if (data[idx] == '/')
                    data[idx] = 63;
                else if (data[idx] == '+')
                    data[idx] = 62;
                else if (data[idx] >= '0' && data[idx] <= '9')
                    data[idx] = (byte) (data[idx] - ('0' - 52));
                else if (data[idx] >= 'a' && data[idx] <= 'z')
                    data[idx] = (byte) (data[idx] - ('a' - 26));
                else if (data[idx] >= 'A' && data[idx] <= 'Z')
                    data[idx] = (byte) (data[idx] - 'A');
            }

            // 4-byte to 3-byte conversion
            int sidx, didx;
            for (sidx = 0, didx = 0; didx < dest.length - 2; sidx += 4, didx += 3) {
                dest[didx] = (byte) (((data[sidx] << 2) & 255) | ((data[sidx + 1] >>> 4) & 3));
                dest[didx + 1] = (byte) (((data[sidx + 1] << 4) & 255) | ((data[sidx + 2] >>> 2) & 017));
                dest[didx + 2] = (byte) (((data[sidx + 2] << 6) & 255) | (data[sidx + 3] & 077));
            }
            if (didx < dest.length) {
                dest[didx] = (byte) (((data[sidx] << 2) & 255) | ((data[sidx + 1] >>> 4) & 3));
            }
            if (++didx < dest.length) {
                dest[didx] = (byte) (((data[sidx + 1] << 4) & 255) | ((data[sidx + 2] >>> 2) & 017));
            }
            return dest;
        }
    }
    
    /**
     * URL-encodes a string. This is largely redundant with URLEncoder.encode,
     * but it tries to avoid using the deprecated URLEncoder.encode(String) while not
     * throwing the exception of URLEncoder.encode(String, String).
     * @param s  The string to URL encode.
     * @return The URL encoded version of s. 
     */
    static public String urlEncode(String s)
    {
        try
        {
            return URLEncoder.encode(s, "UTF-8");
        } 
        catch (UnsupportedEncodingException e)
        {
            Log.e(PsiphonConstants.TAG, e.getMessage());

            // Call the deprecated form of the function, which doesn't throw.
            return URLEncoder.encode(s);
        }                    
    }

    // From:
    // http://stackoverflow.com/questions/156275/what-is-the-equivalent-of-the-c-pairl-r-in-java
    // Use the factory like so: Pair.of("string", 22)
    static public class Pair<LEFT, RIGHT>
    {
        public final LEFT left;
        public final RIGHT right;
        
        private Pair(LEFT left, RIGHT right) {
            this.left = left;
            this.right = right;
        }

        public static <LEFT, RIGHT> Pair<LEFT, RIGHT> of(LEFT left, RIGHT right) {
            return new Pair<LEFT, RIGHT>(left, right);
        }
    }
    
    /**
     * Wrapper around Android's Log functionality. This should be used so that
     * LogCat messages will be turned off in production builds. For the reason
     * why we want this, see the link below.
     * If the logger member variable is set, messages will also be logged to 
     * that facility (except debug messages).
     * @see <a href="http://blog.parse.com/2012/04/10/discovering-a-major-security-hole-in-facebooks-android-sdk/">Discovering a Major Security Hole in Facebook's Android SDK</a>
     */
    static public class MyLog
    {
        static public interface ILogger
        {
            public void log(int priority, String message);
            public String getResString(int stringResID, Object... formatArgs);
            public int getAndroidLogPriorityEquivalent(int priority);
        }
        
        static public ILogger logger;
        
        /**
         * Safely wraps the string resource extraction function. If an error 
         * occurs with the format specifiers, the raw string will be returned.
         * @param stringResID The string resource ID.
         * @param formatArgs The format arguments. May be empty (non-existent).
         * @return The requested string, possibly formatted.
         */
        static private String myGetResString(int stringResID, Object... formatArgs)
        {
            try
            {
                return logger.getResString(stringResID, formatArgs);
            }
            catch (IllegalFormatException e)
            {
                return logger.getResString(stringResID);
            }
        }
        
        static void d(String msg)
        {
            MyLog.println(msg, null, Log.DEBUG);
        }

        static void d(String msg, Throwable throwable)
        {
            MyLog.println(msg, throwable, Log.DEBUG);
        }

        static void e(int stringResID, Object... formatArgs)
        {
            MyLog.println(MyLog.myGetResString(stringResID, formatArgs), null, Log.ERROR);
        }

        static void e(int stringResID, Throwable throwable)
        {
            MyLog.println(MyLog.myGetResString(stringResID), throwable, Log.ERROR);
        }
        
        static void i(int stringResID, Object... formatArgs)
        {
            MyLog.println(MyLog.myGetResString(stringResID, formatArgs), null, Log.INFO);
        }

        static void i(int stringResID, Throwable throwable)
        {
            MyLog.println(MyLog.myGetResString(stringResID), throwable, Log.INFO);
        }
        
        static void w(int stringResID, Object... formatArgs)
        {
            MyLog.println(MyLog.myGetResString(stringResID, formatArgs), null, Log.WARN);
        }

        static void w(int stringResID, Throwable throwable)
        {
            MyLog.println(MyLog.myGetResString(stringResID), throwable, Log.WARN);
        }
        
        private static void println(String msg, Throwable throwable, int priority)
        {
            // If we're not running in debug mode, don't log debug messages at all.
            // (This may be redundant with the logic below, but it'll save us if
            // the log below changes.)
            if (!PsiphonConstants.DEBUG && priority == Log.DEBUG)
            {
                return;
            }
            
            // If the external logger has been set, use it.
            // But don't put debug messages to the external logger.
            if (logger != null && priority != Log.DEBUG)
            {
                String loggerMsg = msg;
                
                if (throwable != null)
                {
                    loggerMsg = loggerMsg + ' ' + Log.getStackTraceString(throwable); 
                }
                
                logger.log(logger.getAndroidLogPriorityEquivalent(priority), loggerMsg);
            }
            
            // Do not log to LogCat at all if we're not running in debug mode.

            if (!PsiphonConstants.DEBUG)
            {
                return;
            }
                        
            // Log to LogCat
            // Note that this is basically identical to how Log.e, etc., are implemented.
            if (throwable != null)
            {
                msg = msg + '\n' + Log.getStackTraceString(throwable);
            }
            Log.println(priority, PsiphonConstants.TAG, msg);
        }
    }

    // From:
    // http://abhinavasblog.blogspot.ca/2011/06/check-for-debuggable-flag-in-android.html
    /*
    Copyright [2011] [Abhinava Srivastava]

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
    */
    public static boolean isDebugMode(Activity context)
    {
        boolean debug = false;
        PackageInfo packageInfo = null;
        try
        {
            packageInfo = context.getPackageManager().getPackageInfo(
                    context.getApplication().getPackageName(),
                    PackageManager.GET_CONFIGURATIONS);
        } 
        catch (NameNotFoundException e)
        {
            e.printStackTrace();
        }
        if (packageInfo != null)
        {
            int flags = packageInfo.applicationInfo.flags;
            if ((flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0)
            {
                debug = true;
            } 
            else
            {
                debug = false;
            }
        }
        return debug;
    }
}
