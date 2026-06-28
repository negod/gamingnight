import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';
import { PlayerForm } from './PlayerForm';

describe('PlayerForm', () => {
  it('submits a trimmed player name', async () => {
    const user = userEvent.setup();
    const onSubmit = vi.fn().mockResolvedValue(undefined);

    render(<PlayerForm submitLabel="Create player" onSubmit={onSubmit} />);

    await user.type(screen.getByLabelText(/player name/i), ' Alice ');
    await user.click(screen.getByRole('button', { name: /create player/i }));

    expect(onSubmit).toHaveBeenCalledWith({ name: 'Alice' });
  });

  it('shows validation error when name is missing', async () => {
    const user = userEvent.setup();
    const onSubmit = vi.fn();

    render(<PlayerForm submitLabel="Create player" onSubmit={onSubmit} />);

    await user.click(screen.getByRole('button', { name: /create player/i }));

    expect(await screen.findByRole('alert')).toHaveTextContent('Player name is required');
    expect(onSubmit).not.toHaveBeenCalled();
  });
});
