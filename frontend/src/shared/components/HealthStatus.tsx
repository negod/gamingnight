import { useEffect, useState } from 'react';
import { getHealth } from '../../features/items/api/healthApi';

export function HealthStatus() {
  const [status, setStatus] = useState<'checking' | 'ok' | 'down'>('checking');

  useEffect(() => {
    let active = true;

    getHealth()
      .then(() => {
        if (active) {
          setStatus('ok');
        }
      })
      .catch(() => {
        if (active) {
          setStatus('down');
        }
      });

    return () => {
      active = false;
    };
  }, []);

  const styles = {
    checking: 'bg-amber-50 text-amber-800 ring-amber-200',
    ok: 'bg-emerald-50 text-emerald-800 ring-emerald-200',
    down: 'bg-red-50 text-red-800 ring-red-200',
  }[status];

  return (
    <span className={`rounded-md px-3 py-1 text-sm font-medium ring-1 ${styles}`}>
      API {status === 'checking' ? 'checking' : status}
    </span>
  );
}
