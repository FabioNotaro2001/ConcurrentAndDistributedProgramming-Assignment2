package pcd.ass01.simtrafficbase;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class BarrierImpl implements Barrier {

    private final int nThreads;
    private ReentrantLock mutex;
    private Condition c;
    private int nWait = 0; // Number of threads blocked on the barrier.
    private int nPassed = 0; // Number of threads that have passed the barrier.
    private Runnable onAllWaitingAction;

    public BarrierImpl(int nThreads){
        this.nThreads = nThreads;
        this.mutex = new ReentrantLock();
        this.c = mutex.newCondition();
    }

    private void runActionOnce() {
        // Execute the runnable, if present.
        if (onAllWaitingAction != null) {
            onAllWaitingAction.run();
            onAllWaitingAction = null;
        }
    }
    
    @Override
    public void waitBeforeActing() {
        // Waiting implementation without runnable.
        try {
            mutex.lock();
            nWait++;
            if (nWait < nThreads) {
                do {
                    c.await();
                } while (nPassed == 0); // Do-while useful for avoiding a double check on the two conditions.
            } else {
                nWait = 0; // Reset of the barrier.
                this.runActionOnce();     // Action executed when all threads are waiting on the barrier, because we don't know if the caller of the function will make the signal.
                c.signalAll();
            }
            nPassed = (nPassed + 1) % nThreads; // Reset of the numer of passed threads after all threads have passed the barrier.

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            mutex.unlock();
        }
    }

    @Override
    public void waitBeforeActing(Runnable onAllWaiting) {
        // Waiting implementation with runnable.
        try {
            mutex.lock();
            onAllWaitingAction = onAllWaiting;
            nWait++;
            if (nWait < nThreads) {
                do {
                    c.await();
                } while (nPassed == 0);
            } else {
                nWait = 0;              // Reset barrier.
                this.runActionOnce();     // Action executed when all threads are waiting on the barrier, because we don't know if the caller of the function will make the signal.
                c.signalAll();
            }
            nPassed = (nPassed + 1) % nThreads; // Reset of the numer of passed threads after all threads have passed the barrier.

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            mutex.unlock();
        }
    }
}
