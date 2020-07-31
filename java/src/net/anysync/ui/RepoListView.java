// Copyright (c) 2020, Yanbin (Henry) Zheng <ybzheng@gmail.com>
// All rights reserved.
//
// Use of this source code is governed by a AGPLv3 license that can be
// found in the LICENSE file.
package net.anysync.ui;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.SelectionMode;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import net.anysync.model.FileData;
import net.anysync.util.NetUtil;
import net.anysync.util.Tokenizer;
import net.anysync.util.UiUtil;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;


public class RepoListView extends javafx.scene.control.ListView<String> {

    private ObservableList<String> mChildrenList;
    private static String [] _items;
    private static Map<String,String> _localFolders = new HashMap<>();
    private static Map<String, String> _repoHashes = new HashMap<>();
    private static Map<String, String> _repoEncrypted= new HashMap<>();
    private static Stage stage;
    private static int currentOpenIndex = -1;
    public RepoListView(String [] items, String[] itemData) {
        super();
        _items = items;
        getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        mChildrenList = FXCollections.observableArrayList();
        setItems(mChildrenList);

        setBorder(null);

        setOnKeyPressed(key -> {
            switch (key.getCode()) {
                case ENTER:
//                    if (isFocused()) navigate(getSelectionModel().getSelectedItem());
                    break;
                case BACK_SPACE:
//                    back();
                    break;
            }
        });

        setCellFactory(list ->{
            ListCell<String> cell = new RepoListCell(RepoListView.this);
            cell.setOnMouseClicked(e -> {
                if(!cell.isEmpty() && e.getClickCount() == 1)
                {
                    int index = getSelectionModel().getSelectedIndex();
                    if(index != currentOpenIndex || (stage != null && !stage.isShowing()))
                    {
                        currentOpenIndex = index;
                        if(stage == null)
                        {
                            stage = new Stage();//Stage) node.getScene().getWindow();
                        }
                        else stage.close();
                        FileBrowserMain.open(stage, _items[index], _repoHashes.get(_items[index]), _localFolders.get(_items[index]));
                        stage.show();
                    }
                    else
                    {
                        if(stage != null && stage.isShowing())
                        {
                            stage.toFront();
                        }
                    }
                }
            });
            return cell;
        });
        refresh();
    }

    public static class RepoListCell extends ListCell<String>
    {
        private RepoListView _mRepoListView;

        public RepoListCell(RepoListView repoListView)
        {
            _mRepoListView = repoListView;
        }

        @Override
        public void updateItem(String item, boolean empty)
        {
            super.updateItem(item, empty);
            if(empty)
            {
                setGraphic(null);
                setText(null);
            }
            else
            {
                Image fxImage = Main.getImage("/images/library.png");// getFileIcon(mListView.getDirectory().resolve(item).toString());
                ImageView imageView = new ImageView(fxImage);
                imageView.setFitHeight(40);
                imageView.setFitWidth(40);
//                setGraphic(imageView);
//                setText("  " + item);
//                setFont(Font.font("Tahoma", 18));

                setText(null);
                setBorder(null);
                GridPane pane = new GridPane();
                setGraphic(pane);
                HBox.setHgrow(pane, Priority.ALWAYS);
                pane.setAlignment(Pos.TOP_CENTER);
                pane.setHgap(10);
                pane.setVgap(10);
                ColumnConstraints col1 = new ColumnConstraints();
                col1.setPercentWidth(15);
                ColumnConstraints col2 = new ColumnConstraints();
                col2.setPercentWidth(60);
                ColumnConstraints col3 = new ColumnConstraints();
                col3.setPercentWidth(25);
                pane.getColumnConstraints().addAll(col1, col2, col3);
                pane.setPadding(new Insets(5, 5, 5, 5));

                pane.add(imageView, 0, 0);
                Label label = new Label(item.toString());
                pane.add(label, 1, 0);
                imageView.setStyle("-fx-cursor: hand");
                label.setStyle("-fx-cursor: hand");

                FontIcon icon = new FontIcon();
                icon.setIconSize(20);
                icon.setIconLiteral("fth-info");
                icon.setIconColor(Color.rgb(99,99,99, 0.7));
                Button btn = new Button();
                btn.setUserData(item);
                UiUtil.setTooltip(btn, "Get repository info");
                btn.setGraphic(icon);
                btn.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
                btn.setOnAction(e -> getInfo(e.getSource()));

                icon = new FontIcon();
                icon.setIconSize(20);
                icon.setIconLiteral("fth-check-circle");
                icon.setIconColor(Color.rgb(99, 99, 99, 0.7));
                Button btn3 = new Button();
                UiUtil.setTooltip(btn3, "Verify if all files have been successfully uploaded to the cloud.");
                btn3.setUserData(item);
                btn3.setGraphic(icon);
                btn3.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
                btn3.setOnAction(e -> fullVerify(e.getSource()));

                HBox box  = new HBox(btn3,/* btn2, */btn);
                box.setAlignment(Pos.CENTER_RIGHT);
                box.setSpacing(4);
                pane.add(box, 2, 0);

            }
        }
    }

    private static void rescan(Object source)
    {
        Button btn = (Button) source;
        String folder = (String) btn.getUserData(); //local folder

        NetUtil.syncSendPutCommand("changes", folder);
    }

    private static void quickVerify(Object source)
    {
        Map<String,String> params = new HashMap<>();
        Button btn = (Button) source;
        String data = (String) btn.getUserData();
        String hash = _repoHashes.get(data);// data.substring(0, pos);
        params.put("hash", hash);
        NetUtil.syncSendGetCommand("qverify", params, false);
        Main.setStatus("Start verifying " + data);
    }

    private static void fullVerify(Object source)
    {
        Map<String, String> params = new HashMap<>();
        Button btn = (Button) source;
        String data = (String) btn.getUserData();
        String hash = _repoHashes.get(data);// data.substring(0, pos);
        params.put("hash", hash);
        NetUtil.syncSendGetCommand("verify", params, false);
        Main.setStatus("Start full verifying " + data);
    }

    public static void getInfo(Object source)
    {
        Button btn = (Button) source;
        btn.setDisable(true);
        new Thread(() -> {
            String data = (String) btn.getUserData();
            String hash = _repoHashes.get(data);// data.substring(0, pos);
            String local = _localFolders.get(data);
            String encrypted = _repoEncrypted.get(data);
            HashMap<String, String> params = new HashMap<>();
            params.put("hash", hash);
            NetUtil.HttpReturn ret = NetUtil.syncSendGetCommand("getsize", params, false);
            if(ret.code != 200) return;
            String[] lines = Tokenizer.parse(ret.response, '\n', true, true);
            long size = Long.parseLong(lines[0]);
            long count = Long.parseLong(lines[1]);

            Map<String, String> map = new LinkedHashMap<>();
            map.put("Size", FileData.getSizeString(size));
            map.put("Number of Files", lines[1]);
            map.put("Local Folder", local);
            map.put("Repository Hash", hash);
            map.put("Encrypted", encrypted);

            Platform.runLater(()-> UiUtil.showPropertiesDialog("Repository Info", map, 750, 300));
            btn.setDisable(false);
        }).start();
    }

    private static Map<String, String> settings;

    public void setData( Map<String, String> props)
    {
        settings = props;
        String[] repos = Tokenizer.parse(props.get("repos"), ',', true, true);
        String[] repoHashes = Tokenizer.parse(props.get("repoHashes"), ',', true, true);
        String[] locals = Tokenizer.parse(props.get("localFolders"), ',', true, true);
        String[] repoEncrypted = Tokenizer.parse(props.get("encrypted"), ',', true, true);
        _items = repos;
        _localFolders.clear();
        _repoHashes.clear();
        _repoEncrypted.clear();
        if(_items == null) return;
        for(int i = 0; i < _items.length; i++)
        {
            _localFolders.put(_items[i], locals[i]);
            _repoHashes.put(_items[i], repoHashes[i]);
            _repoEncrypted.put(_items[i], repoEncrypted[i]);
        }
        refresh();
    }

    public void refresh() {
        mChildrenList.clear();
        if(_items != null) mChildrenList.addAll(_items);
        getSelectionModel().select(0);
    }


}

