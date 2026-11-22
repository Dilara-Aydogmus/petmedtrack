from typing import Literal

from fastapi import FastAPI
from pydantic import BaseModel, Field, field_validator

from pathlib import Path
from app.observation_model_loader import ObservationModelLoader
from app.observation_text_analyzer import ObservationTextAnalyzer

app = FastAPI(title="PetMedTrack Observation Analysis")


class AnalysisRequest(BaseModel):
    text: str = Field(min_length=1, max_length=2000)


class AnalysisResponse(BaseModel):
    riskLevel: Literal["NO_ALERT", "VET_ALERT"]
    confidence: float

    @field_validator("confidence")
    @classmethod
    def validate_confidence_is_between_zero_and_one(
        cls,
        confidence_value,
):
        if confidence_value < 0 or confidence_value > 1:
            raise ValueError("Confidence must be between 0 and 1.")

        return confidence_value


checkpoint_path = (Path(__file__).parent.parent/"models"/"observation_alert_model.pt")
observation_model_loader = ObservationModelLoader()
model, checkpoint, device = observation_model_loader.load_observation_model(checkpoint_path)
observation_model_analyzer = ObservationTextAnalyzer(model, checkpoint, device)


@app.post("/analyze", response_model=AnalysisResponse)
async def analyze_observation(
    analysis_request: AnalysisRequest,
):
       analysis_result = observation_model_analyzer.analyze_observation_text(analysis_request.text)
       return AnalysisResponse(riskLevel=analysis_result["riskLevel"], confidence=analysis_result["confidence"],)
