// Copyright (c) 2020, Yanbin (Henry) Zheng <ybzheng@gmail.com>
// All rights reserved.
//
// Use of this source code is governed by a AGPLv3 license that can be
// found in the LICENSE file.
package net.anysync.ui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import net.anysync.util.AppUtil;
import net.anysync.util.Httpd;
import net.anysync.util.NetUtil;
import org.apache.log4j.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

public class Main extends Application
{
    static Logger log = Logger.getLogger(Main.class);
    public final static int HTTP_PORT = 65068;
    private double xOffset = 0;
    private double yOffset = 0;
    private static Stage currentStage;

    public static URL getResource(String r)
    {
        return Main.class.getResource(r);
    }

    public static void setCurrentStage(Stage s)
    {
        currentStage = s;
        s.getIcons().add(getImage("/images/app128.png"));
    }

    public static Stage getCurrentStage()
    {
        return currentStage;
    }

    public static Image getImage(String fileName)
    {
        return new Image(Main.class.getResourceAsStream(fileName));
    }

    public final static String BUILD = "2100";

    private static ResourceBundle resourceBundle;

    @Override
    public void start(Stage stage) throws Exception
    {
        setCurrentStage(stage);
        ResourceBundle r = ResourceBundle.getBundle(AppUtil.I18N, Locale.getDefault());
        resourceBundle = r;

        if(AppUtil.currentFolderExists())
        {
            LoginController.openMainPane(stage);
            if(minimized) minimize(stage);
            stage.getScene().getWindow().addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, this::closeWindowEvent);
            return;
        }
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getResource("/fxml/Login.fxml"));
        loader.setResources(r);
        Parent root = loader.load();
        stage.initStyle(StageStyle.DECORATED);
        stage.setTitle(getString("app_name") + ". Build " + BUILD);
        stage.setMaximized(false);
        root.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        root.setOnMouseDragged(event -> {
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        });

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.getScene().getWindow().addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, this::closeWindowEvent);
        LoginController controller = loader.getController();
        setStatus(controller.getStatus());
        stage.show();
    }

    private void closeWindowEvent(WindowEvent event)
    {
        minimize(currentStage);
        event.consume();
    }

    private static void minimize(Stage s)
    {
        if(NetUtil.isWindows() )
        {
            s.hide();
        }
        else if(NetUtil.isLinux())
        {
            System.exit(0);
        }
        else
        {
            s.setIconified(true);
        }
    }

    public static void normalize()
    {
        if(!currentStage.isShowing()) currentStage.show();
        currentStage.toFront();
    }

    public static String getStringWithColon(String s)
    {
        return getString(s) + ":";
    }

    public static String getString(String s)
    {
        if(resourceBundle.containsKey(s))
        {
            String text = resourceBundle.getString(s);
            if(text != null) return text;
            else return s;
        }
        else
        {
            return s;
        }
    }

    private static boolean minimized = false;
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        if(args.length > 0 && args[0].equalsIgnoreCase("min"))
        {
            minimized = true;
        }
        String rcFile = System.getProperty("user.home") + "/.AnySync/anysync.rc";
        if(!new File(rcFile).exists())
        {
            ChooseEdition.show();
            try
            {
                synchronized(Main.class)
                {
                    Main.class.wait();
                }
            }
            catch(InterruptedException e){ }
        }
        Platform.setImplicitExit(false);//so don't exit even though there's no window
        setupLogger();
        Httpd.start(HTTP_PORT);
        NetUtil.HttpReturn httpReturn = NetUtil.syncSendGetCommand("echo", null, true);
        if(httpReturn.code != 501)
        {
            initTray();
            launch(args);
        }
    }

    private static ContextMenu contextMenu;
    private static void initTray()
    {
        try
        {
            if(!SystemTray.isSupported()) return;
            contextMenu = new ContextMenu();

            javafx.scene.control.MenuItem item;
            item = new javafx.scene.control.MenuItem();
            item.setOnAction(e -> System.exit(0));
            item.setText("Exit");
            contextMenu.getItems().add(item);
            item = new javafx.scene.control.MenuItem();
            item.setOnAction(e -> System.exit(0));
            item.setText("AnySync (Build " + BUILD + ")");
            item.setDisable(true);
            contextMenu.getItems().add(item);

            ImageIcon image = new ImageIcon(getResource("/images/app16.png"));
            TrayIcon trayIcon = new TrayIcon(image.getImage(), "AnySync Client");
            trayIcon.addMouseListener(new MouseAdapter(){
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e)
                {
                    Platform.runLater(() -> {
                        currentStage.show();
                        currentStage.toFront();
                        if(SwingUtilities.isRightMouseButton(e))
                        {
                            if(null != contextMenu && !contextMenu.isShowing())
                            {
                                double h = contextMenu.getHeight();
                                contextMenu.show(currentStage, e.getX(), e.getY() - h);
                            }
                        }
                    });
                }
            });


            if(SystemTray.isSupported())
            {
                SystemTray tray = SystemTray.getSystemTray();
                tray.add(trayIcon);
            }
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
    }
    
    public interface Status
    {
        void setStatus(String msg);
    }
    private static  Status _status;
    public static void setStatus(Status s)
    {
        _status =s;
    }
    public static void setStatus(String msg)
    {
        if(_status != null)
        {
            Platform.runLater(() ->_status.setStatus(msg));
        }
    }
    private static void setupLogger()
    {
        SimpleLayout layout = new SimpleLayout();
        String dir = System.getProperty("user.home") + "/.AnySync/logs/";
        File f = new File(dir);
        if(!f.exists()) f.mkdirs();
        String logFile = dir + "client.log";
        try
        {
            RollingFileAppender appender = new RollingFileAppender(layout, logFile, true);
            appender.setMaxFileSize("10MB");
            appender.setLayout(new PatternLayout("%d{HH:mm:ss}  %-5.5p  %t %m%n"));
            Logger.getRootLogger().addAppender(appender);
            Logger.getRootLogger().setLevel(Level.INFO);
            log.info("App starting...");
        }
        catch(IOException e)
        {
            System.err.println(e);
        }
    }

    private static boolean isOfficial;
    public static void setOfficial(boolean b)
    {
        isOfficial = b;
    }
    public static boolean isOfficial ()
    {
        return isOfficial;
    }
}
