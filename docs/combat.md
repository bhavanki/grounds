# Combat

The Grounds combat system, nicknamed "Grapple", is a simplified version of the combat system used in [Anima Prime](http://animaprimerpg.com/). Combat takes place between two or more teams of players; a player may be a regular human player or an NPC. Players attack each other and get knocked out. Combat is over when no more than one team has players which aren't knocked out. (Usually there is only one team left, but it's possible to have the last two teams lose simultaneously.)

Combat is not an integral part of Grounds, so use it only when players wish to.

*Note: As of this writing, the combat system is new. The rules may change with time, including addressing some "bugs" in the system.*

## Stats

Players in combat have a set of stats that track their status and what they are able to do, if anything.

Grapple is a dice-based combat system, and players start with some number of dice in their Action Pool (AP). Dice in the Action Pool are called Action Dice (AD). Every player's stats includes their maximum AP size as well as the current number of AD they have.

Using maneuvers (described below), players turn AD into strike dice (SD), which are then used to attack other players. There is no limit to SD. Stats include the current number of SD.

Every player's stats also includes a defense number and a maximum wound limit. Defense protects a player from attacks, and wounds act like hit points. When a player accumulates as many wounds as their wound limit, they are knocked out and cannot participate in combat anymore.

### Skills

Finally, every player has a set of three skills. A skill describes how a player approaches combat and grants the ability to perform a special skill-based action instead of a strike. The table below lists the Grapple skills and their action effects and difficulties. Use of skill actions is described below.

<table>
  <tr><th>Skill</th><th>Abbreviation</th><th>Action</th><th>Difficulty</th></tr>
  <tr>
    <td>Accuracy</td><td>ac</td><td>-1 defense to target</td><td>3</td>
  </tr>
  <tr>
    <td>Courage</td><td>co</td><td>+6 AD to self</td><td>2</td>
  </tr>
  <tr>
    <td>Endurance</td><td>en</td><td>+1 defense to self</td><td>2</td>
  </tr>
  <tr>
    <td>Intimidation</td><td>in</td><td>-4 AD to target</td><td>2</td>
  </tr>
  <tr>
    <td>Leadership</td><td>ld</td><td>+3 AD to target</td><td>2</td>
  </tr>
  <tr>
    <td>Medical</td><td>md</td><td>-1 wounds to target</td><td>4</td>
  </tr>
  <tr>
    <td>Speed</td><td>sp</td><td>+1 die to maneuver</td><td>2</td>
  </tr>
  <tr>
    <td>Spirit</td><td>sr</td><td>+1 die to strikes</td><td>4</td>
  </tr>
  <tr>
    <td>Strategy</td><td>sy</td><td>+4 AP max to self</td><td>3</td>
  </tr>
  <tr>
    <td>Tactics</td><td>tc</td><td>+2 SD to self</td><td>2</td>
  </tr>
  <tr>
    <td>Taunting</td><td>tt</td><td>-2 SD to target</td><td>2</td>
  </tr>
  <tr>
    <td>Trickster</td><td>tr</td><td>-all SD from target</td><td>4</td>
  </tr>
</table>

Every player has one skill with rating 4, one with 3, and one with 2. A skill with a higher rating is more effective.

### Setting up a Player for Combat

When not in combat, stats are stored in player attributes. Before entering combat, a player must have attributes set for each Grapple attribute. Generally,
the attributes only need to be set once; they are drawn upon for each combat
which the player participates in. The recommended initial values are listed in
the table below.

<table>
  <tr><th>Stat</th><th>Attribute</th><th>Type</th><th>Initial value</th></tr>
  <tr>
    <td>Rating 4 skill</td><td>grapple_skill4</td><td>STRING</td><td>n/a</td>
  </tr>
  <tr>
    <td>Rating 3 skill</td><td>grapple_skill3</td><td>STRING</td><td>n/a</td>
  </tr>
  <tr>
    <td>Rating 2 skill</td><td>grapple_skill2</td><td>STRING</td><td>n/a</td>
  </tr>
  <tr>
    <td>AP max size</td><td>grapple_apMaxSize</td><td>INTEGER</td><td>10</td>
  </tr>
  <tr>
    <td>AD</td><td>grapple_ad</td><td>INTEGER</td><td>5</td>
  </tr>
  <tr>
    <td>SD</td><td>grapple_sd</td><td>INTEGER</td><td>0</td>
  </tr>
  <tr>
    <td>defense</td><td>grapple_defense</td><td>INTEGER</td><td>3</td>
  </tr>
  <tr>
    <td>max wounds</td><td>grapple_maxWounds</td><td>INTEGER</td><td>3</td>
  </tr>
</table>

Here are example commands for setting the attributes for a player.

```
$ set_attr me grapple_skill4[STRING]=Accuracy
$ set_attr me grapple_skill3[STRING]=Courage
$ set_attr me grapple_skill2[STRING]=Endurance
$ set_attr me grapple_apMaxSize[INTEGER]=10
$ set_attr me grapple_ad[INTEGER]=5
$ set_attr me grapple_sd[INTEGER]=0
$ set_attr me grapple_defense[INTEGER]=3
$ set_attr me grapple_maxWounds[INTEGER]=3
```

At this time, Grounds does not prevent players from arbitrarily altering their Grapple attributes outside of combat! While players should be encourage to change their skills as their character's personality evolves, honorable players will leave the other attributes alone unless prevailing rules award them with the chance to change them. (The combat system does update some stats when combat ends.)

## Starting Combat

Any non-guest player may initialize combat in a location. Only one combat may be active in a location at a time.

```
$ combat init myfight
Created combat myfight at Tavern. Add players to teams, and then start combat.
```

The player who starts combat is the *combat owner*. Only the combat owner (and wizards) may use some combat commands, as described in the remainder of this document.

After combat is initialized, players may be added to it. Players are always added to a team; if the team doesn't exist, it's created.

```
$ combat add me heroes
+ Ahalish is added to team heroes
```

A player may add themselves to a combat, and the combat owner may add other players to the combat to help populate the teams. If a player doesn't have all of the Grapple attributes set, they cannot be added to combat.

Similarly, players may be removed from combat. The combat owner may remove other players from the combat, too.

```
$ combat remove me heroes
+ Ahalish is removed from team heroes
```

The `combat status` command summarizes who has been added to which team.

```
$ combat status
$ combat status
Players are still being added.
- heroes: Ahalish
- villains: Rehtaoh
```

Once all players have been added, the combat owner starts combat. Combat cannot start unless there are at least two teams (each with at least one player). The combat owner may list teams to set their moving order. If no teams are listed, they are ordered randomly; if only some teams are listed, those teams move first in the given order, and the rest are ordered randomly after them.

```
$ combat start heroes villains
```

After starting combat, no players may be added or removed.

### NPCs

The combat owner may add NPCs - temporary, non-player characters - to combat. NPCs may be added to teams along with human players, or may be kept in teams of their own for a purer PvE experience.

The stats for an NPC must be provided when adding them, using a single string with the following format:

```
<skill4>:<skill3>:<skill2>:<max AP size>:<defense>:<max wounds>:<ad>:<sd>
```

Unlike a regular player, an NPC only needs one skill (of any rating), but may have up to the normal limit of 3. Specify a skill with either its name or abbreviation, or with nothing for an empty skill slot. In this example, the NPC has: an endurance skill with rating 2, an AP max size of 6, defense 1, max wounds 2, starting AD 3, and starting SD 2.

```
$ combat add_npc goblin1 ::en:6:1:2:3:2 monsters
Added goblin1 to team monsters
```

The combat owner can remove NPCs as well.

```
$ combat remove_npc goblin3 monsters
Removed goblin2 from team monsters
```

## Running Combat

Once combat starts, running combat status is shown to all participating players. Any player can use the `combat status` command to see the status again at any time. The status is also shown to all players after each move.

```
$ combat start heroes monsters
+ Combat has started!
=====================================
Round: 1
- - - - - - - - - - - - - - - - - -
heroes <-            S4 S3 S2 AD/AP SD DEF WOUNDS
---------            -- -- -- ----- -- --- ------
Ahalish <-           ac co en  6/10  0   3
- - - - - - - - - - - - - - - - - -
monsters             S4 S3 S2 AD/AP SD DEF WOUNDS
--------             -- -- -- ----- -- --- ------
goblin1                    en   3/6  2   1
goblin2                    en   3/6  2   1
=====================================
```

Each team is listed in status with all of its players. The stats for each player are displayed to the right of their name. Skills are shown by their abbreviations, and most of the other stats are shown numerically. The wound count is shown with symbols; initially, usually all players have no wounds, so that column is blank.

### Rounds

Combat proceeds in rounds, and every team moves in each round. Teams move in the order set by the combat owner when combat starts. An arrow in the combat status indicates which team is moving. When an arrow is pointing to a player's name, that player still needs to move. After all players on a team who are not knocked out or otherwise disabled have moved, it's time for the next team to move. After the last team moves, combat proceeds to the next round, and the first team moves again.

Players on a team can move in any order; normally, a player can only move once in a round. Team members should talk amongst themselves to figure out their strategy, including move order, for the round. For example, a player with a skill action that increases someone else's strike dice may want to go first so that another player can attack with more dice.

### Player Moves

A player moves using the `combat move` command. There are a few different types of combat moves.

#### Dice Rolls

Most combat moves involve dice rolls. Every die is six-sided. A die roll is considered a *success* if it comes up 3 or higher.

#### NPC Moves

The combat owner is responsible for moving NPCs. The commands for moving NPCs are just like those for regular players, but the root command is `combat move_npc`, and the first command argument is always the name of the NPC that is moving.

#### Maneuver

A maneuver turns AD into SD. which are needed to attack enemies. To maneuver, decide how many AD to spend: between 1 and 3. Then, also select a skill to use for the maneuver. In this example, the player uses three AD and their courage skill. (The skill can be specified by name or abbreviation.)

```
$ combat move maneuver 3 co
+ Ahalish moves: The maneuver succeeds (4/6): AD-3=3 SD+4+0=4
  New team moving: monsters

=====================================
Round: 1
- - - - - - - - - - - - - - - - - -
heroes               S4 S3 S2 AD/AP SD DEF WOUNDS
------               -- -- -- ----- -- --- ------
Ahalish              ac CO en  3/10  4   3
- - - - - - - - - - - - - - - - - -
monsters <-          S4 S3 S2 AD/AP SD DEF WOUNDS
-----------          -- -- -- ----- -- --- ------
goblin1 <-                 en   3/6  2   1
goblin2 <-                 en   3/6  2   1
=====================================
```

The combat system rolls the following dice to determine whether the maneuver succeeds:

* the number of AD spent
* an additional number of dice equal to the chosen skill's rating

In the example above, courage has a skill rating of 3, so a total of 3 + 3 = 6 dice were rolled. Four of them succeeded, so the player gained 4 SD. The AD count is reduced by 3 to 3.

No more than 5 SD can be earned by a maneuver.

If there are no successful rolls, the maneuver fails, but all of the AD are returned to the AP for another try.

When a skill is used in a maneuver, it is "marked" and listed in capital letters in combat status. After using all three skills for maneuvering, a player automatically gets 2 bonus SD for their next maneuver. This encourages players to not always maneuver with their strongest skill. This bonus is awarded even if the maneuver fails.

An NPC maneuvers similarly. When an NPC has less than 3 skills, they never receive bonus SD for using all of the skills.

```
$ combat move_npc goblin1 maneuver 2 en
+ goblin1 moves: The maneuver succeeds (4/4): AD-2=1 SD+4+0=6

=====================================
Round: 1
- - - - - - - - - - - - - - - - - -
heroes               S4 S3 S2 AD/AP SD DEF WOUNDS
------               -- -- -- ----- -- --- ------
Ahalish              ac CO en  3/10  4   3
- - - - - - - - - - - - - - - - - -
monsters <-          S4 S3 S2 AD/AP SD DEF WOUNDS
-----------          -- -- -- ----- -- --- ------
goblin1                    EN   1/6  6   1
goblin2 <-                 en   3/6  2   1
=====================================
```

#### Strike

A strike (or attack) turns SDs into wounds on another player. To strike, decide how many SD to spend: between 1 and 6. Also select a target. (There is nothing stopping a player from attacking someone on their own team.) In this example, the player uses 2 SD to strike.

```
$ combat move strike 2 goblin1
+ Ahalish moves: The strike succeeds (2/2 vs. 1): SD-2=2 Wounds=2
  New team moving: monsters

=====================================
Round: 2
- - - - - - - - - - - - - - - - - -
heroes               S4 S3 S2 AD/AP SD DEF WOUNDS
------               -- -- -- ----- -- --- ------
Ahalish              ac CO en  3/10  2   3
- - - - - - - - - - - - - - - - - -
monsters <-          S4 S3 S2 AD/AP SD DEF WOUNDS
-----------          -- -- -- ----- -- --- ------
goblin1                    EN   1/6  6   1     XX
goblin2 <-                 EN   1/6  5   1
=====================================
```

The combat system rolls the SD spent and counts the number of successes. If the number of successes exceeds the target's defense, the strike succeeds. Otherwise, the strike fails, but the SD are returned to the player for another try.

In the example above, 2 SD are rolled and both are successful. Since that exceeds the target's defense of 1, the strike succeeds, and the target is wounded.

The number of wounds from a strike is the number of successful rolls divided by the target's defense, rounded down. So, it is possible for a particularly powerful strike, or a weakened enemy, to produce multiple wounds. In the example above, 2 SD divided by 1 defense equals 2 wounds.

When a player's wound count equals their maximum wounds, they are knocked out. A knocked out player is disabled and cannot move.

An NPC strikes similarly.

```
$ combat move_npc goblin2 strike 4 Ahalish
+ goblin2 moves: The strike succeeds (4/4 vs. 3): SD-4=1 Wounds=1
  New round:       3
  New team moving: heroes

=====================================
Round: 3
- - - - - - - - - - - - - - - - - -
heroes <-            S4 S3 S2 AD/AP SD DEF WOUNDS
---------            -- -- -- ----- -- --- ------
Ahalish <-           ac CO en  3/10  2   3      *
- - - - - - - - - - - - - - - - - -
monsters             S4 S3 S2 AD/AP SD DEF WOUNDS
--------             -- -- -- ----- -- --- ------
goblin1                    EN   1/6  6   1     XX
goblin2                    EN   1/6  1   1
=====================================
```

#### Skill Action

A skill action turns SD into stat changes. Some skill actions are directed at the moving player, while others are directed at a target (not including the moving player). See the skill table above for the available skill actions. You must have a rating for a skill to perform its action in combat.

To perform a skill action, decide how many SD to spend: 0 to 6. Then, also select a skill for the action, and if necessary, a target. In this example, the player uses 2 SD and their endurance skill. Since the endurance skill action targets the player, no target is listed in the command. (Otherwise, the target's name comes last.)

```
$ combat move skill 2 endurance
+ Ahalish moves: The skill action fails (1/2 vs 2). No SD were spent.
  New team moving: monsters

=====================================
Round: 3
- - - - - - - - - - - - - - - - - -
heroes               S4 S3 S2 AD/AP SD DEF WOUNDS
------               -- -- -- ----- -- --- ------
Ahalish              ac CO en  3/10  2   3      *
- - - - - - - - - - - - - - - - - -
monsters <-          S4 S3 S2 AD/AP SD DEF WOUNDS
-----------          -- -- -- ----- -- --- ------
goblin1                    EN   1/6  6   1     XX
goblin2 <-                 EN   1/6  1   1
=====================================
```

The combat system rolls the following dice to determine whether the maneuver succeeds:

* the number of SD spent
* an additional number of dice equal to the chosen skill's rating minus 2

The combat system counts the number of successes. If the number of successes exceeds the action's difficulty, the action succeeds, and the recipient's stats are adjusted. Otherwise, the action fails, but all of the SD are returned to the AP for another try.

In the example above, endurance has a skill rating of 2, so a total of 2 + (2 - 2) = 2 dice were rolled. Only one of them succeeded, so the action failed.

An NPC performs a skill action similarly.

```
$ combat move_npc goblin2 skill 1 en
+ goblin2 moves: The skill action fails (0/1 vs 2). No SD were spent.
  New round:       4
  New team moving: heroes

=====================================
Round: 4
- - - - - - - - - - - - - - - - - -
heroes <-            S4 S3 S2 AD/AP SD DEF WOUNDS
---------            -- -- -- ----- -- --- ------
Ahalish <-           ac CO en  3/10  2   3      *
- - - - - - - - - - - - - - - - - -
monsters             S4 S3 S2 AD/AP SD DEF WOUNDS
--------             -- -- -- ----- -- --- ------
goblin1                    EN   1/6  6   1     XX
goblin2                    EN   1/6  1   1
=====================================
```

#### Catching Breath

After many combat moves, a player's AD become depleted. Catching breath generates AD. Catching breath requires no AD, SD, or skill selection.

```
$ combat move catch breath
+ Ahalish moves: Catching breath succeeds: AD+3=6
  New team moving: monsters

=====================================
Round: 4
- - - - - - - - - - - - - - - - - -
heroes               S4 S3 S2 AD/AP SD DEF WOUNDS
------               -- -- -- ----- -- --- ------
Ahalish              ac CO en  6/10  2   3      *
- - - - - - - - - - - - - - - - - -
monsters <-          S4 S3 S2 AD/AP SD DEF WOUNDS
-----------          -- -- -- ----- -- --- ------
goblin1                    EN   1/6  6   1     XX
goblin2 <-                 EN   1/6  1   1
=====================================
```

Catching breath always succeeds and adds 3 AD to the player.

An NPC catches breath similarly.

```
$ combat move_npc goblin2 catch breath
+ goblin2 moves: Catching breath succeeds: AD+3=4
  New round:       5
  New team moving: heroes

=====================================
Round: 5
- - - - - - - - - - - - - - - - - -
heroes <-            S4 S3 S2 AD/AP SD DEF WOUNDS
---------            -- -- -- ----- -- --- ------
Ahalish <-           ac CO en  6/10  2   3      *
- - - - - - - - - - - - - - - - - -
monsters             S4 S3 S2 AD/AP SD DEF WOUNDS
--------             -- -- -- ----- -- --- ------
goblin1                    EN   1/6  6   1     XX
goblin2                    EN   4/6  1   1
=====================================
```

#### Command Abbreviations

Grounds offers abbreviations for combat commands to make moving easier.

* `combat` => `c`
* `move` => `m`
* `move_npc` => `mn`
* `maneuver` => `m`
* `strike` => `a`
* `skill` => `s`
* `catch breath` => `c`

Examples:

* `combat move maneuver 3 co` => `c m m 3 co`
* `combat move_npc strike 3 Ahalish` => `c mn a 3 Ahalish`
* `combat move catch breath` => `c m c`

## Saving and Restoring Combat

The ongoing state of combat is not automatically persisted, so if there is a server failure, it may be lost. After combat has started, the combat owner can save the state of combat so that it can be restored later. State is saved as an attribute of the combat itself.

```
$ combat save
Combat saved
$ combat restore
+ Combat has been restored to a prior state
Combat restored
```

For proper restoration, ensure that all (non-NPC) players participating in combat are active. Any who are not are converted into NPCs within the combat.

## Ending Combat

Combat is *over* when there is no more than one team with players that are not knocked out. Usually, this means there is one team left standing.

```
$ combat move strike 2 goblin2
+ Ahalish moves: The strike succeeds (2/2 vs. 1): SD-2=0 Wounds=2
  COMBAT IS OVER! Winning team: heroes

=====================================
COMBAT IS OVER! Winning team: heroes
- - - - - - - - - - - - - - - - - -
heroes <-            S4 S3 S2 AD/AP SD DEF WOUNDS
---------            -- -- -- ----- -- --- ------
Ahalish              ac CO en  6/10  0   3      *
- - - - - - - - - - - - - - - - - -
monsters             S4 S3 S2 AD/AP SD DEF WOUNDS
--------             -- -- -- ----- -- --- ------
goblin1                    EN   1/6  6   1     XX
goblin2                    EN   4/6  1   1     XX
=====================================
```

When combat is over, players can no longer move. When the combat owner is ready, they may then end combat. This destroys the combat and any NPCs in it.

```
$ combat end
+ Combat has ended.
Ended and removed combat myfight at Tavern
```

When combat ends:

* Each player's AD is retained.
* Half of each player's SD (rounded up) is converted into additional AD, up to the player's max AP size.
* All wounds are forgotten (healed).
* Any stat changes imposed by skill actions (e.g., defense bonuses, strike bonuses) are discarded and do not carry over to the next combat. (This does not include dice counts, as described above.)
* Skill uses during maneuvers are forgotten.

## Roleplaying

Grapple is oriented around roleplaying. While the system does not require players to RP during combat, it makes it more interesting.

* When maneuvering, describe what your player is doing that involves the selected skill. For example, when maneuvering using Accuracy, the player may be taking careful aim, or moving to an advantageous position.
* When performing a skill action, describe how the player uses the skill. For example, when using Taunting, say the taunt.
* When getting wounded or knocked out, describe what happens to your player.

In these descriptions, it's generally better to only describe the *intent* or *attempt* of the player, and not impose the consequences on the target (unless it's the player themselves receiving the effect, such as a beneficial stat change). A good pattern to follow is to RP the attempt at the combat move, then execute the move, and then (at the next opportunity) react to what happened.
