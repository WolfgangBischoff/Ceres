package Core.Menus.DiscussionGame;

import static Core.Menus.DiscussionGame.CharacterCoinBuff.*;
import static Core.Menus.Personality.MachineTrait.*;
import static Core.Menus.Personality.PersonalityTrait.*;

public interface CoinType
{
    int getCooperationVisibilityThreshold();

    void setCooperationVisibilityThreshold(int threshold);

    String getKnowledgeVisibility();

    void setKnowledgeVisibility(String knowledge);

    String getName();

    static CoinType of(String type)
    {
        switch (type.toLowerCase()) {
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

            case "compute_local":
                return COMPUTE_LOCAL;
            case "compute_cloud":
                return COMPUTE_CLOUD;
            case "compute_virtual":
                return COMPUTE_VIRTUAL;

            case "buff_slowed":
                return BUFF_SLOWED;
            case "buff_double_reward":
                return BUFF_DOUBLE_REWARD;
            default:
                throw new RuntimeException("CoinType unknown: " + type);
        }
    }
}
