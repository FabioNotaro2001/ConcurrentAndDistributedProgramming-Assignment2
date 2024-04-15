package es1.simtrafficbase;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class StopMonitor {
    // Monitor of readers/writers type useful for stopping the simulation.
    private int nr;
    private int nw;
    private final Condition okToRead;
    private final Condition okTowrite;
    private ReentrantLock mutex;

    public StopMonitor(){
        this.nr = 0;
        this.nw = 0;
        this.mutex = new ReentrantLock();
        this.okToRead = mutex.newCondition();
        this.okTowrite = mutex.newCondition();
    }

    public void requestRead(){
        mutex.lock();
        try {
            while(this.nw > 0){
                okToRead.await();
            }
            this.nr++;
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            mutex.unlock();
        }
    }

    public void releaseRead(){
        mutex.lock();
        try {
            this.nr--;
            if (this.nr == 0) {
                this.okTowrite.signalAll();            
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mutex.unlock();
        }
    }

    public void requestWrite(){
        try {
            mutex.lock();
            while(this.nr > 0 || this.nw > 0){
                okTowrite.await();
            }
            this.nw++;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mutex.unlock();
        }
    }

    public void releaseWrite(){
        try {
            mutex.lock();
            this.nw--;
            this.okTowrite.signal();
            this.okToRead.signalAll();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mutex.unlock();
        }
    }
}
