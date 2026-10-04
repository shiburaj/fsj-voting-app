// Base API configuration
const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/ServletApp/api';

/**
 * Universal fetch wrapper for the Java Servlet API
 */
async function request(endpoint, options = {}) {
  const url = `${API_BASE_URL}${endpoint}`;
  
  const headers = {
    'Accept': 'application/json',
    ...options.headers,
  };

  // If payload is provided, set Content-Type
  if (options.body && typeof options.body === 'object' && !(options.body instanceof FormData)) {
    headers['Content-Type'] = 'application/json';
    options.body = JSON.stringify(options.body);
  }

  // Inject logged in user ID header if present in localStorage
  const storedUser = localStorage.getItem('voting_user');
  if (storedUser) {
    try {
      const user = JSON.parse(storedUser);
      if (user && user.id) {
        headers['X-User-Id'] = user.id.toString();
      }
    } catch (e) {
      console.error('Failed to parse stored user', e);
    }
  }

  const response = await fetch(url, {
    ...options,
    credentials: 'include',
    headers,
  });

  const contentType = response.headers.get('content-type');
  let data = null;
  if (contentType && contentType.includes('application/json')) {
    data = await response.json();
  } else {
    data = await response.text();
  }

  if (!response.ok) {
    const errorMessage = data?.message || data?.error || (typeof data === 'string' ? data : `Request failed with status ${response.status}`);
    const error = new Error(errorMessage);
    error.status = response.status;
    error.data = data;
    throw error;
  }

  return data;
}

export const api = {
  auth: {
    login: (email, password) => 
      request('/auth/login', { method: 'POST', body: { email, password } }),
    register: (name, email, password) => 
      request('/auth/register', { method: 'POST', body: { name, email, password } }),
    logout: () =>
      request('/auth/logout', { method: 'POST' }),
    me: () => 
      request('/auth/me', { method: 'GET' }),
  },
  sessions: {
    getMySessions: () => 
      request('/sessions', { method: 'GET' }),
    createSession: (name, candidates) => {
      const candidateObjects = (candidates || []).map((c) =>
        typeof c === 'string' ? { name: c } : c
      );
      return request('/sessions', {
        method: 'POST',
        body: { name, candidates: candidateObjects },
      });
    },
    getByCode: (code) => 
      request(`/sessions?code=${encodeURIComponent(code)}`, { method: 'GET' }),
    getById: (id) => 
      request(`/sessions?id=${encodeURIComponent(id)}`, { method: 'GET' }),
    toggleStatus: (sessionId, votingEnabled) => 
      request('/sessions/status', { method: 'POST', body: { id: sessionId, sessionId, votingEnabled } }),
  },
  votes: {
    cast: (sessionId, candidateId) => 
      request('/votes', { method: 'POST', body: { sessionId, candidateId } }),
    getStatus: (sessionId) => 
      request(`/votes/status?sessionId=${encodeURIComponent(sessionId)}`, { method: 'GET' }),
  },
  results: {
    get: (sessionId) => 
      request(`/results?sessionId=${encodeURIComponent(sessionId)}`, { method: 'GET' }),
  }
};
