from pathlib import Path
import torch

class ObservationModelSaver:
    def save_observation_model(self, model, data_dictionary, file_path):
        if model is None:
            raise ValueError("A trained model is required.")

        model_config_dictionary = {
            "vocabulary_size": model.embedding_layer.num_embeddings,
            "embedding_dimension": model.embedding_layer.embedding_dim,
            "number_of_filters": model.convolution_layer.out_channels,
            "kernel_size": model.convolution_layer.kernel_size[0],
            "number_of_classes": model.classification_layer.out_features,
            "padding_token_id": model.embedding_layer.padding_idx,
        }

        model_checkpoint_dictionary = {
            "model_state_dict": model.state_dict(),
            "model_config": model_config,
            "learned_merges": data_dictionary["learned_merges"],
            "token_to_id": data_dictionary["token_to_id"],
            "label_to_id_dictionary": data_dictionary["label_to_id_dictionary"],
            "maximum_text_length": data_dictionary["maximum_text_length"],
        }

        save_destination = Path(file_path)
        save_destination.parent.mkdir(parents=True, exist_ok=True)
        torch.save(checkpoint, save_destination)