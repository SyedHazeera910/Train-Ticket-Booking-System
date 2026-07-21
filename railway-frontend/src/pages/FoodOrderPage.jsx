import React, { useEffect, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { ShoppingCart } from 'lucide-react';
import { getFoodMenu, placeFoodOrder, getMyBookings, getWallet } from '../api';
import toast from 'react-hot-toast';

export default function FoodOrderPage() {
  const { state } = useLocation();
  const navigate = useNavigate();

  const [menu, setMenu] = useState([]);
  const [cart, setCart] = useState({});
  const [loading, setLoading] = useState(true);
  const [walletBalance, setWalletBalance] = useState(null);
  const [bookings, setBookings] = useState([]);
  const [selectedBooking, setSelectedBooking] = useState(state?.bookingId || '');
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    Promise.all([getFoodMenu(), getMyBookings(), getWallet()])
      .then(([mRes, bRes, wRes]) => {
        setMenu(mRes.data);
        setWalletBalance(wRes.data.balance);
        const activeBookings = bRes.data.filter(b => b.status === 'CONFIRMED');
        setBookings(activeBookings);
        if (!selectedBooking && activeBookings.length > 0) {
          setSelectedBooking(activeBookings[0].id);
        }
      })
      .catch(() => toast.error('Failed to load data'))
      .finally(() => setLoading(false));
  }, [selectedBooking]);

  const updateCart = (id, delta) => {
    setCart(prev => {
      const current = prev[id] || 0;
      const next = current + delta;
      if (next <= 0) {
        const { [id]: _, ...rest } = prev;
        return rest;
      }
      return { ...prev, [id]: next };
    });
  };

  const cartTotal = Object.entries(cart).reduce((sum, [id, qty]) => {
    const item = menu.find(m => m.id.toString() === id);
    return sum + (item ? item.price * qty : 0);
  }, 0);

  const cartCount = Object.values(cart).reduce((a,b) => a+b, 0);

  const handleOrder = async () => {
    if (!selectedBooking) {
      toast.error('Select a confirmed booking to deliver to'); return;
    }
    if (cartCount === 0) return;

    const booking = bookings.find(b => b.id.toString() === selectedBooking.toString());
    
    setIsSubmitting(true);
    try {
      const itemsJson = JSON.stringify(Object.entries(cart).map(([id, qty]) => {
        const i = menu.find(m => m.id.toString() === id);
        return { itemId: i.id, name: i.name, qty, price: i.price };
      }));

      await placeFoodOrder({
        bookingId: parseInt(selectedBooking),
        trainId: 1, // simplified
        itemsJson,
        total: cartTotal
      });
      toast.success('Food order placed! Rs.' + cartTotal + ' deducted from wallet');
      setWalletBalance(prev => prev - cartTotal);
      setCart({});
    } catch {
      toast.error('Order failed. Check your wallet balance.');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="page">
      <div className="page-header">
        <h1 className="page-title">E-Catering</h1>
        <p className="page-subtitle">Order food to your seat</p>
      </div>

      {loading ? (
        <div className="loading"><div className="spinner"></div></div>
      ) : (
        <>
          <div className="form-group mb-4" style={{ maxWidth: 400 }}>
            <label className="form-label">Deliver to Booking (PNR)</label>
            <select className="form-select" value={selectedBooking} onChange={e => setSelectedBooking(e.target.value)}>
              <option value="">Select a confirmed booking...</option>
              {bookings.map(b => (
                <option key={b.id} value={b.id}>{b.pnr} - {b.trainName}</option>
              ))}
            </select>
            {bookings.length === 0 && <div className="error-msg mt-2">No confirmed bookings found. Book a ticket first.</div>}
          </div>

          <div className="food-grid">
            {menu.map(item => (
              <div key={item.id} className="food-card flex flex-col justify-between">
                <div>
                  <div className="flex items-center gap-2 mb-2">
                    <span className={item.vegetarian ? 'veg-dot' : 'nonveg-dot'}></span>
                    <span className="food-name">{item.name}</span>
                  </div>
                  <div className="text-xs text-muted font-bold mb-2">{item.category}</div>
                  <div className="food-desc">{item.description}</div>
                </div>
                
                <div className="food-footer">
                  <div className="food-price">₹{item.price}</div>
                  {cart[item.id] ? (
                    <div className="flex items-center gap-3">
                      <button className="btn btn-secondary btn-sm" onClick={() => updateCart(item.id, -1)}>-</button>
                      <span className="font-bold">{cart[item.id]}</span>
                      <button className="btn btn-secondary btn-sm" onClick={() => updateCart(item.id, 1)}>+</button>
                    </div>
                  ) : (
                    <button className="btn btn-secondary btn-sm" onClick={() => updateCart(item.id, 1)}>Add</button>
                  )}
                </div>
              </div>
            ))}
          </div>

          {cartCount > 0 && (
            <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
              <div className="cart-badge" onClick={handleOrder} style={{ marginBottom: '8px' }}>
                <ShoppingCart size={18} />
                {isSubmitting ? 'Ordering...' : `Place Order (₹${cartTotal})`}
              </div>
              <div style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>
                Wallet Balance: <strong>₹{walletBalance !== null ? walletBalance.toFixed(2) : '...'}</strong>
              </div>
            </div>
          )}
        </>
      )}
    </div>
  );
}
