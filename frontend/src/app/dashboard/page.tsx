'use client';

import { useState, useRef, useEffect } from 'react';
import { Shield, Activity, Clock, Search, Bell, Settings, LayoutDashboard, AlertCircle, BookOpen, LogOut, Terminal, Database, Zap, UploadCloud, Server, PieChart, Moon, Sun, Download, FileText, X, Users, CalendarClock, Trash2, Headphones } from 'lucide-react';
import { authClient } from '@/lib/auth-client';
import { useRouter } from 'next/navigation';
import AnalyticsView from './AnalyticsView';

export default function Dashboard() {
  const router = useRouter();
  const { data: session, isPending } = authClient.useSession();
  
  const [activeView, setActiveView] = useState('analytics');
  const [isDarkMode, setIsDarkMode] = useState(true);
  const [phoneNumber, setPhoneNumber] = useState('');
  const [leadName, setLeadName] = useState('');
  const [isCalling, setIsCalling] = useState(false);
  const [callStatus, setCallStatus] = useState('Idle');

  // Scheduled Calls State
  const [schedPhone, setSchedPhone] = useState('');
  const [schedTime, setSchedTime] = useState('');
  const [schedRetryCount, setSchedRetryCount] = useState(3);
  const [scheduledCalls, setScheduledCalls] = useState<any[]>([]);
  const [isScheduling, setIsScheduling] = useState(false);
  const [schedStatus, setSchedStatus] = useState('');

  // Helper to get effective User ID (URL param override or session)
  const getEffectiveUserId = () => {
    if (typeof window !== 'undefined') {
      const params = new URLSearchParams(window.location.search);
      const override = params.get('user_id');
      if (override) return override;
    }
    return session?.user?.id || 'anonymous';
  };

  const fetchScheduledCalls = async () => {
    try {
      const apiUrl = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8000';
      const res = await fetch(`${apiUrl}/api/calls/scheduled`, {
        headers: { 'X-User-ID': getEffectiveUserId() }
      });
      if (res.ok) setScheduledCalls(await res.json());
    } catch {}
  };

  const handleScheduleCall = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!schedPhone || !schedTime) return;
    setIsScheduling(true);
    setSchedStatus('Scheduling...');
    try {
      const apiUrl = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8000';
      const res = await fetch(`${apiUrl}/api/calls/schedule`, {
        method: 'POST',
        headers: { 
          'Content-Type': 'application/json',
          'X-User-ID': getEffectiveUserId()
        },
        body: JSON.stringify({ phone_number: schedPhone, scheduled_time: schedTime, retry_count: schedRetryCount })
      });
      if (res.ok) {
        const data = await res.json();
        setSchedStatus(`✓ ${data.message}`);
        setSchedPhone('');
        setSchedTime('');
        fetchScheduledCalls();
      } else {
        setSchedStatus('Failed to schedule call.');
      }
    } catch (err: any) {
      setSchedStatus(`Error: ${err.message}`);
    } finally {
      setIsScheduling(false);
      setTimeout(() => setSchedStatus(''), 6000);
    }
  };

  const handleCancelSchedule = async (id: string) => {
    const apiUrl = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8000';
    await fetch(`${apiUrl}/api/calls/scheduled/${id}`, { 
      method: 'DELETE',
      headers: { 'X-User-ID': getEffectiveUserId() }
    });
    fetchScheduledCalls();
  };

  // Trigger History State
  const [recentCalls, setRecentCalls] = useState<any[]>([]);
  const [selectedTranscript, setSelectedTranscript] = useState<any>(null);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [selectedSummary, setSelectedSummary] = useState<any>(null);
  const [isSummaryModalOpen, setIsSummaryModalOpen] = useState(false);
  
  // Leads Management State
  const [newLeadFn, setNewLeadFn] = useState('');
  const [newLeadLn, setNewLeadLn] = useState('');
  const [newLeadPhone, setNewLeadPhone] = useState('');
  const [isCreatingLead, setIsCreatingLead] = useState(false);
  const [leadStatusMessage, setLeadStatusMessage] = useState('');
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  
  const handleCreateLead = async (e?: React.FormEvent, leadsBatch?: any[]) => {
      if (e) e.preventDefault();
      
      const payloadLeads = leadsBatch || [{
          firstName: newLeadFn,
          lastName: newLeadLn,
          phoneNumber: newLeadPhone
      }];
      
      if (!payloadLeads[0].phoneNumber) return;
      
      setIsCreatingLead(true);
      setLeadStatusMessage(`Pushing ${payloadLeads.length} lead(s) to EspoCRM...`);
      
      try {
          const apiUrl = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8000';
          const response = await fetch(`${apiUrl}/api/leads/create`, {
              method: 'POST',
              headers: { 
                  'Content-Type': 'application/json',
                  'X-User-ID': getEffectiveUserId()
              },
              body: JSON.stringify({ leads: payloadLeads })
          });
          
          if (response.ok) {
              const data = await response.json();
              setLeadStatusMessage(`Success: Created ${data.successful} leads.`);
              if (!leadsBatch) {
                  setNewLeadFn(''); setNewLeadLn(''); setNewLeadPhone('');
              }
          } else {
              setLeadStatusMessage(`Failed to create leads. Error ${response.status}`);
          }
      } catch (err: any) {
          setLeadStatusMessage(`Error: ${err.message}`);
      } finally {
          setIsCreatingLead(false);
          setTimeout(() => setLeadStatusMessage(''), 5000);
      }
  };
  
  const processCSV = () => {
      if (!selectedFile) return;
      
      const reader = new FileReader();
      reader.onload = (event) => {
          const text = event.target?.result as string;
          // Extremely basic CSV parser assuming header: firstname,lastname,mobile_no
          const lines = text.split(/\r?\n/).filter(l => l.trim() !== '');
          if (lines.length <= 1) {
              setLeadStatusMessage('No data found in CSV.');
              return;
          }
          
          // Assume columns: [0] firstName, [1] lastName, [2] mobile_no based on prompt
          const uploadedLeads = [];
          for (let i = 1; i < lines.length; i++) {
              const columns = lines[i].split(',');
              if (columns.length >= 3) {
                  const firstName = columns[0].trim();
                  const lastName = columns[1].trim();
                  const phoneNumber = columns[2].trim();
                  
                  // Only add if at least a phone number is present
                  if (phoneNumber) {
                      uploadedLeads.push({
                          firstName: firstName || 'Unknown',
                          lastName: lastName,
                          phoneNumber: phoneNumber
                      });
                  }
              }
          }
          
          if (uploadedLeads.length > 0) {
              handleCreateLead(undefined, uploadedLeads);
              setSelectedFile(null); // Reset after upload
              const input = document.getElementById('csv-upload') as HTMLInputElement;
              if (input) input.value = '';
          } else {
              setLeadStatusMessage('No valid leads found in CSV.');
          }
      };
      reader.readAsText(selectedFile);
  };
  
  useEffect(() => {
    if (!isPending && !session) {
      router.push('/signin');
    }
  }, [session, isPending, router]);

  useEffect(() => {
    if (activeView === 'trigger') {
      const fetchHistory = async () => {
        try {
          const apiUrl = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8000';
          const response = await fetch(`${apiUrl}/api/calls/history`, {
              headers: { 'X-User-ID': getEffectiveUserId() }
          });
          if (response.ok) {
             const data = await response.json();
             setRecentCalls(data);
          }
        } catch (e) {
          console.error('Failed to fetch history', e);
        }
      };
      fetchHistory();
    }
  }, [activeView, callStatus]);

  const downloadTranscriptFile = (transcript: string, callId: string) => {
    const blob = new Blob([transcript], { type: 'text/plain' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `transcript-${callId}.txt`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
  };

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
          'Content-Type': 'application/json',
          'X-User-ID': getEffectiveUserId()
        },
        body: JSON.stringify({ 
            phone_number: phoneNumber,
            lead_name: leadName || undefined
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
          <NavItem id="schedule" icon={CalendarClock} label="Scheduled Calls" />
          <NavItem id="leads" icon={Users} label="Manage Leads" />
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
               <AnalyticsView isDarkMode={isDarkMode} session={{...session, user: {...session?.user, id: getEffectiveUserId()}}} />
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
              Route a direct outbound call via your FastAPI backend. This invokes <code>POST /api/calls/trigger</code> securely.
            </p>
            
            <form onSubmit={handleCall} style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem', background: isDarkMode ? '#1e293b' : '#f8fafc', padding: '24px', borderRadius: '12px', border: `1px solid ${isDarkMode ? '#334155' : '#e2e8f0'}`, maxWidth: '600px' }}>
              <div>
                <label style={{ display: 'block', marginBottom: '0.5rem', color: isDarkMode ? '#e2e8f0' : '#334155', fontWeight: '600', fontSize: '0.9rem' }}>Target Phone Number</label>
                <input 
                  type="tel" 
                  placeholder="+91 98765 43210" 
                  value={phoneNumber}
                  onChange={(e) => setPhoneNumber(e.target.value)}
                  style={{ 
                    width: '100%', 
                    padding: '12px 14px', 
                    borderRadius: '8px', 
                    border: `1px solid ${isDarkMode ? '#475569' : '#cbd5e1'}`,
                    background: isDarkMode ? '#0f172a' : 'white',
                    color: isDarkMode ? '#f8fafc' : '#0f172a',
                    fontFamily: 'inherit',
                    fontSize: '0.95rem',
                    outline: 'none',
                    boxSizing: 'border-box'
                  }}
                />
              </div>

              <div>
                <label style={{ display: 'block', marginBottom: '0.5rem', color: isDarkMode ? '#e2e8f0' : '#334155', fontWeight: '600', fontSize: '0.9rem' }}>Lead Name (Optional)</label>
                <input 
                  type="text" 
                  placeholder="e.g. Sanjay" 
                  value={leadName}
                  onChange={(e) => setLeadName(e.target.value)}
                  style={{ 
                    width: '100%', 
                    padding: '12px 14px', 
                    borderRadius: '8px', 
                    border: `1px solid ${isDarkMode ? '#475569' : '#cbd5e1'}`,
                    background: isDarkMode ? '#0f172a' : 'white',
                    color: isDarkMode ? '#f8fafc' : '#0f172a',
                    fontFamily: 'inherit',
                    fontSize: '0.95rem',
                    outline: 'none',
                    boxSizing: 'border-box'
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

            <div style={{ marginTop: '3rem' }}>
                <h3 style={{ fontSize: '1.1rem', fontWeight: '700', marginBottom: '1rem', color: isDarkMode ? '#f8fafc' : '#0f172a' }}>Recent Outbound Calls</h3>
                
                <div style={{ background: isDarkMode ? '#1e293b' : 'white', borderRadius: '12px', border: `1px solid ${isDarkMode ? '#334155' : '#e2e8f0'}`, overflow: 'hidden' }}>
                    <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
                        <thead style={{ background: isDarkMode ? '#0f172a' : '#f8fafc', borderBottom: `1px solid ${isDarkMode ? '#334155' : '#e2e8f0'}` }}>
                            <tr>
                                <th style={{ padding: '12px 16px', fontSize: '0.85rem', color: isDarkMode ? '#94a3b8' : '#64748b', fontWeight: '600' }}>Phone Number</th>
                                <th style={{ padding: '12px 16px', fontSize: '0.85rem', color: isDarkMode ? '#94a3b8' : '#64748b', fontWeight: '600' }}>Status</th>
                                <th style={{ padding: '12px 16px', fontSize: '0.85rem', color: isDarkMode ? '#94a3b8' : '#64748b', fontWeight: '600' }}>Date</th>
                                <th style={{ padding: '12px 16px', fontSize: '0.85rem', color: isDarkMode ? '#94a3b8' : '#64748b', fontWeight: '600', textAlign: 'right' }}>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            {recentCalls.map((call, idx) => (
                                <tr key={idx} style={{ borderBottom: `1px solid ${isDarkMode ? '#334155' : '#e2e8f0'}` }}>
                                    <td style={{ padding: '12px 16px', fontSize: '0.9rem', color: isDarkMode ? '#f8fafc' : '#1e293b', fontWeight: '500' }}>{call.phone_number}</td>
                                    <td style={{ padding: '12px 16px' }}>
                                        <span style={{ 
                                            background: call.status === 'completed' ? (isDarkMode ? 'rgba(16, 185, 129, 0.2)' : '#ecfdf5') : (isDarkMode ? 'rgba(245, 158, 11, 0.2)' : '#fffbeb'),
                                            color: call.status === 'completed' ? '#10b981' : '#f59e0b',
                                            padding: '4px 10px', borderRadius: '99px', fontSize: '0.75rem', fontWeight: '600'
                                        }}>
                                            {call.status || 'unknown'}
                                        </span>
                                    </td>
                                    <td style={{ padding: '12px 16px', fontSize: '0.85rem', color: isDarkMode ? '#94a3b8' : '#64748b' }}>
                                        {call.created_at ? new Date(call.created_at).toLocaleString() : 'N/A'}
                                    </td>
                                    <td style={{ padding: '12px 16px', textAlign: 'right', display: 'flex', justifyContent: 'flex-end', gap: '8px' }}>
                                        <button 
                                            onClick={() => { setSelectedTranscript(call); setIsModalOpen(true); }}
                                            style={{ display: 'flex', alignItems: 'center', gap: '4px', padding: '6px 12px', background: isDarkMode ? '#334155' : '#f1f5f9', border: 'none', borderRadius: '6px', cursor: 'pointer', fontSize: '0.8rem', color: isDarkMode ? '#e2e8f0' : '#475569', fontWeight: '600' }}
                                        >
                                            <FileText size={14} /> View
                                        </button>
                                        <button 
                                            onClick={() => { setSelectedSummary(call); setIsSummaryModalOpen(true); }}
                                            style={{ display: 'flex', alignItems: 'center', gap: '4px', padding: '6px 12px', background: isDarkMode ? '#1e293b' : '#e2e8f0', border: 'none', borderRadius: '6px', cursor: 'pointer', fontSize: '0.8rem', color: isDarkMode ? '#e2e8f0' : '#475569', fontWeight: '600' }}
                                        >
                                            <AlertCircle size={14} /> Summary
                                        </button>
                                        {call.recording_url && (
                                            <button 
                                                onClick={() => window.open(call.recording_url, '_blank')}
                                                style={{ display: 'flex', alignItems: 'center', gap: '4px', padding: '6px 12px', background: isDarkMode ? 'rgba(16, 185, 129, 0.1)' : '#ecfdf5', border: `1px solid ${isDarkMode ? 'rgba(16, 185, 129, 0.2)' : '#d1fae5'}`, borderRadius: '6px', cursor: 'pointer', fontSize: '0.8rem', color: '#10b981', fontWeight: '600' }}
                                                title="Download/Listen Recording"
                                            >
                                                <Headphones size={14} /> Audio
                                            </button>
                                        )}
                                        <button 
                                            onClick={() => downloadTranscriptFile(call.transcript || 'No transcript', call.call_id)}
                                            style={{ display: 'flex', alignItems: 'center', gap: '4px', padding: '6px 12px', background: 'transparent', border: `1px solid ${isDarkMode ? '#334155' : '#cbd5e1'}`, borderRadius: '6px', cursor: 'pointer', fontSize: '0.8rem', color: isDarkMode ? '#e2e8f0' : '#475569', fontWeight: '600' }}
                                        >
                                            <Download size={14} /> DL
                                        </button>
                                    </td>
                                </tr>
                            ))}
                            {recentCalls.length === 0 && (
                                <tr>
                                    <td colSpan={4} style={{ padding: '24px', textAlign: 'center', color: isDarkMode ? '#64748b' : '#94a3b8', fontSize: '0.9rem' }}>
                                        No recent calls found.
                                    </td>
                                </tr>
                            )}
                        </tbody>
                    </table>
                </div>
            </div>
          </section>
        )}

        {/* Scheduled Calls View */}
        {activeView === 'schedule' && (
          <section className="glass-panel animate-fade-in delay-200" style={{ padding: '2rem' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '1.5rem' }}>
              <div style={{ background: '#ede9fe', padding: '6px', borderRadius: '8px', color: '#7c3aed' }}>
                <CalendarClock size={18} />
              </div>
              <h2 style={{ fontSize: '1.25rem', fontWeight: '700', margin: 0 }}>Scheduled Calls</h2>
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1.5fr', gap: '2rem' }}>
              {/* Schedule Form */}
              <form onSubmit={handleScheduleCall} style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem', background: isDarkMode ? '#1e293b' : '#f8fafc', padding: '24px', borderRadius: '12px', border: `1px solid ${isDarkMode ? '#334155' : '#e2e8f0'}` }}>
                <h3 style={{ fontSize: '1rem', fontWeight: '600', margin: 0, color: isDarkMode ? '#f8fafc' : '#0f172a' }}>Schedule a New Call</h3>

                <div>
                  <label style={{ display: 'block', marginBottom: '0.5rem', color: isDarkMode ? '#e2e8f0' : '#334155', fontWeight: '600', fontSize: '0.85rem' }}>Phone Number</label>
                  <input
                    type="tel"
                    placeholder="+91 98765 43210"
                    value={schedPhone}
                    onChange={e => setSchedPhone(e.target.value)}
                    required
                    style={{ width: '100%', padding: '10px 14px', borderRadius: '8px', border: `1px solid ${isDarkMode ? '#475569' : '#cbd5e1'}`, background: isDarkMode ? '#0f172a' : 'white', color: isDarkMode ? '#f8fafc' : '#0f172a', fontFamily: 'inherit', fontSize: '0.95rem', outline: 'none', boxSizing: 'border-box' }}
                  />
                </div>

                <div>
                  <label style={{ display: 'block', marginBottom: '0.5rem', color: isDarkMode ? '#e2e8f0' : '#334155', fontWeight: '600', fontSize: '0.85rem' }}>Schedule Date & Time (IST)</label>
                  <input
                    type="datetime-local"
                    value={schedTime}
                    onChange={e => setSchedTime(e.target.value)}
                    required
                    style={{ width: '100%', padding: '10px 14px', borderRadius: '8px', border: `1px solid ${isDarkMode ? '#475569' : '#cbd5e1'}`, background: isDarkMode ? '#0f172a' : 'white', color: isDarkMode ? '#f8fafc' : '#0f172a', fontFamily: 'inherit', fontSize: '0.95rem', outline: 'none', boxSizing: 'border-box', colorScheme: isDarkMode ? 'dark' : 'light' }}
                  />
                  <p style={{ fontSize: '0.75rem', color: '#64748b', margin: '6px 0 0 0' }}>All times are treated as IST</p>
                </div>

                <div>
                  <label style={{ display: 'block', marginBottom: '0.5rem', color: isDarkMode ? '#e2e8f0' : '#334155', fontWeight: '600', fontSize: '0.85rem' }}>Max Total Attempts (Initial + Retries)</label>
                  <select value={schedRetryCount} onChange={e => setSchedRetryCount(Number(e.target.value))} style={{ width: '100%', padding: '10px 14px', borderRadius: '8px', border: `1px solid ${isDarkMode ? '#475569' : '#cbd5e1'}`, background: isDarkMode ? '#0f172a' : 'white', color: isDarkMode ? '#f8fafc' : '#0f172a', fontFamily: 'inherit', fontSize: '0.95rem', outline: 'none', boxSizing: 'border-box' }}>
                    <option value={1}>1 — Initial attempt only</option>
                    <option value={2}>2 attempts (1 initial + 1 retry)</option>
                    <option value={3}>3 attempts (1 initial + 2 retries)</option>
                    <option value={4}>4 attempts (1 initial + 3 retries)</option>
                    <option value={6}>6 attempts (1 initial + 5 retries)</option>
                  </select>
                  <p style={{ fontSize: '0.75rem', color: '#64748b', margin: '6px 0 0 0' }}>Retries fire 60s apart when a call is answered and cut</p>
                </div>

                <button type="submit" disabled={isScheduling} className="btn-primary" style={{ padding: '12px', borderRadius: '8px', fontWeight: '600', fontSize: '0.95rem', opacity: isScheduling ? 0.7 : 1 }}>
                  {isScheduling ? 'Scheduling...' : '⏰ Schedule Call'}
                </button>

                {schedStatus && (
                  <div style={{ padding: '10px 14px', background: schedStatus.startsWith('✓') ? '#ecfdf5' : '#fef2f2', borderRadius: '8px', border: `1px solid ${schedStatus.startsWith('✓') ? '#a7f3d0' : '#fca5a5'}` }}>
                    <p style={{ margin: 0, fontSize: '0.85rem', fontWeight: '600', color: schedStatus.startsWith('✓') ? '#065f46' : '#991b1b' }}>{schedStatus}</p>
                  </div>
                )}
              </form>

              {/* Scheduled Calls Table */}
              <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <h3 style={{ fontSize: '1rem', fontWeight: '600', margin: 0, color: isDarkMode ? '#f8fafc' : '#0f172a' }}>Pending & Completed</h3>
                  <button onClick={fetchScheduledCalls} style={{ padding: '6px 14px', borderRadius: '6px', background: 'transparent', border: `1px solid ${isDarkMode ? '#334155' : '#e2e8f0'}`, color: isDarkMode ? '#94a3b8' : '#64748b', fontSize: '0.8rem', cursor: 'pointer', fontWeight: '600' }}>↻ Refresh</button>
                </div>

                <div style={{ background: isDarkMode ? '#1e293b' : 'white', borderRadius: '12px', border: `1px solid ${isDarkMode ? '#334155' : '#e2e8f0'}`, overflow: 'hidden' }}>
                  <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
                    <thead style={{ background: isDarkMode ? '#0f172a' : '#f8fafc', borderBottom: `1px solid ${isDarkMode ? '#334155' : '#e2e8f0'}` }}>
                      <tr>
                        <th style={{ padding: '10px 14px', fontSize: '0.78rem', color: isDarkMode ? '#94a3b8' : '#64748b', fontWeight: '600' }}>Phone</th>
                        <th style={{ padding: '10px 14px', fontSize: '0.78rem', color: isDarkMode ? '#94a3b8' : '#64748b', fontWeight: '600' }}>Scheduled (IST)</th>
                        <th style={{ padding: '10px 14px', fontSize: '0.78rem', color: isDarkMode ? '#94a3b8' : '#64748b', fontWeight: '600' }}>Attempts</th>
                        <th style={{ padding: '10px 14px', fontSize: '0.78rem', color: isDarkMode ? '#94a3b8' : '#64748b', fontWeight: '600' }}>Status</th>
                        <th style={{ padding: '10px 14px', fontSize: '0.78rem', color: isDarkMode ? '#94a3b8' : '#64748b', fontWeight: '600', textAlign: 'right' }}>Action</th>
                      </tr>
                    </thead>
                    <tbody>
                      {scheduledCalls.map((sc, idx) => {
                        const statusColor = sc.status === 'completed' ? '#10b981' : sc.status === 'cancelled' ? '#ef4444' : '#f59e0b';
                        const statusBg = sc.status === 'completed' ? (isDarkMode ? 'rgba(16,185,129,0.15)' : '#ecfdf5') : sc.status === 'cancelled' ? (isDarkMode ? 'rgba(239,68,68,0.15)' : '#fef2f2') : (isDarkMode ? 'rgba(245,158,11,0.15)' : '#fffbeb');
                        return (
                          <tr key={idx} style={{ borderBottom: `1px solid ${isDarkMode ? '#334155' : '#f1f5f9'}` }}>
                            <td style={{ padding: '10px 14px', fontSize: '0.85rem', color: isDarkMode ? '#f8fafc' : '#1e293b', fontWeight: '500' }}>{sc.phone_number}</td>
                            <td style={{ padding: '10px 14px', fontSize: '0.8rem', color: isDarkMode ? '#94a3b8' : '#64748b' }}>{sc.scheduled_time.replace('T', ' ')}</td>
                            <td style={{ padding: '10px 14px', fontSize: '0.8rem', color: isDarkMode ? '#94a3b8' : '#64748b' }}>
                              {sc.current_attempt || 0} / {sc.retry_count}
                            </td>
                            <td style={{ padding: '10px 14px' }}>
                              <span style={{ background: statusBg, color: statusColor, padding: '3px 10px', borderRadius: '99px', fontSize: '0.72rem', fontWeight: '700', textTransform: 'uppercase' }}>{sc.status}</span>
                            </td>
                            <td style={{ padding: '10px 14px', textAlign: 'right' }}>
                              {sc.status === 'pending' && (
                                <button onClick={() => handleCancelSchedule(sc.id)} style={{ display: 'inline-flex', alignItems: 'center', gap: '4px', padding: '5px 10px', background: 'transparent', border: '1px solid #ef4444', borderRadius: '6px', color: '#ef4444', cursor: 'pointer', fontSize: '0.78rem', fontWeight: '600' }}>
                                  <Trash2 size={12} /> Cancel
                                </button>
                              )}
                            </td>
                          </tr>
                        );
                      })}
                      {scheduledCalls.length === 0 && (
                        <tr><td colSpan={4} style={{ padding: '28px', textAlign: 'center', color: '#64748b', fontSize: '0.9rem' }}>No scheduled calls yet. Create one!</td></tr>
                      )}
                    </tbody>
                  </table>
                </div>
              </div>
            </div>
          </section>
        )}

        {/* Manage Leads View */}
        {activeView === 'leads' && (
          <section className="glass-panel animate-fade-in delay-200" style={{ padding: '2rem' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '1.5rem' }}>
              <div style={{ background: '#e0e7ff', padding: '6px', borderRadius: '8px', color: '#4f46e5' }}>
                <Users size={18} />
              </div>
              <h2 style={{ fontSize: '1.25rem', fontWeight: '700', margin: 0 }}>Create CRM Leads</h2>
            </div>
            
            <p style={{ fontSize: '0.9rem', color: '#64748b', marginBottom: '1.5rem' }}>
              Add a new lead to EspoCRM manually, or bulk-upload via a CSV file. The CSV must have exactly 3 columns: <code>firstname, lastname, mobile_no</code>.
            </p>
            
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '2rem' }}>
                <form onSubmit={(e) => handleCreateLead(e)} style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem', background: isDarkMode ? '#1e293b' : '#f8fafc', padding: '24px', borderRadius: '12px', border: `1px solid ${isDarkMode ? '#334155' : '#e2e8f0'}` }}>
                    <h3 style={{ fontSize: '1rem', fontWeight: '600', margin: 0 }}>Manual Entry</h3>
                    
                    <div>
                        <label style={{ display: 'block', marginBottom: '0.5rem', color: isDarkMode ? '#e2e8f0' : '#334155', fontWeight: '600', fontSize: '0.9rem' }}>First Name</label>
                        <input type="text" value={newLeadFn} onChange={e => setNewLeadFn(e.target.value)} required style={{ width: '100%', padding: '10px', borderRadius: '6px', border: `1px solid ${isDarkMode ? '#475569' : '#cbd5e1'}`, background: isDarkMode ? '#0f172a' : 'white', color: isDarkMode ? 'white' : 'black' }} />
                    </div>
                    <div>
                        <label style={{ display: 'block', marginBottom: '0.5rem', color: isDarkMode ? '#e2e8f0' : '#334155', fontWeight: '600', fontSize: '0.9rem' }}>Last Name</label>
                        <input type="text" value={newLeadLn} onChange={e => setNewLeadLn(e.target.value)} required style={{ width: '100%', padding: '10px', borderRadius: '6px', border: `1px solid ${isDarkMode ? '#475569' : '#cbd5e1'}`, background: isDarkMode ? '#0f172a' : 'white', color: isDarkMode ? 'white' : 'black' }} />
                    </div>
                    <div>
                        <label style={{ display: 'block', marginBottom: '0.5rem', color: isDarkMode ? '#e2e8f0' : '#334155', fontWeight: '600', fontSize: '0.9rem' }}>Mobile No</label>
                        <input type="tel" value={newLeadPhone} onChange={e => setNewLeadPhone(e.target.value)} required style={{ width: '100%', padding: '10px', borderRadius: '6px', border: `1px solid ${isDarkMode ? '#475569' : '#cbd5e1'}`, background: isDarkMode ? '#0f172a' : 'white', color: isDarkMode ? 'white' : 'black' }} />
                    </div>

                    <button type="submit" disabled={isCreatingLead} className="btn-primary" style={{ opacity: isCreatingLead ? 0.7 : 1, padding: '12px 20px', borderRadius: '8px', fontSize: '1rem', fontWeight: '600', marginTop: 'auto' }}>
                        {isCreatingLead ? 'Pushing...' : 'Create Lead'}
                    </button>
                </form>

                <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem', background: isDarkMode ? '#1e293b' : '#f8fafc', padding: '24px', borderRadius: '12px', border: `1px solid ${isDarkMode ? '#334155' : '#e2e8f0'}` }}>
                    <h3 style={{ fontSize: '1rem', fontWeight: '600', margin: 0 }}>Bulk Upload CSV</h3>
                    <div style={{ border: `2px dashed ${isDarkMode ? '#475569' : '#cbd5e1'}`, borderRadius: '8px', padding: '32px', textAlign: 'center', background: isDarkMode ? '#0f172a' : 'white', display: 'flex', flexDirection: 'column', height: '100%' }}>
                        <UploadCloud size={32} color="#64748b" style={{ margin: '0 auto 16px auto' }} />
                        <p style={{ margin: '0 0 16px 0', fontSize: '0.9rem', color: '#64748b' }}>Select a .csv file to batch upload leads</p>
                        <input id="csv-upload" type="file" accept=".csv" onChange={(e) => setSelectedFile(e.target.files?.[0] || null)} style={{ margin: '0 auto 20px auto', maxWidth: '250px' }} />
                        
                        <button 
                            onClick={processCSV} 
                            disabled={!selectedFile || isCreatingLead} 
                            className="btn-primary" 
                            style={{ opacity: (!selectedFile || isCreatingLead) ? 0.5 : 1, padding: '12px 20px', borderRadius: '8px', fontSize: '1rem', fontWeight: '600', marginTop: 'auto', width: '100%' }}
                        >
                            {isCreatingLead ? 'Processing...' : 'Upload CSV File'}
                        </button>
                    </div>
                </div>
            </div>

            {leadStatusMessage && (
                <div style={{ marginTop: '1.5rem', padding: '12px 16px', background: '#ecfdf5', borderRadius: '8px', border: '1px solid #a7f3d0' }}>
                    <p style={{ margin: 0, color: '#065f46', fontWeight: '600', fontSize: '0.9rem' }}>
                    {leadStatusMessage}
                    </p>
                </div>
            )}
          </section>
        )}

        {/* Transcript Modal Overlay */}
        {isModalOpen && selectedTranscript && (
            <div style={{ position: 'fixed', top: 0, left: 0, width: '100%', height: '100%', background: 'rgba(0,0,0,0.5)', zIndex: 100, display: 'flex', justifyContent: 'center', alignItems: 'center', padding: '20px' }}>
                <div className="animate-fade-in" style={{ width: '100%', maxWidth: '700px', background: isDarkMode ? '#0f172a' : 'white', borderRadius: '16px', border: `1px solid ${isDarkMode ? '#334155' : '#e2e8f0'}`, overflow: 'hidden', display: 'flex', flexDirection: 'column', maxHeight: '80vh' }}>
                    <div style={{ padding: '24px', borderBottom: `1px solid ${isDarkMode ? '#334155' : '#e2e8f0'}`, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                        <div>
                            <h3 style={{ margin: 0, fontSize: '1.25rem', color: isDarkMode ? 'white' : '#0f172a' }}>Call Transcript</h3>
                            <p style={{ margin: '4px 0 0 0', fontSize: '0.9rem', color: isDarkMode ? '#94a3b8' : '#64748b' }}>{selectedTranscript.phone_number}</p>
                        </div>
                        <button onClick={() => setIsModalOpen(false)} style={{ background: 'transparent', border: 'none', cursor: 'pointer', color: isDarkMode ? '#94a3b8' : '#64748b' }}>
                            <X size={24} />
                        </button>
                    </div>
                    <div style={{ padding: '24px', overflowY: 'auto', flex: 1, background: isDarkMode ? '#1e293b' : '#f8fafc' }}>
                        <pre style={{ whiteSpace: 'pre-wrap', wordWrap: 'break-word', fontFamily: 'monospace', fontSize: '0.9rem', color: isDarkMode ? '#e2e8f0' : '#334155', margin: 0, lineHeight: '1.5' }}>
                            {selectedTranscript.transcript || 'No transcript generated for this log.'}
                        </pre>
                    </div>
                    <div style={{ padding: '16px 24px', borderTop: `1px solid ${isDarkMode ? '#334155' : '#e2e8f0'}`, display: 'flex', justifyContent: 'flex-end', gap: '16px' }}>
                        <button onClick={() => setIsModalOpen(false)} style={{ padding: '10px 20px', background: 'transparent', border: `1px solid ${isDarkMode ? '#475569' : '#cbd5e1'}`, borderRadius: '8px', cursor: 'pointer', color: isDarkMode ? 'white' : '#0f172a', fontWeight: '500' }}>
                            Close
                        </button>
                        <button 
                            onClick={() => downloadTranscriptFile(selectedTranscript.transcript || 'No transcript', selectedTranscript.call_id)}
                            style={{ padding: '10px 20px', background: '#3b82f6', border: 'none', borderRadius: '8px', cursor: 'pointer', color: 'white', fontWeight: 'bold', display: 'flex', alignItems: 'center', gap: '8px' }}
                        >
                            <Download size={16} /> Download .txt
                        </button>
                    </div>
                </div>
            </div>
        )}

        {/* Summary Modal Overlay */}
        {isSummaryModalOpen && selectedSummary && (
            <div style={{ position: 'fixed', top: 0, left: 0, width: '100%', height: '100%', background: 'rgba(0,0,0,0.5)', zIndex: 100, display: 'flex', justifyContent: 'center', alignItems: 'center', padding: '20px' }}>
                <div className="animate-fade-in" style={{ width: '100%', maxWidth: '500px', background: isDarkMode ? '#0f172a' : 'white', borderRadius: '16px', border: `1px solid ${isDarkMode ? '#334155' : '#e2e8f0'}`, overflow: 'hidden', display: 'flex', flexDirection: 'column' }}>
                    <div style={{ padding: '24px', borderBottom: `1px solid ${isDarkMode ? '#334155' : '#e2e8f0'}`, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                        <div>
                            <h3 style={{ margin: 0, fontSize: '1.25rem', color: isDarkMode ? 'white' : '#0f172a' }}>Groq AI Summary</h3>
                            <p style={{ margin: '4px 0 0 0', fontSize: '0.9rem', color: isDarkMode ? '#94a3b8' : '#64748b' }}>{selectedSummary.phone_number}</p>
                        </div>
                        <button onClick={() => setIsSummaryModalOpen(false)} style={{ background: 'transparent', border: 'none', cursor: 'pointer', color: isDarkMode ? '#94a3b8' : '#64748b' }}>
                            <X size={24} />
                        </button>
                    </div>
                    <div style={{ padding: '24px', background: isDarkMode ? '#1e293b' : '#f8fafc' }}>
                        <p style={{ fontSize: '1rem', color: isDarkMode ? '#e2e8f0' : '#334155', margin: 0, lineHeight: '1.6' }}>
                            {selectedSummary.summary || 'No summary available for this call.'}
                        </p>
                    </div>
                    <div style={{ padding: '16px 24px', borderTop: `1px solid ${isDarkMode ? '#334155' : '#e2e8f0'}`, display: 'flex', justifyContent: 'flex-end' }}>
                        <button onClick={() => setIsSummaryModalOpen(false)} style={{ padding: '10px 20px', background: 'transparent', border: `1px solid ${isDarkMode ? '#475569' : '#cbd5e1'}`, borderRadius: '8px', cursor: 'pointer', color: isDarkMode ? 'white' : '#0f172a', fontWeight: '500' }}>
                            Close
                        </button>
                    </div>
                </div>
            </div>
        )}



      </main>
    </div>
  );
}