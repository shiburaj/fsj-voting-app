import React from 'react';
import { useAuth } from '../context/AuthContext';
import { Button } from './ui/Button';
import { Vote, LogOut, User, PlusCircle, CheckCircle2 } from 'lucide-react';

export function Navbar({ activeTab, setActiveTab, onOpenAuthModal }) {
  const { user, logout, isAuthenticated } = useAuth();

  return (
    <header className="sticky top-0 z-40 bg-white/90 backdrop-blur-md border-b border-slate-200">
      <div className="max-w-6xl mx-auto px-4 sm:px-6 h-16 flex items-center justify-between">
        
        {/* Brand */}
        <div 
          onClick={() => setActiveTab('vote')}
          className="flex items-center gap-2.5 cursor-pointer group"
        >
          <div className="w-10 h-10 rounded-xl bg-indigo-600 flex items-center justify-center text-white shadow-md shadow-indigo-100 group-hover:bg-indigo-700 transition-colors">
            <Vote className="w-5 h-5" />
          </div>
          <div>
            <span className="font-bold text-lg text-slate-900 tracking-tight">VoteSphere</span>
            <span className="hidden sm:inline-block ml-2 text-xs font-semibold px-2 py-0.5 bg-indigo-50 text-indigo-700 rounded-full border border-indigo-100">
              FSJ
            </span>
          </div>
        </div>

        {/* Navigation Tabs */}
        <nav className="flex items-center gap-1 sm:gap-2">
          <button
            onClick={() => setActiveTab('vote')}
            className={`px-3.5 py-1.5 rounded-lg text-sm font-medium transition-colors cursor-pointer flex items-center gap-1.5 ${
              activeTab === 'vote'
                ? 'bg-indigo-50 text-indigo-700 font-semibold'
                : 'text-slate-600 hover:text-slate-900 hover:bg-slate-100'
            }`}
          >
            <CheckCircle2 className="w-4 h-4" />
            <span>Cast Vote</span>
          </button>

          <button
            onClick={() => {
              if (!isAuthenticated) {
                onOpenAuthModal('login');
              } else {
                setActiveTab('host');
              }
            }}
            className={`px-3.5 py-1.5 rounded-lg text-sm font-medium transition-colors cursor-pointer flex items-center gap-1.5 ${
              activeTab === 'host'
                ? 'bg-indigo-50 text-indigo-700 font-semibold'
                : 'text-slate-600 hover:text-slate-900 hover:bg-slate-100'
            }`}
          >
            <PlusCircle className="w-4 h-4" />
            <span>Host Sessions</span>
          </button>
        </nav>

        {/* Auth / Profile Area */}
        <div className="flex items-center gap-3">
          {isAuthenticated ? (
            <div className="flex items-center gap-2">
              <div className="hidden sm:flex items-center gap-2 px-3 py-1.5 bg-slate-100 rounded-lg text-xs font-medium text-slate-700">
                <User className="w-3.5 h-3.5 text-slate-500" />
                <span>{user.name}</span>
              </div>
              <Button
                variant="outline"
                size="sm"
                onClick={logout}
                className="text-slate-600 hover:text-red-600 hover:border-red-200"
              >
                <LogOut className="w-4 h-4" />
                <span className="hidden sm:inline">Logout</span>
              </Button>
            </div>
          ) : (
            <Button
              size="sm"
              onClick={() => onOpenAuthModal('login')}
            >
              Sign In
            </Button>
          )}
        </div>
      </div>
    </header>
  );
}
