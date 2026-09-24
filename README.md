# VentureLens

> **Startup Operating System & Decision Intelligence Platform**

VentureLens is an institutional-grade decision support platform and startup operating system designed for founders, angel investors, and startup accelerators. It combines rigorous rule-based financial and semantic evaluation models with Google Gemini AI to analyze early-stage ventures, simulate equity dilution, forecast runway, and generate institutional diligence memos and pitch decks.

---

## Key Features

### 1. Multi-Vector Startup Evaluation Engine
- **Market & TAM Analysis**: Quantifies Total Addressable Market urgency, customer segment specificity, and willingness to pay.
- **Technical & Operational Feasibility**: Evaluates architecture readiness, operational complexity, and delivery risks.
- **Competitive Moat & Defensibility**: Analyzes network effects, high switching costs, systems of record, and proprietary algorithms.
- **Unit Economics & Margin Profile**: Grades business model viability across SaaS recurring revenues, take-rate models, or capital-intensive models.
- **Automated Decision Tiering**: Classifies proposals into actionable tiers: `STRONG_GO`, `GO`, `CAUTION`, and `PIVOT`.

### 2. Gemini AI Diligence & Decision Intelligence
- **Institutional AI Review**: Powered by Google Gemini (`gemini-3.8-flash`) acting as an experienced venture capital investment committee partner.
- **Automated SWOT Analysis**: Generates structured strengths, weaknesses, opportunities, and threats tailored to the venture's target market and business model.
- **Critical Risk Analysis**: Pinpoints top execution and financial failure modes with actionable strategic recommendations.

### 3. CapTable Simulator & Valuation Modeling
- **Round Modeling**: Interactive modeling of pre-money valuation, investment ask, and calculated post-money valuation.
- **Dilution Forecaster**: Dynamically computes founder equity retention, employee stock ownership pool (ESOP), and new investor equity shares.

### 4. BurnWatch & Runway Forecaster
- **Runway Health Monitoring**: Computes remaining survival runway in months based on current cash balance and monthly burn rate.
- **Zero-Cash Date Projections**: Instant visibility into capital exhaustion dates and runway safety buffers.
- **Expense Categorization**: Track burn breakdown across salaries, hosting/cloud infrastructure, marketing, and operations.

### 5. Institutional Diligence Memo & 10-Slide Pitch Deck Generator
- **Comprehensive Diligence Memos**: Produces institutional investment memos covering market opportunity, product defensibility, and financial sanity.
- **10-Slide Pitch Deck Narrative**:
  1. Title & Vision
  2. Problem & Market Pain
  3. Solution & Value Proposition
  4. Market Sizing (TAM/SAM/SOM) & Timing
  5. Business Model & Monetization
  6. Traction & Product Roadmap
  7. Competitive Moats & Defensibility
  8. Cap Table & Financial Ask
  9. Use of Funds & Runway Trajectory
  10. Team & Long-Term Execution
- **Markdown Export**: Direct copy or download of full reports and slide decks in clean Markdown.

### 6. Cloud Persistence & User Accounts
- **Account Management**: Secure user registration, authentication, and session handling.
- **Persistent Storage**: Full support for PostgreSQL and MySQL schemas for storing users, ventures, evaluations, and financial expense records.

---

## Technology Stack

### Web Application & Backend
- **Frontend**: React 19, Tailwind CSS v4, Lucide Icons, Vite
- **Backend API**: Node.js, Express, TypeScript (`tsx`)
- **AI Integration**: Google Gen AI SDK (`@google/genai`) with `gemini-3.8-flash`
- **Database**: PostgreSQL / MySQL (`pg`, JDBC connectors)
- **Export & Utilities**: JSZip, Crypto, Markdown formatters

### Desktop Client (Java)
- **Language**: Java SE 17+
- **Build System**: Apache Maven (`pom.xml`)
- **UI Framework**: Java Swing / AWT architecture
- **Database Layer**: Official MySQL JDBC Driver (`com.mysql:mysql-connector-j`)

---

## Project Structure

```
├── .env.example              # Sample environment variables configuration
├── index.html                # Application entry HTML
├── metadata.json             # AI Studio applet metadata & capabilities
├── package.json              # Node.js dependencies and scripts
├── pom.xml                   # Maven project configuration for Java desktop client
├── server.ts                 # Express full-stack server & Gemini AI endpoints
├── vite.config.js            # Vite configuration with React & Tailwind plugins
├── schema.sql                # MySQL relational database schema
├── schema_postgres.sql       # PostgreSQL relational database schema
│
├── src/                      # Web Application
│   ├── index.css             # Global Tailwind CSS imports
│   ├── main.js               # Core VentureLens application logic & dashboard UI
│   └── ...
│
└── com/                      # Java Desktop Implementation
    └── venturelens/
        ├── Main.java         # Desktop application entry point
        ├── analysis/         # Algorithmic evaluator & financial calculation engines
        ├── dao/              # Data Access Objects (UserDAO, VentureDAO, ExpenseDAO)
        ├── model/            # Domain models (User, Venture, Expense, AnalysisResult)
        ├── ui/               # Desktop UI frames, panels, and custom visual components
        └── utils/            # Database connection pool & security utilities
```

---

## Getting Started

### Prerequisites
- **Node.js**: v18.0.0 or higher
- **npm** or **bun**
- *(Optional for Java client)*: **JDK 17+** and **Apache Maven 3.8+**

### 1. Installation

Clone the repository and install dependencies:

```bash
npm install
```

### 2. Environment Configuration

Create a `.env` file from `.env.example`:

```bash
cp .env.example .env
```

Configure your environment variables:

```env
# Google Gemini API Key for venture analysis and diligence memos
GEMINI_API_KEY="your-gemini-api-key"

# Port (defaults to 3000)
PORT=3000
```

### 3. Running the Development Server

Start the full-stack development server (Express + Vite with hot module replacement):

```bash
npm run dev
```

Open your browser and navigate to:
```
http://localhost:3000
```

### 4. Production Build

Build the client assets and server bundle:

```bash
npm run build
npm start
```

### 5. Running the Java Desktop Client (Optional)

To compile and launch the desktop client:

```bash
mvn clean package
java -jar target/venturelens-desktop-1.0.0.jar
```

---

## API Reference

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/ai/status` | Returns Gemini AI provider status and model configuration |
| `POST` | `/api/ai/analyze` | Generates multi-vector evaluation scores, SWOT, and decision tiering |
| `POST` | `/api/ai/report` | Generates institutional investment memo and 10-slide deck narrative |
| `GET` | `/api/db/status` | Checks database connection health and live record counts |
| `POST` | `/api/auth/signup` | Registers a new user account |
| `POST` | `/api/auth/login` | Authenticates existing user credentials |
| `GET` | `/api/auth/users` | Lists registered user profiles |

---

## Database Schemas

Relational schemas are provided for both **PostgreSQL** (`schema_postgres.sql`) and **MySQL** (`schema.sql`):

- `users`: User identity, authentication hashes, and timestamps.
- `ventures`: Comprehensive venture profiles, inputs (TAM, solution, model), evaluation scores, decision tiers, and CapTable snapshots.
- `expenses`: Cash outflows and operational expenditures linked to venture runway calculations.

---

## License

This project is licensed under the MIT License.
