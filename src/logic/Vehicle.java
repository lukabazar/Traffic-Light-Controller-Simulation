package logic;

import javafx.application.Platform;
import javafx.scene.image.ImageView;

import java.awt.*;

public  abstract class Vehicle implements Runnable {
    private Direction currentDir;
    private double speed = 5;
    private float transparency = 0;
    private boolean intersectionFlag = false;
    final double minBufferDistance = 30;
    private Point location = new Point();
    private Lane lane;
    private boolean running = false;
    private ImageView imageView;
    private double imageRotation;
    private double maxSpeed = 8;
    private double tileSize;
    private int id;
    private Intersection lastIntersection = null;
    private Intersection currentIntersection = null;

    public Intersection getLastIntersection(){
        return this.lastIntersection;
    }

    public Intersection getCurrentIntersection(){
        return this.currentIntersection;
    }

    public void setCurrentIntersection(Intersection inter){
        this.currentIntersection = inter;

    }

    public void setLastIntersection(Intersection inter){
        this.lastIntersection = inter;
    }

    public int getId(){
        return this.id;
    }

    public void setId(int id){
        this.id = id;
    }


    public double getTileSize(){
        return this.tileSize;
    }

    public void setTileSize(double tileSize){
        this.tileSize = tileSize;
    }

    public Direction getDirection(){
        return currentDir;
    }

    public void setDirection(Direction dir){
        this.currentDir = dir;
    }

    public double getSpeed(){
        return this.speed;
    }

    public void setSpeed(double speed){
        this.speed = speed;
    }

    public float getTransparency(){
        return this.transparency;
    }

    public void setTransparency(float transparency){
        this.transparency = transparency;
    }

    public boolean getIntersectionFlag(){
        return this.intersectionFlag;
    }

    public double getMinBufferDistance() {
        return this.minBufferDistance;
    }

    public void flipIntersectionFlag(){
        this.intersectionFlag = !this.intersectionFlag;
    }

    public Point getLocation(){
        return this.location;
    }

    public void setLocation(Point p){
        this.location = p;
    }



    public void GUIupdate(){
        Platform.runLater(() -> {


            this.imageView.setX(this.location.x*tileSize/200);
            this.imageView.setY(this.location.y*tileSize/200);
        });

    }

    public void setLane(Lane lane){
        this.lane = lane;
    }

    public Lane getLane(){
        return this.lane;
    }

    public ImageView getImageView(){
        return this.imageView;
    }

    public void setImageView(ImageView image){
        this.imageView = image;
    }

    public void setImageRotation(Double rotation){
        this.imageRotation = rotation;
    }

    public double getImageRotation(Double rotation){
        return this.imageRotation;
    }

    public double getMaxSpeed(){
        return this.maxSpeed;
    }

    public void setMaxSpeed(int speed){
        this.maxSpeed = speed;
    }


    public abstract boolean move();


    public abstract void run();

    public abstract void stop();

    public boolean checkIntersections(){
        Point temp;
        for (Intersection inter: SystemManager.intersectionList){
            if (!inter.equals(this.lastIntersection)){
                temp = inter.getCenter();
                if (temp.distance(this.location) < 150){
                    this.currentIntersection = inter;
                    return true;
                }
            }

        }
        return false;
    }

}
