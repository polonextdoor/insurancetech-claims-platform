import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { policiesAPI, claimsAPI } from '../../services/api';
import { Policy } from '../../types';
import DashboardLayout from '../common/DashboardLayout';

const NewClaim: React.FC = () => {
  const [policies, setPolicies] = useState<Policy[]>([]);
  const [formData, setFormData] = useState({
    policyId: '',
    incidentDate: '',
    incidentDescription: '',
    incidentLocation: '',
    claimedAmount: '',
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const navigate = useNavigate();

  useEffect(() => {
    fetchPolicies();
  }, []);

  const fetchPolicies = async () => {
    try {
      const data = await policiesAPI.getMyActivePolicies();
      setPolicies(data);
    } catch (err: any) {
      setError('Failed to load policies');
    }
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value,
    });
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      await claimsAPI.createClaim({
        policyId: parseInt(formData.policyId),
        incidentDate: formData.incidentDate,
        incidentDescription: formData.incidentDescription,
        incidentLocation: formData.incidentLocation,
        claimedAmount: parseFloat(formData.claimedAmount),
      });
      navigate('/claims');
    } catch (err: any) {
      setError(err.response?.data?.error || 'Failed to submit claim');
    } finally {
      setLoading(false);
    }
  };

  if (policies.length === 0 && !error) {
    return (
      <DashboardLayout>
        <div className="max-w-3xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          <div className="bg-yellow-50 border border-yellow-200 rounded-lg p-6">
            <h3 className="text-lg font-medium text-yellow-800 mb-2">
              No Active Policies
            </h3>
            <p className="text-yellow-700">
              You need an active insurance policy before you can submit a claim.
              Please contact an administrator to set up a policy for your account.
            </p>
          </div>
        </div>
      </DashboardLayout>
    );
  }

  return (
    <DashboardLayout>
      <div className="max-w-3xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="md:flex md:items-center md:justify-between mb-8">
          <div className="flex-1 min-w-0">
            <h2 className="text-3xl font-bold leading-7 text-gray-900 sm:text-3xl sm:truncate">
              Submit New Claim
            </h2>
          </div>
        </div>

        <div className="bg-white shadow sm:rounded-lg">
          <form onSubmit={handleSubmit} className="space-y-6 p-6">
            {error && (
              <div className="bg-red-50 border border-red-200 text-red-600 px-4 py-3 rounded">
                {error}
              </div>
            )}

            <div>
              <label htmlFor="policyId" className="block text-sm font-medium text-gray-700">
                Select Policy *
              </label>
              <select
                id="policyId"
                name="policyId"
                required
                value={formData.policyId}
                onChange={handleChange}
                className="mt-1 block w-full pl-3 pr-10 py-2 text-base border-gray-300 focus:outline-none focus:ring-primary-500 focus:border-primary-500 sm:text-sm rounded-md"
              >
                <option value="">Choose a policy</option>
                {policies.map((policy) => (
                  <option key={policy.id} value={policy.id}>
                    {policy.policyNumber} - {policy.policyType} (Coverage: ${policy.coverageAmount.toLocaleString()})
                  </option>
                ))}
              </select>
            </div>

            <div>
              <label htmlFor="incidentDate" className="block text-sm font-medium text-gray-700">
                Incident Date *
              </label>
              <input
                type="date"
                id="incidentDate"
                name="incidentDate"
                required
                max={new Date().toISOString().split('T')[0]}
                value={formData.incidentDate}
                onChange={handleChange}
                className="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-primary-500 focus:border-primary-500 sm:text-sm"
              />
            </div>

            <div>
              <label htmlFor="incidentLocation" className="block text-sm font-medium text-gray-700">
                Incident Location
              </label>
              <input
                type="text"
                id="incidentLocation"
                name="incidentLocation"
                value={formData.incidentLocation}
                onChange={handleChange}
                placeholder="e.g., Interstate 95, Exit 42"
                className="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-primary-500 focus:border-primary-500 sm:text-sm"
              />
            </div>

            <div>
              <label htmlFor="claimedAmount" className="block text-sm font-medium text-gray-700">
                Claimed Amount ($) *
              </label>
              <input
                type="number"
                id="claimedAmount"
                name="claimedAmount"
                required
                min="0.01"
                step="0.01"
                value={formData.claimedAmount}
                onChange={handleChange}
                placeholder="0.00"
                className="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-primary-500 focus:border-primary-500 sm:text-sm"
              />
            </div>

            <div>
              <label htmlFor="incidentDescription" className="block text-sm font-medium text-gray-700">
                Incident Description *
              </label>
              <textarea
                id="incidentDescription"
                name="incidentDescription"
                required
                rows={6}
                value={formData.incidentDescription}
                onChange={handleChange}
                placeholder="Please provide a detailed description of what happened..."
                className="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-primary-500 focus:border-primary-500 sm:text-sm"
              />
              <p className="mt-2 text-sm text-gray-500">
                Minimum 10 characters. Be as detailed as possible.
              </p>
            </div>

            <div className="flex justify-end space-x-3">
              <button
                type="button"
                onClick={() => navigate('/claims')}
                className="px-4 py-2 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary-500"
              >
                Cancel
              </button>
              <button
                type="submit"
                disabled={loading}
                className="px-4 py-2 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-primary-600 hover:bg-primary-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary-500 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {loading ? 'Submitting...' : 'Submit Claim'}
              </button>
            </div>
          </form>
        </div>
      </div>
    </DashboardLayout>
  );
};

export default NewClaim;