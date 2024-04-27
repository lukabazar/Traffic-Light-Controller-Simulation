package GUI;

import javafx.animation.AnimationTimer;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Screen;
import javafx.stage.Stage;

import logic.Intersection;
import java.util.Random;

/**
 * Main class for the GUI
 */
// TODO Animation Timer
public class TrafficGUI {

    private final BorderPane borderPane;
    private final Scene scene;
    private final Rectangle2D screenSize = Screen.getPrimary().getBounds();
    public static ImageView[] images = new ImageView[6];
    private final Stage popUp = new Stage();
    private final int rows;
    private final int cols;
    private final PopUpWindow popUpWindow;

    /**
     * GUI for the program
     *
     * @param borderPane BorderPane to use
     * @param scene main scene
     * @param rows num rows
     * @param cols num cols
     */
    public TrafficGUI(BorderPane borderPane, Scene scene,  int rows, int cols) {
        this.borderPane = borderPane;
        this.scene = scene;
        this.rows = rows;
        this.cols = cols;
        this.popUpWindow = new PopUpWindow(screenSize.getHeight() / 1.33);

        //startTimer();
    }

    /**
     * AnimationTimer for cars
     * TODO: Cars (aka vroom vroom)
     */
    private void startTimer() {
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {

            }
        };
        timer.start();
    }

    /**
     * Set up the GUI and clickable elements
     */
    public void setUp() {
        double size = screenSize.getHeight() / (rows + 1);
        VBox vBox = new VBox();
        int interIndex = -1;

        Random randy = new Random();
        popUp.setTitle("Intersection");
        popUp.getIcons().add(new Image("intersection (three-quarter).png"));
        Intersection[] intArray = new Intersection[6];
        Intersection.LightColor[] colors = {Intersection.LightColor.RED, Intersection.LightColor.GREEN};

        for(int i = 0; i < rows; i++) {
            HBox hBox = new HBox();
            boolean inter = true;
            for (int j = 0; j < cols; j++) {
                ImageView imageView;
                // TODO: This sets alternating roads and intersections, later won't be so boring (time permitting)
                if(i % 2 == 0) {
                    if (inter) {
                        int rand = randy.nextInt(2);
                        interIndex++;
                        intArray[interIndex]= new Intersection(interIndex, 1, 2, 3, 4,
                                5, 6,colors[rand], colors[1-rand]);
                        Thread intersectionThread= new Thread(intArray[interIndex]);
                        intersectionThread.start();
                        imageView = setImageView("redgreen.png", size);
                        images[interIndex]= imageView;
                        inter = false;
                    }
                    else {
                        imageView = setImageView("east-west (three-quarter).png", size);
                        inter = true;
                    }
                }

                else {
                    if(inter) {
                        imageView = setImageView("north-south (three-quarter).png", size);
                        inter = false;
                    }
                    else {
                        imageView = new ImageView();
                        inter = true;
                    }
                }

                ImageView grass = setImageView("grass.png", size);

                StackPane stackPane = new StackPane(grass, imageView);
                Circle overlay = new Circle(size / 2 - 5);
                overlay.setFill(Color.LIGHTBLUE);
                overlay.setOpacity(0.33);

                int finalI = i;
                boolean finalInter = inter;
                Color green = Color.web("0x468D34", 1.0);

                // For the popup window
                int finalInterIndex = interIndex;
                stackPane.setOnMouseClicked((MouseEvent e) -> {
                    if(popUp.isShowing()) {
                        popUp.close();
                    }
                    if(finalI % 2 == 0 && !finalInter) {
                        BorderPane popUpBorder = new BorderPane();
                        popUpBorder.setBackground(new Background(new BackgroundFill(green, null , null)));
                        popUpBorder.setCenter(popUpWindow.getPopUp(finalInterIndex));
                        popUp.setScene(new Scene(popUpBorder));
                        popUp.show();
                    }
                });

                // Overlay and cursor
                stackPane.setOnMouseEntered((MouseEvent e) -> {
                    if(finalI % 2 == 0 && !finalInter) {
                        stackPane.getChildren().add(overlay);
                        scene.setCursor(Cursor.HAND);
                    }
                });

                // Remove overlay and cursor
                stackPane.setOnMouseExited((MouseEvent e) -> {
                    stackPane.getChildren().remove(overlay);
                    scene.setCursor(Cursor.DEFAULT);
                });

                hBox.getChildren().add(stackPane);
            }
            hBox.setAlignment(Pos.CENTER);
            vBox.getChildren().add(hBox);
        }
        vBox.setAlignment(Pos.CENTER);

        StackPane roads = new StackPane(vBox);
        // TODO Vroom vroom
        roads.getChildren().add(setImageView("car_1.png", size * 0.133));
        roads.getChildren().add(setImageView("ambulance.png", size * 0.133));
        borderPane.setCenter(roads);
    }

    /**
     * Sets ImageView
     *
     * @param file File string
     * @param size Size of ImageView
     * @return Sized ImageView
     */
    public static ImageView setImageView(String file, double size) {
        ImageView imageView = new ImageView(file);
        imageView.setPreserveRatio(true);
        if(imageView.getFitHeight() >= imageView.getFitWidth()) {
            imageView.setFitHeight(size);
        }
        else {
            imageView.setFitWidth(size);
        }
        return imageView;
    }
}
