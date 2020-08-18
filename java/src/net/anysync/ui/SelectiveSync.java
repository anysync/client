// Copyright (c) 2020, Yanbin (Henry) Zheng <ybzheng@gmail.com>
// All rights reserved.
//
// Use of this source code is governed by a AGPLv3 license that can be
// found in the LICENSE file.
package net.anysync.ui;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import net.anysync.util.AppUtil;
import net.anysync.util.UiUtil;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

import static net.anysync.util.AppUtil.NULL_HASH;

public class SelectiveSync
{
	final static Logger log = LogManager.getLogger(SelectiveSync.class);

	public static void open(TextArea foldersText)
	{
		UiUtil.createDialog(loadMainPane(), "Choose Selective Sync Folders", 300, 400, d -> {
			controller.setData("", NULL_HASH, "", d, foldersText);

			DialogPane dialogPane = d.getDialogPane();
			final Button btOk = (Button) dialogPane.lookupButton(ButtonType.OK);
			btOk.addEventFilter(ActionEvent.ACTION, event -> controller.okButtonClicked());
			final Button btCancel = (Button) dialogPane.lookupButton(ButtonType.CANCEL);
			btCancel.addEventFilter(ActionEvent.ACTION, event -> controller.cancelButtonClicked());
		}, true, false);
	}

	private static SelectiveSyncController controller;
	private static Pane loadMainPane()
	{
		FXMLLoader fxmlLoader = new FXMLLoader();
		Pane pane = null;
		try
		{
			fxmlLoader.setResources(ResourceBundle.getBundle(AppUtil.I18N, Locale.getDefault()));
			fxmlLoader.setLocation(Main.getResource("/fxml/SelectiveSync.fxml"));
			pane =  fxmlLoader.load();
			controller = fxmlLoader.getController();
		}
		catch(IOException ioe)
		{
			log.error("IOException occurred. loadMainPane ::", ioe);
		}
		return pane;
	}

//	private static  Scene createScene(Pane mainPane)
//	{
//		Scene scene = new Scene(mainPane, 800, 600);
//		return scene;
//	}
}
