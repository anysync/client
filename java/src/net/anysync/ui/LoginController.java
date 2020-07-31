// Copyright (c) 2020, Yanbin (Henry) Zheng <ybzheng@gmail.com>
// All rights reserved.
//
// Use of this source code is governed by a AGPLv3 license that can be
// found in the LICENSE file.
package net.anysync.ui;


import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import net.anysync.util.AppUtil;
import net.anysync.util.Httpd;
import net.anysync.util.NetUtil;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * @author oXCToo
 */
public class LoginController implements Initializable
{

    @FXML
    private Label lblErrors;

    @FXML
    private TextField txtUsername;

    @FXML
    private TextField txtServer;

    @FXML
    private TextField txtPassword;

    @FXML
    private Button btnSignin;

    @FXML
    private Button btnSignup;

    ResourceBundle resources;

    @FXML
    public void handleButtonAction(MouseEvent event)
    {
        Node node = (Node) event.getSource();
        Stage stage = (Stage) node.getScene().getWindow();
        if(event.getSource() == btnSignin)
        {
            doSignin();
        }
        else if(event.getSource() == btnSignup)
        {
            URL url = Main.getResource("/fxml/Signup.fxml");
            FXMLLoader loader = new FXMLLoader(url);
            try
            {
                Parent root = loader.load();
                stage.setScene(new Scene(root));
                SignupController controller = loader.getController();
                Main.setStatus(controller.getStatus());
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
       }
    }

    @FXML
    public void onEnter(ActionEvent ae)
    {
        doSignin();
    }

    public void doSignin()
    {
        String username = txtUsername.getText().trim();
        if(username.length() == 0)
        {
            txtUsername.requestFocus();
            return;
        }
        String password = txtPassword.getText().trim();
        if(password.length() == 0)
        {
            txtPassword.requestFocus();
            return;
        }
        if(txtServer.getText().trim().length() == 0)
        {
            setError("Server cannot be empty.");
            return;
        }


        int ret = signin(username, password, txtServer.getText().trim(), false);
        if (ret == 401)
        {
            setError("User name or password are not correct.");
        }
        else if(ret == 405)
        {
            setError("Cannot connect to the server");
        }
    }

    public static void openMainPane(Stage stage)
    {
        try
        {
            stage.close();
            VBox root = new VBox();
            MainPane view = new MainPane(stage);
            stage.setTitle(Main.getString("app_name") + ". Build " + Main.BUILD);
            VBox.setVgrow(view, Priority.ALWAYS);
            root.getChildren().addAll(/*getMenuBar(),*/ view);
            root.getStylesheets().add(Main.getResource("/css/main.css").toString());
            Scene scene = new Scene(root, 500, 650);
            stage.setScene(scene);
            Main.setStatus(view.getStatus());
            stage.show();
        }
        catch(Exception ex)
        {
            System.err.println(ex.getMessage());
        }
    }

    public static void openChooseMode(Stage stage)
    {
        ChooseMode m = new ChooseMode(stage);
        m.show();
    }

//    private static MenuBar getMenuBar()
//    {
//        Menu fileMenu = new Menu(Main.getString("Tools"));
//        return new MenuBar(fileMenu);
//    }

    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
        this.resources = rb;
        txtServer.setText(AppUtil.getServerAddress());
    }

    private void setError(String msg)
    {
        lblErrors.setTextFill(Color.TOMATO);
        lblErrors.setText(msg);//Server Error : Check");
    }

    public LoginController()
    {
//        con = ConnectionUtil.conDB();
    }

    @FXML
    void initialize()
    {
        new Thread(() -> {
            Map<String, String> props = NetUtil.loadSettings();
            Platform.runLater(() -> {
                String server = props.get("server");
                if(server != null && server.length() > 0)
                {
                    txtServer.setText(server);
                }
            });
        }).start();
    }

    //we gonna use string to check for status
    public static int signin(String email, String password, String server, boolean isSignup)
    {
        Stage stage = (Stage) Main.getCurrentStage().getScene().getWindow();

        HashMap<String, String> map = new HashMap<>();
        map.put("e", email);
        map.put("p", password);
        map.put("s", server);
        String action = isSignup?"signup":"login";
        NetUtil.HttpReturn response = NetUtil.syncSendGetCommand(action, map, false);
        if(response.code == 200)
        {
            AddRepoPane pane = new AddRepoPane("AnySync", false);
            pane.show(null);
            openMainPane(stage);
        }
        else if(response.code == 201)
        {//restore account
            //wait for /askrestore: msg
            Httpd.waiting();
            Platform.runLater(() -> openChooseMode(stage));
        }
        return response.code;
    }

    private void setLblError(Color color, String text)
    {
        lblErrors.setTextFill(color);
        lblErrors.setText(text);
    }

    public Main.Status getStatus()
    {
        return msg -> setLblError(Color.BLACK, msg);
    }
}
