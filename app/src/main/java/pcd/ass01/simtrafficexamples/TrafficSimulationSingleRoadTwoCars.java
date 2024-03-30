package pcd.ass01.simtrafficexamples;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import pcd.ass01.simengineseq.AbstractSimulation;
import pcd.ass01.simtrafficbase.CarAgent;
import pcd.ass01.simtrafficbase.CarAgentBasic;
import pcd.ass01.simtrafficbase.SimThreadsSupervisor;
import pcd.ass01.simtrafficbase.P2d;
import pcd.ass01.simtrafficbase.Road;
import pcd.ass01.simtrafficbase.RoadsEnv;

/**
 * 
 * Traffic Simulation about 2 cars moving on a single road, no traffic lights
 * 
 */
public class TrafficSimulationSingleRoadTwoCars extends AbstractSimulation {

	private final SimThreadsSupervisor supervisor;

	public TrafficSimulationSingleRoadTwoCars(int nThreads, boolean isRandom) {
		super(isRandom);
		this.supervisor = new SimThreadsSupervisor(nThreads, 0, this);
	}
	
	public void setup() {
		
		int t0 = 0;
		int dt = 1;
		
		
		RoadsEnv env = new RoadsEnv(this);
		this.setupEnvironment(env);
		
		this.setupTimings(t0, dt);

		Road r = env.createRoad(new P2d(0,300), new P2d(1500,300));

		List<CarAgent> cars = new ArrayList<>();

		Random gen = new Random(super.getRandomSeed());

		for (int i = 0; i < 2; i++) {

			String carId = "car-" + i;
			// double initialPos = i*30;
			double initialPos = i*10;

			double carAcceleration = 0.1;
			double carDeceleration = 0.2;
			double carMaxSpeed = 8;
			if(super.mustBeRandom()){
				carAcceleration += gen.nextDouble()/2;
				carDeceleration +=  gen.nextDouble()/2;
				carMaxSpeed += 4 + gen.nextDouble(6);
			}

			CarAgent car = new CarAgentBasic(carId, env,
					r,
					initialPos,
					carAcceleration,
					carDeceleration,
					carMaxSpeed);

			cars.add(car);
			this.addAgent(car);

		}

		supervisor.createCars(cars);


		/* sync with wall-time: 25 steps per sec */
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
