import os
import sys

# Try importing dependencies; suggest installation if missing.
try:
    import numpy as np
    import pandas as pd
    import tensorflow as tf
except ImportError as e:
    print(f"Missing dependency: {e}")
    print("Please install requirements using: pip install numpy pandas tensorflow")
    sys.exit(1)

# ---------------------------------------------------------
# 1. Generate Synthetic Data
# ---------------------------------------------------------
# 2000 rows of user profiles
# Features: Age (18-80), Income (10,000-800,000), Category (0-5)
# Categories: 0=General, 1=Students, 2=Farmers, 3=Senior Citizens, 4=Women, 5=Disabled
# 
# Labels: 0 to 11 (matching the 12 schemes)
# We apply deterministic rules with slight noise, allowing the model to learn thresholds.

num_samples = 2000

ages = np.random.randint(18, 81, num_samples)
incomes = np.random.randint(10000, 800001, num_samples)
categories = np.random.randint(0, 6, num_samples)
labels = np.zeros(num_samples, dtype=int)

for i in range(num_samples):
    age = ages[i]
    inc = incomes[i]
    cat = categories[i]
    
    # Introduce a 10% chance of random labels for noise
    if np.random.rand() < 0.1:
        labels[i] = np.random.randint(0, 12)
        continue

    # Deterministic base logic
    if cat == 1:
        # Students
        if inc <= 100000:
            labels[i] = 0  # e.g., Post Matric Scholarship / E-Grantz
        else:
            labels[i] = 1  # e.g., General Merit Scholarship
    elif cat == 2:
        # Farmers
        if inc < 200000:
            labels[i] = 2  # e.g., PM-KISAN
        else:
            labels[i] = 3  # e.g., KCC / General Agri Loan
    elif cat == 3 or age >= 60:
        # Senior Citizens
        if inc <= 50000:
            labels[i] = 4  # e.g., Old Age Pension
        else:
            labels[i] = 5  # e.g., Healthcare for Seniors
    elif cat == 4:
        # Women
        if inc < 150000:
            labels[i] = 6  # e.g., Maternity Benefit / PM Matru Vandana
        else:
            labels[i] = 7  # e.g., Women Empowerment Scheme
    elif cat == 5:
        # Disabled
        if inc < 100000:
            labels[i] = 8  # e.g., Disability Pension
        else:
            labels[i] = 9  # e.g., Assistive Devices Scheme
    else:
        # General
        if age < 30 and inc < 200000:
            labels[i] = 10 # e.g., Youth Skilling Scheme
        else:
            labels[i] = 11 # e.g., Generic Health Insurance / Ayushman Bharat

# ---------------------------------------------------------
# 2. Build and Train Model
# ---------------------------------------------------------
# We scale the features so the neural network learns faster
# Normalizing statically for TFLite inference later:
# Max Age=100, Max Income=1,000,000, Max Category=5
X = np.column_stack((ages / 100.0, incomes / 1000000.0, categories / 5.0))
y = labels

model = tf.keras.Sequential([
    tf.keras.layers.Dense(16, activation='relu', input_shape=(3,)),
    tf.keras.layers.Dense(16, activation='relu'),
    tf.keras.layers.Dense(12, activation='softmax')
])

model.compile(optimizer='adam',
              loss='sparse_categorical_crossentropy',
              metrics=['accuracy'])

print("Training Scheme Recommender Model...")
model.fit(X, y, epochs=50, batch_size=32, validation_split=0.2)

# ---------------------------------------------------------
# 3. Export to TensorFlow Lite
# ---------------------------------------------------------
converter = tf.lite.TFLiteConverter.from_keras_model(model)
tflite_model = converter.convert()

output_path = "scheme_recommender.tflite"
with open(output_path, "wb") as f:
    f.write(tflite_model)

print(f"\nModel exported successfully to {output_path}")
print("Run this to generate the TFLite file: python train_model.py")
