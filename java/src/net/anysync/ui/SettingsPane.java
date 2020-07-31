// Copyright (c) 2020, Yanbin (Henry) Zheng <ybzheng@gmail.com>
// All rights reserved.
//
// Use of this source code is governed by a AGPLv3 license that can be
// found in the LICENSE file.
package net.anysync.ui;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Pair;
import net.anysync.util.NetUtil;
import net.anysync.util.StringUtil;
import net.anysync.util.UiUtil;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.awt.*;
import java.io.File;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

;

public class SettingsPane extends BorderPane implements UiUtil.DialogSetter
{
    final static Logger log = LogManager.getLogger(SettingsPane.class);

    public final static int WIDTH = 750;
    public final static int HEIGHT = 600;
    private Dialog settingDialog;
    private TextField _bandwidth;
    private TextField _maxSize;
    private TextField _minAge;
    private TextField _maxAge;
    private TextField _threadCount;
    private TextArea _includes;
    private TextArea _excludes;
    private ComboBox _modes;
    Map<String,String> settings;
    Stage mainStage;

    public SettingsPane(Stage st, Map<String, String> s)
    {
        mainStage = st;
        settings = s;
        init();
    }

    private void init()
    {
        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Tab general = new Tab("General");
        Tab account = new Tab("Account");
//        Tab proxy = new Tab("Proxy");
        tabs.getTabs().addAll(general, account);
        tabs.getStyleClass().add("floating");
        createGeneralTab(general);
        createAccountTab(account);
        setCenter(tabs);
        setPrefSize(WIDTH, HEIGHT);
        //setBottom(createSouthPane());

    }

    public void show()
    {
        if(UiUtil.createDialog(this, Main.getString("Settings"), WIDTH, HEIGHT + 60, this, true))
        {
            settings.put("rate", _bandwidth.getText().trim());
            settings.put("maxsize", _maxSize.getText().trim());
            settings.put("minage",_minAge.getText().trim());
            settings.put("maxage", _maxAge.getText().trim());
            settings.put("threadcount", _threadCount.getText().trim());
            settings.put("included", _includes.getText().trim());
            settings.put("excluded", _excludes.getText().trim());
            settings.put("mode", String.valueOf(_modes.getSelectionModel().getSelectedIndex()));
            NetUtil.updateSettings(settings);
        }

    }

    private void createGeneralTab(Tab tab)
    {
        GridPane pane = new GridPane();
        HBox.setHgrow(pane, Priority.ALWAYS);
        pane.setAlignment(Pos.TOP_CENTER);
        pane.setHgap(10);
        pane.setVgap(10);
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(35);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(65);
        pane.getColumnConstraints().addAll(col1, col2);
        pane.setPadding(new Insets(25, 25, 25, 25));
        Label lab = new Label(Main.getStringWithColon("Mode"));
        pane.add(lab, 0, 1);
        ObservableList<String> options = FXCollections.observableArrayList("Sync - Upload & download", "Placehold");
        _modes = new ComboBox(options);
        _modes.valueProperty().addListener((ChangeListener<String>) (component, oldValue, newValue) -> {
            if(oldValue != null && !newValue.equals(oldValue) && newValue.equals(options.get(0)))
            {
                signout("Complete sign out is required for this change. Proceed and sign out NOW?");
//                DirectoryChooser fileChooser = new DirectoryChooser();
//                fileChooser.setTitle("Choose a local directory for syncing");
//                File f = fileChooser.showDialog(this.getScene().getWindow());
//                System.out.println("Choosen:" + f.getAbsolutePath());
            }
        });
        HBox.setHgrow(_modes, Priority.ALWAYS);
        int mIndex = 0;
        String m = settings.get("mode");
        if(m != null)
        {
            mIndex = Integer.parseInt(m);
        }
        _modes.getSelectionModel().select(mIndex);

        pane.add(_modes, 1, 1);

        _bandwidth = addTextLine(pane, 2, "Bandwidth limit (MBytes/s)", "rate");
        _maxSize = addTextLine(pane, 3, "Maximum file size (GBytes)", "maxsize");
        _minAge =addTextLine(pane, 4, "Minimum file age (seconds)", "minage");
        _maxAge =addTextLine(pane, 5, "Maximum file age (days)", "maxage");
        _threadCount =addTextLine(pane, 6, "Number of upload threads", "threadcount");

        lab = new Label(Main.getStringWithColon("Include files only"));
        pane.add(lab, 0, 7);
        _includes = new TextArea(settings.get("included"));
        _includes.setWrapText(true);
        pane.add(_includes, 0, 8, 2, 1);

        lab = new Label(Main.getStringWithColon("Ignored files"));
        pane.add(lab, 0, 9);
        _excludes = new TextArea(settings.get("excluded"));
        _excludes.setWrapText(true);
        pane.add(_excludes, 0, 10, 2, 1);

        tab.setContent(pane);
    }

    private void createAccountTab(Tab tab)
    {
        GridPane pane = new GridPane();
        HBox.setHgrow(pane, Priority.ALWAYS);
        pane.setAlignment(Pos.TOP_CENTER);
        pane.setHgap(10);
        pane.setVgap(10);
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(35);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(65);
        pane.getColumnConstraints().addAll(col1, col2);
        pane.setPadding(new Insets(25, 25, 25, 25));
        String server = settings.get("server");
        boolean isOfficialSite = server.contains("anysync.net");
        addLine(pane, 1, "Server", "server");
        addLine(pane, 2, "Account Name", "email");
        addLine(pane, 3, "Device ID", "deviceID");
        addLine(pane, 4, "Repositories", "repos");
        addLine(pane, 5, "Local Directories", "localFolders");
        addLine(pane, 6, "Cloud Storage", "cloudStorage");
        String val = settings.get("acct");
        if(isOfficialSite)
        {
            if("0".equals(val))
            {
                val = "Trial account";
            }
            else
            {
                val = "Account";
            }
            String quota = settings.get("quota");
            if(quota == null) quota = "50";
            val += " with quota of " + quota + " GB.";
        }
        else
        {
            val = "Self hosted account";
        }
        addLineValue(pane, 7, "Account", val);
        addLine(pane, 8, "Expiry", "expiry");

        Button btn = new Button(Main.getString("Change Password"));
        btn.setPrefSize(120, 30);
        btn.setOnAction(e->{
            changePassword();
        });
        pane.add(btn, 0, 9);

        if(Main.isOfficial())
        {
            btn = new Button(Main.getString("Choose Plan"));
            btn.setPrefSize(120, 30);
            btn.setOnAction(e -> {
                choosePlan();
            });
            pane.add(btn, 0, 10);
        }

        btn = new Button(Main.getString("Sign out"));
        btn.setPrefSize(120, 30);
        btn.setOnAction(e -> {
            signout("Sign out and then sign in again. Proceed anyway?");
        });
        pane.add(btn, 0, 11);

        tab.setContent(pane);
    }

    private void addLine(GridPane pane, int lineNumber, String label, String key)
    {
        Label lab1 = new Label(Main.getStringWithColon(label));
        pane.add(lab1, 0, lineNumber);
        Label lab2 = new Label(settings.get(key));
        pane.add(lab2, 1, lineNumber);
    }

    private TextField addTextLine(GridPane pane, int lineNumber, String label, String key)
    {
        Label lab1 = new Label(Main.getStringWithColon(label));
        pane.add(lab1, 0, lineNumber);
        TextField text = new TextField(settings.get(key));
        pane.add(text, 1, lineNumber);
        return text;
    }

    private void addLineValue(GridPane pane, int lineNumber, String label, String value)
    {
        Label lab1 = new Label(Main.getStringWithColon(label));
        pane.add(lab1, 0, lineNumber);
        Label lab2 = new Label(value);
        pane.add(lab2, 1, lineNumber);
    }

    private void changePassword()
    {
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Change Password");
        dialog.setHeaderText(" ");

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialogPane.getStylesheets().add(Main.getResource("/css/main.css").toExternalForm());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        PasswordField oldPassword = new PasswordField();
        oldPassword.setPromptText("Old password");
        PasswordField password = new PasswordField();
        PasswordField password2 = new PasswordField();
        password.setPromptText("New password");

        grid.add(new Label(Main.getStringWithColon("Old Password")), 0, 0);
        grid.add(oldPassword, 1, 0);
        grid.add(new Label(Main.getStringWithColon("New Password")), 0, 1);
        grid.add(password, 1, 1);
        grid.add(new Label(Main.getStringWithColon("Re-enter Password")), 0, 2);
        grid.add(password2, 1, 2);

        Node loginButton = dialog.getDialogPane().lookupButton(ButtonType.OK);
        loginButton.setDisable(true);

        oldPassword.textProperty().addListener((observable, oldValue, newValue) -> {
            loginButton.setDisable(newValue.trim().isEmpty());
        });

        dialogPane.setContent(grid);
        UiUtil.centerButtons(dialogPane);
        Platform.runLater(() -> oldPassword.requestFocus());

        final Button btOk = (Button) dialogPane.lookupButton(ButtonType.OK);
        btOk.addEventFilter(
                ActionEvent.ACTION,
                event -> {
                    if(!checkNumericField(_bandwidth) || !checkNumericField(_maxAge) || !checkNumericField(_maxSize) || !checkNumericField(_minAge) || !checkNumericField(_threadCount))
                    {
                        event.consume();
                        return;
                    }
                    if(!password.getText().trim().equals(password2.getText().trim()))
                    {
                        Platform.runLater(() -> password.requestFocus());
                        dialog.setHeaderText("Passwords do not match");
                        event.consume();
                        return;
                    }

                    Map<String,String>params = new HashMap<>();
                    params.put("old", oldPassword.getText().trim());
                    params.put("new", password.getText().trim());
                    NetUtil.HttpReturn ret = NetUtil.syncSendGetCommand("updatepassword", params, false);
                    if(ret.code != 200 ||  "NO".equals(ret.response))
                    {
                        dialog.setHeaderText("Error occurred. Password is not updated.");
                        event.consume();
                        return;
                    }
                }
        );
        dialog.showAndWait();
    }
    boolean checkNumericField(TextField f)
    {
        String val = f.getText().trim();
        if(!val.isEmpty() && !StringUtil.isNumeric(val)) return false;
        else return true;
    }

    private void signout(String msg)
    {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.getDialogPane().getStylesheets().add(Main.getResource("/css/main.css").toExternalForm());
        alert.setTitle("");alert.setHeaderText("");
        alert.setContentText(msg);//"Sign out and restart, then you need to sign in again. Proceed anyway?");
        UiUtil.centerButtons(alert.getDialogPane());

        Optional<ButtonType> result = alert.showAndWait();
        if(result.get() != ButtonType.OK)
        {
            return;
        }
        File f = new File(System.getProperty("user.home") + "/.AnySync/current");
        f.delete();
        NetUtil.syncSendGetCommand("restart", null, false);
        settingDialog.close();
        mainStage.close();

        Stage stage = new Stage();
        Main main = new Main();
        try
        {
            main.start(stage);
        }
        catch(Exception e)
        {
            log.error(e);
        }
    }

    private  void choosePlan()
    {
        NetUtil.HttpReturn ret = NetUtil.syncSendGetCommand("getuser", null, false);
        if(ret.code != 200 || ret.response.length() == 0) return;
        String url = "https:///anysync.net/cloudorder.php?" + ret.response;
        try
        {
            Desktop.getDesktop().browse(new URI(url));
        }
        catch(Exception e)
        {
        }
    }

    @Override
    public void setDialog(Dialog d)
    {
        settingDialog = d;
    }
}