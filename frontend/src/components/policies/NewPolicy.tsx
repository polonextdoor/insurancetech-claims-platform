import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { policiesAPI } from '../../services/api';
import { useAuth } from '../../context/AuthContext';
import DashboardLayout from '../common/DashboardLayout';

const NewPolicy: React.FC = () => {
  const [formData, setFormData] = useState({
    userId: '',
    policyType: '',
    coverageAmount: '',
    deductible: '',
    premiumAmount: '',
    startDate: '',
    endDate: '',
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const navigate = useNavigate();
  const { user } = useAuth();

  // Pre-fill userId for non-admins
  React.useEffect(() => {
    if (user && user.role !== 'ADMIN') {
      setFormData(prev => ({ ...prev, userId: user.id.toString() }));
    }
  }, [user]);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
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
      await policiesAPI.createPolicy({
        userId: parseInt(formData.userId),
        policyType: formData.policyType,
        coverageAmount: parseFloat(formData.coverageAmount),
        deductible: parseFloat(formData.deductible),
        premiumAmount: parseFloat(formData.premiumAmount),
        startDate: formData.startDate,
        endDate: formData.endDate,
      });
      navigate('/policies');
    } catch (err: any) {
      setError(err.response?.data?.error || 'Failed to create policy');
    } finally {
      setLoading(false);
    }
  };

  return (
    <DashboardLayout>
      <div className="max-w-3xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="md:flex md:items-center md:justify-between mb-8">
          <div className="flex-1 min-w-0">
            <h2 className="text-3xl font-bold leading-7 text-gray-900 sm:text-3xl sm:truncate">
              Create New Policy
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
              <label htmlFor="userId" className="block text-sm font-medium text-gray-700">
                Customer User ID *
              </label>
              <input
                type="number"
                id="userId"
                name="userId"
                required
                value={formData.userId}
                onChange={handleChange}
                placeholder="Enter customer's user ID"
                className="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-primary-500 focus:border-primary-500 sm:text-sm"
              />
              <p className="mt-1 text-xs text-gray-500">
                The user ID of the customer this policy belongs to
              </p>
            </div>

            <div>
              <label htmlFor="policyType" className="block text-sm font-medium text-gray-700">
                Policy Type *
              </label>
              <select
                id="policyType"
                name="policyType"
                required
                value={formData.policyType}
                onChange={handleChange}
                className="mt-1 block w-full pl-3 pr-10 py-2 text-base border-gray-300 focus:outline-none focus:ring-primary-500 focus:border-primary-500 sm:text-sm rounded-md"
              >
                <option value="">Select policy type</option>
                <option value="AUTO">Auto Insurance</option>
                <option value="HOME">Home Insurance</option>
                <option value="HEALTH">Health Insurance</option>
                <option value="LIFE">Life Insurance</option>
                <option value="BUSINESS">Business Insurance</option>
              </select>
            </div>

            <div className="grid grid-cols-1 gap-6 sm:grid-cols-2">
              <div>
                <label htmlFor="coverageAmount" className="block text-sm font-medium text-gray-700">
                  Coverage Amount ($) *
                </label>
                <input
                  type="number"
                  id="coverageAmount"
                  name="coverageAmount"
                  required
                  min="1000"
                  step="0.01"
                  value={formData.coverageAmount}
                  onChange={handleChange}
                  placeholder="50000.00"
                  className="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-primary-500 focus:border-primary-500 sm:text-sm"
                />
              </div>

              <div>
                <label htmlFor="deductible" className="block text-sm font-medium text-gray-700">
                  Deductible ($) *
                </label>
                <input
                  type="number"
                  id="deductible"
                  name="deductible"
                  required
                  min="0"
                  step="0.01"
                  value={formData.deductible}
                  onChange={handleChange}
                  placeholder="1000.00"
                  className="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-primary-500 focus:border-primary-500 sm:text-sm"
                />
              </div>
            </div>

            <div>
              <label htmlFor="premiumAmount" className="block text-sm font-medium text-gray-700">
                Annual Premium ($) *
              </label>
              <input
                type="number"
                id="premiumAmount"
                name="premiumAmount"
                required
                min="1"
                step="0.01"
                value={formData.premiumAmount}
                onChange={handleChange}
                placeholder="1200.00"
                className="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-primary-500 focus:border-primary-500 sm:text-sm"
              />
            </div>

            <div className="grid grid-cols-1 gap-6 sm:grid-cols-2">
              <div>
                <label htmlFor="startDate" className="block text-sm font-medium text-gray-700">
                  Start Date *
                </label>
                <input
                  type="date"
                  id="startDate"
                  name="startDate"
                  required
                  value={formData.startDate}
                  onChange={handleChange}
                  className="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-primary-500 focus:border-primary-500 sm:text-sm"
                />
              </div>

              <div>
                <label htmlFor="endDate" className="block text-sm font-medium text-gray-700">
                  End Date *
                </label>
                <input
                  type="date"
                  id="endDate"
                  name="endDate"
                  required
                  value={formData.endDate}
                  onChange={handleChange}
                  className="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-primary-500 focus:border-primary-500 sm:text-sm"
                />
              </div>
            </div>

            <div className="bg-blue-50 border border-blue-200 rounded p-4">
              <h4 className="text-sm font-medium text-blue-800 mb-2">Policy Summary</h4>
              <dl className="grid grid-cols-2 gap-2 text-sm">
                <dt className="text-blue-700">Type:</dt>
                <dd className="text-blue-900 font-medium">{formData.policyType || 'Not selected'}</dd>
                <dt className="text-blue-700">Coverage:</dt>
                <dd className="text-blue-900 font-medium">
                  {formData.coverageAmount ? `$${parseFloat(formData.coverageAmount).toLocaleString()}` : '$0'}
                </dd>
                <dt className="text-blue-700">Deductible:</dt>
                <dd className="text-blue-900 font-medium">
                  {formData.deductible ? `$${parseFloat(formData.deductible).toLocaleString()}` : '$0'}
                </dd>
                <dt className="text-blue-700">Premium:</dt>
                <dd className="text-blue-900 font-medium">
                  {formData.premiumAmount ? `$${parseFloat(formData.premiumAmount).toLocaleString()}/year` : '$0/year'}
                </dd>
              </dl>
            </div>

            <div className="flex justify-end space-x-3">
              <button
                type="button"
                onClick={() => navigate('/policies')}
                className="px-4 py-2 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary-500"
              >
                Cancel
              </button>
              <button
                type="submit"
                disabled={loading}
                className="px-4 py-2 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-primary-600 hover:bg-primary-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary-500 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {loading ? 'Creating...' : 'Create Policy'}
              </button>
            </div>
          </form>
        </div>
      </div>
    </DashboardLayout>
  );
};

export default NewPolicy;