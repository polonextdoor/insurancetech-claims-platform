import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { claimsAPI } from '../../services/api';
import { Claim } from '../../types';
import DashboardLayout from '../common/DashboardLayout';

const ClaimsList: React.FC = () => {
  const [claims, setClaims] = useState<Claim[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const { user, isAdmin, isAdjuster } = useAuth();

  useEffect(() => {
    fetchClaims();
  }, [isAdmin, isAdjuster]);

  const fetchClaims = async () => {
    try {
      setLoading(true);
      const data = isAdmin || isAdjuster 
        ? await claimsAPI.getAllClaims()
        : await claimsAPI.getMyClaims();
      setClaims(data);
    } catch (err: any) {
      setError(err.response?.data?.error || 'Failed to load claims');
    } finally {
      setLoading(false);
    }
  };

  const getStatusColor = (status: string) => {
    const colors: { [key: string]: string } = {
      DRAFT: 'bg-gray-100 text-gray-800',
      SUBMITTED: 'bg-blue-100 text-blue-800',
      UNDER_REVIEW: 'bg-yellow-100 text-yellow-800',
      INVESTIGATING: 'bg-orange-100 text-orange-800',
      APPROVED: 'bg-green-100 text-green-800',
      DENIED: 'bg-red-100 text-red-800',
      CLOSED: 'bg-gray-100 text-gray-800',
    };
    return colors[status] || 'bg-gray-100 text-gray-800';
  };

  const getRiskColor = (level: string) => {
    const colors: { [key: string]: string } = {
      LOW: 'text-green-600',
      MEDIUM: 'text-yellow-600',
      HIGH: 'text-orange-600',
      CRITICAL: 'text-red-600',
    };
    return colors[level] || 'text-gray-600';
  };

  if (loading) {
    return (
      <DashboardLayout>
        <div className="flex justify-center items-center h-64">
          <div className="text-xl text-gray-600">Loading claims...</div>
        </div>
      </DashboardLayout>
    );
  }

  return (
    <DashboardLayout>
      <div className="px-4 sm:px-6 lg:px-8">
        <div className="sm:flex sm:items-center">
          <div className="sm:flex-auto">
            <h1 className="text-3xl font-bold text-gray-900">
              {isAdmin || isAdjuster ? 'All Claims' : 'My Claims'}
            </h1>
            <p className="mt-2 text-sm text-gray-700">
              {isAdmin || isAdjuster 
                ? 'View and manage all insurance claims'
                : 'View your submitted insurance claims'}
            </p>
          </div>
          <div className="mt-4 sm:mt-0 sm:ml-16 sm:flex-none">
            <Link
              to="/claims/new"
              className="inline-flex items-center justify-center rounded-md border border-transparent bg-primary-600 px-4 py-2 text-sm font-medium text-white shadow-sm hover:bg-primary-700 focus:outline-none focus:ring-2 focus:ring-primary-500 focus:ring-offset-2"
            >
              Submit New Claim
            </Link>
          </div>
        </div>

        {error && (
          <div className="mt-4 bg-red-50 border border-red-200 text-red-600 px-4 py-3 rounded">
            {error}
          </div>
        )}

        {claims.length === 0 ? (
          <div className="mt-8 text-center">
            <svg className="mx-auto h-12 w-12 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
            </svg>
            <h3 className="mt-2 text-sm font-medium text-gray-900">No claims</h3>
            <p className="mt-1 text-sm text-gray-500">Get started by submitting a new claim.</p>
            <div className="mt-6">
              <Link
                to="/claims/new"
                className="inline-flex items-center px-4 py-2 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-primary-600 hover:bg-primary-700"
              >
                Submit New Claim
              </Link>
            </div>
          </div>
        ) : (
          <div className="mt-8 flex flex-col">
            <div className="-my-2 -mx-4 overflow-x-auto sm:-mx-6 lg:-mx-8">
              <div className="inline-block min-w-full py-2 align-middle md:px-6 lg:px-8">
                <div className="overflow-hidden shadow ring-1 ring-black ring-opacity-5 md:rounded-lg">
                  <table className="min-w-full divide-y divide-gray-300">
                    <thead className="bg-gray-50">
                      <tr>
                        <th className="px-3 py-3.5 text-left text-sm font-semibold text-gray-900">
                          Claim #
                        </th>
                        <th className="px-3 py-3.5 text-left text-sm font-semibold text-gray-900">
                          Policy
                        </th>
                        {(isAdmin || isAdjuster) && (
                          <th className="px-3 py-3.5 text-left text-sm font-semibold text-gray-900">
                            Customer
                          </th>
                        )}
                        <th className="px-3 py-3.5 text-left text-sm font-semibold text-gray-900">
                          Amount
                        </th>
                        <th className="px-3 py-3.5 text-left text-sm font-semibold text-gray-900">
                          Status
                        </th>
                        <th className="px-3 py-3.5 text-left text-sm font-semibold text-gray-900">
                          Risk
                        </th>
                        <th className="px-3 py-3.5 text-left text-sm font-semibold text-gray-900">
                          Date
                        </th>
                        <th className="relative py-3.5 pl-3 pr-4 sm:pr-6">
                          <span className="sr-only">Actions</span>
                        </th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-gray-200 bg-white">
                      {claims.map((claim) => (
                        <tr key={claim.id}>
                          <td className="whitespace-nowrap px-3 py-4 text-sm font-medium text-gray-900">
                            {claim.claimNumber}
                          </td>
                          <td className="whitespace-nowrap px-3 py-4 text-sm text-gray-500">
                            <div>{claim.policyNumber}</div>
                            <div className="text-xs text-gray-400">{claim.policyType}</div>
                          </td>
                          {(isAdmin || isAdjuster) && (
                            <td className="whitespace-nowrap px-3 py-4 text-sm text-gray-500">
                              {claim.customerName}
                            </td>
                          )}
                          <td className="whitespace-nowrap px-3 py-4 text-sm text-gray-900">
                            ${claim.claimedAmount.toLocaleString()}
                          </td>
                          <td className="whitespace-nowrap px-3 py-4 text-sm">
                            <span className={`inline-flex rounded-full px-2 text-xs font-semibold leading-5 ${getStatusColor(claim.status)}`}>
                              {claim.status.replace('_', ' ')}
                            </span>
                          </td>
                          <td className={`whitespace-nowrap px-3 py-4 text-sm font-medium ${getRiskColor(claim.riskLevel)}`}>
                            {claim.riskLevel}
                          </td>
                          <td className="whitespace-nowrap px-3 py-4 text-sm text-gray-500">
                            {new Date(claim.incidentDate).toLocaleDateString()}
                          </td>
                          <td className="relative whitespace-nowrap py-4 pl-3 pr-4 text-right text-sm font-medium sm:pr-6">
                            <Link
                              to={`/claims/${claim.id}`}
                              className="text-primary-600 hover:text-primary-900"
                            >
                              View
                            </Link>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </div>
            </div>
          </div>
        )}
      </div>
    </DashboardLayout>
  );
};

export default ClaimsList;