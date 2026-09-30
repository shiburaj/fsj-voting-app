import React from 'react';

export function Input({
  label,
  error,
  id,
  className = '',
  type = 'text',
  ...props
}) {
  return (
    <div className="w-full space-y-1.5 text-left">
      {label && (
        <label htmlFor={id} className="block text-xs font-semibold text-slate-700 uppercase tracking-wider">
          {label}
        </label>
      )}
      <input
        id={id}
        type={type}
        className={`w-full px-3.5 py-2 text-sm bg-white border rounded-lg shadow-xs text-slate-900 placeholder:text-slate-400 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 transition-colors disabled:bg-slate-50 disabled:text-slate-400 ${
          error ? 'border-red-300 focus:ring-red-500 focus:border-red-500' : 'border-slate-300'
        } ${className}`}
        {...props}
      />
      {error && <p className="text-xs text-red-500 font-medium">{error}</p>}
    </div>
  );
}
