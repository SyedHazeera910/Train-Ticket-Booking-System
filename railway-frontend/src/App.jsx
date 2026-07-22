import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { Toaster } from 'react-hot-toast';
import { ThemeProvider } from './context/ThemeContext';

import Navbar from './components/Navbar';
import ProtectedRoute from './components/ProtectedRoute';
import ChatbotWidget from './components/ChatbotWidget';

import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import DashboardPage from './pages/DashboardPage';
import SearchTrainsPage from './pages/SearchTrainsPage';
import BookingPage from './pages/BookingPage';
import MyBookingsPage from './pages/MyBookingsPage';
import PnrStatusPage from './pages/PnrStatusPage';
import WalletPage from './pages/WalletPage';
import TransactionsPage from './pages/TransactionsPage';
import TrackingPage from './pages/TrackingPage';
import FoodOrderPage from './pages/FoodOrderPage';
import SupportPage from './pages/SupportPage';

function App() {
  return (
    <ThemeProvider>
      <Router>
        <Toaster position="top-right" toastOptions={{ 
          style: { background: 'var(--bg-secondary)', color: 'var(--text-primary)', border: '1px solid var(--border-color)' }
        }} />
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          
          {/* Protected Routes */}
          <Route path="/*" element={
            <ProtectedRoute>
              <Navbar />
              {/* Chatbot floats over all protected pages */}
              <ChatbotWidget />
              <Routes>
                <Route path="/dashboard" element={<DashboardPage />} />
                <Route path="/search" element={<SearchTrainsPage />} />
                <Route path="/book" element={<BookingPage />} />
                <Route path="/bookings" element={<MyBookingsPage />} />
                <Route path="/pnr" element={<PnrStatusPage />} />
                <Route path="/wallet" element={<WalletPage />} />
                <Route path="/transactions" element={<TransactionsPage />} />
                <Route path="/tracking" element={<TrackingPage />} />
                <Route path="/food" element={<FoodOrderPage />} />
                <Route path="/support" element={<SupportPage />} />
                <Route path="*" element={<Navigate to="/dashboard" replace />} />
              </Routes>
            </ProtectedRoute>
          } />
        </Routes>
      </Router>
    </ThemeProvider>
  );
}

export default App;

