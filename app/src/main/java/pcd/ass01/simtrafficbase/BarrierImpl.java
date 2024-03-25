package pcd.ass01.simtrafficbase;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class BarrierImpl implements Barrier {

    private final int nThreads;
    private ReentrantLock mutex;
    private Condition c;
    private int npass = 0;

    public BarrierImpl(int nThreads){
        this.nThreads = nThreads;
        this.mutex = new ReentrantLock();
        this.c = mutex.newCondition();
    }

    @Override
    public void waitBeforeActing() {
        try {
            mutex.lock();
            npass++;
            if (npass < nThreads) {
                do {
                    c.await();
                } while (npass < nThreads);
            } else {
                npass = 0; // Reset barriera
                c.signalAll();
            }
        } catch (InterruptedException e) {

        } finally {
            mutex.unlock();
        }
    }
    
}
