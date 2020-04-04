import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.DirectoryChooser;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class HomeFX {
    public TableView<BackupDirectory> directoryTableView;
    public BackupManager backupManager;
    @FXML
    public Button browseForDirectoryBtn;


    @FXML
    protected void initialize() {
        this.backupManager = new BackupManager();
        this.createBackupDirectoryTable();
        this.addDirectoryBrowserBtnListener();
    }

    @FXML
    private void addDirectoryBrowserBtnListener() {
        this.browseForDirectoryBtn.setOnAction(event -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            File selectedDirectory = directoryChooser.showDialog(this.browseForDirectoryBtn.getScene().getWindow());
            List<File> filesInDirectory = Arrays.asList(selectedDirectory.listFiles());

            AtomicLong totalFolderSize = new AtomicLong();
            List<BackupFile> listOfFiles = FXCollections.observableArrayList();
            filesInDirectory.forEach(file -> {
                BackupFile newBackupFile = new BackupFile(file.getName(), file.length());
                totalFolderSize.addAndGet(file.length());
                listOfFiles.add(newBackupFile);
            });

            Date date = Calendar.getInstance().getTime();
            DateFormat dateFormat = new SimpleDateFormat("dd-mm-yyyy hh:mm:ss");
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
    }

}
