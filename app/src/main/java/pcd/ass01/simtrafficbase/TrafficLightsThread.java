package pcd.ass01.simtrafficbase;

import pcd.ass01.simengineseq.AbstractEnvironment;
import pcd.ass01.simengineseq.AbstractSimulation;

import java.util.ArrayList;
import java.util.List;

public class TrafficLightsThread extends Thread{
    private final Barrier actBarrier;   // Barrier that aligns before acting.
    private final Barrier stepBarrier;  // Barrier that aligns before doing next step.
    private final List<TrafficLight> trafficLights;
    private AbstractSimulation simulation;
    private final int dt;

    public TrafficLightsThread(Barrier barrier, Barrier eventBarrier, int dt, AbstractSimulation simulation){
        super();
        this.actBarrier = barrier;
        this.stepBarrier = eventBarrier;
        this.trafficLights = new ArrayList<>();
        this.dt = dt;
        this.simulation = simulation;
    }
    
    public void addTrafficLight(TrafficLight tl) {
        this.trafficLights.add(tl);
    }

    public void initTrafficLights(AbstractEnvironment env) {
        for (var a: trafficLights) {
            a.init();
        }
    }

    public void step() {
        this.trafficLights.forEach(tl -> tl.step(this.dt));
        actBarrier.waitBeforeActing();  // Barrier waits that all cars have completed sense/decide phase.
        actBarrier.waitBeforeActing();  // Barrier waits that all cars have completed act phase
    }


    public void run() {
        while(true) {
            stepBarrier.waitBeforeActing(); // Barrier that aligns before doing next step.
            if(simulation.isStopped())
                break;
            this.step();
        }
    }
}
