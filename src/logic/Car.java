package logic;

import GUI.TrafficGUI;
import javafx.application.Platform;
import javafx.scene.image.ImageView;

import java.awt.*;
import java.util.Collections;
import java.util.Random;

public class Car extends Vehicle {

    private boolean EMS_inbound;
    private boolean running = true;
    private State state;
    private int stopLine = -1;
    private int barrierLine = -1;
    private int exitLine = -1;
    private LightColor queryLight = null;
    private double opacity = 1;
    private Point turnHeading;

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




    // returns true if the car moves
    public boolean move() {
        Point tempPoint = this.getLocation();

        //check for EMS cars and modify speed and opacity
        updateEMS();


        switch (this.state) {
            case ROAD:
                if (tempPoint.x < -50 || tempPoint.x > 1050 || tempPoint.y < -50 || tempPoint.y > 650){
                    running = false;
                    this.setLocation(new Point(-100,-100));
                    return true;
                }

                if (getCurrentIntersection() == null) {
                    if (this.checkIntersections()) {
                        query();
                    }
                } else {
                    queryLight();
                }

                // switch case query

                if (queryLight != null) {
                    switch (queryLight) {
                        case GREEN:
                            collisionDetect();
                            if (crossBarrier()) {
                                this.state = State.BUFFER;
                            }
                            break;
                        case LEFTGREEN:
                            if (getLane() == Lane.LEFT) {
                                collisionDetect();
                                if (crossBarrier()) {
                                    this.state = State.BUFFER;
                                }
                                break;
                            }
                            // request intersection for heading point
                            // get the left turn barrier
                            // maybe change direction to that turn?
                        case YELLOW:
                        case RED:
                        case LEFTYELLOW:
                        case EMS:
                            if (!slowDown()) {
                                collisionDetect();
                                if (crossBarrier()) {
                                    this.state = State.BUFFER;
                                }
                            }
                            break;

                        default:

                    }
                } else {
                    collisionDetect();
                }

                return finalMove();
            case BUFFER:
                queryLight();

                if (queryLight != null) {
                    switch (queryLight) {

                        case LEFTGREEN:
                        case LEFTYELLOW:
                            if (getLane() == Lane.LEFT) {
                                collisionDetect();
                                if (inIntersection()) {
                                    this.setImageRotation(this.getDirection()
                                            , Lane.LEFT);
                                    this.state = State.LEFT_TURN;
                                    this.setSpeed(getMaxSpeed());
                                    this.turnHeading =
                                            this.getCurrentIntersection()
                                                    .getLeftTurn(
                                                            this.getDirection());
                                    this.setDirection(getLeftDirection());
                                    this.exitLine =
                                            this.getCurrentIntersection()
                                                    .getExitBarrier(
                                                            this.getDirection());
                                }
                            } else {
                                if (slowDown()){
                                    break;
                                }
                                collisionDetect();
                                break;
                            }
                            // request intersection for heading point
                            // get the left turn barrier
                            // maybe change direction to that turn?
                            break;
                        case YELLOW:
                        case RED:
                        case EMS:
                        case GREEN:
                            collisionDetect();
                            if (inIntersection()) {
                                if (getLane() == Lane.RIGHT) {

                                    Random rand = new Random();
                                    if (rand.nextDouble() < 0.33 && !this.getCurrentIntersection().getPedestrians()) {


                                        this.setImageRotation(
                                                this.getDirection()
                                                , Lane.RIGHT);
                                        this.state = State.RIGHT_TURN;
                                        this.setSpeed(getMaxSpeed());
                                        this.turnHeading =
                                                this.getCurrentIntersection()
                                                        .getRightTurn(
                                                                this.getDirection());
                                        this.setDirection(getRightDirection());
                                        this.exitLine =
                                                this.getCurrentIntersection()
                                                        .getExitBarrier(
                                                                this.getDirection());
                                    } else {
                                        this.state = State.STRAIGHT;
                                    }
                                } else{
                                    this.state = State.STRAIGHT;
                                }
                            }
                            break;

                        default:

                    }
                } else {
                    collisionDetect();
                }

                return finalMove();

            case STRAIGHT:
                this.queryLight = null;
                this.exitLine =
                        getCurrentIntersection().getExitBarrier(this.getDirection());
                collisionDetect();
                if (crossExitBarrier()){
                    this.state = State.ROAD;

                    this.setLastIntersection(getCurrentIntersection());
                    this.setCurrentIntersection(null);
                }

                return finalMove();
            case LEFT_TURN:
            case RIGHT_TURN:
                this.queryLight = null;
                turnMovement();

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
                    Platform.runLater(() -> {
                        getImageView().setRotate(0);
                    });
                    return;
                case SOUTH:
                    Platform.runLater(() -> {
                        getImageView().setRotate(180);
                    });
                    return;

                case EAST:
                    Platform.runLater(() -> {
                        getImageView().setRotate(90);
                    });
                    return;
                case WEST:
                    Platform.runLater(() -> {
                        getImageView().setRotate(270);
                    });
                    return;
            }

        } else {
            switch (dir) {
                case NORTH:
                    if(lane.equals(Lane.LEFT)){
                        Platform.runLater(() -> {
                            getImageView().setRotate(315);
                        });
                    } else {Platform.runLater(() -> {
                        getImageView().setRotate(45);});
                    }
                    return;
                case SOUTH:
                    if(lane.equals(Lane.LEFT)){
                        Platform.runLater(() -> {

                            getImageView().setRotate(135);});
                    } else {Platform.runLater(() -> {
                        getImageView().setRotate(225);});
                    }
                    return;

                case EAST:
                    if(lane.equals(Lane.LEFT)){Platform.runLater(() -> {
                        getImageView().setRotate(45);});
                    } else {Platform.runLater(() -> {
                        getImageView().setRotate(135);});
                    }
                    return;
                case WEST:
                    if(lane.equals(Lane.LEFT)){Platform.runLater(() -> {
                        getImageView().setRotate(225);});
                    } else {Platform.runLater(() -> {
                        getImageView().setRotate(315);});
                    }
            }


        }
    }

    /**
     * checks the distance between a car and all other cars; really only care if the car is going in same
     * direction in same lane
     * @return boolean true if the distance is greater than min buffer distance false otherwise
     */
    public boolean distanceCheck() {
        boolean result = true;
        for (Vehicle otherCar : SystemManager.vehicleList) {
            Direction currentCarDirection = this.getDirection();

            // don't have to do the check if same car or not going in the same direction -- automatically true
            if (this.getClass().getName().equals("logic.EMS") || otherCar.getClass().getName().equals("logic.EMS")
                    || otherCar.equals(this) || otherCar.getDirection() != this.getDirection()
                    || otherCar.getLane() != this.getLane()) {
                continue; // Skip if it's the same car or different direction
            }
            switch (currentCarDirection) {
                case NORTH -> {
                    // first check if it's the car in front; next check if it is in same street/lane
                    if ((this.getLocation().getY() >  otherCar.getLocation().getY() &&
                            this.getLocation().getY() -  otherCar.getLocation().getY() < this.getMinBufferDistance()) &&
                            this.getLocation().getX() == otherCar.getLocation().getX()) {
                        return false;
                    }
                }
                case SOUTH -> {
                    if ((this.getLocation().getY() <  otherCar.getLocation().getY() &&
                            otherCar.getLocation().getY() - this.getLocation().getY() < this.getMinBufferDistance()) &&
                            this.getLocation().getX() == otherCar.getLocation().getX()) {
                        return false;
                    }
                }
                case EAST -> {
                    if ((this.getLocation().getX() <  otherCar.getLocation().getX() &&
                            otherCar.getLocation().getX() - this.getLocation().getX()  < this.getMinBufferDistance()) &&
                            this.getLocation().getY() == otherCar.getLocation().getY()) {
                        return false;
                    }

                }
                case WEST -> {
                    if ((this.getLocation().getX() >  otherCar.getLocation().getX() &&
                            this.getLocation().getX() -  otherCar.getLocation().getX() < this.getMinBufferDistance()) &&
                            this.getLocation().getY() == otherCar.getLocation().getY()) {
                        return false;
                    }
                }
            }
        }
        return result;
    }

    public void flipEMS_inbound() {
        this.EMS_inbound = !this.EMS_inbound;
    }

    public void query(){
        this.queryLight = getCurrentIntersection().queryLight(getDirection());
        this.stopLine =
                getCurrentIntersection().getStop(getDirection());
        this.barrierLine =
                getCurrentIntersection().getBarrier(getDirection());
    }

    public void queryLight(){
        this.queryLight = getCurrentIntersection().queryLight(getDirection());

    }

    public void collisionDetect(){
        if (!distanceCheck()) {
            double new_speed = this.getSpeed() - 1;
            if (new_speed < 0) {
                new_speed = 0;
            }
            this.setSpeed(new_speed);
        } else if (!this.EMS_inbound){
            double new_speed = this.getSpeed() + 0.2;
            if (new_speed > this.getMaxSpeed()) {
                new_speed = this.getMaxSpeed();
            }
            this.setSpeed(new_speed);
        }
    }

    /**
     * check if the car is within minBufferDistance to stop line
     * @return true if within distance of stop line false otherwise
     */
    public boolean distanceToStopLine() {
        boolean result = false;

        switch (this.getDirection()) {
            case NORTH:
            case SOUTH:
                result =
                        Math.abs(this.getLocation().getY() - this.stopLine) < this.getMinBufferDistance()-5;
                break;
            case EAST:
            case WEST:
                result =
                        Math.abs(this.getLocation().getX() - this.stopLine) < this.getMinBufferDistance()-5;
        }
        return result;
    }

    /**
     * slow down car if it is within minBufferDistance of stop line for red light
     */
    public boolean slowDown() {
        if (this.state != State.BUFFER && distanceToStopLine()) {
            double new_speed = this.getSpeed() - 1;

            if (new_speed < 0) {
                new_speed = 0;
            }
            this.setSpeed(new_speed);
            return true;
        }
        return false;
    }


    public boolean crossBarrier() {
        return switch (this.getDirection()) {
            case NORTH -> this.getLocation().getY() <= this.stopLine;
            case SOUTH -> this.getLocation().getY() >= this.stopLine;
            case EAST -> this.getLocation().getX() >= this.stopLine;
            case WEST -> this.getLocation().getX() <= this.stopLine;
        };
    }

    public boolean crossExitBarrier() {
        return switch (this.getDirection()) {
            case NORTH -> this.getLocation().getY() <= this.exitLine;
            case SOUTH -> this.getLocation().getY() >= this.exitLine;
            case EAST -> this.getLocation().getX() >= this.exitLine;
            case WEST -> this.getLocation().getX() <= this.exitLine;
        };
    }

    public boolean finalMove(){
        if (this.getSpeed() == 0){
            return false;
        }

        Point delta = this.getDirection().getDeltaDirection();
        int x = (int) (this.getLocation().x + delta.x*this.getSpeed());
        int y = (int) (this.getLocation().y + delta.y*this.getSpeed());

        this.setLocation(new Point(x,y));

        return true;
    }

    public void updateEMS(){
        this.EMS_inbound = false;

        for (EMS otherCar : SystemManager.EMSList) {


            // don't have to do the check if same car or not going in the same direction -- automatically true
            if (otherCar.getDirection() != this.getDirection()
            ) {
                continue; // Skip if it's the same car or different direction
            }
            if (this.getLocation().distance(otherCar.getLocation()) < 150) {
                this.EMS_inbound = true;
            }
        }

        if (this.EMS_inbound){
            if (this.opacity > 0.3){
                this.opacity -= 0.1;

            } else {
                this.opacity = 0.3;
            }

        } else {
            if (this.opacity < 1){
                this.opacity += 0.1;

            } else {
                this.opacity = 1;
            }

        }
        this.updateOpacity(this.opacity);

        double new_speed;
        if (this.EMS_inbound) {
            new_speed = this.getSpeed() - 1;
            if (new_speed < 0) {
                new_speed = 0;
            }
            this.setSpeed(new_speed);
        }
    }

    public Direction getLeftDirection(){
        return switch (this.getDirection()) {
            case NORTH -> Direction.WEST;
            case SOUTH -> Direction.EAST;
            case EAST -> Direction.NORTH;
            case WEST -> Direction.SOUTH;
        };

    }

    public Direction getRightDirection(){
        return switch (this.getDirection()) {
            case NORTH -> Direction.EAST;
            case SOUTH -> Direction.WEST;
            case EAST -> Direction.SOUTH;
            case WEST -> Direction.NORTH;
        };

    }

    public void moveTowards(Point target, int distance) {
        // Calculate the direction from A to B
        int dx = target.x - getLocation().x;
        int dy = target.y - getLocation().y;

        // Calculate the distance between A and B
        double distanceToTarget = target.distance(getLocation());

        // Normalize the direction
        double dirX = dx / distanceToTarget;
        double dirY = dy / distanceToTarget;

        // Move A towards B by the given distance
        getLocation().x += (int) (dirX * distance);
        getLocation().y += (int) (dirY * distance);
    }

    public void turnMovement(){
        moveTowards(turnHeading,5);
        // move to headng
        // if crossbarrier
        // set location to that point
        if (crossExitBarrier()){
            this.state = State.ROAD;
            this.queryLight = null;
            this.exitLine = -1;
            this.setLastIntersection(getCurrentIntersection());
            this.setCurrentIntersection(null);
            setLocation(turnHeading);
            this.setImageRotation(this.getDirection(), null);
        }
    }

    public boolean inIntersection() {
        return switch (this.getDirection()) {
            case NORTH -> this.getLocation().getY() <= this.barrierLine;
            case SOUTH -> this.getLocation().getY() >= this.barrierLine;
            case EAST -> this.getLocation().getX() >= this.barrierLine;
            case WEST -> this.getLocation().getX() <= this.barrierLine;
        };
    }

}

