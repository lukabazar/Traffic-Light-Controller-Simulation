package logic;

import GUI.TrafficGUI;
import javafx.scene.image.ImageView;

import java.awt.*;
import java.util.Random;

public class Car extends Vehicle {

    private boolean EMS_inbound;
    private boolean running = true;
    private State state;

    public enum State {
        ROAD,
        BUFFER,
        LEFT_TURN,
        RIGHT_TURN,
        STRAIGHT
    }


    public Car(int id, Point p, Direction dir, Lane lane, double tileSize) {
        this.state = State.ROAD;
        this.setId(id);
        this.setLocation(p);
        this.setDirection(dir);
        this.setLane(lane);
        setTileSize(tileSize);
        setImageView(initImageView());
        setImageRotation(getDirection(), null);

        TrafficGUI.addCar(getImageView());
    }


    public void flipEMS_inbound() {
        this.EMS_inbound = !this.EMS_inbound;
    }


    // returns true if the car moves
    public boolean move() {
        Point tempPoint = this.getLocation();

        switch (this.state) {
            case ROAD:
                if (tempPoint.x < -50 || tempPoint.x > 1050 || tempPoint.y < -50 || tempPoint.y > 650){
                    running = false;
                    this.setLocation(new Point(-100,-100));
                    return true;
                }

                if (getCurrentIntersectionID() == -1) {
                    if (this.checkIntersections()) {
                        // queryIntersection

                    }
                } else {
                    //query intersection
                }

                // switch case query

                Point delta = this.getDirection().getDeltaDirection();
                int x = tempPoint.x + delta.x*this.getSpeed();
                int y = tempPoint.y + delta.y*this.getSpeed();

                this.setLocation(new Point(x,y));

                return true;
            case BUFFER:

                return true;

            case STRAIGHT:

                return true;
            case LEFT_TURN:

                return true;
            case RIGHT_TURN:

                return true;
        }


        //System.out.println(getId() + "" +tempPoint);




        return true;
    }

    @Override
    public void run() {
        while (running) {
            if (move()){
                this.GUIupdate();
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // do stuff

        }
    }

    public void stop() {
        this.running = false;
        Thread.currentThread().interrupt();
    }

    public ImageView initImageView() {
        Random random = new Random();
        double probability =
                random.nextDouble(); // Generates a random number between 0 and 1
        ImageView imageView;
        if (probability < 0.8) {
            setMaxSpeed(5);
            imageView =
                    new ImageView("car_1.png"); // 80% probability for car_1.png
        } else {
            setMaxSpeed(3);
            imageView =
                    new ImageView("car_2.png"); // 20% probability for car_2.png
        }
        imageView.setPreserveRatio(true);
        if (imageView.getFitHeight() >= imageView.getFitWidth()) {
            imageView.setFitHeight(getTileSize() * 0.133);
        } else {
            imageView.setFitWidth(getTileSize() * 0.133);
        }
        imageView.setTranslateX(-getTileSize() * 0.133 / 4);
        imageView.setTranslateY(-getTileSize() * 0.133 / 2);
        imageView.setX(getLocation().getX()*getTileSize()/200);
        imageView.setY(getLocation().getY()*getTileSize()/200);
        return imageView;
    }

    // rotate image before sending off, add to gui, translate/offset
    public void setImageRotation(Direction dir, Lane lane) {
        if (lane ==null) {
            switch (dir) {
                case NORTH:
                    // code block
                    getImageView().setRotate(0);
                    return;
                case SOUTH:
                    getImageView().setRotate(180);
                    return;

                case EAST:
                    getImageView().setRotate(90);
                    return;
                case WEST:
                    getImageView().setRotate(270);
                    return;
            }

        } else {
            switch (dir) {
                case NORTH:
                    if(lane.equals(Lane.LEFT)){
                        getImageView().setRotate(315);
                    } else {
                        getImageView().setRotate(45);
                    }
                    return;
                case SOUTH:
                    if(lane.equals(Lane.LEFT)){
                    getImageView().setRotate(135);
                } else {
                    getImageView().setRotate(225);
                }
                return;

                case EAST:
                    if(lane.equals(Lane.LEFT)){
                    getImageView().setRotate(45);
                } else {
                    getImageView().setRotate(135);
                }
                return;
                case WEST:
                    if(lane.equals(Lane.LEFT)){
                    getImageView().setRotate(225);
                } else {
                    getImageView().setRotate(315);
                }
            }


        }
    }

}