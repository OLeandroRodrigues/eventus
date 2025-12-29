# Eventus

**Eventus** is a modular Java library for building **event-driven systems** with clear architectural boundaries, focusing on **decoupling**, **reliability**, and **long-term evolvability**.

Eventus is not a message broker.  
It is an architectural toolkit that defines **explicit contracts** for events, handlers, retries, idempotency, and failure handling, while delegating transport and infrastructure concerns to external systems such as message brokers or application frameworks.

---

## Motivation

Many systems adopt messaging technologies but still suffer from:

- tight coupling between services  
- fragile synchronous workflows  
- hidden retry logic  
- duplicated side effects  
- unclear failure semantics  

Eventus exists to make **Event-Driven Architecture an intentional design choice**, not an accidental outcome of using a messaging tool.

---

## Core Ideas

Eventus is built around a few strong assumptions:

- **Events represent facts that already happened**
- **Delivery is at-least-once by design**
- **Handlers must be idempotent**
- **Failures are explicit and observable**
- **Infrastructure is separate from domain logic**

These ideas are expressed through **code-level contracts**, not conventions or framework magic.

---

## Project Structure

Eventus is organized as a multi-module project:

```text
eventus/
├── eventus-core
├── eventus-transport-inmemory
├── eventus-transport-rabbitmq
└── eventus-spring-boot-starter
```

---

