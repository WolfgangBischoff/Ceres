package Core.Menus.DiscussionGame;

import Core.Enums.Knowledge;

import static Core.Menus.DiscussionGame.CharacterCoinBuff.*;
import static Core.Menus.Personality.MachineTrait.*;
import static Core.Menus.Personality.PersonalityTrait.*;

public interface CoinType
{
    int getCooperationVisibilityThreshold();

    void setCooperationVisibilityThreshold(int threshold);

    Knowledge getKnowledgeVisibility();

    void setKnowledgeVisibility(Knowledge knowledge);

    String getName();

    static CoinType of(String type)
    {
        switch (type.toUpperCase()) {
            case "INTROVERSION":
                return INTROVERSION;
            case "EXTROVERSION":
                return EXTROVERSION;
            case "SENSING":
                return SENSING;
            case "INTUITION":
                return INTUITION;
            case "THINKING":
                return THINKING;
            case "FEELING":
                return FEELING;
            case "JUDGING":
                return JUDGING;
            case "PERCEIVING":
                return PERCEIVING;

            case "COMPUTE_LOCAL":
                return COMPUTE_LOCAL;
            case "COMPUTE_CLOUD":
                return COMPUTE_CLOUD;
            case "COMPUTE_VIRTUAL":
                return COMPUTE_VIRTUAL;
            case "MANAGEMENT_DEBUG":
                return MANAGEMENT_DEBUG;
            case "MANAGEMENT_LOGGING":
                return MANAGEMENT_LOGGING;
            case "MANAGEMENT_MONITORING":
                return MANAGEMENT_MONITORING;

            case "BUFF_SLOWED":
                return BUFF_SLOWED;
            case "BUFF_DOUBLE_REWARD":
                return BUFF_DOUBLE_REWARD;
            default:
                throw new RuntimeException("CoinType unknown: " + type);
        }
    }
}
