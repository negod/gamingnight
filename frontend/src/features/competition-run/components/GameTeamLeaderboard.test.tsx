import { render, screen } from '@testing-library/react';
import { expect, test } from 'vitest';
import { GameTeamLeaderboard } from './GameTeamLeaderboard';
import type { GameTeamLeaderboard as GameTeamLeaderboardType } from '../../../shared/types/leaderboard';

test('renders column header and team rows', () => {
  const leaderboard: GameTeamLeaderboardType = {
    columnHeader: 'Total Score',
    rows: [
      { rank: 1, teamId: 'a', teamName: 'Alpha', value: 500 },
      { rank: 2, teamId: 'b', teamName: 'Beta', value: 300 },
    ],
  };

  render(<GameTeamLeaderboard leaderboard={leaderboard} />);

  expect(screen.getByText('Total Score')).toBeInTheDocument();
  expect(screen.getByText('Alpha')).toBeInTheDocument();
  expect(screen.getByText('Beta')).toBeInTheDocument();
  expect(screen.getByText('500')).toBeInTheDocument();
  expect(screen.getByText('300')).toBeInTheDocument();
});

test('shows no results message when rows are empty', () => {
  const leaderboard: GameTeamLeaderboardType = { columnHeader: 'Total Score', rows: [] };

  render(<GameTeamLeaderboard leaderboard={leaderboard} />);

  expect(screen.getByText(/no results yet/i)).toBeInTheDocument();
});

test('displays rank numbers', () => {
  const leaderboard: GameTeamLeaderboardType = {
    columnHeader: 'Total Time',
    rows: [
      { rank: 1, teamId: 'a', teamName: 'Fast', value: 10.5 },
      { rank: 2, teamId: 'b', teamName: 'Slow', value: 20.0 },
    ],
  };

  render(<GameTeamLeaderboard leaderboard={leaderboard} />);

  const cells = screen.getAllByRole('cell');
  expect(cells[0]).toHaveTextContent('1');
  expect(cells[3]).toHaveTextContent('2');
});

test('formats decimal values with at most three places', () => {
  const leaderboard: GameTeamLeaderboardType = {
    columnHeader: 'Total Score',
    rows: [
      { rank: 1, teamId: 'a', teamName: 'Alpha', value: 0.30000000000000004 },
      { rank: 2, teamId: 'b', teamName: 'Beta', value: 0.1234567 },
    ],
  };

  render(<GameTeamLeaderboard leaderboard={leaderboard} />);

  expect(screen.getByText('0.3')).toBeInTheDocument();
  expect(screen.getByText('0.123')).toBeInTheDocument();
});
