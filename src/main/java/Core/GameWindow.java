package Core;

import Core.Configs.Config;
import Core.WorldView.WorldView;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;

import java.util.ArrayList;

import static Core.Configs.Config.*;


public class GameWindow extends Stage
{
    private static final String CLASSNAME = "GameWindow/";
    public static ArrayList<String> input = new ArrayList<>();
    private static GameWindow singleton;
    private static long currentNanoRenderTimeGameWindow = 0L;
    private final Stage gameStage;
    WorldView currentView;
    boolean mouseClicked = false;
    Point2D mousePosition = new Point2D(0, 0); //To avoid NullPointerException of mouse was not moved at first
    boolean mouseMoved;
    boolean mouseDragged;
    double screenratioX = 1, screenratioY = 1;

    private GameWindow()
    {
        gameStage = new Stage();
    }

    public static GameWindow getSingleton()
    {
        if (singleton == null)
            singleton = new GameWindow();
        return singleton;
    }

    public static ArrayList<String> getInput()
    {
        return input;
    }

    public static String getCLASSNAME()
    {
        return CLASSNAME;
    }

    public static long getCurrentNanoRenderTimeGameWindow()
    {
        return currentNanoRenderTimeGameWindow;
    }

    public void createNextScene(WorldView controller)
    {
        this.currentView = controller;

        //https://stackoverflow.com/questions/16606162/javafx-fullscreen-resizing-elements-based-upon-screen-size
        Pane root = new Pane();
        Pane controllerPane = controller.getRoot();
        root.getChildren().add(controllerPane);
        Scale scale = new Scale(1, 1, 0, 0);
        scale.xProperty().bind(root.widthProperty().divide(CAMERA_WIDTH));
        scale.yProperty().bind(root.heightProperty().divide(CAMERA_HEIGHT));
        root.getTransforms().add(scale);
        Scene gameScene;
        if (GAME_WINDOW_FULL_SCREEN) {
            gameScene = new Scene(root);
            gameStage.setFullScreen(true);
            if (GAME_WINDOW_FULL_SCREEN_DISABLE_EXIT)
                gameStage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
            gameStage.setFullScreenExitHint("");
        }
        else
            gameScene = new Scene(root, CAMERA_WIDTH, Config.CAMERA_HEIGHT);
        gameStage.setScene(gameScene);


        gameScene.setOnKeyPressed(
                e ->
                {
                    String code = e.getCode().toString();
                    if (!input.contains(code))
                        input.add(code);
                });
        gameScene.setOnKeyReleased(
                e ->
                {
                    String code = e.getCode().toString();
                    input.remove(code);
                });

        gameScene.setOnMouseMoved(event ->
        {
            mouseMoved = true;
            setMousePosition(root, event);
        });

        gameScene.setOnMouseDragged(event ->
        {
            mouseDragged = true;
            mouseMoved = true;
            setMousePosition(root, event);
        });
        gameScene.setOnMousePressed(pressed ->
        {
            mouseClicked = false;
        });
        gameScene.setOnMouseReleased(event ->
        {
            if (!mouseDragged)
                mouseClicked = true;
            mouseDragged = false;
        });


    }

    private void setMousePosition(Pane root, MouseEvent event)
    {
        screenratioX = CAMERA_WIDTH / root.getWidth();
        screenratioY = CAMERA_HEIGHT / root.getHeight();
        double x = event.getX() * screenratioX;
        double y = event.getY() * screenratioY;
        mousePosition = new Point2D(x, y);
    }

    public void update(Long elapsedTime)
    {
        currentView.update(elapsedTime);
    }

    public void render(Long currentNanoTime)
    {
        currentNanoRenderTimeGameWindow = currentNanoTime; //To get Time somewhere else when needed
        currentView.render(currentNanoTime);
    }

    public double getScreenWidth()
    {
        return gameStage.getScene().getWidth();
    }

    public double getScreenHeight()
    {
        return gameStage.getScene().getHeight();
    }

    public void showWindow()
    {
        gameStage.show();
    }

    public long getRenderTime()
    {
        return currentNanoRenderTimeGameWindow;
    }

    public boolean isMouseClicked()
    {
        return mouseClicked;
    }

    public void setMouseClicked(boolean mouseClicked)
    {
        this.mouseClicked = mouseClicked;
    }

    public Point2D getMousePosition()
    {
        return mousePosition;
    }

    public boolean isMouseMoved()
    {
        return mouseMoved;
    }

    public void setMouseMoved(boolean mouseMoved)
    {
        this.mouseMoved = mouseMoved;
    }

    public boolean isMouseDragged()
    {
        return mouseDragged;
    }

}