package logic;

import javafx.stage.Screen;

import java.util.concurrent.CopyOnWriteArrayList;

public class Dummy_Thread implements Runnable{
    private CopyOnWriteArrayList<Thread> list = SystemManager.testCarThread;
    private CopyOnWriteArrayList<IdiotCar> list2 = SystemManager.testCars;
    int id;
    int sleeper;
    public Dummy_Thread(int ID, int sleepTime){
        id = ID;
        sleeper = sleepTime;
    }

    public void printList(){
        int size = list.size();
        int size2 = list2.size();
        System.out.println("Thread list size: " + list.size());
        System.out.println("Car list size: " + list2.size());


//        for(int i = 0; i < size; i++) {
//            System.out.println("Dummy :"+ id + " print list: " + list.get(i));
//        }
//
        for(int i = 0; i < size2; i++) {
            System.out.println("Dummy :"+ id + " x: " + list2.get(i).x + " y: " + list2.get(i).y + " direction: " + list2.get(i).direction);
            }


    }
    @Override
    public void run() {
        System.out.println("hello");
        while (true){
            try {
                printList();
                System.out.println("");
                Thread.sleep(sleeper);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

    }
}
