import type { Metadata } from 'next';
import { Syne } from 'next/font/google';
import './globals.css';

const syne = Syne({ subsets: ['latin'], variable: '--font-syne' });

export const metadata: Metadata = {
  title: 'Synapse-AI',
  description: 'AI-Powered Intelligent Agent',
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="en">
      <body className={syne.variable}>{children}</body>
    </html>
  );
}
