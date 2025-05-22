# Enterprise Java and Quarkus (EJQ) — Source Code for Substack Articles

This repository contains the source code and hands-on examples for the [Enterprise Java and Quarkus](https://myfear.substack.com/) Substack publication by [Markus Eisele](https://twitter.com/myfear).

If you're working in enterprise software with Java, Quarkus, Jakarta EE, and cloud-native platforms, this is your launchpad for modern development.

## 📰 About the Publication

[**EJQ on Substack**](https://myfear.substack.com/) is a curated collection of technical articles, guides, and deep dives focused on helping developers and architects:

- Build fast, cloud-native apps with **Quarkus**
- Secure microservices with **Vault**, TOTP, and secrets management
- Integrate **LangChain4j** and **AI services** into Java applications
- Design resilient, scalable systems using **Kubernetes** and **Dev Services**
- Understand modern patterns like **RAG (Retrieval-Augmented Generation)**, validation layers, and service guardrails

All content is hands-on, enterprise-relevant, and written for people building real systems—not prototypes.

## 🧑‍💻 What's in This Repo

Each folder in this repository matches an article from the Substack and includes:

- ✅ Complete project setups
- 💡 Example code with comments
- ⚙️ Quarkus configurations and Dev Services integration
- 📦 Maven projects you can run locally with Podman, Ollama, PostgreSQL, etc.

## 📚 Project Overview

Below are the example projects included in this repository, listed alphabetically. Each project folder is linked for easy access:

### 1. [ai-email-simulator/](ai-email-simulator/)
A Quarkus-based application that:
- Simulates email generation and processing using AI models
- Integrates LangChain4j for advanced AI capabilities
- Manages tasks via a to-do list

**Description:**  
Uses AI to generate and process emails, demonstrating LangChain4j integration and task management in a Quarkus app.

### 2. [csv-uploader/](csv-uploader/)
A file processing application with:
- CSV file upload and parsing
- Automatic table creation with inferred column types
- Metadata tracking of uploads
- REST API endpoints for data access

**Description:**  
Enables uploading and parsing of CSV files, storing data in a database with automatic schema inference and metadata management.

### 3. [custom-error-pages-classic/](custom-error-pages-classic/)
A project demonstrating how to handle exceptions and serve error pages.

### 4. [embedding-tutorial/](embedding-tutorial/)
An AI-focused project demonstrating:
- Text embedding generation
- Vector similarity search
- AI model integration
- Data processing pipelines

**Description:**  
Explains how to generate and use text embeddings in Java, including similarity search and integration with AI models.

### 5. [flyway-adventure/](flyway-adventure/)
A project demonstrating how to integrate Flyway into your Quarkus applications and manage database schema evolutions.

### 6. [i18n-demo/](i18n-demo/)
A project demonstrating database backed internationalization in Quarkus.

### 7. [qr-code-demo/](qr-code-demo/)
A practical example showing:
- QR code generation
- Image processing
- REST API endpoints
- File handling

**Description:**  
Generates QR codes dynamically via REST endpoints and handles image file processing in a Quarkus service.

### 8. [quarkus-chatbot/](quarkus-chatbot/)
A Langchain4j based chat-bot system using a local model and websocket next extension.

### 9. [quarkus-graphql/](quarkus-graphql/)
A project demonstrating:
- Building GraphQL APIs using SmallRye GraphQL
- REST API integration
- Native executable support for high performance

**Description:**  
Shows how to build GraphQL APIs in Quarkus, including REST integration and native build support for performance.

### 10. [quarkus-interceptor-tutorial/](quarkus-interceptor-tutorial/)
A tutorial project showcasing:
- Custom request interceptors
- Logging and auditing with Quarkus filters
- REST API integration

**Description:**  
Teaches how to implement custom interceptors and filters in Quarkus for logging, auditing, and request processing.

### 11. [realtime-monitor/](realtime-monitor/)
A monitoring application featuring:
- Real-time JVM heap memory monitoring
- WebSocket integration
- Dashboard UI
- Data visualization

**Description:**  
Monitors JVM memory usage in real time, pushing updates to a web dashboard using WebSockets for live visualization.

### 12. [resource-reader/](resource-reader/)
A utility application showing:
- File resource handling
- Stream processing
- Configuration management
- Error handling

**Description:**  
Provides examples for reading and processing file resources in Quarkus, with robust error and configuration management.

### 13. [security-jpa-quickstart/](security-jpa-quickstart/)
A secure Quarkus application featuring:
- JPA-based user authentication
- PostgreSQL database integration
- REST API endpoints
- Hibernate ORM with Panache

**Description:**  
Demonstrates secure authentication and authorization using JPA entities, with a PostgreSQL backend and RESTful APIs.

### 14. [sentiment-analysis/](sentiment-analysis/)
A REST API for sentiment analysis featuring:
- Integration with LangChain4j and Ollama
- REST endpoints for text sentiment analysis
- Native executable support for optimal performance

**Description:**  
Implements a sentiment analysis API using AI models, with endpoints for analyzing text and support for native compilation.

### 15. [validation-example/](validation-example/)
A Quarkus application demonstrating form validation with:
- Jakarta Validation annotations
- Localized error messages (English and French)
- REST endpoints with form handling
- Qute templates for HTML rendering

**Description:**  
Shows how to implement robust input validation in Quarkus REST endpoints and web forms, including internationalization of error messages.

## 🚧 Work in Progress

This repo will grow with the publication. Planned future topics include:

- Streaming secure file downloads from object stores or databases
- Building developer portals with Backstage and Red Hat Developer Hub
- Using PostgreSQL and Quarkus Dev Services for fast local prototyping
- Implementing advanced AI agents with Java and LangChain4j

Watch the repo or subscribe to the Substack to stay updated.

## 📬 Follow and Connect

Stay up to date and reach out through my channels:

- 🐦 Twitter/X: [@myfear](https://twitter.com/myfear)
- 💻 GitHub: [myfear](https://github.com/myfear)
- 🔗 LinkedIn: [Markus Eisele](https://www.linkedin.com/in/markuseisele/)
- 📰 Substack: [Enterprise Java and Quarkus](https://myfear.substack.com/)

---

**⭐️ Star this repo** if you find the content useful.  
**📢 Share with your team** if you're working on Java modernization, AI integration, or cloud-native enterprise apps.
