package pcd.ass01.simtrafficbase;

import pcd.ass01.simengineseq.AbstractAgent;

public class CarAgentThread extends Thread {

    private final AbstractAgent carAgent;
    private int t = 0;
    private final int dt;
    private final int nSteps;

	/* in the case of sync with wall-time */
	private final int nStepsPerSec;

    /* for time statistics*/
	private long currentWallTime;

    public CarAgentThread(final AbstractAgent carAgent, int dt, int nSteps, int nStepsPerSec) {
        super();
        this.carAgent = carAgent;
        this.dt = dt;
        this.nSteps = nSteps;
        this.nStepsPerSec = nStepsPerSec;
    }

    @Override
    public void start() {
        int stepsDone = 0;
        while (stepsDone < this.nSteps) {
            this.currentWallTime = System.currentTimeMillis(); // Setta il tempo globale per ciascuan macchina ad ogni step.
            
            this.carAgent.step(dt);
            this.t += this.dt;

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
