import { render, screen } from '@testing-library/react';
import { expect, test } from 'vitest';
import { GamePlayerLeaderboard } from './GamePlayerLeaderboard';
import type { GamePlayerLeaderboard as GamePlayerLeaderboardType } from '../../../shared/types/leaderboard';

test('renders column header and player rows', () => {
  const leaderboard: GamePlayerLeaderboardType = {
    columnHeader: 'Total Score',
    rows: [
      { rank: 1, playerId: 'a', playerName: 'Alice', value: 300 },
      { rank: 2, playerId: 'b', playerName: 'Bob', value: 200 },
    ],
  };

  render(<GamePlayerLeaderboard leaderboard={leaderboard} />);

  expect(screen.getByText('Total Score')).toBeInTheDocument();
  expect(screen.getByText('Alice')).toBeInTheDocument();
  expect(screen.getByText('Bob')).toBeInTheDocument();
  expect(screen.getByText('300')).toBeInTheDocument();
  expect(screen.getByText('200')).toBeInTheDocument();
});

test('shows no results message when rows are empty', () => {
  const leaderboard: GamePlayerLeaderboardType = { columnHeader: 'Average Time', rows: [] };

  render(<GamePlayerLeaderboard leaderboard={leaderboard} />);

  expect(screen.getByText(/no results yet/i)).toBeInTheDocument();
});

test('displays decimal values with at most three places', () => {
  const leaderboard: GamePlayerLeaderboardType = {
    columnHeader: 'Average Time',
    rows: [
      { rank: 1, playerId: 'a', playerName: 'Alice', value: 12.5 },
      { rank: 2, playerId: 'b', playerName: 'Bob', value: 12.34567 },
    ],
  };

  render(<GamePlayerLeaderboard leaderboard={leaderboard} />);

  expect(screen.getByText('12.5')).toBeInTheDocument();
  expect(screen.getByText('12.346')).toBeInTheDocument();
});

test('displays rank numbers', () => {
  const leaderboard: GamePlayerLeaderboardType = {
    columnHeader: 'Total Score',
    rows: [
      { rank: 1, playerId: 'a', playerName: 'Alice', value: 100 },
      { rank: 2, playerId: 'b', playerName: 'Bob', value: 50 },
    ],
  };

  render(<GamePlayerLeaderboard leaderboard={leaderboard} />);

  const cells = screen.getAllByRole('cell');
  expect(cells[0]).toHaveTextContent('1');
  expect(cells[3]).toHaveTextContent('2');
});
