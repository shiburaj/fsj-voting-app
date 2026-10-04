import React, { useState, useCallback, useMemo } from 'react';
import { api } from '../services/api';
import { useAuth } from '../context/AuthContext';
import { usePolling } from '../hooks/usePolling';
import { Button } from './ui/Button';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from './ui/Card';
import { Badge } from './ui/Badge';
import { 
  Trophy, 
  ArrowLeft, 
  RefreshCw, 
  Power, 
  BarChart3, 
  Users, 
  CheckCircle2,
  Lock,
  Radio
} from 'lucide-react';

export function ResultsView({ sessionId, onBack }) {
  const { user } = useAuth();
  const [results, setResults] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [lastUpdated, setLastUpdated] = useState(null);
  const [toggling, setToggling] = useState(false);

  const fetchResults = useCallback(async () => {
    if (!sessionId) return;
    try {
      const res = await api.results.get(sessionId);
      setResults(res.data);
      setLastUpdated(new Date().toLocaleTimeString());
      setError('');
    } catch (err) {
      setError(err.message || 'Failed to fetch live results.');
    } finally {
      setLoading(false);
    }
  }, [sessionId]);

  // Poll results every 4 seconds (4000ms)
  usePolling(fetchResults, 4000, true);

  const handleToggleStatus = async () => {
    if (!results) return;
    setToggling(true);
    try {
      await api.sessions.toggleStatus(results.id || results.sessionId, !results.votingEnabled);
      // Immediately re-fetch
      await fetchResults();
    } catch (err) {
      alert('Failed to toggle status: ' + err.message);
    } finally {
      setToggling(false);
    }
  };

  // Find candidate with max votes for winner/leader highlight
  const topCandidateId = useMemo(() => {
    if (!results || !results.candidates || results.candidates.length === 0) return null;
    const maxVotes = Math.max(...results.candidates.map((c) => c.voteCount));
    if (maxVotes === 0) return null;
    const top = results.candidates.find((c) => c.voteCount === maxVotes);
    return top ? (top.id ?? top.candidateId) : null;
  }, [results]);

  return (
    <div className="max-w-3xl mx-auto space-y-6">
      {/* Back button and live polling status indicator */}
      <div className="flex items-center justify-between">
        <Button
          variant="outline"
          size="sm"
          onClick={onBack}
        >
          <ArrowLeft className="w-4 h-4" />
          <span>Back</span>
        </Button>

        {/* 4-Second Polling status badge */}
        <div className="flex items-center gap-2">
          <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-semibold bg-emerald-50 text-emerald-700 border border-emerald-200 shadow-2xs">
            <span className="relative flex h-2 w-2">
              <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-emerald-400 opacity-75"></span>
              <span className="relative inline-flex rounded-full h-2 w-2 bg-emerald-500"></span>
            </span>
            <span>Live Polling (4s)</span>
          </span>
          {lastUpdated && (
            <span className="text-xs text-slate-400 hidden sm:inline">
              Updated: {lastUpdated}
            </span>
          )}
        </div>
      </div>

      {loading && !results ? (
        <Card className="p-12 text-center">
          <RefreshCw className="w-8 h-8 animate-spin mx-auto text-indigo-600 mb-3" />
          <p className="text-sm text-slate-500">Loading session results...</p>
        </Card>
      ) : error && !results ? (
        <Card className="p-8 text-center bg-red-50 border-red-200">
          <p className="text-sm text-red-600 mb-4">{error}</p>
          <Button size="sm" onClick={fetchResults}>
            Retry
          </Button>
        </Card>
      ) : results ? (
        <Card className="overflow-hidden">
          <CardHeader className="bg-slate-50/50 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
            <div>
              <div className="flex items-center gap-2">
                <CardTitle className="text-2xl">{results.name || results.sessionName}</CardTitle>
              </div>
              <CardDescription>
                Session Code: <span className="font-mono font-bold text-indigo-600">{results.code}</span>
              </CardDescription>
            </div>

            <div className="flex items-center gap-2">
              <Badge variant={results.votingEnabled ? 'success' : 'danger'}>
                {results.votingEnabled ? 'Voting Active' : 'Voting Closed'}
              </Badge>

              {/* Quick toggle button if user might be host */}
              <Button
                variant={results.votingEnabled ? 'outline' : 'success'}
                size="sm"
                loading={toggling}
                onClick={handleToggleStatus}
              >
                <Power className="w-3.5 h-3.5" />
                <span>{results.votingEnabled ? 'End Voting' : 'Re-open Voting'}</span>
              </Button>
            </div>
          </CardHeader>

          <CardContent className="space-y-6">
            {/* Total Votes Stat */}
            <div className="grid grid-cols-2 gap-4">
              <div className="p-4 rounded-xl bg-indigo-50/60 border border-indigo-100 flex items-center gap-3">
                <div className="w-10 h-10 rounded-lg bg-indigo-600 text-white flex items-center justify-center">
                  <Users className="w-5 h-5" />
                </div>
                <div>
                  <span className="text-xs font-semibold text-indigo-700 uppercase tracking-wider block">
                    Total Ballots Cast
                  </span>
                  <span className="text-2xl font-bold text-indigo-900">
                    {results.totalVotes}
                  </span>
                </div>
              </div>

              <div className="p-4 rounded-xl bg-slate-50 border border-slate-200 flex items-center gap-3">
                <div className="w-10 h-10 rounded-lg bg-slate-800 text-white flex items-center justify-center">
                  <BarChart3 className="w-5 h-5" />
                </div>
                <div>
                  <span className="text-xs font-semibold text-slate-500 uppercase tracking-wider block">
                    Candidates Count
                  </span>
                  <span className="text-2xl font-bold text-slate-900">
                    {results.candidates?.length || 0}
                  </span>
                </div>
              </div>
            </div>

            {/* Candidate Breakdown List */}
            <div className="space-y-3">
              <div className="flex items-center justify-between">
                <label className="text-xs font-semibold text-slate-700 uppercase tracking-wider">
                  Live Vote Standings
                </label>
                <button
                  onClick={fetchResults}
                  className="text-xs text-slate-500 hover:text-slate-800 flex items-center gap-1 cursor-pointer transition-colors"
                >
                  <RefreshCw className="w-3 h-3" /> Refresh
                </button>
              </div>

              <div className="space-y-3">
                {results.candidates?.map((candidate, idx) => {
                  const candidateId = candidate.id ?? candidate.candidateId;
                  const candidateName = candidate.name || candidate.candidateName;
                  const isLeader = candidateId === topCandidateId;

                  return (
                    <div
                      key={candidateId}
                      className={`p-4 rounded-xl border transition-all ${
                        isLeader
                          ? 'border-indigo-300 bg-indigo-50/20 shadow-xs'
                          : 'border-slate-200 bg-white'
                      }`}
                    >
                      <div className="flex items-center justify-between mb-2">
                        <div className="flex items-center gap-2">
                          <span className="w-6 h-6 rounded-full bg-slate-100 text-slate-600 font-bold text-xs flex items-center justify-center">
                            #{idx + 1}
                          </span>
                          <span className="font-semibold text-slate-900">
                            {candidateName}
                          </span>
                          {isLeader && (
                            <span className="inline-flex items-center gap-1 text-[11px] font-semibold text-amber-700 bg-amber-50 px-2 py-0.5 rounded-full border border-amber-200">
                              <Trophy className="w-3 h-3 text-amber-500" /> Leader
                            </span>
                          )}
                        </div>

                        <div className="text-right">
                          <span className="text-sm font-bold text-slate-900">
                            {candidate.voteCount}
                          </span>
                          <span className="text-xs text-slate-500 ml-1">
                            ({candidate.percentage}%)
                          </span>
                        </div>
                      </div>

                      {/* Progress Bar */}
                      <div className="w-full bg-slate-100 h-2.5 rounded-full overflow-hidden">
                        <div
                          className={`h-full rounded-full transition-all duration-500 ease-out ${
                            isLeader ? 'bg-indigo-600' : 'bg-slate-500'
                          }`}
                          style={{ width: `${Math.max(candidate.percentage, 0)}%` }}
                        />
                      </div>
                    </div>
                  );
                })}
              </div>
            </div>
          </CardContent>
        </Card>
      ) : null}
    </div>
  );
}
