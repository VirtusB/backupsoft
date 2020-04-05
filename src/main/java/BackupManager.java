import com.alibaba.fastjson.JSON;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.prefs.Preferences;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class BackupManager {
    public static final String BACKUP_DATABASE_PATH = System.getProperty("user.dir") + "/backup-db.json";
    public static final ObservableList<BackupInterval> BACKUP_INTERVAL_OPTIONS =
            FXCollections.observableArrayList(
                    new BackupInterval("Stopped", 0, 1),
                    new BackupInterval("Every 30 sec.", 30, 2),
                    new BackupInterval("Every 2 min.", 120, 3),
                    new BackupInterval("Every 30 min.", 1800, 4),
                    new BackupInterval("Every 2 hrs.", 7200, 5)
            );

    private ScheduledExecutorService backupIntervalExecutor = Executors.newSingleThreadScheduledExecutor();
    private ExecutorService backupExecutor = Executors.newSingleThreadExecutor();

    private DirectoryManager directoryManager;
    private Preferences preferences;

    public BackupManager() {
        this.preferences = Preferences.userNodeForPackage(this.getClass());
        this.createBackupDatabaseIfNotExists();
        this.directoryManager = new DirectoryManager(this);
    }

    public void addBackupInterval(boolean active, BackupInterval backupInterval) {
        if (active) {
            if (!backupIntervalExecutor.isShutdown()) {
                backupIntervalExecutor.shutdownNow();
            }

            backupIntervalExecutor = Executors.newSingleThreadScheduledExecutor();

            if (backupInterval == null || backupInterval.get_intervalSeconds() == 0) {
                return;
            }


            int ms = backupInterval.get_intervalSeconds() * 1000;

            Runnable task = () -> {
                this.startBackup();
            };

            backupIntervalExecutor.scheduleAtFixedRate(task, ms, ms, TimeUnit.MILLISECONDS);
        } else if (backupIntervalExecutor != null && !backupIntervalExecutor.isShutdown()) {
            backupIntervalExecutor.shutdownNow();
        }
    }

    private void startBackup() {
        System.out.println("Running backup now");

        this.directoryManager.getDirectories().forEach(directory -> {
            String destinationPath = this.getDestinationPath();

            if (destinationPath.equals("")) {
                System.out.println("Destionation Path is not set");
                return;
            }

            String sourcePathStr = directory.getPath();
            String dirToBeCreatedPathStr = destinationPath + java.io.File.separator + directory.getName();

            try {
                this.copyFile(sourcePathStr, dirToBeCreatedPathStr);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void copyFile(String srcStr, String destStr) throws IOException {
        Path source = Paths.get(srcStr);
        Path destination = Paths.get(destStr);

        Stream<Path> stream = Files.walk(source);
        List<Path> sources = stream.collect(toList());
        List<Path> destinations = sources.stream()
                .map(source::relativize)
                .map(destination::resolve)
                .collect(toList());

        for (int i = 0; i < sources.size(); i++) {
            boolean exists = new File(destinations.get(i).toUri()).exists();
            if (exists) {
                BackupManager.deleteRecursive(new File(destinations.get(i).toUri()));
            }

            Files.copy(sources.get(i), destinations.get(i), StandardCopyOption.REPLACE_EXISTING);
        }

        stream.close();
    }

    private static boolean deleteRecursive(File path) {
        if (!path.exists()) {
            return false;
        }

        boolean ret = true;
        if (path.isDirectory()){
            for (File f : path.listFiles()){
                ret = ret && deleteRecursive(f);
            }
        }
        return ret && path.delete();
    }

    private String getDestinationPath() {
        return this.preferences.get("destinationPath", "");
    }

    public static BackupInterval findBackupIntervalById(int id) {
        return BACKUP_INTERVAL_OPTIONS.stream().filter(interval -> id == interval.get_id()).findFirst().orElse(null);
    }

    private boolean backupDatabaseExists() {
        File backupDBFile = new File(BACKUP_DATABASE_PATH);
        return backupDBFile.exists();
    }

    public DirectoryManager getDirectoryManager() {
        return this.directoryManager;
    }

    public boolean updateBackupDatabase() {
        String jsonString = null;

        try {
            jsonString = JSON.toJSONString(this.directoryManager.getDirectories());
            return this.writeJSONToBackupDB(jsonString);
        } catch (NullPointerException e) {
            return this.writeJSONToBackupDB("[]");
        }
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
