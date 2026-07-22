import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Train } from 'lucide-react';
import { register } from '../api';
import { useAuthStore } from '../store/authStore';
import ThemeToggle from '../components/ThemeToggle';
import toast from 'react-hot-toast';

export default function RegisterPage() {
  const [form, setForm] = useState({ name: '', email: '', password: '', phone: '' });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const setAuth = useAuthStore((s) => s.setAuth);
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      const { data } = await register(form);
      localStorage.setItem('token', data.token);
      setAuth(data.token, { id: data.userId, name: data.name, email: data.email, role: data.role });
      toast.success('Account created! ₹1000 welcome bonus added to your wallet 🎉');
      navigate('/dashboard');
    } catch (err) {
      setError(err.response?.data?.message || 'Registration failed. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-page">
      <div className="auth-theme-toggle">
        <ThemeToggle />
      </div>
      <div className="auth-card">
        <div className="auth-logo">
          <div className="auth-logo-icon"><Train size={32} /></div>
          <h1 className="auth-title">Create Account</h1>
          <p className="auth-subtitle">Join RailSuperApp & get ₹1000 bonus</p>
        </div>

        {error && <div className="error-msg">{error}</div>}

        <form className="auth-form" onSubmit={handleSubmit}>
          <div className="form-group">
            <label className="form-label">Full Name</label>
            <input id="reg-name" type="text" className="form-input" placeholder="John Doe"
              value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} required />
          </div>
          <div className="form-group">
            <label className="form-label">Email Address</label>
            <input id="reg-email" type="email" className="form-input" placeholder="you@example.com"
              value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} required />
          </div>
          <div className="form-group">
            <label className="form-label">Phone Number</label>
            <input id="reg-phone" type="tel" className="form-input" placeholder="+91 9876543210"
              value={form.phone} onChange={(e) => setForm({ ...form, phone: e.target.value })} required />
          </div>
          <div className="form-group">
            <label className="form-label">Password</label>
            <input id="reg-password" type="password" className="form-input" placeholder="Min. 6 characters"
              value={form.password} onChange={(e) => setForm({ ...form, password: e.target.value })} minLength={6} required />
          </div>
          <button id="reg-submit" type="submit" className="btn btn-primary btn-lg btn-full" disabled={loading}>
            {loading ? 'Creating account...' : 'Create Account'}
          </button>
        </form>

        <div className="auth-switch">
          Already have an account? <Link to="/login">Sign in</Link>
        </div>
      </div>
    </div>
  );
}
