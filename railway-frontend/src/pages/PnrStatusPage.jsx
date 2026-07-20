import React, { useState } from 'react';
import { Search } from 'lucide-react';
import { getPnrStatus } from '../api';
import toast from 'react-hot-toast';

export default function PnrStatusPage() {
  const [pnr, setPnr] = useState('');
  const [result, setResult] = useState(null);
  const [loading, setLoading] = useState(false);

  const handleSearch = async (e) => {
    e.preventDefault();
    if (!pnr.trim()) return;
    setLoading(true);
    try {
      const { data } = await getPnrStatus(pnr);
      setResult(data);
    } catch {
      toast.error('PNR not found or invalid');
      setResult(null);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="page">
      <div className="page-header">
        <h1 className="page-title">PNR Status</h1>
        <p className="page-subtitle">Check the current status of your train ticket</p>
      </div>

      <form className="pnr-search mb-4" onSubmit={handleSearch}>
        <input type="text" className="form-input" placeholder="Enter 10-digit PNR Number"
          value={pnr} onChange={(e) => setPnr(e.target.value.toUpperCase())}
          style={{ flex: 1, textTransform: 'uppercase', fontSize: '1.1rem', letterSpacing: '0.1em' }} required />
        <button type="submit" className="btn btn-primary" disabled={loading} style={{ minWidth: '120px' }}>
          <Search size={18} /> {loading ? 'Checking...' : 'Check Status'}
        </button>
      </form>

      {result && (
        <div className="pnr-result">
          <div className="flex justify-between items-center mb-4">
            <h3 className="font-bold text-lg">PNR: <span className="text-gold">{result.pnr}</span></h3>
            <span className={`badge badge-${result.status.toLowerCase()}`}>{result.status}</span>
          </div>

          <div style={{ background: 'rgba(255,255,255,0.03)', padding: '20px', borderRadius: '12px', border: '1px solid var(--border-color)' }}>
            <div className="font-bold text-lg mb-1">{result.trainName} ({result.trainNumber})</div>
            <div className="text-secondary">{result.fromStation} → {result.toStation}</div>

            <div className="pnr-info-grid">
              <div className="info-item">
                <label>Journey Date</label>
                <p>{new Date(result.departureTime).toLocaleDateString('en-IN', { weekday: 'short', day: 'numeric', month: 'short', year: 'numeric' })}</p>
              </div>
              <div className="info-item">
                <label>Travel Class</label>
                <p>{result.travelClass}</p>
              </div>
              <div className="info-item">
                <label>Total Fare</label>
                <p>₹{result.fare?.toFixed(2)}</p>
              </div>
              <div className="info-item">
                <label>Passengers</label>
                <p>{result.numberOfSeats}</p>
              </div>
            </div>
          </div>

          <div className="mt-4">
            <h4 className="font-bold mb-3">Passenger Details</h4>
            <div className="form-grid form-grid-3">
              {result.passengers?.map((p, i) => {
                const [name, age, gender] = p.split('|');
                return (
                  <div key={i} style={{ padding: '12px', background: 'var(--bg-secondary)', borderRadius: '8px', border: '1px solid var(--border-color)' }}>
                    <div className="text-xs text-muted font-bold mb-1">Passenger {i + 1}</div>
                    <div className="font-bold">{name}</div>
                    <div className="text-sm text-secondary">{age} yrs, {gender}</div>
                  </div>
                );
              })}
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
