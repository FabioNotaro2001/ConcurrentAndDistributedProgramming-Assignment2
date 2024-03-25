package pcd.ass01.simtrafficexamples;

import pcd.ass01.simengineseq.AbstractSimulation;
import pcd.ass01.simtrafficbase.CarsThreadsSupervisor;
import pcd.ass01.simtrafficbase.P2d;
import pcd.ass01.simtrafficbase.Road;
import pcd.ass01.simtrafficbase.RoadsEnv;

/**
 * 
 * Traffic Simulation about a number of cars 
 * moving on a single road, no traffic lights
 * 
 */
public class TrafficSimulationSingleRoadSeveralCars extends AbstractSimulation {

	private final CarsThreadsSupervisor threadsSupervisor;

	public TrafficSimulationSingleRoadSeveralCars(int nThreads) {
		super();
		this.threadsSupervisor = new CarsThreadsSupervisor(nThreads, this);
	}
	
	public void setup() {

		RoadsEnv env = new RoadsEnv(this);
		this.setupEnvironment(env);
		this.setupTimings(0, 1);


		
		Road road = env.createRoad(new P2d(0,300), new P2d(1500,300));

		int nCars = 30;

		for (int i = 0; i < nCars; i++) {
			
			String carId = "car-" + i;
			// double initialPos = i*30;
			double initialPos = i*10;
			
			double carAcceleration = 1; //  + gen.nextDouble()/2;
			double carDeceleration = 0.3; //  + gen.nextDouble()/2;
			double carMaxSpeed = 7; // 4 + gen.nextDouble();
						
			
		}
		
		this.syncWithTime(25);
	}

	@Override
	public void run(int nSteps) {
		this.threadsSupervisor.setSteps(nSteps);
		super.run(nSteps);
	}

	@Override
	protected void setupTimings(int t0, int dt) {
		super.setupTimings(t0, dt);
		this.threadsSupervisor.setTimings(dt);
	}

	@Override
	protected void syncWithTime(int nCyclesPerSec) {
		super.syncWithTime(nCyclesPerSec);
		this.threadsSupervisor.setStepsPerSec(nCyclesPerSec);
	}

	protected void setupEnvironment(RoadsEnv env) {
		super.setupEnvironment(env);
		this.threadsSupervisor.setEnvironment(env);
	}

	@Override
	public synchronized void stop() {
		super.stop();
		this.threadsSupervisor.stopAllThreads();
	}
}
	