from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from typing import Optional, Dict, List, Any
from dotenv import load_dotenv
import logging
from app.models.risk_model import RiskModel
from app.services.llm_service import LLMService
from app.services.explainability_service import ExplainabilityService
from datetime import datetime
import os

# Load environment variables
load_dotenv()

# Verify OpenAI key is loaded
if not os.getenv('OPENAI_API_KEY'):
    logger.warning("OPENAI_API_KEY not found - LLM analysis will fail")

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = FastAPI(
    title="Insurance Claims ML Service",
    description="Machine Learning service for risk assessment and fraud detection",
    version="1.0.0"
)

# CORS configuration
app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost:8080"],  # Spring Boot backend
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Request/Response Models
class ClaimData(BaseModel):
    claim_id: int
    policy_type: str
    claimed_amount: float
    coverage_amount: float
    deductible: float
    incident_description: str
    incident_date: str
    policy_start_date: str
    customer_claims_count: int

class RiskAssessment(BaseModel):
    claim_id: int
    risk_score: int  # 0-100
    risk_level: str  # LOW, MEDIUM, HIGH, CRITICAL
    fraud_probability: float  # 0.0-1.0
    explanation: Dict[str, Any]
    llm_analysis: Optional[str] = None
    recommendations: List[str]

# Initialize services on startup
risk_model = RiskModel()
llm_service = None
explainability_service = None

@app.on_event("startup")
async def startup_event():
    global risk_model, llm_service, explainability_service
    
    logger.info("Loading ML model...")
    success = risk_model.load_model()
    
    if not success:
        logger.warning("Model not found, training new model...")
        risk_model.train()
        risk_model.load_model()
    
    # Initialize SHAP explainer
    logger.info("Initializing explainability service...")
    explainability_service = ExplainabilityService(
        risk_model.model, 
        risk_model.feature_names
    )
    # We'll initialize with background data when needed
    
    # Initialize LLM service
    try:
        llm_service = LLMService()
        logger.info("LLM service initialized")
    except Exception as e:
        logger.error(f"LLM service initialization failed: {e}")


@app.get("/")
async def root():
    return {
        "service": "Insurance Claims ML Service",
        "status": "running",
        "version": "1.0.0"
    }

@app.get("/health")
async def health_check():
    return {
        "status": "healthy",
        "ml_model": "loaded" if risk_model.model is not None else "not loaded",
        "llm_service": "available" if llm_service is not None else "unavailable",
        "explainability": "ready" if explainability_service is not None else "not ready"
    }

@app.post("/api/ml/assess-risk", response_model=RiskAssessment)
async def assess_claim_risk(claim: ClaimData):
    """
    Assess risk for an insurance claim using ML model and LLM analysis
    """
    logger.info(f"Assessing risk for claim {claim.claim_id}")
    
    try:
        # Step 1: Prepare features for ML model
        features_dict = {
            'policy_type': claim.policy_type,
            'claim_to_coverage_ratio': claim.claimed_amount / claim.coverage_amount,
            'customer_claims_count': claim.customer_claims_count,
            'policy_age_days': (
                datetime.fromisoformat(claim.incident_date) - 
                datetime.fromisoformat(claim.policy_start_date)
            ).days,
            'filed_on_weekend': 0,  # Could calculate from incident_date
            'filed_quickly': 1 if (
                datetime.fromisoformat(claim.incident_date) - 
                datetime.fromisoformat(claim.policy_start_date)
            ).days < 30 else 0,
            'description_length': len(claim.incident_description),
            'description_specificity': min(len(claim.incident_description.split()) / 50, 1.0)
        }
        
        # Step 2: Get ML prediction
        fraud_probability, risk_level = risk_model.predict_risk(features_dict)
        risk_score = int(fraud_probability * 100)
        
        logger.info(f"ML prediction: {risk_level} (score: {risk_score})")
        
        # Step 3: Get feature contributions (explainability)
        contributions = risk_model.get_feature_contributions(features_dict)
        
        # Step 4: Get LLM analysis (async)
        llm_analysis = "LLM analysis not available"
        if llm_service:
            try:
                llm_result = await llm_service.analyze_claim_description(
                    claim.incident_description,
                    claim.claimed_amount,
                    claim.policy_type
                )
                llm_analysis = llm_result.get('reasoning', 'Analysis complete')
                
                # Adjust risk score based on LLM findings
                llm_risk = llm_result.get('fraud_risk', 'Medium')
                if llm_risk == 'High' and risk_level in ['LOW', 'MEDIUM']:
                    risk_level = 'HIGH'
                    risk_score = max(risk_score, 70)
                    
            except Exception as e:
                logger.error(f"LLM analysis failed: {e}")
        
        # Step 5: Generate recommendations
        recommendations = []
        if risk_level in ['HIGH', 'CRITICAL']:
            recommendations.append("Manual review recommended")
            recommendations.append("Request additional documentation")
        if fraud_probability > 0.7:
            recommendations.append("Flag for fraud investigation")
        if claim.claimed_amount > claim.coverage_amount * 0.5:
            recommendations.append("Verify claim amount and damages")
        if not recommendations:
            recommendations.append("Standard processing approved")
        
        return RiskAssessment(
            claim_id=claim.claim_id,
            risk_score=risk_score,
            risk_level=risk_level,
            fraud_probability=float(fraud_probability),
            explanation=contributions,
            llm_analysis=llm_analysis,
            recommendations=recommendations
        )
        
    except Exception as e:
        logger.error(f"Risk assessment failed: {e}")
        raise

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)