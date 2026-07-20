import React, { useState, useRef, useEffect, useCallback } from 'react';
import { sendChatMessage } from '../api';
import './ChatbotWidget.css';

// ── Icons (inline SVG to avoid extra deps) ──────────────────────────────────
const BotIcon = () => (
  <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
    <rect x="3" y="11" width="18" height="10" rx="2"/>
    <circle cx="12" cy="5" r="2"/>
    <path d="M12 7v4"/>
    <line x1="8" y1="16" x2="8" y2="16"/>
    <line x1="16" y1="16" x2="16" y2="16"/>
  </svg>
);

const SendIcon = () => (
  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
    <line x1="22" y1="2" x2="11" y2="13"/>
    <polygon points="22 2 15 22 11 13 2 9 22 2"/>
  </svg>
);

const CloseIcon = () => (
  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round">
    <line x1="18" y1="6" x2="6" y2="18"/>
    <line x1="6" y1="6" x2="18" y2="18"/>
  </svg>
);

const TrainIcon = () => (
  <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <rect x="4" y="3" width="16" height="13" rx="2"/>
    <path d="M4 11h16"/>
    <path d="M12 3v8"/>
    <path d="M8 19l-2 3"/>
    <path d="M18 22l-2-3"/>
    <path d="M8 16h8"/>
  </svg>
);

const UserIcon = () => (
  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round">
    <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
    <circle cx="12" cy="7" r="4"/>
  </svg>
);

// ── Quick-action chips shown at the bottom of the panel ────────────────────
const QUICK_CHIPS = [
  { label: '🎫 PNR Status', text: 'Check my PNR status' },
  { label: '🚂 Track Train', text: 'Where is my train right now?' },
  { label: '📋 My Bookings', text: 'Show my recent bookings' },
  { label: '💰 Wallet', text: 'What is my wallet balance?' },
  { label: '🍱 Food Menu', text: 'Show food menu' },
  { label: '🆘 Complaint', text: 'I have a problem' },
];

// ── Simple markdown-style bold renderer ─────────────────────────────────────
function renderMarkdown(text) {
  if (!text) return '';
  // Replace **text** with <strong>text</strong>
  const parts = text.split(/(\*\*[^*]+\*\*)/g);
  return parts.map((part, i) => {
    if (part.startsWith('**') && part.endsWith('**')) {
      return <strong key={i}>{part.slice(2, -2)}</strong>;
    }
    return part;
  });
}

// ── Main component ───────────────────────────────────────────────────────────
export default function ChatbotWidget() {
  const [open, setOpen] = useState(false);
  const [messages, setMessages] = useState([]);
  const [input, setInput] = useState('');
  const [loading, setLoading] = useState(false);
  const [sessionId] = useState(() => crypto.randomUUID());
  const [hasGreeted, setHasGreeted] = useState(false);
  const [unread, setUnread] = useState(0);

  const messagesEndRef = useRef(null);
  const inputRef = useRef(null);

  // Auto-scroll to bottom whenever messages change
  const scrollToBottom = useCallback(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, []);

  useEffect(() => {
    scrollToBottom();
  }, [messages, loading, scrollToBottom]);

  // Focus input when panel opens
  useEffect(() => {
    if (open) {
      setTimeout(() => inputRef.current?.focus(), 100);
      setUnread(0);

      // Show greeting on first open
      if (!hasGreeted) {
        setHasGreeted(true);
        setLoading(true);
        sendChatMessage({ sessionId, message: 'hello' })
          .then(({ data }) => {
            addBotMessage(data.reply);
          })
          .catch(() => {
            addBotMessage(
              "👋 Hi! I'm **RailBot**, your AI travel assistant.\n\nAsk me about PNR status, train tracking, wallet balance, food orders, and more!"
            );
          })
          .finally(() => setLoading(false));
      }
    }
  }, [open]);

  function addBotMessage(text) {
    setMessages(prev => [...prev, { role: 'bot', text }]);
  }

  function addUserMessage(text) {
    setMessages(prev => [...prev, { role: 'user', text }]);
  }

  async function handleSend(messageText) {
    const text = (messageText || input).trim();
    if (!text || loading) return;

    setInput('');
    addUserMessage(text);
    setLoading(true);

    try {
      const { data } = await sendChatMessage({ sessionId, message: text });
      addBotMessage(data.reply);
    } catch (err) {
      addBotMessage(
        "⚠️ I'm having trouble connecting right now. Please check your connection and try again."
      );
    } finally {
      setLoading(false);
    }
  }

  function handleKeyDown(e) {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSend();
    }
  }

  function handleOpen() {
    setOpen(true);
    setUnread(0);
  }

  function handleClose() {
    setOpen(false);
  }

  function handleChip(chipText) {
    handleSend(chipText);
  }

  return (
    <>
      {/* ── Floating Action Button ── */}
      <button
        id="chatbot-fab"
        className="chatbot-fab"
        onClick={open ? handleClose : handleOpen}
        aria-label={open ? 'Close RailBot' : 'Open RailBot'}
        title={open ? 'Close RailBot' : 'Chat with RailBot'}
      >
        {open ? <CloseIcon /> : <TrainIcon />}
        {!open && unread > 0 && (
          <span className="chatbot-fab-badge">{unread}</span>
        )}
      </button>

      {/* ── Chat Panel ── */}
      {open && (
        <div id="chatbot-panel" className="chatbot-panel" role="dialog" aria-label="RailBot Assistant">

          {/* Header */}
          <div className="chatbot-header">
            <div className="chatbot-header-icon">
              <BotIcon />
            </div>
            <div className="chatbot-header-info">
              <div className="chatbot-header-name">RailBot AI</div>
              <div className="chatbot-header-status">
                <span className="chatbot-status-dot" />
                Online &amp; ready to help
              </div>
            </div>
            <button
              className="chatbot-close-btn"
              onClick={handleClose}
              aria-label="Close chatbot"
            >
              <CloseIcon />
            </button>
          </div>

          {/* Messages */}
          <div className="chatbot-messages" role="log" aria-live="polite">
            {messages.length === 0 && !loading && (
              <div style={{ textAlign: 'center', color: '#475569', padding: '20px', fontSize: '0.85rem' }}>
                <div style={{ fontSize: '2rem', marginBottom: '10px' }}>🚂</div>
                Starting conversation…
              </div>
            )}

            {messages.map((msg, idx) => (
              <div key={idx} className={`chatbot-msg ${msg.role}`}>
                <div className="chatbot-msg-avatar">
                  {msg.role === 'bot' ? <BotIcon /> : <UserIcon />}
                </div>
                <div className="chatbot-msg-bubble">
                  {renderMarkdown(msg.text)}
                </div>
              </div>
            ))}

            {/* Typing indicator */}
            {loading && (
              <div className="chatbot-typing">
                <div className="chatbot-msg-avatar" style={{
                  width: '28px', height: '28px', borderRadius: '50%',
                  background: 'linear-gradient(135deg, #fba800, #f59e0b)',
                  display: 'flex', alignItems: 'center', justifyContent: 'center',
                  color: '#000', flexShrink: 0, marginTop: '2px'
                }}>
                  <BotIcon />
                </div>
                <div className="chatbot-typing-bubble">
                  <span className="chatbot-typing-dot" />
                  <span className="chatbot-typing-dot" />
                  <span className="chatbot-typing-dot" />
                </div>
              </div>
            )}

            <div ref={messagesEndRef} />
          </div>

          {/* Quick-action chips */}
          <div className="chatbot-chips">
            {QUICK_CHIPS.map((chip) => (
              <button
                key={chip.label}
                className="chatbot-chip"
                onClick={() => handleChip(chip.text)}
                disabled={loading}
              >
                {chip.label}
              </button>
            ))}
          </div>

          {/* Input bar */}
          <div className="chatbot-input-bar">
            <textarea
              ref={inputRef}
              id="chatbot-input"
              className="chatbot-input"
              placeholder="Ask me anything…"
              value={input}
              onChange={e => setInput(e.target.value)}
              onKeyDown={handleKeyDown}
              rows={1}
              disabled={loading}
              aria-label="Chat message input"
              style={{ height: '38px', lineHeight: '20px', paddingTop: '9px' }}
            />
            <button
              id="chatbot-send-btn"
              className="chatbot-send-btn"
              onClick={() => handleSend()}
              disabled={loading || !input.trim()}
              aria-label="Send message"
            >
              <SendIcon />
            </button>
          </div>
        </div>
      )}
    </>
  );
}
