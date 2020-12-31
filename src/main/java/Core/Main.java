package Core;

import Core.Utils.Console;
import Core.Utils.ConsoleCommandInterpreter;
import Core.WorldView.WorldView;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.function.Consumer;

import static Core.Configs.Config.DEBUG_CONSOLE;
import static Core.Configs.Config.DEBUG_FPS;

public class Main extends Application
{
    public static void main(String[] args)
    {
        launch(args);
    }

    Long updateTime = 0l;
    Long renderTime = 0l;
    static Console console;

    @Override
    public void start(Stage primaryStage) throws IOException
    {
        GameWindow gameWindowController = GameWindow.getSingleton();
        gameWindowController.setTitle("Game Window");
        gameWindowController.createNextScene(WorldView.getSingleton());
        gameWindowController.showWindow();

        if (DEBUG_CONSOLE)
        {
            console = new Console();
            console.setOnMessageReceivedHandler(new Consumer<String>()
            {
                @Override
                public void accept(String s)
                {
                    ConsoleCommandInterpreter.receiveCommand(s);
                }
            });
            Stage consoleStage = new Stage();
            consoleStage.setScene(new Scene(console));
            consoleStage.show();
        }

        new AnimationTimer()
        {
            public void handle(long currentNanoTime)
            {
                Long startUpdate = currentNanoTime;
                gameWindowController.update(currentNanoTime);
                updateTime = System.nanoTime() - startUpdate;
                Long startRender = currentNanoTime;
                gameWindowController.render(currentNanoTime);
                renderTime = System.nanoTime() - startRender;
                if (DEBUG_FPS)
                    System.out.println("Update: " + updateTime / 1000000 + " Render: " + renderTime / 1000000); //60 is good
            }
        }.start();
    }

    public static Console getConsole()
    {
        return console;
    }
}
