import type { Metadata } from 'next';
import './globals.css';

export const metadata: Metadata = {
  title: 'Vedaspark AI Tele-Caller',
  description: 'AI-Powered Intelligent Tele-Calling Agent',
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="en">
      <body>{children}</body>
    </html>
  );
}
