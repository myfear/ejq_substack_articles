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

Below are the example projects included in this repository, listed alphabetically. Each project folder is linked for easy access, and where available, a link to the corresponding Substack article is provided:

### 1. [ai-email-simulator/](ai-email-simulator/)
A Quarkus-based application that simulates email generation and processing using AI models, integrates LangChain4j for advanced AI capabilities, and manages tasks via a to-do list.
- [Source code](./ai-email-simulator/)
- [Substack article](https://myfear.substack.com/p/quarkus-langchain4j-local-ai-task-extractor)

### 2. [ai-error-handler-pii/](ai-error-handler-pii/)
A Quarkus app that demonstrates AI-powered exception handling with LangChain4j, including PII redaction and user-friendly error messages.
- [Source code](./ai-error-handler-pii/)
- [Substack article](https://myfear.substack.com/p/quarkus-ai-exception-handling-pii)

### 3. [csv-uploader/](csv-uploader/)
A file processing application for uploading and parsing CSV files, with automatic table creation, metadata tracking, and REST API endpoints for data access.
- [Source code](./csv-uploader/)
- [Substack article](https://myfear.substack.com/p/ai-java-agent-quarkus-langchain4j-ollama)

### 4. [custom-error-pages-classic/](custom-error-pages-classic/)
A project demonstrating how to handle exceptions and serve custom error pages in Quarkus using Qute templates and ExceptionMappers.
- [Source code](./custom-error-pages-classic/)
- [Substack article](https://myfear.substack.com/p/quarkus-custom-error-pages-rest-qute)

### 5. [data-agent/](data-agent/)
A Quarkus and LangChain4j agent for analyzing CSV data with local LLMs (Ollama), tool-calling, and structured data extraction.
- [Source code](./data-agent/)
- [Substack article](https://myfear.substack.com/p/ai-java-agent-quarkus-langchain4j-ollama)

### 6. [dynamic-renderer/](dynamic-renderer/)
A Quarkus project for dynamic content rendering and template processing. (Description coming soon)
- [Source code](./dynamic-renderer/)

### 7. [embedding-tutorial/](embedding-tutorial/)
An AI-focused project showing how to generate text embeddings, perform vector similarity search, and integrate with AI models in Java.
- [Source code](./embedding-tutorial/)
- [Substack article](https://myfear.substack.com/p/quarkus-dto-mapstruct-guide)

### 8. [flyway-adventure/](flyway-adventure/)
A project demonstrating how to integrate Flyway into Quarkus applications and manage database schema evolutions.
- [Source code](./flyway-adventure/)
- [Substack article](https://myfear.substack.com/p/quarkus-flyway-database-migrations-java)

### 9. [grumbles-live/](grumbles-live/)
A Quarkus project for live feedback or chat features. (Description coming soon)
- [Source code](./grumbles-live/)

### 10. [happy-place-app/](happy-place-app/)
A Quarkus application for managing and sharing happy places. (Description coming soon)
- [Source code](./happy-place-app/)

### 11. [hibernate-search-orm-elasticsearch-quickstart/](hibernate-search-orm-elasticsearch-quickstart/)
A Quarkus quickstart for Hibernate Search ORM with Elasticsearch integration. (Description coming soon)
- [Source code](./hibernate-search-orm-elasticsearch-quickstart/)

### 12. [i18n-demo/](i18n-demo/)
A project demonstrating database-backed internationalization in Quarkus, including message bundles and dynamic translations.
- [Source code](./i18n-demo/)
- [Substack article](https://myfear.substack.com/p/java-quarkus-i18n-multilingual-app)

### 13. [jwt-case/](jwt-case/)
A Quarkus project for JWT-based authentication and authorization. 
- [Source code](./jwt-case/)
- [Substack article](https://myfear.substack.com/p/jwt-quarkus-murder-mystery)

### 14. [matrix-test/](matrix-test/)
A Quarkus project for matrix operations or testing. 
- [Source code](./matrix-test/)
- [Substack article](https://myfear.substack.com/p/quarkus-dev-services-continuous-testing)

### 15. [multi-tenant-todo-app/](multi-tenant-todo-app/)
A Quarkus application for multi-tenant to-do management. (Description coming soon)
- [Source code](./multi-tenant-todo-app/)

### 16. [openapi-ollama-doc-generator/](openapi-ollama-doc-generator/)
A Quarkus tool for generating OpenAPI documentation with Ollama integration. (Description coming soon)
- [Source code](./openapi-ollama-doc-generator/)

### 17. [qr-code-demo/](qr-code-demo/)
A practical example showing QR code generation, image processing, REST API endpoints, and file handling in Quarkus.
- [Source code](./qr-code-demo/)
- [Substack article](https://myfear.substack.com/p/qr-code-java-quarkus)

### 18. [quarkflix-guards/](quarkflix-guards/)
A Quarkus project for implementing service guardrails and access control. (Description coming soon)
- [Source code](./quarkflix-guards/)

### 19. [quarkus-chatbot/](quarkus-chatbot/)
A LangChain4j-based chatbot system using a local model and WebSocket Next extension for real-time, memory-aware AI chat.
- [Source code](./quarkus-chatbot/)
- [Substack article](https://myfear.substack.com/p/java-ai-chatbot-quarkus-langchain4j-websockets)

### 20. [quarkus-coverage-tutorial/](quarkus-coverage-tutorial/)
A Quarkus tutorial for code coverage and testing best practices. (Description coming soon)
- [Source code](./quarkus-coverage-tutorial/)

### 21. [quarkus-dto-tutorial/](quarkus-dto-tutorial/)
A Quarkus tutorial for DTO mapping and transformation. (Description coming soon)
- [Source code](./quarkus-dto-tutorial/)
- [Substack article](https://myfear.substack.com/p/quarkus-dto-mapstruct-guide)

### 22. [quarkus-graphql/](quarkus-graphql/)
A project demonstrating how to build GraphQL APIs in Quarkus, including REST integration and native build support for performance.
- [Source code](./quarkus-graphql/)
- [Substack article](https://myfear.substack.com/p/langchain4j-graphql-websocket-next)

### 23. [quarkus-hibernate-filters/](quarkus-hibernate-filters/)
A Quarkus project for advanced Hibernate filtering. (Description coming soon)
- [Source code](./quarkus-hibernate-filters/)

### 24. [quarkus-image-describer/](quarkus-image-describer/)
A Quarkus project for image description and analysis. (Description coming soon)
- [Source code](./quarkus-image-describer/)

### 25. [quarkus-interceptor-tutorial/](quarkus-interceptor-tutorial/)
A tutorial project showcasing custom request interceptors, logging, and auditing with Quarkus filters and REST API integration.
- [Source code](./quarkus-interceptor-tutorial/)
- [Substack article](https://myfear.substack.com/p/langchain4j-graphql-websocket-next)

### 26. [quarkus-panache-transaction-tutorial/](quarkus-panache-transaction-tutorial/)
A Quarkus tutorial for Panache transactions. 
- [Source code](./quarkus-panache-transaction-tutorial/)
- [Substack article](https://myfear.substack.com/p/quarkus-transactions-panache-guide)

### 27. [quarkus-response-tutorial/](quarkus-response-tutorial/)
A Quarkus tutorial for custom REST responses. 
- [Source code](./quarkus-response-tutorial/)
- [Substack article](https://myfear.substack.com/p/quarkus-http-response-guide-java-developers)

### 28. [quarkus-unleash-tutorial/](quarkus-unleash-tutorial/)
A Quarkus tutorial for feature flagging with Unleash. (Description coming soon)
- [Source code](./quarkus-unleash-tutorial/)

### 29. [quote-cli/](quote-cli/)
A CLI tool for quotes or text processing in Quarkus. 
- [Source code](./quote-cli/)
- [Substack article](https://myfear.substack.com/p/quarkus-native-cli-java-quotes)

### 30. [realtime-monitor/](realtime-monitor/)
A monitoring application featuring real-time JVM heap memory monitoring, WebSocket integration, dashboard UI, and data visualization.
- [Source code](./realtime-monitor/)
- [Substack article](https://myfear.substack.com/p/quarkus-dev-services-continuous-testing)

### 31. [resource-reader/](resource-reader/)
A utility application showing file resource handling, stream processing, configuration management, and error handling in Quarkus.
- [Source code](./resource-reader/)
- [Substack article](https://myfear.substack.com/p/structured-data-llm-quarkus-langchain4j)

### 32. [security-jpa-quickstart/](security-jpa-quickstart/)
A secure Quarkus application featuring JPA-based user authentication, PostgreSQL integration, REST API endpoints, and Hibernate ORM with Panache.
- [Source code](./security-jpa-quickstart/)
- [Substack article](https://myfear.substack.com/p/jwt-quarkus-murder-mystery)

### 33. [sentiment-analysis/](sentiment-analysis/)
A REST API for sentiment analysis featuring integration with LangChain4j and Ollama, REST endpoints for text sentiment analysis, and native executable support for optimal performance.
- [Source code](./sentiment-analysis/)
- [Substack article](https://myfear.substack.com/p/quarkus-dto-mapstruct-guide)

### 34. [structured-ollama-tutorial/](structured-ollama-tutorial/)
A Quarkus tutorial for structured data extraction with Ollama and LangChain4j. 
- [Source code](./structured-ollama-tutorial/)
- [Substack article](https://myfear.substack.com/p/structured-data-llm-quarkus-langchain4j)

### 35. [totp-vault/](totp-vault/)
A Quarkus project for TOTP and Vault-based secrets management. 
- [Source code](./totp-vault/)
- [Substack article](https://myfear.substack.com/p/secure-java-api-totp-quarkus-vault)

### 36. [validation-example/](validation-example/)
A Quarkus application demonstrating form validation with Jakarta Validation annotations, localized error messages, REST endpoints, and Qute templates for HTML rendering.
- [Source code](./validation-example/)
- [Substack article](https://myfear.substack.com/p/validation-java-quarkus)

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
