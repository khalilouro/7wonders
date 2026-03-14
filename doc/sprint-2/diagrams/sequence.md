```mermaid
sequenceDiagram
    participant User
    participant System

    User->>System: runProgram()
    System->>System: print("Hello World")
    System-->>User: affiche "Hello World"

```