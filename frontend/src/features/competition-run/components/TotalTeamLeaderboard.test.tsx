import { render, screen } from '@testing-library/react';
import { expect, test } from 'vitest';
import { TotalTeamLeaderboard } from './TotalTeamLeaderboard';
import type { TotalTeamLeaderboardRow } from '../../../shared/types/leaderboard';

test('renders teams with points', () => {
  const rows: TotalTeamLeaderboardRow[] = [
    { rank: 1, teamId: 'a', teamName: 'Alpha', points: 100 },
    { rank: 2, teamId: 'b', teamName: 'Beta', points: 90 },
  ];

  render(<TotalTeamLeaderboard rows={rows} />);

  expect(screen.getByText('Alpha')).toBeInTheDocument();
  expect(screen.getByText('Beta')).toBeInTheDocument();
  expect(screen.getByText('100')).toBeInTheDocument();
  expect(screen.getByText('90')).toBeInTheDocument();
});

test('shows no results message when rows are empty', () => {
  render(<TotalTeamLeaderboard rows={[]} />);

  expect(screen.getByText(/no results yet/i)).toBeInTheDocument();
});

test('renders Points column header', () => {
  const rows: TotalTeamLeaderboardRow[] = [
    { rank: 1, teamId: 'a', teamName: 'Alpha', points: 100 },
  ];

  render(<TotalTeamLeaderboard rows={rows} />);

  expect(screen.getByText(/points/i)).toBeInTheDocument();
});

test('first place row is bold', () => {
  const rows: TotalTeamLeaderboardRow[] = [
    { rank: 1, teamId: 'a', teamName: 'Winners', points: 100 },
    { rank: 2, teamId: 'b', teamName: 'Runners', points: 90 },
  ];

  render(<TotalTeamLeaderboard rows={rows} />);

  const winnerRow = screen.getByText('Winners').closest('tr');
  expect(winnerRow).toHaveClass('font-semibold');
  const runnerRow = screen.getByText('Runners').closest('tr');
  expect(runnerRow).not.toHaveClass('font-semibold');
});
