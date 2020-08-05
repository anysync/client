// Copyright (c) 2020, Yanbin (Henry) Zheng <ybzheng@gmail.com>
// All rights reserved.
//
// Use of this source code is governed by a AGPLv3 license that can be
// found in the LICENSE file.
package net.anysync.ui;

import javafx.collections.FXCollections;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import net.anysync.model.FileData;
import net.anysync.util.*;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.*;

public class FileBrowserMainController
{
	final static Logger log = LogManager.getLogger(FileBrowserMainController.class);
	private double x, y = 0;
	private TableView<FileData> tableViewMain;
	private String localFolder;
	@FXML
	private TextField pathText;
	@FXML
	private BorderPane mainPane;

	@FXML
	private void initialize()
	{
	}

	public void setFolderHash(String name, String folderHash, String local)
	{
		localFolder= local;
		names.add(name);
		hashes.add(folderHash);
		ArrayList<IndexBinRow> rows = getFolderItems(folderHash);
		listHomeDirectories(folderHash, rows);
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

	private void listHomeDirectories(String folderHash, ArrayList<IndexBinRow> rows)
	{
		if(rows == null || rows.size() == 0) return;
		List<FileData> homeDirList = new ArrayList<>();
		for(IndexBinRow row : rows)
		{
			FileData fileData = new FileData(folderHash,row);
			homeDirList.add(fileData);
		}
		createMainPane(homeDirList);
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
			TableColumn<FileData, String> col;
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
					col.setPrefWidth(600.0);
					break;
				case DESC:
					col = new TableColumn<>(pvf.getProperty());
					col.setText(Main.getString(DESC));
					col.setMaxWidth(-1.0);
					col.setMinWidth(100.0);
					col.setPrefWidth(100.0);
					break;
				case SIZE:
					col = new TableColumn<>(pvf.getProperty());
					col.setText(Main.getString(SIZE));
					col.setMinWidth(100.0);
					col.setPrefWidth(100.0);
					col.setComparator((o1, o2) -> {
						long l1 = FileData.getRawSize(o1);
						long l2 = FileData.getRawSize(o2);
						if(l1 > l2) return 1;
						else
						{
							if(l1 == l2) return 0;
							else return -1;
						}
					});
					break;
				case CREATED:
					col = new TableColumn<>(pvf.getProperty());
					col.setText(Main.getString(CREATED));
					col.setMinWidth(150.0);
					col.setPrefWidth(150.0);
					break;
				case MODIFIED:
					col = new TableColumn<>(pvf.getProperty());
					col.setText(Main.getString(MODIFIED));
					col.setMinWidth(150.0);
					col.setPrefWidth(150.0);
					break;
				default:
					col = new TableColumn<>(pvf.getProperty());
					col.setMinWidth(0);
					col.setPrefWidth(0);
					break;
			}
			col.setCellValueFactory(pvf);
			tableView.getColumns().add(col);
		}

		Label lblEmpty = new Label(EMPTY_DIR);
		lblEmpty.setId(EMPTY_LBL);
		tableView.setPlaceholder(lblEmpty);
		tableView.setContextMenu(getContextMenu());
		tableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		tableView.setOnMouseClicked(new tableViewMouseEventHandler());
		tableView.setOnKeyReleased(new tableViewKeyEventHandler());
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
					enterItem(item);
				}
			}
			if(me.getButton() == MouseButton.SECONDARY)
			{
				((TableView<FileData>) me.getSource()).getContextMenu().show((TableView<FileData>) me.getSource(), me.getScreenX(), me.getScreenY());
				setRightClickedFileName(getPathText());

				if(((TableView<FileData>) me.getSource()).getSelectionModel().getSelectedItem() != null)
					setRightClickedFileName(getPathText() + File.separator + ((TableView<FileData>) me.getSource()).getSelectionModel().getSelectedItem().getName());

			}
			else
			{
				((TableView<FileData>) me.getSource()).getContextMenu().hide();
			}
		}
	}

	private class tableViewKeyEventHandler implements EventHandler<KeyEvent>
	{
		@SuppressWarnings("unchecked")
		@Override
		public void handle(KeyEvent ke)
		{
			if(ke.getCode().equals(KeyCode.ENTER))
			{
				if(((TableView<FileData>) ke.getSource()).getSelectionModel().getSelectedItem() != null)
				{
					FileData item = ((TableView<FileData>) ke.getSource()).getSelectionModel().getSelectedItem();
					enterItem(item);
				}
			}
			else if(ke.getCode().equals(KeyCode.BACK_SPACE))
			{
				moveToPreviousDirectory();
			}

		}
	}

	private ContextMenu getContextMenu()
	{
		ContextMenu contextMenu = new ContextMenu();
		MenuItem item;
		item = new MenuItem("Open");
		contextMenu.getItems().add(item);
		item.setOnAction(e -> {
			if( tableViewMain.getSelectionModel().getSelectedItem() != null)
			{
				FileData fd = tableViewMain.getSelectionModel().getSelectedItem();
				enterItem(fd);
			}

		});
		item = new MenuItem("Save As...");
		contextMenu.getItems().add(item);
		item.setOnAction(e -> {
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Save As");
			File dest = fileChooser.showSaveDialog(Main.getCurrentStage().getOwner());
			if(dest != null)
			{
				FileData fd = tableViewMain.getSelectionModel().getSelectedItem();
				File src = new File(getFullLocalPath(fd.getName()));
				try
				{
					FileUtil.copyFile(src, dest);
				}
				catch(IOException ex)
				{
					log.warn(ex);
				}
			}
		});

		contextMenu.getItems().add(new SeparatorMenuItem());
		item = new MenuItem("Rename");
		contextMenu.getItems().add(item);
		item.setOnAction(e -> {
			FileData fd = tableViewMain.getSelectionModel().getSelectedItem();
			File src = new File(getFullLocalPath(fd.getName()));
			TextInputDialog dialog = new TextInputDialog(fd.getName());
			dialog.setTitle("Rename");
			dialog.setHeaderText(null);
			dialog.setContentText("Rename file to");
			UiUtil.centerButtons(dialog.getDialogPane());
			Optional<String> result = dialog.showAndWait();
			if(result.isPresent())
			{
				String dest = getFullLocalPath(result.get().trim());
				if(src.renameTo(new File(dest)))
				{
					NetUtil.syncSendGetCommand("rescan", null, false);
				}
			}
		});
		item = new MenuItem("Delete");
		contextMenu.getItems().add(item);
		item.setOnAction(e -> {
			FileData fd = tableViewMain.getSelectionModel().getSelectedItem();
			File src = new File(getFullLocalPath(fd.getName()));
			Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
			alert.setTitle("Delete File");
			alert.setHeaderText("");
			alert.setContentText("Are you sure to delete the file?");
			UiUtil.centerButtons(alert.getDialogPane());
			Optional<ButtonType> result = alert.showAndWait();
			if(result.get() == ButtonType.OK)
			{
				if(src.delete())
				{
					NetUtil.syncSendGetCommand("rescan", null, false);
				}
			}

		});
		contextMenu.getItems().add(new SeparatorMenuItem());
		item = new MenuItem("Version History");
		contextMenu.getItems().add(item);
		item.setOnAction(e -> {
			FileData fd = tableViewMain.getSelectionModel().getSelectedItem();
			showVersionHistory(fd);
		});
		item = new MenuItem("Get Info");
		contextMenu.getItems().add(item);
		item.setOnAction(e -> {
			FileData fd = tableViewMain.getSelectionModel().getSelectedItem();
			showFileInfo(fd);
		});
		return contextMenu;
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

	private void enterItem(FileData item)
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
				Desktop.getDesktop().open(new File(getFullLocalPath(item.getName())));
			}
			catch(IOException e)
			{
				log.error(e);
			}
		}
	}

	private String getFullLocalPath(String fname)
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
	private void changeFolder(String folderHash)
	{
		ArrayList<IndexBinRow> rows = getFolderItems(folderHash);
		List<FileData> homeDirList = new ArrayList<>(rows.size());
		for(IndexBinRow row : rows)
		{
			homeDirList.add(new FileData(folderHash, row));
		}
		listTableData(homeDirList);
		setPathText();
	}

	private void setRightClickedFileName(String rightClickedFileName)
	{
	}

	private void setPathText()
	{
		String text = "/" + String.join("/", names);
		this.pathText.setText(text);
	}

	private String getPathText()
	{
		return this.pathText.getText().trim();
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

	private void showVersionHistory(FileData data)
	{
		Map<String, String> map = new HashMap<>();
		map.put("path", getFolderHash());
		map.put("i", String.valueOf(data.getIndex()));
		NetUtil.HttpReturn httpReturn = NetUtil.syncSendGetCommand("getversions", map, false);
		if(httpReturn.code != 200) return;
		VersionHistory versionHistory = new VersionHistory(getFolderHash(), data.getIndex(), httpReturn.response);

		versionHistory.show();

	}

	private void showFileInfo(FileData fd)
	{
		Map<String, String> map = new LinkedHashMap<>();
		map.put("Name", fd.getName());
		map.put("Size", fd.getSize());
		map.put("Date Created", fd.getCreated());
		map.put("Last Modified", fd.getModified());
		map.put("File Hash", fd.getHash());

		UiUtil.showPropertiesDialog("File Info", map, 750, 250);
	}

	public static final String ICON = "icon";
	public static final String DESC = ("Type");
	public static final String SIZE = ("Size");
	public static final String NAME = ("Name");
	public static final String CREATED = ("Created");
	public static final String MODIFIED = ("Modified");
	public static final String EMPTY_DIR = "Empty directory";
	public static final String EMPTY_LBL = "labelEmpty";
	public static List<PropertyValueFactory<FileData, String>> getColumnsList()
	{
		List<PropertyValueFactory<FileData, String>> colList = new ArrayList<PropertyValueFactory<FileData, String>>();
		colList.add(0, new PropertyValueFactory<>(ICON));
		colList.add(1, new PropertyValueFactory<>(NAME));
		colList.add(2, new PropertyValueFactory<>(DESC));
		colList.add(3, new PropertyValueFactory<>(SIZE));
		colList.add(4, new PropertyValueFactory<>(CREATED));
		colList.add(5, new PropertyValueFactory<>(MODIFIED));
		return colList;
	}
}