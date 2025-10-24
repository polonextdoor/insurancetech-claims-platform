import os
from openai import OpenAI
from typing import Dict
import logging

logger = logging.getLogger(__name__)

class LLMService:
    def __init__(self):
        api_key = os.getenv('OPENAI_API_KEY')
        if not api_key:
            raise ValueError("OPENAI_API_KEY not found in environment")
        self.client = OpenAI(api_key=api_key)
        self.model = "gpt-3.5-turbo"  # Cheap and fast (~$0.002 per analysis)
        
    async def analyze_claim_description(self, description: str, claim_amount: float, 
                                       policy_type: str) -> Dict:
        """
        Analyze claim description for fraud indicators using OpenAI
        """
        try:
            prompt = f"""You are an insurance fraud detection AI. Analyze this claim description for fraud risk.

Claim Details:
- Policy Type: {policy_type}
- Claimed Amount: ${claim_amount:,.2f}
- Description: "{description}"

Analyze for:
1. Vagueness or lack of specific details
2. Inconsistencies or contradictions
3. Language patterns common in fraudulent claims
4. Overly emotional or exaggerated language
5. Missing key information (when, where, how)

Provide:
1. Fraud Risk (Low/Medium/High)
2. Confidence (0-100%)
3. Key Risk Indicators (2-3 specific points)
4. Red Flags (if any)

Format as JSON:
{{
  "fraud_risk": "Low|Medium|High",
  "confidence": 85,
  "risk_indicators": ["point 1", "point 2"],
  "red_flags": ["flag 1"] or [],
  "reasoning": "Brief explanation"
}}"""

            response = self.client.chat.completions.create(
                model=self.model,
                messages=[
                    {"role": "system", "content": "You are an insurance fraud detection expert."},
                    {"role": "user", "content": prompt}
                ],
                temperature=0.3,  # Lower temperature for consistent analysis
                max_tokens=400,
                response_format={ "type": "json_object" }
            )
            
            import json
            result = json.loads(response.choices[0].message.content)
            
            logger.info(f"LLM analysis completed: {result['fraud_risk']} risk")
            return result
            
        except Exception as e:
            logger.error(f"LLM analysis failed: {e}")
            # Fallback response
            return {
                "fraud_risk": "Medium",
                "confidence": 50,
                "risk_indicators": ["LLM service unavailable - using fallback"],
                "red_flags": [],
                "reasoning": "Unable to perform deep analysis due to service error"
            }