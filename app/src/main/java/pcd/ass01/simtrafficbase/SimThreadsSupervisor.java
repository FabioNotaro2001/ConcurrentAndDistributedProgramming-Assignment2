package pcd.ass01.simtrafficbase;

import java.util.ArrayList;
import java.util.List;

import pcd.ass01.simengineseq.AbstractSimulation;

public class SimThreadsSupervisor {
    // Supervisor for managing cars and trafficlights threads.
    private final int nThreadsForCars;
    private final int nThreadsForTrafficLights;
    private RoadsEnv env;
    private final List<CarsThread> carsThreads;

    private final List<TrafficLightsThread> trafficLightsThreads;
    /**
     * Barrier for coordinating threads on when to act.
     */
    private final Barrier actBarrier;   // Barrier for alignment before acting phase.
    /**
     * Barrier for coordinating with the threads on when they can perform a step.
     */
    private final Barrier stepBarrier;  // Barrier for alignment before next step.
    private AbstractSimulation simulation;
    private int nStepsPerSec = 0;
    private int nSteps = 0;
    private int dt = Integer.MAX_VALUE;

    private int t0;
    
    /* for time statistics*/
	private long currentWallTime;

    private long totalTime=0;

    public SimThreadsSupervisor(int nThreadsForCars, int nThreadsForLights, AbstractSimulation simulation) {
        this.nThreadsForCars = nThreadsForCars;
        this.nThreadsForTrafficLights = nThreadsForLights;
        this.actBarrier = new BarrierImpl(nThreadsForCars + nThreadsForLights);
        this.stepBarrier = new BarrierImpl(nThreadsForCars + nThreadsForLights + 1);  // +1 per il supervisor, bisogna aspettare anche lui.
        this.carsThreads = new ArrayList<>(nThreadsForCars);
        this.trafficLightsThreads = new ArrayList<>(nThreadsForLights);
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
        // Method that creates threads for cars and assign them car agents.
        this.carsThreads.clear();   // Necessary because of reset button.
        var iter = cars.iterator(); // Iterator of cars.
        int carsPerThread = cars.size() / nThreadsForCars;
        int remainingCars = cars.size() % nThreadsForCars;
        for (int i = 0; i < nThreadsForCars; i++) { // Scroll n threads.
            // Create new car thread and add it into the list of car thread (owned by the supervisor).
            CarsThread th = new CarsThread(actBarrier, stepBarrier, dt, simulation);
            carsThreads.add(th);

            // Scroll n cars and assign them to the thread created above.
            for (int j = 0; j < carsPerThread; j++) {
                th.addCar(iter.next());
            }

            if (remainingCars > 0) {    // If the cars cannot be evenly distributed on all threads, the remaining ones are distributed each to a different thread.
                remainingCars--;
                th.addCar(iter.next());
            }
        }
    }

    public void createTrafficLights(List<TrafficLight> trafficLights) {
        // Method that creates threads for trafficlights and assign them n trafficlights.
        this.trafficLightsThreads.clear();  // Necessary because of reset button.
        var iter = trafficLights.iterator();    // Iterator of trafficlights.
        int lightsPerThread = trafficLights.size() / this.nThreadsForTrafficLights;
        int remainingLights = trafficLights.size() % this.nThreadsForTrafficLights;

        for (int i = 0; i < nThreadsForTrafficLights; i++) {    // Scroll n threads.
            // Create new trafficlight thread and add it into the list of traffic light threads (owned by the supervisor).
            TrafficLightsThread th = new TrafficLightsThread(actBarrier, stepBarrier, dt, simulation);
            this.trafficLightsThreads.add(th);

            // Scroll n trafficlights and assign them to the thread created above.
            for (int j = 0; j < lightsPerThread; j++) {
                th.addTrafficLight(iter.next());
            }
            if (remainingLights > 0) {  // If the traffic lights cannot be evenly distributed on all threads, the remaining ones are distributed each to a different thread.
                th.addTrafficLight(iter.next());
                remainingLights--;
            }
        }
    }

    public void runAllThreads() {
		carsThreads.forEach(th -> {
            th.initCars(this.env);
            th.start();
        });
        
        trafficLightsThreads.forEach(th -> {
            th.initTrafficLights(this.env);
            th.start();
        });

        // Start the supervisor thread.
        new Thread(() -> {
            long startWallTime = System.currentTimeMillis();
            int stepsDone = 0;
            int t = this.t0;
            long timePerStep = 0;
            long startStepTime = 0;

            while (stepsDone < this.nSteps && !this.simulation.isStopped()) {   // Checks if the simulation must stop.
                this.stepBarrier.waitBeforeActing();    // Barrier aligns before starting the new step.

                if(startStepTime!=0){
                    timePerStep += System.currentTimeMillis() - startStepTime;
                }
                t += this.dt;
                currentWallTime = System.currentTimeMillis();
                this.simulation.notifySimulationStep(t);

                if(nStepsPerSec>0){
                    syncWithWallTime();
                }
                stepsDone++;
                startStepTime = System.currentTimeMillis();
            }

            // Exited the loop, the simulation must stop, so the barrier waits that all cars and lights have stopped and after execute the runnable.
            this.stepBarrier.waitBeforeActing(() -> this.simulation.stop());

            timePerStep += System.currentTimeMillis() - startStepTime;
            totalTime = System.currentTimeMillis() - startWallTime;
            System.out.println("Completed in " + totalTime + " ms - average time per step: " + (timePerStep/nSteps));
        }).start();
    }

    public long getTime(){
        return this.totalTime;
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