package pcd.ass01.simtrafficexamples;

import java.util.ArrayList;
import java.util.List;

import pcd.ass01.simengineseq.AbstractSimulation;
import pcd.ass01.simtrafficbase.CarAgent;
import pcd.ass01.simtrafficbase.CarAgentExtended;
import pcd.ass01.simtrafficbase.CarsThreadsSupervisor;
import pcd.ass01.simtrafficbase.P2d;
import pcd.ass01.simtrafficbase.Road;
import pcd.ass01.simtrafficbase.RoadsEnv;
import pcd.ass01.simtrafficbase.TrafficLight;

/**
 * 
 * Traffic Simulation about 2 cars moving on a single road, with one semaphore
 * 
 */
public class TrafficSimulationSingleRoadWithTrafficLightTwoCars extends AbstractSimulation {

	private final CarsThreadsSupervisor supervisor;

	public TrafficSimulationSingleRoadWithTrafficLightTwoCars(int nThreads) {
		super();
		this.supervisor = new CarsThreadsSupervisor(nThreads, this);

	}
	
	public void setup() {

		RoadsEnv env = new RoadsEnv(this);
		this.setupEnvironment(env);
		this.setupTimings(0, 1);
		

				
		Road r = env.createRoad(new P2d(0,300), new P2d(1500,300));

		TrafficLight tl = env.createTrafficLight(new P2d(740,300), TrafficLight.TrafficLightState.GREEN, 75, 25, 100);
		r.addTrafficLight(tl, 740);
		
		List<CarAgent> cars = new ArrayList<>();

		CarAgent car1 = new CarAgentExtended("car-1", env, r, 0, 0.1, 0.3, 6);
		this.addAgent(car1);		
		CarAgent car2 = new CarAgentExtended("car-2", env, r, 100, 0.1, 0.3, 5);
		this.addAgent(car2);

		cars.add(car1);

		cars.add(car2);
		supervisor.createCars(cars);

		this.syncWithTime(25);
	}	
	
	@Override
	public void run(int nSteps) {
		this.supervisor.setSteps(nSteps);
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
		this.supervisor.stopAllThreads();
	}
}
