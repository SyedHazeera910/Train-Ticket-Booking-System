import React from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';
import {
  Train, Ticket, Wallet, MapPin, UtensilsCrossed,
  HeadphonesIcon, LogOut, User, LayoutDashboard, Search
} from 'lucide-react';

const navItems = [
  { path: '/dashboard', icon: LayoutDashboard, label: 'Dashboard' },
  { path: '/search', icon: Search, label: 'Search Trains' },
  { path: '/bookings', icon: Ticket, label: 'My Bookings' },
  { path: '/pnr', icon: Train, label: 'PNR Status' },
  { path: '/wallet', icon: Wallet, label: 'Wallet' },
  { path: '/transactions', icon: Wallet, label: 'Transactions' },
  { path: '/tracking', icon: MapPin, label: 'Live Tracking' },
  { path: '/food', icon: UtensilsCrossed, label: 'Food Order' },
  { path: '/support', icon: HeadphonesIcon, label: 'Support' },
];

export default function Navbar() {
  const { user, logout } = useAuthStore();
  const navigate = useNavigate();
  const location = useLocation();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <nav className="navbar">
      <div className="navbar-brand">
        <Train size={28} className="brand-icon" />
        <span className="brand-text">RailSuperApp</span>
      </div>

      <div className="navbar-links">
        {navItems.map(({ path, icon: Icon, label }) => (
          <Link
            key={path}
            to={path}
            className={`nav-link ${location.pathname === path ? 'active' : ''}`}
          >
            <Icon size={16} />
            <span>{label}</span>
          </Link>
        ))}
      </div>

      <div className="navbar-user">
        <div className="user-badge">
          <User size={16} />
          <span>{user?.name?.split(' ')[0] || 'User'}</span>
        </div>
        <button className="logout-btn" onClick={handleLogout}>
          <LogOut size={16} />
        </button>
      </div>
    </nav>
  );
}
