package logic;

import GUI.TrafficGUI;
import javafx.scene.image.ImageView;

import java.awt.*;
import java.util.Random;

public class EMS extends Vehicle {
    private boolean running = true;
    private int exitLine;


    public EMS(Point p, Direction dir, Lane lane, double tileSize) {
        this.setLocation(spawnAdust(p, dir));
        this.setDirection(dir);
        this.setLane(lane);
        setTileSize(tileSize);
        setImageView(initImageView());
        setImageRotation(getDirection(), null);

        TrafficGUI.addCar(getImageView());
    }

    public Point spawnAdust(Point p, Direction dir){

        return switch (dir) {
            case NORTH -> new Point(p.x, p.y + 100);
            case SOUTH -> new Point(p.x, p.y - 100);
            case EAST -> new Point(p.x - 100, p.y);
            case WEST -> new Point(p.x + 100, p.y);
        };
    }

    public ImageView initImageView() {
        ImageView imageView;
        imageView = new ImageView("ambulance.png");

        imageView.setPreserveRatio(true);
        if (imageView.getFitHeight() >= imageView.getFitWidth()) {
            imageView.setFitHeight(getTileSize() * 0.133);
        } else {
            imageView.setFitWidth(getTileSize() * 0.133);
        }
        imageView.setTranslateX(-getTileSize() * 0.133 / 4);
        imageView.setTranslateY(-getTileSize() * 0.133 / 2);
        imageView.setX(getLocation().getX() * getTileSize() / 200);
        imageView.setY(getLocation().getY() * getTileSize() / 200);
        return imageView;
    }

    public void setImageRotation(Direction dir, Lane lane) {
        if (lane == null) {
            switch (dir) {
                case NORTH:
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
                    if (lane.equals(Lane.LEFT)) {
                        getImageView().setRotate(315);
                    } else {
                        getImageView().setRotate(45);
                    }
                    return;
                case SOUTH:
                    if (lane.equals(Lane.LEFT)) {
                        getImageView().setRotate(135);
                    } else {
                        getImageView().setRotate(225);
                    }
                    return;

                case EAST:
                    if (lane.equals(Lane.LEFT)) {
                        getImageView().setRotate(45);
                    } else {
                        getImageView().setRotate(135);
                    }
                    return;
                case WEST:
                    if (lane.equals(Lane.LEFT)) {
                        getImageView().setRotate(225);
                    } else {
                        getImageView().setRotate(315);
                    }
            }


        }
    }

    public boolean move() {
        Point tempPoint = this.getLocation();


        if (tempPoint.x < -150 || tempPoint.x > 1150 || tempPoint.y < -150 ||
                tempPoint.y > 750) {
            running = false;
            this.setLocation(new Point(-100, -100));
            return true;
        }

        if (getCurrentIntersection() == null) {
            if (this.checkIntersections()) {
                query();
            }
        } else {
            query();
        }


        if (getCurrentIntersection() != null && crossExitBarrier()) {
            getCurrentIntersection().setEMSinbound(false);
            this.setLastIntersection(getCurrentIntersection());
            this.setCurrentIntersection(null);
        }

        return finalMove();

    }

    @Override
    public void run() {
        while (running) {
            if (move()) {
                this.GUIupdate();
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void stop() {
        this.running = false;
        Thread.currentThread().interrupt();
    }

    public void query() {
        this.exitLine =
                getCurrentIntersection().getExitBarrier(getDirection());
        getCurrentIntersection().setEMSinbound(true);
    }

    public boolean finalMove() {
        if (this.getSpeed() == 0) {
            return false;
        }

        Point delta = this.getDirection().getDeltaDirection();
        int x = (int) (this.getLocation().x + delta.x * this.getMaxSpeed());
        int y = (int) (this.getLocation().y + delta.y * this.getMaxSpeed());

        this.setLocation(new Point(x, y));

        return true;
    }

    public boolean crossExitBarrier() {
        return switch (this.getDirection()) {
            case NORTH -> this.getLocation().getY() <= this.exitLine;
            case SOUTH -> this.getLocation().getY() >= this.exitLine;
            case EAST -> this.getLocation().getX() >= this.exitLine;
            case WEST -> this.getLocation().getX() <= this.exitLine;
        };
    }
}