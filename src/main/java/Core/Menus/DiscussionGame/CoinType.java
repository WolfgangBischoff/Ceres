package Core.Menus.DiscussionGame;

import Core.Menus.Personality.PersonalityTrait;

import static Core.Menus.DiscussionGame.CharacterCoinBuff.*;
import static Core.Menus.Personality.PersonalityTrait.*;

public interface CoinType
{
    static CoinType of(String type)
    {
        switch (type.toLowerCase())
        {
            case "introversion":
                return INTROVERSION;
            case "extroversion":
                return EXTROVERSION;
            case "sensing":
                return SENSING;
            case "intuition":
                return INTUITION;
            case "thinking":
                return THINKING;
            case "feeling":
                return FEELING;
            case "judging":
                return JUDGING;
            case "perceiving":
                return PERCEIVING;

            case "buff_slowed":
                return SLOWED;
            case "buff_doubleReward":
                return DOUBLE_REWARD;
            default:
                throw new RuntimeException("CoinType unknown: " + type);
        }
    }
}
