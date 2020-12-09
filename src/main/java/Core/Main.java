package Core;

import Core.ActorSystem.SystemStatus;
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

    Long updateTime = 0l;
    Long renderTime = 0l;

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
                //Long startUpdate = currentNanoTime;
                gameWindowController.update(currentNanoTime);
                //updateTime = System.nanoTime() - startUpdate;
                //Long startRender = currentNanoTime;
                gameWindowController.render(currentNanoTime);
                //renderTime = System.nanoTime() - startRender;
                //System.out.println("Update: " + updateTime/1000000 + " Render : " + renderTime/1000000);

                //System.out.println("FPS: " + FXUtils.getAverageFPS());
            }
        }.start();
    }
}
