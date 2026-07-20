import React, { useEffect, useState } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { getTrackingPositions } from '../api';
import { MapPin, Navigation } from 'lucide-react';
import toast from 'react-hot-toast';

export default function TrackingPage() {
  const [positions, setPositions] = useState([]);
  const [selectedTrainId, setSelectedTrainId] = useState(null);
  const [loading, setLoading] = useState(true);

  // Initialize and load all positions via REST
  useEffect(() => {
    getTrackingPositions()
      .then((res) => setPositions(res.data))
      .catch(() => toast.error('Failed to load tracking data'))
      .finally(() => setLoading(false));
  }, []);

  // Connect to WebSocket with STOMP
  useEffect(() => {
    const client = new Client({
      webSocketFactory: () => new SockJS('http://localhost:8080/ws'),
      debug: function (str) { /* console.log(str) */ },
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
    });

    client.onConnect = () => {
      // Subscribe to all train topics
      positions.forEach(pos => {
        client.subscribe(`/topic/tracking/${pos.trainId}`, (message) => {
          const updatedPos = JSON.parse(message.body);
          setPositions(prev => prev.map(p => p.trainId === updatedPos.trainId ? updatedPos : p));
        });
      });
    };

    if (positions.length > 0) {
      client.activate();
    }

    return () => {
      if (client.active) client.deactivate();
    };
  }, [positions.length]);

  return (
    <div className="page">
      <div className="page-header">
        <h1 className="page-title">Live Tracking</h1>
        <p className="page-subtitle">Real-time GPS tracking of active trains <span style={{ marginLeft: 8, fontSize: '0.75rem', padding: '2px 8px', borderRadius: 12, background: 'rgba(0,212,184,0.1)', color: 'var(--accent-teal)' }}>LIVE</span></p>
      </div>

      {loading ? (
        <div className="loading"><div className="spinner"></div></div>
      ) : (
        <div className="tracking-grid">
          {/* Mock Map */}
          <div className="map-container">
            <div className="map-grid"></div>
            {positions.length === 0 ? (
              <div className="map-placeholder">No tracking data available</div>
            ) : (
              positions.map((pos) => {
                // Normalize lat/long to percentage for our fixed size CSS map
                // assuming bounds: Lat 15 to 30, Long 70 to 90 roughly
                let top = 100 - ((pos.latitude - 15) / 15 * 100);
                let left = ((pos.longitude - 70) / 20 * 100);

                // constrain to 5-95% for visual display
                top = Math.max(5, Math.min(95, top));
                left = Math.max(5, Math.min(95, left));

                const isSelected = selectedTrainId === pos.trainId;

                return (
                  <div key={pos.trainId} className="train-dot"
                    style={{
                      top: `${top}%`, left: `${left}%`,
                      transform: isSelected ? 'scale(1.5)' : 'scale(1)',
                      borderColor: isSelected ? 'var(--accent-teal)' : '#fff',
                      zIndex: isSelected ? 10 : 1
                    }}
                    onClick={() => setSelectedTrainId(pos.trainId)}>
                    {isSelected && (
                      <div style={{ position: 'absolute', top: -30, left: '50%', transform: 'translateX(-50%)', background: 'var(--bg-secondary)', border: '1px solid var(--border-glow)', padding: '4px 8px', borderRadius: 4, fontSize: '0.7rem', whiteSpace: 'nowrap', color: 'var(--accent-gold)', fontWeight: 'bold' }}>
                        Train #{pos.trainId}
                      </div>
                    )}
                  </div>
                );
              })
            )}
          </div>

          <div className="train-list-panel">
            <h3 className="font-bold mb-2 flex items-center gap-2"><Navigation size={16} /> Active Trains</h3>
            {positions.map(pos => (
              <div key={pos.trainId}
                className={`train-tracking-item ${selectedTrainId === pos.trainId ? 'selected' : ''}`}
                onClick={() => setSelectedTrainId(pos.trainId)}>
                <div className="flex justify-between items-center mb-1">
                  <span className="font-bold">Train #{pos.trainId}</span>
                  <span className="speed-badge">{pos.speed?.toFixed(0)} km/h</span>
                </div>
                <div className="text-xs text-secondary flex items-center gap-1">
                  <MapPin size={12} /> {pos.currentStation || 'In transit'}
                </div>
                <div className="text-xs text-muted mt-2" style={{ fontFamily: 'monospace' }}>
                  Lat: {pos.latitude?.toFixed(4)}, Lng: {pos.longitude?.toFixed(4)}
                </div>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}
