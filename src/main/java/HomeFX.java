import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class HomeFX {
    public TableView<BackupDirectory> directoryTableView;
    public BackupManager backupManager;

    @FXML
    protected void initialize() {
        this.backupManager = new BackupManager();
        this.createBackupDirectoryTable();

//        BackupFile fileOne = new BackupFile("debug.log", 19920);
//        List<BackupFile> listOfFiles = FXCollections.observableArrayList(
//                fileOne
//        );
//
//        BackupDirectory testDirectory = new BackupDirectory("Documents", "C:\\Users\\Focuz", "01-04-2020 20:25", 19920, listOfFiles);
//
//        backupManager.getDirectoryManager().addDirectory(testDirectory);


//        List<BackupDirectory> directoriesForBackup = backupManager.getDirectoryManager().getDirectories();
//
//        directoriesForBackup.forEach(directory -> {
//            System.out.println(directory.getName());
//        });
    }

    private void createBackupDirectoryTable() {
        TableColumn<BackupDirectory, String> nameColumn = new TableColumn<>("Name");
        TableColumn<BackupDirectory, String> dateAddedColumn = new TableColumn<>("Date Added");
        TableColumn<BackupDirectory, Long> sizeColumn = new TableColumn<>("Size");
        TableColumn<BackupDirectory, Integer> countOfFilesColumn = new TableColumn<>("# of files");

        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        dateAddedColumn.setCellValueFactory(new PropertyValueFactory<>("dateAdded"));
        sizeColumn.setCellValueFactory(new PropertyValueFactory<>("size"));
        countOfFilesColumn.setCellValueFactory(cellData -> cellData.getValue().getCountOfFiles());

        this.directoryTableView.setItems(this.backupManager.getDirectoryManager().getDirectories());
        this.directoryTableView.getColumns().addAll(nameColumn, dateAddedColumn, sizeColumn, countOfFilesColumn);
        this.directoryTableView.setMaxHeight(450);
        this.directoryTableView.setMaxWidth(750);

//        private String name;
//        private String path;
//        private String dateAdded;
//        private long size;
//        private List<BackupFile> backupFiles;
    }

}
