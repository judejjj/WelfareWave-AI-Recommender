# WelfareWave: AI-Powered Government Scheme Recommender

WelfareWave is a mobile application designed to bridge the gap between citizens and government welfare schemes. Using **Machine Learning (TensorFlow Lite)** and a **Hybrid Recommendation Engine**, the app provides personalized scheme suggestions based on a user's socio-economic profile.

## 🚀 Features
- **AI-Powered Recommendations:** Uses a Deep Learning Neural Network (TFLite) to rank schemes based on Age, Income, and Category.
- **Hybrid Filtering:** Combines ML predictions with strict rule-based category filtering for 100% accuracy.
- **On-Device Translation:** Full-page Malayalam translation using Google ML Kit.
- **Accessibility:** Integrated Malayalam Text-to-Speech (TTS) for scheme details.
- **Firebase Backend:** Real-time data sync with Firestore and Secure Authentication.

## 🛠️ Tech Stack
- **Frontend:** Android (Java/XML)
- **Backend:** Firebase (Auth, Firestore)
- **AI/ML:** Python (TensorFlow, NumPy, Pandas), TensorFlow Lite
- **Tools:** Android Studio, Cursor AI, Git

## 🧠 How the AI Works
The app employs a **Hybrid Recommendation System**:
1. **The Brain:** A Feed-Forward Neural Network trained on synthetic demographic data predicts the probability of a scheme's relevance.
2. **The Bouncer:** A Java-based filtering layer ensures users only see schemes relevant to their specific category (e.g., Farmers, Students).

## 📥 Installation
1. Clone the repo: `git clone https://github.com/yourusername/WelfareWave.git`
2. Build and run the Android project.

---
*Developed as an MCA Project. Dedicated to improving digital accessibility for welfare services.*