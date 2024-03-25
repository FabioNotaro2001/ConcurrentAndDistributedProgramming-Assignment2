package pcd.ass01.simtrafficbase;

import java.util.ArrayList;
import java.util.List;

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

    public void setTimings(int dt) {
        this.dt = dt;
    }

    public void setSteps(int nSteps) {
        this.nSteps = nSteps;
    }

    public void setStepsPerSec(int nStepsPerSec) {
        this.nStepsPerSec = nStepsPerSec;
    }

    public void createCars(int nCars, Road road, double initialPos, double deltaPos, double carAcceleration, double carDeceleration, double carMaxSpeed) {
        int carsPerThread = nThreads / nCars;
        int remainingCars = nThreads % nCars;
        int carId = 0;
        for (int i = 0; i < nThreads; i++) {
            CarsThread th = new CarsThread(actBarrier, stepBarrier, dt);
            carsThreads.add(th);
            for (int j = 0; j < carsPerThread; j++, carId++) {
                th.addCar(new CarAgentBasic("car" + carId, env, road, 
                                            initialPos + deltaPos * carId, 
                                            carAcceleration, 
                                            carDeceleration, 
                                            carMaxSpeed, 
                                            actBarrier));
            }
        }
        for (int i = 0; i < remainingCars; i++) {
            CarsThread th = carsThreads.getLast();
            for (int j = 0; j < carsPerThread; j++, carId++) {
                th.addCar(new CarAgentBasic("car" + carId, env, road, 
                                            initialPos + deltaPos * carId, 
                                            carAcceleration, 
                                            carDeceleration, 
                                            carMaxSpeed, 
                                            actBarrier));
            }
        }
    }

    public void runAllThreads() {
		carsThreads.forEach(th -> th.initCars(this.env));

        int stepsDone = 0;
        while (stepsDone < this.nSteps && !this.simulation.isStopped()) {
            this.stepBarrier.waitBeforeActing();    // Avvia l'inizio del passo.
            this.stepBarrier.waitBeforeActing();    // Attende il completamento del passo.
            syncWithWallTime();
            stepsDone++;
        }
        this.carsThreads.forEach(th -> th.requestInterrupt());
        this.stepBarrier.waitBeforeActing();        // Avvia il passo di terminazione.
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
