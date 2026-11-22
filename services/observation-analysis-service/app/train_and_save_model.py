from pathlib import Path
from app.observation_model_saver import ObservationModelSaver
from app.observation_model_trainer import ObservationModelTrainer

def train_and_save_model():
    number_of_merges = 100
    observation_model_trainer = ObservationModelTrainer()
    trained_model = observation_model_trainer.train(number_of_merges)

    checkpoint_path = (Path(__file__).parent.parent/"models"/"observation_alert_model.pt")

    observation_model_saver = ObservationModelSaver()
    observation_model_saver.save_observation_model(trained_model,
                                                   observation_model_trainer.data_dictionary,
                                                   checkpoint_path,)
    if __name__ == "__main__":
        train_and_save_model()
