import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { describe, expect, it } from 'vitest';
import { ItemList } from './ItemList';

describe('ItemList', () => {
  it('renders empty state', () => {
    render(
      <MemoryRouter>
        <ItemList items={[]} />
      </MemoryRouter>,
    );

    expect(screen.getByText(/no items yet/i)).toBeInTheDocument();
  });

  it('renders item links', () => {
    render(
      <MemoryRouter>
        <ItemList
          items={[
            {
              id: 'item-1',
              title: 'Desk',
              description: 'Standing desk',
              createdAt: '2026-01-01T10:00:00Z',
              updatedAt: '2026-01-01T10:00:00Z',
            },
          ]}
        />
      </MemoryRouter>,
    );

    expect(screen.getByRole('link', { name: /desk/i })).toHaveAttribute('href', '/items/item-1');
  });
});
