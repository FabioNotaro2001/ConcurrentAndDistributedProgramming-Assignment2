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

public class TrafficSimulationSingleRoadMassiveNumberOfCars extends AbstractSimulation {
	private int numCars;
	private SimThreadsSupervisor supervisor;
	private final int nThreads;

	
	public TrafficSimulationSingleRoadMassiveNumberOfCars(int numCars, int nThreads, boolean isRandom) {
		super(isRandom);
		this.numCars = numCars;
		this.nThreads = nThreads;
	}
	
	public void setup() {
		supervisor = new SimThreadsSupervisor(nThreads, 0, this);
		RoadsEnv env = new RoadsEnv(this);
		this.setupEnvironment(env);
		
		this.setupTimings(0, 1);

		Road road = env.createRoad(new P2d(0,300), new P2d(15000,300));

		List<CarAgent> cars = new ArrayList<>();

		Random gen = new Random(super.getRandomSeed());

		for (int i = 0; i < numCars; i++) {
			
			String carId = "car-" + i;
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
			
			/* no sync with wall-time */
		}
		supervisor.createCars(cars);
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

	@Override
	public long getSimulationDuration() {
		return supervisor.getTime();
	}
}
	