import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;

import java.util.List;

public class BackupDirectory {
    private String name;
    private String path;
    private String dateAdded;
    private long size;
    private List<BackupFile> backupFiles;

    public BackupDirectory() {

    }

    public BackupDirectory(String name, String path, String dateAdded, long size, List<BackupFile> backupFiles) {
        this.name = name;
        this.path = path;
        this.dateAdded = dateAdded;
        this.size = size;
        this.backupFiles = backupFiles;
    }

    public ObjectProperty<Integer> getCountOfFiles() {
        return new SimpleIntegerProperty(this.backupFiles.size()).asObject();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(String dateAdded) {
        this.dateAdded = dateAdded;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public List<BackupFile> getBackupFiles() {
        return backupFiles;
    }

    public void setBackupFiles(List<BackupFile> backupFiles) {
        this.backupFiles = backupFiles;
    }
}
