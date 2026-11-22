import torch
from app.observation_data_preparation import (
    encode_text_with_bpe,
    pad_token_ids,
)

class ObservationTextAnalyzer:
    def __init__(self, model, model_checkpoint_dictionary, device):
        self.model = model
        self.device = device
        self.learned_merges = model_checkpoint_dictionary["learned_merges"]
        self.token_to_id = model_checkpoint_dictionary["token_to_id"]
        self.maximum_text_length = model_checkpoint_dictionary["maximum_text_length"]
        self.label_to_id_dictionary = model_checkpoint_dictionary["label_to_id_dictionary"]



    def analyze_observation_text(self, observation_text):
        input_tensor = self.prepare_observation_text_tensor(observation_text)
        with torch.no_grad():
            class_scores = self.model(input_tensor)
            class_probabilities = torch.softmax(class_scores, dim=1)
            maximum_result = class_probabilities.max(dim=1)
            highest_probability = maximum_result.values
            predicted_class_id = maximum_result.indices

        predicted_label = self.find_label_for_predicted_class_id(predicted_class_id)

        observation_analysis_result_dictionary = {
            "riskLevel": predicted_label,
            "confidence": float(highest_probability[0]),
        }
        return observation_analysis_result_dictionary



    def prepare_observation_text_tensor(self, observation_text):
        token_ids = encode_text_with_bpe(observation_text, self.learned_merges, self.token_to_id)
        if len(token_ids) == 0:
            raise ValueError("Observation text contains no usable tokens.")

        padded_token_ids = pad_token_ids(token_ids, self.maximum_text_length, self.token_to_id)
        input_tensor = torch.tensor([padded_token_ids], dtype=torch.long, device=self.device,)
        return input_tensor



    def find_label_for_predicted_class_id(self, predicted_class_id):
        for label in self.label_to_id_dictionary:
            existing_id = self.label_to_id_dictionary[label]
            if predicted_class_id == existing_id:
                return label
        raise ValueError("No label matches the predicted class ID")



