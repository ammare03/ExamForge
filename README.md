# 📄 ExamForge

![Android Build](https://img.shields.io/badge/Android%20Build-passing-brightgreen)
![OpenAI API](https://img.shields.io/badge/OpenAI%20API-blue)
![License: MIT](https://img.shields.io/badge/License-MIT-yellow)

> Transform PDF study material into fully formatted exam papers—powered by ChatGPT!

---

## ✨ Features

- **PDF Extraction**: Parse any PDF with PDFBox.
- **AI-Driven Q&A**: Generate questions using OpenAI’s ChatGPT via a Retrofit client.
- **Custom Prompts**: Configure total marks, question types (MCQ, short answer, etc.), and additional guidelines.
- **PDF Generation**: Create and preview final papers using iText.
- **History & Search**: Save past papers with LiveData + Room-style repo for quick access.
- **One-Tap Share**: Instantly share generated papers.

---

## 🛠️ Tech Stack

| Component          | Details                                 |
|--------------------|-----------------------------------------|
| **Platform**       | Android (Java & Kotlin)                 |
| **Networking**     | Retrofit, OkHttp Interceptor            |
| **AI Engine**      | OpenAI ChatGPT API                      |
| **PDF Tools**      | PDFBox, iText                           |
| **Architecture**   | MVVM-inspired patterns, LiveData        |

---

## 🚀 Getting Started

### Prerequisites

- Android Studio Arctic Fox or later
- JDK 11+
- OpenAI API Key

### Installation

```bash
# Clone repository
git clone https://github.com/ammare03/ExamForge.git
cd ExamForge
# Open in Android Studio and let Gradle sync
```

### Configuration

1. Obtain your OpenAI API key from https://beta.openai.com/account/api-keys
2. In `app/local.properties` add:
   ```properties
   CHATGPT_API_KEY="YOUR_OPENAI_API_KEY"
   ```
3. Rebuild the project.

---

## 🎬 Usage

1. Launch **ExamForge** on your device/emulator.
2. Tap **Choose PDF** and select your lecture notes.
3. Enter **Total Marks** and pick **Question Type**.
4. Tap **Generate** and wait for ChatGPT to compose questions.
5. Preview your paper, save, or share instantly.

---

## 🤝 Collaborators

- **[Vistaar Gilani](https://github.com/Vistaar07)** – UX design & PDF modules
- **[Shawn Joseph](https://github.com/Shawn-758)** – Front-end integration & testing

---

## 🌟 Mentor

Special thanks to **[Mohd Fahim Khan](https://github.com/ifahimkhan)** for guidance on AI integration, architecture reviews, and best practices.

---

## 📄 License

This project is licensed under the **MIT License**. See [LICENSE](LICENSE) for details.

---

<p align="center">
  Made with ❤️ by the ExamForge team
</p>
