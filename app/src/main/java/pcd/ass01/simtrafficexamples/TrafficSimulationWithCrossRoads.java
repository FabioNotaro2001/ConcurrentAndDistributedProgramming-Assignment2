package pcd.ass01.simtrafficexamples;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import pcd.ass01.simengineseq.AbstractSimulation;
import pcd.ass01.simtrafficbase.CarAgent;
import pcd.ass01.simtrafficbase.CarAgentExtended;
import pcd.ass01.simtrafficbase.SimThreadsSupervisor;
import pcd.ass01.simtrafficbase.P2d;
import pcd.ass01.simtrafficbase.Road;
import pcd.ass01.simtrafficbase.RoadsEnv;
import pcd.ass01.simtrafficbase.TrafficLight;

public class TrafficSimulationWithCrossRoads extends AbstractSimulation {

	private final SimThreadsSupervisor supervisor;


	public TrafficSimulationWithCrossRoads(int nThreads) {
		super();
		this.supervisor = new SimThreadsSupervisor(nThreads, 2, this);
	}
	
	public void setup() {

		Random  gen = new Random(1);

		RoadsEnv env = new RoadsEnv(this);
		this.setupEnvironment(env);
		
		this.setupTimings(0, 1);
				
		TrafficLight tl1 = env.createTrafficLight(new P2d(740,300), TrafficLight.TrafficLightState.GREEN, 75, 25, 100);
		
		Road r1 = env.createRoad(new P2d(0,300), new P2d(1500,300));
		r1.addTrafficLight(tl1, 740);
		
		List<CarAgent> cars = new ArrayList<>();
		List<TrafficLight> lights = new ArrayList<>();

		CarAgent car1 = new CarAgentExtended("car-1", env, r1, 0, 0.1, 0.3, 6);
		this.addAgent(car1);		
		CarAgent car2 = new CarAgentExtended("car-2", env, r1, 100, 0.1, 0.3, 5);
		this.addAgent(car2);		
		
		TrafficLight tl2 = env.createTrafficLight(new P2d(750,290),  TrafficLight.TrafficLightState.RED, 75, 25, 100);

		Road r2 = env.createRoad(new P2d(750,0), new P2d(750,600));
		r2.addTrafficLight(tl2, 290);

		CarAgent car3 = new CarAgentExtended("car-3", env, r2, 0, 0.1, 0.2, 5);
		this.addAgent(car3);		
		CarAgent car4 = new CarAgentExtended("car-4", env, r2, 100, 0.1, 0.1, 4);
		this.addAgent(car4);
		
		cars.add(car1);
		cars.add(car2);
		cars.add(car3);
		cars.add(car4);

		lights.add(tl1);
		lights.add(tl2);

		supervisor.createCars(cars);
		supervisor.createTrafficLights(lights);

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
		this.supervisor.stopAllThreads();
	}
}
