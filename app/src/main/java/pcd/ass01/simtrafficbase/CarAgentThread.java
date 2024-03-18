package pcd.ass01.simtrafficbase;

import pcd.ass01.simengineseq.AbstractAgent;

public class CarAgentThread extends Thread {

    private final AbstractAgent carAgent;  

    public CarAgentThread(final AbstractAgent carAgent) {
        super();
        this.carAgent = carAgent;
    }

    @Override
    public void start() {
        while (true) {
            this.carAgent.step();
            //Thread.sleep(...); // Fatto meglio per limitare a X fps
        }
    }
}
