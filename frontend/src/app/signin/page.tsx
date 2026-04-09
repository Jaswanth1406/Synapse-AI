'use client';

import { useState } from 'react';
import { authClient } from '@/lib/auth-client';
import { Activity, Mail, Lock, ArrowRight } from 'lucide-react';
import Link from 'next/link';

export default function SignIn() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSignIn = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    
    try {
        const { data, error } = await authClient.signIn.email({
            email,
            password
        });
        if (error) {
            setError(error.message);
        } else {
            // Redirect to dashboard
            window.location.href = '/dashboard';
        }
    } catch (err: any) {
        setError(err.message || 'An error occurred');
    } finally {
        setLoading(false);
    }
  };

  const handleGoogleSignIn = async () => {
    setLoading(true);
    setError('');

    try {
      const { data, error } = await authClient.signIn.social({
        provider: 'google',
        callbackURL: '/dashboard'
      });
      if (error) {
        setError(error.message);
        setLoading(false);
      }
    } catch (err: any) {
        setError(err.message || 'A Google sign-in error occurred');
        setLoading(false);
    }
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', minHeight: '100vh', padding: '0 24px', position: 'relative', background: '#f6fcfc' }}>
      <div className="glass-panel" style={{ width: '100%', maxWidth: '400px', display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '2rem' }}>
          <div style={{ background: '#10b981', padding: '8px', borderRadius: '8px', color: 'white' }}>
            <Activity size={24} />
          </div>
          <h2 style={{ fontSize: '1.5rem', fontWeight: '800', letterSpacing: '-0.5px' }}>SYNAPSE <span style={{ fontWeight: '400' }}>AI</span></h2>
        </div>
        
        <h1 style={{ fontSize: '1.5rem', marginBottom: '0.5rem', fontWeight: '700' }}>Welcome Back</h1>
        <p style={{ color: '#64748b', marginBottom: '2rem', textAlign: 'center', fontSize: '0.9rem' }}>Sign in to continue to your dashboard</p>
        
        {error && (
            <div style={{ width: '100%', padding: '12px', background: '#fef2f2', color: '#ef4444', borderRadius: '8px', marginBottom: '1rem', fontSize: '0.85rem', border: '1px solid #fca5a5' }}>
                {error}
            </div>
        )}
        
        <form onSubmit={handleSignIn} style={{ width: '100%', display: 'flex', flexDirection: 'column', gap: '1rem' }}>
          <div>
            <label style={{ display: 'block', marginBottom: '0.5rem', color: '#64748b', fontWeight: '600', fontSize: '0.85rem' }}>Email Address</label>
            <div style={{ position: 'relative' }}>
                <Mail size={16} style={{ position: 'absolute', left: '12px', top: '50%', transform: 'translateY(-50%)', color: '#94a3b8' }} />
                <input 
                  type="email" 
                  required
                  placeholder="agent@synapse.ai"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  style={{ width: '100%', padding: '12px 16px 12px 36px', borderRadius: '8px', border: '1px solid #cbd5e1', background: '#f8fafc', color: '#0f172a', fontFamily: 'inherit', fontSize: '0.95rem', outline: 'none' }}
                />
            </div>
          </div>
          
          <div>
            <label style={{ display: 'block', marginBottom: '0.5rem', color: '#64748b', fontWeight: '600', fontSize: '0.85rem' }}>Password</label>
            <div style={{ position: 'relative' }}>
                <Lock size={16} style={{ position: 'absolute', left: '12px', top: '50%', transform: 'translateY(-50%)', color: '#94a3b8' }} />
                <input 
                  type="password" 
                  required
                  placeholder="••••••••"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  style={{ width: '100%', padding: '12px 16px 12px 36px', borderRadius: '8px', border: '1px solid #cbd5e1', background: '#f8fafc', color: '#0f172a', fontFamily: 'inherit', fontSize: '0.95rem', outline: 'none' }}
                />
            </div>
          </div>
          
          <button type="submit" disabled={loading} className="btn-primary" style={{ width: '100%', padding: '12px', borderRadius: '8px', marginTop: '1rem', display: 'flex', justifyContent: 'center', alignItems: 'center', gap: '8px', opacity: loading ? 0.7 : 1 }}>
            {loading ? 'Signing In...' : 'Sign In'} <ArrowRight size={16} />
          </button>
        </form>

        <div style={{ width: '100%', display: 'flex', alignItems: 'center', gap: '12px', margin: '2rem 0 1rem 0' }}>
            <div style={{ flex: 1, height: '1px', background: '#e2e8f0' }}></div>
            <span style={{ fontSize: '0.8rem', color: '#94a3b8', fontWeight: '500' }}>OR</span>
            <div style={{ flex: 1, height: '1px', background: '#e2e8f0' }}></div>
        </div>

        <button 
          type="button" 
          onClick={handleGoogleSignIn}
          disabled={loading} 
          style={{ width: '100%', padding: '12px', background: 'white', color: '#334155', border: '1px solid #cbd5e1', borderRadius: '8px', fontWeight: '600', display: 'flex', justifyContent: 'center', alignItems: 'center', gap: '12px', cursor: 'pointer', transition: 'background 0.2s', opacity: loading ? 0.7 : 1 }}
        >
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M22.56 12.25C22.56 11.47 22.49 10.72 22.36 10H12V14.26H17.92C17.67 15.63 16.64 16.8 15.46 17.58V20.33H19.04C21.03 18.5 22.56 15.64 22.56 12.25Z" fill="#4285F4"/>
              <path d="M12 23C14.97 23 17.47 22.02 19.04 20.33L15.46 17.58C14.61 18.15 13.43 18.52 12 18.52C9.23999 18.52 6.89999 16.66 6.03999 14.16H2.35999V17C4.13999 20.53 7.82 23 12 23Z" fill="#34A853"/>
              <path d="M6.04 14.16C5.82 13.49 5.69 12.76 5.69 12C5.69 11.24 5.82 10.51 6.04 9.84V6.99998H2.36C1.63 8.44998 1.2 10.15 1.2 12C1.2 13.85 1.63 15.55 2.36 17L6.04 14.16Z" fill="#FBBC05"/>
              <path d="M12 5.48C13.62 5.48 15.06 6.04 16.2 7.12L19.12 4.2C17.47 2.68 14.97 1.76 12 1.76C7.82 1.76 4.14 4.25 2.36 7.74L6.04 10.6C6.9 8.1 9.24 5.48 12 5.48Z" fill="#EA4335"/>
          </svg>
          Continue with Google
        </button>
        
        <p style={{ marginTop: '2rem', fontSize: '0.85rem', color: '#64748b' }}>
            Don't have an account? <Link href="/signup" style={{ color: '#10b981', fontWeight: '600', textDecoration: 'none' }}>Create Account</Link>
        </p>
      </div>
    </div>
  );
}