package pcd.ass01.simtrafficexamples;

/**
 * 
 * Main class to create and run a simulation
 * 
 */
public class RunTrafficSimulation {

	public static void main(String[] args) {		

		int nThreads = Runtime.getRuntime().availableProcessors();
		System.err.println("Numero Thread = " + nThreads);
		//var simulation = new TrafficSimulationSingleRoadTwoCars(nThreads);
		//var simulation = new TrafficSimulationSingleRoadSeveralCars(nThreads);
		 var simulation = new TrafficSimulationSingleRoadWithTrafficLightTwoCars(nThreads);
		// var simulation = new TrafficSimulationWithCrossRoads(nThreads);
		//simulation.setup();
		
		RoadSimStatistics stat = new RoadSimStatistics();
		RoadSimView view = new RoadSimView(simulation);
		view.display();

		simulation.addSimulationListener(stat);
		simulation.addSimulationListener(view);
	}
}
