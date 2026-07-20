import React, { useEffect, useState } from 'react';
import { createTicket, getMyTickets } from '../api';
import toast from 'react-hot-toast';

export default function SupportPage() {
  const [tickets, setTickets] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  
  const [form, setForm] = useState({ subject: '', description: '', pnrReference: '' });
  const [submitting, setSubmitting] = useState(false);

  const fetchTickets = async () => {
    try {
      const { data } = await getMyTickets();
      setTickets(data);
    } catch {
      toast.error('Failed to load tickets');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchTickets();
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    try {
      await createTicket(form);
      toast.success('Support ticket created successfully');
      setForm({ subject: '', description: '', pnrReference: '' });
      setShowForm(false);
      fetchTickets();
    } catch {
      toast.error('Failed to create ticket');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="page">
      <div className="page-header flex justify-between items-end">
        <div>
          <h1 className="page-title">Help & Support</h1>
          <p className="page-subtitle">Need assistance with your journey?</p>
        </div>
        {!showForm && (
          <button className="btn btn-primary btn-sm" onClick={() => setShowForm(true)}>Raise Ticket</button>
        )}
      </div>

      {showForm && (
        <div className="card mb-4">
          <div className="font-bold mb-4">New Support Ticket</div>
          <form onSubmit={handleSubmit} className="flex flex-col gap-3">
            <div className="form-group">
              <label className="form-label">Subject</label>
              <input type="text" className="form-input" required 
                value={form.subject} onChange={e => setForm({...form, subject: e.target.value})} />
            </div>
            <div className="form-group">
              <label className="form-label">PNR Reference (Optional)</label>
              <input type="text" className="form-input"
                value={form.pnrReference} onChange={e => setForm({...form, pnrReference: e.target.value})} />
            </div>
            <div className="form-group">
              <label className="form-label">Description</label>
              <textarea className="form-input" rows="4" required
                value={form.description} onChange={e => setForm({...form, description: e.target.value})}></textarea>
            </div>
            <div className="flex gap-2 justify-end mt-2">
              <button type="button" className="btn btn-secondary" onClick={() => setShowForm(false)}>Cancel</button>
              <button type="submit" className="btn btn-primary" disabled={submitting}>
                {submitting ? 'Submitting...' : 'Submit'}
              </button>
            </div>
          </form>
        </div>
      )}

      {loading ? (
        <div className="loading"><div className="spinner"></div></div>
      ) : (
        <div className="ticket-list">
          {tickets.length === 0 && !showForm ? (
            <p className="text-secondary text-sm">No support tickets found.</p>
          ) : (
            tickets.map(t => (
              <div key={t.id} className="ticket-item">
                <div className="flex justify-between items-start mb-2">
                  <div className="ticket-subject">{t.subject}</div>
                  <span className={`badge badge-${t.status.toLowerCase()}`}>{t.status}</span>
                </div>
                <div className="ticket-desc">{t.description}</div>
                {t.pnrReference && (
                  <div className="text-xs text-gold mt-2 font-bold">PNR: {t.pnrReference}</div>
                )}
                {t.adminReply && (
                  <div className="mt-3" style={{ padding: '10px', background: 'rgba(0,212,184,0.1)', borderLeft: '3px solid var(--accent-teal)', fontSize: '0.8rem' }}>
                    <div className="font-bold text-teal mb-1">Support Reply:</div>
                    {t.adminReply}
                  </div>
                )}
                <div className="ticket-meta">Ticket #{t.id} • {new Date(t.createdAt).toLocaleString()}</div>
              </div>
            ))
          )}
        </div>
      )}
    </div>
  );
}
