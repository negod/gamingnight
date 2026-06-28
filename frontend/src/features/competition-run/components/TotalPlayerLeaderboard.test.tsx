import { render, screen } from '@testing-library/react';
import { expect, test } from 'vitest';
import { TotalPlayerLeaderboard } from './TotalPlayerLeaderboard';
import type { TotalPlayerLeaderboardRow } from '../../../shared/types/leaderboard';

test('renders players with points', () => {
  const rows: TotalPlayerLeaderboardRow[] = [
    { rank: 1, playerId: 'a', playerName: 'Alice', points: 190 },
    { rank: 2, playerId: 'b', playerName: 'Bob', points: 180 },
  ];

  render(<TotalPlayerLeaderboard rows={rows} />);

  expect(screen.getByText('Alice')).toBeInTheDocument();
  expect(screen.getByText('Bob')).toBeInTheDocument();
  expect(screen.getByText('190')).toBeInTheDocument();
  expect(screen.getByText('180')).toBeInTheDocument();
});

test('shows no results message when rows are empty', () => {
  render(<TotalPlayerLeaderboard rows={[]} />);

  expect(screen.getByText(/no results yet/i)).toBeInTheDocument();
});

test('renders Points column header', () => {
  const rows: TotalPlayerLeaderboardRow[] = [
    { rank: 1, playerId: 'a', playerName: 'Alice', points: 100 },
  ];

  render(<TotalPlayerLeaderboard rows={rows} />);

  expect(screen.getByText(/points/i)).toBeInTheDocument();
});

test('first place row is bold', () => {
  const rows: TotalPlayerLeaderboardRow[] = [
    { rank: 1, playerId: 'a', playerName: 'Alice', points: 100 },
    { rank: 2, playerId: 'b', playerName: 'Bob', points: 90 },
  ];

  render(<TotalPlayerLeaderboard rows={rows} />);

  const aliceRow = screen.getByText('Alice').closest('tr');
  expect(aliceRow).toHaveClass('font-semibold');
  const bobRow = screen.getByText('Bob').closest('tr');
  expect(bobRow).not.toHaveClass('font-semibold');
});
