import React, { useState, useEffect, useCallback } from 'react';
import { api } from '../services/api';
import { Button } from './ui/Button';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from './ui/Card';
import { Input } from './ui/Input';
import { Badge } from './ui/Badge';
import { Modal } from './ui/Modal';
import { 
  Plus, 
  Trash2, 
  Copy, 
  Check, 
  BarChart3, 
  Power, 
  Users, 
  Calendar,
  AlertCircle,
  RefreshCw 
} from 'lucide-react';

export function HostDashboard({ onSelectSessionResults }) {
  const [sessions, setSessions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [copiedCode, setCopiedCode] = useState(null);

  // Create Session Modal State
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [newTitle, setNewTitle] = useState('');
  const [candidates, setCandidates] = useState(['', '']);
  const [createError, setCreateError] = useState('');
  const [creating, setCreating] = useState(false);

  // Status toggle tracking
  const [togglingId, setTogglingId] = useState(null);

  const fetchSessions = useCallback(async () => {
    try {
      setError('');
      const res = await api.sessions.getMySessions();
      setSessions(res.data || []);
    } catch (err) {
      setError(err.message || 'Failed to fetch sessions.');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchSessions();
  }, [fetchSessions]);

  const handleAddCandidate = () => {
    setCandidates((prev) => [...prev, '']);
  };

  const handleRemoveCandidate = (index) => {
    if (candidates.length <= 2) return;
    setCandidates((prev) => prev.filter((_, i) => i !== index));
  };

  const handleCandidateChange = (index, value) => {
    setCandidates((prev) => {
      const updated = [...prev];
      updated[index] = value;
      return updated;
    });
  };

  const handleCreateSession = async (e) => {
    e.preventDefault();
    setCreateError('');

    if (!newTitle.trim()) {
      setCreateError('Please enter a session name.');
      return;
    }

    const validCandidates = candidates.map((c) => c.trim()).filter((c) => c.length > 0);
    if (validCandidates.length < 2) {
      setCreateError('At least 2 non-empty candidate names are required.');
      return;
    }

    setCreating(true);
    try {
      await api.sessions.createSession(newTitle.trim(), validCandidates);
      setIsCreateModalOpen(false);
      setNewTitle('');
      setCandidates(['', '']);
      fetchSessions();
    } catch (err) {
      setCreateError(err.message || 'Failed to create voting session.');
    } finally {
      setCreating(false);
    }
  };

  const handleToggleStatus = async (session) => {
    setTogglingId(session.id);
    try {
      await api.sessions.toggleStatus(session.id, !session.votingEnabled);
      // Update local state
      setSessions((prev) =>
        prev.map((s) => (s.id === session.id ? { ...s, votingEnabled: !s.votingEnabled } : s))
      );
    } catch (err) {
      alert('Failed to toggle session voting status: ' + err.message);
    } finally {
      setTogglingId(null);
    }
  };

  const handleCopyCode = (code) => {
    navigator.clipboard.writeText(code);
    setCopiedCode(code);
    setTimeout(() => setCopiedCode(null), 2000);
  };

  return (
    <div className="space-y-6">
      {/* Header bar */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 bg-white p-6 rounded-2xl border border-slate-200 shadow-xs">
        <div>
          <h1 className="text-2xl font-bold text-slate-900 tracking-tight m-0">Host Control Panel</h1>
          <p className="text-sm text-slate-500 mt-1">
            Create new voting sessions, distribute codes, and monitor live voter counts.
          </p>
        </div>

        <div className="flex items-center gap-2">
          <Button
            variant="outline"
            size="md"
            onClick={fetchSessions}
            disabled={loading}
          >
            <RefreshCw className={`w-4 h-4 ${loading ? 'animate-spin' : ''}`} />
            <span>Refresh</span>
          </Button>

          <Button
            onClick={() => setIsCreateModalOpen(true)}
            size="md"
          >
            <Plus className="w-4 h-4" />
            <span>New Session</span>
          </Button>
        </div>
      </div>

      {/* Global Error Banner */}
      {error && (
        <div className="p-4 rounded-xl bg-red-50 border border-red-200 text-red-700 text-sm flex items-center justify-between">
          <div className="flex items-center gap-2">
            <AlertCircle className="w-5 h-5 shrink-0" />
            <span>{error}</span>
          </div>
          <Button size="sm" variant="outline" onClick={fetchSessions}>
            Retry
          </Button>
        </div>
      )}

      {/* Sessions Grid */}
      {loading ? (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {[1, 2].map((n) => (
            <div key={n} className="h-48 bg-white border border-slate-200 rounded-2xl animate-pulse" />
          ))}
        </div>
      ) : sessions.length === 0 ? (
        <Card className="p-12 text-center">
          <div className="w-16 h-16 bg-indigo-50 text-indigo-600 rounded-full flex items-center justify-center mx-auto mb-4">
            <Users className="w-8 h-8" />
          </div>
          <h3 className="text-lg font-bold text-slate-900 mb-1">No Voting Sessions Created Yet</h3>
          <p className="text-sm text-slate-500 max-w-md mx-auto mb-6">
            Get started by creating your first voting session, adding candidates, and sharing the code with voters.
          </p>
          <Button onClick={() => setIsCreateModalOpen(true)}>
            <Plus className="w-4 h-4" />
            <span>Create Your First Session</span>
          </Button>
        </Card>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
          {sessions.map((session) => (
            <Card key={session.id} className="flex flex-col justify-between hover:shadow-md transition-shadow">
              <CardHeader className="flex flex-row items-start justify-between gap-4 pb-4">
                <div className="space-y-1">
                  <div className="flex items-center gap-2">
                    <CardTitle>{session.name}</CardTitle>
                  </div>
                  <div className="flex items-center gap-2 text-xs text-slate-500">
                    <Calendar className="w-3.5 h-3.5" />
                    <span>Created: {session.createdAt || 'Just now'}</span>
                  </div>
                </div>

                <Badge variant={session.votingEnabled ? 'success' : 'danger'}>
                  <span className={`w-1.5 h-1.5 rounded-full ${session.votingEnabled ? 'bg-emerald-500 animate-pulse' : 'bg-rose-500'}`} />
                  {session.votingEnabled ? 'Voting Open' : 'Voting Closed'}
                </Badge>
              </CardHeader>

              <CardContent className="space-y-4 py-4">
                {/* Session Code Card */}
                <div className="flex items-center justify-between p-3.5 bg-slate-50 rounded-xl border border-slate-200/80">
                  <div>
                    <span className="text-[11px] font-semibold text-slate-500 uppercase tracking-wider block">
                      Voter Join Code
                    </span>
                    <span className="text-xl font-mono font-bold tracking-widest text-indigo-700">
                      {session.code}
                    </span>
                  </div>

                  <Button
                    size="sm"
                    variant="outline"
                    onClick={() => handleCopyCode(session.code)}
                    className="h-9"
                  >
                    {copiedCode === session.code ? (
                      <>
                        <Check className="w-3.5 h-3.5 text-emerald-600" />
                        <span className="text-emerald-600">Copied!</span>
                      </>
                    ) : (
                      <>
                        <Copy className="w-3.5 h-3.5 text-slate-500" />
                        <span>Copy Code</span>
                      </>
                    )}
                  </Button>
                </div>

                {/* Candidate List Snippet */}
                {session.candidates && session.candidates.length > 0 && (
                  <div>
                    <span className="text-xs font-semibold text-slate-500 uppercase tracking-wider block mb-2">
                      Candidates ({session.candidates.length})
                    </span>
                    <div className="flex flex-wrap gap-1.5">
                      {session.candidates.map((c) => (
                        <span
                          key={c.id}
                          className="px-2.5 py-1 bg-white text-slate-700 border border-slate-200 rounded-lg text-xs font-medium"
                        >
                          {c.name}
                        </span>
                      ))}
                    </div>
                  </div>
                )}
              </CardContent>

              <div className="p-5 border-t border-slate-100 bg-slate-50/50 flex items-center justify-between gap-3">
                <Button
                  size="sm"
                  variant={session.votingEnabled ? 'outline' : 'success'}
                  loading={togglingId === session.id}
                  onClick={() => handleToggleStatus(session)}
                  className="flex-1"
                >
                  <Power className="w-4 h-4" />
                  <span>{session.votingEnabled ? 'Close Voting' : 'Enable Voting'}</span>
                </Button>

                <Button
                  size="sm"
                  variant="default"
                  onClick={() => onSelectSessionResults(session.id)}
                  className="flex-1"
                >
                  <BarChart3 className="w-4 h-4" />
                  <span>View Results</span>
                </Button>
              </div>
            </Card>
          ))}
        </div>
      )}

      {/* Create Session Modal */}
      <Modal
        isOpen={isCreateModalOpen}
        onClose={() => setIsCreateModalOpen(false)}
        title="Create New Voting Session"
        description="Enter session details and at least two candidate names."
        maxWidth="max-w-lg"
      >
        {createError && (
          <div className="p-3 mb-4 rounded-lg bg-red-50 border border-red-200 text-red-700 text-xs flex items-start gap-2">
            <AlertCircle className="w-4 h-4 shrink-0 mt-0.5" />
            <span>{createError}</span>
          </div>
        )}

        <form onSubmit={handleCreateSession} className="space-y-4">
          <Input
            id="session-name"
            label="Session Name / Title"
            placeholder="e.g. Student Council Election 2026"
            value={newTitle}
            onChange={(e) => setNewTitle(e.target.value)}
            required
          />

          <div className="space-y-2">
            <div className="flex items-center justify-between">
              <label className="text-xs font-semibold text-slate-700 uppercase tracking-wider">
                Candidate Names (Min 2)
              </label>
              <button
                type="button"
                onClick={handleAddCandidate}
                className="text-xs font-medium text-indigo-600 hover:text-indigo-800 flex items-center gap-1 cursor-pointer"
              >
                <Plus className="w-3.5 h-3.5" /> Add Candidate
              </button>
            </div>

            <div className="space-y-2 max-h-56 overflow-y-auto pr-1">
              {candidates.map((cand, index) => (
                <div key={index} className="flex items-center gap-2">
                  <Input
                    placeholder={`Candidate ${index + 1} Name`}
                    value={cand}
                    onChange={(e) => handleCandidateChange(index, e.target.value)}
                    required
                  />
                  {candidates.length > 2 && (
                    <button
                      type="button"
                      onClick={() => handleRemoveCandidate(index)}
                      className="p-2 text-slate-400 hover:text-red-500 rounded-lg transition-colors cursor-pointer"
                    >
                      <Trash2 className="w-4 h-4" />
                    </button>
                  )}
                </div>
              ))}
            </div>
          </div>

          <div className="pt-3 border-t border-slate-100 flex items-center justify-end gap-2">
            <Button
              type="button"
              variant="outline"
              onClick={() => setIsCreateModalOpen(false)}
            >
              Cancel
            </Button>
            <Button
              type="submit"
              loading={creating}
            >
              Launch Session
            </Button>
          </div>
        </form>
      </Modal>
    </div>
  );
}
