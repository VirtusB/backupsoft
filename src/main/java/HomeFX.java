import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.DirectoryChooser;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.prefs.Preferences;

public class HomeFX {
    public TableView<BackupDirectory> directoryTableView;
    public BackupManager backupManager;
    public Button browseForDirectoryBtn;
    public Button browseDestinationBtn;
    public TextField backupDestinationTextField;
    public CheckBox activeCheckbox;
    public ComboBox<BackupInterval> backupIntervalCb;

    private Preferences preferences;

    @FXML
    protected void initialize() {
        this.setupPreferences();

        this.backupManager = new BackupManager();
        this.createBackupDirectoryTable();
        this.addDirectoryBrowserBtnListener();
        this.addDestinationBrowserBtnListener();
        this.addActiveCheckBoxListener();
        this.addBackupIntervalOptions();
        this.addBackupIntervalListener();

        this.fireEventsAfterPreferences();
    }

    private void setupPreferences() {
        this.preferences = Preferences.userNodeForPackage(this.getClass());

        boolean activeStatus = this.preferences.getBoolean("isActive", false);
        this.setActiveStatusManually(activeStatus);

        String destinationPath = this.preferences.get("destinationPath", "");
        this.backupDestinationTextField.setText(destinationPath);

        int backupIntervalId = this.preferences.getInt("backupIntervalId", -1);
        if (backupIntervalId != -1) {
            BackupInterval interval = BackupManager.findBackupIntervalById(backupIntervalId);
            this.backupIntervalCb.getSelectionModel().select(interval);
        }
    }

    private void fireEventsAfterPreferences() {
        this.backupIntervalCb.fireEvent(new ActionEvent());
    }

    private void addActiveCheckBoxListener() {
        this.activeCheckbox.selectedProperty().addListener((oldVal, old, newv) -> {
            this.notifyBackupManager();
            this.preferences.putBoolean("isActive", this.isActive());
        });
    }

    private void addBackupIntervalListener() {
        this.backupIntervalCb.setOnAction(event -> {
            this.notifyBackupManager();

            BackupInterval selectedBackupInterval = (BackupInterval) this.backupIntervalCb.getSelectionModel().getSelectedItem();
            this.preferences.putInt("backupIntervalId", selectedBackupInterval.get_id());
        });
    }

    private void setActiveStatusManually(boolean active) {
        this.activeCheckbox.setSelected(active);
    }

    private boolean isActive() {
        return this.activeCheckbox.isSelected();
    }

    private void notifyBackupManager() {
        this.backupManager.addBackupInterval(this.isActive(), (BackupInterval) this.backupIntervalCb.getValue());
    }

    private void addBackupIntervalOptions() {
        this.backupIntervalCb.getItems().addAll(BackupManager.BACKUP_INTERVAL_OPTIONS);
    }

    private void addDestinationBrowserBtnListener() {
        this.browseDestinationBtn.setOnAction(event -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            File destinationDirectory = directoryChooser.showDialog(this.browseDestinationBtn.getScene().getWindow());

            if (destinationDirectory == null) {
                return;
            }

            String path = destinationDirectory.getAbsolutePath();

            this.preferences.put("destinationPath", path);
            this.backupDestinationTextField.setText(path);
        });
    }

    private void addDirectoryBrowserBtnListener() {
        this.browseForDirectoryBtn.setOnAction(event -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            File selectedDirectory = directoryChooser.showDialog(this.browseForDirectoryBtn.getScene().getWindow());

            if (selectedDirectory == null) {
                return;
            }

            Collection<File> filesInDirectory = FileUtils.listFiles(
                    selectedDirectory,
                    new RegexFileFilter("^(.*?)"),
                    DirectoryFileFilter.DIRECTORY
            );

            AtomicLong totalFolderSize = new AtomicLong();
            List<BackupFile> listOfFiles = FXCollections.observableArrayList();

            filesInDirectory.forEach(file -> {
                BackupFile newBackupFile = new BackupFile(file.getName(), file.length(), file.getAbsolutePath());
                totalFolderSize.addAndGet(file.length());
                listOfFiles.add(newBackupFile);
            });

            Date date = Calendar.getInstance().getTime();
            DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
            String strDate = dateFormat.format(date);

            if (backupManager.getDirectoryManager().directoryWithPathExists(selectedDirectory.getAbsolutePath())) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Folder already added");
                alert.setContentText("The folder have already been added");

                alert.showAndWait();
                return;
            }

            BackupDirectory newDirectory = new BackupDirectory(selectedDirectory.getName(), selectedDirectory.getAbsolutePath(), strDate, totalFolderSize.longValue(), listOfFiles);

            backupManager.getDirectoryManager().addDirectory(newDirectory);
        });
    }

    private void createBackupDirectoryTable() {
        TableColumn<BackupDirectory, String> nameColumn = new TableColumn<>("Name");
        TableColumn<BackupDirectory, String> dateAddedColumn = new TableColumn<>("Date Added");
        TableColumn<BackupDirectory, Long> sizeColumn = new TableColumn<>("Size");
        TableColumn<BackupDirectory, Integer> countOfFilesColumn = new TableColumn<>("# of files");
        TableColumn<BackupDirectory, Button> removeColumn = new TableColumn<>("Remove");


        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        dateAddedColumn.setCellValueFactory(new PropertyValueFactory<>("dateAdded"));
        sizeColumn.setCellValueFactory(new PropertyValueFactory<>("size"));
        countOfFilesColumn.setCellValueFactory(cellData -> cellData.getValue().getCountOfFiles());

        removeColumn.setCellFactory(ActionButtonTableCell.<BackupDirectory>forTableColumn("Remove", (BackupDirectory bd) -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm");
            alert.setHeaderText("Please confirm");
            alert.setContentText("Are you sure you want to remove the directory?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.OK){
                this.backupManager.getDirectoryManager().removeDirectory(bd);
                this.directoryTableView.getItems().remove(bd);
            }

            return bd;
        }));

        this.directoryTableView.setItems(this.backupManager.getDirectoryManager().getDirectories());
        this.directoryTableView.getColumns().addAll(nameColumn, dateAddedColumn, sizeColumn, countOfFilesColumn, removeColumn);
        this.directoryTableView.setMaxHeight(450);
        this.directoryTableView.setMaxWidth(750);
        this.directoryTableView.setPrefWidth(750);
        this.directoryTableView.setPrefHeight(450);
    }

}
