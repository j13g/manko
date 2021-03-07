# Interface

This document describes the interface of this modification.
The chat window of the client (the user of this mod)
is the only building block of this interface.
Every action should be invoked through commands.
Response messages are to be printed to the chat,
only visible to the user of this mod.


## Important elements

There are a couple of important aspects that will be
key takeaways in making a usable and powerful interface.

### Natural language

Commands should be named with natural language in mind.
They should be short and concise, and easy to remember.

### Shortcuts for frequent commands

Frequently used commands should get shortcuts,
i.e. an equivalent command with just a few characters,
preferably a single character.

### Auto completion

Commands should make as much use of
command or name completion as possible.
This way the user has to type less
and can reach their goals quicker.

### Communicate state changes

It is crucial to inform the user about important state changes.

When e.g. the finale needs to be played again
because every entrant has a single score point (with Round Robin of Three)
the user wants to know that the tournament is not over
after setting the third winner.
The finale needs to be played again.

Thus hidden state changes that
severely impact the course of the tournament
have to be communicated to the user
when they occur.


## Notation

Commands are written as follows:

```
/<Command> <Argument1> [<OptionalArgument1> [...]]
```

Shortcuts for commands are prepended like this:

```
/<Shortcut1> [/<Shortcut2> [...]] /<Command> ...
```

This would be an example of a command with two shortcuts:

```
/r /rm /remove <Player>
```

Below the command syntax and the description text
there is another code box listing all the possible response messages
that could be displayed to the user after they entered the command.

Each line represents a single message.
Each message has a number associated with it
that might be referenced later for explanation.
There is also a single letter
which categorizes the message in one of the following types:

| Letter | Symbol | Explanation | Modified state? |
| :---: | :--- | :--- | :--- |
| `O` | `OKAY` | The operation was successful. | Yes |
| `I` | `INFO` | This message is informational. | No |
| `E` | `ERROR` | The operation did not succeed. | No |

The number and letter are not part of the actual message.

There can also be a small letter (like a, b, etc.)
which makes the line it appears on an option.
Exactly one option is picked from all the options.
Wich option is picked depends on the current state.
This is done to save space and reduce redundancy
by duplicating the message for each option.

In case there needs to be text after those options
one must designate the starting point with a dash (`-`).
The message starting at the line with this dash
always appears, no matter which option was picked.

Messages can span across multiple lines
if they would be too verbose otherwise.
There can also be an empty line between messages
to semantically separate them.

```
1 O  This is a message.
2 O  This is message number two.
     It spans across two lines.

2 E  This is an error message.
a    The first option is here.
b    Alternatively this option is emitted.
     This line is still part of option b.
-    This message is not part of an option.
     It also spans two lines and is always displayed.
```

Below this box can be further details
about one or multiple messages.
Messages can be referenced by their number,
like `1` or `2`.

### Confirmation

Some commands need to be confirmed
before their actions go into effect.
These are labeled with `(y/n)` after that command.
The action is confirmed by entering the very same command again.
This is easily done by pressing the Up arrow key and pressing Enter.

The action can be canceled with `/cancel`.
(Apply prefixes as necessary)

### Command Prefix

At the very start of the section
there is a code box with command prefixes.
Each prefix is on a single line.
The prefix is to be prepended
before the command name and after the first slash `/`.

The command `/command` with prefixes `prefix:` and `p:`
would result in these two final commands:

```
/prefix:command
/p:command
```


## Commands and Messages

**Prefixes**

```
tournament:
t:
```

### Add an entrant

```
/a /add <Player>
```
Adds a player to the current round of the tournament.

The player is now participant of the tournament.

```
1 I  <Player> already participates in the tournament.
a    They are currently paired with another participant (<Participant>).
b    They have already advanced to the next round (vs. <Participant>).
c    They were already eliminated (by <Participant>).

2 O  <Player> was added to the tournament.
3 O  <Player> was added back to the tournament.
     They are still <state>. Reset the player if necessary.
4 O  <Player> was added to the tournament.
     Note that they were eliminated in a previous round.
```

In message `1`, depending on the state of the `<Player>`,
i.e. `Paired`, `Advanced` or `Eliminated`,
the options `a`, `b`, or `c` are picked respectively.

`<state>` in message `3` is either `advanced` or `eliminated`.
When the player has previously participated
and either won or lost a pairing,
removing and adding them back does not erase that state.
Thus adding a player back to the tournament
should inform the user about their (possibly) unexpected state
and that it can be reset.

Message `4` is displayed
when the player was eliminated in a previous round.
This might be interesting to know for the tournament organizer,
as usually you do not want to add a player again
that was eliminated previously
(unless he might get a second chance).

### Remove an entrant

```
/r /remove <Player>
```

Removes a player from the current round.

They do not participate anymore.

```
1 E  <Player> does not participate in the tournament.
2 E  <Player> is currently paired with another participant (<Participant>).
     To proceed, first reset one of the players or finish the pairing.

3 O  <Player> was removed from the tournament.
```

### Create a pairing 

```
/p /pair [<Player1> <Player2>]
```

Creates a pairing with two players.

When the optional arguments are omitted,
two players are picked at random from the pending entrants.

```
1 E  <Player> does not participate in the tournament.
2 E  Neither of the players participate in the tournament.

3 E  <Player> is already paired with another participant (<Participant>).
4 E  <Player> has already advanced to the next round (vs. <Participant>).
5 E  <Player> was already eliminated in a previous pairing (by <Participant>).

6 E  Both players are already paired with other participants.
7 E  Both players have already advanced to the next round.
8 E  Both players were already eliminated in a previous pairing.
9 E  <Player1> is already <state1>. <Player2> is already <state2>.

10 E  There are no participants left for another pairing.
11 E  Not enough players. There is just one player left (<Participant>).
12 E  The tournament is finished.

13 I  <Player1> and <Player2> are already paired.  
14 O  <Player1> and <Player2> were successfully paired.
```

Messages `1` through `9` are error messages
for the case when two players are explicitly designated,
i.e. arguments `<Player1>` and `<Player2>` are set.
Messages `10` and `11` can occur
when there are not enough pending entrants left to pick from.

Message `12` is for the case when it's the final round,
and the tournament is over.

The placeholders `<state1>` and `<state2>` can be one of
`pending`, `advanced` or `eliminated`.
They belong to `<Player1>` and `<Player2>` respectively.

### Declare a winner

```
/w /win /winner <Player>
```

Declares the winner of an active pairing.

They advance to the next round.
Consequently, the other player is eliminated.

```
1 E  <Player> does not participate in the tournament.
2 E  <Player> is not in a pairing.
a    They are currently pending.
b    They have already advanced to the next round (vs. <Participant>).
c    They were already eliminated (by <Participant>).

3 O  <Player> has won their pairing (vs. <Participant>).
     They advance to the next round. <Participant> was eliminated.
```

### Replay a pairing

```
/replay <Player1> <Player2>  (y/n)
```

Replays a pairing of two players in the current round.

This is equivalent to the following sequence of commands:

```
/reset <Player1>  (y)
/reset <Player2>  (y)
/pair <Player1> <Player2>
```

> **WARNING**  
> This is only the case with dynamic rounds.
> In a final round with three players,
> resetting a player might result in loosing all state
> as they might have played more than one round.

The precondition is that both players
are part of the same finished pairing.

```
1 E  <Player> does not participate in the tournament.
2 E  Neither of the players participate in the tournament.

3 I  The pairing between <Player1> and <Player2> is still running.
4 E  <Player1> and <Player2> did not appear in a previous pairing. 

5 O  <Player1> and <Player2> were reset and are now paired again.  
```

### Reset an entrant

```
/reset <Player>  (y/n)
```

Resets the state of a player in the current round.

If that player participates in the current round,
their state switches to `Pending`.
Otherwise, any remaining state is forgotten
and there will be no more information associated with that player,
as if they hadn't participated in this round at all.

It's important to understand that
only information of the current round is reset,
but not information of previous rounds.
Resetting a player that was eliminated in a previous round
has no effect.
This is to prevent deletion of historic events
that wouldn't affect the course of the tournament anyway.

#### Errors

```
1 I  <Player> does not participate in the tournament.
```

#### Confirmation

```
2 I  About to reset <Player>.
a    They are currently paired with <Participant>.
     A reset will cause <Participant> to be reset as well.
b    They have currently advanced to the next round (vs. <Participant>).
c    They have currently been eliminated (by <Participant>).
-    Please confirm this action to proceed.

3 I  About to reset <Player>.
     They do not actively participate in this round,
a    but they would advance to the next round
b    but they would be eliminated
-    if added back.
     Please confirm this action to proceed.
```

#### Success

```
2 O  <Player> was reset.
a    They were fully removed from the tournament.
b    They are now in pending state.
```

[comment]: <> (## Events)
