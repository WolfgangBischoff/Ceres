
#Backlog

####Bug
       
#### Main Quest
Level: auf Seite Lift, viele Wohnungen, gemeinschaftplatz mit Müll und Resistance
* Introduction to Ceres and you room
* Find work at bioTech company => move by train then person dialogue
  * unpaid traineeship to know process by trainee bacteria
  * finde respawning materials => bring me fuella to get electric bac
* Investigation starts (mail after part1 of training)
* Get help of resistance (somebody blocks the way) => go back to capigeni and get fuel
* augen verdunkelt, base is shown with team, its not safe here, we need a base
* plan: We have a ship with no fuel 
  => go to work and steal fuel in manufacturing area
  => Option A; Create Electric Lockpic in advanced area (get key after first day, then spawn) 
  => Option B; Get in touch with teacher, and tell you need to the storage
  => Option C; Use mtx area, maybe blocked by cartonage that can be removed, its blocked on the first day
  => bring to resistance
* start engine and flee
* smuggler docks shuttle
##### Main Quest Steps
* Monrail has TraineeArea option after aplication at worktime
* Trainee Area on first day
   * has spawning trainee bacteria
   * options to manufacturing are blocked
* Mail after successfull traineeship (receptions blocked)
    * Resistance at place stop you
    * Goto hideout with sister (shuttle bay) => monrail unblocked and start part 2
* Trainee Area second Day
    * get access to advanced area, mtx, need storage dialogue
    * steal fuel
    * meet resistance => goto hideout => goto ship



#### Speicher
* Problem mit Persistens, wird nur geladen beim ersten laden des basis files, 
danach lassen sich keine solchen actors mehr hinzufügen, ggf später ein problem bei truhen die später kommen sollen und die nicht immer neu geladen weredn können
* V2: Levelstate weiß welche base files und included bereits geladen wurden, bei erstmaligem laden werden persistente hinzugefügt
* V2: Persistente bleiben im Speicher
* V2: Base Karte bleibt auch im Speicher, includes werden geprüft und ggf vom Speicher geholt
* V2: Respawn wird über eigenen persistenten ActorType gemanaged
* V2: Includes abhängig von Var und diskreter Zeit die reload triggered wie Tag/Nacht wechsel
#### Konzept laden
* Einige Sachen sollen immer geladen werden => Immer vom File oder Save (aber keine Spawnitems mehr)
* Einige Sachen sollen nur einmal geladen werden und ihr zustand gespeichert werden. bzw ihre Löschung => Persistent tag, aber merken ob file mal geladen wurde (Auch items)
* Andere sollen erst abhängig von Vars geladen werden => Über FileCondition
* Andere in Zeitabständen geladen werden => Unsichtbares Item hat SpawnData(eg. alle zwei Stunden, um 0000 Uhr)
* Reload Befehl von Actor/Textbox

#### Zeit
* Sprite: Change dialogue, img, position, script at time (eg. when to enter an area and dialogue)

#### Bacteria mechanic
* While Spreading, new bacteria can be created/ kreuzung => mit example bild sprite
* Bacteria Resource can be put into splicer to craft or extract resource

#### Inventar
* Teilen button
* render method for CollectibleStack, damit auch der Incubator anzahl anzeigt

#### Various
* first stranger sound
* Test: StatusInfo over sprite on key + ExclamationMark
* "Rumors"/Random dialogue
* Textbox Show Name of Person/Sprite eg. Julian Soulles : Trader and img defined in xml
* Test: Dialogoption sollte weg sein bis retrigger
* Actor that listens to variable and changes dialogue if set
* Show money in Shop view
* Coin Game
    * Test: Game ends if no correct coins are left
* Cooperation value just increases after time
* Create Questlog

####Sprites
* mehr NPC, Varianten Generiert
* Docking Bay Bench im Tisch look
* einrahmen der meldung bei items
* aktenschrank; schachteln; pflanze, water dispenser/sink, Server
* Ship: rename sleep capsules to numbers
* Industrial: Ventilation/Movingpart with bridge, Lavalampen Maschine, solar panels
* Hammer and Sickle modern, Säulen, Tank    
* Placeabel Structures: Kindergarten, HoloKino, Bar, Schule, Krankenhaus, Küche, LuftErfrischer, Medics, Brothel, Museum
* test: Air hub deutlicherer sprite das kaputt


# Ideas
##Basemechanics
### Zeit
* Spieler muss schlafen, Hunger wird beim schlafen aufgefüllt
* In schlafzeit wird Wirtschaft weiterberechnet, man muss also ggf täglcih lager auffüllen

### Transform your Transporter to a better place to live
* Build and maintain Support Infrastructure
  - Food/Air Recycling 
    => Food ist recycelt by 90%, for new persons we have to stock up. Constant supply of neutral biomass from bacteria
  - Private Capsules show who lives there
    => Upgrades
  - Holo Cinema
    => Goods from Ceres
  - Bar
  - Childcare
  - Medical Bay
  - Garden with Earth Food
* Gather resources on Ceres or asteriods => Random Encounters with hazards or enemies
  - Lost Places without athmosphere
  - Criminal UnderStation
  - Asteriod, Crypto Volcano
* Find new People for the ship, maybe moral dillema (bad skills vs good, Laster)
  - People come to better their life
  - leave if needs not fullfilled
  - Your access to Ceres/resources is limited by the MaM
  - You are not biometrical registered on Ceres, you can move like a trader
  - You are a criminal, stay undetected during operations
  - Citiziens of Ceres are registered, you fellows are dept slaves
  - Level 0 => Open gates
  - Level 1 Ausfuhrbeschränkungen, bestimmte Güter werden konfisziert (bypass)
  - Level 2 ID Checks, Checkpoint are a no Go

###Ceres
* board time and ceres time are different, ceres night/day changes world
* public areas / maintenance areas / Work areas / forgotten areas to trade/harvest/scavenge/sabotage
    * Propaganda actions increase revSpirit
    * Once revSpirit Threshold is reached you can go to another area/ world changes
    * Bonus for good service in an area, endgame if all areas are maintained
    * Maintain Service by inputs to increase revolutionary spirit

###Work for companies
* destroy them from inside (Tagelöhner) and get resources
* kartenbasierten mechaniken
* mining (crypto vulcano or asteroid)
* Medicine (cook serve delocious)
* puzzle; suchen im dunkeln

###Random Under-Station
* Siege Survival: Gloria Victis
* Guards that should not see you or enviroment hazards
* randomly generated to loot and textadventures
* getting tips from rumors for events

###Social
* go to cafes and talk to people, befriend them
* everyday other people there
* meet entertainers and use their influence
* storyrelevant characters that can be recruited => assign to activieties 
* Dossiers in INFO, background story

###Textadenture wie Gloria Vicits

##Contents
- Checkpoints als Barierer, man will nicht kontrolliert werden
-Nightclub
- get a robot begleiter, Robot Avatars, remote control, oder Amöbe
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



### Lore & World (Info not related to Plot, History, descriptions)
##### General 
- Earth damaged, people want to live in space and station, Ceres is base for asteriod belt mining
    - Private Company build Station, Earth law doesnt apply, Earth Gov am Gängelband
    - Free trade and frequent new faces from mining ships
- Subscriptions to benefits like housing, food and air
- Board time / Ceres Time (9h 4min per day) connection affects world (year 4,605 years)
- Society changes, very liberal. Open Sex at Clubs, man/women not important, just prefered position, no classic partnerships
- People are created by bioengeneering, sexuality is fluent
- Children usually designed, just Ökos take old way
- drugs are normal
- home-delivery of goods is standard, maybe stealing?
- biotechnology is advanced: Materials (green) for goods, human genes optimmized

##### Ceres
- Ceres Base gets energy and water from old colony ship, which works automated;
- The Colony Ship has no air life systems, but in secret workers life there under bad conditions (air masks all day, dept slaves from cryo capsules)
- The trade hub contains a space elevator and in secret luxury apartements
- There are water and mineral resource fields near cryo vulcanos + other asteriods
- Resources very limited, nearly autarc; cheap drones with oxygen fuel can sent back resources, but oxygen is rare;
-   goods from earth must be sent to space, expensive, from ceres weight is not a problem

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
        - Medics Company "CapiGen" (African Company, founded in Cap ...?)
            - Heilt niemand, will weiter Kunden haben; teuer
            - Protest: Some products are cheaper, but more people are waiting
            - Revolution: Gesundheitssystem heilt und ist nicht mehr überlastet, jeder wird behandelt



### Inspiration

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
- jar xf .\Ceres-1.0-SNAPSHOT.jar => To unzip jars and check paths
- Create Executable by
    gradle->tasks->distribution->installDist
    In A:\IdeaProjects\Ceres\build\install\Ceres\bin use Ceres.bat (GitShell, JAVA_HOME must be set to JDK-11)
    JAVA_HOME example: A:\Java\jdk-11
    https://sourceforge.net/projects/openjdk11/
