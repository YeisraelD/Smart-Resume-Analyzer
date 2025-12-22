# Smart Hire
### Smart Resume Analyzer
> From a CRUD to decision support system.
--- 
Smart Hire is a JavaFX-based application created to make resume screening simpler, fairer, and more meaningful. Instead of relying on basic keyword matching, the system uses a pre-trained AI model to understand resumes in context and evaluate how well a candidate aligns with a job description.

This project was built as part of an academic Java/OOP project, with a strong focus on clean design, usability, and real-world relevance.

### Why Smart Hire?

- Resume screening is often:
- Time consuming
- Inconsistent
- Heavily dependent on manual judgment

Smart Hire explores how AI-assisted tools can support recruiters by providing clear insights, while still leaving the final decision to humans.

### What the System Does

- Reads resumes in PDF and DOCX formats
- Analyzes resumes using a pre-trained NLP model
- Compares resumes with job descriptions semantically
- Calculates a candidate relevance score
- Highlights matched and missing skills
- Presents results in a clean, interactive JavaFX interface


### Intelligence Behind the System

At the core of Smart Hire is a pre-trained Natural Language Processing (NLP) model used to extract meaning from text rather than just matching keywords.
[sentence-transformers/all-MiniLM-L6-v2](https://huggingface.co/sentence-transformers/all-MiniLM-L6-v2) hosted on Hugging Face.

Why this model? I chose the all-MiniLM-L6-v2 for its perfect balance of speed and intelligence. It is a "mini" model, meaning it is incredibly fast for real-time applications like a JavaFX dashboard, yet it is trained on over 1 billion sentence pairs to ensure it understands professional context deeply.

#### Model Specifications:
- Dimensions: 384 (providing a rich mathematical "fingerprint" of each resume)
- Max Sequence Length: 256 tokens (ideal for concise resume bullet points)
- Input: Raw text from PDF/DOCX
- Output: Dense vector space for high-speed cosine similarity comparison

#### This allows the system to:

- Recognize related skills and concepts
- Understand context within resumes
- Provide more realistic and useful evaluations

This is what makes Smart Hire an intelligent system, not a traditional rule-based application.

### Design & Architecture

The project follows a Model–View–Controller (MVC) structure:

- Model – Represents resume data and analysis results
- View – JavaFX UI built with FXML and Scene Builder
- Controller – Handles user actions and system logic
- This separation keeps the code organized, readable, and easy to extend.

### Object-Oriented Principles Applied

Smart Hire was intentionally designed to demonstrate core OOP concepts:

- Classes and Objects
- Encapsulation
- Inheritance
- Polymorphism
- Abstraction
- Exception Handling
These principles are applied throughout the system to ensure maintainability and clarity.

### Possible Future Improvements

- Persisting candidate data using a database
- Supporting more document formats
- Adding recruiter-specific dashboards
- Improving AI scoring explanations
- Exporting analysis results

#### Final Thoughts

Smart Hire is an exploration of how software engineering principles and AI tools can be combined to solve a real world problem.
The project emphasizes understanding, usability, and thoughtful design over unnecessary complexity.

> The goal isn't just to build a system that reads resumes; it's to build a system that understands potential.

