
##### 08 21
* GameVariableManager implemented
* after leaving capsule, Energy ist back
* während nachtschicht sind alle geladen fix

##### 07 21
* added item consume from textbox
* level build trainee area
* new sprites: public_desk, resistance blue jacket; fence, green_display, carton
* Bacteria spreads on time, just to fitting nutrition
* load include from dayPart
* MapTimeData; Leave area with message; dayNight cycle and TimeMode
* Inventar: stacken beim aufsammeln
* Emails empfangen basic

##### 06 21
* from inventory world interaction mode
* aus Inventor genutzten Scrap/Food Item auf Growspace anwenden => Nahrung
* aus Inventor genutzten Bagrcteriatprobe Item auf Growspace anwenden => Beginne Prozess (Sprite entspricht Pflanze)
* Sprite von Menu und maus adden
* item stacks können einzelne items bewegen
* item grid shows hint "Esc to leave"
* growspace löscht inventar nach rotten/leerung
* ergebnis ergibt sich aus Nahrung/Bac Kombination => Fail/BacterioColony
* Sprites: grüner graben, deutlicher; public bench, rohrprop; UN150; capiGeniReceptionist
* Docking Bay: add ladder to enter MTX area which leads to air control;
* Docking Bay: function remains, lever should within the room; some items/stamps
* added levelchange V2

##### 05 21
* interact => set bacteria, info
* bacteria can rot
* added collectibeStack
* added sprites: elevator + tank big
* first grow script
* Test: nur mehr dicke wände
* level build

##### 04 21
* consoel levelchange
* added sprites medic and traveler
* Incubator Button and bacteria
* Space suit: front helmrahmen fehlt
* level build
* Vllt activeSprites teilen um performance zu verbessern
  - actorlist statt sprite list die duplikate entfernt
  - onUpdate, inRange, intersection sollten alle actors durchsuchen
  - oninteraction ist passiv
* Test: More detailed desc, not push same colornitmated finish button
* performance improvement and FPS measurement
* Test: Coin Game Ende bei Gewinn gleich raus, bei Verlust neustart
* bugfix: coin outside area

##### 22 03 21
* Tooltip is rendered over Other Overlays
* Test: Augenhöhle seltsam
* new medic sprites
##### 16 03 21
* added biocomputing stamp
##### 15 03 21
* test fixes
* Night shift should be more clear, maybe message before fade in
* Broken capsule sollte nicht einfach verschlossen sein
* Beschreibung für StasusCapsules
* Stamp nicht sammelbar
* Beschriftung Inkubator, klarer Bacterien erklären
* Tür zur Docking Bay öffnen
* anreise tag leer, keine anleitung
* change text of pod at arrival day, add people
* door is missing on arrival day
##### 11 03 21
* Enable fullscreen mode, check higher resolution and zoom
##### 10 03 21
* fill capsule 
* crewdeck lift should work arrival day
* set ARRIVEL variable => load docking bay access
* added sprite
##### 09 03 21
* fadeIn after Level load
* invalidate stages moved
##### 08 03 21
* added fadeout
* level build
##### 04 03 21
* main quest build
##### 03 03 21
* add easy characterGame -> sister before talk to trader, get some item
* build main quest, added sprites
##### 02 03 21
* added sprites
##### 25 02 21
* item description not visible
* refactoring, multiple gui bugs fixed  
##### 24 02 21
* added Incubator Convert Button
* refactored Inventory, now with mouseEvent
* added Close button
* add item in box
* added tooltip
* added first item desc
##### 18 02 21
* fixed bugs and quest build
* changed font of textbox
##### 17 02 21
* added optional time clock tick
* changed many textbox types to attributes
* Click on Coins buggy, now also uses drag
* disable bed, should be dependent on quest status
* spawn during night on control deck
##### 15 02 21
* added trader side sprite
##### 28 02 21
* added linebreak
* refactor textbox
* fixed hasItem() bug
##### 14 02 21
* Fixed: Item on last index can vanish with DragAndDrop
* Finalized Drag and Drop
* Add Incubator DragDrop
##### 11 02 21
* drag and drop improved
##### 08 02 21
* added first DragAndDrop
##### 04 02 21
* added incubator menu
#####01 02 21    
* more metroids
* added trader sprites, changed sprites
* Storage on Control Deck instead of antenna

31 01 21    - calculateDodge dodges into wall fixed - added meteorit and additional sprite layer - changes move behavior
29 01 21    - added black overal side sprites - fixed animation missing bug
28 01 21    - refactored Sprite - added idle script
26 01 21    - remove __xx__, default is nothing. - arrianne reference poster
25 01 21    - new level living_quarter - refinery sprite update, new shelf sprite
14 01 21    - fixed air vent doesnt change dialogue on trigger - moved box - sprites added
13 01 21    - changed air-hub sprite - security guy added - Test with Bisa
12 01 21    - added ladder and rescue pods - disable lift while no energy - sprites: Schriftzug MachineRoom, added animation control central damaged
10 01 21    - Test with Alex:
Erkundet Welt sehr genau, alles einsammeln und im Inventar nutzen Kothaufen lustig, ggf mehr Humor/Background Unklar was Maschine Room ist, Text anbringen; was AirHub ist
Mehr Interaktionstexte, Missverständlichkeiten hinzugefügt Kapseln leer => filled Bugs: Interaktion mit InfoTerminal, Kaffeemaschine, Zustandsterminal
Reinigt sofort filter, sollte erst nach Questbeginn möglich sein Dialogue: Frage nach Airsystem status; Never mind unklar; Tipp Luca falsch; "Change to overall" unklar
CoinGame: Zu wenig Zeit und Erklärung was zu tun ist Performance schlecht nach mehreren Coingames Textbox sollte weiter nachen egal wo man hinklickt
Hinweis "look at the screen" nicht eindeutig, Kapsel nicht zu finden. In der Kapsel brennt licht, sollte dunkel sein - added/changed/repaired several dialogues
- performance improvement shadow layer
  09 01 21 - improved textbox performance - added light dependent on time
  02 01 21 - refactored Textbox -> Dialogue - fixed minor bugs - Bug: technician changes dialogue id after sprite change (turn) => dialogueId should be changed by spritechange
- onInrange prevents interaction due to timeBetweenInteraction. - Person turns back after talk
  31 12 20 - improved render performance for coin game and HUD by drawing on WorldView Canvas - removed old capsule - Person turns if talking
  27 12 20 - player light is duplicated after quest
  21 12 20 - added Script for movement "route" and "repeat"
- fixed no wall for non player sprites - remove old paths of actors
  16 12 20 - textbox mode where all options are visible - first Console change variables
  09 12 20 - Created export with gradle - performance improvement & refactor
  30 11 20 - restart power at generator to fix light - money msg
  19 11 20 - added capsule level, sprites, dialogues - player status dependent textbox - night on textbox
  16 11 20 - added info box if new trait/knowledge is visible; new item - exit game after finished by clicking into coin area AND coin game
  12 11 20 - space suit moving south & north
  09 11 20 - sister -> family info; air hub =>  find manual at technician; trader send you to bed - pipe in the floor sprite added; exit button, finish button
- Coin Game: Player Finish message changed; finish earlier button that removes all coins; show cooperation value, name, and number traits in INFO
  08 11 20 - added actor changes on global actor get - added dialogueFile and dialogueId change via Textbox
  04 11 20 - background coin game
  02 11 20 - better coin game explanation from trader - change Eyes Box more obvious, added carton box sprite - added interface, network trait
- show residual coin game time - air system coin game in form of ventilation
  01 11 20 - reworked personality traits - added machine traits and game - decoration, background - add buffs; number unknown traits
  29 10 20 - reworked Coin movement - added buff_double_reward, win threshold calculation
  28 10 20 - first buff for Coin Game

26 10 20    - Test with Alex => Coin Game needs better explanation, maybe symbols ingame; more sprites with interaction; more content to freight capsules
- wall sign Crewdeck, Controldeck - more sprite txt: Crewdeck table, small controls, beds - add cursor with border hitbox
- add item with textbox
  22 10 20 - changed box sprite - added Ceres behind window, speaker, clock img, first space suit, improved bump; changed textbox
  21 10 20 - added global actors; placed on map according to variables - add sprint button on SHIFT - talk to collegue at window, impact; control changes status; trader text
- added sprite status change by textbox
  19 10 20 - added eyes box
  17 10 20 - added algae tank transparency, mirror in bath
  15 10 20 - Highlight text & Text "write"
- add right capsule wall - added medic, added money field - window onInteraction: bump
  14 10 20 - dodge mechanic to avoid weird hitboxes - Return to World by Esc from Inventory
  13 10 20 - Fixed Docking Bay Door doesnt work - air vent from freight deck are visible on crewdeck if base system becomes on and vent changes status
  12 10 20 - once system is online, you cannot switch of hub; nextDialogue in XML now attribute - tenderer introduces Quest, then checks fullfillment (both on global variable)
- door opens after tenderer assigns quest
  10 10 20 - Textbox should be opened on Click even if the sprite has no actor but just a defined textbox file - Interaction by click should use sprite images

OLD

19 08 20    - reactor sprite animation
23 08 20    - Item Exchange GUI and Persistence of chest
26 08 20    - thrusters animated sprite
30 08 20    - Added money from tenderer - Added first Shop interface
31 08 20    - Added Collectible value, improved Shop interface
06 09 20    - Added Shop items buyable - added second dormitory, AsiaStarBento, built right hull
07 09 20    - added back-woman sprite
09 09 20    - added Clock - added set Actor with exact position
11 09 20    - added machine room prop machine
15 09 20    - Food Changing over time - Refuel by eating
16 09 20    - Added Health, reduces if hungry during night - added board time GUI - added maintenance hole sprite and rescue room
17 09 20    - added tenderer game, collegue and food dependent on MaM; improved floor - added personality screen coins, dependent on cooperation value
- fixed exception during launch
  20 09 20 - Test with Alex => Skip textbox, change introversion coin, more description, button and click to close inventory

20 09 20    - Added flat crate and capsule roof - Fixed unused tile detection
21 09 20    - added bridge windows, cerex logo and small control - added chart, incubator - clicking outside menu in Inventory closes menu
- added alternative font, improved clock
  23 09 20 - improved debugging - added air system - added first Global System
  27 09 20 - moved Project to Java 11 and Gradle; now "Ceres"
- remove start screen, refactored code - add Escape to Textbox
  04 10 20 - added global StageMonitor
  05 10 20 - hydrau aka airHubSmall add broken sprite; - cerex logo C now red - new symbol for extra/introversion coin
- tenderer checks if air system condition full filled; world variable
  08 10 20 - DialogueFile part of sprite status, lightweight textbox - control, coffee, projector with description - standing animation
  09 10 20 - added status bar images







