import torch.nn as neural_network
from app.transpose_token_dimensions import TransposeTokenDimensions

class ObservationAlertCNN(neural_network.Module):
    def __init__(self,
                 vocabulary_size,
                 embedding_dimension=64,
                 number_of_filters=64,
                 kernel_size=3,
                 number_of_classes=2,
                 padding_token_id=0):
        super().__init__()

        self.embedding_layer = neural_network.Embedding(
            vocabulary_size,
            embedding_dimension,
            padding_idx=padding_token_id,
        )
        self.transpose_layer = TransposeTokenDimensions()

        self.convolution_layer = neural_network.Conv1d(
            in_channels=embedding_dimension,
            out_channels=number_of_filters,
            kernel_size=kernel_size,
            padding=kernel_size // 2,
        )
        self.activation_layer = neural_network.ReLU()
        self.pooling_layer = neural_network.AdaptiveMaxPool1d(1)
        self.classification_layer = neural_network.Linear(
            number_of_filters,
            number_of_classes,
        )

    def forward(self, token_ids):
        # token_ids: [batch_size, sequence_length]
        embedded_tokens = self.embedding_layer(token_ids)
        # embedded_tokens: [batch_size, sequence_length, embedding_dimension]
        tokens_for_convolution = self.transpose_layer(embedded_tokens)
        convolution_features = self.convolution_layer(tokens_for_convolution)
        activated_features = self.activation_layer(convolution_features)
        pooled_features = self.pooling_layer(activated_features)
        pooled_features = pooled_features.squeeze(-1)
        class_scores = self.classification_layer(pooled_features)
        return class_scores
