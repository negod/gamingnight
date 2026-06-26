import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { vi } from 'vitest';
import { GameStepNav } from './GameStepNav';
import type { Game } from '../../../shared/types/game';

const games: Game[] = [
  { id: '1', name: 'Bowling', gameType: 'SCORE_BASED', calculationMethod: 'SUM', description: '' },
  { id: '2', name: 'Darts', gameType: 'SCORE_BASED', calculationMethod: 'SUM', description: '' },
  { id: '3', name: 'Run', gameType: 'TIME_BASED', calculationMethod: 'AVERAGE', description: '' },
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
