package hodoku.chinesization.upgrade.observer;

import java.util.ArrayList;
import java.util.List;

public class MainFrame {
    private List<PropertyListener> listeners = new ArrayList<>();

    public void addPropertyListener(PropertyListener listener) {
        listeners.add(listener);
    }

    public void removePropertyListener(PropertyListener listener) {
        listeners.remove(listener);
    }

    private void notifyPropertyChanged(String propertyName, Object newValue) {
        for (PropertyListener listener : listeners) {
            listener.onPropertyChanged(propertyName, newValue);
        }
    }
    // 其他成员方法和属性
}