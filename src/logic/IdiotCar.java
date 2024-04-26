package logic;

import javafx.scene.shape.Circle;
import javafx.stage.Screen;

import java.awt.*;

public class IdiotCar implements Runnable {
    private double size;
    protected boolean running = true;
    int id;
    String direction;
    String lane;
    double x;
    double y;
    int driveDistance = 10;
    int sleepTime = 700;
    public IdiotCar(int ID, String dir, Point coord, double Size, String Lane){
        id = ID;
        direction = dir;
        x = coord.getX();
        y = coord.getY();
        size = Size;
        lane = Lane;
    }

    private void drive(){
        if(direction.equals("North")){
            y = y - driveDistance;
        }
        else if(direction.equals("South")){
            y = y + driveDistance;
        }
        else if(direction.equals("East")){
            x = x + driveDistance;
        }
        else if(direction.equals("West")){
            x = x - driveDistance;
        }
        cah().setCenterX(x);
        cah().setCenterY(y);
    }

    public Circle cah(){
        Circle carGUI = new Circle();
        carGUI.setRadius(6.0f);
        carGUI.setCenterX(x);
        carGUI.setCenterY(y);
        return carGUI;
    }

    private void stop(){
        if(direction.equals("North")){
            if(y < size*(-5)){
                running = false;
            }
        }
        else if(direction.equals("South")) {
            if(y > size*(5)){
                running = false;
            }
        }
        else if(direction.equals("East")){
            if(x > size*(6)){
                running = false;
            }
        }
        else if(direction.equals("West")){
            if(x < size*(-6)){
                running = false;
            }
        }

    }
    @Override
    public void run() {
        while(running) {
            try {
                drive();
                stop();
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

    }
}
