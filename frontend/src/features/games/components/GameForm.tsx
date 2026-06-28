import { Save } from 'lucide-react';
import { FormEvent, useState } from 'react';
import type { GameFormValues } from '../../../shared/types/game';
import { ErrorMessage } from '../../../shared/components/ErrorMessage';

type GameFormProps = {
  initialValues?: GameFormValues;
  submitLabel: string;
  onSubmit: (values: GameFormValues) => Promise<void>;
};

const defaultValues: GameFormValues = {
  name: '',
  gameType: 'SCORE_BASED',
  calculationMethod: 'SUM',
  description: '',
};

export function GameForm({ initialValues, submitLabel, onSubmit }: GameFormProps) {
  const [values, setValues] = useState<GameFormValues>(initialValues ?? defaultValues);
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError(null);

    if (!values.name.trim()) {
      setError('Game name is required');
      return;
    }

    setSubmitting(true);
    try {
      await onSubmit(values);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Unable to save game');
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <form onSubmit={handleSubmit} noValidate className="space-y-6">
      {error ? <ErrorMessage message={error} /> : null}

      <label className="block">
        <span className="text-sm font-medium text-slate-700">Game name</span>
        <input
          className="mt-1 w-full rounded-md border border-slate-300 px-3 py-2 outline-none focus:border-teal-700 focus:ring-2 focus:ring-teal-100"
          maxLength={120}
          value={values.name}
          onChange={(e) => setValues({ ...values, name: e.target.value })}
          required
        />
      </label>

      <fieldset>
        <legend className="text-sm font-medium text-slate-700">Game type</legend>
        <div className="mt-2 flex flex-wrap gap-3">
          {(['SCORE_BASED', 'TIME_BASED'] as const).map((type) => (
            <label
              key={type}
              className="flex cursor-pointer items-center gap-2 rounded-md border border-slate-200 px-3 py-2 hover:bg-slate-50"
            >
              <input
                type="radio"
                name="gameType"
                value={type}
                checked={values.gameType === type}
                onChange={() => setValues({ ...values, gameType: type })}
                className="h-4 w-4 accent-teal-700"
              />
              <span className="text-sm text-slate-800">
                {type === 'SCORE_BASED' ? 'Score-based (higher is better)' : 'Time-based (lower is better)'}
              </span>
            </label>
          ))}
        </div>
      </fieldset>

      <fieldset>
        <legend className="text-sm font-medium text-slate-700">Calculation method</legend>
        <div className="mt-2 flex flex-wrap gap-3">
          {(['SUM', 'AVERAGE'] as const).map((method) => (
            <label
              key={method}
              className="flex cursor-pointer items-center gap-2 rounded-md border border-slate-200 px-3 py-2 hover:bg-slate-50"
            >
              <input
                type="radio"
                name="calculationMethod"
                value={method}
                checked={values.calculationMethod === method}
                onChange={() => setValues({ ...values, calculationMethod: method })}
                className="h-4 w-4 accent-teal-700"
              />
              <span className="text-sm text-slate-800">
                {method === 'SUM' ? 'Sum (add all scores)' : 'Average (divide by player count)'}
              </span>
            </label>
          ))}
        </div>
      </fieldset>

      <label className="block">
        <span className="text-sm font-medium text-slate-700">
          Description{' '}
          <span className="font-normal text-slate-500">(optional)</span>
        </span>
        <textarea
          className="mt-1 w-full rounded-md border border-slate-300 px-3 py-2 outline-none focus:border-teal-700 focus:ring-2 focus:ring-teal-100"
          rows={3}
          value={values.description}
          onChange={(e) => setValues({ ...values, description: e.target.value })}
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
