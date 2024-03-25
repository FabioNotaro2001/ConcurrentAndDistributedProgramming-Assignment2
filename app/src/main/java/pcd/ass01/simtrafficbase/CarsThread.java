package pcd.ass01.simtrafficbase;


public class CarsThread extends Thread{

    private final int nCars;
    private final Barrier barrier;

    public CarsThread(int nCars, Barrier barrier){
        this.nCars = nCars;
        this.barrier = barrier;
    }

    



    
}
