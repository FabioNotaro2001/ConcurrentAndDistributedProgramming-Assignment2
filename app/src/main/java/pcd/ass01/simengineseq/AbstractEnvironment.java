package pcd.ass01.simengineseq;

// TODO: Gli elementi dell'environment devono essere Thread a loro volta

/**
 *   
 * Base class to define the environment of the simulation
 *   
 */
public abstract class AbstractEnvironment {

	private String id;
	private int dt;
	private int nSteps;
	private int nCyclesPerSec;

	protected AbstractEnvironment(String id) {
		this.id = id;		
	}
	
	public String getId() {
		return id;
	}

	public int getDt() {
		return dt;
	}

	public void setDt(int dt) {
		this.dt = dt;
	}


	public int getnSteps() {
		return nSteps;
	}

	public void setnSteps(int nSteps) {
		this.nSteps = nSteps;
	}

	
	public int getCyclesPerSec() {
		return nCyclesPerSec;
	}

	public void setCyclesPerSec(int nCyclesPerSec) {
		this.nCyclesPerSec = nCyclesPerSec;
	}
	
	/**
	 * 
	 * Called at the beginning of the simulation
	 */
	public abstract void init();
	
	/**
	 * 
	 * Called at each step of the simulation
	 * 
	 * @param dt
	 */
	public abstract void step(int dt);

	/**
	 * 
	 * Called by an agent to get its percepts 
	 * 
	 * @param agentId - identifier of the agent
	 * @return agent percept
	 */
	public abstract Percept getCurrentPercepts(String agentId);

	/**
	 * 
	 * Called by agent to submit an action to the environment
	 * 
	 * @param agentId - identifier of the agent doing the action
	 * @param act - the action
	 */
	public abstract void doAction(String agentId, Action act);
}
