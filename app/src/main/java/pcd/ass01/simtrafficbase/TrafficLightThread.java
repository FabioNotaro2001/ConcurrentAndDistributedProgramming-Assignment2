package pcd.ass01.simtrafficbase;


import pcd.ass01.simengineseq.AbstractSimulation;

public class TrafficLightThread extends Thread {

    private TrafficLight trafficLight;
    private final int dt;
    private final int nSteps;

	/* in the case of sync with wall-time */
	private final int nStepsPerSec;

    /* for time statistics*/
	private long currentWallTime;

    private AbstractSimulation simulation;

    public TrafficLightThread(final TrafficLight trafficLight, int dt, int nSteps, int nStepsPerSec, AbstractSimulation simulation) {
        super();
        this.trafficLight = trafficLight;
        this.dt = dt;
        this.nSteps = nSteps;
        this.nStepsPerSec = nStepsPerSec;
        this.simulation = simulation;
    }

    @Override
    public void run() {
        int stepsDone = 0;
        while (stepsDone < this.nSteps && !this.simulation.isStopped()) {
            this.currentWallTime = System.currentTimeMillis(); // Setta il tempo globale per ciascuan macchina ad ogni step.
            
            this.trafficLight.step(dt);

            this.syncWithWallTime(); // Aspettiamo il tempo prima del prossimo passo da fare.
            stepsDone++;
        }
    }

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
