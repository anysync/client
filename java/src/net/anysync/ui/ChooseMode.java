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
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import net.anysync.util.NetUtil;
import net.anysync.util.UiUtil;


import java.util.HashMap;
import java.util.Map;

public class ChooseMode extends BorderPane  implements UiUtil.DialogSetter
{
    public final static int WIDTH = 750;
    public final static int HEIGHT = 300;
    private TextField[] _localFolders;
    private Label [] _labels;
    private Button[] _buttons;

    private Label _errorField;
    private RadioButton _sync;
    private Stage _stage;
    private RadioButton _newOnly;

    public ChooseMode(Stage s)
    {
        _stage = s;
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
        col1.setPercentWidth(45);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(40);
        ColumnConstraints col3 = new ColumnConstraints();
        col3.setPercentWidth(15);
        pane.getColumnConstraints().addAll(col1, col2, col3);
        pane.setPadding(new Insets(5, 5, 5, 5));

        Text text = new Text();
        text.setText(Main.getStringWithColon("Mode"));
        text.setStyle("-fx-font-size:16pt; -fx-font-weight: bold;");
        pane.add(text, 0, 0, 3, 1);

        RadioButton placeholder = new RadioButton("Placeholder Mode - Just a placeholder and no files will be downloaded");
        placeholder.setSelected(true);
//        UiUtil.setTooltip(placeholder, "Just a placeholder and no files will be downloaded");
        pane.add(placeholder, 0, 1, 3, 1);

        RadioButton sync = new RadioButton("Sync Mode - Bidirectional sync");
        _sync = sync;
        sync.setSelected(false);
//        UiUtil.setTooltip(sync, "Keep existing files under the folder and download all files from cloud. Existing\n      files in the local directory will be synced later.");
        pane.add(sync, 0, 2, 3, 1);

        _newOnly = new RadioButton("New Only Mode - Only track new files and ignore other local file changes");
        _newOnly.setSelected(false);
//        UiUtil.setTooltip(_newOnly, "Only track new files and ignore other local file changes.");
        pane.add(_newOnly, 0, 3, 3, 1);

        final ToggleGroup group = new ToggleGroup();
        placeholder.setToggleGroup(group);
        _sync.setToggleGroup(group);
        _newOnly.setToggleGroup(group);

        String[] repos = NetUtil.getRepos();
        _localFolders = new TextField[repos.length];
        _buttons = new Button[repos.length];
        _labels = new Label[repos.length];

        int start = 4;
        for(int i = 0; i < repos.length; i++)
        {
            TextField localFolder = new TextField();
            localFolder.setPromptText("Local folder to be synced");
            localFolder.setVisible(false);
            localFolder.setEditable(false);
            Label label = new Label("For repository \"" + repos[i] + "\"");
            label.setVisible(false);
            pane.add(label, 0, start);
            pane.add(localFolder, 1, start);
            Button btn = new Button("Choose");
            btn.setVisible(false);
            btn.setOnAction(e -> {
                AddRepoPane.chooseDirectory(localFolder, this.getScene().getWindow());
            });
            pane.add(btn, 2, start);
            start ++;
            _localFolders [i] = localFolder;
            _labels [i] = label;
            _buttons [i] = btn;
        }


        _errorField = new Label();
        _errorField.setTextFill(Color.TOMATO);
        pane.add(_errorField,0, 4, 3, 1);

        placeholder.selectedProperty().addListener((observable, oldValue, newValue) ->{
            _errorField.setText("");
        });
        sync.selectedProperty().addListener((observable, oldValue, newValue) -> {
            for(int i = 0; i < _localFolders.length; i++)
            {
                _localFolders[i].setVisible(newValue);
                _buttons[i].setVisible(newValue);
                _labels[i].setVisible(newValue);
            }
        });
        _newOnly.selectedProperty().addListener((observable, oldValue, newValue) -> {
            for(int i = 0; i < _localFolders.length; i++)
            {
                _localFolders[i].setVisible(newValue);
                _buttons[i].setVisible(newValue);
                _labels[i].setVisible(newValue);
            }
        });

    }

    public void show()
    {
        UiUtil.createDialog(this, Main.getString("Choose Mode"), WIDTH, HEIGHT + 60, this, false);
    }

    public void setDialog(Dialog d)
    {
        DialogPane dialogPane = d.getDialogPane();
        final Button btOk = (Button) dialogPane.lookupButton(ButtonType.OK);
        btOk.addEventFilter(
                ActionEvent.ACTION,
                event -> {
                    if(_sync.isSelected() || _newOnly.isSelected())
                    {
                        for(int i = 0; i < _localFolders.length; i++)
                        {
                            if(_localFolders[i].getText().trim().length() == 0)
                            {
                                _localFolders[i].requestFocus();
                                _errorField.setText("Error: Local folder is required in sync mode. Press 'Choose' button to select one.");
                                event.consume();
                                return;
                            }
                        }
                    }

                    updateLocal();
                    LoginController.openMainPane(_stage);
                }
        );

    }

    private boolean updateLocal()
    {
        Map<String, String> params = new HashMap<>();
        for(int i = 0; i < _localFolders.length; i++)
        {
            params.put("local" + String.valueOf(i), _localFolders[i].getText().trim());

        }
        String m;
        if(_sync.isSelected())
        {
            m = "s";
        }
        else if(_newOnly.isSelected())
        {
            m = "n";
        }
        else
        {
            m = "p";
        }
        params.put("mode", m);
        NetUtil.HttpReturn ret = NetUtil.syncSendGetCommand("updatelocal", params, false);
        return ret.code == 200;
    }
}
