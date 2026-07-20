import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Search, ArrowRight, Clock, Users } from 'lucide-react';
import { getStations, searchTrains } from '../api';
import toast from 'react-hot-toast';

function formatTime(dt) {
  if (!dt) return '--:--';
  return new Date(dt).toLocaleTimeString('en-IN', { hour: '2-digit', minute: '2-digit', hour12: false });
}

function formatDuration(dep, arr) {
  const diff = new Date(arr) - new Date(dep);
  const h = Math.floor(diff / 3600000);
  const m = Math.floor((diff % 3600000) / 60000);
  return `${h}h ${m}m`;
}

export default function SearchTrainsPage() {
  const [stations, setStations] = useState([]);
  const [form, setForm] = useState({ from: '', to: '', date: new Date().toISOString().slice(0, 10), seats: 1 });
  const [trains, setTrains] = useState([]);
  const [loading, setLoading] = useState(false);
  const [searched, setSearched] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    getStations().then((r) => setStations(r.data)).catch(() => {});
  }, []);

  const handleSearch = async (e) => {
    e.preventDefault();
    if (!form.from || !form.to) { toast.error('Please select departure and destination'); return; }
    if (form.from === form.to) { toast.error('Departure and destination cannot be the same'); return; }
    setLoading(true);
    setSearched(true);
    try {
      const { data } = await searchTrains(form.from, form.to, form.date, form.seats);
      setTrains(data);
      if (data.length === 0) toast('No trains found for this route & date', { icon: '🚫' });
    } catch {
      toast.error('Search failed. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleBook = (train) => {
    navigate('/book', { state: { train, seats: form.seats } });
  };

  return (
    <div className="page">
      <div className="page-header">
        <h1 className="page-title">Search Trains</h1>
        <p className="page-subtitle">Find available trains between stations</p>
      </div>

      {/* Search Form */}
      <div className="card glow-gold" style={{ marginBottom: '28px' }}>
        <form onSubmit={handleSearch}>
          <div className="form-grid form-grid-2" style={{ marginBottom: '16px' }}>
            <div className="form-group">
              <label className="form-label">From Station</label>
              <select id="search-from" className="form-select" value={form.from}
                onChange={(e) => setForm({ ...form, from: e.target.value })} required>
                <option value="">Select departure station</option>
                {stations.map((s) => (
                  <option key={s.id} value={s.code}>{s.name} ({s.code})</option>
                ))}
              </select>
            </div>
            <div className="form-group">
              <label className="form-label">To Station</label>
              <select id="search-to" className="form-select" value={form.to}
                onChange={(e) => setForm({ ...form, to: e.target.value })} required>
                <option value="">Select destination station</option>
                {stations.map((s) => (
                  <option key={s.id} value={s.code}>{s.name} ({s.code})</option>
                ))}
              </select>
            </div>
          </div>
          <div className="form-grid form-grid-2" style={{ marginBottom: '20px' }}>
            <div className="form-group">
              <label className="form-label">Travel Date</label>
              <input id="search-date" type="date" className="form-input" value={form.date}
                min={new Date().toISOString().slice(0, 10)}
                onChange={(e) => setForm({ ...form, date: e.target.value })} required />
            </div>
            <div className="form-group">
              <label className="form-label">Number of Seats</label>
              <input id="search-seats" type="number" className="form-input" min={1} max={6}
                value={form.seats} onChange={(e) => setForm({ ...form, seats: parseInt(e.target.value) })} />
            </div>
          </div>
          <button id="search-submit" type="submit" className="btn btn-primary btn-lg" disabled={loading}>
            <Search size={18} />
            {loading ? 'Searching...' : 'Search Trains'}
          </button>
        </form>
      </div>

      {/* Results */}
      {searched && !loading && (
        <div>
          <p className="text-secondary" style={{ marginBottom: '16px' }}>
            {trains.length} train{trains.length !== 1 ? 's' : ''} found
          </p>
          <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
            {trains.map((train) => (
              <div key={train.id} className="train-card" onClick={() => handleBook(train)}>
                <div>
                  <div className="train-name">{train.name}</div>
                  <div className="train-number">#{train.trainNumber} • {train.trainType}</div>
                </div>

                <div className="train-route">
                  <div className="station-info">
                    <div className="station-time">{formatTime(train.departureTime)}</div>
                    <div className="station-name">{train.fromStation?.code}</div>
                  </div>
                  <div className="route-line">
                    <div className="route-dots"></div>
                    <div className="route-duration"><Clock size={10} style={{ display: 'inline' }} /> {formatDuration(train.departureTime, train.arrivalTime)}</div>
                  </div>
                  <div className="station-info">
                    <div className="station-time">{formatTime(train.arrivalTime)}</div>
                    <div className="station-name">{train.toStation?.code}</div>
                  </div>
                </div>

                <div className="train-fare">
                  <div className="fare-from">From</div>
                  <div className="fare-amount">₹{train.baseFare}</div>
                  <div className="seats-left"><Users size={10} style={{ display: 'inline' }} /> {train.availableSeats} seats left</div>
                </div>

                <button className="btn btn-primary btn-sm" onClick={(e) => { e.stopPropagation(); handleBook(train); }}>
                  Book <ArrowRight size={14} />
                </button>
              </div>
            ))}
          </div>
        </div>
      )}

      {loading && <div className="loading"><div className="spinner"></div></div>}
    </div>
  );
}
