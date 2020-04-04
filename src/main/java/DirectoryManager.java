import com.alibaba.fastjson.JSON;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

public class DirectoryManager {
    private ObservableList<BackupDirectory> directories;
    private BackupManager backupManager;

    public DirectoryManager(BackupManager backupManager) {
        this.backupManager = backupManager;
        this.directories = this.getDirectoriesFromDatabase();
    }

    public ObservableList<BackupDirectory> getDirectories() {
        return this.directories;
    }

    public boolean addDirectory(BackupDirectory directory) {
        this.directories.add(directory);
        return this.backupManager.updateBackupDatabase();
    }

    public boolean removeDirectory(BackupDirectory directory) {
        this.directories.remove(directory);
        return this.backupManager.updateBackupDatabase();
    }


    public boolean directoryWithPathExists(final String path){
        return this.directories.stream().anyMatch(o -> o.getPath().equals(path));
    }

    private ObservableList<BackupDirectory> getDirectoriesFromDatabase() {
        File backupDBFile = new File(BackupManager.BACKUP_DATABASE_PATH);

        String jsonString = "";

        try {
            jsonString = new String(Files.readAllBytes(backupDBFile.toPath()), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }

        ObservableList<BackupDirectory> directories = FXCollections.observableArrayList(

        );

        List<BackupDirectory> dirs = JSON.parseArray(jsonString, BackupDirectory.class);

        if (dirs != null && dirs.size() != 0) {
            directories.addAll(dirs);
            return directories;
        }

        ObservableList<BackupDirectory> emptyList = FXCollections.observableArrayList();
        return emptyList;
    }
}
