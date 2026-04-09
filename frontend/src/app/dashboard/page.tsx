'use client';

import { useState, useRef } from 'react';
import { Shield, Activity, Clock, Search, Bell, Settings, LayoutDashboard, AlertCircle, BookOpen, LogOut, Terminal, Database, Zap, UploadCloud, Server } from 'lucide-react';

export default function Dashboard() {
  const [activeView, setActiveView] = useState('dashboard');
  const [phoneNumber, setPhoneNumber] = useState('');
  const [isCalling, setIsCalling] = useState(false);
  const [callStatus, setCallStatus] = useState('Idle');
  
  // Knowledge Base State
  const [uploadStatus, setUploadStatus] = useState('');
  
  // Health State
  const [healthStatus, setHealthStatus] = useState<any>(null);

  const handleCall = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!phoneNumber) return;

    setIsCalling(true);
    setCallStatus('Scheduling Call to ' + phoneNumber + ' via Backend...');
    
    try {
      const response = await fetch('http://localhost:8000/api/calls/schedule', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ 
            phone_number: phoneNumber, 
            scheduled_time: new Date().toISOString(),
            language: 'english',
            retry_count: 3
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

  const handleFileUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
      const file = e.target.files?.[0];
      if (!file) return;

      setUploadStatus('Uploading...');
      const formData = new FormData();
      formData.append('file', file);

      try {
          const response = await fetch('http://localhost:8000/api/knowledge/upload', {
              method: 'POST',
              body: formData
          });
          if (response.ok) {
              const data = await response.json();
              setUploadStatus(`Success: ${data.message}`);
          } else {
              setUploadStatus('Failed to upload file.');
          }
      } catch (err: any) {
          setUploadStatus(`Error: ${err.message}`);
      }
  };

  const checkHealth = async () => {
      try {
          const response = await fetch('http://localhost:8000/health');
          if (response.ok) {
              const data = await response.json();
              setHealthStatus(data);
          } else {
              setHealthStatus({ error: 'Backend returned an error' });
          }
      } catch (err: any) {
          setHealthStatus({ error: `Backend unavailable: ${err.message}` });
      }
  };

  const NavItem = ({ id, icon: Icon, label }: { id: string, icon: any, label: string }) => {
      const isActive = activeView === id;
      return (
        <button 
            onClick={() => setActiveView(id)} 
            style={{ 
                display: 'flex', alignItems: 'center', gap: '12px', padding: '10px 12px', 
                background: isActive ? '#ecfdf5' : 'transparent', 
                color: isActive ? '#059669' : '#64748b', 
                borderRadius: '8px', fontWeight: isActive ? '600' : '500',
                border: 'none', cursor: 'pointer', textAlign: 'left', width: '100%', transition: 'all 0.2s'
            }}
            className="hover-lift"
        >
            <Icon size={18} /> {label}
        </button>
      );
  };

  return (
    <div className="grid-dashboard" style={{ background: '#f6fcfc' }}>
      <aside className="dashboard-sidebar" style={{ display: 'flex', flexDirection: 'column' }}>
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
          <NavItem id="knowledge" icon={BookOpen} label="Knowledge Base" />
          <NavItem id="health" icon={Server} label="System Health" />
        </nav>
      </aside>

      <main className="dashboard-main" style={{ maxWidth: '1400px', margin: '0 auto', width: '100%', padding: '32px 40px' }}>
        <header style={{ marginBottom: '2.5rem', display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
          <div>
            <h1 className="animate-fade-in" style={{ fontSize: '2.2rem', marginBottom: '0.5rem', fontWeight: 800, color: '#0f172a', letterSpacing: '-0.5px' }}>
              Welcome back, <span className="text-gradient">Agent</span>
            </h1>
            <p className="text-muted animate-fade-in delay-100" style={{ color: '#64748b', fontSize: '0.95rem', margin: 0 }}>
              Here is what's happening with your AI campaigns today.
            </p>
          </div>
          
          <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
             <button className="btn-primary animate-fade-in delay-100" style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '0.95rem', padding: '10px 24px', borderRadius: '999px' }}>
               + New Campaign
             </button>
             
             {/* Header User Profile & Logout */}
             <div style={{ display: 'flex', alignItems: 'center', gap: '16px', borderLeft: '1px solid #e2e8f0', paddingLeft: '16px', marginLeft: '8px' }}>
               <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                   <div style={{ width: '40px', height: '40px', borderRadius: '50%', background: '#ffedd5', border: '2px solid white', boxShadow: '0 2px 4px rgba(0,0,0,0.05)', display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#c2410c', fontWeight: 'bold', fontSize: '1.1rem' }}>A</div>
                   <div style={{ display: 'flex', flexDirection: 'column' }}>
                       <span style={{ fontSize: '0.95rem', fontWeight: '600', color: '#1e293b', lineHeight: '1.2' }}>Agent Admin</span>
                       <span style={{ fontSize: '0.8rem', color: '#64748b' }}>Workspace Owner</span>
                   </div>
               </div>
               
               <button onClick={() => window.location.href = '/'} style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', width: '36px', height: '36px', borderRadius: '50%', background: '#f8fafc', color: '#ef4444', border: '1px solid #e2e8f0', cursor: 'pointer', transition: 'all 0.2s', padding: 0 }} aria-label="Logout" className="hover-lift" title="Logout">
                   <LogOut size={16} />
               </button>
             </div>
          </div>
        </header>

        {activeView === 'dashboard' && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(12, 1fr)', gap: '1.5rem' }}>
          
          {/* KPI Dashboard Metrics - Spans full row */}
          <section style={{ gridColumn: 'span 12', display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '1.5rem', marginBottom: '0.5rem' }}>
            <div className="glass-panel animate-fade-in delay-200" style={{ borderTop: '4px solid #3b82f6', padding: '20px' }}>
              <h3 style={{ color: '#64748b', fontSize: '0.8rem', textTransform: 'uppercase', marginBottom: '0.5rem', fontWeight: 'bold' }}>Total Calls Made</h3>
              <p style={{ fontSize: '2rem', fontWeight: '800', margin: 0, color: '#0f172a' }}>1,284</p>
            </div>
            <div className="glass-panel animate-fade-in delay-200" style={{ borderTop: '4px solid #10b981', padding: '20px' }}>
              <h3 style={{ color: '#64748b', fontSize: '0.8rem', textTransform: 'uppercase', marginBottom: '0.5rem', fontWeight: 'bold' }}>Positive Intent Rate</h3>
              <p style={{ fontSize: '2rem', fontWeight: '800', margin: 0, color: '#10b981' }}>42.8%</p>
            </div>
            <div className="glass-panel animate-fade-in delay-200" style={{ borderTop: '4px solid #f59e0b', padding: '20px' }}>
              <h3 style={{ color: '#64748b', fontSize: '0.8rem', textTransform: 'uppercase', marginBottom: '0.5rem', fontWeight: 'bold' }}>Active Leads</h3>
              <p style={{ fontSize: '2rem', fontWeight: '800', margin: 0, color: '#f59e0b' }}>312</p>
            </div>
          </section>

          {/* Quick Actions / Activity Feed - Span 12 columns */}
          <section className="glass-panel animate-fade-in delay-300" style={{ gridColumn: 'span 12', padding: '24px' }}>
            <h2 style={{ fontSize: '1.1rem', fontWeight: '700', marginBottom: '1.5rem' }}>Recent Activity</h2>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
              
              <div style={{ display: 'flex', gap: '12px' }}>
                <div style={{ marginTop: '2px', width: '8px', height: '8px', borderRadius: '50%', background: '#10b981' }}></div>
                <div>
                  <p style={{ fontSize: '0.9rem', fontWeight: '600', margin: 0, color: '#334155' }}>Call Completed</p>
                  <p style={{ fontSize: '0.8rem', color: '#64748b', margin: 0 }}>+1 (555) 123-4567 • High Intent</p>
                </div>
              </div>
              
              <div style={{ display: 'flex', gap: '12px' }}>
                <div style={{ marginTop: '2px', width: '8px', height: '8px', borderRadius: '50%', background: '#3b82f6' }}></div>
                <div>
                  <p style={{ fontSize: '0.9rem', fontWeight: '600', margin: 0, color: '#334155' }}>Campaign Started</p>
                  <p style={{ fontSize: '0.8rem', color: '#64748b', margin: 0 }}>Q2 Product Outreach</p>
                </div>
              </div>
              
              <div style={{ display: 'flex', gap: '12px' }}>
                <div style={{ marginTop: '2px', width: '8px', height: '8px', borderRadius: '50%', background: '#f59e0b' }}></div>
                <div>
                  <p style={{ fontSize: '0.9rem', fontWeight: '600', margin: 0, color: '#334155' }}>Follow-up Scheduled</p>
                  <p style={{ fontSize: '0.8rem', color: '#64748b', margin: 0 }}>Sarah Jenkins @ Acme Corp</p>
                </div>
              </div>

            </div>
          </section>

        </div>
        
        {/* Project Documentation Section */}
        <section className="glass-panel animate-fade-in delay-300" style={{ padding: '2rem', marginTop: '1.5rem', marginBottom: '3rem' }}>
          <h2 style={{ fontSize: '1.25rem', fontWeight: '700', marginBottom: '1.5rem', color: '#0f172a', borderBottom: '1px solid #e2e8f0', paddingBottom: '0.75rem', display: 'flex', alignItems: 'center', gap: '8px' }}>
            <Terminal size={20} className="text-primary"/> Project Documentation
          </h2>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))', gap: '2rem' }}>
              <div>
                 <h3 style={{fontSize: '1.1rem', fontWeight: '600', color: '#334155', marginBottom: '1rem', display: 'flex', alignItems: 'center', gap: '6px'}}><Zap size={16} color="#eab308" /> Key Features</h3>
                 <ul style={{ paddingLeft: '1.2rem', color: '#475569', fontSize: '0.95rem', lineHeight: '1.6' }}>
                   <li style={{marginBottom: '8px'}}><strong>Automated Call Triggering</strong>: Initiates calls using the Dograh Cloud via FastAPI.</li>
                   <li style={{marginBottom: '8px'}}><strong>Real-Time Updates</strong>: Receives live statuses and call transcripts via ngrok-exposed webhooks.</li>
                   <li style={{marginBottom: '8px'}}><strong>Scalable Database</strong>: Stores campaign and call logs with Neon Postgres and Prisma ORM.</li>
                   <li style={{marginBottom: '8px'}}><strong>Modern Frontend</strong>: Next.js + React.js UI tailored for dashboard-style analytics and actions.</li>
                 </ul>
              </div>
              <div>
                 <h3 style={{fontSize: '1.1rem', fontWeight: '600', color: '#334155', marginBottom: '1rem', display: 'flex', alignItems: 'center', gap: '6px'}}><Database size={16} color="#3b82f6" /> Tech Stack</h3>
                 <ul style={{ paddingLeft: '1.2rem', color: '#475569', fontSize: '0.95rem', lineHeight: '1.6' }}>
                   <li style={{marginBottom: '8px'}}><strong>Backend</strong>: Python, FastAPI (uvicorn)</li>
                   <li style={{marginBottom: '8px'}}><strong>Frontend</strong>: Next.js, React, TailwindCSS</li>
                   <li style={{marginBottom: '8px'}}><strong>Database</strong>: Neon (Serverless Postgres), Prisma</li>
                   <li style={{marginBottom: '8px'}}><strong>Third-Party</strong>: Dograh Cloud APIs</li>
                   <li style={{marginBottom: '8px'}}><strong>Tunneling</strong>: ngrok (for local webhook testing)</li>
                 </ul>
              </div>
          </div>
          
           <div style={{ marginTop: '2rem' }}>
              <h3 style={{fontSize: '1.1rem', fontWeight: '600', color: '#334155', marginBottom: '1rem', display: 'flex', alignItems: 'center', gap: '6px'}}><Activity size={16} color="#10b981" /> Architecture Flow</h3>
              <div style={{ background: '#f8fafc', padding: '1.5rem', borderRadius: '12px', border: '1px solid #e2e8f0' }}>
                 <ol style={{ paddingLeft: '1.2rem', color: '#475569', fontSize: '0.95rem', lineHeight: '1.7', margin: 0 }}>
                   <li style={{marginBottom: '12px'}}><strong>User Action</strong>: User submits phone number & task via Next.js frontend to FastAPI backend (`/api/calls/schedule`).</li>
                   <li style={{marginBottom: '12px'}}><strong>API Trigger</strong>: FastAPI posts to `api.dograh.com/api/v1/public/agent/{'{id}'}` with agent credentials.</li>
                   <li style={{marginBottom: '12px'}}><strong>Call Execution</strong>: Dograh AI dialer places the outbound call.</li>
                   <li style={{marginBottom: '12px'}}><strong>Webhook Updates</strong>: Dograh sends real-time events (started, ringing, answered, ended, transcripts) to `POST /api/webhooks/dograh`.</li>
                   <li style={{marginBottom: '12px'}}><strong>Data Storage</strong>: Webhook payloads are persisted to the Neon database via Prisma.</li>
                 </ol>
              </div>
           </div>
        </section>
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

        {/* Knowledge Base View */}
        {activeView === 'knowledge' && (
          <section className="glass-panel animate-fade-in delay-200" style={{ padding: '2rem' }}>
             <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '1.5rem' }}>
              <div style={{ background: '#fef08a', padding: '6px', borderRadius: '8px', color: '#854d0e' }}>
                <UploadCloud size={18} />
              </div>
              <h2 style={{ fontSize: '1.25rem', fontWeight: '700', margin: 0 }}>RAG Knowledge Base</h2>
            </div>
            <p style={{ fontSize: '0.9rem', color: '#64748b', marginBottom: '1.5rem' }}>
              Upload FAQs or scripts to build the Knowledge Base. <code>POST /api/knowledge/upload</code>
            </p>
            <div style={{ background: '#f8fafc', padding: '2rem', borderRadius: '12px', border: '2px dashed #cbd5e1', textAlign: 'center', maxWidth: '600px' }}>
                <UploadCloud size={40} color="#94a3b8" style={{ margin: '0 auto 1rem auto' }}/>
                <h3 style={{ fontSize: '1rem', color: '#334155', marginBottom: '0.5rem' }}>Select a PDF or TXT file</h3>
                <input 
                  type="file" 
                  onChange={handleFileUpload} 
                  style={{ marginTop: '1rem' }} 
                  accept=".txt,.pdf"
                />
                
                {uploadStatus && (
                    <div style={{ marginTop: '1.5rem', fontSize: '0.9rem', color: '#059669', background: '#ecfdf5', padding: '8px', borderRadius: '4px' }}>
                        {uploadStatus}
                    </div>
                )}
            </div>
          </section>
        )}

        {/* System Health View */}
        {activeView === 'health' && (
          <section className="glass-panel animate-fade-in delay-200" style={{ padding: '2rem' }}>
             <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '1.5rem' }}>
              <div style={{ background: '#e0f2fe', padding: '6px', borderRadius: '8px', color: '#0284c7' }}>
                <Server size={18} />
              </div>
              <h2 style={{ fontSize: '1.25rem', fontWeight: '700', margin: 0 }}>Backend System Health</h2>
            </div>
            
            <button onClick={checkHealth} className="btn-secondary" style={{ padding: '10px 20px', borderRadius: '8px', marginBottom: '1rem' }}>
               Ping API (/health)
            </button>

            {healthStatus && (
                <div style={{ background: '#1e293b', padding: '1.5rem', borderRadius: '8px', color: '#f8fafc', fontFamily: 'monospace', maxWidth: '600px', whiteSpace: 'pre-wrap' }}>
                    {JSON.stringify(healthStatus, null, 2)}
                </div>
            )}
          </section>
        )}

      </main>
    </div>
  );
}