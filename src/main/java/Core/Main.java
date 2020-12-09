package Core;

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
    public void start(Stage primaryStage) throws IOException
    {
        GameWindow gameWindowController = GameWindow.getSingleton();
        gameWindowController.setTitle("Game Window");
        gameWindowController.createNextScene(WorldView.getSingleton());
        gameWindowController.showWindow();
        System.out.println("Base directory: " + new File(IMAGE_DIRECTORY_PATH).getCanonicalPath());
        System.out.println("Base directory: " + new File(IMAGE_DIRECTORY_PATH).getPath());
        System.out.println("Base directory: " + new File(IMAGE_DIRECTORY_PATH).getAbsolutePath());

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
