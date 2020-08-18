// Copyright (c) 2020, Yanbin (Henry) Zheng <ybzheng@gmail.com>
// All rights reserved.
//
// Use of this source code is governed by a AGPLv3 license that can be
// found in the LICENSE file.
package net.anysync.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import net.anysync.ui.ControllerBase;
import net.anysync.ui.Main;
import net.anysync.util.AppUtil;
import net.anysync.util.IndexBinRow;
import org.kordamp.ikonli.javafx.FontIcon;

import java.awt.*;
import java.io.File;
import java.util.HashMap;

import static net.anysync.util.AppUtil.dateFormat;

public class FileData
{
	public static final int ICON_SIZE = 36;
	
	private SimpleStringProperty name, type,size,created,modified;
	private Button btn;
	private String hash;
	private boolean isDir;
	public final static Color FOLDER_COLOR = Color.rgb(13, 36, 129, 1);
	private long index;
	private boolean isSelected;
	private String fileNameKey;
	ControllerBase controller;
	int mode = 0;
	public static final String NO_SYNC = "No Sync";
	public FileData(String folderHash, IndexBinRow row, ControllerBase c)
	{
		this(folderHash, row, c, false);
	}
	public FileData(String folderHash, IndexBinRow row, ControllerBase c, boolean selected)
	{
		isSelected = selected;
		btn = new Button();
		FontIcon icon = new FontIcon();
		index = row.index;
		controller = c;
		name = new SimpleStringProperty(row.name);
		hash = row.getHashString();//IndexBinRow.byteArrayToHex(row.hash);
		fileNameKey = row.fileNameKey;
		isDir = row.isFileModeDirectory() || row.isFileModeRepository();
		String typeString ;//isDir?AppUtil.FOLDER:AppUtil.FILE;
		if(isDir)
		{
			icon.setIconLiteral("fth-folder-plus");
			icon.setIconColor(FOLDER_COLOR);
			typeString = AppUtil.FOLDER;
		}
		else
		{
			typeString = AppUtil.FILE;
			String n = getFileType(row.name);
			if(n != null)
			{
				if("icm-image".equals(n))
				{//          QString file = getTopObjectsFolder() + "/" + hashToPath(dirent.folderHash) + "_" + dirent.hash + ".png";
					String thumbnail = AppUtil.getFolder("objects") + AppUtil.hashToPath(folderHash) + "_" + hash + ".png";
					File file = new File(thumbnail);
					if(file.exists())
					{
						Image img = new Image(file.toURI().toString());
						ImageView view = new ImageView(img);
						view.setFitWidth(ICON_SIZE - 5);
						view.setFitHeight(ICON_SIZE - 5);
						btn.setGraphic(view);
						btn.setMaxSize(ICON_SIZE - 3, ICON_SIZE - 3);

						Tooltip tooltip = new Tooltip();
						tooltip.setGraphic(new ImageView(img));
						tooltip.setShowDelay(Duration.millis(100));
						tooltip.setStyle("-fx-background-radius: 0 0 0 0; -fx-background-color: aquamarine;");
						btn.setTooltip(tooltip);
						icon = null;
					}
				}
				if(icon != null) icon.setIconLiteral(n);
				n = typeStringMap.get(n);
				if(n != null)
				{
					typeString = n;
				}
			}
			else
			{
				icon.setIconLiteral("fth-file");
			}
			if(icon != null) icon.setIconColor(Color.GRAY);

		}
		if(isSelected)
		{
			typeString = NO_SYNC;
		}
		if(icon != null)
		{
			icon.setIconSize(ICON_SIZE - 12);
			btn.setAlignment(Pos.CENTER);
			btn.setMaxSize(ICON_SIZE - 12, ICON_SIZE - 12);
			btn.setGraphic(icon);
		}
		btn.setOnAction(e-> openFile());
		type = new SimpleStringProperty(typeString) ;//new SimpleStringProperty("desc");
		size = isDir ? new SimpleStringProperty("--") : new SimpleStringProperty(getSizeString(row.fileSize));
		created = new SimpleStringProperty(dateFormat.format(row.createTime*1000));
		modified = new SimpleStringProperty(dateFormat.format(row.lastModified*1000));
	}

	public void openFile()
	{
		try
		{
			if(controller != null)
			{
				if(isDir)
				{
					if(mode == 0 || (mode == 1 && !isSelected))
					{
						controller.enterItem(this);
						return;
					}
				}

				Desktop.getDesktop().open(new File(controller.getFullLocalPath(getName())));
			}
		}
		catch(Exception ignored)
		{
		}
	}
	
	public void setMode(int m)
	{
		mode = m;
	}

	public long getIndex()
	{
		return index;
	}
	public String getFileNameKey()
	{
		return fileNameKey;
	}
	public boolean isDirectory()
	{
		return isDir;
	}
	private static HashMap<String, String> typeMap = new HashMap<>();
	private static HashMap<String, String> typeStringMap = new HashMap<>();

	static
	{
		typeMap.put(".pdf", "icm-file-pdf");
		typeMap.put(".txt", "icm-file-text2");
		typeMap.put(".md", "icm-file-text2");
		typeMap.put(".log", "icm-file-text2");
		typeMap.put(".css", "icm-file-text2");
		typeMap.put(".java", "icm-file-text2");
		typeMap.put(".c", "icm-file-text2");
		typeMap.put(".cpp", "icm-file-text2");
		typeMap.put(".h", "icm-file-text2");
		typeMap.put(".go", "icm-file-text2");
		typeMap.put(".js", "icm-file-text2");
		typeMap.put(".html", "icm-file-text2");
		typeMap.put(".jpg", "icm-image" );
		typeMap.put(".jpeg", "icm-image");
		typeMap.put(".png", "icm-image");
		typeMap.put(".bmp", "icm-image");
		typeMap.put(".tif", "icm-image");
		typeMap.put(".tiff", "icm-image");
		typeMap.put(".gif", "icm-image");
		typeMap.put(".webp", "icm-image");
		//AVI, MPG, VOB, MOV, WMV, MP4, MKV, AMV
		typeMap.put(".mp4", "icm-file-video");
		typeMap.put(".avi", "icm-file-video");
		typeMap.put(".mpg", "icm-file-video");
		typeMap.put(".vob", "icm-file-video");
		typeMap.put(".mov", "icm-file-video");
		typeMap.put(".wmv", "icm-file-video");
		typeMap.put(".mkv", "icm-file-video");
		typeMap.put(".amv", "icm-file-video");
		typeMap.put(".doc", "icm-file-word");
		typeMap.put(".docx", "icm-file-word");
		typeMap.put(".xls", "icm-file-excel");
		typeMap.put(".xlsx", "icm-file-excel");
		typeMap.put(".mp3", "icm-file-music");
		typeMap.put(".m4a", "icm-file-music");
		typeMap.put(".m4b", "icm-file-music");
		typeMap.put(".m4p", "icm-file-music");
		typeMap.put(".wav", "icm-file-music");
		typeMap.put(".webm", "icm-file-music");
		typeMap.put(".aac", "icm-file-music");
		typeMap.put(".ape", "icm-file-music");

		typeStringMap.put("icm-file-pdf", "PDF");
		typeStringMap.put("icm-file-text2", Main.getString("Text"));
		typeStringMap.put("icm-image", Main.getString("Image"));
		typeStringMap.put("icm-file-video", Main.getString("Video"));
		typeStringMap.put("icm-file-word", "Word");
		typeStringMap.put("icm-file-excel", "Excel");
		typeStringMap.put("icm-file-music", Main.getString("Music"));
	}
	public static String getFileType(String fileName)
	{
		if(fileName == null) return null;
		String name = fileName.toLowerCase();
		int l = name.length();
		if(l > 5)
		{
			String ext = name.substring(l - 5);
			String ret = typeMap.get(ext);
			if(ret != null) return ret;
		}
		if(l > 4)
		{
			String ext = name.substring(l - 4);
			String ret = typeMap.get(ext);
			if(ret != null) return ret;
		}
		if(l > 3)
		{
			String ext = name.substring(l - 3);
			String ret = typeMap.get(ext);
			if(ret != null) return ret;
		}
		return null;
	}

	public static String getSizeString(long d)
	{
		if(d < 1024)
		{
			return String.format("%d bytes", d);
		}
		else if(d < 1024 * 1024)
		{
			return String.format("%.3f KB", d / 1000.0);
		}
		else if(d < 1024 * 1024 * 1024)
		{
			return String.format("%.3f MB", d / (1000 * 1000.0));
		}
		else //if ( d < 1000*1000*1000*1000)
		{
			return String.format("%.3f GB", d / (1000 * 1000 * 1000.0));
		}
	}

	public static long getRawSize(String s)
	{
		int pos = s.indexOf(" GB");
		if(pos > 0)
		{
			return (long) ( Double.parseDouble(s.substring(0, pos)) * (1000 * 1000 * 1000) );
		}
		pos = s.indexOf(" MB");
		if(pos > 0)
		{
			return (long) (Double.parseDouble(s.substring(0, pos)) * ( 1000 * 1000));
		}
		pos = s.indexOf(" KB");
		if(pos > 0)
		{
			return (long) (Double.parseDouble(s.substring(0, pos)) * (1000));
		}
		pos = s.indexOf(" bytes");
		if(pos > 0)
		{
			return Long.parseLong(s.substring(0, pos));
		}
		return 0;
	}

	public Button getIcon() {
		return btn;
	}
    public String getHash()
	{
		return hash;
	}
	public String getName() {
		return name.get();
	}
	public void setName(String strName) {
		name.set(strName);
	}
	public String getType() {
		return type.get();
	}
	public void setType(String strDescription) {
		type.set(strDescription);
	}
	public String getSize() {
		return size.get();
	}
	public void setSize(String strSize) {
		size.set(strSize);
	}
	public String getCreated() {
		return created.get();
	}
	public void setCreated(String strCreated) {
		created.set(strCreated);
	}
	public String getModified() {
		return modified.get();
	}
	public void setModified(String strModified) {
		modified.set(strModified);
	}
	public boolean isSelected()
	{
		return isSelected;
	}
	public void setSelected(boolean b)
	{
		isSelected = b;
	}

}
