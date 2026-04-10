'use client';
import { useState, useEffect } from 'react';
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid, Cell, PieChart, Pie, Legend } from 'recharts';
import { Download, PhoneCall, ArrowRightLeft, TrendingUp, Star, Users, MessageSquare } from 'lucide-react';

export default function AnalyticsView({ isDarkMode, session }: { isDarkMode: boolean, session: any }) {
  const [data, setData] = useState<any>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const bg = isDarkMode ? '#111111' : '#f6fcfc';
  const panel = isDarkMode ? '#1c1c1c' : '#ffffff';
  const text = isDarkMode ? '#ffffff' : '#0f172a';
  const muted = isDarkMode ? '#a1a1aa' : '#64748b';
  const grid = isDarkMode ? '#333333' : '#e2e8f0';
  const border = isDarkMode ? '#27272a' : '#e2e8f0';

  useEffect(() => {
    const fetchAnalytics = async () => {
      try {
        const apiUrl = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8000';
        const res = await fetch(`${apiUrl}/api/analytics`, {
            headers: { 'X-User-ID': session?.user?.id || 'anonymous' }
        });
        if (!res.ok) throw new Error('Failed to fetch analytics');
        const json = await res.json();
        setData(json);
      } catch (err: any) {
        setError(err.message);
      } finally {
        setLoading(false);
      }
    };
    fetchAnalytics();
  }, []);

  const handleDownloadCSV = () => {
    const apiUrl = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8000';
    const userId = session?.user?.id || 'anonymous';
    window.location.href = `${apiUrl}/api/analytics/csv?x_user_id=${userId}`;
  };

  const INTENT_COLORS: Record<string, string> = {
    INTERESTED: '#10b981',
    NOT_INTERESTED: '#ef4444',
    CALLBACK: '#f59e0b',
    INFO_SEEKING: '#3b82f6',
    UNKNOWN: '#6b7280',
  };

  const LEAD_COLORS: Record<string, string> = {
    HOT: '#ef4444',
    WARM: '#f59e0b',
    COLD: '#3b82f6',
    UNKNOWN: '#6b7280',
  };

  const SENTIMENT_COLORS: Record<string, string> = {
    positive: '#10b981',
    neutral: '#3b82f6',
    negative: '#ef4444',
    unknown: '#6b7280',
  };

  const statCard = (icon: React.ReactNode, label: string, value: string | number, sub: string, accent: string) => (
    <div style={{ background: panel, padding: '24px', borderRadius: '12px', borderRight: `1px solid ${border}`, borderBottom: `1px solid ${border}`, borderLeft: `1px solid ${border}`, borderTop: `3px solid ${accent}` }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
        <h3 style={{ fontSize: '0.85rem', fontWeight: 600, color: muted, margin: 0, textTransform: 'uppercase', letterSpacing: '0.5px' }}>{label}</h3>
        <div style={{ color: accent }}>{icon}</div>
      </div>
      <p style={{ fontSize: '2.4rem', fontWeight: 800, margin: '12px 0 4px 0', color: text }}>{value}</p>
      <p style={{ fontSize: '0.8rem', color: muted, margin: 0 }}>{sub}</p>
    </div>
  );

  if (loading) return <div style={{ color: text, padding: '2rem', textAlign: 'center' }}>Loading analytics from Neon DB...</div>;
  if (error) return <div style={{ color: '#ef4444', padding: '2rem' }}>{error}</div>;

  return (
    <div style={{ color: text, transition: 'all 0.3s' }}>
      {/* Header Row */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '28px' }}>
        <div>
          <h2 style={{ fontSize: '1.8rem', fontWeight: 700, margin: 0 }}>Daily Reports</h2>
          <p style={{ color: muted, fontSize: '0.85rem', marginTop: '4px' }}>Powered by Groq AI intent analysis • Live from Neon DB</p>
        </div>
        <button
          onClick={handleDownloadCSV}
          style={{ display: 'flex', alignItems: 'center', gap: '8px', padding: '10px 18px', background: 'transparent', border: `1px solid ${border}`, color: text, borderRadius: '8px', cursor: 'pointer', fontWeight: '500', transition: 'all 0.2s' }}
        >
          <Download size={16} /> Download CSV
        </button>
      </div>

      {/* KPI Row */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '16px', marginBottom: '24px' }}>
        {statCard(<PhoneCall size={18} />, 'Total Calls', data?.total_runs || 0, 'All-time workflow runs', '#3b82f6')}
        {statCard(<TrendingUp size={18} />, 'Conversion Rate', `${data?.conversion_rate || 0}%`, `${data?.interested_count || 0} interested leads`, '#10b981')}
        {statCard(<MessageSquare size={18} />, 'Callback Requests', data?.callback_count || 0, 'Leads requesting callback', '#f59e0b')}
        {statCard(<Star size={18} />, 'Avg Engagement', data?.avg_engagement ? `${data.avg_engagement}/10` : 'N/A', 'Groq-scored engagement', '#8b5cf6')}
      </div>

      {/* Charts Row 1 */}
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '20px', marginBottom: '20px' }}>
        {/* Intent Breakdown */}
        <div style={{ background: panel, padding: '24px', borderRadius: '12px', border: `1px solid ${border}` }}>
          <h3 style={{ fontSize: '1rem', fontWeight: 600, color: text, marginBottom: '20px' }}>Intent Detection <span style={{ fontSize: '0.75rem', color: muted, fontWeight: 400 }}>• Groq AI</span></h3>
          <div style={{ width: '100%', height: '240px' }}>
            <ResponsiveContainer width="100%" height={240}>
              <BarChart data={data?.intent_breakdown || []} margin={{ top: 0, right: 0, left: -20, bottom: 0 }}>
                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke={grid} />
                <XAxis dataKey="name" stroke={muted} fontSize={11} tickLine={false} axisLine={false} tickMargin={8} />
                <YAxis stroke={muted} fontSize={11} tickLine={false} axisLine={false} />
                <Tooltip cursor={{ fill: isDarkMode ? '#2a2a2a' : '#f1f5f9' }} contentStyle={{ background: panel, border: `1px solid ${border}`, color: text, borderRadius: '8px' }} />
                <Bar dataKey="value" radius={[4, 4, 0, 0]}>
                  {(data?.intent_breakdown || []).map((entry: any, i: number) => (
                    <Cell key={i} fill={INTENT_COLORS[entry.name] || '#6b7280'} />
                  ))}
                </Bar>
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* Lead Quality Funnel */}
        <div style={{ background: panel, padding: '24px', borderRadius: '12px', border: `1px solid ${border}` }}>
          <h3 style={{ fontSize: '1rem', fontWeight: 600, color: text, marginBottom: '20px' }}>Lead Quality <span style={{ fontSize: '0.75rem', color: muted, fontWeight: 400 }}>• HOT / WARM / COLD</span></h3>
          <div style={{ width: '100%', height: '240px', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
            <ResponsiveContainer width="100%" height={240}>
              <PieChart>
                <Pie data={data?.lead_quality || []} dataKey="value" nameKey="name" cx="50%" cy="50%" outerRadius={90} label={({ name, percent }) => `${name} ${((percent ?? 0) * 100).toFixed(0)}%`} labelLine={false}>
                  {(data?.lead_quality || []).map((entry: any, i: number) => (
                    <Cell key={i} fill={LEAD_COLORS[entry.name] || '#6b7280'} />
                  ))}
                </Pie>
                <Tooltip contentStyle={{ background: panel, border: `1px solid ${border}`, color: text, borderRadius: '8px' }} />
              </PieChart>
            </ResponsiveContainer>
          </div>
        </div>
      </div>

      {/* Charts Row 2 */}
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '20px' }}>
        {/* Sentiment Distribution */}
        <div style={{ background: panel, padding: '24px', borderRadius: '12px', border: `1px solid ${border}` }}>
          <h3 style={{ fontSize: '1rem', fontWeight: 600, color: text, marginBottom: '20px' }}>Sentiment Analysis <span style={{ fontSize: '0.75rem', color: muted, fontWeight: 400 }}>• Groq AI</span></h3>
          <div style={{ width: '100%', height: '240px' }}>
            <ResponsiveContainer width="100%" height={240}>
              <BarChart data={data?.sentiment_dist || []} margin={{ top: 0, right: 0, left: -20, bottom: 0 }}>
                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke={grid} />
                <XAxis dataKey="name" stroke={muted} fontSize={11} tickLine={false} axisLine={false} tickMargin={8} />
                <YAxis stroke={muted} fontSize={11} tickLine={false} axisLine={false} />
                <Tooltip cursor={{ fill: isDarkMode ? '#2a2a2a' : '#f1f5f9' }} contentStyle={{ background: panel, border: `1px solid ${border}`, color: text, borderRadius: '8px' }} />
                <Bar dataKey="value" radius={[4, 4, 0, 0]}>
                  {(data?.sentiment_dist || []).map((entry: any, i: number) => (
                    <Cell key={i} fill={SENTIMENT_COLORS[entry.name] || '#6b7280'} />
                  ))}
                </Bar>
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* Call Duration */}
        <div style={{ background: panel, padding: '24px', borderRadius: '12px', border: `1px solid ${border}` }}>
          <h3 style={{ fontSize: '1rem', fontWeight: 600, color: text, marginBottom: '20px' }}>Call Duration Distribution</h3>
          <div style={{ width: '100%', height: '240px' }}>
            <ResponsiveContainer width="100%" height={240}>
              <BarChart data={data?.duration_stats || []} margin={{ top: 0, right: 0, left: -20, bottom: 0 }}>
                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke={grid} />
                <XAxis dataKey="range" stroke={muted} fontSize={11} tickLine={false} axisLine={false} tickMargin={8} />
                <YAxis stroke={muted} fontSize={11} tickLine={false} axisLine={false} />
                <Tooltip cursor={{ fill: isDarkMode ? '#2a2a2a' : '#f1f5f9' }} contentStyle={{ background: panel, border: `1px solid ${border}`, color: text, borderRadius: '8px' }} />
                <Bar dataKey="count" radius={[4, 4, 0, 0]} fill="#a7f3d0" />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>
      </div>
    </div>
  );
}
