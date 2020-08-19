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
    private TextField _scanInterval;
    private TextArea _selectedFolders;
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
        if(UiUtil.createDialog(this, Main.getString("Settings"), WIDTH, HEIGHT + 60, this, true, false))
        {
            settings.put("rate", _bandwidth.getText().trim());
            settings.put("maxsize", _maxSize.getText().trim());
            settings.put("minage",_minAge.getText().trim());
            settings.put("maxage", _maxAge.getText().trim());
            settings.put("threadcount", _threadCount.getText().trim());
            settings.put("scaninterval", _scanInterval.getText().trim());
            settings.put("selectedfolders", _selectedFolders.getText().trim());
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
        col1.setPercentWidth(43);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(7);

        ColumnConstraints col3 = new ColumnConstraints();
        col3.setPercentWidth(43);
        ColumnConstraints col4 = new ColumnConstraints();
        col4.setPercentWidth(7);
        pane.getColumnConstraints().addAll(col1, col2, col3, col4);
        pane.setPadding(new Insets(25, 25, 25, 25));
        Label lab = new Label(Main.getStringWithColon("Mode"));
        pane.add(lab, 0, 1);
        ObservableList<String> options = FXCollections.observableArrayList("Sync - Upload & download", "Placehold", "New Only");
        _modes = new ComboBox(options);
        _modes.valueProperty().addListener((ChangeListener<String>) (component, oldValue, newValue) -> {
            if(oldValue != null && !newValue.equals(oldValue) && newValue.equals(options.get(0)))
            {
                signout("Complete sign out is required for this change. Proceed and sign out NOW?");
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

        pane.add(_modes, 1, 1, 4, 1);

        _bandwidth = addTextLine(pane, 2, "Bandwidth limit (MBytes/s)", "rate", true);
        _maxSize = addTextLine(pane, 2, "Maximum file size (GBytes)", "maxsize", false);
        _minAge =addTextLine(pane, 3, "Minimum file age (seconds)", "minage", true);
        _maxAge =addTextLine(pane, 3, "Maximum file age (days)", "maxage", false);
        _threadCount =addTextLine(pane, 4, "Number of upload threads", "threadcount", true);
        _scanInterval = addTextLine(pane, 4, "Scan Interval (minutes)", "scaninterval", false);

        lab = new Label(Main.getStringWithColon("Selective Sync Folders"));
        pane.add(lab, 0, 5);
        Button mod = new Button("Modify");
        mod.setOnAction(e->{
            SelectiveSync.open(_selectedFolders);
        });
        if(NetUtil.isNonSyncMode())
        {
            mod.setDisable(true);
        }
        pane.add(mod, 2, 5);
        _selectedFolders = new TextArea(settings.get("selectedfolders"));
        _selectedFolders.setWrapText(true);
        _selectedFolders.setEditable(false);
        pane.add(_selectedFolders, 0, 6, 4, 1);

        lab = new Label(Main.getStringWithColon("Include files only"));
        pane.add(lab, 0, 7, 4, 1);
        _includes = new TextArea(settings.get("included"));
        _includes.setWrapText(true);
        pane.add(_includes, 0, 8, 4, 1);

        lab = new Label(Main.getStringWithColon("Ignored files"));
        pane.add(lab, 0, 9, 4, 1);
        _excludes = new TextArea(settings.get("excluded"));
        _excludes.setWrapText(true);
        pane.add(_excludes, 0, 10, 4, 1);

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

    private TextField addTextLine(GridPane pane, int lineNumber, String label, String key, boolean left)
    {
        int index = left ? 0 : 2;
        Label lab1 = new Label(Main.getStringWithColon(label));
        pane.add(lab1, index, lineNumber);
        TextField text = new TextField(settings.get(key));
        pane.add(text, index + 1, lineNumber);
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
                    String p = password.getText().trim();
                    if(p.length() == 0 || !p.equals(password2.getText().trim()))
                    {
                        Platform.runLater(() -> password.requestFocus());
                        dialog.setHeaderText("Passwords are empty or they do not match");
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
        if(!val.isEmpty() && !StringUtil.isNumeric(val))
        {
            f.requestFocus();
            return false;
        }
        else return true;
    }

    boolean checkNumericField(TextField f, int minVal)
    {
        String val = f.getText().trim();
        if(!val.isEmpty() && !StringUtil.isNumeric(val))
        {
            f.requestFocus();
            return false;
        }
        try
        {
            int i = Integer.parseInt(val);
            if(i >= minVal) return true;
            else
            {
                f.requestFocus();
                return false;
            }
        }
        catch(Exception e)
        {
            f.requestFocus();
            return false;
        }
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
        final Button btOk = (Button) d.getDialogPane().lookupButton(ButtonType.OK);
        btOk.addEventFilter(
                ActionEvent.ACTION,
                event -> {
                    if(!checkNumericField(_bandwidth, 0) || !checkNumericField(_maxAge, 0) || !checkNumericField(_maxSize, 0)
                            || !checkNumericField(_minAge, 0) || !checkNumericField(_threadCount, 1) || !checkNumericField(_scanInterval, 1))
                    {
                        UiUtil.messageBox(Alert.AlertType.INFORMATION, "Error", "Invalid value.");
                        event.consume();
                        return;
                    }

                });
    }
}