import React, { useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { Plus, Minus, User, Train } from 'lucide-react';
import { createBooking } from '../api';
import toast from 'react-hot-toast';

const CLASSES = [
  { code: 'SLEEPER', label: 'Sleeper (SL)', multiplier: 1.0 },
  { code: 'AC3', label: 'AC 3 Tier (3A)', multiplier: 1.5 },
  { code: 'AC2', label: 'AC 2 Tier (2A)', multiplier: 2.0 },
  { code: 'AC1', label: 'AC First Class (1A)', multiplier: 3.0 },
];

export default function BookingPage() {
  const { state } = useLocation();
  const navigate = useNavigate();
  const train = state?.train;
  const [travelClass, setTravelClass] = useState('SLEEPER');
  const [passengers, setPassengers] = useState([{ name: '', age: '', gender: 'MALE' }]);
  const [loading, setLoading] = useState(false);

  if (!train) {
    navigate('/search');
    return null;
  }

  const classInfo = CLASSES.find((c) => c.code === travelClass);
  const fare = (train.baseFare * classInfo.multiplier * passengers.length).toFixed(2);

  const addPassenger = () => {
    if (passengers.length < 6) setPassengers([...passengers, { name: '', age: '', gender: 'MALE' }]);
  };
  const removePassenger = (i) => {
    if (passengers.length > 1) setPassengers(passengers.filter((_, idx) => idx !== i));
  };
  const updatePassenger = (i, field, value) => {
    const updated = [...passengers];
    updated[i][field] = value;
    setPassengers(updated);
  };

  const handleBook = async () => {
    if (passengers.some((p) => !p.name || !p.age)) {
      toast.error('Please fill all passenger details');
      return;
    }
    setLoading(true);
    try {
      const { data } = await createBooking({
        trainId: train.id,
        travelClass,
        numberOfSeats: passengers.length,
        passengers: passengers.map((p) => `${p.name}|${p.age}|${p.gender}`),
      });
      toast.success(`Booking confirmed! PNR: ${data.pnr}`);
      navigate('/bookings');
    } catch (err) {
      toast.error(err.response?.data?.message || 'Booking failed. Check your wallet balance.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="page">
      <div className="page-header">
        <h1 className="page-title">Book Ticket</h1>
        <p className="page-subtitle">{train.name} — {train.fromStation?.name} → {train.toStation?.name}</p>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 320px', gap: '24px' }}>
        {/* Left */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
          {/* Train Summary */}
          <div className="card">
            <div className="flex items-center gap-2 mb-4">
              <Train size={18} className="text-gold" />
              <span className="font-bold">Train Details</span>
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <div>
                <div className="font-bold">{train.name}</div>
                <div className="text-xs text-secondary">#{train.trainNumber} • {train.trainType}</div>
              </div>
              <div className="flex gap-3" style={{ textAlign: 'center' }}>
                <div>
                  <div className="font-bold">{new Date(train.departureTime).toLocaleTimeString('en-IN', { hour: '2-digit', minute: '2-digit', hour12: false })}</div>
                  <div className="text-xs text-secondary">{train.fromStation?.name}</div>
                </div>
                <div className="text-muted">→</div>
                <div>
                  <div className="font-bold">{new Date(train.arrivalTime).toLocaleTimeString('en-IN', { hour: '2-digit', minute: '2-digit', hour12: false })}</div>
                  <div className="text-xs text-secondary">{train.toStation?.name}</div>
                </div>
              </div>
            </div>
          </div>

          {/* Class Selection */}
          <div className="card">
            <div className="font-bold mb-4">Select Travel Class</div>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '10px' }}>
              {CLASSES.map((c) => (
                <div key={c.code}
                  onClick={() => setTravelClass(c.code)}
                  style={{
                    padding: '14px', borderRadius: '10px', border: '1px solid',
                    borderColor: travelClass === c.code ? 'var(--accent-gold)' : 'var(--border-color)',
                    background: travelClass === c.code ? 'rgba(251,168,0,0.08)' : 'transparent',
                    cursor: 'pointer', transition: 'all 0.2s'
                  }}>
                  <div className="font-bold" style={{ fontSize: '0.85rem' }}>{c.label}</div>
                  <div className="text-xs text-gold">₹{(train.baseFare * c.multiplier).toFixed(0)} / seat</div>
                </div>
              ))}
            </div>
          </div>

          {/* Passengers */}
          <div className="card">
            <div className="flex justify-between items-center mb-4">
              <div className="font-bold">Passenger Details</div>
              <button className="btn btn-secondary btn-sm" onClick={addPassenger} disabled={passengers.length >= 6}>
                <Plus size={14} /> Add
              </button>
            </div>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
              {passengers.map((p, i) => (
                <div key={i} style={{ padding: '14px', background: 'rgba(255,255,255,0.03)', borderRadius: '10px', border: '1px solid var(--border-color)' }}>
                  <div className="flex justify-between items-center mb-3">
                    <div className="flex items-center gap-2 text-sm font-bold"><User size={14} /> Passenger {i + 1}</div>
                    {passengers.length > 1 && (
                      <button className="btn btn-danger btn-sm" onClick={() => removePassenger(i)}><Minus size={12} /></button>
                    )}
                  </div>
                  <div className="form-grid form-grid-3">
                    <div className="form-group">
                      <label className="form-label">Name</label>
                      <input type="text" className="form-input" placeholder="Full name" value={p.name}
                        onChange={(e) => updatePassenger(i, 'name', e.target.value)} />
                    </div>
                    <div className="form-group">
                      <label className="form-label">Age</label>
                      <input type="number" className="form-input" placeholder="25" min={1} max={120} value={p.age}
                        onChange={(e) => updatePassenger(i, 'age', e.target.value)} />
                    </div>
                    <div className="form-group">
                      <label className="form-label">Gender</label>
                      <select className="form-select" value={p.gender}
                        onChange={(e) => updatePassenger(i, 'gender', e.target.value)}>
                        <option value="MALE">Male</option>
                        <option value="FEMALE">Female</option>
                        <option value="OTHER">Other</option>
                      </select>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>

        {/* Right — Fare Summary */}
        <div>
          <div className="card glow-gold" style={{ position: 'sticky', top: '84px' }}>
            <div className="font-bold" style={{ marginBottom: '16px', fontSize: '1rem' }}>Fare Summary</div>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '10px', marginBottom: '16px' }}>
              <div className="flex justify-between text-sm">
                <span className="text-secondary">Class</span>
                <span>{classInfo.label}</span>
              </div>
              <div className="flex justify-between text-sm">
                <span className="text-secondary">Passengers</span>
                <span>{passengers.length}</span>
              </div>
              <div className="flex justify-between text-sm">
                <span className="text-secondary">Base fare/seat</span>
                <span>₹{(train.baseFare * classInfo.multiplier).toFixed(0)}</span>
              </div>
              <div className="divider" style={{ margin: '4px 0' }}></div>
              <div className="flex justify-between font-bold">
                <span>Total</span>
                <span className="text-gold" style={{ fontSize: '1.2rem' }}>₹{fare}</span>
              </div>
            </div>
            <div style={{ padding: '10px', background: 'rgba(0,212,184,0.08)', borderRadius: '8px', border: '1px solid rgba(0,212,184,0.2)', fontSize: '0.75rem', color: 'var(--accent-teal)', marginBottom: '16px' }}>
              💳 Amount will be deducted from your wallet
            </div>
            <button id="confirm-booking" className="btn btn-primary btn-lg btn-full" onClick={handleBook} disabled={loading}>
              {loading ? 'Confirming...' : `Confirm & Pay ₹${fare}`}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
