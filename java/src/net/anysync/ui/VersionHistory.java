// Copyright (c) 2020, Yanbin (Henry) Zheng <ybzheng@gmail.com>
// All rights reserved.
//
// Use of this source code is governed by a AGPLv3 license that can be
// found in the LICENSE file.
package net.anysync.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.layout.Priority;
import net.anysync.util.NetUtil;
import net.anysync.util.Tokenizer;
import net.anysync.util.UiUtil;
import org.apache.log4j.*;;
import java.util.HashMap;
import java.util.Map;

public class VersionHistory extends BorderPane implements UiUtil.DialogSetter
{
    final static Logger log = LogManager.getLogger(AddRepoPane.class);
    public final static int WIDTH = 800;
    public final static int HEIGHT = 500;
    private String folderHash;
    private long index;
    public VersionHistory (String fHash, long idx, String response)
    {
        folderHash = fHash;
        index = idx;
        init(response);
    }

    private void init(String response)
    {
        GridPane pane = new GridPane();
        setCenter(pane);
        HBox.setHgrow(pane, Priority.ALWAYS);
        pane.setAlignment(Pos.TOP_CENTER);
        pane.setHgap(10);
        pane.setVgap(10);
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(35);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(25);
        ColumnConstraints col3 = new ColumnConstraints();
        col3.setPercentWidth(15);
        ColumnConstraints col4 = new ColumnConstraints();
        col4.setPercentWidth(10);
        ColumnConstraints col5 = new ColumnConstraints();
        col5.setPercentWidth(15);
        pane.getColumnConstraints().addAll(col1, col2, col3, col4, col5);
        pane.setPadding(new Insets(5, 5, 5, 5));
        int pos = response.indexOf("\n");
        String firstLine = response.substring(0, pos).trim();
        boolean hasMore = firstLine.equals("true");   //todo: if hasMore, show info
        response = response.substring(pos + 1);
        String[] lines = Tokenizer.parse(response, '\n', true, true);
        int i = 0;
        for(int j = 0; j < lines.length; j++)
        {
            String line = lines[j];
            //40.65 kb|Renamed|  |2020-07-09 15:34:12|11aa050fc8e8e2423ab0c4424d5e529e61fbe7709a0b02c746f263c7|495f062f0edac51e719c5937d0c860002baa3b2050b58da0181881c3|11|300px3.jpg
            String[] tokens = Tokenizer.parse(line, '|', true, false);
            pane.add(newLabel(tokens[tokens.length - 1]), 0,i);
            pane.add(newLabel(tokens[3]), 1, i);
            pane.add(newLabel(tokens[1]), 2, i);
            pane.add(newLabel(tokens[0]), 3, i);
            if(j != 0)
            {
                Button btn = new Button("Copy");
                UiUtil.setTooltip(btn, "Copy file to current user's Downloads directory");
                btn.setUserData(tokens[4] + "|" + tokens[5]);
                pane.add(btn, 4, i);
                btn.setOnAction(e -> {
                    Map<String, String> map = new HashMap<>();
                    map.put("path", folderHash);
                    map.put("i", String.valueOf(index));
                    map.put("m", "c");
                    map.put("l", "Downloads");
                    String userData = (String) ((Button) e.getSource()).getUserData();
                    String[] cs = Tokenizer.parse(userData, '|', true, true);
                    map.put("hash", cs[0]) ;
                    map.put("key", cs[1]);
                    NetUtil.HttpReturn httpReturn = NetUtil.syncSendGetCommand("getversions", map, false);
                    if(httpReturn.code != 200)
                    {
                        log.warn("Failed to copy");
                        return;
                    }
                });
            }
            else
            {
                pane.add(newLabel("Current Version"), 4, i);
            }
            i++;
        }

    }

    @Override
    public void setDialog(Dialog d)
    {
    }

    private Label newLabel(String text)
    {
        Label l = new Label(text);
        l.setStyle("-fx-font-size: 9pt;");
        return l;
    }
}
