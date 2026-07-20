import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { Train, Ticket, Wallet, MapPin, UtensilsCrossed, HeadphonesIcon, Search, TrendingUp } from 'lucide-react';
import { useAuthStore } from '../store/authStore';
import { getMyBookings, getWallet } from '../api';

export default function DashboardPage() {
  const { user } = useAuthStore();
  const [bookings, setBookings] = useState([]);
  const [wallet, setWallet] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([getMyBookings(), getWallet()])
      .then(([bRes, wRes]) => {
        setBookings(bRes.data);
        setWallet(wRes.data);
      })
      .catch(() => {})
      .finally(() => setLoading(false));
  }, []);

  const confirmed = bookings.filter((b) => b.status === 'CONFIRMED').length;
  const totalSpent = bookings
    .filter((b) => b.status === 'CONFIRMED')
    .reduce((sum, b) => sum + b.fare, 0);

  const quickLinks = [
    { to: '/search', icon: Search, label: 'Search Trains', color: 'gold', desc: 'Find & book tickets' },
    { to: '/bookings', icon: Ticket, label: 'My Bookings', color: 'teal', desc: 'View all bookings' },
    { to: '/wallet', icon: Wallet, label: 'My Wallet', color: 'green', desc: `₹${wallet?.balance?.toFixed(2) || '—'} balance` },
    { to: '/tracking', icon: MapPin, label: 'Live Tracking', color: 'blue', desc: 'Track your train' },
    { to: '/food', icon: UtensilsCrossed, label: 'Order Food', color: 'purple', desc: 'Food in your seat' },
    { to: '/support', icon: HeadphonesIcon, label: 'Support', color: 'teal', desc: 'Get help' },
  ];

  return (
    <div className="page">
      <div className="page-header">
        <h1 className="page-title">Welcome, {user?.name?.split(' ')[0]} 👋</h1>
        <p className="page-subtitle">Your railway super-app dashboard</p>
      </div>

      {/* Stats */}
      <div className="stat-grid">
        <div className="stat-card">
          <div className="stat-icon gold"><Ticket size={22} /></div>
          <div>
            <div className="stat-label">Total Bookings</div>
            <div className="stat-value">{bookings.length}</div>
          </div>
        </div>
        <div className="stat-card">
          <div className="stat-icon teal"><TrendingUp size={22} /></div>
          <div>
            <div className="stat-label">Confirmed</div>
            <div className="stat-value">{confirmed}</div>
          </div>
        </div>
        <div className="stat-card">
          <div className="stat-icon green"><Wallet size={22} /></div>
          <div>
            <div className="stat-label">Wallet Balance</div>
            <div className="stat-value" style={{ fontSize: '1.2rem' }}>
              ₹{loading ? '—' : (wallet?.balance?.toFixed(0) || '0')}
            </div>
          </div>
        </div>
        <div className="stat-card">
          <div className="stat-icon purple"><Train size={22} /></div>
          <div>
            <div className="stat-label">Total Spent</div>
            <div className="stat-value" style={{ fontSize: '1.2rem' }}>₹{totalSpent.toFixed(0)}</div>
          </div>
        </div>
      </div>

      {/* Quick Links */}
      <h2 className="section-title">Quick Access</h2>
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(200px, 1fr))', gap: '12px', marginBottom: '32px' }}>
        {quickLinks.map(({ to, icon: Icon, label, color, desc }) => (
          <Link key={to} to={to} style={{ textDecoration: 'none' }}>
            <div className="card" style={{ cursor: 'pointer', transition: 'all 0.2s' }}
              onMouseEnter={e => e.currentTarget.style.transform = 'translateY(-3px)'}
              onMouseLeave={e => e.currentTarget.style.transform = 'translateY(0)'}>
              <div className={`stat-icon ${color}`} style={{ marginBottom: '12px' }}>
                <Icon size={22} />
              </div>
              <div className="font-bold" style={{ fontSize: '0.9rem' }}>{label}</div>
              <div className="text-xs text-secondary mt-1">{desc}</div>
            </div>
          </Link>
        ))}
      </div>

      {/* Recent Bookings */}
      {bookings.length > 0 && (
        <>
          <div className="flex justify-between items-center mb-4">
            <h2 className="section-title" style={{ marginBottom: 0 }}>Recent Bookings</h2>
            <Link to="/bookings" className="btn btn-secondary btn-sm">View All</Link>
          </div>
          <div className="booking-list">
            {bookings.slice(0, 3).map((b) => (
              <div key={b.id} className="booking-card">
                <div>
                  <div className="booking-pnr">PNR: <span>{b.pnr}</span></div>
                  <div className="booking-train">{b.trainName}</div>
                  <div className="booking-meta">{b.fromStation} → {b.toStation} • {b.travelClass} • {b.numberOfSeats} seat(s)</div>
                </div>
                <div style={{ textAlign: 'right' }}>
                  <span className={`badge badge-${b.status.toLowerCase()}`}>{b.status}</span>
                  <div className="booking-fare" style={{ marginTop: '8px' }}>₹{b.fare?.toFixed(2)}</div>
                </div>
              </div>
            ))}
          </div>
        </>
      )}
    </div>
  );
}
