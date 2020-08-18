// Copyright (c) 2020, Yanbin (Henry) Zheng <ybzheng@gmail.com>
// All rights reserved.
//
// Use of this source code is governed by a AGPLv3 license that can be
// found in the LICENSE file.
package net.anysync.ui;

import com.moandjiezana.toml.Toml;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.util.Callback;
import net.anysync.model.FileData;
import net.anysync.util.*;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.*;

public class SelectiveSyncController implements ControllerBase
{
	final static Logger log = LogManager.getLogger(SelectiveSyncController.class);
	private double x, y = 0;
	private TableView<FileData> tableViewMain;
	private Set<String> selectedFolders = new HashSet<>();
	private Set<String> selectedFoldersCopy = new HashSet<>();

	private String localFolder;
	@FXML
	private TextField pathText;
	@FXML
	private BorderPane mainPane;

	@FXML
	private void initialize()
	{
		Toml t = AppUtil.getConfig();
		if(t != null)
		{
			String fs = t.getString("SelectedFolders").trim();
			if(fs.length() > 0)
			{
				String[] tokens = Tokenizer.parse(fs, ',', true, true);
				Collections.addAll(selectedFolders, tokens);
				selectedFoldersCopy.addAll(selectedFolders);
			}
		}

	}

	Dialog stage;
	TextArea foldersText;
	public void setData(String name, String folderHash, String local, Dialog s, TextArea foldersText)
	{
		localFolder= local;
		this.foldersText = foldersText;
		stage = s;
		names.add(name);
		hashes.add(folderHash);
		listHomeDirectories(folderHash);
		setPathText();
	}

	public String getFolderHash()
	{
		return hashes.get(hashes.size() -1);
	}
	private ArrayList<IndexBinRow> getFolderItems(String folderHash)
	{
		String fileName = AppUtil.getBinFileName(folderHash);
		ArrayList<IndexBinRow> rows = null;
		try
		{
			rows = BinUtil.readBinFile(folderHash, fileName, true);
		}
		catch(IOException e)
		{
			log.error(e);
		}
		return rows;
	}

	private void listHomeDirectories(String folderHash)
	{
		List<FileData> homeDirList = createList(folderHash);
		if(homeDirList == null) return;
		createMainPane(homeDirList);
	}

	private void changeFolder(String folderHash)
	{
		List<FileData> homeDirList = createList(folderHash);
		listTableData(homeDirList);
	}

	private List<FileData>  createList(String folderHash)
	{
		setPathText();
		ArrayList<IndexBinRow> rows = getFolderItems(folderHash);
		List<FileData> homeDirList = new ArrayList<>();
		if(rows == null || rows.size() == 0) return homeDirList;
		String dir = pathText.getText().trim();
		for(IndexBinRow row : rows)
		{
			boolean isRepo = row.isFileModeRepository();
			if(!row.isFileModeDirectory() && !isRepo) continue;
			String d = dir + "/" + row.name;
			FileData fileData = new FileData(folderHash, row, this);
			fileData.setMode(1);
			if(selectedFolders.contains(d))
			{
				fileData.setSelected(true);
			}
			homeDirList.add(fileData);
		}
		return homeDirList;
	}

	private void createMainPane(List<FileData> homeDirList)
	{
		mainPane.setCenter(createTableView());
		listTableData(homeDirList);
	}

	private TableView createTableView()
	{
		TableView<FileData> tableView = new TableView<>();
		tableView.setFixedCellSize(FileData.ICON_SIZE + 2);//setRowHeight
		for(PropertyValueFactory<FileData, String> pvf : getColumnsList())
		{
			TableColumn<FileData, String> col = null;
			String p = pvf.getProperty();
			switch(p)
			{
				case ICON:
					col = new TableColumn<>();
					col.setMaxWidth(-1.0);
					col.setMinWidth(FileData.ICON_SIZE);
					col.setPrefWidth(FileData.ICON_SIZE);
					break;
				case NAME:
					col = new TableColumn<>(pvf.getProperty());
					col.setText(Main.getString(NAME));
					col.setMinWidth(100.0);
					col.setPrefWidth(400.0);
					break;
			}
			col.setCellValueFactory(pvf);
			tableView.getColumns().add(col);
		}


		TableColumn select = new TableColumn(FileData.NO_SYNC);
		select.setMinWidth(200);
		select.setCellValueFactory((Callback<TableColumn.CellDataFeatures<FileData, CheckBox>, ObservableValue<CheckBox>>) arg0 -> {
			FileData fileData = arg0.getValue();
			CheckBox checkBox = new CheckBox();
			checkBox.selectedProperty().setValue(fileData.isSelected());
			checkBox.selectedProperty().addListener(new ChangeListener<Boolean>()
			{
				public void changed(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val)
				{
					fileData.setSelected(new_val);

					String k = pathText.getText();
					if(k.length() > 0) k += "/";
					k += fileData.getName();
					if(new_val)
					{
						Iterator<String> it = selectedFolders.iterator();
						while(it.hasNext())
						{
							String key = it.next();
							if(key.contains(k))
							{
								it.remove();
							}
						}
						selectedFolders.add(k);
					}
					else
					{
						selectedFolders.remove(k);
					}
				}
			});
			return new SimpleObjectProperty<CheckBox>(checkBox);
		});
		tableView.getColumns().addAll(select);


		Label lblEmpty = new Label(EMPTY_DIR);
		lblEmpty.setId(EMPTY_LBL);
		tableView.setPlaceholder(lblEmpty);
		tableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		tableView.setOnMouseClicked(new tableViewMouseEventHandler());
		setTableView(tableView);
		return tableView;
	}

	private class tableViewMouseEventHandler implements EventHandler<MouseEvent>
	{
		@Override
		public void handle(MouseEvent me)
		{
			if(me.getButton().equals(MouseButton.PRIMARY) && me.getClickCount() == 2)
			{
				if(((TableView<FileData>) me.getSource()).getSelectionModel().getSelectedItem() != null)
				{
					FileData item = ((TableView<FileData>) me.getSource()).getSelectionModel().getSelectedItem();
					if(!item.isSelected()) enterItem(item);
				}
			}
		}
	}

	private void listTableData(List<FileData> homeDirList)
	{
		tableViewMain.getItems().setAll(FXCollections.observableList(homeDirList));
	}

	@FXML
	private void homeImageButtonClicked()
	{
		int n = hashes.size();
		if(n == 1) return;
		for(int i = names.size() - 1; i > 0; i--)
		{
			forwardNames.add(names.remove(i));
			forwardHashes.add(hashes.remove(i));
		}
		changeFolder(hashes.get(0));
	}

	@FXML
	private void backButtonClicked()
	{
		moveToPreviousDirectory();
	}

	@FXML
	private void forwardButtonClicked()
	{
		if(forwardHashes.size() > 0)
		{
			int n = forwardHashes.size() - 1;
			String h = forwardHashes.remove(n);
			hashes.add(h);
			names.add(forwardNames.remove(n));
			changeFolder(h);
		}
	}

	public void okButtonClicked()
	{
		String fs = selectedFolders.toString();
		fs = fs.substring(1, fs.length() - 1);
		Map<String, String> m = new HashMap<>();
		m.put("selectedfolders", fs);

		String newfolders = "";
		Iterator<String> it = selectedFolders.iterator();
		while(it.hasNext())
		{
			String k = it.next();
			if(!selectedFoldersCopy.contains(k))
			{
				if(newfolders.length() > 0) newfolders += ",";
				newfolders += k;
			}
		}

		String deletedfolders = "";
		it = selectedFoldersCopy.iterator();
		while(it.hasNext())
		{
			String k = it.next();
			if(!selectedFolders.contains(k))
			{
				if(deletedfolders.length() > 0) deletedfolders += ",";
				deletedfolders += k;
			}
		}

		if(newfolders.length() ==0 && deletedfolders.length() == 0) return;

		if(deletedfolders.length() > 0)
		{
			Map<String, String> params = new HashMap<>();
			params.put("selectedfolders", fs);
			params.put("restore", deletedfolders);
			NetUtil.syncSendGetCommand("restoreselected", params, false);
		}
		else
		{
			NetUtil.updateSettings(m);
		}

		if(foldersText != null) foldersText.setText(fs);

		if(newfolders.length() > 0 && foldersText != null) //foldersText != null indicates that it was launched from settings window, rather that from restore.
		{
			Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
			alert.setTitle("To Delete Folders");
			alert.setHeaderText("");
			alert.setContentText("Do you want to completely delete selected folders? (" + newfolders + ")");
			UiUtil.centerButtons(alert.getDialogPane());
			Optional<ButtonType> result = alert.showAndWait();
			if(result.get() == ButtonType.OK)
			{
				Map<String, String> params = new HashMap<>();
				params.put("fs", newfolders);
				NetUtil.syncSendGetCommand("deleteselected", params, false);
			}
		}

		stage.close();
	}

	public void cancelButtonClicked()
	{
		stage.close();
	}


	@FXML
	public void reloadButtonClicked()
	{
		changeFolder(getFolderHash());
	}

	private void moveToPreviousDirectory()
	{
		int size = names.size();
		if(size > 1)
		{
			String hash = hashes.get(size - 2);
			forwardNames.add(names.remove(size - 1));
			forwardHashes.add(hashes.remove(size - 1));
			changeFolder(hash);
		}
	}


	private void setTableView(TableView<FileData> tableView)
	{
		this.tableViewMain = tableView;
	}

	private ArrayList<String> names = new ArrayList<>();
	private ArrayList<String> hashes = new ArrayList<>();
	private ArrayList<String> forwardNames = new ArrayList<>();
	private ArrayList<String> forwardHashes = new ArrayList<>();

	public void enterItem(FileData item)
	{
		if(item.isDirectory())
		{
			names.add(item.getName());
			hashes.add(item.getHash());
			forwardHashes.clear();
			forwardNames.clear();
			changeFolder(item.getHash());
		}
		else
		{
			try
			{
				String fname = getFullLocalPath(item.getName());
				if(NetUtil.isNonSyncMode())
				{
					if( ! NetUtil.isPlaceholderMode())
					{
						if(new File(fname).exists())
						{
							Desktop.getDesktop().open(new File(fname));
						}
						return;
					}
					downloadFile(item,fname,true);
					return;
				}
				else
				{
					if(new File(fname).exists())
					{
						Desktop.getDesktop().open(new File(fname));
					}
					else if(!item.isDirectory())
					{
						String path = pathText.getText().substring(1);
						Toml toml = AppUtil.getConfig();
						if(toml != null)
						{
							String value = toml.getString("SelectedFolders");
							if(value != null && value.trim().length() > 0)
							{
								String[] tokens = Tokenizer.parse(value, ',', true, true);
								for(String t : tokens)
								{
									if(path.startsWith(t))
									{
										File f = new File(fname);
										String dir = f.getParent();
										new File(dir).mkdirs();
										downloadFile(item, fname, true);
									}
								}
							}
						}
					}
				}
			}
			catch(IOException e)
			{
				log.error(e);
			}
		}
	}

	public String getFullLocalPath(String fname)
	{
		String path = pathText.getText();
		int pos = path.indexOf("/", 1);
		if(pos > 0)
		{
			path = path.substring(pos);
		}
		else
		{
			path = "";
		}
		return localFolder +  path + "/" + fname;
	}

	private void setPathText()
	{
		String text =  String.join("/", names);
		if(text.startsWith("/")) text = text.substring(1);
		this.pathText.setText(text);
	}

	@FXML
	private void mouseDragged(MouseEvent event)
	{
		Node node = (Node) event.getSource();
		Stage stage = (Stage) node.getScene().getWindow();
		stage.setX(event.getScreenX() - x);
		stage.setY(event.getScreenY() - y);
	}

	@FXML
	private void mousePressed(MouseEvent event)
	{
		x = event.getSceneX();
		y = event.getSceneY();
	}

	public static final String ICON = "icon";
	public static final String NAME = ("Name");
	public static final String EMPTY_DIR = "Empty directory";
	public static final String EMPTY_LBL = "labelEmpty";
	public static List<PropertyValueFactory<FileData, String>> getColumnsList()
	{
		List<PropertyValueFactory<FileData, String>> colList = new ArrayList<>();
		colList.add(0, new PropertyValueFactory<>(ICON));
		colList.add(1, new PropertyValueFactory<>(NAME));
		return colList;
	}

	private void downloadFile(FileData item, String fname,  boolean open)
	{
		Map<String, String> map = new HashMap<>();
		map.put("path", getFolderHash());
		map.put("m", "c");
		map.put("hash", item.getHash());
		map.put("open", open ? "1" : "0");
		map.put("key", item.getFileNameKey());
		map.put("l", fname);
		NetUtil.HttpReturn ret = NetUtil.syncSendGetCommand("getversions", map, false);
		if(ret.response.equals("ERR"))
		{
			Main.setStatus("Error occurred.");
		}
		else if(ret.response.length() == 0)
		{
			Main.setStatus("File does not exist: " + fname);
		}
		else
		{
			Main.setStatus("To download file " + fname);
		}
		return;

	}
}
