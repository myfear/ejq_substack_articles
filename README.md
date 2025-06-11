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

Below are the example projects included in this repository, organized by topic. Each project folder is linked for easy access, and where available, a link to the corresponding Substack article is provided:

### 🧠 AI & LLM Integration
- [ai-email-simulator/](ai-email-simulator/): Automating email generation and task extraction with LangChain4j and Ollama.  
  [Substack article](https://myfear.substack.com/p/quarkus-langchain4j-local-ai-task-extractor)
- [ai-error-handler-pii/](ai-error-handler-pii/): AI-powered exception handling, PII redaction, and user-friendly error messages.  
  [Substack article](https://myfear.substack.com/p/quarkus-ai-exception-handling-pii)
- [data-agent/](data-agent/): AI agent for CSV data analysis using tool-calling and local LLMs.  
  [Substack article](https://myfear.substack.com/p/ai-java-agent-quarkus-langchain4j-ollama)
- [embedding-tutorial/](embedding-tutorial/): Text similarity and embeddings with LangChain4j and Ollama.  
  [Substack article](https://myfear.substack.com/p/java-quarkus-text-embeddings-similarity)
- [quarkus-chatbot/](quarkus-chatbot/): Real-time AI chatbot with LangChain4j and WebSocket.  
  [Substack article](https://myfear.substack.com/p/java-ai-chatbot-quarkus-langchain4j-websockets)
- [sentiment-analysis/](sentiment-analysis/): REST API for sentiment analysis with LangChain4j and Ollama.  
  [Substack article](https://myfear.substack.com/p/quarkus-dto-mapstruct-guide)
- [structured-ollama-tutorial/](structured-ollama-tutorial/): Structured data extraction with Ollama and LangChain4j.  
  [Substack article](https://myfear.substack.com/p/structured-data-llm-quarkus-langchain4j)

### 📊 Data Processing & Integration
- [csv-uploader/](csv-uploader/): Dynamic CSV ingestion and schema creation in PostgreSQL.  
  [Substack article](https://myfear.substack.com/p/dynamic-csv-uploads-java-quarkus-postgresql)
- [resource-reader/](resource-reader/): File resource handling, stream processing, and error handling.  
  [Substack article](https://myfear.substack.com/p/structured-data-llm-quarkus-langchain4j)
- [qr-code-demo/](qr-code-demo/): QR code generation, image processing, and REST endpoints.  
  [Substack article](https://myfear.substack.com/p/qr-code-java-quarkus)

### 🛡️ Security & Error Handling
- [custom-error-pages-classic/](custom-error-pages-classic/): Custom error pages and JSON error responses with Qute and ExceptionMappers.  
  [Substack article](https://myfear.substack.com/p/quarkus-custom-error-pages-rest-qute)
- [jwt-case/](jwt-case/): JWT-based authentication and authorization.  
  [Substack article](https://myfear.substack.com/p/jwt-quarkus-murder-mystery)
- [security-jpa-quickstart/](security-jpa-quickstart/): JPA-based user authentication and PostgreSQL integration.  
  [Substack article](https://myfear.substack.com/p/jwt-quarkus-murder-mystery)
- [totp-vault/](totp-vault/): TOTP and Vault-based secrets management.  
  [Substack article](https://myfear.substack.com/p/secure-java-api-totp-quarkus-vault)

### 🏗️ Application Architecture & Patterns
- [flyway-adventure/](flyway-adventure/): Database migrations with Flyway.  
  [Substack article](https://myfear.substack.com/p/quarkus-flyway-database-migrations-java)
- [quarkus-dto-tutorial/](quarkus-dto-tutorial/): DTO mapping and transformation.  
  [Substack article](https://myfear.substack.com/p/quarkus-dto-mapstruct-guide)
- [quarkus-graphql/](quarkus-graphql/): Building GraphQL APIs in Quarkus.  
  [Substack article](https://myfear.substack.com/p/langchain4j-graphql-websocket-next)
- [quarkus-interceptor-tutorial/](quarkus-interceptor-tutorial/): Custom request interceptors, logging, and auditing.  
  [Substack article](https://myfear.substack.com/p/langchain4j-graphql-websocket-next)
- [validation-example/](validation-example/): Form validation with Jakarta Validation and Qute.  
  [Substack article](https://myfear.substack.com/p/validation-java-quarkus)
- [i18n-demo/](i18n-demo/): Database-backed internationalization.  
  [Substack article](https://myfear.substack.com/p/java-quarkus-i18n-multilingual-app)

### 🛠️ Utilities, Monitoring & Misc
- [realtime-monitor/](realtime-monitor/): JVM heap memory monitoring and dashboard UI.  
  [Substack article](https://myfear.substack.com/p/quarkus-dev-services-continuous-testing)
- [quote-cli/](quote-cli/): CLI tool for quotes or text processing.  
  [Substack article](https://myfear.substack.com/p/quarkus-native-cli-java-quotes)

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
