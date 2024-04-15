package es1.simtrafficexamples;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import es1.simengineseq.AbstractSimulation;
import es1.simtrafficbase.CarAgent;
import es1.simtrafficbase.CarAgentBasic;
import es1.simtrafficbase.SimThreadsSupervisor;
import es1.simtrafficbase.P2d;
import es1.simtrafficbase.Road;
import es1.simtrafficbase.RoadsEnv;

/**
 * 
 * Traffic Simulation about a number of cars 
 * moving on a single road, no traffic lights
 * 
 */
public class TrafficSimulationSingleRoadSeveralCars extends AbstractSimulation {

	private final SimThreadsSupervisor supervisor;

	public TrafficSimulationSingleRoadSeveralCars(int nThreads, boolean isRandom) {
		super(isRandom);
		this.supervisor = new SimThreadsSupervisor(nThreads, this);
	}
	
	public void setup() {

		RoadsEnv env = new RoadsEnv(this);
		this.setupEnvironment(env);
		this.setupTimings(0, 1);


		
		Road road = env.createRoad(new P2d(0,300), new P2d(1500,300));

		int nCars = 30;

		List<CarAgent> cars = new ArrayList<>();

		Random gen = new Random(super.getRandomSeed());

		for (int i = 0; i < nCars; i++) {
			
			String carId = "car-" + i;
			// double initialPos = i*30;
			double initialPos = i*10;

			double carAcceleration = 1;
			double carDeceleration = 0.3;
			double carMaxSpeed = 7;
			if(super.mustBeRandom()){
				carAcceleration += gen.nextDouble()/2;
				carDeceleration +=  gen.nextDouble()/2;
				carMaxSpeed += 4 + gen.nextDouble(6);
			}
						
			CarAgent car = new CarAgentBasic(carId, env, 
									road,
									initialPos, 
									carAcceleration, 
									carDeceleration,
									carMaxSpeed);
			
			cars.add(car);
			this.addAgent(car);

		}
		supervisor.createCars(cars);

		this.syncWithTime(25);
	}

	@Override
	public void run(int nSteps) {
		this.supervisor.setSteps(nSteps);
		super.run(nSteps);
		this.supervisor.runAllThreads();
	}

	@Override
	protected void setupTimings(int t0, int dt) {
		super.setupTimings(t0, dt);
		this.supervisor.setTimings(t0, dt);
	}

	@Override
	protected void syncWithTime(int nCyclesPerSec) {
		super.syncWithTime(nCyclesPerSec);
		this.supervisor.setStepsPerSec(nCyclesPerSec);
	}

	protected void setupEnvironment(RoadsEnv env) {
		super.setupEnvironment(env);
		this.supervisor.setEnvironment(env);
	}

}
	