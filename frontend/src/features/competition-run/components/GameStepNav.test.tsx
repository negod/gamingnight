import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { expect, test, vi } from 'vitest';
import { GameStepNav } from './GameStepNav';
import type { Game } from '../../../shared/types/game';

const games: Game[] = [
  game('1', 'Bowling'),
  game('2', 'Darts'),
  game('3', 'Run'),
];

test('renders all game names', () => {
  render(<GameStepNav games={games} activeIndex={0} onSelect={vi.fn()} />);

  expect(screen.getByRole('button', { name: 'Bowling' })).toBeInTheDocument();
  expect(screen.getByRole('button', { name: 'Darts' })).toBeInTheDocument();
  expect(screen.getByRole('button', { name: 'Run' })).toBeInTheDocument();
});

test('previous button is disabled on first game', () => {
  render(<GameStepNav games={games} activeIndex={0} onSelect={vi.fn()} />);

  expect(screen.getByRole('button', { name: /previous game/i })).toBeDisabled();
  expect(screen.getByRole('button', { name: /next game/i })).toBeEnabled();
});

test('next button is disabled on last game', () => {
  render(<GameStepNav games={games} activeIndex={2} onSelect={vi.fn()} />);

  expect(screen.getByRole('button', { name: /next game/i })).toBeDisabled();
  expect(screen.getByRole('button', { name: /previous game/i })).toBeEnabled();
});

test('calls onSelect with next index when next is clicked', async () => {
  const onSelect = vi.fn();
  render(<GameStepNav games={games} activeIndex={0} onSelect={onSelect} />);

  await userEvent.click(screen.getByRole('button', { name: /next game/i }));

  expect(onSelect).toHaveBeenCalledWith(1);
});

test('calls onSelect with previous index when previous is clicked', async () => {
  const onSelect = vi.fn();
  render(<GameStepNav games={games} activeIndex={1} onSelect={onSelect} />);

  await userEvent.click(screen.getByRole('button', { name: /previous game/i }));

  expect(onSelect).toHaveBeenCalledWith(0);
});

test('calls onSelect with game index when game button is clicked', async () => {
  const onSelect = vi.fn();
  render(<GameStepNav games={games} activeIndex={0} onSelect={onSelect} />);

  await userEvent.click(screen.getByRole('button', { name: 'Darts' }));

  expect(onSelect).toHaveBeenCalledWith(1);
});

function game(id: string, name: string): Game {
  return {
    id,
    name,
    description: '',
    platform: null,
    genre: null,
    referenceUrl: null,
    isActive: true,
    matchType: 'FREE_FOR_ALL',
    participantRule: { minPlayersPerTeam: 1, maxPlayersPerTeam: 4, numberOfTeams: null, allowSubstitutes: false },
    resultType: 'SCORE',
    winnerRule: 'HIGHEST_VALUE_WINS',
    scoringRule: { type: 'WIN_DRAW_LOSS', pointsForWin: 3, pointsForDraw: 1, pointsForLoss: 0 },
    tieBreakerRule: 'ALLOW_DRAW',
    validationRule: null,
    rotationRule: 'NONE',
    timeLimitRule: null,
    bonusRules: [],
    createdAt: '2026-01-01T10:00:00Z',
    updatedAt: '2026-01-01T10:00:00Z',
  };
}
