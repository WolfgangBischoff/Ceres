package Core.Utils;

import Core.GameVariables;
import Core.Main;
import Core.WorldView.WorldView;

import java.util.List;
import java.util.stream.Collectors;

public class ConsoleCommandInterpreter
{
    final static String CLASSNAME = "CommandInterpreter/";
    static Console console = Main.getConsole();

    private ConsoleCommandInterpreter()
    {
    }

    public static void receiveCommand(String cmd)
    {
        interpret(tokenize(cmd));
    }

    private static List<Token> tokenize(String cmd)
    {
        return List.of(cmd.split(" ")).stream().map(
                (token) ->
                {
                    return new Token(token);
                }).collect(Collectors.toList());
    }

    private static void interpret(List<Token> tokens)
    {
        for (Token token : tokens)
            print(token.text + " ");
        println("");

        if (tokens.isEmpty())
            return;

        switch (tokens.get(0).text)
        {
            case "var":
                if (tokens.size() >= 3)
                {
                    GameVariables.setGenericVariable(tokens.get(1).text, tokens.get(2).text);
                    println("Set " + tokens.get(1) + " to " + tokens.get(2));
                }
                else
                {
                    println("Generic Variables: " + GameVariables.getGenericVariables().toString());
                }
                break;
            case "sprites":
                int total = WorldView.getBottomLayer().size() + WorldView.getMiddleLayer().size() + WorldView.getUpperLayer().size();
                println("Total: " + total);
                println("Bottom Layer (" + WorldView.getBottomLayer().size() + "):");
                //WorldView.getBottomLayer().stream().forEach(s -> print(s.getName()));
                println("Middle Layer (" + WorldView.getMiddleLayer().size() + "):");
                //WorldView.getMiddleLayer().stream().forEach(s -> print(s.getName()));
                println("Top Layer (" + WorldView.getUpperLayer().size() + "):");
                //WorldView.getTopLayer().stream().forEach(s -> print(s.getName()));
                break;
            case "level":
                if (tokens.size() >= 2)
                {
                    WorldView.getSingleton().changeStage(tokens.get(1).text, "default", false);
                }
                break;
        }
    }

    private static void println(String msg)
    {
        console.println(msg);
    }

    private static void print(String msg)
    {
        console.print(msg);
    }

}

