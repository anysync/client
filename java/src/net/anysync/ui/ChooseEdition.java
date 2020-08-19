// Copyright (c) 2020, Yanbin (Henry) Zheng <ybzheng@gmail.com>
// All rights reserved.
//
// Use of this source code is governed by a AGPLv3 license that can be
// found in the LICENSE file.
package net.anysync.ui;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import net.anysync.util.FileUtil;
import net.anysync.util.UiUtil;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;

import static net.anysync.util.NetUtil.getJarPath;

public class ChooseEdition extends BorderPane  implements UiUtil.DialogSetter
{
    final static Logger log = LogManager.getLogger(ChooseEdition .class);

    public final static int WIDTH = 550;
    public final static int HEIGHT = 140;
    private CheckBox _isSelfEdition;

    public ChooseEdition()
    {
        init();
    }

    private void init()
    {
        GridPane pane = new GridPane();
        setCenter(pane);
        HBox.setHgrow(pane, Priority.ALWAYS);
        pane.setAlignment(Pos.TOP_CENTER);
        pane.setHgap(10);
        pane.setVgap(10);
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(WIDTH - 10);
        pane.getColumnConstraints().addAll(col1);
        pane.setPadding(new Insets(5, 5, 5, 5));

        CheckBox official = new CheckBox("Sync with anysync.net. No server installation required.");
        official.setSelected(true);
        UiUtil.setTooltip(official, "Sync with server at anysync.net");
        pane.add(official, 0, 1);
        CheckBox selfhosted = new CheckBox("Self hosted edition. Server needs to be configured.");
        UiUtil.setTooltip(selfhosted, "You need to download and install server.");
        _isSelfEdition = selfhosted;
        selfhosted.setSelected(false);
        pane.add(selfhosted, 0, 2);

        Button ok = new Button("Ok");
        ok.setOnAction(e -> {
            String dir = System.getProperty("user.home") + "/.AnySync/";
            String dest = dir + "anysync.rc";
            File f = new File(dir);
            if(!f.exists())
            {
                f.mkdir();
            }
            String path = getJarPath();
            String file = _isSelfEdition.isSelected()?"self.rc":"anysync.rc";
            System.out.println("file:" + file + "; iselected:" + _isSelfEdition.isSelected());
            try
            {
                FileUtil.copyFile(new File(path + "/" + file), new File(dest));
            }
            catch(IOException ie)
            {
                log.error("Cannot copy rc file to " + dest);
            }
            _stage.hide();
            synchronized(Main.class)
            {
                Main.class.notifyAll();
            }
        });
        setBottom(ok);
        ok.setPrefSize(100, 28);
        BorderPane.setMargin(ok, new Insets(20,20,20,20));
        setAlignment(ok, Pos.CENTER);

        official.selectedProperty().addListener((observable, oldValue, newValue) ->{
            selfhosted.setSelected(!newValue);
        });
        selfhosted.selectedProperty().addListener((observable, oldValue, newValue) -> {
            official.setSelected(!newValue);

        });

    }

    private static Stage _stage;
    public static void show()
    {
        Platform.runLater(()-> {
            Stage stage = new Stage();
            _stage = stage;
            stage.getIcons().add(Main.getImage("/images/app128.png"));
            ChooseEdition m = new ChooseEdition();
            Scene scene = new Scene(m, WIDTH, HEIGHT);
            stage.setScene(scene);
            stage.getScene().getStylesheets().add(Main.getResource("/css/main.css").toExternalForm());
            stage.getScene().getWindow().addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, windowEvent -> windowEvent.consume());
            stage.setTitle("Choose Mode");

            stage.show();
        });
    }

    public void setDialog(Dialog d)
    {
    }

}
