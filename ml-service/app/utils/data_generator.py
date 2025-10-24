import pandas as pd
import numpy as np
from datetime import datetime, timedelta
import random

def generate_synthetic_claims(n_samples=1000):
    """
    Generate synthetic insurance claims data for ML training
    """
    np.random.seed(42)
    random.seed(42)
    
    policy_types = ['AUTO', 'HOME', 'HEALTH', 'LIFE', 'BUSINESS']
    
    # Generate features
    data = []
    
    for i in range(n_samples):
        # Policy details
        policy_type = random.choice(policy_types)
        coverage_amount = random.choice([25000, 50000, 100000, 250000, 500000])
        deductible = random.choice([500, 1000, 2500, 5000])
        
        # Claim amount (influenced by coverage)
        # Normal claims: 5-30% of coverage
        # Suspicious claims: 40-80% of coverage
        is_suspicious = random.random() < 0.2  # 20% suspicious
        
        if is_suspicious:
            claim_ratio = random.uniform(0.4, 0.8)
            fraud_label = 1
        else:
            claim_ratio = random.uniform(0.05, 0.3)
            fraud_label = 0
            
        claimed_amount = coverage_amount * claim_ratio
        
        # Customer history
        customer_claims_count = random.choices([0, 1, 2, 3, 4, 5], 
                                               weights=[40, 30, 15, 10, 3, 2])[0]
        
        # Dates
        policy_start = datetime.now() - timedelta(days=random.randint(30, 1095))
        incident_date = policy_start + timedelta(days=random.randint(1, 365))
        
        # Calculate policy age at incident (days)
        policy_age_days = (incident_date - policy_start).days
        
        # Suspicious patterns
        filed_on_weekend = incident_date.weekday() >= 5
        filed_quickly = policy_age_days < 30  # Suspicious if very new policy
        
        # Description patterns (simplified)
        if is_suspicious:
            description_length = random.randint(20, 50)  # Vague descriptions
            description_specificity = random.uniform(0.2, 0.5)
        else:
            description_length = random.randint(50, 200)  # Detailed
            description_specificity = random.uniform(0.6, 0.9)
        
        data.append({
            'policy_type': policy_type,
            'coverage_amount': coverage_amount,
            'deductible': deductible,
            'claimed_amount': claimed_amount,
            'claim_to_coverage_ratio': claim_ratio,
            'customer_claims_count': customer_claims_count,
            'policy_age_days': policy_age_days,
            'filed_on_weekend': int(filed_on_weekend),
            'filed_quickly': int(filed_quickly),
            'description_length': description_length,
            'description_specificity': description_specificity,
            'is_fraudulent': fraud_label
        })
    
    df = pd.DataFrame(data)
    
    # Save to CSV
    df.to_csv('app/data/synthetic_claims.csv', index=False)
    print(f"Generated {n_samples} synthetic claims")
    print(f"Fraudulent claims: {df['is_fraudulent'].sum()} ({df['is_fraudulent'].mean()*100:.1f}%)")
    
    return df

if __name__ == "__main__":
    # Create data directory
    import os
    os.makedirs('app/data', exist_ok=True)
    
    # Generate data
    df = generate_synthetic_claims(1000)
    print("\nDataset preview:")
    print(df.head())
    print("\nFeature statistics:")
    print(df.describe())