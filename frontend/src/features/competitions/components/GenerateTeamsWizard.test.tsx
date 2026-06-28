import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';
import { GenerateTeamsWizard } from './GenerateTeamsWizard';

describe('GenerateTeamsWizard', () => {
  it('selects players and submits team size', async () => {
    const user = userEvent.setup();
    const onGenerate = vi.fn().mockResolvedValue(undefined);

    render(<GenerateTeamsWizard players={players} onGenerate={onGenerate} />);

    await user.click(screen.getByLabelText('Alice'));
    await user.click(screen.getByLabelText('Bob'));
    await user.clear(screen.getByLabelText(/players per team/i));
    await user.type(screen.getByLabelText(/players per team/i), '2');
    await user.click(screen.getByRole('button', { name: /generate teams/i }));

    expect(onGenerate).toHaveBeenCalledWith(['player-1', 'player-2'], 2);
  });

  it('supports selecting all players and shows leftover summary', async () => {
    const user = userEvent.setup();

    render(<GenerateTeamsWizard players={players} onGenerate={vi.fn().mockResolvedValue(undefined)} />);

    await user.click(screen.getByRole('button', { name: 'All' }));

    expect(screen.getByText(/will create/i)).toHaveTextContent('Will create 1 team');
    expect(screen.getByText(/distributed one per team/i)).toBeInTheDocument();
  });

  it('shows validation error when no player is selected', async () => {
    const user = userEvent.setup();
    const onGenerate = vi.fn();

    render(<GenerateTeamsWizard players={players} onGenerate={onGenerate} />);

    await user.click(screen.getByRole('button', { name: /generate teams/i }));

    expect(await screen.findByRole('alert')).toHaveTextContent('Select at least one player');
    expect(onGenerate).not.toHaveBeenCalled();
  });
});

const players = [
  {
    id: 'player-1',
    name: 'Alice',
    createdAt: '2026-01-01T10:00:00Z',
    updatedAt: '2026-01-01T10:00:00Z',
  },
  {
    id: 'player-2',
    name: 'Bob',
    createdAt: '2026-01-01T10:00:00Z',
    updatedAt: '2026-01-01T10:00:00Z',
  },
  {
    id: 'player-3',
    name: 'Carol',
    createdAt: '2026-01-01T10:00:00Z',
    updatedAt: '2026-01-01T10:00:00Z',
  },
];
