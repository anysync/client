// Copyright (c) 2020, Yanbin (Henry) Zheng <ybzheng@gmail.com>
// All rights reserved.
//
// Use of this source code is governed by a AGPLv3 license that can be
// found in the LICENSE file.
package net.anysync.util;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import net.anysync.ui.AddRepoPane;
import net.anysync.ui.FileBrowserMain;
import net.anysync.ui.ListDialog;
import net.anysync.ui.Main;
import org.apache.log4j.*;;


import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class Httpd implements java.io.Serializable
{
    final static Logger log = LogManager.getLogger(AddRepoPane.class);
    private final static String WAIT_OBJ = "";
    public static void start(int port)
    {
        new Thread(() -> {
            try
            {
                HttpServer server = HttpServer.create(new InetSocketAddress("localhost", port), 0);
                server.createContext("/", new MyHttpHandler());
                ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
                server.setExecutor(threadPoolExecutor);
                server.start();
                log.info("HttpServer started on port " + port);
            }
            catch(java.net.BindException be)
            {
                try
                {
                    Thread.sleep(1000);
                } catch(InterruptedException e){ }
                Platform.runLater(()->{
                    NetUtil.LOCAL_URL_PREFIX = "http://127.0.0.1:" + port + "/";
                    NetUtil.syncSendGetCommand("toFront", null, false);
                    System.exit(0);
                });
            }
            catch(IOException e)
            {
                log.error(e);
            }

        }).start();
    }

    private static class MyHttpHandler implements HttpHandler
    {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException
        {
//            if("GET".equals(httpExchange.getRequestMethod()))
            handleResponse(httpExchange, httpExchange.getRequestURI().toString());
        }

        private void handleResponse(HttpExchange httpExchange, String uri) throws IOException
        {
//            System.out.println("uri: " + uri);
            handleURI(uri);
            OutputStream outputStream = httpExchange.getResponseBody();
            String htmlResponse = "";
            // this line is a must
            httpExchange.sendResponseHeaders(200, htmlResponse.length());
            outputStream.write(htmlResponse.getBytes());
            outputStream.flush();
            outputStream.close();
        }

        private void handleURI(String url)
        {
            int pos;
            url = decode(url);
            if(url.startsWith("/askrestore:"))
            {
                synchronized(WAIT_OBJ)
                {
                    WAIT_OBJ.notifyAll();
                }
            }
            else if(url.startsWith("/rest/toFront"))
            {
                Platform.runLater(()->{
                   Main.normalize();
                });
            }
            else if(url.startsWith("/torestore"))
            {
                Main.setStatus("To restore data ...");
            }
            else if(url.startsWith("/restoreDone"))
            {
                Main.setStatus("Files restored.");
            }
            else if(url.startsWith("/ready"))
            {
                Main.setStatus("");
            }
            else if(url.startsWith("/scanStart"))
            {
                Main.setStatus("Begin scanning ...");
            }
            else if(url.startsWith("/scanDone"))
            {
                Main.setStatus("Finished scanning.");
                FileBrowserMain.getFileBrowserController().reloadButtonClicked();
            }
            else if(url.startsWith("/scanning"))
            {
                UiUtil.messageBox(Alert.AlertType.INFORMATION, "Info", "Scanning is already in progress.");
                Main.setStatus("Scanning is already in progress.");
            }
            else if( (pos = url.indexOf("MSG:")) >=0)
            {
                String msg = url.substring(pos + 4);
                Main.setStatus(msg);
            }
            else if(url.startsWith("/doneDownload:"))
            {
                String file = url.substring(14);
                Main.setStatus("Downloaded file to: " + file);
            }
            else if(url.startsWith("/failDownload:"))
            {
                String file = url.substring(14);
                Main.setStatus("Failed to download: " + file);
            }
            else if(url.startsWith("/startUpload"))
            {
                Main.setStatus("Start uploading files...");
            }
            else if(url.startsWith("/failUpload"))
            {
                Main.setStatus("Failed to upload.");
            }
            else if(url.startsWith("/localFile:"))
            {
//                String data = url.substring(11);
//                int pos = data.indexOf(".");
//                uint modTime = data.substring(0, pos).toInt();
//                String filename = data.substring(pos + 1);
//                qDebug() << "modTime: " << modTime << "; file: " << filename;
//                QFileInfo fi (filename);
//                OCC::FolderMan * folderMan = OCC::FolderMan::instance();
//
//                folderMan -> addIgnoredFile(fi.absoluteFilePath(), modTime);
            }
            else if(url.startsWith("/openFile:"))
            {
                String file = url.substring(10);
                Main.setStatus("");
                Main.setStatus("To open file: " + file);
                try
                {
                    Desktop.getDesktop().open(new File(file));
                }
                catch(IOException e)
                {
                }
            }
            else if(url.startsWith("/qverify"))
            {
                Main.setStatus("");
                char c = url.charAt(8);
                if(c == '0')
                {
                    Main.setStatus("All files have been verified.");
                    UiUtil.messageBox(Alert.AlertType.INFORMATION, "Info", "All files have been verified.");
                }
                else
                {
                    String[] tokens = Tokenizer.parse(url, ':', true, true);
                    if(tokens.length == 3)
                    {
                        String number = tokens[1];
                        String[] files = Tokenizer.parse(tokens[2], ',', true, true);
                        Platform.runLater(()-> {
                                              ListDialog dlg = new ListDialog(files);
                                              dlg.show("Unsynced Files. Total file count:" + number, 750, 300);
                                          });
//                        String msg = "There are " + number + " file(s) that are not synced. Unsynced files are " + files;
//                        Main.setStatus(msg);
                    }

                }
            }
            else if(url.startsWith("/verified:"))
            {
                String folder = url.substring(10);
                String msg = "Cloud storage has been verified for " + folder;
                Main.setStatus(msg);
            }
            else if(url.startsWith("/nverified"))
            {
                verifyFailed(url);
            }
            else if(url.startsWith("/working"))
            {
                Main.setStatus("Work in progress...");
            }
            else if(url.startsWith("/fixed:"))
            {
//                String file = url.substring(6);
//                String msg = "Cloud storage issue has been fixed for " + file;
//                QMetaObject::invokeMethod (anysyncApplet, "messageBox", Qt::QueuedConnection, Q_ARG(String, msg));
            }
            else if(url.startsWith("/nfixed:"))
            {
//                String file = url.substring(7);
//                String msg = "Cloud storage issue has NOT been fixed for " + file;
//                QMetaObject::invokeMethod (anysyncApplet, "messageBox", Qt::QueuedConnection, Q_ARG(String, msg));
            }
            else if(url.startsWith("/shutdown"))
            {
                System.exit(0);
            }
            else if(url.indexOf("done") >= 0)
            {
                Main.setStatus("Done.");
            }
            else if(url.startsWith("/code:"))
            {
            }
            else
            {
            }
        }

        private void verifyFailed(String url)
        {
            Map<String,String> map = NetUtil.parseURL(url);
            String v = map.get("code");
            int code = Integer.parseInt(v);
            if(code == 404)//file not found on cloud
            {
            
            }
            String msg = map.get("msg");
            if(msg != null)
            {
                Main.setStatus(msg);
            }
        }

    }

    private static String decode(String msg)
    {
        try
        {
            msg = URLDecoder.decode(msg, StandardCharsets.UTF_8.toString());
        }
        catch(UnsupportedEncodingException e)
        {
            return msg;
        }
        return msg;
    }

    public static void waiting()
    {
        try
        {
            synchronized(WAIT_OBJ)
            {
                WAIT_OBJ.wait();
            }
        }
        catch(InterruptedException e)
        {
        }
    }

}
