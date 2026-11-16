from pathlib import Path
import pandas as pd


def preprocess_text(text):
    text = text.replace("İ", "i")
    text = text.replace("I", "ı")
    text = text.lower()

    cleaned_characters = []

    for character in text:
        if character.isalnum() or character.isspace():
            cleaned_characters.append(character)
        else:
            cleaned_characters.append(" ")

    cleaned_text = "".join(cleaned_characters)
    return normalize_spaces(cleaned_text)


def normalize_spaces(cleaned_text):
    cleaned_text = cleaned_text.strip()
    normalized_characters = []

    for character in cleaned_text:
        if character.isspace():
            if len(normalized_characters) > 0:
                last_character = normalized_characters[-1]
                if last_character != " ":
                    normalized_characters.append(" ")
        else:
            normalized_characters.append(character)

    return "".join(normalized_characters)



def build_bpe_token_sequence_frequencies(training_texts):
    token_sequence_frequencies = {}

    for training_text in training_texts:
        preprocessed_text = preprocess_text(training_text)
        words = preprocessed_text.split()

        for word in words:
            token_sequence = tuple(word)

            if token_sequence in token_sequence_frequencies:
                token_sequence_frequencies[token_sequence] += 1
            else:
                token_sequence_frequencies[token_sequence] = 1

    return token_sequence_frequencies



def count_bpe_pairs(token_sequence_frequencies):
    pair_frequencies = {}

    for token_sequence in token_sequence_frequencies:
        sequence_frequency = token_sequence_frequencies[token_sequence]

        for index in range(len(token_sequence) - 1):
            pair = (token_sequence[index], token_sequence[index + 1])

            if pair in pair_frequencies:
                pair_frequencies[pair] += sequence_frequency
            else:
                pair_frequencies[pair] = sequence_frequency

    return pair_frequencies



def find_most_frequent_pair(pair_frequencies):
    if len(pair_frequencies) == 0:
        return None

    most_frequent_pair = None
    highest_frequency = 0

    for pair in pair_frequencies:
        current_frequency = pair_frequencies[pair]
        if current_frequency > highest_frequency:
            most_frequent_pair = pair
            highest_frequency = current_frequency

    return most_frequent_pair



def merge_pair_in_sequence(sequence, merge_rule):
    merged_sequence = []
    index = 0

    while index < len(sequence):
        pair_matches = False
        has_next_piece = index + 1 < len(sequence)

        if has_next_piece:
            current_piece = sequence[index]
            next_piece = sequence[index + 1]

            first_piece_matches = current_piece == merge_rule[0]
            second_piece_matches = next_piece == merge_rule[1]

            if first_piece_matches:
                if second_piece_matches:
                    pair_matches = True

        if pair_matches:
            first_piece = merge_rule[0]
            second_piece = merge_rule[1]
            merged_piece = first_piece + second_piece

            merged_sequence.append(merged_piece)
            index = index + 2
        else:
            current_piece = sequence[index]
            merged_sequence.append(current_piece)
            index = index + 1

    return tuple(merged_sequence)



def merge_pair_in_token_sequence_frequencies(
    token_sequence_frequencies,
    merge_rule
):
    updated_token_sequence_frequencies = {}

    for token_sequence in token_sequence_frequencies:
        sequence_frequency = token_sequence_frequencies[token_sequence]

        merged_token_sequence = merge_pair_in_sequence(
            token_sequence,
            merge_rule
        )

        if merged_token_sequence in updated_token_sequence_frequencies:
            old_frequency = updated_token_sequence_frequencies[merged_token_sequence]
            updated_token_sequence_frequencies[merged_token_sequence] = (
                old_frequency + sequence_frequency
            )
        else:
            updated_token_sequence_frequencies[merged_token_sequence] = (
                sequence_frequency
            )

    return updated_token_sequence_frequencies




def learn_bpe_merges(training_texts, number_of_merges):
    token_sequence_frequencies = build_bpe_token_sequence_frequencies(
        training_texts
    )
    learned_merges = []

    while len(learned_merges) < number_of_merges:
        pair_frequencies = count_bpe_pairs(token_sequence_frequencies)
        merge_rule = find_most_frequent_pair(pair_frequencies)

        if merge_rule is None:
            break

        token_sequence_frequencies = merge_pair_in_token_sequence_frequencies(
            token_sequence_frequencies,
            merge_rule
        )
        learned_merges.append(merge_rule)

    return learned_merges



def tokenize_text_with_bpe(text, learned_merges):
    preprocessed_text = preprocess_text(text)
    words = preprocessed_text.split()
    tokenized_word_sequences = []

    for word in words:
        merged_token_sequence = tuple(word)

        for merge_rule in learned_merges:
            merged_token_sequence = merge_pair_in_sequence(
                merged_token_sequence,
                merge_rule
            )

        tokenized_word_sequences.append(merged_token_sequence)

    return tokenized_word_sequences



def build_token_to_id(training_texts, learned_merges):
    token_to_id = {
    "<PAD>": 0,
    "<UNKNOWN>": 1,
    "<WORD_BOUNDARY>": 2}

    for training_text in training_texts:
        tokenized_word_sequences = tokenize_text_with_bpe(
            training_text,
            learned_merges
        )
        for token_sequence in tokenized_word_sequences:
            for token in token_sequence:
                if token not in token_to_id:
                    token_to_id[token] = len(token_to_id)
    return token_to_id


def encode_text_with_bpe(text, learned_merges, token_to_id):
    tokenized_word_sequences = tokenize_text_with_bpe(text, learned_merges)
    token_ids = []
    is_first_word = True

    for token_sequence in tokenized_word_sequences:
        if not is_first_word:
            word_boundary_id = token_to_id["<WORD_BOUNDARY>"]
            token_ids.append(word_boundary_id)

        is_first_word = False

        for token in token_sequence:
            if token in token_to_id:
                token_id = token_to_id[token]
            else:
                token_id = token_to_id["<UNKNOWN>"]

            token_ids.append(token_id)
    return token_ids



def pad_token_ids(token_ids, max_length, token_to_id):
    if len(token_ids)> max_length:
        raise ValueError("Token sequence exceeds the max length.")

    padded_token_ids = token_ids.copy()
    padding_token_id = token_to_id["<PAD>"]

    while len(padded_token_ids) < max_length:
        padded_token_ids.append(padding_token_id)

    return padded_token_ids


def load_source_dataset():
    service_folder = Path(__file__).parent.parent
    dataset_path = Path(service_folder, "data", "hopepet_synthetic_dataset.csv")

    if not dataset_path.exists():
        raise FileNotFoundError(f"Training dataset not found: {dataset_path}")

    dataset = pd.read_csv(dataset_path)
    if dataset.empty:
        raise ValueError("Training dataset is empty.")

    return dataset


def build_bpe_tokenizer(training_texts, number_of_merges):
    learned_merges = learn_bpe_merges(training_texts, number_of_merges)
    token_to_id = build_token_to_id(training_texts, learned_merges)
    return learned_merges, token_to_id


def find_maximum_encoded_text_length(training_texts, learned_merges, token_to_id):
    maximum_sequence_length = 0

    for training_text in training_texts:
        token_ids = encode_text_with_bpe(training_text, learned_merges, token_to_id)

        if len(token_ids) > maximum_sequence_length:
            maximum_sequence_length = len(token_ids)

    return maximum_sequence_length



def convert_training_texts_to_padded_token_ids(training_texts, learned_merges, token_to_id, maximum_text_length):
    encoded_training_texts = []

    for training_text in training_texts:
        token_ids = encode_text_with_bpe(training_text, learned_merges, token_to_id)

        padded_token_ids = pad_token_ids(token_ids, maximum_text_length, token_to_id)

        encoded_training_texts.append(padded_token_ids)

    return encoded_training_texts



def load_training_texts_and_labels():
    source_dataset = load_source_dataset()
    text_column_name = "user_question"
    label_column_name = "urgency_level"

    urgency_level_to_alert_level = {
        "Low": "NO_ALERT",
        "Medium": "NO_ALERT",
        "High": "VET_ALERT",
        "Emergency": "VET_ALERT"
    }

    training_texts = []
    training_labels = []

    for dataset_row in source_dataset.to_dict(orient="records"):
        text_value = dataset_row[text_column_name]
        label_value = dataset_row[label_column_name]

        if pd.notna(text_value) and pd.notna(label_value):
            training_text = str(text_value).strip()
            training_label = str(label_value).strip()

            if training_text and training_label:
                alert_level = urgency_level_to_alert_level[training_label]
                training_texts.append(training_text)
                training_labels.append(alert_level)


    if not training_texts:
        raise ValueError("The dataset does not contain any training examples.")

    return training_texts, training_labels




def build_label_to_id_dictionary(training_labels):
    label_to_id_dictionary = {}

    for training_label in training_labels:
        if training_label not in label_to_id_dictionary:
            label_to_id_dictionary[training_label] = len(label_to_id_dictionary)
    return label_to_id_dictionary



def convert_training_labels_to_ids(training_labels, label_to_id_dictionary):
    training_label_ids = []

    for training_label in training_labels:
        training_label_ids.append(label_to_id_dictionary[training_label])

    return training_label_ids



def prepare_training_data(number_of_merges):
    training_texts, training_labels = load_training_texts_and_labels()
    learned_merges, token_to_id = build_bpe_tokenizer(training_texts, number_of_merges)
    maximum_text_length = find_maximum_encoded_text_length(training_texts, learned_merges, token_to_id)
    training_texts_as_padded_token_ids = convert_training_texts_to_padded_token_ids(training_texts, learned_merges, token_to_id, maximum_text_length)
    label_to_id_dictionary = build_label_to_id_dictionary(training_labels)
    training_label_ids = convert_training_labels_to_ids(training_labels, label_to_id_dictionary)

    return {
        "training_texts_as_padded_token_ids": training_texts_as_padded_token_ids,
        "training_label_ids": training_label_ids,
        "learned_merges": learned_merges,
        "token_to_id": token_to_id,
        "label_to_id_dictionary": label_to_id_dictionary,
        "maximum_text_length": maximum_text_length
    }