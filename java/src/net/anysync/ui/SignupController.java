// Copyright (c) 2020, Yanbin (Henry) Zheng <ybzheng@gmail.com>
// All rights reserved.
//
// Use of this source code is governed by a AGPLv3 license that can be
// found in the LICENSE file.
package net.anysync.ui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import net.anysync.util.AppUtil;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignupController implements Initializable
{

    @FXML
    private Label lblErrors;

    @FXML
    private TextField txtUsername;

    @FXML
    private TextField txtPassword;
    @FXML
    private TextField txtPassword2;

    @FXML
    private TextField txtServer;

    @FXML
    private Button btnSignup;

    @FXML
    public void handleButtonAction(MouseEvent event)
    {
        doSignup();
    }

    @FXML
    public void onEnter(ActionEvent ae)
    {
        doSignup();
    }

    private static final String REGEX = "^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";

    public static boolean isValidEmail(String email)
    {
        Pattern pattern = Pattern.compile(REGEX);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }
    private void doSignup()
    {
        String username = txtUsername.getText().trim();
        if(username.length() == 0 || !isValidEmail(username) || username.contains("@test.com"))
        {
            txtUsername.requestFocus();
            setError("Username is not valid.");
            return;
        }
        String password = txtPassword.getText().trim();
        if(password.length() < 8)
        {
            setError("Password must be at least 8 characters.");
            txtPassword.requestFocus();
            return;
        }
        String password2 = txtPassword2.getText().trim();
        if(password2.length() == 0)
        {
            txtPassword2.requestFocus();
            return;
        }
        if(!password.equals(password2))
        {
            setError("Passwords do not match.");
            return;
        }
        if(txtServer.getText().trim().length() == 0)
        {
            setError("Server cannot be empty.");
            return;
        }

        int ret = LoginController.signin(txtUsername.getText().trim(), txtPassword.getText().trim(),  txtServer.getText().trim(), true);
        if(ret == 401)
        {
            setError("User name or password are not correct.");
        }
        else if(ret == 405)
        {
            setError("Cannot connect to the server");
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
        txtServer.setText(AppUtil.getServerAddress());
    }

    private void setError(String msg)
    {
        lblErrors.setTextFill(Color.TOMATO);
        lblErrors.setText(msg);//Server Error : Check");
    }


    public SignupController ()
    {
    }

    public Main.Status getStatus()
    {
        return new Main.Status()
        {
            @Override
            public void setStatus(String msg)
            {
                lblErrors.setTextFill(Color.BLACK);
                lblErrors.setText(msg);
            }
        };
    }
}
