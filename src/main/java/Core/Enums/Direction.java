package Core.Enums;

public enum Direction
{
    NORTH(0), EAST(1), SOUTH(2), WEST(3), UNDEFINED(4);

    final Integer value;
    Direction(Integer value)
    {
        this.value = value;
    }

    public static Direction of(String value)
    {
        switch (value.toLowerCase()) {
            case "north":
                return NORTH;
            case "east":
                return EAST;
            case "south":
                return SOUTH;
            case "west":
                return WEST;
            case "undefined":
            case "none":
                return UNDEFINED;
            default:
                throw new RuntimeException("Direction not defined: " + value);
        }
    }

    public static Direction of(int value)
    {
        switch (value) {
            case 0:
                return NORTH;
            case 1:
                return EAST;
            case 2:
                return SOUTH;
            case 3:
                return WEST;
            default:
                return UNDEFINED;
        }
    }

    public Direction getOpposite()
    {
        switch (value) {
            case 0:
                return SOUTH;
            case 1:
                return WEST;
            case 2:
                return NORTH;
            case 3:
                return EAST;
            default:
                return UNDEFINED;
        }
    }

    public Integer getValue()
    {
        return value;
    }
}
