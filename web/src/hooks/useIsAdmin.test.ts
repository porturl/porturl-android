import { describe, it, expect, vi } from 'vitest';
import { useIsAdmin } from './useIsAdmin';
import { renderHook } from '@testing-library/react';
import { useAuth } from 'react-oidc-context';

vi.mock('react-oidc-context');

describe('useIsAdmin', () => {
  it('returns false if not authenticated', () => {
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    vi.mocked(useAuth).mockReturnValue({ user: null } as any);
    const { result } = renderHook(() => useIsAdmin());
    expect(result.current).toBe(false);
  });

  it('returns true if realm role admin is present', () => {

    vi.mocked(useAuth).mockReturnValue({
      user: {
        profile: {
          realm_access: { roles: ['admin'] }
        }
      }
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
    } as any);
    const { result } = renderHook(() => useIsAdmin());
    expect(result.current).toBe(true);
  });

  it('returns false if no admin role', () => {

    vi.mocked(useAuth).mockReturnValue({
      user: {
        profile: {
          realm_access: { roles: ['user'] }
        }
      }
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
    } as any);
    const { result } = renderHook(() => useIsAdmin());
    expect(result.current).toBe(false);
  });
});
