package logic;

import GUI.TrafficGUI;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.util.Pair;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class Intersection implements Runnable {
    private final long greenRedDuration = 8000;
            // green and red light have minimum of 5 second duration
    private final long yellowDuration = 3500;
            // yellow light minimum of 2 second duration
    private final long accLimit = 10;// number of cars required to change light
    private final long minLength = 4000;
            //minimum time spent during red or green light
    private long lightChangeTime; // time of previous light change
    private final int intersectionNumber; //
    private LightDirection northSouthDir; // direction of north, south lights
    private LightDirection eastWestDir; // direction fo east, west lights
    private LightColor northSouthColor; // color of the north and south lights
    private LightColor eastWestColor; // color of east and west lights

    private boolean construction = true;

    private Point center;
    private int northStop, southStop, eastStop, westStop;
            // northStop = boundary incoming cars from NORTH may not cross
            // when RED
    private int northBarrier, southBarrier, eastBarrier, westBarrier;
            // north/south refer to a Y coordinate, east/west X coordinate
    private ArrayList<Point> exits = new ArrayList<>();
    private ArrayList<Point> spawns = new ArrayList<>();

    private ImageView[] images;
    private int eastWestAcc;
    private int northSouthAcc;


    // assuming that there will be some sort of number assigned to an
    // intersection so that we can differentiate btwn them
    public Intersection(int id, Point center) {
        Random rand = new Random();
        this.lightChangeTime = System.currentTimeMillis();
        this.intersectionNumber = id;
        this.northSouthDir = LightDirection.NORTHSOUTH;
        this.eastWestDir = LightDirection.EASTWEST;

        if (rand.nextDouble() < 0.5) {
            this.northSouthColor = LightColor.GREEN;
            this.eastWestColor = LightColor.RED;

        } else {
            this.northSouthColor = LightColor.RED;
            this.eastWestColor = LightColor.GREEN;

        }

        this.eastWestAcc = 0; //traffic accumulator
        this.northSouthAcc = 0;
        this.images = TrafficGUI.intersectionImages;

        this.center = center;

        this.northStop = (int) center.getY() - 60;
        this.southStop = (int) center.getY() + 59;
        this.westStop = (int) center.getX() - 60;
        this.eastStop = (int) center.getX() + 59;

        this.northBarrier = (int) center.getY() - 40;
        this.southBarrier = (int) center.getY() + 39;
        this.eastBarrier = (int) center.getX() + 39;
        this.westBarrier = (int) center.getX() - 40;

        this.spawns.add(new Point(new Point(
                (int) center.getX() - 30,
                (int) center.getY() - 110)));
        this.spawns.add(new Point(new Point(
                (int) center.getX() - 10,
                (int) center.getY() - 110)));

        this.spawns.add(new Point(new Point(
                (int) center.getX() + 110,
                (int) center.getY() - 30)));
        this.spawns.add(new Point(new Point(
                (int) center.getX() + 110,
                (int) center.getY() - 10)));

        this.spawns.add(new Point(new Point(
                (int) center.getX() + 28,
                (int) center.getY() + 110)));
        this.spawns.add(new Point(new Point(
                (int) center.getX() + 9,
                (int) center.getY() + 110)));

        this.spawns.add(new Point(new Point(
                (int) center.getX() - 110,
                (int) center.getY() + 28)));
        this.spawns.add(new Point(new Point(
                (int) center.getX() - 110,
                (int) center.getY() + 9)));


        // adding EXITS in clockwise order, important for future indexed get(i)
        // NORTH, EAST, SOUTH, WEST
        this.exits.add(new Point(
                (int) center.getX() + 9,
                this.northBarrier));//northLeft
        this.exits.add(new Point(
                (int) center.getX() + 28,
                this.northBarrier)); //northRight

        this.exits.add(new Point(
                this.eastBarrier,
                (int) center.getY() - 10));//eastLeft
        this.exits.add(new Point(
                this.eastBarrier,
                (int) center.getY() - 30));//eastRight

        this.exits.add(new Point(
                (int) center.getX() - 10,
                this.southBarrier)); //southLeft
        this.exits.add(new Point(
                (int) center.getX() - 30,
                this.southBarrier)); // southRight

        this.exits.add(new Point(
                this.westBarrier,
                (int) center.getY() + 9));//westLeft
        this.exits.add(new Point(
                this.westBarrier,
                (int) center.getY() + 28));//westRight

        setImages();
        construction = false;

    }

    public enum LightColor {
        RED,
        YELLOW,
        GREEN
    }

    private enum LightDirection {
        NORTHSOUTH,
        EASTWEST
    }

    @Override
    public void run() {
        while (true) {
            updateIntersection();

            try {
                Thread.sleep(100);//ms
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void setImages() {
        long currentTime = System.currentTimeMillis();
        if (images != null) {

            if (eastWestColor == LightColor.GREEN &&
                    northSouthColor == LightColor.RED && !construction) {
                String[] greenred = {"greenredppl1.png","greenredppl2.png","greenredppl3.png",
                        "greenredppl4.png","greenredppl5.png","greenredppl6.png","greenRed.png"};
                for(int i=0; i<7; i++){
                    images[intersectionNumber].setImage((new Image(greenred[i])));
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }


            }
            if (eastWestColor == LightColor.RED &&
                    northSouthColor == LightColor.GREEN && !construction) {
                String[] redgreenppl = {"redgreenppl1.png",
                        "redgreenppl2.png", "redgreenppl3.png","redgreenppl1.png",
                        "redgreenppl4.png","redgreenppl5.png", "redgreen.png"};
                for(int i=0; i<7; i++){
                    images[intersectionNumber].setImage(new Image(redgreenppl[i]));
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }


            }
            if (eastWestColor == LightColor.RED &&
                    northSouthColor == LightColor.YELLOW) {

                images[intersectionNumber].setImage(new Image("redyellow.png"));

            }
            if (eastWestColor == LightColor.YELLOW &&
                    northSouthColor == LightColor.RED) {

                images[intersectionNumber].setImage(new Image("yellowred.png"));

            }
        }
    }

    private LightColor oppositeLight(LightColor color) {
        if (color == LightColor.GREEN) {
            return LightColor.YELLOW;
        } else if (color ==
                LightColor.YELLOW) {//keeps the other light red for duration
            // of yellow
            return LightColor.RED;
        } else {
            return LightColor.GREEN;
        }
    }

    // just changes color of the lights
    private void changeLight(LightDirection direction, LightColor newColor) {
        if (direction == LightDirection.NORTHSOUTH) {
            northSouthColor = newColor;
            eastWestColor = oppositeLight(newColor);
        } else if (direction == LightDirection.EASTWEST) {
            if(oppositeLight(newColor) == LightColor.YELLOW &&
                    newColor == LightColor.GREEN) {
                eastWestColor = LightColor.RED;
                northSouthColor = oppositeLight(newColor);
            } else {
                eastWestColor = newColor;
                northSouthColor = oppositeLight(newColor);
            }
        }
        setImages();
        lightChangeTime = System.currentTimeMillis();
    }

    public LightColor getEWState() {
        return eastWestColor;
    }

    public LightColor getNSState() {
        return northSouthColor;
    }

    public int getID() {
        return intersectionNumber;
    }

    // changes state of intersection based on time, will call changeLight method
    private void updateIntersection() {
        long currentTime = System.currentTimeMillis();
        if ((((eastWestColor == LightColor.GREEN ||
                eastWestColor == LightColor.RED) ||
                (northSouthColor == LightColor.GREEN ||
                        northSouthColor == LightColor.RED))
                && (currentTime - lightChangeTime >= greenRedDuration))
                || (eastWestAcc >= accLimit &&
                currentTime - lightChangeTime >= minLength)) {

            changeLight(eastWestDir, oppositeLight(eastWestColor));

        }
       /* if (((northSouthColor == LightColor.GREEN || northSouthColor ==
       LightColor.RED) && (currentTime-lightChangeTime>= greenRedDuration))
                || (northSouthAcc>=accLimit && currentTime-lightChangeTime >=
                 minLength)){
            changeLight(northSouthDir, oppositeLight(northSouthColor));
            //System.out.printf("red or green %d\n", intersectionNumber);
        }*/
        if (northSouthAcc >= accLimit &&
                currentTime - lightChangeTime >= minLength) {
            changeLight(northSouthDir, oppositeLight(northSouthColor));
        }

        //handles yellow light timing (unaffected by volume)
        if (eastWestColor == LightColor.YELLOW &&
                currentTime - lightChangeTime >= yellowDuration) {
            eastWestColor = LightColor.RED;
            northSouthColor = LightColor.GREEN;
            setImages();
            lightChangeTime = System.currentTimeMillis();
        } else if (northSouthColor == LightColor.YELLOW &&
                currentTime - lightChangeTime >= yellowDuration) {
            northSouthColor = LightColor.RED;
            eastWestColor = LightColor.GREEN;
            setImages();
            lightChangeTime = System.currentTimeMillis();
        }

    }


    public int getNorthStop() {
        return this.northStop;
    }

    public int getSouthStop() {
        return this.southStop;
    }

    public int getEastStop() {
        return this.eastStop;
    }

    public int getWestStop() {
        return this.westStop;
    }

    public int getNorthBarrier() {
        return this.northBarrier;
    }

    public int getSouthBarrier() {
        return this.southBarrier;
    }

    public int getEastBarrier() {
        return this.eastBarrier;
    }

    public int getWestBarrier() {
        return this.westBarrier;
    }

    // Parameter: dir = current direction of incoming car
    // Returns destination point for Car
    public Pair<Point, Lane> getSpawn(Direction dir) {
        ArrayList<Point> temp = new ArrayList<>();
        Lane tempLane;
        Random random = new Random();
        int choice = random.nextInt(2);
        if (choice == 0) {
            tempLane = Lane.RIGHT;
        } else {
            tempLane = Lane.LEFT;
        }
        switch (dir) {
            case NORTH -> {
                temp.add(spawns.get(4));
                temp.add(spawns.get(5));
                System.out.println(temp);
                // code block
                return new Pair<>(temp.get(choice), tempLane);
            }
            case SOUTH -> {
                temp.add(spawns.get(0));
                temp.add(spawns.get(1));
                System.out.println(temp);
                // code block
                return new Pair<>(temp.get(choice), tempLane);
            }
            case EAST -> {
                temp.add(spawns.get(6));
                temp.add(spawns.get(7));
                System.out.println(temp);
                // code block
                return new Pair<>(temp.get(choice), tempLane);
            }
            case WEST -> {
                temp.add(spawns.get(2));
                temp.add(spawns.get(3));
                System.out.println(temp);
                // code block
                return new Pair<>(temp.get(choice), tempLane);
            }
        }
        return null;

    }

    // Parameter: dir = current direction of incoming car
    // Returns destination point for Car
    public Point getLeftTurn(Direction dir) {
        return switch (dir) {
            case NORTH ->
                    // code block
                    exits.get(6);
            case SOUTH ->
                    // code block
                    exits.get(2);
            case EAST ->
                    // code block
                    exits.get(0);
            case WEST ->
                    // code block
                    exits.get(4);
            // add more cases as needed
        };
    }

    // Parameter: dir = current direction of incoming car
    public Point getRightTurn(Direction dir) {
        return switch (dir) {
            case NORTH ->
                    // code block
                    exits.get(3);
            case SOUTH ->
                    // code block
                    exits.get(7);
            case EAST ->
                    // code block
                    exits.get(5);
            case WEST ->
                    // code block
                    exits.get(1);
            // add more cases as needed
        };
    }


}
