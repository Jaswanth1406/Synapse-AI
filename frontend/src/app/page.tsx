'use client';

import { ArrowRight, Shield, Activity, Clock, ShieldCheck, Zap, Terminal, Database } from 'lucide-react';
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
          <a href="#how-it-works" style={{ color: '#64748b', fontWeight: '500', transition: 'color 0.2s', fontSize: '0.95rem', textDecoration: 'none' }}>How it Works</a>
          <a href="#capabilities" style={{ color: '#64748b', fontWeight: '500', transition: 'color 0.2s', fontSize: '0.95rem', textDecoration: 'none' }}>Capabilities</a>
        </nav>
        
        <div style={{ display: 'flex', alignItems: 'center', gap: '24px' }}>
          <Link href="/signin" style={{ color: '#0f172a', fontWeight: '600', fontSize: '0.95rem', textDecoration: 'none' }}>Sign In</Link>
          <Link href="/signup" className="btn-primary" style={{ textDecoration: 'none' }}>Sign Up</Link>
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

        <div style={{ display: 'flex', gap: '24px', marginBottom: '4rem', justifyContent: 'center' }}>
          <Link href="/signin" className="btn-primary" style={{ display: 'flex', alignItems: 'center', gap: '8px', padding: '16px 32px', fontSize: '1.1rem', textDecoration: 'none' }}>
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

      {/* How it Works Section */}
      <section id="how-it-works" style={{ width: '100%', maxWidth: '1000px', marginTop: '6rem', paddingTop: '4rem', paddingBottom: '2rem', textAlign: 'center' }}>
          <h2 style={{ fontSize: '2.5rem', fontWeight: '800', marginBottom: '1rem', color: '#0f172a' }}>How It Works</h2>
          <p style={{ color: '#64748b', marginBottom: '3rem', fontSize: '1.1rem', maxWidth: '600px', margin: '0 auto 3rem auto' }}>Our AI agent handles the entire lifecycle of an outbound prospect call.</p>
          
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '32px', textAlign: 'left' }}>
              <div style={{ background: 'white', padding: '32px', borderRadius: '16px', border: '1px solid #e2e8f0', boxShadow: '0 4px 6px rgba(0,0,0,0.02)' }}>
                  <div style={{ background: '#ecfdf5', width: '48px', height: '48px', borderRadius: '12px', display: 'flex', alignItems: 'center', justifyContent: 'center', marginBottom: '24px', color: '#10b981', fontWeight: 'bold', fontSize: '1.5rem' }}>1</div>
                  <h3 style={{ fontSize: '1.25rem', fontWeight: '700', marginBottom: '12px', color: '#1e293b' }}>Trigger Webhook</h3>
                  <p style={{ color: '#64748b', lineHeight: 1.6 }}>Send a lead's info via our API or Dashboard to instantly dispatch a call using Dograh AI infrastructure.</p>
              </div>
              <div style={{ background: 'white', padding: '32px', borderRadius: '16px', border: '1px solid #e2e8f0', boxShadow: '0 4px 6px rgba(0,0,0,0.02)' }}>
                  <div style={{ background: '#ecfdf5', width: '48px', height: '48px', borderRadius: '12px', display: 'flex', alignItems: 'center', justifyContent: 'center', marginBottom: '24px', color: '#10b981', fontWeight: 'bold', fontSize: '1.5rem' }}>2</div>
                  <h3 style={{ fontSize: '1.25rem', fontWeight: '700', marginBottom: '12px', color: '#1e293b' }}>AI Conversation</h3>
                  <p style={{ color: '#64748b', lineHeight: 1.6 }}>The agent holds a natural, ultra-low latency conversation, answering questions and handling objections dynamically.</p>
              </div>
              <div style={{ background: 'white', padding: '32px', borderRadius: '16px', border: '1px solid #e2e8f0', boxShadow: '0 4px 6px rgba(0,0,0,0.02)' }}>
                  <div style={{ background: '#ecfdf5', width: '48px', height: '48px', borderRadius: '12px', display: 'flex', alignItems: 'center', justifyContent: 'center', marginBottom: '24px', color: '#10b981', fontWeight: 'bold', fontSize: '1.5rem' }}>3</div>
                  <h3 style={{ fontSize: '1.25rem', fontWeight: '700', marginBottom: '12px', color: '#1e293b' }}>Analytics Sync</h3>
                  <p style={{ color: '#64748b', lineHeight: 1.6 }}>Variables like Sentiment, Intent, and Budget are extracted and synced securely to your Neon PostgreSQL database.</p>
              </div>
          </div>
      </section>

      {/* Capabilities Section */}
      <section id="capabilities" style={{ width: '100%', maxWidth: '1000px', marginTop: '6rem', paddingTop: '4rem', paddingBottom: '6rem', textAlign: 'center' }}>
          <h2 style={{ fontSize: '2.5rem', fontWeight: '800', marginBottom: '1rem', color: '#0f172a' }}>Capabilities</h2>
          <p style={{ color: '#64748b', marginBottom: '3rem', fontSize: '1.1rem', maxWidth: '600px', margin: '0 auto 3rem auto' }}>Built for scale and customized for rigorous enterprise environments.</p>
          
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: '16px', textAlign: 'left' }}>
              <div style={{ background: '#f8fafc', padding: '24px', borderRadius: '12px', display: 'flex', alignItems: 'center', gap: '16px' }}>
                  <Activity size={24} style={{ color: '#10b981' }} />
                  <div><strong style={{ display: 'block', color: '#1e293b' }}>Real-time Dashboarding</strong><span style={{ fontSize: '0.9rem', color: '#64748b' }}>Monitor calls actively.</span></div>
              </div>
              <div style={{ background: '#f8fafc', padding: '24px', borderRadius: '12px', display: 'flex', alignItems: 'center', gap: '16px' }}>
                  <Shield style={{ color: '#10b981' }} />
                  <div><strong style={{ display: 'block', color: '#1e293b' }}>Secure Webhooks</strong><span style={{ fontSize: '0.9rem', color: '#64748b' }}>Bank-grade encryption endpoint.</span></div>
              </div>
              <div style={{ background: '#f8fafc', padding: '24px', borderRadius: '12px', display: 'flex', alignItems: 'center', gap: '16px' }}>
                  <Database style={{ color: '#10b981' }} />
                  <div><strong style={{ display: 'block', color: '#1e293b' }}>Neon Serverless DB</strong><span style={{ fontSize: '0.9rem', color: '#64748b' }}>Infinite scaling for transcripts.</span></div>
              </div>
              <div style={{ background: '#f8fafc', padding: '24px', borderRadius: '12px', display: 'flex', alignItems: 'center', gap: '16px' }}>
                  <Terminal style={{ color: '#10b981' }} />
                  <div><strong style={{ display: 'block', color: '#1e293b' }}>Extensive APIs</strong><span style={{ fontSize: '0.9rem', color: '#64748b' }}>RESTful endpoints for custom CRM.</span></div>
              </div>
          </div>
      </section>
      
      <style dangerouslySetInnerHTML={{__html: `
        html { scroll-behavior: smooth; }
      `}} />
    </div>
  );
}
