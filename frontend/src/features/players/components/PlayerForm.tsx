import { FormEvent, useState } from 'react';
import { Save } from 'lucide-react';
import { ErrorMessage } from '../../../shared/components/ErrorMessage';
import type { PlayerFormValues } from '../../../shared/types/player';

type PlayerFormProps = {
  initialValues?: PlayerFormValues;
  submitLabel: string;
  onSubmit: (values: PlayerFormValues) => Promise<void>;
};

export function PlayerForm({ initialValues, submitLabel, onSubmit }: PlayerFormProps) {
  const [values, setValues] = useState<PlayerFormValues>(initialValues ?? { name: '' });
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError(null);

    if (!values.name.trim()) {
      setError('Player name is required');
      return;
    }

    setSubmitting(true);
    try {
      await onSubmit({ name: values.name.trim() });
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Unable to save player');
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <form onSubmit={handleSubmit} noValidate className="space-y-6">
      {error ? <ErrorMessage message={error} /> : null}

      <label className="block">
        <span className="text-sm font-medium text-slate-700">Player name</span>
        <input
          className="mt-1 w-full rounded-md border border-slate-300 px-3 py-2 outline-none focus:border-teal-700 focus:ring-2 focus:ring-teal-100"
          maxLength={120}
          value={values.name}
          onChange={(e) => setValues({ name: e.target.value })}
          required
        />
      </label>

      <button
        type="submit"
        disabled={submitting}
        className="inline-flex items-center gap-2 rounded-md bg-teal-700 px-4 py-2 text-sm font-semibold text-white hover:bg-teal-800 disabled:cursor-not-allowed disabled:opacity-60"
      >
        <Save aria-hidden="true" className="h-4 w-4" />
        {submitting ? 'Saving...' : submitLabel}
      </button>
    </form>
  );
}
