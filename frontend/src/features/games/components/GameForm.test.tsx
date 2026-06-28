import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';
import { GameForm } from './GameForm';

describe('GameForm', () => {
  it('submits game details with selected type and calculation method', async () => {
    const user = userEvent.setup();
    const onSubmit = vi.fn().mockResolvedValue(undefined);

    render(<GameForm submitLabel="Create game" onSubmit={onSubmit} />);

    await user.type(screen.getByLabelText(/game name/i), 'Darts');
    await user.click(screen.getByLabelText(/time-based/i));
    await user.click(screen.getByLabelText(/average/i));
    await user.type(screen.getByLabelText(/description/i), 'Lowest average time wins');
    await user.click(screen.getByRole('button', { name: /create game/i }));

    expect(onSubmit).toHaveBeenCalledWith({
      name: 'Darts',
      gameType: 'TIME_BASED',
      calculationMethod: 'AVERAGE',
      description: 'Lowest average time wins',
    });
  });

  it('shows validation error when name is missing', async () => {
    const user = userEvent.setup();
    const onSubmit = vi.fn();

    render(<GameForm submitLabel="Create game" onSubmit={onSubmit} />);

    await user.click(screen.getByRole('button', { name: /create game/i }));

    expect(await screen.findByRole('alert')).toHaveTextContent('Game name is required');
    expect(onSubmit).not.toHaveBeenCalled();
  });
});
