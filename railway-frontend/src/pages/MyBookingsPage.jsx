import React, { useEffect, useState } from 'react';
import { getMyBookings, cancelBooking, getMyFoodOrders } from '../api';
import { Train, CalendarX2, Utensils } from 'lucide-react';
import toast from 'react-hot-toast';
import { Link } from 'react-router-dom';

export default function MyBookingsPage() {
  const [bookings, setBookings] = useState([]);
  const [foodOrders, setFoodOrders] = useState([]);
  const [loading, setLoading] = useState(true);

  const fetchData = async () => {
    try {
      const [bRes, fRes] = await Promise.all([getMyBookings(), getMyFoodOrders()]);
      setBookings(bRes.data);
      setFoodOrders(fRes.data);
    } catch {
      toast.error('Failed to load data');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  const handleCancel = async (id) => {
    if (!window.confirm('Are you sure you want to cancel this booking? You will get an 80% refund.')) return;
    try {
      await cancelBooking(id);
      toast.success('Booking cancelled successfully');
      fetchData();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Cancellation failed');
    }
  };

  return (
    <div className="page">
      <div className="page-header">
        <h1 className="page-title">My Bookings</h1>
        <p className="page-subtitle">View and manage all your train tickets</p>
      </div>

      {loading ? (
        <div className="loading"><div className="spinner"></div></div>
      ) : bookings.length === 0 ? (
        <div className="empty-state">
          <CalendarX2 className="empty-state-icon" />
          <h3>No bookings found</h3>
          <p>You haven't booked any train tickets yet.</p>
          <Link to="/search" className="btn btn-primary mt-4">Book a Ticket</Link>
        </div>
      ) : (
        <div className="booking-list">
          {bookings.map((b) => (
            <div key={b.id} className="card">
              <div className="flex justify-between items-center mb-4">
                <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                  <div className="stat-icon gold"><Train size={20} /></div>
                  <div>
                    <div className="booking-train">{b.trainName} ({b.trainNumber})</div>
                    <div className="booking-pnr mt-1">PNR: <span>{b.pnr}</span></div>
                  </div>
                </div>
                <div style={{ textAlign: 'right' }}>
                  <span className={`badge badge-${b.status.toLowerCase()}`}>{b.status}</span>
                </div>
              </div>

              <div className="form-grid form-grid-3 text-sm">
                <div>
                  <div className="text-secondary mb-1">Journey</div>
                  <div className="font-bold">{b.fromStation} → {b.toStation}</div>
                  <div className="text-muted mt-1">
                    📅 {b.travelDate
                      ? new Date(b.travelDate).toLocaleDateString('en-IN', { day: 'numeric', month: 'short', year: 'numeric' })
                      : new Date(b.departureTime).toLocaleDateString('en-IN', { day: 'numeric', month: 'short', year: 'numeric' })
                    }
                  </div>
                </div>
                <div>
                  <div className="text-secondary mb-1">Details</div>
                  <div className="font-bold">{b.travelClass} • {b.numberOfSeats} Seat(s)</div>
                  <div className="text-muted mt-1">Fare: ₹{b.fare?.toFixed(2)}</div>
                </div>
                <div>
                  <div className="text-secondary mb-1">Passengers</div>
                  {b.passengers?.map((p, i) => (
                    <div key={i} className="text-muted">{p.split('|')[0]} ({p.split('|')[1]})</div>
                  ))}
                </div>
              </div>

              {foodOrders.filter(f => f.bookingId === b.id).length > 0 && (
                <div className="mt-4 pt-3" style={{ borderTop: '1px dashed var(--border-color)' }}>
                  <div className="text-secondary mb-2 flex items-center gap-2 font-semibold">
                    <Utensils size={14} /> Food Ordered
                  </div>
                  {foodOrders.filter(f => f.bookingId === b.id).map(order => (
                    <div key={order.id} className="text-sm">
                      {JSON.parse(order.itemsJson).map((item, idx) => (
                        <div key={idx} className="flex justify-between items-center text-muted mb-1">
                          <span>{item.qty}x {item.name}</span>
                          <span>₹{(item.price * item.qty).toFixed(2)}</span>
                        </div>
                      ))}
                    </div>
                  ))}
                </div>
              )}

              {b.status === 'CONFIRMED' && (
                <div className="flex gap-3 justify-end mt-4 pt-4" style={{ borderTop: '1px solid var(--border-color)' }}>
                  <Link to="/food" state={{ bookingId: b.id, trainId: b.trainNumber }} className="btn btn-secondary btn-sm">Order Food</Link>
                  <button className="btn btn-danger btn-sm" onClick={() => handleCancel(b.id)}>Cancel Ticket</button>
                </div>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
