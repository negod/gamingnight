import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';
import { ItemForm } from './ItemForm';

describe('ItemForm', () => {
  it('submits valid values', async () => {
    const user = userEvent.setup();
    const onSubmit = vi.fn().mockResolvedValue(undefined);

    render(<ItemForm submitLabel="Create item" onSubmit={onSubmit} />);

    await user.type(screen.getByLabelText(/title/i), 'Desk');
    await user.type(screen.getByLabelText(/description/i), 'Standing desk');
    await user.click(screen.getByRole('button', { name: /create item/i }));

    expect(onSubmit).toHaveBeenCalledWith({ title: 'Desk', description: 'Standing desk' });
  });

  it('shows validation error when title is missing', async () => {
    const user = userEvent.setup();
    const onSubmit = vi.fn();

    render(<ItemForm submitLabel="Create item" onSubmit={onSubmit} />);

    await user.click(screen.getByRole('button', { name: /create item/i }));

    expect(await screen.findByRole('alert')).toHaveTextContent('Title is required');
    expect(onSubmit).not.toHaveBeenCalled();
  });
});
