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
- [ai-document-assistant/](ai-document-assistant/): Document Q&A system with LangChain4j, Ollama, and pgvector for RAG capabilities.
- [ai-dungeon-master/](ai-dungeon-master/): Interactive text-based adventure game with AI dungeon master using LangChain4j and Ollama for dynamic storytelling.
- [ai-email-simulator/](ai-email-simulator/): Automating email generation and task extraction with LangChain4j and Ollama.  
  [Substack article](https://myfear.substack.com/p/quarkus-langchain4j-local-ai-task-extractor)
- [ai-error-handler-pii/](ai-error-handler-pii/): AI-powered exception handling, PII redaction, and user-friendly error messages.  
  [Substack article](https://myfear.substack.com/p/quarkus-ai-exception-handling-pii)
- [ai-memory/](ai-memory/): AI memory management and conversation context handling with LangChain4j.
- [captains-log-generator/](captains-log-generator/): Star Trek-style captain's log generator using AI with stardate calculation and mood-based entries.
- [credit-card-validator/](credit-card-validator/): AI-powered credit card validation using Luhn algorithm, brand detection, and LangChain4j chat interface.
- [dad-joke-generator/](dad-joke-generator/): AI-powered dad joke generator with LangChain4j and Ollama for guaranteed groans.
- [data-agent/](data-agent/): AI agent for CSV data analysis using tool-calling and local LLMs.  
  [Substack article](https://myfear.substack.com/p/ai-java-agent-quarkus-langchain4j-ollama)
- [dns-ai/](dns-ai/): AI-powered DNS lookup and analysis tool using LangChain4j with DNS record querying capabilities.
- [embedding-tutorial/](embedding-tutorial/): Text similarity and embeddings with LangChain4j and Ollama.  
  [Substack article](https://myfear.substack.com/p/java-quarkus-text-embeddings-similarity)
- [expense-splitter/](expense-splitter/): AI-powered expense splitting with receipt image processing using LangChain4j and Ollama.
- [flight-tracker-ollama/](flight-tracker-ollama/): AI-powered flight tracking with real-time aviation data and natural language queries.
- [gospel-in-code/](gospel-in-code/): Bible analysis application with semantic comparison, sentiment analysis, and vector embeddings.
- [intelligent-ticketing/](intelligent-ticketing/): AI-powered customer support system with ticket classification, knowledge base search, and automated processing using embeddings.
- [jvm-inspector-ai/](jvm-inspector-ai/): AI-powered JVM monitoring tool with thread analysis, deadlock detection, and intelligent diagnostics using LangChain4j.
- [llm-filter-demo/](llm-filter-demo/): Content moderation system using Bloom filters for fast pre-filtering and LLM for final content safety analysis.
- [llm-observability/](llm-observability/): LLM monitoring and observability tools for tracking AI model performance and usage.
- [quarkus-chain-of-thought/](quarkus-chain-of-thought/): AI reasoning application implementing structured chain-of-thought problem solving.
- [quarkus-chatbot/](quarkus-chatbot/): Real-time AI chatbot with LangChain4j and WebSocket.  
  [Substack article](https://myfear.substack.com/p/java-ai-chatbot-quarkus-langchain4j-websockets)
- [quarkus-image-describer/](quarkus-image-describer/): Describes images with a LangChain4j service.
[Substack article](https://myfear.substack.com/p/quarkus-langchain4j-image-description-api)
- [quarkus-langchain4j-observability/](quarkus-langchain4j-observability/): Observability and monitoring for LangChain4j applications in Quarkus.
- [quarkus-meme-generator/](quarkus-meme-generator/): AI-powered meme generator using LangChain4j and image processing.
- [semantic-llm-router/](semantic-llm-router/): Intelligent LLM request routing based on semantic analysis and model capabilities.
- [sentiment-analysis/](sentiment-analysis/): REST API for sentiment analysis with LangChain4j and Ollama.  
  [Substack article](https://myfear.substack.com/p/quarkus-dto-mapstruct-guide)
- [structured-ollama-tutorial/](structured-ollama-tutorial/): Structured data extraction with Ollama and LangChain4j.  
  [Substack article](https://myfear.substack.com/p/structured-data-llm-quarkus-langchain4j)
- [summarization-agent/](summarization-agent/): AI-powered text summarization agent with document processing capabilities.
- [grumbles-live/](grumbles-live/): Humorous AI news anchor built with LangChain4j.
[Substack article](https://myfear.substack.com/p/quarkus-langchain4j-sarcastic-ai-news-anchor)
- [happy-place-app/](happy-place-app/): Sentiment-based content generator using local LLMs.
[Substack article](https://myfear.substack.com/p/quarkus-ai-happy-feed-reactive-app)
- [openapi-ollama-doc-generator/](openapi-ollama-doc-generator/): Generate docs from OpenAPI using Ollama.
[Substack article](https://myfear.substack.com/p/quarkus-openapi-ai-docs-langchain4j)

### 📊 Data Processing & Integration
- [collaborative-editor/](collaborative-editor/): Real-time collaborative text editor using CRDT (Conflict-free Replicated Data Types) with WebSocket synchronization and AI writing assistance.
- [csv-uploader/](csv-uploader/): Dynamic CSV ingestion and schema creation in PostgreSQL.  
  [Substack article](https://myfear.substack.com/p/dynamic-csv-uploads-java-quarkus-postgresql)
- [file-upload-progress/](file-upload-progress/): Chunked file uploads with real-time progress tracking using Server-Sent Events (SSE).
- [quarkus-docling-converter/](quarkus-docling-converter/): Document conversion service using Docling library to convert various formats to text.
- [reactive-db-app/](reactive-db-app/): Reactive database operations and streaming with Quarkus and reactive programming patterns.
- [reactive-streaming-example/](reactive-streaming-example/): Reactive streams implementation with backpressure handling and event processing.
- [resource-reader/](resource-reader/): File resource handling, stream processing, and error handling.  
  [Substack article](https://myfear.substack.com/p/structured-data-llm-quarkus-langchain4j)
- [temporary-file-sharing/](temporary-file-sharing/): Secure temporary file sharing service with download limits and automatic cleanup using MinIO object storage.
- [qr-code-demo/](qr-code-demo/): QR code generation, image processing, and REST endpoints.  
  [Substack article](https://myfear.substack.com/p/qr-code-java-quarkus)
- [hibernate-search-orm-elasticsearch-quickstart/](hibernate-search-orm-elasticsearch-quickstart/): Hibernate Search quickstart with Elasticsearch.
[Substack article](https://myfear.substack.com/p/hibernate-search-quarkus-elasticsearch-tutorial)
- [quarkus-hibernate-filters/](quarkus-hibernate-filters/): Query filtering using Hibernate ORM.
[Substack article](https://myfear.substack.com/p/quarkus-hibernate-filters-soft-deletes)
- [color-palette-extractor/](color-palette-extractor/): Color palette extraction from images.

### 🛡️ Security & Error Handling
- [api-key-manager/](api-key-manager/): Secret Agent themed API key management system with secure key generation, rotation, and usage tracking.
- [custom-error-pages-classic/](custom-error-pages-classic/): Custom error pages and JSON error responses with Qute and ExceptionMappers.  
  [Substack article](https://myfear.substack.com/p/quarkus-custom-error-pages-rest-qute)
- [jwt-case/](jwt-case/): JWT-based authentication and authorization.  
  [Substack article](https://myfear.substack.com/p/jwt-quarkus-murder-mystery)
- [security-jpa-quickstart/](security-jpa-quickstart/): JPA-based user authentication and PostgreSQL integration.  
  [Substack article](https://myfear.substack.com/p/jwt-quarkus-murder-mystery)
- [security-jpa-quickstart2/](security-jpa-quickstart2/): Alternative JPA-based authentication example.
- [totp-vault/](totp-vault/): TOTP and Vault-based secrets management.  
  [Substack article](https://myfear.substack.com/p/secure-java-api-totp-quarkus-vault)
- [quarkflix-guards/](quarkflix-guards/): Hands on CDI Interceptors tutorial
[Substack article](https://myfear.substack.com/p/quarkus-cdi-interceptors-real-world)
- [quarkus-response-tutorial/](quarkus-response-tutorial/): HTTP Response handling
[Substack article](https://myfear.substack.com/p/quarkus-http-response-guide-java-developers)

### 🏗️ Application Architecture & Patterns
- [composite-key-app/](composite-key-app/): Demonstrates composite keys with Hibernate Panache and student enrollment modeling.
- [flyway-adventure/](flyway-adventure/): Database migrations with Flyway.  
  [Substack article](https://myfear.substack.com/p/quarkus-flyway-database-migrations-java)
- [http-header-tutorial/](http-header-tutorial/): HTTP header handling tutorial with request/response manipulation and GZIP compression.
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
- [chirper/](chirper/): Simple microblogging demo.
[Substack article](https://myfear.substack.com/p/build-twitter-clone-quarkus-kafka-qute)
- [credit-line-app/](credit-line-app/): Workflow example managing credit line approvals.
[Substack article](https://myfear.substack.com/p/quarkus-credit-approval-state-machine)
- [greeting-operator/](greeting-operator/): Kubernetes operator for greeting resources.
[Substack article](https://myfear.substack.com/p/java-kubernetes-operator-quarkus-guide)
- [mission-control/](mission-control/): Mission planning service with async tasks.
[Substack article](https://myfear.substack.com/p/quarkus-ai-pii-redaction-dashboard)
- [multi-tenant-todo-app/](multi-tenant-todo-app/): Multi-tenant TODO API.
[Substack article](https://myfear.substack.com/p/quarkus-multi-tenant-todo-java-hibernate)
- [quarkus-panache-transaction-tutorial/](quarkus-panache-transaction-tutorial/): Transaction patterns with Hibernate Panache.
[Substack article](https://myfear.substack.com/p/quarkus-transactions-panache-guide?r=17bggb)
- [dynamic-agent-spawner/](dynamic-agent-spawner/): Dynamic agent spawning example.
[Substack article](https://myfear.substack.com/p/agentic-java-multi-model-ai-quarkus)
- [dynamic-renderer/](dynamic-renderer/): Dynamic rendering service.
[Substack article](https://myfear.substack.com/p/quarkus-langchain4j-ollama-two-step-ai-pipeline)
- [url-shortener/](url-shortener/): URL shortening service.
[Substack article](https://myfear.substack.com/p/java-quarkus-url-shortener-tutorial)
- [wizard-app/](wizard-app/): Wizard-style application UI.
[Substack article](https://myfear.substack.com/p/quarkus-multi-step-form-session-csrf)
### 🛠️ Utilities, Monitoring & Misc
- [realtime-monitor/](realtime-monitor/): JVM heap memory monitoring and dashboard UI.  
  [Substack article](https://myfear.substack.com/p/quarkus-dev-services-continuous-testing)
- [quote-cli/](quote-cli/): CLI tool for quotes or text processing.  
  [Substack article](https://myfear.substack.com/p/quarkus-native-cli-java-quotes)
- [cloud-metrics/](cloud-metrics/): Tenant-aware metrics and rate limiting demo.
- [matrix-test/](matrix-test/): Simple Quarkus demo inspired by The Matrix.
- [quarkus-coverage-tutorial/](quarkus-coverage-tutorial/): Jacoco code coverage setup.
- [quarkus-otel-adventure/](quarkus-otel-adventure/): OpenTelemetry observability and distributed tracing implementation with Quarkus microservices.
- [quarkus-task-runner/](quarkus-task-runner/): Background task execution and scheduling with Quarkus and job processing patterns.
- [quarkus-unleash-tutorial/](quarkus-unleash-tutorial/): Feature flags and toggles implementation using Unleash with Quarkus for controlled feature rollouts.

## 🚧 Work in Progress

This repo will grow with the publication. Planned future topics include:

- Streaming secure file downloads from object stores or databases
- Building developer portals with Backstage and Red Hat Developer Hub
- Using PostgreSQL and Quarkus Dev Services for fast local prototyping
- Implementing advanced AI agents with Java and LangChain4j

Watch the repo or subscribe to the Substack to stay updated.

## 📬 Follow and Connect

Stay up to date and reach out through my channels:

- Mastodon: [myfear@mastodon.online](https://mastodon.online/@myfear)
- Bluesky: [@myfear.com](https://bsky.app/profile/myfear.com)
- 🐦 Twitter/X: [@myfear](https://twitter.com/myfear)
- 💻 GitHub: [myfear](https://github.com/myfear)
- 🔗 LinkedIn: [Markus Eisele](https://www.linkedin.com/in/markuseisele/)
- 📰 Substack: [Enterprise Java and Quarkus](https://myfear.substack.com/)

---

**⭐️ Star this repo** if you find the content useful.  
**📢 Share with your team** if you're working on Java modernization, AI integration, or cloud-native enterprise apps.
