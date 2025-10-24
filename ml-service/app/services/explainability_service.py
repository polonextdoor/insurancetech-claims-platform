import shap
import pandas as pd
import numpy as np
import logging

logger = logging.getLogger(__name__)

class ExplainabilityService:
    def __init__(self, model, feature_names):
        self.model = model
        self.feature_names = feature_names
        self.explainer = None
        
    def initialize_explainer(self, background_data):
        """
        Initialize SHAP explainer with background data
        """
        try:
            # Use TreeExplainer for Random Forest (faster and exact)
            self.explainer = shap.TreeExplainer(self.model)
            logger.info("SHAP explainer initialized")
        except Exception as e:
            logger.error(f"Failed to initialize SHAP explainer: {e}")
    
    def explain_prediction(self, features_array):
        """
        Generate SHAP explanation for a prediction
        Returns human-readable explanation
        """
        if self.explainer is None:
            return {
                "error": "Explainer not initialized",
                "explanation": "Unable to generate explanation"
            }
        
        try:
            # Calculate SHAP values
            shap_values = self.explainer.shap_values(features_array)
            
            # Get SHAP values for positive class (fraud)
            if isinstance(shap_values, list):
                shap_values_fraud = shap_values[1][0]  # For binary classification
            else:
                shap_values_fraud = shap_values[0]
            
            # Create feature contribution dict
            contributions = {}
            for i, feature in enumerate(self.feature_names):
                contributions[feature] = {
                    'value': float(features_array[0, i]),
                    'shap_value': float(shap_values_fraud[i]),
                    'impact': 'increases' if shap_values_fraud[i] > 0 else 'decreases'
                }
            
            # Sort by absolute SHAP value
            sorted_features = sorted(
                contributions.items(), 
                key=lambda x: abs(x[1]['shap_value']), 
                reverse=True
            )
            
            # Generate human-readable explanation
            top_factors = []
            for feature, data in sorted_features[:3]:  # Top 3 factors
                impact_word = "increases" if data['shap_value'] > 0 else "decreases"
                feature_readable = feature.replace('_', ' ').title()
                top_factors.append(f"{feature_readable} {impact_word} risk")
            
            return {
                'all_contributions': contributions,
                'top_factors': top_factors,
                'explanation': f"Risk primarily driven by: {', '.join(top_factors[:2])}"
            }
            
        except Exception as e:
            logger.error(f"SHAP explanation failed: {e}")
            return {
                "error": str(e),
                "explanation": "Unable to generate detailed explanation"
            }