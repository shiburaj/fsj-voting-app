import React, { useState } from 'react';
import { api } from '../services/api';
import { useAuth } from '../context/AuthContext';
import { Button } from './ui/Button';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from './ui/Card';
import { Input } from './ui/Input';
import { Badge } from './ui/Badge';
import { 
  Search, 
  CheckCircle2, 
  AlertCircle, 
  Vote, 
  BarChart3, 
  ArrowLeft,
  Check,
  Lock
} from 'lucide-react';

export function VoterView({ onSelectSessionResults, onOpenAuthModal }) {
  const { user, isAuthenticated } = useAuth();

  const [sessionCode, setSessionCode] = useState('');
  const [loading, setLoading] = useState(false);
  const [session, setSession] = useState(null);
  const [selectedCandidateId, setSelectedCandidateId] = useState(null);
  const [hasVoted, setHasVoted] = useState(false);
  const [error, setError] = useState('');
  const [successMessage, setSuccessMessage] = useState('');
  const [submittingVote, setSubmittingVote] = useState(false);

  const handleSearchSession = async (e) => {
    if (e) e.preventDefault();
    if (!sessionCode.trim()) {
      setError('Please enter a valid session code.');
      return;
    }

    setError('');
    setLoading(true);
    setSuccessMessage('');
    setSelectedCandidateId(null);

    try {
      const res = await api.sessions.getByCode(sessionCode.trim());
      const sessionData = res.data;
      setSession(sessionData);

      // Check if user already voted in this session
      if (isAuthenticated && user?.id) {
        try {
          const statusRes = await api.votes.getStatus(sessionData.id);
          setHasVoted(statusRes.data?.hasVoted || false);
        } catch (statusErr) {
          console.warn('Could not verify vote status', statusErr);
        }
      }
    } catch (err) {
      setSession(null);
      setError(err.message || 'Session not found. Please double-check the code.');
    } finally {
      setLoading(false);
    }
  };

  const handleCastVote = async () => {
    if (!isAuthenticated) {
      onOpenAuthModal('login');
      return;
    }

    if (!selectedCandidateId) {
      setError('Please select a candidate to cast your vote.');
      return;
    }

    setSubmittingVote(true);
    setError('');

    try {
      await api.votes.cast(session.id, selectedCandidateId);
      setHasVoted(true);
      setSuccessMessage('Your vote has been officially cast and recorded!');
    } catch (err) {
      setError(err.message || 'Failed to submit vote.');
    } finally {
      setSubmittingVote(false);
    }
  };

  const handleReset = () => {
    setSession(null);
    setSessionCode('');
    setSelectedCandidateId(null);
    setHasVoted(false);
    setError('');
    setSuccessMessage('');
  };

  return (
    <div className="max-w-2xl mx-auto space-y-6">
      {/* Code Search Card (shown when no session is loaded) */}
      {!session ? (
        <Card className="p-6 sm:p-10 text-center">
          <div className="w-14 h-14 bg-indigo-50 text-indigo-600 rounded-2xl flex items-center justify-center mx-auto mb-4 shadow-xs">
            <Vote className="w-7 h-7" />
          </div>
          <h2 className="text-2xl font-bold text-slate-900 tracking-tight mb-2">
            Cast Your Vote
          </h2>
          <p className="text-sm text-slate-500 max-w-md mx-auto mb-6">
            Enter the 6-character voting code provided by the election host to view candidates and submit your ballot.
          </p>

          <form onSubmit={handleSearchSession} className="max-w-md mx-auto space-y-4">
            <div className="relative">
              <Input
                id="session-code-input"
                placeholder="Enter Code (e.g. VOTE01)"
                value={sessionCode}
                onChange={(e) => setSessionCode(e.target.value.toUpperCase())}
                className="text-center font-mono text-lg tracking-widest uppercase h-12"
                maxLength={10}
                required
              />
            </div>

            {error && (
              <div className="p-3 rounded-lg bg-red-50 border border-red-200 text-red-700 text-xs flex items-center gap-2 text-left">
                <AlertCircle className="w-4 h-4 shrink-0" />
                <span>{error}</span>
              </div>
            )}

            <Button
              type="submit"
              className="w-full h-11 text-base"
              loading={loading}
            >
              <Search className="w-4 h-4" />
              <span>Find Voting Session</span>
            </Button>
          </form>
        </Card>
      ) : (
        /* Ballot Card (when session is found) */
        <Card className="overflow-hidden">
          <CardHeader className="bg-slate-50/50 flex flex-row items-center justify-between gap-4">
            <div>
              <div className="flex items-center gap-2 mb-1">
                <button
                  type="button"
                  onClick={handleReset}
                  className="text-slate-400 hover:text-slate-700 p-1 -ml-1 rounded-md transition-colors cursor-pointer"
                >
                  <ArrowLeft className="w-4 h-4" />
                </button>
                <CardTitle className="text-xl">{session.name}</CardTitle>
              </div>
              <CardDescription>
                Session Code: <span className="font-mono font-bold text-indigo-600">{session.code}</span>
              </CardDescription>
            </div>

            <Badge variant={session.votingEnabled ? 'success' : 'danger'}>
              {session.votingEnabled ? 'Voting Active' : 'Voting Closed'}
            </Badge>
          </CardHeader>

          <CardContent className="space-y-6">
            {/* Warning if voting is closed */}
            {!session.votingEnabled && (
              <div className="p-4 rounded-xl bg-amber-50 border border-amber-200 text-amber-800 text-sm flex items-start gap-3">
                <Lock className="w-5 h-5 shrink-0 mt-0.5 text-amber-600" />
                <div>
                  <h4 className="font-semibold">Voting is Currently Closed</h4>
                  <p className="text-xs mt-0.5 text-amber-700">
                    The host has paused or ended voting for this session. You can view the live tally or wait for it to re-open.
                  </p>
                </div>
              </div>
            )}

            {/* Success banner if voted */}
            {hasVoted && (
              <div className="p-4 rounded-xl bg-emerald-50 border border-emerald-200 text-emerald-800 text-sm flex items-start gap-3">
                <CheckCircle2 className="w-5 h-5 shrink-0 mt-0.5 text-emerald-600" />
                <div className="flex-1">
                  <h4 className="font-semibold">Ballot Submitted!</h4>
                  <p className="text-xs mt-0.5 text-emerald-700">
                    {successMessage || 'You have already cast your vote in this session. Each voter is limited to one ballot.'}
                  </p>
                </div>
              </div>
            )}

            {/* Error banner */}
            {error && (
              <div className="p-3 rounded-lg bg-red-50 border border-red-200 text-red-700 text-xs flex items-start gap-2">
                <AlertCircle className="w-4 h-4 shrink-0 mt-0.5" />
                <span>{error}</span>
              </div>
            )}

            {/* Candidate selection cards */}
            <div className="space-y-3">
              <label className="text-xs font-semibold text-slate-700 uppercase tracking-wider block">
                Select Your Preferred Candidate
              </label>

              <div className="grid grid-cols-1 gap-3">
                {session.candidates?.map((candidate) => {
                  const isSelected = selectedCandidateId === candidate.id;
                  const isDisabled = !session.votingEnabled || hasVoted;

                  return (
                    <div
                      key={candidate.id}
                      onClick={() => !isDisabled && setSelectedCandidateId(candidate.id)}
                      className={`p-4 rounded-xl border transition-all flex items-center justify-between ${
                        isDisabled
                          ? 'opacity-70 bg-slate-50 border-slate-200 cursor-not-allowed'
                          : isSelected
                          ? 'border-indigo-600 bg-indigo-50/50 shadow-sm cursor-pointer'
                          : 'border-slate-200 bg-white hover:border-slate-300 hover:bg-slate-50/50 cursor-pointer'
                      }`}
                    >
                      <div className="flex items-center gap-3">
                        <div
                          className={`w-6 h-6 rounded-full border flex items-center justify-center transition-colors ${
                            isSelected
                              ? 'border-indigo-600 bg-indigo-600 text-white'
                              : 'border-slate-300 bg-white'
                          }`}
                        >
                          {isSelected && <Check className="w-3.5 h-3.5 stroke-[3]" />}
                        </div>
                        <span className="font-medium text-slate-900">{candidate.name}</span>
                      </div>

                      <span className="text-xs text-slate-400 font-mono">
                        #{candidate.id}
                      </span>
                    </div>
                  );
                })}
              </div>
            </div>

            {/* Actions */}
            <div className="pt-2 flex flex-col sm:flex-row items-center gap-3">
              {!hasVoted && session.votingEnabled ? (
                <Button
                  onClick={handleCastVote}
                  loading={submittingVote}
                  disabled={!selectedCandidateId}
                  className="w-full sm:flex-1 h-11"
                >
                  <Vote className="w-4 h-4" />
                  <span>Submit My Vote</span>
                </Button>
              ) : null}

              <Button
                variant="outline"
                onClick={() => onSelectSessionResults(session.id)}
                className="w-full sm:flex-1 h-11"
              >
                <BarChart3 className="w-4 h-4" />
                <span>View Results & Tally</span>
              </Button>
            </div>
          </CardContent>
        </Card>
      )}
    </div>
  );
}
