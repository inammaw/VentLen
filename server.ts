import express from "express";
import path from "path";
import dotenv from "dotenv";
import crypto from "crypto";
import { createServer as createViteServer } from "vite";
import { GoogleGenAI } from "@google/genai";
import pg from "pg";

dotenv.config();

const { Client } = pg;

function getPgClient() {
  return new Client({
    host: process.env.PG_HOST || process.env.AIVEN_PG_HOST || "pg-24c49dac-gowthamgowri73-1585.h.aivencloud.com",
    port: parseInt(process.env.PG_PORT || process.env.AIVEN_PG_PORT || "28072", 10),
    database: process.env.PG_DATABASE || process.env.AIVEN_PG_DB || "defaultdb",
    user: process.env.PG_USER || process.env.AIVEN_PG_USER || "avnadmin",
    password: process.env.PG_PASSWORD || process.env.AIVEN_PG_PASSWORD || "",
    ssl: { rejectUnauthorized: false }
  });
}

function hashPassword(password: string): string {
  return crypto.createHash("sha256").update(password).digest("hex");
}

let aiClient: GoogleGenAI | null = null;
function getGemini(): GoogleGenAI {
  if (!aiClient) {
    const key = process.env.GEMINI_API_KEY || "";
    aiClient = new GoogleGenAI({ apiKey: key });
  }
  return aiClient;
}

async function startServer() {
  const app = express();
  const PORT = process.env.PORT ? parseInt(process.env.PORT, 10) : 3000;

  app.use(express.json({ limit: "5mb" }));

  // Live Aiven PostgreSQL Status and Row Counts
  app.get("/api/db/status", async (req, res) => {
    const client = getPgClient();
    try {
      await client.connect();
      const userRes = await client.query("SELECT count(*) FROM users;");
      const ventureRes = await client.query("SELECT count(*) FROM ventures;");
      const expenseRes = await client.query("SELECT count(*) FROM expenses;");
      const sampleVentures = await client.query("SELECT id, startup_name, decision_tier, overall_score, post_money_val, runway_months FROM ventures ORDER BY id ASC LIMIT 5;");
      
      res.json({
        connected: true,
        host: "pg-24c49dac-gowthamgowri73-1585.h.aivencloud.com",
        port: 28072,
        database: "defaultdb",
        counts: {
          users: parseInt(userRes.rows[0].count, 10),
          ventures: parseInt(ventureRes.rows[0].count, 10),
          expenses: parseInt(expenseRes.rows[0].count, 10)
        },
        ventures: sampleVentures.rows
      });
    } catch (err: any) {
      res.json({
        connected: false,
        error: err.message,
        host: "pg-24c49dac-gowthamgowri73-1585.h.aivencloud.com",
        port: 28072
      });
    } finally {
      await client.end().catch(() => {});
    }
  });

  // User Authentication Endpoints (Aiven PostgreSQL 'users' table)
  app.post("/api/auth/login", async (req, res) => {
    const { usernameOrEmail, password } = req.body;
    if (!usernameOrEmail || !password) {
      return res.status(400).json({ success: false, error: "Username/email and password are required." });
    }

    const client = getPgClient();
    try {
      await client.connect();
      const query = `
        SELECT id, username, email, full_name, password_hash, created_at 
        FROM users 
        WHERE LOWER(username) = LOWER($1) OR LOWER(email) = LOWER($1)
        LIMIT 1;
      `;
      const result = await client.query(query, [usernameOrEmail.trim()]);
      if (result.rows.length === 0) {
        return res.status(401).json({ success: false, error: "No user found with that username or email." });
      }

      const user = result.rows[0];
      const passwordHash = hashPassword(password);
      if (user.password_hash !== passwordHash) {
        return res.status(401).json({ success: false, error: "Incorrect password. Please verify and retry." });
      }

      res.json({
        success: true,
        user: {
          id: user.id,
          username: user.username,
          email: user.email,
          fullName: user.full_name,
          createdAt: user.created_at
        },
        message: `Welcome back, ${user.full_name}!`
      });
    } catch (err: any) {
      console.error("Login error:", err);
      res.status(500).json({ success: false, error: err.message || "Failed to log in" });
    } finally {
      await client.end().catch(() => {});
    }
  });

  app.post("/api/auth/signup", async (req, res) => {
    const { username, email, password, fullName } = req.body;
    if (!username || !email || !password || !fullName) {
      return res.status(400).json({ success: false, error: "All fields (Full Name, Username, Email, Password) are required." });
    }

    const cleanUsername = username.trim().toLowerCase().replace(/[^a-z0-9_]/g, '');
    const cleanEmail = email.trim().toLowerCase();
    const cleanFullName = fullName.trim();

    if (cleanUsername.length < 3) {
      return res.status(400).json({ success: false, error: "Username must be at least 3 alphanumeric characters." });
    }
    if (!cleanEmail.includes("@") || !cleanEmail.includes(".")) {
      return res.status(400).json({ success: false, error: "Please provide a valid email address." });
    }
    if (password.length < 6) {
      return res.status(400).json({ success: false, error: "Password must be at least 6 characters long." });
    }

    const client = getPgClient();
    try {
      await client.connect();

      // Check for existing username or email
      const checkQuery = `
        SELECT id, username, email FROM users 
        WHERE LOWER(username) = $1 OR LOWER(email) = $2
        LIMIT 1;
      `;
      const checkRes = await client.query(checkQuery, [cleanUsername, cleanEmail]);
      if (checkRes.rows.length > 0) {
        const existing = checkRes.rows[0];
        if (existing.username.toLowerCase() === cleanUsername) {
          return res.status(409).json({ success: false, error: `Username "${cleanUsername}" is already taken. Please choose another.` });
        }
        return res.status(409).json({ success: false, error: `Email "${cleanEmail}" is already registered. Please log in instead.` });
      }

      const passwordHash = hashPassword(password);
      const insertQuery = `
        INSERT INTO users (username, email, password_hash, full_name)
        VALUES ($1, $2, $3, $4)
        RETURNING id, username, email, full_name, created_at;
      `;
      const insertRes = await client.query(insertQuery, [cleanUsername, cleanEmail, passwordHash, cleanFullName]);
      const newUser = insertRes.rows[0];

      res.status(201).json({
        success: true,
        user: {
          id: newUser.id,
          username: newUser.username,
          email: newUser.email,
          fullName: newUser.full_name,
          createdAt: newUser.created_at
        },
        message: `Account created successfully! Welcome to VentureLens, ${newUser.full_name}.`
      });
    } catch (err: any) {
      console.error("Signup error:", err);
      res.status(500).json({ success: false, error: err.message || "Failed to create user account" });
    } finally {
      await client.end().catch(() => {});
    }
  });

  app.get("/api/auth/users", async (req, res) => {
    const client = getPgClient();
    try {
      await client.connect();
      const result = await client.query(`
        SELECT id, username, email, full_name, created_at 
        FROM users 
        ORDER BY id ASC;
      `);
      res.json({
        success: true,
        users: result.rows.map(u => ({
          id: u.id,
          username: u.username,
          email: u.email,
          fullName: u.full_name,
          createdAt: u.created_at
        }))
      });
    } catch (err: any) {
      res.status(500).json({ success: false, error: err.message });
    } finally {
      await client.end().catch(() => {});
    }
  });

  // Status endpoint
  app.get("/api/ai/status", (req, res) => {
    const hasKey = Boolean(process.env.GEMINI_API_KEY);
    res.json({
      status: "ok",
      aiProvider: "Google Gemini",
      model: "gemini-3.8-flash",
      configured: hasKey
    });
  });

  // AI Startup Evaluation Endpoint
  app.post("/api/ai/analyze", async (req, res) => {
    try {
      const {
        startupName,
        targetCustomer,
        problemStatement,
        proposedSolution,
        businessModel,
        preMoneyValuation,
        investmentAmount,
        currentCashBalance
      } = req.body;

      const ai = getGemini();

      const prompt = `You are a venture capital investment committee partner and veteran startup operator.
Evaluate the following early-stage startup proposal:

Startup Name: ${startupName || "Untitled Startup"}
Target Customer Profile: ${targetCustomer || "Not specified"}
Problem Statement: ${problemStatement || "Not specified"}
Proposed Solution & Moat: ${proposedSolution || "Not specified"}
Business Model: ${businessModel || "Not specified"}
Financial Ask: Pre-money ₹${Number(preMoneyValuation || 0).toLocaleString('en-IN')}, Round ask ₹${Number(investmentAmount || 0).toLocaleString('en-IN')}, Cash balance ₹${Number(currentCashBalance || 0).toLocaleString('en-IN')}

Conduct a deep analysis and return a valid JSON object matching this schema:
{
  "marketScore": <number between 0 and 100 based on TAM, urgency, customer willingness to pay>,
  "feasibilityScore": <number between 0 and 100 based on tech readiness, operational complexity>,
  "competitionScore": <number between 0 and 100 where higher means better defensibility and moat against incumbents>,
  "depthScore": <number between 0 and 100 based on clarity, specificity, and unit economics>,
  "overallScore": <weighted average composite score between 0 and 100>,
  "decisionTier": <"STRONG_GO" if score >= 80, "GO" if 65-79, "CAUTION" if 45-64, "NO_GO" if < 45>,
  "strengths": <string with 3-4 bullet points separated by " • ">,
  "weaknesses": <string with 2-3 bullet points separated by " • ">,
  "opportunities": <string with 2-3 bullet points separated by " • ">,
  "threats": <string with 2-3 bullet points separated by " • ">,
  "criticalRisks": <string describing top 1-2 execution and financial risks>,
  "aiVerdict": <paragraph (3-4 sentences) summarizing the investment committee's core verdict, market viability, and defensibility>,
  "strategicRecommendations": <array of 3 concise actionable next steps for the founder>
}

Provide realistic, discerning venture evaluation. Output strictly valid JSON with no markdown wrapping.`;

      const response = await ai.models.generateContent({
        model: "gemini-3.8-flash",
        contents: prompt,
        config: {
          responseMimeType: "application/json"
        }
      });

      const responseText = response.text || "{}";
      const result = JSON.parse(responseText);
      res.json({ success: true, analysis: result });
    } catch (err: any) {
      console.error("Gemini AI Analysis Error:", err);
      res.status(500).json({
        success: false,
        error: err.message || "Failed to analyze startup with Gemini AI"
      });
    }
  });

  // AI Comprehensive Investor Diligence Memo & Deck Reporting Endpoint
  app.post("/api/ai/report", async (req, res) => {
    try {
      const venture = req.body;
      const ai = getGemini();

      const prompt = `You are a Tier-1 venture capital principal writing an institutional Investment Diligence Memo and Pitch Deck narrative for a startup.

Startup Profile:
- Name: ${venture.startupName || "Startup"}
- Target Customer: ${venture.targetCustomer || "Target market"}
- Problem: ${venture.problemStatement || "Problem"}
- Solution & Moat: ${venture.proposedSolution || "Solution"}
- Business Model: ${venture.businessModel || "Business Model"}
- Overall Viability Score: ${venture.overallScore || 75}/100 (${venture.decisionTier || "GO"})
- Pre-Money Valuation: ₹${Number(venture.preMoneyValuation || 0).toLocaleString('en-IN')}
- Investment Ask: ₹${Number(venture.investmentAmount || 0).toLocaleString('en-IN')}
- Post-Money Valuation: ₹${Number(venture.postMoneyValuation || 0).toLocaleString('en-IN')}
- Survival Runway: ${venture.runwayMonths || 12} Months
- Monthly Burn: ₹${Number(venture.monthlyBurn || 0).toLocaleString('en-IN')}
- Founders Equity Retained: ${venture.founderEquity || 70}%

Generate a comprehensive diligence report and 10-slide investor presentation outline.
Return strictly a valid JSON object matching this schema:
{
  "executiveSummary": <string: 2-3 concise paragraphs summarizing the thesis, opportunity, and core metrics>,
  "marketOpportunity": <string: TAM/SAM analysis, tailwinds, and customer psychology>,
  "productMoat": <string: technical or operational advantages and defensibility against copycats>,
  "monetizationViability": <string: revenue streams, pricing power, and unit economics>,
  "financialAssessment": <string: analysis of valuation sanity, dilution impact, and runway runway sufficiency>,
  "riskMitigation": <string: key risks and proposed countermeasures>,
  "pitchDeckSlides": [
    {
      "slideNumber": 1,
      "title": "Title & Vision",
      "headline": <string>,
      "bulletPoints": [<string>, <string>, <string>]
    },
    ... (total 10 slides covering: 1. Title/Vision, 2. Problem/Pain, 3. Solution/Value Prop, 4. Market Size & Timing, 5. Business Model, 6. Traction/Roadmap, 7. Competitive Moat, 8. Cap Table & Financial Ask, 9. Use of Funds & Runway, 10. Team & Vision)
  ],
  "fullMarkdownReport": <string: complete, beautifully formatted Markdown document containing the memo and all 10 slide outlines formatted with headers, bold key phrases, and tables where applicable>
}

Output strictly valid JSON with no markdown block fences.`;

      const response = await ai.models.generateContent({
        model: "gemini-3.8-flash",
        contents: prompt,
        config: {
          responseMimeType: "application/json"
        }
      });

      const responseText = response.text || "{}";
      const result = JSON.parse(responseText);
      res.json({ success: true, report: result });
    } catch (err: any) {
      console.error("Gemini AI Report Error:", err);
      res.status(500).json({
        success: false,
        error: err.message || "Failed to generate AI report with Gemini AI"
      });
    }
  });

  // Vite middleware for development
  if (process.env.NODE_ENV !== "production") {
    const vite = await createViteServer({
      server: { middlewareMode: true },
      appType: "spa",
    });
    app.use(vite.middlewares);
  } else {
    const distPath = path.join(process.cwd(), "dist");
    app.use(express.static(distPath));
    app.get("*", (req, res) => {
      res.sendFile(path.join(distPath, "index.html"));
    });
  }

  app.listen(PORT, "0.0.0.0", () => {
    console.log(`VentureLens Server running on http://0.0.0.0:${PORT}`);
  });
}

startServer();
