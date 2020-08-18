package net.anysync.ui;
/*
 * Copyright (c) YuanXin Technologies Inc. All Rights Reserved.
 */

import net.anysync.model.FileData;

/**
 * This class
 * Author: winner
 * Date: 8/17/20
 * Time: 6:04 PM
 */
public interface ControllerBase extends java.io.Serializable
{
    public void enterItem(FileData item);

    public String getFullLocalPath(String fname);
}
