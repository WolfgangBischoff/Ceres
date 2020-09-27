package Core;

import Core.WorldView.WorldView;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
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
            }
        }.start();
    }
}
