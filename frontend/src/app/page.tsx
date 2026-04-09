'use client';

import { ArrowRight, Shield, Activity, Clock, ShieldCheck, Zap } from 'lucide-react';
import Link from 'next/link';

export default function Home() {
  return (
    <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', minHeight: '100vh', padding: '0 24px', position: 'relative' }}>
      
      {/* Header */}
      <header style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', width: '100%', maxWidth: '1200px', padding: '24px 0', zIndex: 10 }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
          <div style={{ background: '#10b981', padding: '8px', borderRadius: '8px', color: 'white' }}>
            <Activity size={24} />
          </div>
          <h2 style={{ fontSize: '1.5rem', fontWeight: '800', letterSpacing: '-0.5px' }}>SYNAPSE <span style={{ fontWeight: '400' }}>AI</span></h2>
        </div>
        
        <nav style={{ display: 'flex', gap: '32px' }}>
          <a href="#" style={{ color: '#64748b', fontWeight: '500', transition: 'color 0.2s', fontSize: '0.95rem', textDecoration: 'none' }}>Features</a>
          <a href="#dashboard-preview" style={{ color: '#64748b', fontWeight: '500', transition: 'color 0.2s', fontSize: '0.95rem', textDecoration: 'none' }}>Dashboard</a>
          <a href="#" style={{ color: '#64748b', fontWeight: '500', transition: 'color 0.2s', fontSize: '0.95rem', textDecoration: 'none' }}>How it Works</a>
          <a href="#" style={{ color: '#64748b', fontWeight: '500', transition: 'color 0.2s', fontSize: '0.95rem', textDecoration: 'none' }}>Capabilities</a>
        </nav>
        
        <div style={{ display: 'flex', alignItems: 'center', gap: '24px' }}>
          <Link href="/signin" style={{ color: '#0f172a', fontWeight: '600', fontSize: '0.95rem', textDecoration: 'none' }}>Sign In</Link>
          <Link href="/dashboard" className="btn-primary" style={{ textDecoration: 'none' }}>Get Started</Link>
        </div>
      </header>

      {/* Hero Section */}
      <main style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', flex: 1, paddingTop: '10vh', textAlign: 'center', zIndex: 10, maxWidth: '800px' }}>
        
        <div style={{ display: 'inline-flex', alignItems: 'center', gap: '8px', background: '#ecfdf5', color: '#10b981', padding: '8px 16px', borderRadius: '999px', fontSize: '0.75rem', fontWeight: '800', letterSpacing: '1px', marginBottom: '2rem', border: '1px solid rgba(16, 185, 129, 0.2)' }}>
          <div style={{ width: '8px', height: '8px', borderRadius: '50%', background: '#10b981' }}></div>
          INTELLIGENT AI TELE-CALLING PLATFORM
        </div>
        
        <h1 className="text-gradient" style={{ fontSize: '7rem', fontWeight: '800', lineHeight: 1, letterSpacing: '-2px', marginBottom: '1.5rem' }}>
          SYNAPSE AI
        </h1>
        
        <h2 style={{ fontSize: '1.5rem', color: '#64748b', fontWeight: '400', marginBottom: '1rem', fontFamily: 'var(--font-syne)' }}>
          Your Next-Generation Tele-Calling Agent
        </h2>
        
        <p style={{ fontSize: '1.1rem', color: '#94a3b8', lineHeight: 1.6, marginBottom: '3rem', maxWidth: '600px' }}>
          Synapse AI learns your campaign objectives, communicates naturally, engages leads seamlessly, and converts prospects automatically before a human even has to react.
        </p>

        <div style={{ display: 'flex', gap: '24px', marginBottom: '4rem' }}>
          <Link href="/dashboard" className="btn-primary" style={{ display: 'flex', alignItems: 'center', gap: '8px', padding: '16px 32px', fontSize: '1.1rem', textDecoration: 'none' }}>
            <Activity size={20} /> Deploy AI Agent <ArrowRight size={20} />
          </Link>
          <Link href="/signin" className="btn-outline" style={{ display: 'flex', alignItems: 'center', gap: '8px', padding: '16px 32px', fontSize: '1.1rem', textDecoration: 'none', background: 'transparent' }}>
            <ShieldCheck size={20} /> Sign In to Platform
          </Link>
        </div>

        <div style={{ display: 'flex', gap: '24px', flexWrap: 'wrap', justifyContent: 'center' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px', background: 'white', padding: '10px 20px', borderRadius: '999px', border: '1px solid #e2e8f0', color: '#334155', fontWeight: '600', fontSize: '0.9rem', boxShadow: '0 4px 6px rgba(0,0,0,0.02)' }}>
            <Zap size={16} style={{ color: '#10b981' }} /> &lt;200ms Latency
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px', background: 'white', padding: '10px 20px', borderRadius: '999px', border: '1px solid #e2e8f0', color: '#334155', fontWeight: '600', fontSize: '0.9rem', boxShadow: '0 4px 6px rgba(0,0,0,0.02)' }}>
            <ShieldCheck size={16} style={{ color: '#10b981' }} /> Human-like Flow
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px', background: 'white', padding: '10px 20px', borderRadius: '999px', border: '1px solid #e2e8f0', color: '#334155', fontWeight: '600', fontSize: '0.9rem', boxShadow: '0 4px 6px rgba(0,0,0,0.02)' }}>
            <Activity size={16} style={{ color: '#10b981' }} /> 24/7 Availability
          </div>
        </div>
      </main>

      {/* Dashboard Peek */}
      <div id="dashboard-preview" className="glass-panel" style={{ width: '100%', maxWidth: '900px', height: '300px', marginTop: '4rem', borderBottomLeftRadius: 0, borderBottomRightRadius: 0, borderBottom: 'none', position: 'relative', overflow: 'hidden' }}>
        <div style={{ display: 'flex', gap: '8px', marginBottom: '24px' }}>
          <div style={{ width: '12px', height: '12px', borderRadius: '50%', background: '#ef4444' }}></div>
          <div style={{ width: '12px', height: '12px', borderRadius: '50%', background: '#f59e0b' }}></div>
          <div style={{ width: '12px', height: '12px', borderRadius: '50%', background: '#10b981' }}></div>
          <span style={{ fontSize: '0.75rem', color: '#94a3b8', marginLeft: '16px', fontWeight: '600', fontFamily: 'monospace' }}>SYNAPSE Dashboard - Live</span>
        </div>
        
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '16px' }}>
          <div style={{ background: '#f8fafc', padding: '24px', borderRadius: '12px', textAlign: 'center' }}>
            <p style={{ fontSize: '2rem', fontWeight: '800', color: '#ef4444', margin: 0 }}>4</p>
            <p style={{ fontSize: '0.75rem', fontWeight: 'bold', color: '#64748b', textTransform: 'uppercase', marginTop: '8px' }}>Critical</p>
          </div>
          <div style={{ background: '#f8fafc', padding: '24px', borderRadius: '12px', textAlign: 'center' }}>
            <p style={{ fontSize: '2rem', fontWeight: '800', color: '#f59e0b', margin: 0 }}>12</p>
            <p style={{ fontSize: '0.75rem', fontWeight: 'bold', color: '#64748b', textTransform: 'uppercase', marginTop: '8px' }}>Alerts</p>
          </div>
          <div style={{ background: '#f8fafc', padding: '24px', borderRadius: '12px', textAlign: 'center' }}>
            <p style={{ fontSize: '2rem', fontWeight: '800', color: '#10b981', margin: 0 }}>847</p>
            <p style={{ fontSize: '0.75rem', fontWeight: 'bold', color: '#64748b', textTransform: 'uppercase', marginTop: '8px' }}>Calls Handled</p>
          </div>
        </div>
      </div>
    </div>
  );
}
