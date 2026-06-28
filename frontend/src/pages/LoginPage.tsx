import { FormEvent, useState } from 'react';
import { Navigate } from 'react-router-dom';
import { LogIn, UserPlus } from 'lucide-react';
import { login as loginRequest, signup as signupRequest } from '../features/auth/api/authApi';
import { ErrorMessage } from '../shared/components/ErrorMessage';
import { useAuth } from '../shared/auth/AuthContext';

type Mode = 'login' | 'signup';

const MIN_PASSWORD_LENGTH = 8;

export function LoginPage() {
  const auth = useAuth();
  const [mode, setMode] = useState<Mode>('login');

  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  if (auth.user) {
    return <Navigate to={auth.user.role === 'ADMIN' ? '/competitions' : '/users/me'} replace />;
  }

  function switchMode(next: Mode) {
    setMode(next);
    setError(null);
    setUsername('');
    setEmail('');
    setPassword('');
    setConfirmPassword('');
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError(null);

    if (mode === 'signup') {
      if (password.length < MIN_PASSWORD_LENGTH) {
        setError(`Password must be at least ${MIN_PASSWORD_LENGTH} characters`);
        return;
      }
      if (password !== confirmPassword) {
        setError('Passwords do not match');
        return;
      }
    }

    setSubmitting(true);
    try {
      const response = mode === 'login'
        ? await loginRequest({ username, password })
        : await signupRequest({ username, email, password });
      auth.login(response.token, response.user);
    } catch (err) {
      setError(err instanceof Error ? err.message : mode === 'login' ? 'Unable to sign in' : 'Unable to create account');
    } finally {
      setSubmitting(false);
    }
  }

  const isLogin = mode === 'login';

  return (
    <main className="mx-auto flex min-h-screen max-w-md items-center px-4">
      <form onSubmit={handleSubmit} className="w-full space-y-5 rounded-lg border border-slate-200 bg-white p-6">
        <div>
          <h1 className="text-2xl font-semibold text-slate-950">
            {isLogin ? 'Sign in' : 'Create account'}
          </h1>
          <p className="mt-1 text-sm text-slate-600">
            {isLogin ? 'Use your Gaming Night user account.' : 'Join Gaming Night — you will get a User role.'}
          </p>
        </div>

        {error ? <ErrorMessage message={error} /> : null}

        <label className="block">
          <span className="text-sm font-medium text-slate-700">Username</span>
          <input
            autoComplete="username"
            maxLength={120}
            className="mt-1 w-full rounded-md border border-slate-300 px-3 py-2 outline-none focus:border-teal-700 focus:ring-2 focus:ring-teal-100"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            required
          />
        </label>

        {!isLogin && (
          <label className="block">
            <span className="text-sm font-medium text-slate-700">Email</span>
            <input
              type="email"
              autoComplete="email"
              maxLength={320}
              className="mt-1 w-full rounded-md border border-slate-300 px-3 py-2 outline-none focus:border-teal-700 focus:ring-2 focus:ring-teal-100"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
            />
          </label>
        )}

        <label className="block">
          <span className="text-sm font-medium text-slate-700">Password</span>
          <input
            type="password"
            autoComplete={isLogin ? 'current-password' : 'new-password'}
            minLength={isLogin ? undefined : MIN_PASSWORD_LENGTH}
            maxLength={200}
            className="mt-1 w-full rounded-md border border-slate-300 px-3 py-2 outline-none focus:border-teal-700 focus:ring-2 focus:ring-teal-100"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
          />
          {!isLogin && (
            <span className="mt-1 block text-xs text-slate-500">
              At least {MIN_PASSWORD_LENGTH} characters
            </span>
          )}
        </label>

        {!isLogin && (
          <label className="block">
            <span className="text-sm font-medium text-slate-700">Confirm password</span>
            <input
              type="password"
              autoComplete="new-password"
              maxLength={200}
              className="mt-1 w-full rounded-md border border-slate-300 px-3 py-2 outline-none focus:border-teal-700 focus:ring-2 focus:ring-teal-100"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              required
            />
          </label>
        )}

        <button
          type="submit"
          disabled={submitting}
          className="inline-flex w-full items-center justify-center gap-2 rounded-md bg-teal-700 px-4 py-2 text-sm font-semibold text-white hover:bg-teal-800 disabled:cursor-not-allowed disabled:opacity-60"
        >
          {isLogin
            ? <><LogIn aria-hidden="true" className="h-4 w-4" />{submitting ? 'Signing in…' : 'Sign in'}</>
            : <><UserPlus aria-hidden="true" className="h-4 w-4" />{submitting ? 'Creating account…' : 'Create account'}</>
          }
        </button>

        <p className="text-center text-sm text-slate-500">
          {isLogin ? (
            <>No account?{' '}
              <button type="button" onClick={() => switchMode('signup')} className="text-teal-700 hover:underline">
                Create one
              </button>
            </>
          ) : (
            <>Already have an account?{' '}
              <button type="button" onClick={() => switchMode('login')} className="text-teal-700 hover:underline">
                Sign in
              </button>
            </>
          )}
        </p>
      </form>
    </main>
  );
}
