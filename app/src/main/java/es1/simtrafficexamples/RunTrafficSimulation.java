package es1.simtrafficexamples;

/**
 * 
 * Main class to create and run a simulation
 * 
 */
public class RunTrafficSimulation {

	public static void main(String[] args) {
		int nThreads = Runtime.getRuntime().availableProcessors();
		System.out.println("Numero thread = " + nThreads);
		//var simulation = new TrafficSimulationSingleRoadTwoCars(nThreads, false);
		//var simulation = new TrafficSimulationSingleRoadSeveralCars(nThreads, false);
		//var simulation = new TrafficSimulationSingleRoadWithTrafficLightTwoCars(nThreads, false);
		//var simulation = new TrafficSimulationWithCrossRoads(nThreads, false);
		var simulation = new OurCustomTrafficSimulation(nThreads, false);
		
		RoadSimStatistics stat = new RoadSimStatistics();
		RoadSimView view = new RoadSimView(simulation);
		view.display();

		simulation.addSimulationListener(stat);
		simulation.addSimulationListener(view);
	}
}
