# Elimination and Ranking

## Abstract

Instead of forcing the tournament organizer into
choosing which tournament type is best for them,
the system will operate in two high-level modes
that abstract common tournament types very well.

The original incentive by the author
was to create a system that would first
let participants compete against each other in elimination rounds,
and then carry out the finals in one of two fashions.
That would either be Round Robin (in case of 3 players)
or a Half Final and the Finals (in case of 4 players).

Round Robin heavily deviates from the Elimination pattern,
while Half Final and Final are more or less an extension of it.
While it would be possible to restrict tournament flow
to one of these options and only possibly allow future extensions,
it would be much cleaner and forward-looking
to implement a system that doesn't come with any of such constraints.

## Task

Find an abstraction for the above goals.
The system should enable the user
to carry out the listed tournament types,
while being future-proof
and easy to understand and use.

## Analysis

Taking a step back,
one can observe a couple of important properties of the stated goals.

### Elimination

The purpose of an elimination round
is to reduce the number of entrants in the tournament,
such that the concluding final pairings 
do not have to cope with too many competitors,
to a point where it would become clumsy and unmanageable.

The first step is thus to let pairs of participants
compete against each other in order to halve the number of entrants.
This is repeated in so many iterations,
until the tournament organizer assesses the participant count
to be feasible for determining the final standings. 

This assessment can be subjective or enforced by external factors.
One of these factors could be
the required number of placements,
like 5 instead of the common 3.
Enforcing a specific practice
might not be beneficial to the success of the tournament organizer,
in some cases even so restrictive that it renders the system unusable.

### Ranking

After enough iterations of elimination,
the organizer may choose to proceed
to ranking the tournament's participants
that have advanced to the finals.

#### Round Robin

In this format the remaining participants are paired,
such that every participant competes once against all others. 
Ideally the result would be 






## Restrictions of Ranking Mode

### Adding players

One cannot add new players to the round.


## Behaviour

The tournament switches from elimination to ranking mode automatically,
based on the number of standings that the user expects to be generated
(i.e. the argument passed to `new`).
This only happens when advancing to the next round,
but not dynamically during a round
since that might cause serious trouble.

A manual switch to ranking mode
first prints the number of rounds that would be added.
This way the tournament organizer can assess
if the time and effort required is feasible.


## Proposed Interface

### Tournament Management

Command | Function
:--- | :---
`new <N>` | Creates a new tournament resulting in `N` standings.
`pause` or `end` | Puts the current tournament in the background (inactive).
`select [<ID>]` | Selects the last or a specific tournament as active (foreground).
||

### Round Management

Command | Function
:--- | :---
`add <Player>` | Adds a player to the current round.
`remove <Player>` | Removes a player. Achieved results are kept.
||
`next` | Advances to the next round. Switches to ranking mode if applicable.
`back` | Goes back to the previous round. Only possible without any results.
||

[comment]: <> (Declaring a round as the final round is only possible)

[comment]: <> (if enough players are participating.)

[comment]: <> (The number must be)

[comment]: <> (greater or equal to `N` &#40;the number of standings&#41;)

[comment]: <> (but less than `2N` &#40;enough participants to eliminate half of them&#41;.)

### Pairing Management

Command | Function
:--- | :---
`pair` | Pairs two players (randomly, if applicable) conforming to sequence logic.
||
`win <Winner> [<Loser>]` | Declares a player winner of their pairing or against a specific player.
`tie [<Player> [<Player>]]` | Declares the single active pairing, the one of a player or a specific one  a tie.
||
`replay [<Player> <Player>]` | Replays the pairing that was last finished or a specific one.
||

### Dummy Pairings

Sometimes it might be desirable to create a pairing for a player,
without having to let the system know with whom they are paired
because the opponent just acts as an intermediate opponent
to determine if that player advances to the next round or not.

Adding that dummy opponent to the round
and forgetting to remove them after
could alter the course of the tournament.
While this might not be irrecoverable,
it's certainly annoying.
That's what these commands are for.

There can only be one active dummy round at a time.
Dummies can only be used in rounds in elimination mode.
Ranking rounds should never require a dummy.

Command | Function
:--- | :---
`pair dummy [<Player>]` | Adds a dummy pairing for the last remaining or a specific player.
||
`win dummy` | Declares the dummy as the winner.
`tie dummy` | Declares the dummy pairing as a tie.
||
`replay dummy [<Player>]` | Replays the last dummy pairing or a specific one. 
||

### Shortcuts

Frequently issued commands deserve shortcuts.

```
a add
r remove
p pair
w win
```

### Workflows

#### Delay a Pairing

Generally one can just create the next pairing.

```
/pair  # just leave this open

/pair
```

One can also temporarily remove the player
and add them back later.

```
/pair

/remove PairedPlayer
# ...
/add PairedPlayer
/pair  # continue
```

Be cautious.
One should not advance to the next round then.

#### Undo Declaring a Winner

Useful when mistakenly declaring the wrong player the winner.

```
/win WrongPlayer

/replay
/win CorrectPlayer
```

#### Undo Creating a Pairing

If, for some reason, this pairing shouldn't be carried out.

```
/pair

/remove APlayer
/add APlayer
```

#### Back to Previous Round after Declaring a Winner

When there are no results (finished pairings),
one can go back to the previous round.
So simply replay all played pairings.

```
/next
/pair
/win Player

/replay
/back
```


---

```
/add <Player>  --  Adds the player to the tournament
/remove <Player>  --  Removes the player. Results are kept.

/pair  --  Pairs two random players conforming to sequence logic.
/cancel [<Player> [<Player>]]  --  Cancels the last pairing (of a player) or a specific one.
/replay [<Player> <Player>]  --  Replays a pairing or the last finished one.  

/tie <Player> [<Player>]  --  Pairing (of a player) is a tie.
/win <Winner> [<Loser>]  --  Player has won their pairing (against Player).
/revive <Player>  --  Gives a player another chance. Only during elimination.

/knockout  --  Switches to elimination mode. Initial mod for a new tournament.
/ranking  --  Switches to ranking mode.

/info [...]  --  Gives information about a tournament aspect.
```

#### Notes

* One cannot pair players manually

```
Intervenation (for the future):
/pair <Player> <Player>
/reset <Player>
```
