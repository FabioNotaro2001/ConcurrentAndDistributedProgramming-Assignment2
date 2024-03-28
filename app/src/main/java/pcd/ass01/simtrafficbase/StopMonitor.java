package pcd.ass01.simtrafficbase;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class StopMonitor {
    
    private int nr;
    private int nw;
    private final Condition okToRead;
    private final Condition okTowrite;
    private ReentrantLock mutexReader;
    private ReentrantLock mutexWriter;

    public StopMonitor(){
        this.nr = 0;
        this.nw = 0;
        this.mutexReader = new ReentrantLock();
        this.mutexWriter = new ReentrantLock();
        this.okToRead = mutexReader.newCondition();
        this.okTowrite = mutexWriter.newCondition();
    }

    public synchronized void requestRead(){
        try {
            mutexReader.lock();
            while(this.nw > 0){
                okToRead.await();
            }
            this.nr++;
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            mutexReader.unlock();
        }
    }

    public synchronized void releaseRead(){
        try {
            mutexWriter.lock();
            this.nr--;
            if (this.nr == 0) {
                this.okTowrite.signalAll();            
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mutexWriter.unlock();
        }
    }

    public synchronized void requestWrite(){
        try {
            mutexWriter.lock();
            while(this.nr > 0 || this.nw > 0){
                try {
                    okTowrite.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
            this.nw++;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mutexWriter.unlock();
        }
    }

    public synchronized void releaseWrite(){
        try {
            mutexWriter.lock();
            mutexReader.lock();
            this.nw--;
            this.okTowrite.signalAll();
            this.okToRead.signalAll();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mutexWriter.unlock();
            mutexReader.unlock();
        }
    }
}
