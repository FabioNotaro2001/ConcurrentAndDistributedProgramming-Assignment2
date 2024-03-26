package pcd.ass01.simtrafficbase;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class BarrierImpl implements Barrier {

    private final int nThreads;
    private ReentrantLock mutex;
    private Condition c;
    private int nWait = 0; // nro Threads bloccati sulla barriera
    private int nPassed = 0; // nro Threads che hanno passato la barriera

    public BarrierImpl(int nThreads){
        this.nThreads = nThreads;
        this.mutex = new ReentrantLock();
        this.c = mutex.newCondition();
    }

    
    @Override
    public void waitBeforeActing() {
        try {
            mutex.lock();
            nWait++;
            if (nWait < nThreads) {
                do {
                    //System.out.println("Waiting");
                    c.await();
                } while (nPassed == 0);
            } else {
                nWait = 0; // Reset barriera
                c.signalAll();
            }
            // nPassed++;
            // if (nPassed == nThreads) {
            //     nPassed = 0;
            // }
            nPassed = (nPassed + 1) % nThreads; 

            System.out.println("unlock");

        } catch (InterruptedException e) {

        } finally {
            mutex.unlock();
        }
    }
    
}
