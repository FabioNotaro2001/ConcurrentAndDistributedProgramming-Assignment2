package pcd.ass01.simengineseq;

import java.util.ArrayList;
import java.util.List;

import pcd.ass01.simtrafficbase.SimThreadsSupervisor;
import pcd.ass01.simtrafficbase.StopMonitor;


/**
 * Base class for defining concrete simulations
 *  
 */
public abstract class AbstractSimulation {

	/* environment of the simulation */
	private AbstractEnvironment env;
		
	/* list of the agents */
	private List<AbstractAgent> agents;
		
	/* simulation listeners */
	private List<SimulationListener> listeners;

	/* logical time step */
	private int dt;
	
	/* initial logical time */
	private int t0;

	/* in the case of sync with wall-time */
	private boolean toBeInSyncWithWallTime;
	private int nStepsPerSec;
	
	/* for time statistics*/
	private long currentWallTime;
	private long startWallTime;
	private long endWallTime;
	private long averageTimePerStep;

	// New field that changes when the stop button is pressed.
	private volatile Boolean stopRequested = false;
	private StopMonitor stopMonitor = new StopMonitor();	// Monitor useful for stopping the simulation without race condition.

	private boolean isRandom;	// Specifies if the simulation should include randomness.

	private final int randomSeed = 1;	// Constant random seed for reproducibility.

	protected AbstractSimulation(boolean isRandom) {
		agents = new ArrayList<AbstractAgent>();
		listeners = new ArrayList<SimulationListener>();
		toBeInSyncWithWallTime = false;
		this.isRandom = isRandom;
	}
	
	/**
	 * 
	 * Method used to configure the simulation, specifying env and agents
	 * 
	 */
	public abstract void setup();
	
	/**
	 * Method running the simulation for a number of steps,
	 * using a sequential approach
	 * 
	 * @param numSteps
	 */
	public void run(int numSteps) {

		this.stopRequested = false;	// Shared variable.

		startWallTime = System.currentTimeMillis();

		/* initialize the env and the agents inside */
		int t = t0;

		env.setnSteps(numSteps);
		env.init();
		

		this.notifyReset(t, agents, env);
		
		long timePerStep = 0;
		
		endWallTime = System.currentTimeMillis();
		this.averageTimePerStep = timePerStep / numSteps;
	}
	
	public long getSimulationDuration() {
		return endWallTime - startWallTime;
	}
	
	/* methods for configuring the simulation */
	
	protected void setupTimings(int t0, int dt) {
		this.dt = dt;
		this.t0 = t0;
		this.env.setDt(dt);
	}
	
	protected void syncWithTime(int nCyclesPerSec) {
		this.toBeInSyncWithWallTime = true;
		this.nStepsPerSec = nCyclesPerSec;
		this.env.setCyclesPerSec(nCyclesPerSec);
	}
		
	protected void setupEnvironment(AbstractEnvironment env) {
		this.env = env;
	}

	protected void addAgent(AbstractAgent agent) {
		agents.add(agent);
	}
	
	/* methods for listeners */
	
	public void addSimulationListener(SimulationListener l) {
		this.listeners.add(l);
	}
	
	private void notifyReset(int t0, List<AbstractAgent> agents, AbstractEnvironment env) {
		for (var l: listeners) {
			l.notifyInit(t0, agents, env);
		}
	}

	private void notifyNewStep(int t, List<AbstractAgent> agents, AbstractEnvironment env) {
		for (var l: listeners) {
			l.notifyStepDone(t, agents, env);
		}
	}

	public boolean isStopped(){
		// We use the monitor for correct reading the shared variable.
		this.stopMonitor.requestRead();
		boolean state = this.stopRequested;
		this.stopMonitor.releaseRead();
		return state;
	}

	public void stop(){
		// We use the monitor for correct writing the shared variable.
		this.stopMonitor.requestWrite();
		this.stopRequested = true;
		this.stopMonitor.releaseWrite();
	}

	public void notifySimulationStep(int t){
		notifyNewStep(t, agents, env);
	}	// Useful for GUI.

	public boolean mustBeRandom(){
		return this.isRandom;
	}

	public int getRandomSeed(){
		return this.randomSeed;
	}
}
