// Copyright (c) 2020, Yanbin (Henry) Zheng <ybzheng@gmail.com>
// All rights reserved.
//
// Use of this source code is governed by a AGPLv3 license that can be
// found in the LICENSE file.
package net.anysync.ui;

import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import net.anysync.util.UiUtil;

public class ListDialog extends BorderPane
{
    public ListDialog(Object[] items)
    {
        init(items);
    }
    private void init(Object[] items)
    {
        ListView listView = new ListView();

        for(Object o : items)
        {
            listView.getItems().add(o);
        }

        setCenter(listView);
    }


    public void show(String title, int width, int height)
    {
        UiUtil.createDialog(this, title, width, height + 60, null, false, true);
    }

}
