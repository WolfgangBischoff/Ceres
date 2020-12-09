package Core;

import Core.Configs.Config;
import Core.WorldView.WorldView;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.util.ArrayList;


public class GameWindow extends Stage
{
    public static ArrayList<String> input = new ArrayList<>();
    private static final String CLASSNAME = "GameWindow-";
    private static GameWindow singleton;
    private static long currentNanoRenderTimeGameWindow = 0L;
    private Stage gameStage;
    private Scene gameScene;
    WorldView currentView;
    boolean mouseClicked = false;
    Point2D mousePosition = new Point2D(0, 0); //To avoid NullPointerException of mouse was not moved at first
    boolean mouseMoved;

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

    public void createNextScene(WorldView controller)
    {
        String methodName = "createNextScene()";
        this.currentView = controller;
        gameScene = new Scene(controller.getRoot(), Config.GAME_WINDOW_WIDTH, Config.GAME_WINDOW_HEIGHT);
        //input
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
        gameScene.setOnMouseClicked(event ->
                mouseClicked = true);
        gameScene.setOnMouseMoved(event ->
        {
            mouseMoved = true;
            mousePosition = new Point2D(event.getX(), event.getY());
        });
        gameStage.setScene(gameScene);
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

    public static ArrayList<String> getInput()
    {
        return input;
    }

    public boolean isMouseClicked()
    {
        return mouseClicked;
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

    public static String getCLASSNAME()
    {
        return CLASSNAME;
    }

    public static long getCurrentNanoRenderTimeGameWindow()
    {
        return currentNanoRenderTimeGameWindow;
    }

    public void setMouseClicked(boolean mouseClicked)
    {
        this.mouseClicked = mouseClicked;
    }

}