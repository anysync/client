// Copyright (c) 2020, Yanbin (Henry) Zheng <ybzheng@gmail.com>
// All rights reserved.
//
// Use of this source code is governed by a AGPLv3 license that can be
// found in the LICENSE file.
package net.anysync.ui;

import com.moandjiezana.toml.Toml;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import net.anysync.model.FileData;
import net.anysync.util.*;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class FileBrowserMainController implements  ControllerBase, Main.Status
{
	final static Logger log = LogManager.getLogger(FileBrowserMainController.class);
	private double x, y = 0;
	private TableView<FileData> tableViewMain;
	private Set<String> selectedFolders = new HashSet<>();

	private String localFolder;
	@FXML
	private TextField pathText;
	@FXML
	private BorderPane mainPane;
	@FXML
	private Label statusLabel;

	@FXML
	private void initialize()
	{
		Toml t = AppUtil.getConfig();
		if( t != null)
		{
			String fs = t.getString("SelectedFolders").trim();
			if(fs.length() > 0)
			{
				String[] tokens = Tokenizer.parse(fs, ',', true, true);
				selectedFolders.addAll(Arrays.asList(tokens));
			}
		}
		Main.setStatus(this);
	}

	public void setFolderHash(String name, String folderHash, String local)
	{
		localFolder= local;
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

	private List<FileData> createList(String folderHash)
	{
		setPathText();
		ArrayList<IndexBinRow> rows = getFolderItems(folderHash);
		List<FileData> homeDirList = new ArrayList<>();
		if(rows == null || rows.size() == 0) return homeDirList;

		String dir = getPathText();
		for(IndexBinRow row : rows)
		{
			boolean selected = false;
			String d = dir + "/" + row.name;
			if(selectedFolders.contains(d))
			{
				selected = true;
			}
			else
			{
				for(String f : selectedFolders)
				{
					if(d.startsWith(f))
					{
						selected = true;
						break;
					}
				}
			}
			FileData fileData = new FileData(folderHash, row, this, selected);
			homeDirList.add(fileData);


		}
		return homeDirList;
	}


	private void createMainPane(List<FileData> homeDirList)
	{
		mainPane.setCenter(createTableView());
		listTableData(homeDirList);
	}
	ContextMenu _contextMenu;
	TableView<FileData> _tableView;

	private TableView createTableView()
	{
		TableView<FileData> tableView = new TableView<>();
		_tableView = tableView;
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
				case FILE_TYPE:
					col = new TableColumn<>(pvf.getProperty());
					col.setText(FILE_TYPE);
					col.setMaxWidth(-1.0);
					col.setMinWidth(100.0);
					col.setPrefWidth(100.0);
					col.setCellFactory(new Callback<>()
					{
						@Override
						public TableCell<FileData, String> call(TableColumn<FileData, String> param)
						{
							return new TableCell<>()
							{

								@Override
								public void updateItem(String item, boolean empty)
								{
									super.updateItem(item, empty);
									if(!isEmpty())
									{
										setText(item);
										if(item.equals(FileData.NO_SYNC))
										{
											setStyle("-fx-text-fill: brown; ");
										}

									}
									else
									{
										setText(null);
										setStyle("");
									}
								}

							};
						}
					});

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
		tableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		setTableView(tableView);
		tableView.addEventHandler(MouseEvent.MOUSE_RELEASED, e -> {
			FileData fd = ((TableView<FileData>) e.getSource()).getSelectionModel().getSelectedItem();
			if(e.getButton().equals(MouseButton.PRIMARY) && e.getClickCount() == 2)
			{
				if(_contextMenu != null) _contextMenu.hide();
				if(((TableView<FileData>) e.getSource()).getSelectionModel().getSelectedItem() != null)
				{
					enterItem(fd);
				}
			}

			if(e.getButton() == MouseButton.SECONDARY)
			{
				if(fd != null && !fd.isDirectory())
				{
					_contextMenu = getContextMenu (fd);
					_contextMenu.show(tableView, e.getScreenX(), e.getScreenY());
				}
			}
			else
			{
				if(_contextMenu != null) _contextMenu.hide();
			}
		});
		return tableView;
	}

	private ContextMenu getContextMenu(FileData fd)
	{

		if(_contextMenu == null)
		{
			_contextMenu = new ContextMenu();
		}
		_contextMenu.getItems().clear();
		MenuItem item;
		String text = "Save As...";
		if(NetUtil.isNonSyncMode() || fd.isSelected())
		{
			text = "Download Local Copy";
		}
		item = new MenuItem(text);
		_contextMenu.getItems().add(item);
		item.setOnAction(e -> {
			if(NetUtil.isNonSyncMode() || fd.isSelected())
			{
				String fname = getFullLocalPath(fd.getName());
				if(NetUtil.isPlaceholderMode())
				{
					downloadFile(fd, "Downloads", false);
				}
				else
				{
					downloadFile(fd, fname, false);
				}
				return;
			}
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Save As");
			File dest = fileChooser.showSaveDialog(Main.getCurrentStage().getOwner());
			if(dest != null)
			{
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

		if(NetUtil.isSyncMode() && !fd.isSelected())
		{
			_contextMenu.getItems().add(new SeparatorMenuItem());
			item = new MenuItem("Rename");
			_contextMenu.getItems().add(item);
			item.setOnAction(e -> {
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
			_contextMenu.getItems().add(item);
			item.setOnAction(e -> {
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
		}
		_contextMenu.getItems().add(new SeparatorMenuItem());
		item = new MenuItem("Version History");
		_contextMenu.getItems().add(item);
		item.setOnAction(e -> {
			showVersionHistory(fd);
		});
		item = new MenuItem("Get Info");
		_contextMenu.getItems().add(item);
		item.setOnAction(e -> {
			showFileInfo(fd);
		});
		return _contextMenu;
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
							java.awt.Desktop.getDesktop().open(new File(fname));
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
						java.awt.Desktop.getDesktop().open(new File(fname));
					}
					else if(!item.isDirectory())
					{
						String path = getPathText().substring(1);
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
		String path = getPathText();
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
		String text = String.join("/", names);
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
		UiUtil.createDialog(_tableView.getScene().getWindow(), versionHistory, Main.getString("Version History"),
							VersionHistory.WIDTH, VersionHistory.HEIGHT + 60, versionHistory, false, true);
	}

	private void showFileInfo(FileData fd)
	{
		Map<String, String> map = new LinkedHashMap<>();
		map.put("Name", fd.getName());
		map.put("Size", fd.getSize());
		map.put("Date Created", fd.getCreated());
		map.put("Last Modified", fd.getModified());
		map.put("File Hash", fd.getHash());

		UiUtil.showPropertiesDialog(_tableView.getScene().getWindow(), "File Info", map, 750, 250);
	}

	public static final String ICON = "icon";
	public static final String FILE_TYPE = ("Type");
	public static final String SIZE = ("Size");
	public static final String NAME = ("Name");
	public static final String CREATED = ("Created");
	public static final String MODIFIED = ("Modified");
	public static final String EMPTY_DIR = "Empty directory";
	public static final String EMPTY_LBL = "labelEmpty";
	public static List<PropertyValueFactory<FileData, String>> getColumnsList()
	{
		List<PropertyValueFactory<FileData, String>> colList = new ArrayList<>();
		colList.add(0, new PropertyValueFactory<>(ICON));
		colList.add(1, new PropertyValueFactory<>(NAME));
		colList.add(2, new PropertyValueFactory<>(FILE_TYPE));
		colList.add(3, new PropertyValueFactory<>(SIZE));
		colList.add(4, new PropertyValueFactory<>(CREATED));
		colList.add(5, new PropertyValueFactory<>(MODIFIED));
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
//					String url = NetUtil.LOCAL_URL_PREFIX + "tpl/versions.html?m=c&path=" + getFolderHash() + "&hash=" + item.getHash() + "&open=1";
//					NetUtil.HttpReturn ret = NetUtil.syncSendGetRequest(url, false);
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

	public void setStatus(String m)
	{
		statusLabel.setText(m);
	}
}