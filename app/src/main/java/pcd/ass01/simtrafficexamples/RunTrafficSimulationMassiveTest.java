package pcd.ass01.simtrafficexamples;

public class RunTrafficSimulationMassiveTest {

	public static void main(String[] args) {		

		int numCars = 100;
		int nSteps = 1;
		int nThreads = 2;
		
		var simulation = new TrafficSimulationSingleRoadMassiveNumberOfCars(numCars, nThreads);
		simulation.setup();
		
		log("Running the simulation: " + numCars + " cars, for " + nSteps + " steps ...");
		
		simulation.run(nSteps);

		long d = simulation.getSimulationDuration();
		log("Completed in " + d + " ms - average time per step: " + simulation.getAverageTimePerCycle() + " ms");
	}
	
	private static void log(String msg) {
		System.out.println("[ SIMULATION ] " + msg);
	}
}
