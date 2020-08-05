// Copyright (c) 2020, Yanbin (Henry) Zheng <ybzheng@gmail.com>
// All rights reserved.
//
// Use of this source code is governed by a AGPLv3 license that can be
// found in the LICENSE file.
package net.anysync.util;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import net.anysync.ui.Main;

import java.util.Map;
import java.util.Optional;

public class UiUtil implements java.io.Serializable
{
    public interface DialogSetter
    {
        void setDialog(Dialog d);
    }
    public static boolean createDialog(Node content, String title, double width, double height, DialogSetter setter, boolean hasCancelBtn)
    {
        Dialog<ButtonType> dialog = new Dialog<>();

        Object o = dialog.getDialogPane().getScene().getWindow();
        if(o != null && o instanceof Stage)
        {
            ((Stage)o).getIcons().add(Main.getImage("/images/app128.png"));
        }
        dialog.setTitle(title);
        if(hasCancelBtn) dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);
        else dialog.getDialogPane().getButtonTypes().addAll( ButtonType.OK);
        dialog.getDialogPane().setContent(content);
        dialog.setResizable(true);

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setPrefSize(width, height);
        dialogPane.getStylesheets().add(Main.getResource("/css/main.css").toExternalForm());
        centerButtons(dialogPane);
        dialog.getDialogPane().getScene().getWindow().setOnShown(event -> {
            //this method is invoked right before it's visible, and after layout is done.
            if(setter != null)
            {
                setter.setDialog(dialog);
            }
        });

        Optional<ButtonType> result = dialog.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    public static void centerButtons(DialogPane dialogPane)
    {
        Region spacer = new Region();
        ButtonBar.setButtonData(spacer, ButtonBar.ButtonData.BIG_GAP);
        HBox.setHgrow(spacer, Priority.ALWAYS);
        dialogPane.applyCss();
        HBox hboxDialogPane = (HBox) dialogPane.lookup(".container");
        hboxDialogPane.getChildren().add(spacer);
    }

    public static void messageBox(Alert.AlertType t, String title, String msg)
    {
        Platform.runLater(() -> {
            Alert alert = new Alert(t);
            alert.setHeaderText("");
            alert.setTitle(title);
            alert.setContentText(msg);
            centerButtons(alert.getDialogPane());
            alert.showAndWait();
        });
    }

    public static void fatalError(String title, String msg)
    {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("");
            alert.setTitle(title);
            alert.setContentText(msg);
            UiUtil.centerButtons(alert.getDialogPane());
            alert.showAndWait();
            System.exit(1);
        });
    }

    public static void setTooltip(Control n, String tip)
    {
        Tooltip tooltip = new Tooltip();
        tooltip.setText(tip);
        tooltip.setStyle("-fx-background-color: aquamarine; -fx-text-fill: black");
        n.setTooltip(tooltip);

    }

    public static void showPropertiesDialog(String title, Map<String,String> props, int width, int height)
    {
        BorderPane borderPane = new BorderPane();
        GridPane pane = new GridPane();
        borderPane.setCenter(pane);
        HBox.setHgrow(pane, Priority.ALWAYS);
        pane.setAlignment(Pos.TOP_CENTER);
        pane.setHgap(10);
        pane.setVgap(10);
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(25);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(75);
        pane.getColumnConstraints().addAll(col1, col2);
        pane.setPadding(new Insets(5, 5, 5, 5));

        int lineNumber = 0;
        for(Map.Entry<String,String> entry : props.entrySet())
        {
            pane.add(new Label(entry.getKey() + ":" ), 0, lineNumber);
            pane.add(new Label(entry.getValue()), 1, lineNumber);
            lineNumber++;
        }
        UiUtil.createDialog(borderPane, title, width, height, null, false);
    }

}
