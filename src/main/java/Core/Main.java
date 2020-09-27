package Core;

import Core.WorldView.WorldView;
import GuiController.MainMenuController;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class Main extends Application
{

    public static void main(String[] args)
    {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage)
    {
        //Game Window
        GameWindow gameWindowController = GameWindow.getSingleton();
        MainMenuController mainMenuController = new MainMenuController();
        gameWindowController.setTitle("Game Window");
        gameWindowController.createNextScene(mainMenuController);
        gameWindowController.showWindow();


        new AnimationTimer()
        {
            public void handle(long currentNanoTime)
            {
                gameWindowController.update(currentNanoTime);
                gameWindowController.render(currentNanoTime);
            }
        }.start();
    }
}
