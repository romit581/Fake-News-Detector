# 🔍 AI Fake News Detector

An AI-powered desktop application that analyzes news headlines and articles to determine whether they are **real or fake**, built with Java and OpenRouter AI.

---

## 📸 Features

- 🤖 AI-powered analysis using OpenRouter API
- 🖥️ Clean dark-themed Java Swing GUI
- 📊 Shows verdict, confidence percentage, and reasoning
- ⚡ Real-time analysis with loading indicator
- 🖱️ Simple one-click detection

---

## 🛠️ Tech Stack

- **Language:** Java 17+
- **GUI:** Java Swing
- **AI:** OpenRouter API (free model)
- **HTTP:** Java built-in `HttpClient`

---

## 🚀 Getting Started

### Prerequisites
- Java 17 or higher
- An OpenRouter API key → [Get one free at openrouter.ai](https://openrouter.ai)

### Setup

**1. Clone the repository**
```bash
git clone https://github.com/yourusername/fake-news-detector.git
cd fake-news-detector
```

**2. Set your API key**

In IntelliJ IDEA:
- Go to **Run → Edit Configurations**
- Find **Environment Variables**
- Add: `OPENROUTER_API_KEY=your_key_here`

**3. Compile and Run**
```bash
javac FakeNewsDetector.java FakeNewsGUI.java
java FakeNewsGUI
```

---

## 📁 Project Structure

```
fake-news-detector/
│
├── FakeNewsDetector.java   # Core AI detection logic & CLI
├── FakeNewsGUI.java        # Swing desktop interface
├── .gitignore              # Hides API key files
└── README.md               # You are here
```

---

## ⚠️ Important

- Never hardcode your API key in the source code
- The `.env` file is excluded from Git via `.gitignore`
- Results are AI-generated and should not be taken as definitive fact


---

## 👤 Author

Made by **Raven**  
GitHub: [@yourusername](https://github.com/yourusername)
