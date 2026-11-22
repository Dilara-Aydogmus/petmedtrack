from pathlib import Path
import pandas as pd
import matplotlib.pyplot as plt
from app.observation_model_loader import ObservationModelLoader
from app.observation_text_analyzer import  ObservationTextAnalyzer
from sklearn.metrics import confusion_matrix

class ObservationModelEvaluator:
    def __init__(self, csv_path):
        self.data = pd.read_csv(csv_path)


    def analyze_csv(self):
        print("Total row count:", len(self.data))
        print("\nClass distribution:")
        print(self.data["urgency_level"].value_counts())


    def draw_confusion_matrix(self):
        checkpoint_path = (Path(__file__).parent.parent/"models"/"observation_alert_model.pt")
        model_loader = ObservationModelLoader()
        model, checkpoint, device = model_loader.load_observation_model(checkpoint_path)
        analyzer = ObservationTextAnalyzer(model, checkpoint, device)
        label_mapping = {
            "Low": "NO_ALERT",
            "Medium": "NO_ALERT",
            "High": "VET_ALERT",
            "Emergency": "VET_ALERT",
        }

        actual_labels = []
        predicted_labels = []

        for _, row in self.data.iterrows():
            actual_labels.append(label_mapping[row["urgency_level"]])
            prediction = analyzer.analyze_observation_text(row["user_question"])
            predicted_labels.append(prediction["riskLevel"])

        labels = ["NO_ALERT", "VET_ALERT"]
        matrix = confusion_matrix(actual_labels, predicted_labels, labels=labels)


        fig, ax = plt.subplots()
        ax.imshow(matrix, cmap="Blues")
        labels = ["NO_ALERT", "VET_ALERT"]
        ax.set_xticks([0, 1], labels=labels)
        ax.set_yticks([0, 1], labels=labels)
        ax.set_xlabel("Predicted label")
        ax.set_ylabel("Actual label")

        for row in range(2):
            for column in range(2):
                ax.text(column, row, matrix[row, column], ha="center", va="center")

        plt.tight_layout()
        plt.show()



if __name__ == "__main__":
    csv_path = (Path(__file__).parent.parent/"data"/"hopepet_synthetic_dataset.csv")
    evaluator = ObservationModelEvaluator(csv_path)
    evaluator.analyze_csv()
    evaluator.draw_confusion_matrix()
