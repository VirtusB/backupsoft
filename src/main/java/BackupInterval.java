public class BackupInterval {
    private String _displayName;
    private int _intervalSeconds;
    private int _id;

    public BackupInterval(String DisplayName, int IntervalSeconds, int Id) {
        this._displayName = DisplayName;
        this._intervalSeconds = IntervalSeconds;
        this._id = Id;
    }

    @Override
    public String toString() {
        return this.get_displayName();
    }

    public String get_displayName() {
        return this._displayName;
    }

    public void set_displayName(String _displayName) {
        this._displayName = _displayName;
    }

    public int get_intervalSeconds() {
        return this._intervalSeconds;
    }

    public void set_intervalSeconds(int _intervalSeconds) {
        this._intervalSeconds = _intervalSeconds;
    }

    public int get_id () { return this._id;}

    public void set_id (int _id) { this._id = _id; }
}
