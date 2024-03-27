package pcd.ass01.simtrafficbase;

import pcd.ass01.simengineseq.AbstractEnvironment;

import java.util.ArrayList;
import java.util.List;

public class TrafficLightsThread extends Thread{
    private final Barrier barrier;
    private final Barrier stepBarrier;
    private final List<TrafficLight> trafficLights;
    private boolean interrupted = false;
    private final int dt;

    public TrafficLightsThread(Barrier barrier, Barrier eventBarrier, int dt){
        super();
        this.barrier = barrier;
        this.stepBarrier = eventBarrier;
        this.trafficLights = new ArrayList<>();
        this.dt = dt;
    }

    public void requestInterrupt() {
        this.interrupted = true;
    }

    public void addTrafficLight(TrafficLight tl) {
        this.trafficLights.add(tl);
    }

    public void initTrafficLight(AbstractEnvironment env) {
        for (var a: trafficLights) {
            a.init();
        }
    }

    public void step() {
        barrier.waitBeforeActing(); // Attende che le macchinine abbiano fatto fase sense/decide
        //barrier.waitBeforeActing(); // Attende che le macchinine abbiano fatto act
        this.trafficLights.forEach(tl -> tl.step(this.dt)); // Il loro step viene così fatto in contemporanea alla fase act delle macchinine.

    }

    public void run() {
        while(!interrupted) {

            stepBarrier.waitBeforeActing();     // Attende l'ok per eseguire il passo.

            if (interrupted) {
                return;
            }

            this.step();
        }
    }
}
