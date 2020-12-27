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
        if (tokens.isEmpty())
            return;

        if (tokens.get(0).text.equals("var")) {
            if (tokens.size() >= 3) {
                GameVariables.setGenericVariable(tokens.get(1).text, tokens.get(2).text);
                print("Set " + tokens.get(1) + " to " + tokens.get(2));
            }
            else {
                print("Generic Variables: " + GameVariables.getGenericVariables().toString());
            }
        }
        else if (tokens.get(0).text.equals("sprites")) {
            int total = WorldView.getBottomLayer().size() + WorldView.getMiddleLayer().size() + WorldView.getTopLayer().size();
            print("Total: " + total);
            print("Bottom Layer (" + WorldView.getBottomLayer().size() + "):");
            //WorldView.getBottomLayer().stream().forEach(s -> print(s.getName()));
            print("Middle Layer (" + WorldView.getMiddleLayer().size() + "):");
            //WorldView.getMiddleLayer().stream().forEach(s -> print(s.getName()));
            print("Top Layer (" + WorldView.getTopLayer().size() + "):");
            //WorldView.getTopLayer().stream().forEach(s -> print(s.getName()));
        }
    }

    private static void print(String msg)
    {
        console.println(msg);
    }

}

