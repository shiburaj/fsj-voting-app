import { useEffect, useRef } from 'react';

/**
 * Custom hook to poll an async function at fixed intervals (default: 4000ms)
 * Includes immediate initial call and proper interval cleanup.
 */
export function usePolling(callback, delay = 4000, isEnabled = true) {
  const savedCallback = useRef(callback);

  useEffect(() => {
    savedCallback.current = callback;
  }, [callback]);

  useEffect(() => {
    if (!isEnabled || delay === null) {
      return;
    }

    // Call immediately upon enabling
    savedCallback.current();

    // Set interval for recurring calls
    const id = setInterval(() => {
      savedCallback.current();
    }, delay);

    return () => clearInterval(id);
  }, [delay, isEnabled]);
}
