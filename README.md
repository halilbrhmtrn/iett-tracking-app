# IETT Bus Tracking System
This is a technical assignment for a job interview.

## Design Phase Notes

**Initial Approach**: started with a basic Spring Boot application structure with essential dependencies for web, JPA, and PostgreSQL integration.

**SOAP Integration Challenges**: Initially, attempted to use traditional SOAP client implementations with JAX-WS/Jakarta EE, but faced compatibility issues with the IETT services that did not provide proper WSDL for client generation. Used Nodejs script to
check if the WSDL is valid and problem solely about my lack of experience with Java then used RestTemplate to make raw SOAP requests and process the XML responses manually.

**Data Retrieval Logic**: 
   - Implemented a caching mechanism where data is fetched from SOAP services only if it's older than 1 hour
   - Created a data retrieval logging system to track successful/failed attempts

### Frontend Development Process

**Page Structure**:
   - Home page with navigation to Garages and Buses pages
   - Garages page with search functionality and table/card display
   - Buses page with search functionality and nearest garage information

**API Integration**: Created service modules to interact with backend APIs, with proper error handling and loading states.

**State Management**: Used React's built-in hooks for state management, avoiding extra libraries to keep the app lightweight.

## How to Run the Project

### Prerequisites

- Docker and Docker Compose installed on your system

### Installation

1. Clone the repository:
   ```bash
   git clone <repository-url> or extract the zip file
   cd iett-bus-tracking-system
   ```

2. Start the application using Docker Compose:
   ```bash
   docker-compose up
   ```

This will start three containers:
- PostgreSQL database on port 5432
- Backend Spring Boot application on port 8080
- Frontend React application on port 3000

### Accessing the Application

- **Frontend UI**: http://localhost:3000
- **Swagger API Documentation**: http://localhost:8080/swagger-ui/index.html
- **OpenAPI JSON**: http://localhost:8080/api-docs
- **API**: http://localhost:8080