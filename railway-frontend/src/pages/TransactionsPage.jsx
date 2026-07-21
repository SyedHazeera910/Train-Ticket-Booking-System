import React, { useEffect, useState } from 'react';
import { getWallet } from '../api';
import toast from 'react-hot-toast';
import { ArrowDownLeft, ArrowUpRight, ReceiptText } from 'lucide-react';

export default function TransactionsPage() {
  const [wallet, setWallet] = useState(null);
  const [loading, setLoading] = useState(true);

  const fetchTransactions = async () => {
    try {
      const { data } = await getWallet();
      setWallet(data);
    } catch {
      toast.error('Failed to load transactions');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchTransactions();
  }, []);

  const totalDebits = wallet?.transactions
    ?.filter((t) => t.type === 'DEBIT')
    .reduce((sum, t) => sum + Number(t.amount), 0) || 0;

  const totalRefunds = wallet?.transactions
    ?.filter(t => t.type === 'CREDIT' && t.description?.startsWith('Refund'))
    .reduce((sum, t) => sum + Number(t.amount), 0) || 0;

  const totalSpent = totalDebits;

  const totalAdded = wallet?.transactions
    ?.filter((t) => t.type === 'CREDIT')
    .reduce((sum, t) => sum + Number(t.amount), 0) || 0;

  return (
    <div className="page">
      <div className="page-header">
        <h1 className="page-title">Transactions</h1>
        <p className="page-subtitle">A detailed ledger of your wallet activities</p>
      </div>

      {loading ? (
        <div className="loading">
          <div className="spinner"></div>
        </div>
      ) : (
        <div className="flex flex-col gap-6">
          <div className="form-grid form-grid-3 text-sm">
            <div className="card">
              <div className="text-secondary mb-1">Current Balance</div>
              <div className="font-bold text-2xl text-primary">
                ₹{wallet?.balance?.toFixed(2) || '0.00'}
              </div>
            </div>
            <div className="card">
              <div className="text-secondary mb-1">Total Spent</div>
              <div className="font-bold text-2xl" style={{ color: 'var(--accent-red)' }}>
                -₹{totalSpent.toFixed(2)}
              </div>
            </div>
            <div className="card">
              <div className="text-secondary mb-1">Total Credited</div>
              <div className="font-bold text-2xl" style={{ color: 'var(--accent-green)' }}>
                +₹{totalAdded.toFixed(2)}
              </div>
            </div>
          </div>

          <div className="card">
            <h2 className="section-title flex gap-2 items-center mb-4">
              <ReceiptText size={20} /> Transaction Ledger
            </h2>
            
            {wallet?.transactions?.length === 0 ? (
              <p className="text-secondary text-sm">No transactions yet.</p>
            ) : (
              <div className="txn-list">
                {wallet?.transactions?.map((t) => (
                  <div key={t.id} className="txn-item" style={{ padding: '16px 0', borderBottom: '1px solid var(--border-color)' }}>
                    <div className="flex items-center gap-3">
                      <div
                        style={{
                          padding: '12px',
                          borderRadius: '50%',
                          background: t.type === 'CREDIT' ? 'rgba(34,197,94,0.1)' : 'rgba(239,68,68,0.1)',
                          color: t.type === 'CREDIT' ? 'var(--accent-green)' : 'var(--accent-red)',
                        }}
                      >
                        {t.type === 'CREDIT' ? <ArrowDownLeft size={20} /> : <ArrowUpRight size={20} />}
                      </div>
                      <div>
                        <div className="font-bold text-base" style={{ color: 'var(--text-color)' }}>
                          {t.description}
                        </div>
                        <div className="text-xs text-muted mt-1">
                          {new Date(t.createdAt).toLocaleString('en-IN', {
                            dateStyle: 'medium',
                            timeStyle: 'short',
                          })}
                        </div>
                      </div>
                    </div>
                    <div
                      style={{
                        fontWeight: 'bold',
                        fontSize: '18px',
                        color: t.type === 'CREDIT' ? 'var(--accent-green)' : 'var(--accent-red)',
                      }}
                    >
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
