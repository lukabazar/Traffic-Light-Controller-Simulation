package logic;

import GUI.TrafficGUI;
import javafx.scene.image.ImageView;
import javafx.stage.Screen;
import javafx.util.Pair;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

public class SysMan2 implements Runnable{

    private double tileSize;
    private static int carID = 0;
    protected static int sleepDelay = 3000;
    private static int currentCars = 0;
    private static int maxNumCars = 100;
    protected static double createVehicleProbability = 0.55;
    protected static double createEMSProbability = 0.25;
    private Random rand = new Random();
    private static Direction directions[] = {Direction.NORTH, Direction.SOUTH
            , Direction.EAST,
            Direction.WEST};

    protected static CopyOnWriteArrayList<Car> carList =
            new CopyOnWriteArrayList<>();
    public static CopyOnWriteArrayList<Intersection> intersectionList =
            new CopyOnWriteArrayList<>();

    private CopyOnWriteArrayList<Thread> carThreads =
            new CopyOnWriteArrayList<>();
    private CopyOnWriteArrayList<Thread> intersectionThreads =
            new CopyOnWriteArrayList<>();



    public SysMan2(){
        this.tileSize = TrafficGUI.getTileSize();
        createIntersections();
    }


    private void createIntersections() {
        int id = 0;
        for (int i = 0; i < 3; i += 2) {
            for (int j = 0; j < 5; j += 2) {
                intersectionList.add(new Intersection(
                        id,
                        new Point((i * 200) + 100, (j * 200) + 100))
                );

                Thread intersectionThread =
                        new Thread(intersectionList.get(id));
                intersectionThreads.add(intersectionThread);
                intersectionThread.start();
                id++;
            }
        }
    }


    private void createVehicle(){
        //RNG EMS vehicle creation.
        rand = new Random();
        double EMS_RNG = rand.nextDouble();
        int RNG;

        RNG = rand.nextInt(4);
        int startingX = 0;
        int startingY = 0;
        Direction dir = directions[RNG];

        ArrayList<Intersection> tempList = new ArrayList<>();
        Pair<Point,Lane> spawn;

        switch (dir) {
            case NORTH:
                    tempList.add(intersectionList.get(3));
                    tempList.add(intersectionList.get(4));
                    tempList.add(intersectionList.get(5));
                    break;
            case SOUTH:
                    tempList.add(intersectionList.get(0));
                    tempList.add(intersectionList.get(1));
                    tempList.add(intersectionList.get(2));
                    break;
            case EAST:
                    tempList.add(intersectionList.get(0));
                    tempList.add(intersectionList.get(3));
                    break;
            case WEST:
                    tempList.add(intersectionList.get(2));
                    tempList.add(intersectionList.get(5));
        };

        RNG = rand.nextInt(tempList.size());
        spawn = tempList.get(RNG).getSpawn(dir);

        Car car;
        if(EMS_RNG < createEMSProbability){
            // EMS go here, duplicate for now
            car = new Car(carID, spawn.getKey(), dir, spawn.getValue(),
                                           tileSize);
        }
        else{
            car = new Car(carID, spawn.getKey(), dir, spawn.getValue(),
                              tileSize);
        }
        //System.out.println(car);
        carList.add(car);
        currentCars++;
        carID++;
    }

    private void createDMS(){}

    private int RNGCarRoll(){

        int NumOfCarsToCreate = 0;
        int probability;

        while(true){
            probability = (int)(Math.random()*100);
            //System.out.println("RNGCarRoll: " + probability + " max prop: " + createVehicleProbability);
            if(NumOfCarsToCreate + currentCars >= maxNumCars) {
                break;
            }

            if(probability < createVehicleProbability){
                NumOfCarsToCreate++;
                //System.out.println("number of cars to make: " + NumOfCarsToCreate);
            }

            else if(probability > createVehicleProbability){
                break;
            }
        }
        //System.out.println("number of cars to make: " + NumOfCarsToCreate);
        return NumOfCarsToCreate;
    }

    private void removeVehicles(){
        int size = carThreads.size();
        if(size > 0){
            for(int i = 0; i < size-1; i++){
                if(!carThreads.get(i).isAlive()){
                    //System.out.println("should remove car");
                    carThreads.remove(i);
                    carList.remove(i);
                    currentCars--;
                    break;
                }
            }
        }
    }

    @Override
    public void run() {
        int numCarCreate = 0;
        createIntersections();
        createDMS();

        while(true){
            try {
                //Add if statement for max number of cars
                numCarCreate = RNGCarRoll();
                for(int i = 0; i < numCarCreate; i++){
                    createVehicle();
                    TimeUnit.SECONDS.sleep(3);
                }
                removeVehicles();
                Thread.sleep(sleepDelay);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
