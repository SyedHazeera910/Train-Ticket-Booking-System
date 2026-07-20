import axios from 'axios';

const API = axios.create({
  baseURL: 'http://localhost:8080/api',
});

API.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

API.interceptors.response.use(
  (res) => res,
  (err) => {
    if (err.response?.status === 401) {
      localStorage.clear();
      window.location.href = '/login';
    }
    return Promise.reject(err);
  }
);

export default API;

// Auth
export const register = (data) => API.post('/auth/register', data);
export const login = (data) => API.post('/auth/login', data);
export const getMe = () => API.get('/auth/me');

// Stations
export const getStations = (q) => API.get('/stations', { params: q ? { q } : {} });

// Trains
export const searchTrains = (from, to, date, seats) =>
  API.get('/trains/search', { params: { from, to, date, seats } });

// Bookings
export const createBooking = (data) => API.post('/bookings', data);
export const getMyBookings = () => API.get('/bookings/my');
export const getPnrStatus = (pnr) => API.get(`/bookings/pnr/${pnr}`);
export const cancelBooking = (id) => API.post(`/bookings/${id}/cancel`);

// Wallet
export const getWallet = () => API.get('/wallet');
export const topUpWallet = (amount) => API.post('/wallet/topup', { amount });

// Tracking
export const getTrackingPositions = () => API.get('/tracking');
export const getTrainPosition = (trainId) => API.get(`/tracking/${trainId}`);

// Food
export const getFoodMenu = () => API.get('/food/menu');
export const placeFoodOrder = (data) => API.post('/food/order', data);
export const getMyFoodOrders = () => API.get('/food/orders/my');

// Support
export const createTicket = (data) => API.post('/support/tickets', data);
export const getMyTickets = () => API.get('/support/tickets/my');

// Chatbot
export const sendChatMessage = (data) => API.post('/chatbot/message', data);
