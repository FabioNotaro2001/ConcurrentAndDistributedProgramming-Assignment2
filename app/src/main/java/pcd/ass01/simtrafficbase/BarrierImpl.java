package pcd.ass01.simtrafficbase;

import java.util.Optional;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class BarrierImpl implements Barrier {

    private final int nThreads;
    private ReentrantLock mutex;
    private Condition c;
    private int nWait = 0; // nro Threads bloccati sulla barriera
    private int nPassed = 0; // nro Threads che hanno passato la barriera
    private Runnable onAllWaitingAction;

    public BarrierImpl(int nThreads){
        this.nThreads = nThreads;
        this.mutex = new ReentrantLock();
        this.c = mutex.newCondition();
    }

    private void runActionOnce() {
        if (onAllWaitingAction != null) {
            onAllWaitingAction.run();
            onAllWaitingAction = null;
        }
    }
    
    @Override
    public void waitBeforeActing() {
        try {
            mutex.lock();
            nWait++;
            if (nWait < nThreads) {
                do {

                    c.await();
                } while (nPassed == 0);
            } else {
                nWait = 0; // Reset barriera
                this.runActionOnce();     // Azione eseguita quando tutti i thread sono in attesa alla barriera.
                c.signalAll();
            }
            nPassed = (nPassed + 1) % nThreads;

        } catch (InterruptedException e) {

        } finally {
            mutex.unlock();
        }
    }

    @Override
    public void waitBeforeActing(Runnable onAllWaiting) {
        try {
            mutex.lock();
            onAllWaitingAction = onAllWaiting;
            nWait++;
            if (nWait < nThreads) {
                do {
                    c.await();
                } while (nPassed == 0);
            } else {
                nWait = 0;              // Reset barriera
                runActionOnce();     // Azione eseguita quando tutti i thread sono in attesa alla barriera.
                c.signalAll();
            }
            nPassed = (nPassed + 1) % nThreads;

        } catch (InterruptedException e) {

        } finally {
            mutex.unlock();
        }
    }
    
}
