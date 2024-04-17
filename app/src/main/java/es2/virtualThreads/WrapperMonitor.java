package es2.virtualThreads;

public class WrapperMonitor<T> {
    private T value;

    public WrapperMonitor(T value) {
        this.value = value;
    }

    public synchronized T getValue() {
        return this.value;
    }

    public synchronized void setValue(T value) {
        this.value = value;
    }
}
