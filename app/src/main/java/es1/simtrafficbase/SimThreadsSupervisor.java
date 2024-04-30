package es1.simtrafficbase;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;

import es1.simengineseq.AbstractSimulation;

public class SimThreadsSupervisor {
    // Supervisor for managing cars and trafficlights threads.
    private List<CarAgent> carsList;
    private List<TrafficLight> trafficLightsList;

    private RoadsEnv env;

    private final ExecutorService executor; // Executor that executes the steps of all entities in the correct order.

    private AbstractSimulation simulation;
    private int nStepsPerSec = 0;
    private int nSteps = 0;
    private int dt = Integer.MAX_VALUE;

    private int t0;
    
    /* for time statistics*/
	private long currentWallTime;

    private long totalTime = 0;

    public SimThreadsSupervisor(int nThreads, AbstractSimulation simulation) { 
        this.simulation = simulation;
        this.carsList = new ArrayList<>();
        this.trafficLightsList = new ArrayList<>();
        this.executor = Executors.newFixedThreadPool( nThreads + 1); // + 1 for supervisor.
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
        this.carsList = cars;
    }

    public void createTrafficLights(List<TrafficLight> trafficLights) {
        this.trafficLightsList = trafficLights;
    }

    private void cycle() {
        // Single cycle of iteration.
        List<Future<?>> listOfFutures = new ArrayList<>();
        Consumer<Future<?>> futureGet = f -> {  // Action to be done when we need the result of a future.
            try {
                f.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        };

        // Traffic light step: for each traffic light the executor submit its step and add the future to the list.
        this.trafficLightsList.forEach(tr -> {
            listOfFutures.add(executor.submit( () -> {
                tr.step(this.dt);
            }));
        });
        listOfFutures.forEach(futureGet);   // Get of all futures in the list. Sort of barrier.

        // Car sense and decide step.
        listOfFutures.clear();
        this.carsList.forEach(cr -> {
            listOfFutures.add(executor.submit( () -> {
                cr.senseAndDecide(this.dt);
            }));
        });
        listOfFutures.forEach(futureGet);

        // Car act step.
        listOfFutures.clear();
        this.carsList.forEach(cr -> {
            listOfFutures.add(executor.submit( () -> {
                cr.act();
            }));
        });
        listOfFutures.forEach(futureGet);
        listOfFutures.clear();
    }

    public void runAllThreads() {
		carsList.forEach(c -> {
            c.init(this.env);
        });
        
        trafficLightsList.forEach(tr -> {
            tr.init();
        });

        // Starts the supervisor.
        executor.execute(() -> {
            long startWallTime = System.currentTimeMillis();
            int stepsDone = 0;
            int t = this.t0;
            long timePerStep = 0;
            long startStepTime = 0;

            try {
                while (stepsDone < this.nSteps && !this.simulation.isStopped()) {   // Checks if the simulation must stop.
                    this.cycle();
    
                    if(startStepTime!=0){
                        timePerStep += System.currentTimeMillis() - startStepTime;
                    }
                    t += this.dt;
                    currentWallTime = System.currentTimeMillis();
                    this.simulation.notifySimulationStep(t);
    
                    if(nStepsPerSec>0){
                        syncWithWallTime(); // Wait between two consecutive cycles.
                    }
                    stepsDone++;
                    startStepTime = System.currentTimeMillis();
                }
            } catch (RuntimeException e) {
                e.printStackTrace();
            } finally {
                this.simulation.stop();
                timePerStep += System.currentTimeMillis() - startStepTime;
                totalTime = System.currentTimeMillis() - startWallTime;
                System.out.println("Completed in " + totalTime + " ms - average time per step: " + (timePerStep/nSteps));
            }
        });
    }

    public long getTime(){
        return this.totalTime;
    }

    // Method that waits some time if the cycle is quicker than expected.
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