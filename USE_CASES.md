# KDGM Scoreboard — Use Cases

This document describes all use cases in the KDGM Scoreboard application.
It is organized so multiple AI assistants can work on separate areas in parallel.
No implementation details are included — only what the system does and why.

---

## What the system is

KDGM Scoreboard is a competition management system for running multi-game events.
An event (competition) consists of several individual games. Players are grouped
into teams. Each team plays every other team in every game. The system tracks
scores, calculates leaderboards per game, and produces an overall ranking.

---

## Area A — Player Management

**A1. Add a player**
An administrator registers a new participant by entering their name.
The player is stored and becomes available to be assigned to teams.

**A2. Edit a player**
An administrator can change the name of an existing player.

**A3. Delete a player**
An administrator can remove a player who is no longer participating.

**A4. List all players**
An administrator can see a table of all registered players.

---

## Area B — Game Management

**B1. Add a game**
An administrator creates a new game by providing:
- A name (e.g. "Bowling", "Darts")
- A game type: either **score-based** (higher is better) or **time-based** (lower is better)
- A calculation method: either **sum** (add up all individual scores) or **average** (divide the total by the number of players)
- Optional rules or description text

**B2. Edit a game**
An administrator can update the name, type, calculation method, or rules of an existing game.

**B3. Delete a game**
An administrator can remove a game that is no longer needed.

**B4. List all games**
An administrator can see a table of all registered games.

---

## Area C — Team Management

**C1. Add a team**
An administrator creates a named team and assigns players to it from a list of available players.

**C2. Edit a team**
An administrator can change the team's name or modify which players belong to it.

**C3. Delete a team**
An administrator can remove a team.

**C4. List all teams**
An administrator can see a table of all registered teams.

---

## Area D — Competition Setup

**D1. Create a competition**
An administrator creates a new competition by providing:
- A name
- A date
- Whether it uses single matches between teams or multiple matches (round-robin variant)

**D2. Assign games to a competition**
The administrator picks which games are included and sets the order in which they will be played during the event.

**D3. Assign teams to a competition**
The administrator picks which teams participate.

**D4. Auto-generate teams during setup (wizard)**
Instead of assigning pre-existing teams, the administrator can use a setup wizard:
1. Select which players will participate from the full player list
2. Enter how many players each team should have
3. The system randomly shuffles the players, divides them into teams, and assigns each team a random name
4. Any leftover players (when the number doesn't divide evenly) are distributed one per team

**D5. Edit an existing competition**
Before a competition is started, the administrator can change its name, date, assigned games, game order, or participating teams.

**D6. Delete a competition**
An administrator can remove a competition that was created by mistake or is no longer needed.

**D7. List all competitions**
An administrator can see a table of all competitions and which ones have been started.

---

## Area E — Running a Competition

**E1. Start a competition**
When the administrator opens a competition for the first time, the system automatically generates all the matches.
For every game in the competition, every team is paired against every other team exactly once (or more, depending on the single-match setting).
The competition is then marked as started and cannot return to the setup state.

**E2. Navigate between games**
During the competition the administrator can step forward and backward through the games using a step-by-step navigation bar.
Each step shows the game name so it is easy to know which activity is currently active.

**E3. View matches for the active game**
For the currently selected game, the administrator sees a list of all match-ups (Team A vs Team B).

**E4. Enter results for a match**
The administrator selects a match and enters each player's individual score or time.
For score-based games a higher number is better; for time-based games a lower number is better.

**E5. Update results**
If a result was entered incorrectly, the administrator can open the match again and correct the values.
The leaderboard updates immediately after saving.

---

## Area F — Leaderboards

**F1. View per-game team leaderboard**
For the currently active game, the system shows a ranked table of all teams.
Teams are ranked by their combined score across all their matches in that game.
For score-based games the highest score ranks first.
For time-based games the lowest time ranks first.
The column header changes to show whether the values represent total score, average score, total time, or average time — depending on how the game was configured.

**F2. View per-game player leaderboard**
For the currently active game, the system shows a ranked table of all individual players.
Players are ranked by their personal score or time in that game, following the same better-higher / better-lower logic.

**F3. View total team leaderboard**
A single ranked table shows all teams across all games.
After each game, teams earn placement points: 1st place earns 100 points, 2nd place 90, 3rd place 80, and so on down to 10th place which earns 10 points. Teams placed lower than 10th earn 0 points.
The team with the most accumulated placement points across all games wins the overall competition.

**F4. View total player leaderboard**
The same placement-point system is applied per player across all games.
The player with the most accumulated points across all games is the overall winner.

---

## Parallel work instructions for AI assistants

The areas above are independent and can be implemented simultaneously.
Each area maps to one section of the application with no hard dependencies between them,
except that Area E and Area F require Areas A–D to exist first (players, games, teams,
and competitions must exist before a competition can be run or scored).

**Suggested split:**
- **Assistant 1** → Areas A and B (Player and Game management)
- **Assistant 2** → Areas C and D (Team management and Competition setup)
- **Assistant 3** → Areas E and F (Running a competition and Leaderboards)

Each assistant should deliver a fully working vertical slice of their assigned areas
before the slices are integrated.
