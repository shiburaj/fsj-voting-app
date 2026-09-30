import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { Modal } from './ui/Modal';
import { Input } from './ui/Input';
import { Button } from './ui/Button';
import { AlertCircle } from 'lucide-react';

export function AuthModal({ isOpen, onClose, initialMode = 'login' }) {
  const { login, register } = useAuth();
  const [mode, setMode] = useState(initialMode); // 'login' | 'register'

  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  // Sync mode if initialMode prop changes
  React.useEffect(() => {
    setMode(initialMode);
    setError('');
  }, [initialMode, isOpen]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      if (mode === 'login') {
        if (!email.trim() || !password.trim()) {
          setError('Please provide both email and password.');
          setLoading(false);
          return;
        }
        await login(email.trim(), password.trim());
      } else {
        if (!name.trim() || !email.trim() || !password.trim()) {
          setError('All fields are required.');
          setLoading(false);
          return;
        }
        if (password.length < 4) {
          setError('Password must be at least 4 characters.');
          setLoading(false);
          return;
        }
        await register(name.trim(), email.trim(), password.trim());
      }
      onClose();
    } catch (err) {
      setError(err.message || 'Authentication failed. Please check your credentials.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title={mode === 'login' ? 'Sign In to Your Account' : 'Create an Account'}
      description={
        mode === 'login'
          ? 'Enter your credentials to manage or create voting sessions.'
          : 'Register once to participate and host voting sessions.'
      }
    >
      <div className="flex border-b border-slate-200 mb-5">
        <button
          type="button"
          onClick={() => { setMode('login'); setError(''); }}
          className={`pb-2 text-sm font-semibold flex-1 border-b-2 cursor-pointer transition-colors ${
            mode === 'login'
              ? 'border-indigo-600 text-indigo-600'
              : 'border-transparent text-slate-500 hover:text-slate-800'
          }`}
        >
          Sign In
        </button>
        <button
          type="button"
          onClick={() => { setMode('register'); setError(''); }}
          className={`pb-2 text-sm font-semibold flex-1 border-b-2 cursor-pointer transition-colors ${
            mode === 'register'
              ? 'border-indigo-600 text-indigo-600'
              : 'border-transparent text-slate-500 hover:text-slate-800'
          }`}
        >
          Register
        </button>
      </div>

      {error && (
        <div className="p-3 mb-4 rounded-lg bg-red-50 border border-red-200 text-red-700 text-xs flex items-start gap-2">
          <AlertCircle className="w-4 h-4 shrink-0 mt-0.5" />
          <span>{error}</span>
        </div>
      )}

      <form onSubmit={handleSubmit} className="space-y-4">
        {mode === 'register' && (
          <Input
            id="auth-name"
            label="Full Name"
            placeholder="John Doe"
            value={name}
            onChange={(e) => setName(e.target.value)}
            required
          />
        )}

        <Input
          id="auth-email"
          label="Email Address"
          type="email"
          placeholder="user@example.com"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          required
        />

        <Input
          id="auth-password"
          label="Password"
          type="password"
          placeholder="••••••••"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          required
        />

        <div className="pt-2">
          <Button
            type="submit"
            className="w-full"
            loading={loading}
          >
            {mode === 'login' ? 'Sign In' : 'Create Account'}
          </Button>
        </div>
      </form>
    </Modal>
  );
}
