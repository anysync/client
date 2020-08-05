// Copyright (c) 2020, Yanbin (Henry) Zheng <ybzheng@gmail.com>
// All rights reserved.
//
// Use of this source code is governed by a AGPLv3 license that can be
// found in the LICENSE file.
package net.anysync.ui;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import net.anysync.util.AppUtil;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

public class FileBrowserMain
{
	final static Logger log = LogManager.getLogger(FileBrowserMain.class);
	public static void open(Stage stage, String name, String folderHash, String localFolder)
	{
		stage.setTitle(Main.getString("app_name"));
		stage.getIcons().add(Main.getImage("/images/app128.png"));
		stage.setScene(createScene(loadMainPane(name, folderHash, localFolder)));
		stage.show();
	}

	private static FileBrowserMainController controller;
	private static Pane loadMainPane(String name, String folderHash, String localFolder)
	{
		FXMLLoader fxmlLoader = new FXMLLoader();
		Pane pane = null;
		try
		{
			fxmlLoader.setResources(ResourceBundle.getBundle(AppUtil.I18N, Locale.getDefault()));
			fxmlLoader.setLocation(Main.getResource("/fxml/FileBrowser.fxml"));
			pane =  fxmlLoader.load();
			controller = fxmlLoader.getController();
			controller.setFolderHash(name, folderHash, localFolder);
		}
		catch(IOException ioe)
		{
			log.error("IOException occurred. loadMainPane ::", ioe);
		}
		return pane;
	}

	public static FileBrowserMainController getFileBrowserController()
	{
		return controller;
	}

	private static  Scene createScene(Pane mainPane)
	{
		Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
		Scene scene = new Scene(mainPane, 1100, 600);
		return scene;
	}
}
