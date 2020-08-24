// Copyright (c) 2020, Yanbin (Henry) Zheng <ybzheng@gmail.com>
// All rights reserved.
//
// Use of this source code is governed by a AGPLv3 license that can be
// found in the LICENSE file.
package net.anysync.ui;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToolBar;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import net.anysync.util.NetUtil;
import net.anysync.util.UiUtil;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.Map;

public class MainPane extends BorderPane
{
    private final ImageView logoImageView = new ImageView();
    private final BorderPane borderPane = new BorderPane();

    private final VBox topVBox = new VBox();
    private final BorderPane centerBorderPane = new BorderPane();
    private RepoListView centerPane;
    private Label status = new Label();


    private final VBox bottomBox = new VBox();
    private final ToolBar functionBox = new ToolBar();
    private Stage stage;
    public MainPane(Stage s)
    {
        stage = s;
        init();
    }

    private void init()
    {
        centerPane = new RepoListView(null);
        centerPane.setStyle("-fx-background-color: null;");
        FontIcon repoIcon = new FontIcon();
        repoIcon.setIconLiteral("fth-list");
        repoIcon.setIconSize(32);
        Label titleLabel = new Label(Main.getString("repositories")/*, repoIcon  doesn't work properly on Windows*/);
        DropShadow ds = new DropShadow();
        ds.setOffsetY(3.0f);
        ds.setColor(Color.color(0.4f, 0.4f, 0.4f));
        titleLabel.setEffect(ds);
        titleLabel.setStyle("-fx-text-fill: rgba(13, 36, 129,1); -fx-font: 28px Tahoma;");
        repoIcon.setIconColor( Color.rgb(13, 36, 129, 1));
        centerBorderPane.setCenter(centerPane);
        status.setWrapText(true);
        centerBorderPane.setBottom(status);
        Region region1 = new Region();
        HBox.setHgrow(region1, Priority.ALWAYS);

        Region region2 = new Region();
        HBox.setHgrow(region2, Priority.ALWAYS);


        HBox titleHBox = new HBox(region1, titleLabel, region2);
        titleHBox.setPadding(new Insets(5, 5, 10, 5));
        titleHBox.setPrefHeight(40);
        titleHBox.setAlignment(Pos.CENTER_LEFT);

        topVBox.getChildren().add(titleHBox);



        borderPane.setTop(topVBox);
        borderPane.setCenter(centerBorderPane);
        borderPane.setBottom(bottomBox);
        setCenter(borderPane);

        functionBox.setOnMouseEntered(m -> functionBox.setCursor(Cursor.DEFAULT));

        FontIcon icon = new FontIcon();
        icon.setIconLiteral("fth-plus-circle");
        icon.setIconSize(24);
        icon.setIconColor(javafx.scene.paint.Color.WHITE);
        icon.setOnMouseClicked(event -> {
            AddRepoPane pane = new AddRepoPane(null, true);
            pane.show(this);
        });
        Button btn = new Button();
        UiUtil.setTooltip(btn, "Add repository");

        btn.setGraphic(icon);
        btn.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
        functionBox.getItems().add(btn);

        icon = new FontIcon();
        icon.setIconLiteral("fth-settings");
        icon.setIconSize(22);
        icon.setIconColor(javafx.scene.paint.Color.WHITE);
        icon.setOnMouseClicked(event -> {
            new Thread(() -> {
                Map<String, String> props = NetUtil.loadSettings();
                Platform.runLater(()-> {
                    settings = props;
                    SettingsPane pane = new SettingsPane(stage, settings);
                    pane.show();
                });
            }).start();
        });
        btn = new Button();
        UiUtil.setTooltip(btn, "Show settings window");
        btn.setGraphic(icon);
        btn.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");

        icon = new FontIcon();
        icon.setIconLiteral("fth-rotate-cw");
        icon.setIconSize(22);
        icon.setIconColor(javafx.scene.paint.Color.WHITE);
        icon.setOnMouseClicked(event -> {
            NetUtil.syncSendGetCommand("rescan", null, true);
        });
        Button btn2 = new Button();
        btn2.setGraphic(icon);
        btn2.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
        UiUtil.setTooltip(btn2, "Rescan all repositories");
        HBox rightFunctionBox = new HBox(btn2, btn);
        rightFunctionBox.setSpacing(10);

        

        BorderPane bottomBorderPane = new BorderPane();
        bottomBorderPane.setCenter(functionBox);
        bottomBorderPane.setRight(rightFunctionBox);
        bottomBox.getChildren().add(bottomBorderPane);

        functionBox.setStyle("-fx-background-color:rgba(13, 36, 129, 1)");
        rightFunctionBox.setStyle("-fx-background-color:rgba(13, 36, 129, 1)");
        rightFunctionBox.setAlignment(Pos.CENTER);

        rightFunctionBox.setPadding(new Insets(1, 10, 1, 1));

        new Thread(() -> {
            setRepos();
        }).start();
    }
    private Map<String,String> settings;

    public void setRepos()
    {
        while(true)
        {
            Map<String, String> props = NetUtil.loadSettings();
            if(props.size() == 0)
            {
                try
                {
                    Thread.sleep(100);
                }
                catch(InterruptedException e)
                {
                }
            }
            else
            {
                settings = props;
                break;
            }
        }
        Platform.runLater(() -> centerPane.setData(settings));
    }
    
    public Main.Status getStatus()
    {
        return msg -> status.setText(msg);
    }
}
