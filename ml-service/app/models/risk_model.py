import pandas as pd
import numpy as np
from sklearn.ensemble import RandomForestClassifier
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import LabelEncoder
from sklearn.metrics import classification_report, confusion_matrix
import joblib
import os
import logging

logger = logging.getLogger(__name__)

class RiskModel:
    def __init__(self):
        self.model = None
        self.label_encoders = {}
        self.feature_names = []
        self.model_path = 'app/models/saved_models/'
        
    def prepare_features(self, df):
        """
        Prepare features for training/prediction
        """
        # Make a copy to avoid modifying original
        data = df.copy()
        
        # Encode categorical variables
        categorical_cols = ['policy_type']
        
        for col in categorical_cols:
            if col not in self.label_encoders:
                self.label_encoders[col] = LabelEncoder()
                data[col] = self.label_encoders[col].fit_transform(data[col])
            else:
                data[col] = self.label_encoders[col].transform(data[col])
        
        # Select features for model
        feature_cols = [
            'policy_type',
            'claim_to_coverage_ratio',
            'customer_claims_count',
            'policy_age_days',
            'filed_on_weekend',
            'filed_quickly',
            'description_length',
            'description_specificity'
        ]
        
        self.feature_names = feature_cols
        return data[feature_cols]
    
    def train(self, csv_path='app/data/synthetic_claims.csv'):
        """
        Train the risk prediction model
        """
        logger.info("Loading training data...")
        df = pd.read_csv(csv_path)
        
        # Prepare features
        X = self.prepare_features(df)
        y = df['is_fraudulent']
        
        # Split data
        X_train, X_test, y_train, y_test = train_test_split(
            X, y, test_size=0.2, random_state=42, stratify=y
        )
        
        logger.info(f"Training set: {len(X_train)} samples")
        logger.info(f"Test set: {len(X_test)} samples")
        logger.info(f"Fraudulent claims in training: {y_train.sum()} ({y_train.mean()*100:.1f}%)")
        
        # Train Random Forest model
        logger.info("Training Random Forest model...")
        self.model = RandomForestClassifier(
            n_estimators=100,
            max_depth=10,
            min_samples_split=5,
            min_samples_leaf=2,
            random_state=42,
            class_weight='balanced'  # Handle imbalanced data
        )
        
        self.model.fit(X_train, y_train)
        
        # Evaluate
        train_score = self.model.score(X_train, y_train)
        test_score = self.model.score(X_test, y_test)
        
        logger.info(f"Training accuracy: {train_score:.3f}")
        logger.info(f"Test accuracy: {test_score:.3f}")
        
        # Detailed evaluation
        y_pred = self.model.predict(X_test)
        logger.info("\nClassification Report:")
        logger.info("\n" + classification_report(y_test, y_pred))
        
        # Feature importance
        feature_importance = pd.DataFrame({
            'feature': self.feature_names,
            'importance': self.model.feature_importances_
        }).sort_values('importance', ascending=False)
        
        logger.info("\nFeature Importance:")
        logger.info("\n" + str(feature_importance))
        
        # Save model
        self.save_model()
        
        return {
            'train_accuracy': train_score,
            'test_accuracy': test_score,
            'feature_importance': feature_importance.to_dict('records')
        }
    
    def predict_risk(self, features_dict):
        """
        Predict fraud risk for a single claim
        Returns: (probability, risk_level)
        """
        if self.model is None:
            raise ValueError("Model not loaded. Call load_model() first.")
        
        # Convert dict to DataFrame
        features_df = pd.DataFrame([features_dict])
        
        # Prepare features
        X = self.prepare_features(features_df)
        
        # Get probability
        prob = self.model.predict_proba(X)[0][1]  # Probability of fraud
        
        # Determine risk level
        if prob >= 0.7:
            risk_level = "CRITICAL"
        elif prob >= 0.5:
            risk_level = "HIGH"
        elif prob >= 0.3:
            risk_level = "MEDIUM"
        else:
            risk_level = "LOW"
        
        return prob, risk_level
    
    def get_feature_contributions(self, features_dict):
        """
        Get contribution of each feature to the prediction
        Used for explainability
        """
        if self.model is None:
            raise ValueError("Model not loaded")
        
        # Prepare features
        features_df = pd.DataFrame([features_dict])
        X = self.prepare_features(features_df)
        
        # Get feature importance
        contributions = {}
        for i, feature in enumerate(self.feature_names):
            importance = self.model.feature_importances_[i]
            value = X.iloc[0, i]
            contributions[feature] = {
                'value': float(value),
                'importance': float(importance)
            }
        
        return contributions
    
    def save_model(self):
        """
        Save trained model and encoders
        """
        os.makedirs(self.model_path, exist_ok=True)
        
        # Save model
        model_file = os.path.join(self.model_path, 'risk_model.joblib')
        joblib.dump(self.model, model_file)
        logger.info(f"Model saved to {model_file}")
        
        # Save label encoders
        encoders_file = os.path.join(self.model_path, 'label_encoders.joblib')
        joblib.dump(self.label_encoders, encoders_file)
        logger.info(f"Encoders saved to {encoders_file}")
        
        # Save feature names
        features_file = os.path.join(self.model_path, 'feature_names.joblib')
        joblib.dump(self.feature_names, features_file)
        logger.info(f"Feature names saved to {features_file}")
    
    def load_model(self):
        """
        Load trained model from disk
        """
        try:
            model_file = os.path.join(self.model_path, 'risk_model.joblib')
            self.model = joblib.load(model_file)
            
            encoders_file = os.path.join(self.model_path, 'label_encoders.joblib')
            self.label_encoders = joblib.load(encoders_file)
            
            features_file = os.path.join(self.model_path, 'feature_names.joblib')
            self.feature_names = joblib.load(features_file)
            
            logger.info("Model loaded successfully")
            return True
        except FileNotFoundError:
            logger.warning("Model files not found. Train model first.")
            return False

if __name__ == "__main__":
    # Train the model
    logging.basicConfig(level=logging.INFO)
    
    model = RiskModel()
    results = model.train()
    
    print("\n" + "="*50)
    print("MODEL TRAINING COMPLETE")
    print("="*50)
    print(f"Train Accuracy: {results['train_accuracy']:.3f}")
    print(f"Test Accuracy: {results['test_accuracy']:.3f}")