import { useState, useEffect } from 'react';
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid, Cell } from 'recharts';
import { Download, PhoneCall, ArrowRightLeft } from 'lucide-react';

export default function AnalyticsView({ isDarkMode }: { isDarkMode: boolean }) {
  const [data, setData] = useState<any>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const bgColor = isDarkMode ? '#111111' : '#ffffff';
  const panelColor = isDarkMode ? '#1c1c1c' : '#ffffff';
  const textColor = isDarkMode ? '#ffffff' : '#0f172a';
  const mutedColor = isDarkMode ? '#a1a1aa' : '#64748b';
  const gridColor = isDarkMode ? '#333333' : '#e2e8f0';

  useEffect(() => {
    const fetchAnalytics = async () => {
      try {
        const apiUrl = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8000';
        const res = await fetch(`${apiUrl}/api/analytics`);
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
    window.location.href = `${apiUrl}/api/analytics/csv`;
  };

  if (loading) return <div style={{ color: textColor, padding: '2rem' }}>Loading analytics...</div>;
  if (error) return <div style={{ color: 'red', padding: '2rem' }}>{error}</div>;

  return (
    <div style={{ background: bgColor, color: textColor, padding: '24px', borderRadius: '16px', minHeight: '600px', transition: 'all 0.3s' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px' }}>
        <div>
          <h2 style={{ fontSize: '1.8rem', fontWeight: 700, margin: 0 }}>Daily Reports</h2>
          <p style={{ color: mutedColor, fontSize: '0.9rem', marginTop: '4px' }}>Showing all historical database transcripts</p>
        </div>
        <button 
          onClick={handleDownloadCSV}
          style={{ 
            display: 'flex', alignItems: 'center', gap: '8px', padding: '8px 16px', 
            background: 'transparent', border: `1px solid ${gridColor}`, color: textColor, 
            borderRadius: '6px', cursor: 'pointer', transition: 'all 0.2s'
          }}
          className="hover-lift"
        >
          <Download size={16} /> Download CSV
        </button>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '24px', marginBottom: '24px' }}>
        {/* KPI Panel 1 */}
        <div style={{ background: panelColor, padding: '24px', borderRadius: '12px', border: `1px solid ${gridColor}` }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
            <h3 style={{ fontSize: '1rem', fontWeight: 600, color: textColor, margin: 0 }}>Total Workflow Runs</h3>
            <PhoneCall size={18} color={mutedColor} />
          </div>
          <p style={{ fontSize: '2.5rem', fontWeight: 800, margin: '12px 0 4px 0' }}>{data?.total_runs || 0}</p>
          <p style={{ fontSize: '0.85rem', color: mutedColor, margin: 0 }}>Total calls processed</p>
        </div>

        {/* KPI Panel 2 */}
        <div style={{ background: panelColor, padding: '24px', borderRadius: '12px', border: `1px solid ${gridColor}` }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
            <h3 style={{ fontSize: '1rem', fontWeight: 600, color: textColor, margin: 0 }}>Transfer Dispositions</h3>
            <ArrowRightLeft size={18} color={mutedColor} />
          </div>
          <p style={{ fontSize: '2.5rem', fontWeight: 800, margin: '12px 0 4px 0' }}>{data?.transfer_count || 0}</p>
          <p style={{ fontSize: '0.85rem', color: mutedColor, margin: 0 }}>Calls transferred (XFER)</p>
        </div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: 'minmax(0, 1fr) minmax(0, 1fr)', gap: '24px' }}>
        {/* Disposition Distribution */}
        <div style={{ background: panelColor, padding: '24px', borderRadius: '12px', border: `1px solid ${gridColor}`, overflow: 'hidden' }}>
          <h3 style={{ fontSize: '1.2rem', fontWeight: 600, color: textColor, marginBottom: '24px' }}>Disposition Distribution</h3>
          <div style={{ width: '100%', height: '300px' }}>
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={data?.dispositions || []} margin={{ top: 0, right: 0, left: -20, bottom: 0 }}>
                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke={gridColor} />
                <XAxis dataKey="name" stroke={mutedColor} fontSize={12} tickLine={false} axisLine={false} tickMargin={10} />
                <YAxis stroke={mutedColor} fontSize={12} tickLine={false} axisLine={false} />
                <Tooltip cursor={{ fill: isDarkMode ? '#2a2a2a' : '#f1f5f9' }} contentStyle={{ background: panelColor, border: `1px solid ${gridColor}`, color: textColor }} />
                <Bar dataKey="value" radius={[4, 4, 0, 0]}>
                  {
                    (data?.dispositions || []).map((entry: any, index: number) => (
                      <Cell key={`cell-${index}`} fill={index % 2 === 0 ? '#3b82f6' : '#10b981'} />
                    ))
                  }
                </Bar>
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* Call Duration Distribution */}
        <div style={{ background: panelColor, padding: '24px', borderRadius: '12px', border: `1px solid ${gridColor}`, overflow: 'hidden' }}>
          <h3 style={{ fontSize: '1.2rem', fontWeight: 600, color: textColor, marginBottom: '24px' }}>Call Duration Distribution</h3>
          <div style={{ width: '100%', height: '300px' }}>
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={data?.duration_stats || []} margin={{ top: 0, right: 0, left: -20, bottom: 0 }}>
                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke={gridColor} />
                <XAxis dataKey="range" stroke={mutedColor} fontSize={12} tickLine={false} axisLine={false} tickMargin={10} />
                <YAxis stroke={mutedColor} fontSize={12} tickLine={false} axisLine={false} />
                <Tooltip cursor={{ fill: isDarkMode ? '#2a2a2a' : '#f1f5f9' }} contentStyle={{ background: panelColor, border: `1px solid ${gridColor}`, color: textColor }} />
                <Bar dataKey="count" radius={[4, 4, 0, 0]}>
                   {
                    (data?.duration_stats || []).map((entry: any, index: number) => (
                      <Cell key={`cell-${index}`} fill={'#a7f3d0'} />
                    ))
                  }
                </Bar>
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>
      </div>
    </div>
  );
}
