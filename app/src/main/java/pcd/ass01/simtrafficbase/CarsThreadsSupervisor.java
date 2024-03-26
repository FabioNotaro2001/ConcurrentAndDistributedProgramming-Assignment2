package pcd.ass01.simtrafficbase;

import java.util.ArrayList;
import java.util.List;

import javax.swing.text.html.HTMLDocument.Iterator;

import com.google.common.collect.Iterables;

import pcd.ass01.simengineseq.AbstractSimulation;

public class CarsThreadsSupervisor {
    private final int nThreads;
    private RoadsEnv env;
    private final List<CarsThread> carsThreads;
    /**
     * Barrier for coordinating threads on when to act.
     */
    private final Barrier actBarrier;
    /**
     * Barrier for coordinating with the threads on when they can perform a step.
     */
    private final Barrier stepBarrier;
    private AbstractSimulation simulation;
    private int nStepsPerSec = 1;
    private int nSteps = 0;
    private int dt = Integer.MAX_VALUE;

    private int t0;
    
    /* for time statistics*/
	private long currentWallTime;

    public CarsThreadsSupervisor(int nThreads, AbstractSimulation simulation) {
        this.nThreads = nThreads;
        this.actBarrier = new BarrierImpl(nThreads);
        this.stepBarrier = new BarrierImpl(nThreads + 1);
        this.carsThreads = new ArrayList<>(nThreads);
        this.simulation = simulation;
    }

    public void setEnvironment(RoadsEnv env) {
        this.env = env;
    }

    public void setTimings(int t0, int dt) {
        this.t0 = t0;
        this.dt = dt;
    }

    public void setSteps(int nSteps) {
        this.nSteps = nSteps;
    }

    public void setStepsPerSec(int nStepsPerSec) {
        this.nStepsPerSec = nStepsPerSec;
    }

    public void createCars(List<CarAgent> cars) {
        var iter = cars.iterator();
        int carsPerThread = cars.size() / nThreads;
        int remainingCars = cars.size() % nThreads;
        for (int i = 0; i < nThreads; i++) {
            CarsThread th = new CarsThread(actBarrier, stepBarrier, dt);
            carsThreads.add(th);
            for (int j = 0; j < carsPerThread; j++) {
                th.addCar(iter.next());
            }
        }
        for (int i = 0; i < 1; i++) {
            CarsThread th = carsThreads.getLast();
            for (int j = 0; j < remainingCars; j++) {
                th.addCar(iter.next());
            }
        }
    }

    public void runAllThreads() {
		carsThreads.forEach(th -> th.initCars(this.env));
		carsThreads.forEach(th -> th.start());

        new Thread(() -> {
            int stepsDone = 0;
            int t = this.t0;
            while (stepsDone < this.nSteps && !this.simulation.isStopped()) {

                this.stepBarrier.waitBeforeActing();    // Avvia l'inizio del passo.
                t += this.dt;
                currentWallTime = System.currentTimeMillis();
                this.simulation.notifySimulationStep(t);

                syncWithWallTime();
                //try {
                    //Thread.sleep(500);
                //} catch (InterruptedException e) {
                //throw new RuntimeException(e);
                //}
                stepsDone++;
            }
            this.stopAllThreads();
            this.stepBarrier.waitBeforeActing();        // Avvia il passo di terminazione.
        }).start();


    }

    public void stopAllThreads() {
        this.carsThreads.forEach(th -> th.requestInterrupt());
    }

    private void syncWithWallTime() {
		try {
			long newWallTime = System.currentTimeMillis();
			long delay = 1000 / this.nStepsPerSec;
			long wallTimeDT = newWallTime - this.currentWallTime;
			if (wallTimeDT < delay) {
				Thread.sleep(delay - wallTimeDT);
			}
		} catch (Exception ex) {}		
	}
}
