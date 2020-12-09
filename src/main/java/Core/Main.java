package Core;

import Core.Utils.FXUtils;
import Core.WorldView.WorldView;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

import static Core.Configs.Config.IMAGE_DIRECTORY_PATH;

public class Main extends Application
{
    public static void main(String[] args)
    {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage)
    {
        GameWindow gameWindowController = GameWindow.getSingleton();
        gameWindowController.setTitle("Game Window");
        gameWindowController.createNextScene(WorldView.getSingleton());
        gameWindowController.showWindow();

        new AnimationTimer()
        {
            public void handle(long currentNanoTime)
            {
                gameWindowController.update(currentNanoTime);
                gameWindowController.render(currentNanoTime);
                System.out.println("FPS: " + FXUtils.getAverageFPS());
            }
        }.start();
    }
}
