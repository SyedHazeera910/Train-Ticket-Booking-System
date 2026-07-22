import React from 'react';
import { Printer, X, Train, CheckCircle2, ShieldCheck } from 'lucide-react';

export default function TicketPrintView({ booking, onClose }) {
  if (!booking) return null;

  const handlePrint = () => {
    window.print();
  };

  const departureDateStr = booking.travelDate
    ? new Date(booking.travelDate).toLocaleDateString('en-IN', { weekday: 'short', day: 'numeric', month: 'short', year: 'numeric' })
    : booking.departureTime
      ? new Date(booking.departureTime).toLocaleDateString('en-IN', { weekday: 'short', day: 'numeric', month: 'short', year: 'numeric' })
      : 'N/A';

  // Parse passengers if array of strings "Name|Age|Gender" or array of objects
  const parsedPassengers = (booking.passengers || []).map((p, idx) => {
    if (typeof p === 'string') {
      const [name, age, gender] = p.split('|');
      return { name, age: age || '-', gender: gender || '-', seat: `S1-${idx + 1}` };
    }
    return {
      name: p.name || 'Passenger',
      age: p.age || '-',
      gender: p.gender || '-',
      seat: p.seatNumber || `S1-${idx + 1}`
    };
  });

  return (
    <div className="modal-overlay ticket-modal-overlay">
      <div className="modal ticket-modal-container">
        {/* Modal Action Controls (Screen Only) */}
        <div className="ticket-modal-actions no-print">
          <button className="btn btn-primary" onClick={handlePrint}>
            <Printer size={18} /> Print / Save as PDF
          </button>
          <button className="btn btn-secondary icon-only" onClick={onClose} title="Close">
            <X size={18} />
          </button>
        </div>

        {/* Official Printable Ticket Area */}
        <div id="ticket-print-area" className="ticket-card">
          <div className="ticket-header">
            <div className="ticket-brand">
              <Train size={32} className="text-gold" />
              <div>
                <h2>RailSuperApp E-TICKET</h2>
                <p className="ticket-subtitle">Indian Railways Electronic Reservation Slip</p>
              </div>
            </div>
            <div className="ticket-pnr-box">
              <div className="pnr-title">PNR NUMBER</div>
              <div className="pnr-val">{booking.pnr}</div>
              <div className="ticket-status-tag">
                <CheckCircle2 size={14} /> {booking.status || 'CONFIRMED'}
              </div>
            </div>
          </div>

          <div className="ticket-section-grid">
            <div className="ticket-info-block">
              <span className="info-label">Train Name & Number</span>
              <span className="info-value">{booking.trainName} ({booking.trainNumber})</span>
            </div>
            <div className="ticket-info-block">
              <span className="info-label">Class & Quota</span>
              <span className="info-value">{booking.travelClass || 'SL'} • General (GN)</span>
            </div>
            <div className="ticket-info-block">
              <span className="info-label">Date of Journey</span>
              <span className="info-value">{departureDateStr}</span>
            </div>
            <div className="ticket-info-block">
              <span className="info-label">Booking ID</span>
              <span className="info-value">#RS-{booking.id?.toString().substring(0, 8).toUpperCase()}</span>
            </div>
          </div>

          <div className="ticket-route-box">
            <div className="route-station">
              <span className="station-code">FROM</span>
              <span className="station-full">{booking.fromStation || booking.source}</span>
            </div>
            <div className="route-arrow">
              <span>➔</span>
            </div>
            <div className="route-station right">
              <span className="station-code">TO</span>
              <span className="station-full">{booking.toStation || booking.destination}</span>
            </div>
          </div>

          {/* Passengers Manifest */}
          <div className="ticket-table-container">
            <h4 className="table-title">PASSENGER DETAILS</h4>
            <table className="ticket-table">
              <thead>
                <tr>
                  <th>#</th>
                  <th>Passenger Name</th>
                  <th>Age</th>
                  <th>Gender</th>
                  <th>Status / Seat No</th>
                </tr>
              </thead>
              <tbody>
                {parsedPassengers.map((p, index) => (
                  <tr key={index}>
                    <td>{index + 1}</td>
                    <td className="font-semibold">{p.name}</td>
                    <td>{p.age} yrs</td>
                    <td>{p.gender}</td>
                    <td><span className="seat-badge">CNF / {p.seat}</span></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          {/* Payment & Fare Summary */}
          <div className="ticket-fare-box">
            <div className="fare-detail">
              <span>Ticket Fare:</span>
              <strong>₹{booking.fare?.toFixed(2)}</strong>
            </div>
            <div className="fare-detail">
              <span>Payment Mode:</span>
              <strong>RailSuperApp Wallet (Paid)</strong>
            </div>
            <div className="fare-total">
              <span>Total Amount Paid:</span>
              <span className="total-amount">₹{booking.fare?.toFixed(2)}</span>
            </div>
          </div>

          {/* Footer Notice */}
          <div className="ticket-footer-notice">
            <ShieldCheck size={16} />
            <p>This E-Ticket is valid with a government-approved original ID proof (Aadhaar / PAN / Passport / Driving License).</p>
          </div>
        </div>
      </div>
    </div>
  );
}
