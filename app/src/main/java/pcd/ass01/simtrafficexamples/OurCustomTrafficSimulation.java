package pcd.ass01.simtrafficexamples;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import pcd.ass01.simengineseq.AbstractSimulation;
import pcd.ass01.simtrafficbase.*;

public class OurCustomTrafficSimulation extends AbstractSimulation {

    private final SimThreadsSupervisor supervisor;


    public OurCustomTrafficSimulation(int nThreads, boolean isRandom) {
        super(isRandom);
        this.supervisor = new SimThreadsSupervisor(nThreads, 2, this);
    }

    public void setup() {
        Random gen = new Random(super.getRandomSeed());
        RoadsEnv env = new RoadsEnv(this);
        this.setupEnvironment(env);

        this.setupTimings(0, 1);

        Road r1 = env.createRoad(new P2d(0,360), new P2d(1500,360));
        TrafficLight tl1 = env.createTrafficLight(new P2d(270,360), TrafficLight.TrafficLightState.GREEN, 75, 25, 125);
        TrafficLight tl2 = env.createTrafficLight(new P2d(470,360), TrafficLight.TrafficLightState.GREEN, 75, 25, 125);
        TrafficLight tl3 = env.createTrafficLight(new P2d(1170,360), TrafficLight.TrafficLightState.RED, 75, 25, 125);
        r1.addTrafficLight(tl1, 270);
        r1.addTrafficLight(tl2, 470);
        r1.addTrafficLight(tl3, 1170);

        Road r2 = env.createRoad(new P2d(0,180), new P2d(1500,180));
        TrafficLight tl4 = env.createTrafficLight(new P2d(270,180), TrafficLight.TrafficLightState.GREEN, 75, 25, 125);
        TrafficLight tl5 = env.createTrafficLight(new P2d(470,180), TrafficLight.TrafficLightState.GREEN, 75, 25, 125);
        TrafficLight tl6 = env.createTrafficLight(new P2d(1170,180), TrafficLight.TrafficLightState.RED, 75, 25, 125);
        r2.addTrafficLight(tl4, 270);
        r2.addTrafficLight(tl5, 470);
        r2.addTrafficLight(tl6, 1170);

        Road r3 = env.createRoad(new P2d(300,0), new P2d(300,600));
        TrafficLight tl7 = env.createTrafficLight(new P2d(300,330), TrafficLight.TrafficLightState.RED, 75, 25, 125);
        TrafficLight tl8 = env.createTrafficLight(new P2d(300,150), TrafficLight.TrafficLightState.RED, 75, 25, 125);
        r3.addTrafficLight(tl7, 330);
        r3.addTrafficLight(tl8, 150);

        Road r4 = env.createRoad(new P2d(500,0), new P2d(500,600));
        TrafficLight tl9 = env.createTrafficLight(new P2d(500,330), TrafficLight.TrafficLightState.RED, 75, 25, 125);
        TrafficLight tl10 = env.createTrafficLight(new P2d(500,150), TrafficLight.TrafficLightState.RED, 75, 25, 125);
        r4.addTrafficLight(tl9, 330);
        r4.addTrafficLight(tl10, 150);

        Road r5 = env.createRoad(new P2d(1200,0), new P2d(1200,600));
        TrafficLight tl11 = env.createTrafficLight(new P2d(1200,330), TrafficLight.TrafficLightState.GREEN, 75, 25, 125);
        TrafficLight tl12 = env.createTrafficLight(new P2d(1200,150), TrafficLight.TrafficLightState.GREEN, 75, 25, 125);
        r5.addTrafficLight(tl11, 330);
        r5.addTrafficLight(tl12, 150);

        List<CarAgent> cars = new ArrayList<>();
        List<TrafficLight> lights = new ArrayList<>();

        createCarsForRoad(env, r1, 0, 5, 0.1, 0.3, 5, super.mustBeRandom() ? gen : null)
                .forEach(car -> {
                    this.addAgent(car);
                    cars.add(car);
                });

        createCarsForRoad(env, r2, 5, 5, 0.1, 0.3, 5, super.mustBeRandom() ? gen : null)
                .forEach(car -> {
                    this.addAgent(car);
                    cars.add(car);
                });

        createCarsForRoad(env, r3, 10, 5, 0.1, 0.3, 5, super.mustBeRandom() ? gen : null)
                .forEach(car -> {
                    this.addAgent(car);
                    cars.add(car);
                });

        createCarsForRoad(env, r4, 15, 5, 0.1, 0.3, 5, super.mustBeRandom() ? gen : null)
                .forEach(car -> {
                    this.addAgent(car);
                    cars.add(car);
                });

        createCarsForRoad(env, r5, 20, 5, 0.1, 0.3, 5, super.mustBeRandom() ? gen : null)
                .forEach(car -> {
                    this.addAgent(car);
                    cars.add(car);
                });

        lights.add(tl1);
        lights.add(tl2);
        lights.add(tl3);
        lights.add(tl4);
        lights.add(tl5);
        lights.add(tl6);
        lights.add(tl7);
        lights.add(tl8);
        lights.add(tl9);
        lights.add(tl10);
        lights.add(tl11);
        lights.add(tl12);

        supervisor.createCars(cars);
        supervisor.createTrafficLights(lights);

        this.syncWithTime(25);
    }

    @Override
    public void run(int nSteps) {
        this.supervisor.setSteps(nSteps);
        super.run(nSteps);
        this.supervisor.runAllThreads();
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

    private List<CarAgent> createCarsForRoad(RoadsEnv env, Road r, int carIdOffset, int nCars, double acc, double dec, double vmax, Random rand){
        List<CarAgent> result = new ArrayList<>(nCars);
        boolean isRoadHorizontal = r.getTo().y() == r.getFrom().y();
        double deltaX = isRoadHorizontal ? 20 : 0;
        double deltaY = isRoadHorizontal ? 0 : 20;
        var start = r.getFrom();

        for(int i = 0; i < nCars; i++) {
            double pos = isRoadHorizontal ? start.x() + deltaX * i : start.y() + deltaY * i;

            double carAcc = acc;
            double carDec = dec;
            double carMaxSp = vmax;
            if (rand != null) {
                carAcc += rand.nextDouble() / 2;
                carDec += rand.nextDouble() / 2;
                carMaxSp += rand.nextDouble(6);

            }
            result.add(new CarAgentExtended("car-" + (carIdOffset + i), env, r, pos, carAcc, carDec, carMaxSp));
        }
        return result;
    }
}
