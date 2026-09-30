import React, { useState } from 'react';
import { AuthProvider, useAuth } from './context/AuthContext';
import { Navbar } from './components/Navbar';
import { AuthModal } from './components/AuthModal';
import { HostDashboard } from './components/HostDashboard';
import { VoterView } from './components/VoterView';
import { ResultsView } from './components/ResultsView';

function MainContent() {
  const { isAuthenticated } = useAuth();
  const [activeTab, setActiveTab] = useState('vote'); // 'vote' | 'host' | 'results'
  const [selectedSessionId, setSelectedSessionId] = useState(null);
  const [authModalOpen, setAuthModalOpen] = useState(false);
  const [authModalMode, setAuthModalMode] = useState('login');

  const handleOpenAuthModal = (mode = 'login') => {
    setAuthModalMode(mode);
    setAuthModalOpen(true);
  };

  const handleSelectSessionResults = (sessionId) => {
    setSelectedSessionId(sessionId);
    setActiveTab('results');
  };

  const handleBackFromResults = () => {
    setActiveTab('vote');
    setSelectedSessionId(null);
  };

  return (
    <div className="min-h-screen bg-slate-50 flex flex-col font-sans">
      <Navbar
        activeTab={activeTab}
        setActiveTab={(tab) => {
          if (tab === 'host' && !isAuthenticated) {
            handleOpenAuthModal('login');
          } else {
            setActiveTab(tab);
          }
        }}
        onOpenAuthModal={handleOpenAuthModal}
      />

      <main className="flex-1 max-w-6xl w-full mx-auto px-4 sm:px-6 py-8">
        {activeTab === 'vote' && (
          <VoterView
            onSelectSessionResults={handleSelectSessionResults}
            onOpenAuthModal={handleOpenAuthModal}
          />
        )}

        {activeTab === 'host' && isAuthenticated && (
          <HostDashboard
            onSelectSessionResults={handleSelectSessionResults}
          />
        )}

        {activeTab === 'results' && selectedSessionId && (
          <ResultsView
            sessionId={selectedSessionId}
            onBack={handleBackFromResults}
          />
        )}
      </main>

      <footer className="border-t border-slate-200 bg-white py-6 mt-auto">
        <div className="max-w-6xl mx-auto px-4 sm:px-6 flex flex-col sm:flex-row items-center justify-between gap-3 text-xs text-slate-500">
          <p>© 2026 VoteSphere. Full Stack Java Servlet + ReactJS Voting System.</p>
          <div className="flex items-center gap-4">
            <span>Jakarta Servlet 5.0</span>
            <span>•</span>
            <span>MySQL 8</span>
            <span>•</span>
            <span>React 19 + Tailwind</span>
          </div>
        </div>
      </footer>

      <AuthModal
        isOpen={authModalOpen}
        onClose={() => setAuthModalOpen(false)}
        initialMode={authModalMode}
      />
    </div>
  );
}

export default function App() {
  return (
    <AuthProvider>
      <MainContent />
    </AuthProvider>
  );
}
