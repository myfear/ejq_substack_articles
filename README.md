## 📰 About the Publication

Welcome to [The Main Thread](https://www.the-main-thread.com/) — your strategic companion for navigating the evolving world of enterprise Java, software architecture, and AI-infused systems.

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

## Build Status

<!-- BUILD_BADGES_START -->
| Project | Status |
| :--- | :--- |
| **age-everywhere** | ![age-everywhere](https://img.shields.io/endpoint?url=https://raw.githubusercontent.com/myfear/ejq_substack_articles/badges/age-everywhere.json) |
| **ai-design-critic** | ![ai-design-critic](https://img.shields.io/endpoint?url=https://raw.githubusercontent.com/myfear/ejq_substack_articles/badges/ai-design-critic.json) |
| **ai-detector** | ![ai-detector](https://img.shields.io/endpoint?url=https://raw.githubusercontent.com/myfear/ejq_substack_articles/badges/ai-detector.json) |
| **ai-document-assistant** | ![ai-document-assistant](https://img.shields.io/endpoint?url=https://raw.githubusercontent.com/myfear/ejq_substack_articles/badges/ai-document-assistant.json) |
| **ai-dungeon-master** | ![ai-dungeon-master](https://img.shields.io/endpoint?url=https://raw.githubusercontent.com/myfear/ejq_substack_articles/badges/ai-dungeon-master.json) |
<!-- BUILD_BADGES_END -->

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
- [ai-whitepaper-generator/](ai-whitepaper-generator/): AI-powered white paper generator using LangChain4j, Ollama, and iText for PDF generation.  
  [Substack article](https://myfear.substack.com/p/ai-whitepaper-generator-quarkus-langchain4j-itext)
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
- [quarkus-supervisor-ai/](quarkus-supervisor-ai/): AI supervisor system with multiple specialized agents using Kafka messaging for distributed AI workflow orchestration.
- [semantic-llm-router/](semantic-llm-router/): Intelligent LLM request routing based on semantic analysis and model capabilities.
- [sentiment-analysis/](sentiment-analysis/): REST API for sentiment analysis with LangChain4j and Ollama.  
  [Substack article](https://myfear.substack.com/p/quarkus-dto-mapstruct-guide)
- [structured-ollama-tutorial/](structured-ollama-tutorial/): Structured data extraction with Ollama and LangChain4j.  
  [Substack article](https://myfear.substack.com/p/structured-data-llm-quarkus-langchain4j)
- [summarization-agent/](summarization-agent/): AI-powered text summarization agent with document processing capabilities.
- [text-analytics/](text-analytics/): Character interaction graph analysis for Huckleberry Finn using Quarkus, OpenNLP, and Ollama for sentiment analysis.  
  [Substack article](https://myfear.substack.com/p/text-analytics-quarkus-opennlp-huckleberry-finn)
- [grumbles-live/](grumbles-live/): Humorous AI news anchor built with LangChain4j.
  [Substack article](https://myfear.substack.com/p/quarkus-langchain4j-sarcastic-ai-news-anchor)
- [happy-place-app/](happy-place-app/): Sentiment-based content generator using local LLMs.
  [Substack article](https://myfear.substack.com/p/quarkus-ai-happy-feed-reactive-app)
- [openapi-ollama-doc-generator/](openapi-ollama-doc-generator/): Generate docs from OpenAPI using Ollama.
  [Substack article](https://myfear.substack.com/p/quarkus-openapi-ai-docs-langchain4j)
- [ai-design-critic/](ai-design-critic/): AI-powered design critique and feedback system.
  [Substack article](https://www.the-main-thread.com/p/langchain4j-learning-path-java-quarkus-ai)

### 📊 Data Processing & Integration
- [collaborative-editor/](collaborative-editor/): Real-time collaborative text editor using CRDT (Conflict-free Replicated Data Types) with WebSocket synchronization and AI writing assistance.
- [csv-uploader/](csv-uploader/): Dynamic CSV ingestion and schema creation in PostgreSQL.  
  [Substack article](https://myfear.substack.com/p/dynamic-csv-uploads-java-quarkus-postgresql)
- [file-upload-progress/](file-upload-progress/): Chunked file uploads with real-time progress tracking using Server-Sent Events (SSE).
- [panache-primevue-tutorial/](panache-primevue-tutorial/): Beautiful PrimeVue table backed by Quarkus and Panache with filterable, sortable Vue 3 frontend.  
  [Substack article](https://myfear.substack.com/p/quarkus-primevue-table-panache-tutorial)
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
- [pdf-processing-pipeline/](pdf-processing-pipeline/): PDF processing and document analysis pipeline.
- [substack-discovery/](substack-discovery/): Substack content discovery and analysis tool.

### 🛡️ Security & Error Handling
- [api-key-manager/](api-key-manager/): Secret Agent themed API key management system with secure key generation, rotation, and usage tracking.
- [custom-error-pages-classic/](custom-error-pages-classic/): Custom error pages and JSON error responses with Qute and ExceptionMappers.  
  [Substack article](https://myfear.substack.com/p/quarkus-custom-error-pages-rest-qute)
- [jwt-case/](jwt-case/): JWT-based authentication and authorization.  
  [Substack article](https://myfear.substack.com/p/jwt-quarkus-murder-mystery)
- [secure-diary/](secure-diary/): Secure diary application with Bouncy Castle encryption, protecting thoughts one encrypted entry at a time.  
  [Substack article](https://myfear.substack.com/p/secure-diary-quarkus-bouncy-castle-java-tutorial)
- [security-jpa-quickstart/](security-jpa-quickstart/): JPA-based user authentication and PostgreSQL integration.  
  [Substack article](https://myfear.substack.com/p/jwt-quarkus-murder-mystery)
- [security-jpa-quickstart2/](security-jpa-quickstart2/): Alternative JPA-based authentication example.
- [self-learning-waf/](self-learning-waf/): Self-learning Web Application Firewall using DeepLearning4j for reactive Java security without static rules.  
  [Substack article](https://myfear.substack.com/p/self-learning-waf-quarkus-java-deeplearning4j)
- [totp-vault/](totp-vault/): TOTP and Vault-based secrets management.  
  [Substack article](https://myfear.substack.com/p/secure-java-api-totp-quarkus-vault)
- [zero-trust-uploads/](zero-trust-uploads/): Zero-trust file uploads with ClamAV antivirus scanning using secure in-memory pipeline before storage.  
  [Substack article](https://www.the-main-thread.com/p/zero-trust-file-uploads-quarkus-antivirus)
- [quarkflix-guards/](quarkflix-guards/): Hands on CDI Interceptors tutorial
  [Substack article](https://myfear.substack.com/p/quarkus-cdi-interceptors-real-world)
- [quarkus-response-tutorial/](quarkus-response-tutorial/): HTTP Response handling
  [Substack article](https://myfear.substack.com/p/quarkus-http-response-guide-java-developers)
- [quarkus-otp/](quarkus-otp/): TOTP (Time-based One-Time Password) implementation with Quarkus.
  [Substack article](https://www.the-main-thread.com/p/java-quarkus-totp-tan-security-tutorial)
- [secure-service/](secure-service/): Secure service implementation with authentication and authorization.
- [fraud-detector/](fraud-detector/): Fraud detection system with machine learning capabilities.

### 🏗️ Application Architecture & Patterns
- [breadcrumb-navigation/](breadcrumb-navigation/): Dynamic breadcrumb navigation component in Quarkus with zero-configuration auto-generation from URI paths.  
  [Substack article](https://myfear.substack.com/p/quarkus-qute-dynamic-breadcrumb)
- [composite-key-app/](composite-key-app/): Demonstrates composite keys with Hibernate Panache and student enrollment modeling.
- [flyway-adventure/](flyway-adventure/): Database migrations with Flyway.  
  [Substack article](https://myfear.substack.com/p/quarkus-flyway-database-migrations-java)
- [hll-tutorial/](hll-tutorial/): Scalable unique counting with HyperLogLog algorithm for counting millions of things with kilobytes of memory.  
  [Substack article](https://myfear.substack.com/p/quarkus-hyperloglog-unique-counting-java)
- [http-header-tutorial/](http-header-tutorial/): HTTP header handling tutorial with request/response manipulation and GZIP compression.
- [quarkus-dto-tutorial/](quarkus-dto-tutorial/): DTO mapping and transformation.  
  [Substack article](https://myfear.substack.com/p/quarkus-dto-mapstruct-guide)
- [quarkus-graphql/](quarkus-graphql/): Building GraphQL APIs in Quarkus.  
  [Substack article](https://myfear.substack.com/p/langchain4j-graphql-websocket-next)
- [quarkus-interceptor-tutorial/](quarkus-interceptor-tutorial/): Custom request interceptors, logging, and auditing.  
  [Substack article](https://myfear.substack.com/p/langchain4j-graphql-websocket-next)
- [quarkus-timezone-tutorial/](quarkus-timezone-tutorial/): Timezone handling best practices with Quarkus and Hibernate Panache for proper date management.  
  [Substack article](https://myfear.substack.com/p/quarkus-java-timezone-best-practices)
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
- [a2a-demo/](a2a-demo/): Agent-to-Agent communication demonstration.
- [age-everywhere/](age-everywhere/): Age calculation and date handling service.
  [Substack article](https://www.the-main-thread.com/p/java-date-time-quarkus-birthday-api-tutorial)
- [approval-workflow/](approval-workflow/): Business process management and approval workflows.
- [archunit-bce/](archunit-bce/): Architecture testing with ArchUnit for business capability enforcement.
  [Substack article](https://www.the-main-thread.com/p/quarkus-archunit-java-bce-architecture-tutorial)
- [audit-app/](audit-app/): Application auditing and monitoring system.
  [Substack article](https://www.the-main-thread.com/p/quarkus-business-score-java-tutorial-detect-zombie-apps)
- [banner-studio/](banner-studio/): ASCII banner generation and styling tool.
  [Substack article](https://www.the-main-thread.com/p/quarkus-ascii-banner-studio-qute-figlet)
- [car-filter-app/](car-filter-app/): Car filtering and search application.
- [cdi-events-demo/](cdi-events-demo/): CDI events and observer pattern demonstration.
- [coffee-api/](coffee-api/): Coffee shop API with OpenAPI documentation.
  [Substack article](https://www.the-main-thread.com/p/quarkus-openapi-java-coffee-api-tutorial)
- [community-app/](community-app/): Community management application.
  [Substack article](https://www.the-main-thread.com/p/remote-office-work-from-home-guide)
- [fleet-insure/](fleet-insure/): Fleet insurance management system.
- [granular-rbac-tutorial/](granular-rbac-tutorial/): Granular Role-Based Access Control implementation.
- [greet-hypermedia/](greet-hypermedia/): Hypermedia API implementation with HATEOAS.
  [Substack article](https://www.the-main-thread.com/p/spring-hateoas-to-quarkus-hypermedia-migration)
- [meme-service/](meme-service/): Meme generation and management service.
  [Substack article](https://www.the-main-thread.com/p/quarkus-json-jackson-langchain4j-meme-service)
- [museum/](museum/): Museum management and exhibition system.
- [negotiation-simulator/](negotiation-simulator/): Business negotiation simulation system.
- [order-service/](order-service/): Order processing and management service.
- [poll-doodle/](poll-doodle/): Polling and scheduling application.
  [Substack article](https://www.the-main-thread.com/p/quarkus-java-doodle-group-poll-tutorial)
- [property-testing/](property-testing/): Property-based testing with Jqwik.
- [qualifiers-lab/](qualifiers-lab/): CDI qualifiers and dependency injection lab.
- [quantum-identicons/](quantum-identicons/): Quantum-inspired identicon generation.
- [quarkus-agentic/](quarkus-agentic/): Agentic AI system implementation.
- [quarkus-ai-debugger/](quarkus-ai-debugger/): AI-powered debugging assistant.
- [quarkus-api-testing/](quarkus-api-testing/): API testing framework and examples.
  [Substack article](https://www.the-main-thread.com/p/quarkus-rest-api-testing-vs-spring-mockmvc)
- [quarkus-coverage-tutorial/](quarkus-coverage-tutorial/): Code coverage analysis and reporting.
- [quarkus-guardrails/](quarkus-guardrails/): AI guardrails and safety measures.
- [quarkus-ldap-demo/](quarkus-ldap-demo/): LDAP authentication integration.
  [Substack article](https://www.the-main-thread.com/p/quarkus-ldap-authentication-tutorial)
- [quarkus-mcp/](quarkus-mcp/): Model Context Protocol implementation.
  [Substack article](https://www.the-main-thread.com/p/java-quarkus-langchain4j-ollama-mcp-tutorial)
- [quarkus-mutation-demo/](quarkus-mutation-demo/): Mutation testing demonstration.
- [quarkus-read-replica/](quarkus-read-replica/): Database read replica configuration.
- [quarkus-roq-tutorial/](quarkus-roq-tutorial/): Roq framework tutorial and examples.
- [quarkus-soap-multi/](quarkus-soap-multi/): Multi-protocol SOAP service implementation.
- [quarkus-todo-app/](quarkus-todo-app/): Todo application with CRUD operations.
- [quarkus-versioning-tutorial/](quarkus-versioning-tutorial/): API versioning strategies and implementation.
- [quarky-cart/](quarky-cart/): Shopping cart implementation.
- [renarde-tasks/](renarde-tasks/): Task management with Renarde framework.
  [Substack article](https://www.the-main-thread.com/p/quarkus-renarde-full-stack-java-web-tutorial)
- [resilient-ai/](resilient-ai/): AI resilience and fault tolerance patterns.
  [Substack article](https://www.the-main-thread.com/p/quarkus-fault-tolerance-langchain4j-ai-resilience)
- [rest-deprecation-practice/](rest-deprecation-practice/): REST API deprecation strategies.
- [rest-testing-demo/](rest-testing-demo/): REST API testing with RestAssured and Pact.
  [Substack article](https://www.the-main-thread.com/p/quarkus-api-testing-restassured-pact-jqwik)
- [roq-author-website/](roq-author-website/): Author website built with Roq framework.
- [unicode-greetings/](unicode-greetings/): Unicode and internationalization examples.
- [very-simple-app/](very-simple-app/): Minimal Quarkus application example.
  [Substack article](https://www.the-main-thread.com/p/proof-of-concept-java-quarkus-guide)
- [xa-transaction-tutorial/](xa-transaction-tutorial/): XA transaction management tutorial.
- [year-end-invoices/](year-end-invoices/): Year-end invoice processing system.

### 🛠️ Utilities, Monitoring & Misc
- [error-handling-app/](error-handling-app/): RFC 7807 standardized error handling in Quarkus for secure, standards-based API error responses.  
  [Substack article](https://myfear.substack.com/p/quarkus-rfc7807-error-handling-java)
- [geonames-search/](geonames-search/): High-performance autocomplete search with Quarkus, Panache, and PostgreSQL for geographic locations.  
  [Substack article](https://myfear.substack.com/p/high-performance-autocomplete-search-quarkus-java)
- [health-checker/](health-checker/): Website health monitoring service using MicroProfile Health, YAML configuration, and HttpClient for uptime checking.  
  [Substack article](https://myfear.substack.com/p/quarkus-website-health-check-yaml)
- [household-chore-assignment/](household-chore-assignment/): Fair household chore assignment using the Gale-Shapley algorithm for stable matching.  
  [Substack article](https://myfear.substack.com/p/fair-chore-assignment-java-quarkus-gale-shapley)
- [quarkus-sftp-compose/](quarkus-sftp-compose/): Smart SFTP file upload system with PostgreSQL metadata storage using Quarkus Dev Services.  
  [Substack article](https://myfear.substack.com/p/quarkus-sftp-postgresql-file-upload-tutorial)
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
- [api-perf-resilience/](api-perf-resilience/): API performance and resilience patterns.
  [Substack article](https://www.the-main-thread.com/p/quarkus-api-performance-caching-rate-limiting-fault-tolerance)
- [bsky-javafeed-generator/](bsky-javafeed-generator/): Bluesky Java feed generator.
- [bsky-javafeed-generator-reactive/](bsky-javafeed-generator-reactive/): Reactive Bluesky Java feed generator.
- [caching-tutorial/](caching-tutorial/): Caching strategies and implementation.
- [camel-rest-sql/](camel-rest-sql/): Apache Camel REST SQL integration.
- [camel-rss-quarkus/](camel-rss-quarkus/): Apache Camel RSS integration with Quarkus.
- [log-to-obs/](log-to-obs/): Logging to observability platforms.
- [mutiny-test/](mutiny-test/): Mutiny reactive programming testing.

## 🚧 Work in Progress

This repo will grow alongside the publication. Have an idea for a new tutorial or example you'd like to see covered? 👉 [Open an Issue and suggest it](https://github.com/myfear/ejq_substack_articles/issues)!

🤝 Contributing

Contributions are welcome! If you'd like to:
- Fix a bug or improve code → fork the repo and open a Pull Request
- Share feedback or ideas → join the discussion in Issues
- Comment on articles or reach out to me directly!

Every contribution, big or small, helps improve the learning experience for everyone following The Main Thread.

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