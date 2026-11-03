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



def build_word_to_id(training_texts):
    word_to_id = {"<PAD>": 0, "<UNKNOWN>": 1}

    for training_text in training_texts:
        preprocessed_training_text = preprocess_text(training_text)
        words = preprocessed_training_text.split()

        for word in words:
            if word not in word_to_id:
                word_to_id[word] = len(word_to_id)

    return word_to_id


def encode_text(text, word_to_id):
    preprocessed_text = preprocess_text(text)
    words = preprocessed_text.split()

    token_ids = []

    for word in words:
        if word in word_to_id:
            token_id = word_to_id[word]
        else:
            token_id = word_to_id["<UNKNOWN>"]

        token_ids.append(token_id)

    return token_ids


def pad_sequence(token_ids, max_length):
    padded_token_ids = token_ids.copy()

    while len(padded_token_ids) < max_length:
        padded_token_ids.append(0)

    return padded_token_ids




def build_bpe_word_frequencies(training_texts):
    word_frequencies = {}

    for training_text in training_texts:
        preprocessed_text = preprocess_text(training_text)
        words = preprocessed_text.split()

        for word in words:
            character_sequence = tuple(word)

            if character_sequence in word_frequencies:
                word_frequencies[character_sequence] += 1
            else:
                word_frequencies[character_sequence] = 1

    return word_frequencies



def count_bpe_pairs(word_frequencies):
    pair_frequencies = {}

    for character_sequence in word_frequencies:
        word_frequency = word_frequencies[character_sequence]

        for index in range(len(character_sequence) - 1):
            pair = (character_sequence[index], character_sequence[index + 1])

            if pair in pair_frequencies:
                pair_frequencies[pair] += word_frequency
            else:
                pair_frequencies[pair] = word_frequency

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