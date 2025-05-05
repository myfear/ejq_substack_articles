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

### 1. Validation Example (`validation-example/`)
A Quarkus application demonstrating form validation with:
- Jakarta Validation annotations
- Localized error messages (English and French)
- REST endpoints with form handling
- Qute templates for HTML rendering

### 2. Security JPA Quickstart (`security-jpa-quickstart/`)
A secure Quarkus application featuring:
- JPA-based user authentication
- PostgreSQL database integration
- REST API endpoints
- Hibernate ORM with Panache

### 3. QR Code Demo (`qr-code-demo/`)
A practical example showing:
- QR code generation
- Image processing
- REST API endpoints
- File handling

### 4. CSV Uploader (`csv-uploader/`)
A file processing application with:
- CSV file upload and parsing
- Automatic table creation with inferred column types
- Metadata tracking of uploads
- REST API endpoints for data access

### 5. Realtime Monitor (`realtime-monitor/`)
A monitoring application featuring:
- Real-time JVM heap memory monitoring
- WebSocket integration
- Dashboard UI
- Data visualization

### 6. Embedding Tutorial (`embedding-tutorial/`)
An AI-focused project demonstrating:
- Text embedding generation
- Vector similarity search
- AI model integration
- Data processing pipelines

### 7. Resource Reader (`resource-reader/`)
A utility application showing:
- File resource handling
- Stream processing
- Configuration management
- Error handling

### 8. AI Email Simulator (`ai-email-simulator/`)
A Quarkus-based application that:
- Simulates email generation and processing using AI models
- Integrates LangChain4j for advanced AI capabilities
- Manages tasks via a to-do list

### 9. Sentiment Analysis API (`sentiment-analysis/`)
A REST API for sentiment analysis featuring:
- Integration with LangChain4j and Ollama
- REST endpoints for text sentiment analysis
- Native executable support for optimal performance

### 10. Quarkus GraphQL (`quarkus-graphql/`)
A project demonstrating:
- Building GraphQL APIs using SmallRye GraphQL
- REST API integration
- Native executable support for high performance

### 11. Quarkus Interceptor Tutorial (`quarkus-interceptor-tutorial/`)
A tutorial project showcasing:
- Custom request interceptors
- Logging and auditing with Quarkus filters
- REST API integration

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
