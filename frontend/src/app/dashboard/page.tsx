'use client';

import { useState, useRef, useEffect } from 'react';
import { Shield, Activity, Clock, Search, Bell, Settings, LayoutDashboard, AlertCircle, BookOpen, LogOut, Terminal, Database, Zap, UploadCloud, Server, PieChart, Moon, Sun } from 'lucide-react';
import { authClient } from '@/lib/auth-client';
import { useRouter } from 'next/navigation';
import AnalyticsView from './AnalyticsView';

export default function Dashboard() {
  const router = useRouter();
  const { data: session, isPending } = authClient.useSession();
  
  const [activeView, setActiveView] = useState('analytics');
  const [isDarkMode, setIsDarkMode] = useState(true);
  const [phoneNumber, setPhoneNumber] = useState('');
  const [isCalling, setIsCalling] = useState(false);
  const [callStatus, setCallStatus] = useState('Idle');
  
  // Knowledge Base State
  const [uploadStatus, setUploadStatus] = useState('');
  
  // Health State
  const [healthStatus, setHealthStatus] = useState<any>(null);

  useEffect(() => {
    if (!isPending && !session) {
      router.push('/signin');
    }
  }, [session, isPending, router]);

  const handleCall = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!phoneNumber) return;

    setIsCalling(true);
    setCallStatus('Dispatched Agent to ' + phoneNumber + ' via Dograh AI...');
    
    try {
      const apiUrl = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8000';
      const response = await fetch(`${apiUrl}/api/calls/trigger`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ 
            phone_number: phoneNumber
        })
      });
      
      if (response.ok) {
        const data = await response.json();
        setCallStatus(`Success: ${data.message}`);
      } else {
        const errText = await response.text();
        setCallStatus(`Failed: ${errText}`);
      }
    } catch (e: any) {
        setCallStatus(`Error connecting to backend: ${e.message}`);
    } finally {
        setTimeout(() => {
          setIsCalling(false);
          setCallStatus('Idle');
        }, 5000);
    }
  };

  const NavItem = ({ id, icon: Icon, label, href }: { id: string, icon: any, label: string, href?: string }) => {
      const isActive = activeView === id && !href;
      
      const content = (
          <>
            <Icon size={18} /> {label}
          </>
      );

      const navStyle = { 
          display: 'flex', alignItems: 'center', gap: '12px', padding: '10px 12px', 
          background: isActive ? '#ecfdf5' : 'transparent', 
          color: isActive ? '#059669' : '#64748b', 
          borderRadius: '8px', fontWeight: isActive ? '600' : 500,
          border: 'none', cursor: 'pointer', textAlign: 'left' as const, width: '100%', 
          transition: 'all 0.2s', textDecoration: 'none'
      };

      if (href) {
          return <a href={href} target="_blank" rel="noopener noreferrer" style={navStyle} className="hover-lift">{content}</a>;
      }

      return (
        <button onClick={() => setActiveView(id)} style={navStyle} className="hover-lift">
            {content}
        </button>
      );
  };

  if (isPending) {
    return <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '100vh', background: '#f6fcfc', color: '#0f172a' }}>Verifying Session...</div>;
  }
  
  if (!session) {
    return null; // Don't render until redirected to signin
  }

  return (
    <div className="grid-dashboard" style={{ background: isDarkMode ? '#09090b' : '#f6fcfc', transition: 'background 0.3s' }}>
      <aside className="dashboard-sidebar" style={{ display: 'flex', flexDirection: 'column', background: isDarkMode ? '#18181b' : 'white', borderRight: `1px solid ${isDarkMode ? '#27272a' : '#e2e8f0'}` }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '2rem' }}>
          <div style={{ background: '#10b981', padding: '6px', borderRadius: '8px', color: 'white' }}>
            <Shield size={20} />
          </div>
          <h2 style={{ fontSize: '1.2rem', fontWeight: '800', letterSpacing: '-0.5px' }}>SYNAPSE <span style={{ fontWeight: '400' }}>AI</span></h2>
        </div>
        
        <p style={{ fontSize: '0.75rem', fontWeight: 'bold', color: '#94a3b8', letterSpacing: '1px', marginBottom: '1rem' }}>NAVIGATION</p>
        <nav style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem', flex: 1 }}>
          <NavItem id="dashboard" icon={LayoutDashboard} label="Dashboard" />
          <NavItem id="trigger" icon={Activity} label="Trigger Call" />
        </nav>
      </aside>

      <main className="dashboard-main" style={{ maxWidth: '1400px', margin: '0 auto', width: '100%', padding: '32px 40px' }}>
        <header style={{ marginBottom: '2.5rem', display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
          <div>
            <h1 className="animate-fade-in" style={{ fontSize: '2.2rem', marginBottom: '0.5rem', fontWeight: 800, color: isDarkMode ? 'white' : '#0f172a', letterSpacing: '-0.5px' }}>
              Welcome back, <span className="text-gradient">Agent</span>
            </h1>
            <p className="text-muted animate-fade-in delay-100" style={{ color: '#64748b', fontSize: '0.95rem', margin: 0 }}>
              Here is what's happening with your AI campaigns today.
            </p>
          </div>
          
          <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
             <button 
               onClick={() => setIsDarkMode(!isDarkMode)} 
               style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', width: '36px', height: '36px', borderRadius: '50%', background: isDarkMode ? '#27272a' : '#f8fafc', color: isDarkMode ? '#f4f4f5' : '#334155', border: `1px solid ${isDarkMode ? '#3f3f46' : '#e2e8f0'}`, cursor: 'pointer', transition: 'all 0.2s', padding: 0 }} 
               className="hover-lift" title="Toggle Theme"
             >
               {isDarkMode ? <Sun size={18} /> : <Moon size={18} />}
             </button>
             
             <a href="https://app.dograh.com/files" target="_blank" rel="noopener noreferrer" style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '0.95rem', padding: '10px 24px', borderRadius: '999px', background: isDarkMode ? '#27272a' : '#e2e8f0', color: isDarkMode ? '#f8fafc' : '#0f172a', textDecoration: 'none', fontWeight: '500', transition: 'all 0.2s' }} className="hover-lift">
               <UploadCloud size={16} /> Knowledge Base
             </a>
             <a href="https://app.dograh.com/campaigns" target="_blank" rel="noopener noreferrer" className="btn-primary" style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '0.95rem', padding: '10px 24px', borderRadius: '999px', textDecoration: 'none' }}>
               + New Campaign
             </a>
             
             {/* Header User Profile & Logout */}
             <div style={{ display: 'flex', alignItems: 'center', gap: '16px', borderLeft: '1px solid #e2e8f0', paddingLeft: '16px', marginLeft: '8px' }}>
               <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                   <div style={{ width: '40px', height: '40px', borderRadius: '50%', background: '#ffedd5', border: '2px solid white', boxShadow: '0 2px 4px rgba(0,0,0,0.05)', display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#c2410c', fontWeight: 'bold', fontSize: '1.1rem' }}>
                       {session?.user?.name ? session.user.name.charAt(0).toUpperCase() : 'U'}
                   </div>
                   <div style={{ display: 'flex', flexDirection: 'column' }}>
                       <span style={{ fontSize: '0.95rem', fontWeight: '600', color: isDarkMode ? 'white' : '#1e293b', lineHeight: '1.2' }}>{session?.user?.name || 'User'}</span>
                       <span style={{ fontSize: '0.8rem', color: '#64748b' }}>Workspace Owner</span>
                   </div>
               </div>
               
               <button 
                  onClick={async () => {
                      await authClient.signOut();
                      window.location.href = '/';
                  }} 
                  style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', width: '36px', height: '36px', borderRadius: '50%', background: '#f8fafc', color: '#ef4444', border: '1px solid #e2e8f0', cursor: 'pointer', transition: 'all 0.2s', padding: 0 }} 
                  aria-label="Logout" className="hover-lift" title="Logout"
               >
                   <LogOut size={16} />
               </button>
             </div>
          </div>
        </header>

        {/* Primary Dashboard / Analytics View */}
        {(activeView === 'dashboard' || activeView === 'analytics') && (
           <div className="animate-fade-in delay-100">
               <AnalyticsView isDarkMode={isDarkMode} />
           </div>
        )}

        {/* Trigger Call View */}
        {activeView === 'trigger' && (
          <section className="glass-panel animate-fade-in delay-200" style={{ padding: '2rem' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '1.5rem' }}>
              <div style={{ background: '#e0e7ff', padding: '6px', borderRadius: '8px', color: '#4f46e5' }}>
                <Search size={18} />
              </div>
              <h2 style={{ fontSize: '1.25rem', fontWeight: '700', margin: 0 }}>Schedule an AI Call</h2>
            </div>
            
            <p style={{ fontSize: '0.9rem', color: '#64748b', marginBottom: '1.5rem' }}>
              Route a direct outbound call via your FastAPI backend. This invokes <code>POST /api/calls/schedule</code> securely.
            </p>
            
            <form onSubmit={handleCall} style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem', background: '#f8fafc', padding: '24px', borderRadius: '12px', border: '1px solid #e2e8f0', maxWidth: '600px' }}>
              <div>
                <label style={{ display: 'block', marginBottom: '0.5rem', color: '#334155', fontWeight: '600', fontSize: '0.9rem' }}>Target Phone Number</label>
                <input 
                  type="tel" 
                  placeholder="+1 (555) 000-0000" 
                  value={phoneNumber}
                  onChange={(e) => setPhoneNumber(e.target.value)}
                  style={{ 
                    width: '100%', 
                    padding: '12px 14px', 
                    borderRadius: '8px', 
                    border: '1px solid #cbd5e1',
                    background: 'white',
                    color: '#0f172a',
                    fontFamily: 'inherit',
                    fontSize: '0.95rem',
                    outline: 'none',
                    boxShadow: 'inset 0 1px 2px rgba(0,0,0,0.05)'
                  }}
                />
              </div>
              <button type="submit" disabled={isCalling} className="btn-primary" style={{ opacity: isCalling ? 0.7 : 1, padding: '12px 20px', borderRadius: '8px', fontSize: '1rem', fontWeight: '600' }}>
                {isCalling ? 'Sending Request...' : 'Schedule Call Now'}
              </button>
            </form>
            
            {callStatus !== 'Idle' && (
              <div style={{ marginTop: '1.5rem', padding: '12px 16px', background: '#ecfdf5', borderRadius: '8px', border: '1px solid #a7f3d0', maxWidth: '600px' }}>
                <p style={{ display: 'flex', alignItems: 'center', gap: '8px', color: '#065f46', margin: 0, fontWeight: '600', fontSize: '0.9rem' }}>
                  <span style={{ 
                    display: 'inline-block', 
                    width: '8px', 
                    height: '8px', 
                    borderRadius: '50%', 
                    background: isCalling ? '#f59e0b' : '#10b981',
                    boxShadow: `0 0 10px ${isCalling ? '#f59e0b' : '#10b981'}`
                  }}></span>
                  {callStatus}
                </p>
              </div>
            )}
          </section>
        )}



      </main>
    </div>
  );
}