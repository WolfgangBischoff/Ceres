package Core.Utils;

import Core.GameVariables;
import Core.Main;

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

        if (tokens.get(0).text.equals("var"))
        {
            if (tokens.size() >= 3)
            {
                GameVariables.setGenericVariable(tokens.get(1).text, tokens.get(2).text);
                print("Set " + tokens.get(1) + " to " + tokens.get(2));
            }
            else
            {
                print("Generic Variables: " + GameVariables.getGenericVariables().toString());
            }
        }
    }

    private static void print(String msg)
    {
        console.println(msg);
    }

}

