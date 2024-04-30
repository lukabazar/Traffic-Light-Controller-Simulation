package logic;

import GUI.TrafficGUI;
import javafx.scene.image.ImageView;

import java.awt.*;
import java.util.Random;

public class EMS extends Vehicle {
    private boolean running = true;

    public EMS(Point p, Direction dir, Lane lane, double tileSize) {
        this.setLocation(p);
        this.setDirection(dir);
        this.setLane(lane);
        setTileSize(tileSize);
        setImageView(initImageView());
        setImageRotation(getDirection(), null);

        TrafficGUI.addCar(getImageView());
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
        imageView.setX(getLocation().getX()*getTileSize()/200);
        imageView.setY(getLocation().getY()*getTileSize()/200);
        return imageView;
    }

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

    public boolean move() {
        Point tempPoint = this.getLocation();
        if (tempPoint.x < -50 || tempPoint.x > 1050 || tempPoint.y < -50 || tempPoint.y > 650){
            this.running = false;
            this.setLocation(new Point(-100,-100));
            return true;
        }

        Point delta = this.getDirection().getDeltaDirection();
        int x = tempPoint.x + delta.x * 15;
        int y = tempPoint.y + delta.y * 15;

        this.setLocation(new Point(x,y));

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
        }
    }

    public void stop() {
        this.running = false;
        Thread.currentThread().interrupt();
    }
}