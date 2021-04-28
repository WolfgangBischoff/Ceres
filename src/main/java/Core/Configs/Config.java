package Core.Configs;

import Core.GameTime.Time;
import Core.Utilities;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class Config
{
    //General
    public static final Boolean DEBUG_ACTORS = false;
    public static final Boolean DEBUG_BLOCKER = true;
    public static final Boolean DEBUG_NO_WALL = true;
    public static final Boolean DEBUG_MOUSE_ANALYSIS = true;
    public static final Boolean DEBUG_ALL_TEXT_OPTIONS_VISIBLE = false;
    public static final Boolean DEBUG_CONSOLE = false;
    public static final Boolean DEBUG_FPS = false;
    public static final boolean GAME_WINDOW_FULL_SCREEN = false;
    public static final boolean GAME_WINDOW_FULL_SCREEN_DISABLE_EXIT = false;
    //public static final String FIRST_LEVEL = "transporter/transporter";
    //public static final String FIRST_LEVEL = "dockingBay";
    public static final String FIRST_LEVEL = "crewdeck/crewdeck";
    //public static final String FIRST_LEVEL = "living_quarter/air_lock";
    public static double CAMERA_WIDTH = 1280;//1600;
    public static double CAMERA_HEIGHT = 800;//1024;
    public static final Font FONT_ESTROG_30_DEFAULT = Utilities.readFont("font/estrog__.ttf", 30);
    public static final Font FONT_ESTROG_20 = Utilities.readFont("font/estrog__.ttf", 20);
    public static final Font FONT_ORBITRON_20 = Utilities.readFont("font/Orbitron-Regular.ttf", 22);
    public static final Font FONT_ORBITRON_12 = Utilities.readFont("font/Orbitron-Regular.ttf", 12);

    public static final int TEXT_WIDTH = 800;
    public static final int TEXT_HEIGHT = 240;
    public static final Point2D TEXT_BOX_POSITION = new Point2D(CAMERA_WIDTH / 2f - TEXT_WIDTH / 2f, CAMERA_HEIGHT - TEXT_HEIGHT - 32);

    public static final int PERSONALITY_WIDTH = 900;
    public static final int PERSONALITY_HEIGHT = 600;
    public static final Point2D PERSONALITY_POSITION = new Point2D(CAMERA_WIDTH / 2f - PERSONALITY_WIDTH / 2.0, CAMERA_HEIGHT / 2.0 - PERSONALITY_HEIGHT / 2.0); //Centered

    public static final int MESSAGE_OVERLAY_WIDTH = 700;
    public static final int MESSAGE_OVERLAY_HEIGHT = 80;
    public static final Point2D SCREEN_CENTER = new Point2D(CAMERA_WIDTH / 2.0, CAMERA_HEIGHT / 2.0); //Centered

    public static final int INVENTORY_WIDTH = 550;
    public static final int INVENTORY_HEIGHT = 600;
    public static Point2D INVENTORY_POSITION = new Point2D(64, CAMERA_HEIGHT / 2.0 - INVENTORY_HEIGHT / 2.0);
    public static Point2D EXCHANGE_INVENTORY_POSITION = new Point2D(64 + INVENTORY_WIDTH, CAMERA_HEIGHT / 2.0 - INVENTORY_HEIGHT / 2.0);
    public static Point2D INCUBATOR_POSITION = new Point2D(64 + INVENTORY_WIDTH, CAMERA_HEIGHT / 2.0 - INVENTORY_HEIGHT / 2.0);

    public static final int COIN_AREA_WIDTH = 800;
    public static final int COIN_AREA_HEIGHT = 650;
    public static final int COIN_AREA_WIDTH_OFFSET = 40;
    public static final int COIN_AREA_HEIGHT_OFFSET = 30;
    public static final int COINGAME_WIDTH = COIN_AREA_WIDTH + 250;
    public static final int COINGAME_HEIGHT = COIN_AREA_HEIGHT + COIN_AREA_HEIGHT_OFFSET * 2;
    public static Point2D COINGAME_POSITION = new Point2D(CAMERA_WIDTH / 2f - COINGAME_WIDTH / 2.0, CAMERA_HEIGHT / 2.0 - COINGAME_HEIGHT / 2.0);

    public static final int DAY_SUMMARY_WIDTH = 900;
    public static final int DAY_SUMMARY_HEIGHT = 600;
    public static Point2D DAY_SUMMARY_POSITION = new Point2D(CAMERA_WIDTH / 2f - DAY_SUMMARY_WIDTH / 2.0, CAMERA_HEIGHT / 2.0 - DAY_SUMMARY_HEIGHT / 2.0); //Center

    public static final int MAM_BAR_WIDTH = 300;
    public static final int MAM_BAR_HEIGHT = 64;
    public static final Point2D MAM_BAR_POSITION = new Point2D(CAMERA_WIDTH - MAM_BAR_WIDTH - 50, 30); //Right Upper Edge
    public static final int MONEY_FIELD_WIDTH = 150;
    public static final int MONEY_FIELD_HEIGHT = 64;
    public static final Point2D MONEY_POSITION = new Point2D(CAMERA_WIDTH - MAM_BAR_WIDTH - 50, 170);
    public static final int BOARD_TIME_WIDTH = 150;
    public static final int BOARD_TIME_HEIGHT = 64;
    public static final Point2D BOARD_TIME_POSITION = new Point2D(CAMERA_WIDTH - BOARD_TIME_WIDTH - 50, 170);
    public static final Point2D HUNGER_BAR_POSITION = new Point2D(CAMERA_WIDTH - MAM_BAR_WIDTH - 50, 100);

    //Keyboard
    public static final String KEYBOARD_INVENTORY = "TAB";
    public static final String KEYBOARD_INTERACT = "E";
    public static final String KEYBOARD_ESCAPE = "ESCAPE";
    public static final String KEYBOARD_SPRINT = "SHIFT";

    //Gameplay
    public static final float TIME_BETWEEN_INTERACTIONS = 0.5f;
    public static final float TIME_BETWEEN_AUTOMATIC_INTERACTIONS = 0.5f;
    public static final float TIME_BETWEEN_DIALOGUE = 0.2f;
    public static final int TIME_MS_MESSAGE_VISIBLE = 3000;
    public static final float DODGE_VELOCITY = 50f;
    public static final float RUMBLE_GRADE = 8;
    public static final float RUMBLE_GRADE_DECREASE = 0.1f;
    public static final float RUMBLE_MAX_DURATION = 1.3f;
    //Game Time
    public static final Time DAY_LIGHT_ON_TIME = new Time(6, 30);
    public static final Time DAY_LIGHT_OFF_TIME = new Time(22, 0);
    public static final Time DAY_WAKE_UP_TIME = new Time(7, 0);
    public static final Color COLOR_EMERGENCY_LIGHT = Color.rgb(0, 30, 30);
    public static final Color COLOR_NIGHT_LIGHT = Color.rgb(50, 80, 80);
    public static final float LENGTH_GAME_MINUTE_SECONDS = 1f;

    public static final int MAX_HUNGER = 100;
    public static final int INIT_HUNGER = 50;
    public static final int INIT_HEALTH = 5;
    public static final int MAX_HEALTH = 5;
    public static final int INIT_MONEY = 5;

    //Map file keywords
    public static final String MAPDEFINITION_EMPTY = "______";
    public static final String KEYWORD_NEW_LAYER = "layer:";
    public static final String KEYWORD_PASSIV_LAYER = "passivlayer:";
    public static final String KEYWORD_ACTORS = "actors:";
    public static final String KEYWORD_TILEDEF = "tiledefinition:";
    public static final String KEYWORD_WORLDSHADOW = "shadow:";
    public static final String KEYWORD_GROUPS = "actorgroups:";
    public static final String KEYWORD_SPAWNPOINTS = "spawnpoints:";
    public static final String KEYWORD_INCLUDE = "include:";
    public static final String KEYWORD_POSITION = "position:";
    public static final String KEYWORD_GLOBAL_SYSTEM_ACTOR = "global_system_actor:";
    public static final String KEYWORD_TIME_MODE = "time_mode:";
    public static final String KEYWORD_LOG = "log:";

    public static final String INCLUDE_CONDITION_suspicion_lessequal = "suspicion_lessequal";
    public static final String INCLUDE_CONDITION_day_greaterequal = "day_greaterequal";
    public static final String INCLUDE_CONDITION_IF = "onDaystart_if";
    public static final String INCLUDE_CONDITION_IF_NOT = "onDaystart_ifNot";

    //Actorfile Keywords
    public static final String KEYWORD_sensorStatus = "sensorStatus";
    public static final String KEYWORD_transition = "transition";
    public static final String KEYWORD_interactionArea = "interactionArea";
    public static final String KEYWORD_text_box_analysis_group = "textbox_analysis_group";
    public static final String COLLECTIBLE_DATA_ACTOR = "collectible_data";
    public static final String CONTAINS_COLLECTIBLE_ACTOR = "contains_collectible";
    public static final String TAGS_ACTOR = "tags";
    public static final String CONDITION_ACTOR = "condition";
    public static final String PERSONALITY_ACTOR = "personality";
    public static final String SUSPICIOUS_VALUE_ACTOR = "suspicious_value";
    public static final String SCRIPT_ACTOR = "script";

    //DialogueFile Keywords
    public static final String DIALOGUE_TYPE_DECISION = "decision";
    public static final String DIALOGUE_TYPE_TEXT = "normal";
    public static final String DIALOGUE_TYPE_TECHNICAL = "technical";

    public static final String DIALOGUE_TAG = "dialogue";
    public static final String ID_TAG = "id";
    public static final String ACTOR_STATUS_TAG = "spritestatus";
    public static final String SENSOR_STATUS_TAG = "sensorstatus";
    public static final String LINE_TAG = "line";
    public static final String NEXT_DIALOGUE_TAG = "nextDialogue";
    public static final String OPTION_TAG = "option";
    public static final String SPRITECHANGE_TAG = "spritechange";

    public static final String TEXTBOX_ATTRIBUTE_COIN_GAME = "coingame";
    public static final String TEXTBOX_ATTRIBUTE_SPRITE_ID = "sprite_id";
    public static final String TEXTBOX_ATTRIBUTE_NEW_STATUS = "status";
    public static final String TEXTBOX_ATTRIBUTE_SENSOR_STATUS = "sensorstatus";
    public static final String TEXTBOX_ATTRIBUTE_TYPE = "type";
    public static final String TEXTBOX_ATTRIBUTE_ITEM_ACTOR = "item_actor";
    public static final String TEXTBOX_ATTRIBUTE_ITEM_NAME = "item_name";
    public static final String TEXTBOX_ATTRIBUTE_ITEM_STATUS = "item_status";
    public static final String TEXTBOX_ATTRIBUTE_SUCCESS = "success";
    public static final String TEXTBOX_ATTRIBUTE_DEFEAT = "defeat";
    public static final String TEXTBOX_ATTRIBUTE_SPAWN_ID = "spawnID";
    public static final String TEXTBOX_ATTRIBUTE_GET_MONEY = "getmoney";
    public static final String TEXTBOX_ATTRIBUTE_VISIBLE_IF = "visibleIf";
    public static final String TEXTBOX_ATTRIBUTE_SET = "set";
    public static final String TEXTBOX_ATTRIBUTE_VARIABLE_NAME = "variablename";
    public static final String TEXTBOX_ATTRIBUTE_TRUE = "true";
    public static final String TEXTBOX_ATTRIBUTE_FALSE = "false";
    public static final String TEXTBOX_ATTRIBUTE_BUMP = "bump";
    public static final String TEXTBOX_ATTRIBUTE_KNOWLEDGE = "knowledge";
    public static final String TEXTBOX_ATTRIBUTE_DIALOGUE_FILE = "dialogueFile";
    public static final String TEXTBOX_ATTRIBUTE_DIALOGUE_ID = "dialogueId";
    public static final String TEXTBOX_ATTRIBUTE_VALUE_BOOLEAN = "boolean";
    public static final String TEXTBOX_ATTRIBUTE_SET_WORLD_LIGHT = "worldlight";
    public static final String TEXTBOX_ATTRIBUTE_TIME_CHANGE = "timechange";
    public static final String TEXTBOX_ATTRIBUTE_LEVEL_CHANGE = "levelchange";
    public static final String TEXTBOX_ATTRIBUTE_DAY_CHANGE = "daychange";
    public static final String TEXTBOX_ATTRIBUTE_FADE = "fadeout";
    public static final String TEXTBOX_ATTRIBUTE_INVALID_STAGES = "invalidstages";

    //Paths
    public static final String CSV_POSTFIX = ".csv";
    public static final String PNG_POSTFIX = ".png";
    public static final String ACTOR_DIRECTORY_PATH = "actorData/";
    public static final String STAGE_FILE_PATH = "level/";
    public static final String IMAGE_DIRECTORY_PATH = "img/";
    public static final String COINGAME_DIRECTORY_PATH = "discussions/";

    //Discussion-Game
    public static final String COIN_BEHAVIOR_MOVING = "moving";
    public static final String COIN_BEHAVIOR_JUMP = "jump";
    public static final String COIN_BEHAVIOR_SPIRAL = "spiral";
    public static final String COIN_BEHAVIOR_CIRCLE = "circle";
    public static final String COIN_TAG_ANGLE = "angle";
    public static final String COIN_TAG_INITSPEED = "initspeed";
    public static final String COIN_ATTRIBUTE_MAX_TIME = "maxTime";
    public static final String DISCUSSION_ATTRIBUTE_PERCENTAGE_OF_POINTS_TO_WIN = "percentageOfPointsToWin";
    public static final int COIN_DEFAULT_MAX_TIME = 10;
    public static final int DISCUSSION_DEFAULT_THRESHOLD_WIN = 6;

    //Management-Attention-Meter MAM
    public static final int MAM_DAILY_DECREASE = 2;
    public static final int MAM_THRESHOLD_INTERROGATION = 6;

    //Colors
    public static final Color COLOR_BACKGROUND_BLUE = Color.rgb(60, 90, 85);
    public static final Color COLOR_BACKGROUND_GREY = Color.rgb(20, 25, 30);
    public static final Color COLOR_MARKING = Color.rgb(80, 120, 120);
    public static final Color COLOR_FONT = Color.rgb(100, 160, 160);
    public static final String REGEX_GREEN = "%%GR";
    public static final Color COLOR_GREEN = Color.hsb(140, 0.33, 0.90); //Good
    public static final String REGEX_RED = "%%RD";
    public static final Color COLOR_RED = Color.hsb(0, 0.33, 0.90); //Bad
    public static final String REGEX_VIOLET = "%%VT";
    public static final Color COLOR_VIOLET = Color.rgb(185, 165, 185);//Main Quest
    public static final String REGEX_GOLD = "%%GD";
    public static final Color COLOR_GOLD = Color.rgb(180, 155, 110);//For secrets
    public static final String REGEX_LINEBREAK = "%n";


}
