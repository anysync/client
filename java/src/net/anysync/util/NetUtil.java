// Copyright (c) 2020, Yanbin (Henry) Zheng <ybzheng@gmail.com>
// All rights reserved.
//
// Use of this source code is governed by a AGPLv3 license that can be
// found in the LICENSE file.
package net.anysync.util;
import net.anysync.ui.Main;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.log4j.*;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class NetUtil implements java.io.Serializable
{
    final static Logger log = LogManager.getLogger(NetUtil .class);

    public static class HttpReturn implements Serializable
    {
        public int code;
        public String response;
        public HttpReturn(int cd, String s)
        {
            code = cd;
            response = s;
        }
    }

    public  static String LOCAL_URL_PREFIX = "http://127.0.0.1:65066/";
    public static HttpReturn syncSendGetCommand(String command, Map<String, String> params, boolean restartGoServer)
    {
        String content = "";
        StringBuffer buf = new StringBuffer();
        if(params != null)
        {
            buf.append("?");
            for(Map.Entry<String, String> entry : params.entrySet())
            {
                if(buf.length() > 0)
                {
                    buf.append("&");
                }
                buf.append(entry.getKey() + "=" + encodeURL(entry.getValue()));
            }
        }
        String url = LOCAL_URL_PREFIX + "rest/" + command + buf.toString();
//        System.out.println("url:<" + url + ">");
        long t1 = System.currentTimeMillis();
        try (CloseableHttpClient httpclient = HttpClients.createDefault())
        {
            HttpGet httpGet = new HttpGet(url);
            RequestConfig.Builder requestConfig = RequestConfig.custom();
            requestConfig.setConnectTimeout(500, TimeUnit.MILLISECONDS);
            requestConfig.setConnectionRequestTimeout(500, TimeUnit.MILLISECONDS);

            httpGet.setConfig(requestConfig.build());
            try (CloseableHttpResponse response1 = httpclient.execute(httpGet))
            {
                HttpEntity entity = response1.getEntity();
                try
                {
                    content = entity != null?EntityUtils.toString(entity):"";
                }
                catch(ParseException e)
                {

                }
                return new HttpReturn(response1.getCode(), content);
            }
        }
        catch(IOException e)
        {
            long t2 = System.currentTimeMillis();
            log.error("Couldn't connect to server, conn.time: " + (t2-t1) + "; To restart server: " + restartGoServer);
            if(restartGoServer)
            {
                if(!startGoServer())
                {
                    return new HttpReturn(501, "");
                }
                try
                {
                    Thread.sleep(200);
                }
                catch(InterruptedException ex)
                {
                }
            }
            return new HttpReturn(500, "");
        }
    }

    public static HttpReturn syncSendPutCommand(String command, String data)
    {
        String content = "";
        String url = LOCAL_URL_PREFIX + "rest/" + command ;

        try (CloseableHttpClient httpclient = HttpClients.createDefault())
        {
            HttpPut httpPut = new HttpPut(url);
            if(data != null && data.length() > 0)
            {
                httpPut.setEntity(new StringEntity(data));
            }
            try (CloseableHttpResponse response1 = httpclient.execute(httpPut))
            {
                HttpEntity entity = response1.getEntity();
                try
                {
                    content = entity != null?EntityUtils.toString(entity):"";
                }
                catch(ParseException e)
                {

                }
                return new HttpReturn(response1.getCode(), content);
            }
        }
        catch(IOException e)
        {
            log.error("", e);
            return new HttpReturn(500, "");
        }
    }

    public static Map<String,String> loadSettings()
    {
        NetUtil.HttpReturn httpReturn = NetUtil.syncSendGetCommand("getsettings", null, false);
        Map<String, String> props = new HashMap<String, String>();
        if(httpReturn.code == 500)
        {
            return props;
        }
        String[] lines = Tokenizer.parse(httpReturn.response, '\n', true, true);
        for(String line : lines)
        {
            String[] tokens = Tokenizer.parse(line, '=', true, true);
            String val = "";
            if(tokens.length > 1)
            {
                val = tokens[1];
            }
            props.put(tokens[0], val);
        }
        String server = props.get("server");
        if(server != null && server.contains(".anysync.net"))
        {
            Main.setOfficial(true);
        }
        return props;
    }

    public static boolean updateSettings(Map<String,String> p)
    {
        NetUtil.HttpReturn httpReturn = syncSendGetCommand("updatesetting", p, false);
        return httpReturn.code == 200;
    }
    
    public static Properties getFileNames(String folderHash, List<String> keys)
    {
        String command = "getnames?folder=" + folderHash;
        String data = String.join( ",", keys);
        NetUtil.HttpReturn httpReturn = syncSendPutCommand(command, data);
        String[] lines = Tokenizer.parse(httpReturn.response, '\n', true, true);
        Properties props = new Properties();
        for(String line : lines)
        {
            String[] tokens = Tokenizer.parse(line, '=', true, true);
            String val = "";
            if(tokens.length > 1)
            {
                val = tokens[1];
            }
            props.put(tokens[0], val);
        }
        return props;
    }

    public final static String APP_SERVER_NAME = "anysync-server";
    public static boolean startGoServer()
    {
        String program = APP_SERVER_NAME;
        if(isWindows()) program += ".exe";
        String path = getJarPath();
        String exeFile = new File(path + "/" + program).getAbsolutePath();
        log.info("server.exe: " + exeFile);
//        ProcessBuilder p = new ProcessBuilder();
//        p.command(exeFile);
        try
        {
            Process ps = Runtime.getRuntime().exec(exeFile);
//            Process ps = p.start();
            log.info("server pid:" + ps.pid());
            return true;
        }
        catch(IOException e)
        {
            UiUtil.fatalError("Error", "Cannot start internal server. Exit now.");
            return false;
        }
    }

    public static String getJarPath()
    {
        try
        {
            String path = NetUtil.class.getProtectionDomain().getCodeSource().getLocation().getFile();
            path = java.net.URLDecoder.decode(path, "UTF-8");
            File f = new File(path);
            return f.getParent();
        }
        catch(Exception exp)
        {
            log.error(exp);
        }
        return null;
    }

    public static boolean isWindows()
    {
        return isOS("WINDOWS");
    }

    public static final boolean IS_MAC = ("" + System.getProperty("os.name").toLowerCase()).startsWith("mac os x");

    public static boolean isLinux()
    {
        return isOS("LINUX");
    }

    private static boolean isOS(String osName)
    {
        //os.name=Windows 2000
        String name = System.getProperty("os.name").toUpperCase();
        if(name != null && name.indexOf(osName.toUpperCase()) >= 0) return true;
        return false;
    }

    public static String encodeURL(String s)
    {
        try
        {
            return URLEncoder.encode(s, StandardCharsets.UTF_8.toString());
        }
        catch(UnsupportedEncodingException e)
        {
            return "";
        }
    }

    public static String[] getRepos()
    {
        Map<String, String> params = new HashMap<>();
        params.put("name", "yes");
        HttpReturn response = syncSendGetCommand("getrepos", params, false);
        if(response.code != 200)
        {
            return null;
        }
        String[] repos = Tokenizer.parse(response.response, '\n', true, true);
        return repos;
    }

    public static Map<String, String> parseURL(String urlstring)
    {
        Map<String, String> query_pairs = new LinkedHashMap<String, String>();
        try
        {
            int pos = urlstring.indexOf("?");
            if(pos < 0) return query_pairs;

            String query = urlstring.substring(pos + 1);
            String[] pairs = query.split("&");
            for(String pair : pairs)
            {
                int idx = pair.indexOf("=");
                query_pairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"), URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
            }
        }
        catch(Exception e)
        {
            log.error(e);
        }
        return query_pairs;
    }
}
