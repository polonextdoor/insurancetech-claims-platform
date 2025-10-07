# InsuranceTech Claims Analytics Dashboard

## Overview
A modern, full-stack insurance claims processing platform featuring automated risk assessment, fraud detection, and real-time analytics. Built to demonstrate enterprise-level software engineering capabilities with a focus on the insurance industry.

## Features
- **Claims Processing Workflow**: End-to-end claim lifecycle management
- **Risk Assessment Engine**: Automated claim risk scoring and evaluation
- **Fraud Detection System**: Pattern recognition and alert generation
- **Customer Portal**: Self-service claim submission and tracking
- **Analytics Dashboard**: Business intelligence and reporting
- **Document Management**: Secure file upload and storage

## Technology Stack

### Backend
- **Framework**: Java Spring Boot 3.2
- **Database**: PostgreSQL 15
- **Cache**: Redis
- **Security**: Spring Security with JWT
- **Build Tool**: Maven
- **Testing**: JUnit 5, Mockito

### Frontend
- **Framework**: React 18 with TypeScript
- **State Management**: React Context API
- **Routing**: React Router v6
- **UI Components**: Custom components with Tailwind CSS
- **Data Visualization**: Chart.js
- **HTTP Client**: Axios

### Cloud Infrastructure (AWS)
- **Compute**: EC2 t3.micro
- **Database**: RDS PostgreSQL t3.micro
- **Storage**: S3
- **Monitoring**: CloudWatch
- **CI/CD**: GitHub Actions

## Architecture
```
┌─────────────────┐
│  React Frontend │
└────────┬────────┘
         │ HTTPS
         ▼
┌─────────────────┐
│  Spring Boot    │
│  REST API       │
└────────┬────────┘
         │
    ┌────┴────┐
    ▼         ▼
┌────────┐ ┌────────┐
│ PostgreSQL│ │  Redis │
└────────┘ └────────┘
```

## Getting Started

### Prerequisites
- Java 17 or higher
- Node.js 18 or higher
- PostgreSQL 15
- Maven 3.8+
- AWS Account (for deployment)

### Local Development Setup

#### Backend Setup
```bash
cd backend
mvn clean install
mvn spring-boot:run
```

Backend will run on `http://localhost:8080`

#### Frontend Setup
```bash
cd frontend
npm install
npm start
```

Frontend will run on `http://localhost:3000`

### Environment Variables
Create `.env` files in both backend and frontend directories:

**Backend (.env)**
```
DB_URL=jdbc:postgresql://localhost:5432/insurancetech
DB_USERNAME=your_username
DB_PASSWORD=your_password
JWT_SECRET=your_jwt_secret
AWS_ACCESS_KEY_ID=your_aws_key
AWS_SECRET_ACCESS_KEY=your_aws_secret
AWS_S3_BUCKET=your_bucket_name
```

**Frontend (.env)**
```
REACT_APP_API_URL=http://localhost:8080/api
```

## Project Structure
```
insurancetech-claims-platform/
├── backend/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/insurancetech/
│   │   │   │   ├── config/
│   │   │   │   ├── controller/
│   │   │   │   ├── model/
│   │   │   │   ├── repository/
│   │   │   │   ├── service/
│   │   │   │   └── security/
│   │   │   └── resources/
│   │   └── test/
│   └── pom.xml
├── frontend/
│   ├── public/
│   ├── src/
│   │   ├── components/
│   │   ├── pages/
│   │   ├── services/
│   │   ├── context/
│   │   └── utils/
│   └── package.json
├── docs/
│   ├── architecture.md
│   ├── api-documentation.md
│   └── deployment-guide.md
└── infrastructure/
    └── aws/
```

## Development Roadmap

### Phase 1: Foundation (Weeks 1-2) ✓ In Progress
- [x] Project setup and architecture
- [ ] Database schema design
- [ ] Authentication system
- [ ] Basic CRUD operations

### Phase 2: Core Features (Weeks 3-4)
- [ ] Claims submission workflow
- [ ] Risk assessment engine
- [ ] File upload system
- [ ] Status tracking

### Phase 3: Advanced Features (Weeks 5-6)
- [ ] Fraud detection algorithms
- [ ] Analytics dashboard
- [ ] Reporting system
- [ ] Email notifications

### Phase 4: Production (Weeks 7-8)
- [ ] AWS deployment
- [ ] CI/CD pipeline
- [ ] Performance optimization
- [ ] Security hardening

## API Documentation
API documentation available at `http://localhost:8080/swagger-ui.html` when running locally.

## Testing
```bash
# Backend tests
cd backend
mvn test

# Frontend tests
cd frontend
npm test
```

## Deployment
Detailed deployment instructions in `docs/deployment-guide.md`

## Contributing
This is a portfolio project, but feedback and suggestions are welcome!

## License
MIT License

## Contact
Marcos Santiago
marcos.jj.santiago@gmail.com
https://www.linkedin.com/in/marcos-santiago-cs/
[portfolio in prog]

## Acknowledgments
Built as a demonstration project to showcase modern software engineering practices and insurance domain knowledge.
