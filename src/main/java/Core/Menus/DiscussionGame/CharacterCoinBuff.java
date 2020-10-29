package Core.Menus.DiscussionGame;

public enum CharacterCoinBuff implements CoinType
{
    BUFF_SLOWED, BUFF_DOUBLE_REWARD, PROTECTED, NO_CLICK, HARD_CLICK; //multiple clicks per coin?!

    int duration = 5;
    Long activeSince = null;

}
