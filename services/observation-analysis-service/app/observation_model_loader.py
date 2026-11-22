from pathlib import Path
import torch

from app.observation_alert_cnn import ObservationAlertCNN

class ObservationModelLoader:
    def load_observation_model(self, file_path, device=None):
            if device is None:
                device = "cuda" if torch.cuda.is_available() else "cpu"

            device = torch.device(device)
            checkpoint_path = Path(file_path)
            if not checkpoint_path.exists():
                raise FileNotFoundError(f"Model checkpoint not found: {checkpoint_path}")

            model_checkpoint_dictionary = torch.load(
                checkpoint_path,
                map_location=device,
                weights_only=True,
            )


            model_config = model_checkpoint_dictionary["model_config"]

            model = ObservationAlertCNN(
                vocabulary_size=model_config["vocabulary_size"],
                embedding_dimension=model_config["embedding_dimension"],
                number_of_filters=model_config["number_of_filters"],
                kernel_size=model_config["kernel_size"],
                number_of_classes=model_config["number_of_classes"],
                padding_token_id=model_config["padding_token_id"],
            )

            model.load_state_dict(model_checkpoint_dictionary["model_state_dict"])
            model.to(device)
            model.eval()

            return model, model_checkpoint_dictionary, device