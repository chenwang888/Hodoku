package hodoku.chinesization.upgrade.observer;

public interface PropertyListener {
    void onPropertyChanged(String propertyName, Object newValue);
}