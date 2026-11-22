from pathlib import Path
import pandas as pd

class ObservationModelEvaluator:
    def __init__(self, csv_path):
        self.data = pd.read_csv(csv_path)


    def analyze_csv(self):
        print("Total row count:", len(self.data))
        print("\nClass distribution:")
        print(self.data["urgency_level"].value_counts())


if __name__ == "__main__":
    csv_path = (Path(__file__).parent.parent/"data"/"hopepet_synthetic_dataset.csv")
    evaluator = ObservationModelEvaluator(csv_path)
    evaluator.analyze_csv()
