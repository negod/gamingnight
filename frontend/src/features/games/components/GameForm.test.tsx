import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';
import { GameForm } from './GameForm';
import type { GameFormValues } from '../../../shared/types/game';

describe('GameForm', () => {
  it('submits with default rule values when only name is entered', async () => {
    const user = userEvent.setup();
    const onSubmit = vi.fn().mockResolvedValue(undefined);

    render(<GameForm submitLabel="Create game" onSubmit={onSubmit} />);

    await user.type(screen.getByLabelText(/game name/i), 'Darts');
    await user.click(screen.getByRole('button', { name: /create game/i }));

    expect(onSubmit).toHaveBeenCalledOnce();
    const submitted: GameFormValues = onSubmit.mock.calls[0][0];
    expect(submitted.name).toBe('Darts');
    expect(submitted.matchType).toBe('FREE_FOR_ALL');
    expect(submitted.resultType).toBe('SCORE');
    expect(submitted.winnerRule).toBe('HIGHEST_VALUE_WINS');
    expect(submitted.scoringRule).toMatchObject({ type: 'WIN_DRAW_LOSS', pointsForWin: 3, pointsForDraw: 1, pointsForLoss: 0 });
    expect(submitted.isActive).toBe(true);
  });

  it('shows validation error when name is missing', async () => {
    const user = userEvent.setup();
    const onSubmit = vi.fn();

    render(<GameForm submitLabel="Create game" onSubmit={onSubmit} />);

    await user.click(screen.getByRole('button', { name: /create game/i }));

    expect(await screen.findByRole('alert')).toHaveTextContent('Game name is required');
    expect(onSubmit).not.toHaveBeenCalled();
  });

  it('resets scoring sub-fields when scoring type changes', async () => {
    const user = userEvent.setup();
    const onSubmit = vi.fn().mockResolvedValue(undefined);

    render(<GameForm submitLabel="Create game" onSubmit={onSubmit} />);

    await user.type(screen.getByLabelText(/game name/i), 'Bowling');
    await user.selectOptions(screen.getByRole('combobox', { name: /scoring type/i }), 'WINNER_TAKES_ALL');

    expect(screen.getByLabelText(/points to winner/i)).toBeInTheDocument();
    expect(screen.queryByLabelText(/win points/i)).not.toBeInTheDocument();
  });

  it('shows validation rule fields when enabled', async () => {
    const user = userEvent.setup();

    render(<GameForm submitLabel="Create game" onSubmit={vi.fn()} />);

    expect(screen.queryByLabelText(/min value/i)).not.toBeInTheDocument();

    await user.click(screen.getByLabelText(/enable result validation/i));

    expect(screen.getByLabelText(/min value/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/max value/i)).toBeInTheDocument();
  });

  it('pre-populates all fields from initialValues', () => {
    const initial: GameFormValues = {
      name: 'Mario Kart',
      description: 'Racing game',
      platform: 'Wii',
      genre: 'Racing',
      isActive: false,
      matchType: 'PLAYER_VS_PLAYER',
      participantRule: { minPlayersPerTeam: 1, maxPlayersPerTeam: 1, numberOfTeams: 2, allowSubstitutes: false },
      resultType: 'PLACEMENT',
      winnerRule: 'LOWEST_VALUE_WINS',
      scoringRule: { type: 'PLACEMENT', pointsByPlacement: { 1: 10, 2: 7, 3: 5 } },
      tieBreakerRule: 'EXTRA_ROUND',
      validationRule: null,
      rotationRule: 'NONE',
      timeLimitRule: null,
      bonusRules: [],
    };

    render(<GameForm submitLabel="Save" onSubmit={vi.fn()} initialValues={initial} />);

    expect(screen.getByDisplayValue('Mario Kart')).toBeInTheDocument();
    expect(screen.getByDisplayValue('Wii')).toBeInTheDocument();
  });
});
