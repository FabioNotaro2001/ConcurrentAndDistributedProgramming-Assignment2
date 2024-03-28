package pcd.ass01.simtrafficexamples;

import java.util.ArrayList;
import java.util.List;

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

	public TrafficSimulationSingleRoadTwoCars(int nThreads) {
		super();
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

		CarAgent car1 = new CarAgentBasic("car-1", env, r, 0, 0.1, 0.2, 8);
		this.addAgent(car1);		
		CarAgent car2 = new CarAgentBasic("car-2", env, r, 100, 0.1, 0.1, 7);
		this.addAgent(car2);

		cars.add(car1);
		cars.add(car2);

		supervisor.createCars(cars);


		/* sync with wall-time: 25 steps per sec */
		this.syncWithTime(25);
	}	
	
	@Override
	public void run(int nSteps) {
		this.supervisor.setSteps(nSteps);
		this.supervisor.runAllThreads();
		super.run(nSteps);
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
	public synchronized void stop() {
		super.stop();
	}
}
