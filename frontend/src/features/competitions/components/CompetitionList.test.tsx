import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { describe, expect, it, vi } from 'vitest';
import { CompetitionList } from './CompetitionList';

describe('CompetitionList', () => {
  it('renders empty state', () => {
    render(
      <MemoryRouter>
        <CompetitionList competitions={[]} onDelete={vi.fn()} />
      </MemoryRouter>,
    );

    expect(screen.getByText(/no competitions yet/i)).toBeInTheDocument();
  });

  it('shows setup competitions with edit action and started status', () => {
    render(
      <MemoryRouter>
        <CompetitionList competitions={[competition({ started: false })]} onDelete={vi.fn()} />
      </MemoryRouter>,
    );

    expect(screen.getByText('Cup')).toBeInTheDocument();
    expect(screen.getByText('Setup')).toBeInTheDocument();
    expect(screen.getByRole('link', { name: /edit/i })).toHaveAttribute('href', '/competitions/competition-1/edit');
  });

  it('hides edit action for started competitions', () => {
    render(
      <MemoryRouter>
        <CompetitionList competitions={[competition({ started: true })]} onDelete={vi.fn()} />
      </MemoryRouter>,
    );

    expect(screen.getByText('Started')).toBeInTheDocument();
    expect(screen.queryByRole('link', { name: /edit/i })).not.toBeInTheDocument();
  });

  it('calls delete handler', async () => {
    const user = userEvent.setup();
    const onDelete = vi.fn();

    render(
      <MemoryRouter>
        <CompetitionList competitions={[competition({ started: false })]} onDelete={onDelete} />
      </MemoryRouter>,
    );

    await user.click(screen.getByRole('button', { name: /delete/i }));

    expect(onDelete).toHaveBeenCalledWith('competition-1');
  });
});

function competition({ started }: { started: boolean }) {
  return {
    id: 'competition-1',
    name: 'Cup',
    date: '2026-07-01',
    singleMatch: true,
    started,
    gameIds: ['game-1', 'game-2'],
    teamIds: ['team-1'],
    createdAt: '2026-01-01T10:00:00Z',
    updatedAt: '2026-01-01T10:00:00Z',
  };
}
