import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { policiesAPI } from '../../services/api';
import { Policy } from '../../types';
import DashboardLayout from '../common/DashboardLayout';

const PoliciesList: React.FC = () => {
  const [policies, setPolicies] = useState<Policy[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const { isAdmin, isAgent } = useAuth();

  useEffect(() => {
    fetchPolicies();
  }, [isAdmin, isAgent]);

  const fetchPolicies = async () => {
    try {
      setLoading(true);
      const data = isAdmin || isAgent
        ? await policiesAPI.getAllPolicies()
        : await policiesAPI.getMyPolicies();
      setPolicies(data);
    } catch (err: any) {
      setError(err.response?.data?.error || 'Failed to load policies');
    } finally {
      setLoading(false);
    }
  };

  const getPolicyTypeColor = (type: string) => {
    const colors: { [key: string]: string } = {
      AUTO: 'bg-blue-100 text-blue-800',
      HOME: 'bg-green-100 text-green-800',
      HEALTH: 'bg-purple-100 text-purple-800',
      LIFE: 'bg-pink-100 text-pink-800',
      BUSINESS: 'bg-yellow-100 text-yellow-800',
    };
    return colors[type] || 'bg-gray-100 text-gray-800';
  };

  if (loading) {
    return (
      <DashboardLayout>
        <div className="flex justify-center items-center h-64">
          <div className="text-xl text-gray-600">Loading policies...</div>
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
              {isAdmin || isAgent ? 'All Policies' : 'My Policies'}
            </h1>
            <p className="mt-2 text-sm text-gray-700">
              {isAdmin || isAgent
                ? 'View and manage all insurance policies'
                : 'View your active insurance policies'}
            </p>
          </div>
          {(isAdmin || isAgent) && (
            <div className="mt-4 sm:mt-0 sm:ml-16 sm:flex-none">
              <Link
                to="/policies/new"
                className="inline-flex items-center justify-center rounded-md border border-transparent bg-primary-600 px-4 py-2 text-sm font-medium text-white shadow-sm hover:bg-primary-700 focus:outline-none focus:ring-2 focus:ring-primary-500 focus:ring-offset-2"
              >
                Create New Policy
              </Link>
            </div>
          )}
        </div>

        {error && (
          <div className="mt-4 bg-red-50 border border-red-200 text-red-600 px-4 py-3 rounded">
            {error}
          </div>
        )}

        {policies.length === 0 ? (
          <div className="mt-8 text-center">
            <svg className="mx-auto h-12 w-12 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" />
            </svg>
            <h3 className="mt-2 text-sm font-medium text-gray-900">No policies</h3>
            <p className="mt-1 text-sm text-gray-500">
              {isAdmin || isAgent 
                ? 'Get started by creating a new policy.'
                : 'You don\'t have any policies yet. Contact an administrator.'}
            </p>
            {(isAdmin || isAgent) && (
              <div className="mt-6">
                <Link
                  to="/policies/new"
                  className="inline-flex items-center px-4 py-2 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-primary-600 hover:bg-primary-700"
                >
                  Create New Policy
                </Link>
              </div>
            )}
          </div>
        ) : (
          <div className="mt-8 grid gap-6 sm:grid-cols-1 lg:grid-cols-2">
            {policies.map((policy) => (
              <div
                key={policy.id}
                className="bg-white overflow-hidden shadow rounded-lg border border-gray-200 hover:shadow-lg transition-shadow"
              >
                <div className="px-6 py-5">
                  <div className="flex items-center justify-between">
                    <div className="flex items-center">
                      <div className={`inline-flex rounded-md px-3 py-1 text-sm font-semibold ${getPolicyTypeColor(policy.policyType)}`}>
                        {policy.policyType}
                      </div>
                      {policy.isActive ? (
                        <span className="ml-2 inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800">
                          Active
                        </span>
                      ) : (
                        <span className="ml-2 inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-gray-100 text-gray-800">
                          Inactive
                        </span>
                      )}
                    </div>
                    <Link
                      to={`/policies/${policy.id}`}
                      className="text-primary-600 hover:text-primary-900 text-sm font-medium"
                    >
                      View Details →
                    </Link>
                  </div>

                  <div className="mt-4">
                    <h3 className="text-lg font-semibold text-gray-900">
                      {policy.policyNumber}
                    </h3>
                    {(isAdmin || isAgent) && (
                      <p className="mt-1 text-sm text-gray-600">
                        Customer: {policy.customerName}
                      </p>
                    )}
                  </div>

                  <dl className="mt-4 grid grid-cols-2 gap-4">
                    <div>
                      <dt className="text-xs font-medium text-gray-500 uppercase">Coverage</dt>
                      <dd className="mt-1 text-lg font-semibold text-gray-900">
                        ${policy.coverageAmount.toLocaleString()}
                      </dd>
                    </div>
                    <div>
                      <dt className="text-xs font-medium text-gray-500 uppercase">Deductible</dt>
                      <dd className="mt-1 text-lg font-semibold text-gray-900">
                        ${policy.deductible.toLocaleString()}
                      </dd>
                    </div>
                    <div>
                      <dt className="text-xs font-medium text-gray-500 uppercase">Premium</dt>
                      <dd className="mt-1 text-lg font-semibold text-gray-900">
                        ${policy.premiumAmount.toLocaleString()}/yr
                      </dd>
                    </div>
                    <div>
                      <dt className="text-xs font-medium text-gray-500 uppercase">End Date</dt>
                      <dd className="mt-1 text-sm font-medium text-gray-900">
                        {new Date(policy.endDate).toLocaleDateString()}
                      </dd>
                    </div>
                  </dl>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </DashboardLayout>
  );
};

export default PoliciesList;