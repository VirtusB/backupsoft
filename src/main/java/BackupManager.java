import com.alibaba.fastjson.JSON;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class BackupManager {
    public static final String BACKUP_DATABASE_PATH = System.getProperty("user.dir") + "/backup-db.json";
    private DirectoryManager directoryManager;

    public BackupManager() {
        this.createBackupDatabaseIfNotExists();
        this.directoryManager = new DirectoryManager(this);
    }

    private boolean backupDatabaseExists() {
        File backupDBFile = new File(BACKUP_DATABASE_PATH);
        return backupDBFile.exists();
    }

    public DirectoryManager getDirectoryManager() {
        return this.directoryManager;
    }

    public boolean updateBackupDatabase() {
        String jsonString = JSON.toJSONString(this.directoryManager.getDirectories());

        return this.writeJSONToBackupDB(jsonString);
    }

    private boolean writeJSONToBackupDB(String jsonString) {
        try {
            Files.write(Paths.get(BACKUP_DATABASE_PATH), jsonString.getBytes());
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void createBackupDatabaseIfNotExists() {
        if (!this.backupDatabaseExists()) {
            try {
                File backupDBFile = new File(BACKUP_DATABASE_PATH);
                backupDBFile.createNewFile();
            } catch (IOException e) {
                System.out.println("An error occurred.");
                e.printStackTrace();
            }
        }
    }
}
