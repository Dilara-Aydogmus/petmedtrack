import torch.nn as neural_network

class TransposeTokenDimensions(neural_network.Module):
    def forward(self, token_vectors):
        return token_vectors.transpose(1,2)