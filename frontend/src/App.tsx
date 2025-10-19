import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import ProtectedRoute from './components/common/ProtectedRoute';
import Login from './components/auth/Login';
import Register from './components/auth/Register';
import Dashboard from './components/common/Dashboard';
import ClaimsList from './components/claims/ClaimsList';
import NewClaim from './components/claims/NewClaim';
import PoliciesList from './components/policies/PoliciesList';
import NewPolicy from './components/policies/NewPolicy';

function App() {
  return (
    <Router>
      <AuthProvider>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          
          <Route
            path="/dashboard"
            element={
              <ProtectedRoute>
                <Dashboard />
              </ProtectedRoute>
            }
          />
          
          {/* Claims Routes */}
          <Route
            path="/claims"
            element={
              <ProtectedRoute>
                <ClaimsList />
              </ProtectedRoute>
            }
          />
          <Route
            path="/claims/new"
            element={
              <ProtectedRoute>
                <NewClaim />
              </ProtectedRoute>
            }
          />
          
          {/* Policies Routes */}
          <Route
            path="/policies"
            element={
              <ProtectedRoute>
                <PoliciesList />
              </ProtectedRoute>
            }
          />
          <Route
            path="/policies/new"
            element={
              <ProtectedRoute allowedRoles={['ADMIN', 'AGENT']}>
                <NewPolicy />
              </ProtectedRoute>
            }
          />
          
          <Route path="/" element={<Navigate to="/dashboard" replace />} />
        </Routes>
      </AuthProvider>
    </Router>
  );
}

export default App;