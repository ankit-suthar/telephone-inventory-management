# Telephone Inventory Management

A Spring Boot–based system for managing telephone number inventory across multiple data stores (Postgres, Cassandra, Elasticsearch).  
It supports number booking, searching, and auditing operations, making it useful for telecom provisioning workflows.

---

## Features
- 📞 Manage telephone number inventory  
- 🔍 Search available numbers  
- 📝 Book and transition numbers with audit tracking  
- 🗄️ Supports multiple storage backends:
  - PostgreSQL
  - Cassandra
  - Elasticsearch  

---

## Project Structure
- **controller/** – REST controllers (`CustomerServiceController`, `SearchController`)  
- **service/** – Business logic (`NumberBookingService`, `SearchService`)  
- **model/** – Request/response objects (`SearchRequest`, `TransitionRequest`)  
- **repository/** – Repositories for Postgres, Cassandra, and Elasticsearch  
- **resources/** – Application configuration (`application.properties`)  

---

## Tech Stack
- Java 17+
- Spring Boot
- Maven
- PostgreSQL, Cassandra, Elasticsearch

---

## Getting Started

### Prerequisites
- Java 17+
- Maven 3.6+
- Running instances of PostgreSQL, Cassandra, and Elasticsearch

### Build & Run
```bash
# Build project
mvn clean install

# Run application
mvn spring-boot:run
