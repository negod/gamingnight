import { Save } from 'lucide-react';
import { FormEvent, useState } from 'react';
import type { ItemFormValues } from '../../../shared/types/item';
import { ErrorMessage } from '../../../shared/components/ErrorMessage';

type ItemFormProps = {
  initialValues?: ItemFormValues;
  submitLabel: string;
  onSubmit: (values: ItemFormValues) => Promise<void>;
};

export function ItemForm({ initialValues, submitLabel, onSubmit }: ItemFormProps) {
  const [values, setValues] = useState<ItemFormValues>(initialValues ?? { title: '', description: '' });
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError(null);

    if (!values.title.trim()) {
      setError('Title is required');
      return;
    }

    setSubmitting(true);
    try {
      await onSubmit(values);
    } catch (error) {
      setError(error instanceof Error ? error.message : 'Unable to save item');
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <form onSubmit={handleSubmit} noValidate className="space-y-5">
      {error ? <ErrorMessage message={error} /> : null}

      <label className="block">
        <span className="text-sm font-medium text-slate-700">Title</span>
        <input
          className="mt-1 w-full rounded-md border border-slate-300 px-3 py-2 outline-none focus:border-teal-700 focus:ring-2 focus:ring-teal-100"
          maxLength={120}
          value={values.title}
          onChange={(event) => setValues({ ...values, title: event.target.value })}
          required
        />
      </label>

      <label className="block">
        <span className="text-sm font-medium text-slate-700">Description</span>
        <textarea
          className="mt-1 min-h-36 w-full rounded-md border border-slate-300 px-3 py-2 outline-none focus:border-teal-700 focus:ring-2 focus:ring-teal-100"
          maxLength={1000}
          value={values.description}
          onChange={(event) => setValues({ ...values, description: event.target.value })}
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
