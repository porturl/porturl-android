import React, { useEffect, useState } from 'react';
import { AuthProvider as OidcProvider } from 'react-oidc-context';
import type { AuthProviderProps } from 'react-oidc-context';

export const AuthProvider = ({ children }: { children: React.ReactNode }) => {
  const [oidcConfig, setOidcConfig] = useState<AuthProviderProps | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchConfig = async () => {
      try {
        const backendUrl = import.meta.env.VITE_BACKEND_URL || 'http://localhost:8080';
        const response = await fetch(`${backendUrl}/actuator/info`);
        if (!response.ok) throw new Error('Failed to fetch config');
        const data = await response.json();

        // The backend returns { "auth": { "issuer-uri": "..." } }
        // Note: The key in JSON might be "issuer-uri" (kebab-case) but Gson might map it.
        // In Kotlin: @SerializedName("issuer-uri"). So JSON has "issuer-uri".
        const issuer = data.auth['issuer-uri'];

        if (!issuer) throw new Error('Issuer URI not found in config');

        setOidcConfig({
          authority: issuer,
          client_id: 'porturl-web-client',
          redirect_uri: window.location.origin,
          scope: 'openid profile email offline_access',
          onSigninCallback: () => {
             // Remove code and state from URL
             window.history.replaceState({}, document.title, window.location.pathname);
          },
        });
      } catch (err) {
        console.error(err);
        setError('Could not load configuration. Ensure backend is running and accessible.');
      }
    };
    fetchConfig();
  }, []);

  if (error) {
    return (
      <div className="flex items-center justify-center min-h-screen bg-red-50 text-red-800 p-4">
        <div>
          <h1 className="text-xl font-bold mb-2">Configuration Error</h1>
          <p>{error}</p>
          <p className="text-sm mt-2 text-gray-600">Backend URL: {import.meta.env.VITE_BACKEND_URL || 'http://localhost:8080'}</p>
        </div>
      </div>
    );
  }

  if (!oidcConfig) {
    return (
      <div className="flex items-center justify-center min-h-screen bg-gray-50">
        <div className="text-gray-600">Loading configuration...</div>
      </div>
    );
  }

  return <OidcProvider {...oidcConfig}>{children}</OidcProvider>;
};
