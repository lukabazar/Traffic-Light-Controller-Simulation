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

public class SystemManager implements Runnable{

    private double tileSize;
    private static int carID = 0;
    protected static int sleepDelay = 1000;
    private static int currentCars = 0;
    private static int maxNumCars = 100;
    protected static double createVehicleProbability = 0.40;
    protected static double createEMSProbability = 0.05;
    private Random rand = new Random();
    private static Direction directions[] = {Direction.NORTH, Direction.SOUTH
            , Direction.EAST,
            Direction.WEST};

    public static CopyOnWriteArrayList<Vehicle> vehicleList =
            new CopyOnWriteArrayList<>();
    public static CopyOnWriteArrayList<Intersection> intersectionList =
            new CopyOnWriteArrayList<>();

    private CopyOnWriteArrayList<Thread> vehicleThreads =
            new CopyOnWriteArrayList<>();
    private CopyOnWriteArrayList<Thread> intersectionThreads =
            new CopyOnWriteArrayList<>();

    public static CopyOnWriteArrayList<EMS> EMSList =
            new CopyOnWriteArrayList<>();

    public static CopyOnWriteArrayList<Thread> EMSThreads =
            new CopyOnWriteArrayList<>();



    public SystemManager(){
        this.tileSize = TrafficGUI.getTileSize();
        //createIntersections();
        Thread thread = new Thread(this);
        thread.start();
    }


    private void createIntersections() {

        int id = 0;
        for (int i = 0; i < 3; i += 2) {
            for (int j = 0; j < 5; j += 2) {
                intersectionList.add(new Intersection(
                        id,
                        new Point((j * 200) + 100, (i * 200) + 100))
                );
                int x1 = (j * 200) + 100;
                int y1 = (i * 200) + 100;

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

        RNG = rand.nextInt(directions.length);
        int startingX = 0;
        int startingY = 0;
        Direction dir = directions[RNG];

        ArrayList<Integer> tempList = new ArrayList<>();
        tempList.clear();
        Pair<Point,Lane> spawn;


        switch (dir) {
            case NORTH:
                tempList.add(3);
                tempList.add(4);
                tempList.add(5);
                break;
            case SOUTH:
                tempList.add(0);
                tempList.add(1);
                tempList.add(2);
                break;
            case EAST:
                tempList.add(0);
                tempList.add(3);
                break;
            case WEST:
                tempList.add(2);
                tempList.add(5);
        };

        RNG = rand.nextInt(tempList.size());
        spawn = intersectionList.get(tempList.get(RNG)).getSpawn(dir);

        Car car;
        EMS ems;
        if(EMS_RNG < createEMSProbability){
            // EMS go here, duplicate for now
            ems = new EMS(spawn.getKey(), dir, spawn.getValue(),
                          tileSize);
            Thread emsThread = new Thread(ems);
            EMSList.add(ems);

            emsThread.start();

            EMSThreads.add(emsThread);
        }
        else{
            car = new Car(carID, spawn.getKey(), dir, spawn.getValue(),
                          tileSize);
            vehicleList.add(car);
            Thread carThread = new Thread(car);

            carThread.start();

            vehicleThreads.add(carThread);
        }

        currentCars++;
        carID++;

    }

    private void createDMS(){}

    private int RNGCarRoll(){

        int NumOfCarsToCreate = 1;
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
        //.println(NumOfCarsToCreate);
        return NumOfCarsToCreate;
    }

    private void removeVehicles(){
        int size = vehicleThreads.size();
        if(size > 0){
            for(int i = 0; i < size-1; i++){
                if(!vehicleThreads.get(i).isAlive()){
                    //System.out.println("should remove car");
                    vehicleThreads.remove(i);
                    vehicleList.remove(i);
                    currentCars--;
                    break;
                }
            }
        }
    }

    @Override
    public void run() {
        int numCarCreate = 1;
        createIntersections();
        createDMS();

        while(true){
            try {
                //Add if statement for max number of cars
                numCarCreate = RNGCarRoll();
                for(int i = 0; i < numCarCreate; i++){
                    createVehicle();
                    Thread.sleep(300);
                }
                for(int  j = 0; j < vehicleList.size(); j++) {
                    removeVehicles();
                }
                Thread.sleep(300);
                //System.out.println(carList.size());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
