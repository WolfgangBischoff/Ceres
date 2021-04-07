
#Backlog

####Bug
       
#### Main Quest

#### Set/Upgrade item mechanic (provide Service)
* sprite an Pos adden via Key
* Sprite von Menu und maus adden
* Zeit & Resourcenkosten
* Position check
* Different Sprites           
           
#### Grow Plant / Genes
* workenbench/breeder that uses base bacteria and genes
* air bacteria, food, material, social buff, computers?
* bases need organic matter to grow, can be spliced for improvements
* bases can be mixed, mutated over time
* bacteria cultures can be used in incubators (bottleneck)

#### Engine
* Problem mit Persistens, wird nur geladen beim ersten laden des basis files, danach lassen sich keine solchen actors mehr hinzufügen, ggf später ein problem bei truhen die später kommen sollen und die nicht immer neu geladen weredn können
    * Includes sollten gespeichert werden (Teil von LevelState?), bei initial geladenen werden auch persistente hinzugefügt

#### Various
* first stranger sound
* Test: StatusInfo over sprite on key + ExclamationMark
* "Rumors"/Random dialogue
* Textbox Show Name of Person/Sprite eg. Julian Soulles : Trader and img defined in xml
* Test: Dialogoption sollte weg sein bis retrigger
* Actor that listens to variable and changes dialogue if set
* Blinklicht
* Show money in Shop view
* Coin Game
    * Test: Game ends if no correct coins are left
   
#### Stealth mechanic
* split inRange area into several parts
* every part will be blocked by bocker sprites
* in range player interaction

####Sprites  
* Ship: rename sleep capsules
* Machines: Ventilation/Movingpart with bridge, Lavalampen Maschine, solar panels
* Hammer and Sickle modern, Säulen, Tank
* stranger sprite with air mask and gloves  
* Docking Bay: space elevator rope, bodenmarkierung lift    
* Placeabel Structures: Kindergarten, HoloKino, Bar, Schule, Krankenhaus, Küche, LuftErfrischer, Food, Medics, Brothel, Museum
* Test: nur mehr dicke wände
* test: Air hub deutlicherer sprite das kaputt
* Incubator Button and bacteria
    
    => Level changes with time
            While Map load set global actors according to time, date, variables defined in actor file
            Set Actor script according to time
  
    - Cooperation value just increases two time a day
    - Create Questlog
    

    
# Ideas
##Basemechanics
### Transform your Transporter to better place to live
* Build Support Infrastructure (predefinded place or semi free)
* Maintain Service by inputs to increase revolutionary spirit
* Propaganda actions also increase revSpirit
* Once revSpirit Threshold is reached you can go to another area
* Delegate mtx to other people
* Bonus for good service in an area, endgame if all areas are maintained

###Daily needs
* Sleep -> based on time like in StardewVallay, but maybe no defined end time. Some areas dont have time
* board time and ceres time are different, ceres night/day changes world
* Better bed increases skills, also Kuschis; different places over time
* Food -> Eat during game (if not eaten for days, dies)
* Air -> needed for space suit
* Entertainment -> maybe useless but nice value

###Mining in crypto vulcano
* randomly generated

###Containering and Looting
* getting tips from rumors for events

###Social
* go to cafes and talk to people, befriend them
* everyday other people there
* meet entertainers and use their influence

##Contents
- Biocomputer
- People are created by bioengeneering, sexuality is fluent
- get a robot begleiter, Robot Avatars, remote control
- Cook Serve delicious
Mechanics:
    With high attention meter place has surveilance
    - get money from Companies for doing Quest (money changes if workers are unionized, work is more rentable if much union)
    - steal
    - sabotage to increase chaos, create work or change behavior
    - be lazy to get job over multiple days
    - repair
    - scavenge/search with game as coin game variant
    Public Work (Revolutionary meter, needed for main questline or acsess to areas due to people that feel more safe with helping you)
    - Spray, Flugzettel
    - Gain trust through dailies like provide food/water/communication between people
- CoinGame: buff areas; killing line/shots; unbesiegbarkeit buff
- Increasable Skills like Sabotage (Harm Machines), Craft (Tools/Weapons/Propaganda), Rethorik
- Gewerkschaft is corrupted, helps worker but ignores slaves; but unionization helps for more money from quest work
- Verbindung, Social layers
    -> Drugs, Spirituality
    -> Gay
    -> Hobbies
- Company is private autocracy; factions within "government"
    - Earth Defenders want to save earth, bad conditions are needed to fulfill production, no risk acceptable
    - Red Faction wants better conditions, egoistic, risk is ok. Earth supply may jeopardized
    - Indepence/Union movement wants Earth to pay, bit more equal but Feudal

- Tragik der Allmende
- Verteilungslogik: Egalitär, Utilitaristisch
- Die die seit sie geboren wurden niemals Mangel an etwas hatten, hasste ich immer
- Traitors that interacts with multiple roles, but you dont know that they are the same
- Different suites
    space suit for outer world and water pipe
    other uniforms to get access to areas
- Textbox die mit dem Spieler mitgeht als Lautsprecher
- Input line secrets, enter number or names
- Schuldsklaven "Verbrecher" vom sYstem absichtlich produziert
- Main menu with dark parts that get light abwechseln, reveal hammer/sickle, Money, such things
- Social Assignment mechanic
- Minigames: Minesweeper, Einarmiger Bandit, test knowledge with symbols or inputs
- espionage/desinformation via Alexa devices
- Why are people poor? Donations from rich are stupid. System is not fair, we had never a change


Main Quest:
Ceres is fucked up => Kaper SChiff => Random Asteriod? => Eigene Crew => Ausbau
=> Beeinglussung von Ceres (Leaks, Unterstützung, stolen Knowledge, Arbeitskampf)

Character (Who are they, dreams, personality, history)

Cultural Dominance Plot:
    Style of Station, Music, Interests

Spiritual Mission:
    Drugs, secret cult


------------------------------------------
----- Lore & World -----------------------
------------------------------------------
Lore (Info not related to Plot, History, descriptions)
    Third Class
        => Earth damaged, people want to live in space and station, Ceres is base for asteriod belt mining
            - Private Company build Station, Earth law doesnt apply, Earth Gov am Gängelband
        => Subscriptions to benefits like housing, food and air
        => Dwarf planet Ceres, Water colony with science and mining; can travel to asteriods; Vanguard of Humankind in private ownership (CereX company)
        => Board time / Ceres Time (9h 4min per day) connection affects world (year 4,605 years)
        => Society changes, very liberal. Open Sex at Clubs, man/women not important, just prefered position, no classic partnerships
        => drugs are normal
        => home-delivery of goods is standard, maybe stealing?
        => arguments for human body
        => Children usually designed, just Ökos take old way

    Ceres
        - Ceres Base gets energy and water from old colony ship, which works automated;
        - The Colony Ship has no air life systems, but in secret workers life there under bad conditions,
                - need air masks all day, union knows about
                - workers are dept slaves from cryo capsules
        - The trade hub contains a space elevator and in secret luxury apartements
        - There are water and mineral resource fields near cryo vulcanos + other asteriods
        - Resources very limited, nearly autarc; cheap drones with oxygen fuel can sent back resources, but oxygen is rare;
            goods from earth must be sent to space, expensive, from ceres weight is not a problem

=> Vielleciht auch unabhängige Systeme. Besiegte Firmen geben nur Bereiche frei, aber Verbesserungen individuell
       - Food/Water Supplies Company (Erster Gegner; Stolen Knowledge / Algae)
            - bad air, people get ill; experiments on own workers; lose health every day
            - Infiltration durch Luftschacht, stealthmission; finde algenprobe und baue eigenen reaktor
            - Wachmänner müssen befreundet, mit item abgelenkt oder krank gemacht werden. (Gift in Heim)
            - Sobald Luft besser, AirLock öffnet sich zu neuem Bereich
            - Zeitung meldet später, dass Luft sich verbessert hat wegen Firma, keine Folgen für Firma
        - Food/Water Supplies Company (Shared Knowledge / Patente)
            - Biomasse wird anonym eingekauft, um Arbeitsverträge zu vermeiden. (Akkordarbeit, Werkvertrag)
            - Spieler gibt viel ab, wird bemerkt. Kriegt spezialauftrag. Gff bekommt anzug um Ceresflora zu sammeln
            - Kriegt mehr Zugang, stiehlt Geheimnisse
            - Öffentlich bekannte anbaumethode/Gerät führt zu lokal produzierter Nahrung
            - Adia is a small coffee company owned
            - Firma nicht wirklich besiegt, muss aber einlenken um Kunden zu behalten
        - Trade Company "Smile" (Zweiter Gegner; Wisthleblowing, Gemeinschaftsarbeit)
            - destroys Ceres Ecosystem, erhöht Preise künstlich
            - Produkte stehlen, Riot => Kurzfristige Verbesserung aber dann mehr Security, Situation verschlechtert sich
            - WistleBlower über Preisabsprache und Eigenproduktion verbessert Situation
        - CereX owns base and maintains station with Colony Ship (Endgegner; Revolution?)
            - kontrolliert andere Fraktionen, cut off base resources
            - Wird aktiv nachdem reichen viertel gestürmt
        - Mineralcompany (Arbeitskampf?)
            - schlechte, gefährliche Arbeitsbedingungen
            - Protest führt zu Entlassung und maschinsierung
            - Revolution zu besseren arbeitsbedingungen und produkten für die menschen
        - Medics Company (Giftterror?)
            - Heilt niemand, will weiter Kunden haben; teuer
            - Protest: Some products are cheaper, but more people are waiting
            - Revolution: Gesundheitssystem heilt und ist nicht mehr überlastet, jeder wird behandelt


------------------------------------------
----- INSPIRATION ------------------------
------------------------------------------
Oasis-Movie
    Eigenes Team und böse Firma wollen Kontrolle über Oasis, Firma inhafiert Mitglied, wird befreit und ist jetzt in der Firma, schädigt von innen

Snowpiercer
    Zu indem alle Menschen sind, die Armen am Ende kämpfen sich nach Vorne. Sehen Reichtum. Am Ende wird klar dass Anführer zusammengearbeitet haben, viele grausame Maßnahman waren zum überleben.
    Drakonische strafen wie Gliedmasen trennen; Message übermitteln mit Stofffetzen und ablenkung

Snowpiercer Series
    Strenge Klassengesellschaft, Arbeitskräfte für Oberklasse, spezielles Talent wird gebraucht (Detektive).
    Auch erste Klasse nicht homogen, einige Mitfühlend und helfen Revolution. Mehrere verfeindete Fraktion in der ersten die Rev ermöglichen

Passenger
    Protagonist wacht wegen technischem Defect auf, alleine auf Schiff, wird sterben bis ankommt. Weckt andere Person. Retten schiff leben glücklich. Klassen/Ticket unterschiede sichtbar. Fusionsreaktor

Prey
    You are the boss of the station and have to save the world from investation; the whole game is a test thereby

How to Revolution
    Angst vor dem Gefängnis schlimmer als Gefängnis selbst, Information macht Mut. Organize people outside to call police and ask questions, celebrate once freed.
    Use social media to show police bruality, let oppression backfire.
    Invite all groups, patriotic and liberal under one motto, not too much topics.

Die Form des Wassers
    Einfache Putzkräfte haben Zugang zu geheimen Bereichen, bei Befragungen werde sie aber nicht ernst genommen

Lupin
    Mitarbeiter hilft Räubern beim Eindringen, er kennt Sicherheitsprotokolle und wie man sie umgeht. Rein/raus als MA
------------------------------------------
----- SHORTKEY ---------------------------
------------------------------------------
- Windows Recorder: Win + G
- Ctrl + Alt + </>: Jump to code parts
- Ctrl + (numblock)/: Comment line
- jar xf .\Ceres-1.0-SNAPSHOT.jar => To unzip jars
- Create Executable by
    gradle->tasks->distribution->installDist
    In A:\IdeaProjects\Ceres\build\install\Ceres\bin use Ceres.bat
