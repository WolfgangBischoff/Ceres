package Core.Menus.DiscussionGame;

public enum CharacterCoinBuff implements CoinType
{
    SLOWED, DOUBLE_REWARD, PROTECTED, NO_CLICK, HARD_CLICK; //multiple clicks per coin?!

    int duration = 5;
    Long activeSince = null;

}
