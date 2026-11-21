import torch
from torch.utils.data import DataLoader, TensorDataset
from app.observation_alert_cnn import ObservationAlertCNN
from app.observation_data_preparation import prepare_training_data

class ObservationTrainer:
    def __init__(self,
                 number_of_epochs=10,
                 batch_size=16,
                 learning_rate=0.001,
                 device=None,
                 ):

        self.number_of_epochs = number_of_epochs
        self.batch_size = batch_size
        self.learning_rate = learning_rate

        if device is None:
            device = "cuda" if torch.cuda.is_available() else "cpu"

        self.device = torch.device(device)
        self.model = None

        self.training_losses = []
        self.data_dictionary = None


    def train(self, number_of_merges):
        self.data_dictionary = prepare_training_data(number_of_merges)
        token_ids = torch.tensor(self.data_dictionary["training_texts_as_padded_token_ids"])
        label_ids = torch.tensor(self.data_dictionary["training_label_ids"])

        training_dataset = TensorDataset(token_ids, label_ids)
        training_data_loader = DataLoader(training_dataset, batch_size=self.batch_size, shuffle=True)

        vocabulary_size=len(self.data_dictionary["token_to_id"])
        number_of_classes=len(self.data_dictionary["label_to_id_dictionary"])
        padding_token_id=self.data_dictionary["token_to_id"]["<PAD>"]

        self.model = ObservationAlertCNN(vocabulary_size=vocabulary_size, number_of_classes=number_of_classes, padding_token_id=padding_token_id)

        self.model.to(self.device)

        loss_function = torch.nn.CrossEntropyLoss()
        optimizer = torch.optim.Adam(self.model.parameters(), lr=self.learning_rate)

        self.training_losses = []
        self.model.train()

        for epoch in range(self.number_of_epochs):
            total_loss = 0.0
            number_of_batches = len(training_data_loader)

            for batch_token_ids, batch_label_ids in training_data_loader:
                batch_token_ids = batch_token_ids.to(self.device)
                batch_label_ids = batch_label_ids.to(self.device)

                optimizer.zero_grad()

                class_scores = self.model(batch_token_ids)
                loss = loss_function(class_scores, batch_label_ids)

                loss.backward()
                optimizer.step()

                total_loss += loss.item()

            average_loss = total_loss / number_of_batches
            self.training_losses.append(average_loss)

        return self.model







