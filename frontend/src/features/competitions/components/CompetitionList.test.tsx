import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { describe, expect, it, vi } from 'vitest';
import { CompetitionList } from './CompetitionList';

describe('CompetitionList', () => {
  it('renders empty state', () => {
    render(
      <MemoryRouter>
        <CompetitionList competitions={[]} onRun={vi.fn()} onDelete={vi.fn()} />
      </MemoryRouter>,
    );

    expect(screen.getByText(/no competitions yet/i)).toBeInTheDocument();
  });

  it('shows setup competitions with edit action and setup status', async () => {
    const user = userEvent.setup();
    const onEdit = vi.fn();
    render(
      <MemoryRouter>
        <CompetitionList competitions={[competition({ started: false })]} onEdit={onEdit} onRun={vi.fn()} onDelete={vi.fn()} />
      </MemoryRouter>,
    );

    expect(screen.getByText('Cup')).toBeInTheDocument();
    expect(screen.getByText('Setup')).toBeInTheDocument();
    await user.click(screen.getByRole('button', { name: /edit/i }));
    expect(onEdit).toHaveBeenCalledWith(expect.objectContaining({ id: 'competition-1' }));
  });

  it('shows open setup competitions', () => {
    render(
      <MemoryRouter>
        <CompetitionList competitions={[competition({ started: false, registrationOpen: true })]} onRun={vi.fn()} onDelete={vi.fn()} />
      </MemoryRouter>,
    );

    expect(screen.getByText('Open')).toBeInTheDocument();
  });

  it('hides edit action for started competitions', () => {
    render(
      <MemoryRouter>
        <CompetitionList competitions={[competition({ started: true })]} onRun={vi.fn()} onDelete={vi.fn()} />
      </MemoryRouter>,
    );

    expect(screen.getByText('Started')).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: /edit/i })).not.toBeInTheDocument();
  });

  it('calls delete handler', async () => {
    const user = userEvent.setup();
    const onDelete = vi.fn();

    render(
      <MemoryRouter>
        <CompetitionList competitions={[competition({ started: false })]} onRun={vi.fn()} onDelete={onDelete} />
      </MemoryRouter>,
    );

    await user.click(screen.getByRole('button', { name: /delete/i }));

    expect(onDelete).toHaveBeenCalledWith('competition-1');
  });

  it('lets regular users register for setup competitions', async () => {
    const user = userEvent.setup();
    const onRegister = vi.fn();

    render(
      <MemoryRouter>
        <CompetitionList
          competitions={[competition({ started: false, registrationOpen: true })]}
          onRun={vi.fn()}
          onDelete={vi.fn()}
          onRegister={onRegister}
          canManage={false}
          currentPlayerId="player-1"
        />
      </MemoryRouter>,
    );

    await user.click(screen.getByRole('button', { name: /register/i }));

    expect(onRegister).toHaveBeenCalledWith('competition-1');
  });

  it('lets regular users unregister from setup competitions', async () => {
    const user = userEvent.setup();
    const onUnregister = vi.fn();

    render(
      <MemoryRouter>
        <CompetitionList
          competitions={[competition({ started: false, registrationOpen: true, registeredPlayerIds: ['player-1'] })]}
          onRun={vi.fn()}
          onDelete={vi.fn()}
          onUnregister={onUnregister}
          canManage={false}
          currentPlayerId="player-1"
        />
      </MemoryRouter>,
    );

    await user.click(screen.getByRole('button', { name: /registered/i }));

    expect(onUnregister).toHaveBeenCalledWith('competition-1');
  });

  it('does not show registration action for closed setup competitions', () => {
    render(
      <MemoryRouter>
        <CompetitionList
          competitions={[competition({ started: false, registrationOpen: false })]}
          onRun={vi.fn()}
          onDelete={vi.fn()}
          onRegister={vi.fn()}
          canManage={false}
          currentPlayerId="player-1"
        />
      </MemoryRouter>,
    );

    expect(screen.queryByRole('button', { name: /register/i })).not.toBeInTheDocument();
  });
});

function competition({
  started,
  registrationOpen = false,
  registeredPlayerIds = [],
}: {
  started: boolean;
  registrationOpen?: boolean;
  registeredPlayerIds?: string[];
}) {
  return {
    id: 'competition-1',
    name: 'Cup',
    date: '2026-07-01',
    singleMatch: true,
    registrationOpen,
    started,
    gameIds: ['game-1', 'game-2'],
    teamIds: ['team-1'],
    registeredPlayerIds,
    createdAt: '2026-01-01T10:00:00Z',
    updatedAt: '2026-01-01T10:00:00Z',
  };
}
