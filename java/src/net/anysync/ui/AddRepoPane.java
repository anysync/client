// Copyright (c) 2020, Yanbin (Henry) Zheng <ybzheng@gmail.com>
// All rights reserved.
//
// Use of this source code is governed by a AGPLv3 license that can be
// found in the LICENSE file.
package net.anysync.ui;

import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.layout.Priority;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import net.anysync.util.NetUtil;
import net.anysync.util.UiUtil;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class AddRepoPane extends BorderPane implements UiUtil.DialogSetter
{
    public final static int WIDTH = 750;
    public final static int HEIGHT = 300;
    String defaultRepoName;
    boolean hasCancelBtn;
    public AddRepoPane(String useRepoName, boolean hasCancel)
    {
        defaultRepoName = useRepoName;
        hasCancelBtn = hasCancel;
        init();
    }

    MainPane _mainPane;
    public void show(MainPane main)
    {
        _mainPane = main;
        if(UiUtil.createDialog(this, Main.getString("Add"), WIDTH, HEIGHT + 60, this, hasCancelBtn))
        {
            saveRepo();
        }
    }
    private TextField _localFolder;
    private TextField _repoName;
    private CheckBox _encrypt = new CheckBox("Encrypt Files on Remote.");
//    private ComboBox _remotes ;
    private void init()
    {
        GridPane pane = new GridPane();
        setCenter(pane);
        HBox.setHgrow(pane, Priority.ALWAYS);
        pane.setAlignment(Pos.TOP_CENTER);
        pane.setHgap(10);
        pane.setVgap(10);
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(30);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        ColumnConstraints col3 = new ColumnConstraints();
        col3.setPercentWidth(20);
        pane.getColumnConstraints().addAll(col1, col2, col3);
        pane.setPadding(new Insets(5, 5, 5, 5));
        pane.add(_encrypt, 0, 1, 3, 1);
        _encrypt.setSelected(true);
        _localFolder = addTextLine(pane, 2, "Local Folder");
        _localFolder.setEditable(false);
        Button btn = new Button("Choose");
        btn.setOnAction(e->{ chooseDirectory(_localFolder, this.getScene().getWindow()); });
        btn.setPrefSize(120, 30);
        pane.add(btn,2, 2);

        _repoName = addTextLine(pane, 3, "Repository Name");
        if(defaultRepoName != null)
        {
            _repoName.setText(defaultRepoName);
            _repoName.setEditable(false);
        }
    }

    public static  void chooseDirectory(TextField field, Window window)
    {
        DirectoryChooser fileChooser = new DirectoryChooser();
        fileChooser.setTitle("Choose a local directory for syncing");
        File f = fileChooser.showDialog(window);
        if(f != null) field.setText (f.getAbsolutePath());
    }

    @Override
    //invoked right before visible
    public void setDialog(Dialog d)
    {
        d.setTitle("Add Local Folder for Syncing");
        DialogPane dialogPane = d.getDialogPane();
        dialogPane.getScene().getWindow().addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, this::closeWindowEvent);
        final Button btOk = (Button) dialogPane.lookupButton(ButtonType.OK);
        btOk.addEventFilter(
                ActionEvent.ACTION,
                event -> {
                    if(_localFolder.getText().trim().length() == 0 || _repoName.getText().trim().length() == 0)
                    {
                        if(_localFolder.getText().trim().length() == 0) _localFolder.requestFocus();
                        if(_repoName.getText().trim().length() == 0) _repoName.requestFocus();
                        event.consume();
                        return;
                    }
                }
        );

    }

    private void closeWindowEvent(WindowEvent event)
    {//do not allow close
        event.consume();
    }


    private TextField addTextLine(GridPane pane, int lineNumber, String label)
    {
        Label lab1 = new Label(Main.getStringWithColon(label));
        pane.add(lab1, 0, lineNumber);
        TextField text = new TextField();
        text.setPrefSize(450, 28);
        pane.add(text, 1, lineNumber);
        return text;
    }

    private boolean saveRepo()
    {
        Map<String,String> params = new HashMap<>();
        params.put("local", _localFolder.getText().trim());
        params.put("name", _repoName.getText().trim());
        String e = _encrypt.isSelected() ? "on" :"off";
        params.put("encrypted", e);
        NetUtil.HttpReturn ret = NetUtil.syncSendGetCommand("saverepo", params, false);
        if(ret.code == 200 && _mainPane != null) _mainPane.setRepos();
        return ret.code == 200;
    }
}
