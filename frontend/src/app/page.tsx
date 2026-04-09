'use client';

import { useState } from 'react';

export default function Home() {
  const [phoneNumber, setPhoneNumber] = useState('');
  const [isCalling, setIsCalling] = useState(false);
  const [callStatus, setCallStatus] = useState('Idle');

  const handleCall = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!phoneNumber) return;

    setIsCalling(true);
    setCallStatus('Initiating Call to ' + phoneNumber + '...');
    // Real Dograh API Call
    try {
      const agentId = process.env.NEXT_PUBLIC_DOGRAH_AGENT_ID || "af96de66-753e-4201-b166-ce5eccab3951";
      const apiKey = process.env.NEXT_PUBLIC_DOGRAH_API_KEY || "";
      
      const response = await fetch(`https://api.dograh.com/api/v1/public/agent/${agentId}`, {
        method: 'POST',
        headers: {
          'X-API-Key': apiKey,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ phone_number: phoneNumber, initial_context: {} })
      });
      
      if (response.ok) {
        setCallStatus('Call Connected. AI is attempting to dial...');
      } else {
        const errText = await response.text();
        setCallStatus(`Failed: ${errText}`);
      }
    } catch (e: any) {
        setCallStatus(`Error: ${e.message}`);
    } finally {
        setTimeout(() => {
          setIsCalling(false);
          setCallStatus('Idle');
        }, 5000);
    }
  };

  return (
    <div className="grid-dashboard">
      <aside className="dashboard-sidebar">
        <div>
          <h2 className="text-gradient" style={{ fontSize: '1.5rem', marginBottom: '2rem' }}>Vedaspark AI</h2>
        </div>
        <nav style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
          <a href="#" style={{ color: 'var(--primary)', textDecoration: 'none', fontWeight: '500' }}>Dashboard</a>
          <a href="#" style={{ color: 'var(--text-muted)', textDecoration: 'none' }}>Campaigns</a>
          <a href="#" style={{ color: 'var(--text-muted)', textDecoration: 'none' }}>Contacts</a>
          <a href="#" style={{ color: 'var(--text-muted)', textDecoration: 'none' }}>Settings</a>
        </nav>
      </aside>

      <main className="dashboard-main">
        <header style={{ marginBottom: '3rem', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <div>
            <h1 className="animate-fade-in" style={{ fontSize: '2.5rem', marginBottom: '0.5rem' }}>
              Welcome back, <span className="text-gradient">Agent</span>
            </h1>
            <p className="text-muted animate-fade-in delay-100" style={{ color: 'var(--text-muted)' }}>
              Here is what's happening with your AI campaigns today.
            </p>
          </div>
          <button className="btn-primary animate-fade-in delay-100">
            + New Campaign
          </button>
        </header>

        <section style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))', gap: '2rem', marginBottom: '3rem' }}>
          <div className="glass-panel animate-fade-in delay-200">
            <h3 style={{ color: 'var(--text-muted)', fontSize: '0.9rem', textTransform: 'uppercase', marginBottom: '0.5rem' }}>Total Calls Made</h3>
            <p style={{ fontSize: '2rem', fontWeight: '700', fontFamily: 'Outfit' }}>1,284</p>
          </div>
          <div className="glass-panel animate-fade-in delay-200">
            <h3 style={{ color: 'var(--text-muted)', fontSize: '0.9rem', textTransform: 'uppercase', marginBottom: '0.5rem' }}>Positive Intent Rate</h3>
            <p style={{ fontSize: '2rem', fontWeight: '700', color: '#10b981', fontFamily: 'Outfit' }}>42.8%</p>
          </div>
          <div className="glass-panel animate-fade-in delay-200">
            <h3 style={{ color: 'var(--text-muted)', fontSize: '0.9rem', textTransform: 'uppercase', marginBottom: '0.5rem' }}>Active Leads</h3>
            <p style={{ fontSize: '2rem', fontWeight: '700', color: 'var(--secondary)', fontFamily: 'Outfit' }}>312</p>
          </div>
        </section>

        <section className="glass-panel animate-fade-in delay-300">
          <h2 style={{ marginBottom: '1.5rem' }}>Trigger Manual AI Call (Dograh Cloud)</h2>
          <form onSubmit={handleCall} style={{ display: 'flex', gap: '1rem', alignItems: 'flex-end' }}>
            <div style={{ flex: 1 }}>
              <label style={{ display: 'block', marginBottom: '0.5rem', color: 'var(--text-muted)' }}>Phone Number</label>
              <input 
                type="tel" 
                placeholder="+1 (555) 000-0000" 
                value={phoneNumber}
                onChange={(e) => setPhoneNumber(e.target.value)}
                style={{ 
                  width: '100%', 
                  padding: '12px 16px', 
                  borderRadius: '8px', 
                  border: '1px solid var(--glass-border)',
                  background: 'rgba(0,0,0,0.2)',
                  color: 'white',
                  fontFamily: 'Inter',
                  fontSize: '1rem',
                  outline: 'none'
                }}
              />
            </div>
            <button type="submit" disabled={isCalling} className="btn-primary" style={{ opacity: isCalling ? 0.7 : 1 }}>
              {isCalling ? 'Calling...' : 'Dial Now'}
            </button>
          </form>
          
          {callStatus !== 'Idle' && (
            <div style={{ marginTop: '1.5rem', padding: '1rem', background: 'rgba(139, 92, 246, 0.1)', borderRadius: '8px', border: '1px solid rgba(139, 92, 246, 0.3)' }}>
              <p style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                <span style={{ 
                  display: 'inline-block', 
                  width: '8px', 
                  height: '8px', 
                  borderRadius: '50%', 
                  background: isCalling ? 'var(--secondary)' : '#10b981',
                  boxShadow: `0 0 10px ${isCalling ? 'var(--secondary)' : '#10b981'}`
                }}></span>
                {callStatus}
              </p>
            </div>
          )}
        </section>
      </main>
    </div>
  );
}
