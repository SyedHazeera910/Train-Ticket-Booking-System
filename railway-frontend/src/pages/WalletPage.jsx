import React, { useEffect, useState } from 'react';
import { Plus, ArrowDownLeft, ArrowUpRight } from 'lucide-react';
import { getWallet, topUpWallet } from '../api';
import toast from 'react-hot-toast';

export default function WalletPage() {
  const [wallet, setWallet] = useState(null);
  const [loading, setLoading] = useState(true);
  const [topupAmount, setTopupAmount] = useState(500);

  const fetchWallet = async () => {
    try {
      const { data } = await getWallet();
      setWallet(data);
    } catch {
      toast.error('Failed to load wallet');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchWallet();
  }, []);

  const handleTopup = async () => {
    if (topupAmount <= 0) return;
    try {
      const { data } = await topUpWallet(topupAmount);
      setWallet(data);
      toast.success(`₹${topupAmount} added to wallet successfully`);
      setTopupAmount(500);
    } catch {
      toast.error('Top-up failed');
    }
  };

  return (
    <div className="page">
      <div className="page-header">
        <h1 className="page-title">My Wallet</h1>
        <p className="page-subtitle">Manage your funds for seamless ticket and food booking</p>
      </div>

      {loading ? (
        <div className="loading"><div className="spinner"></div></div>
      ) : (
        <div style={{ display: 'grid', gridTemplateColumns: 'minmax(300px, 400px) 1fr', gap: '32px' }}>
          <div>
            <div className="wallet-balance-card mb-4">
              <div className="wallet-label">Available Balance</div>
              <div className="wallet-amount">₹{wallet?.balance?.toFixed(2) || '0.00'}</div>
            </div>

            <div className="card">
              <div className="font-bold mb-3">Add Money to Wallet</div>
              <div className="flex gap-2 mb-4">
                {[500, 1000, 2000, 5000].map(amt => (
                  <button key={amt} className={`cat-btn ${topupAmount === amt ? 'active' : ''}`}
                    onClick={() => setTopupAmount(amt)} style={{ flex: 1, padding: '8px' }}>
                    +₹{amt}
                  </button>
                ))}
              </div>
              <div className="flex gap-3">
                <div style={{ position: 'relative', flex: 1 }}>
                  <span style={{ position: 'absolute', left: 14, top: '50%', transform: 'translateY(-50%)', fontWeight: 'bold' }}>₹</span>
                  <input type="number" className="form-input" style={{ paddingLeft: '30px' }}
                    value={topupAmount} onChange={(e) => setTopupAmount(Number(e.target.value))} />
                </div>
                <button className="btn btn-teal" onClick={handleTopup}><Plus size={16} /> Top Up</button>
              </div>
            </div>
          </div>

          <div>
            <h2 className="section-title">Transaction History</h2>
            {wallet?.transactions?.length === 0 ? (
              <p className="text-secondary text-sm">No transactions yet.</p>
            ) : (
              <div className="txn-list">
                {wallet?.transactions?.map((t) => (
                  <div key={t.id} className="txn-item">
                    <div className="flex items-center gap-3">
                      <div style={{ padding: '8px', borderRadius: '50%', background: t.type === 'CREDIT' ? 'rgba(34,197,94,0.1)' : 'rgba(239,68,68,0.1)', color: t.type === 'CREDIT' ? 'var(--accent-green)' : 'var(--accent-red)' }}>
                        {t.type === 'CREDIT' ? <ArrowDownLeft size={16} /> : <ArrowUpRight size={16} />}
                      </div>
                      <div>
                        <div className="txn-desc">{t.description}</div>
                        <div className="txn-time">{new Date(t.createdAt).toLocaleString('en-IN')}</div>
                      </div>
                    </div>
                    <div className={t.type === 'CREDIT' ? 'txn-credit' : 'txn-debit'}>
                      {t.type === 'CREDIT' ? '+' : '-'}₹{t.amount.toFixed(2)}
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
}
