import pg from 'pg';
import fs from 'fs';
import path from 'path';
import 'dotenv/config';

const { Client } = pg;

async function runSeed() {
  console.log("Connecting to Aiven Cloud PostgreSQL...");
  const client = new Client({
    host: process.env.PG_HOST || process.env.AIVEN_PG_HOST || "pg-24c49dac-gowthamgowri73-1585.h.aivencloud.com",
    port: parseInt(process.env.PG_PORT || process.env.AIVEN_PG_PORT || "28072", 10),
    database: process.env.PG_DATABASE || process.env.AIVEN_PG_DB || "defaultdb",
    user: process.env.PG_USER || process.env.AIVEN_PG_USER || "avnadmin",
    password: process.env.PG_PASSWORD || process.env.AIVEN_PG_PASSWORD || "",
    ssl: {
      rejectUnauthorized: false
    }
  });

  try {
    await client.connect();
    console.log("[OK] Successfully connected to Aiven PostgreSQL cloud instance!");

    const sqlScript = fs.readFileSync(path.resolve('./schema_postgres.sql'), 'utf-8');
    console.log("Executing schema_postgres.sql statements...");

    await client.query(sqlScript);
    console.log("[OK] Schema created and initial seed data inserted successfully!\n");

    // Verification queries
    console.log("--- TABLE: users ---");
    const usersRes = await client.query("SELECT id, username, email, full_name FROM users;");
    console.table(usersRes.rows);

    console.log("--- TABLE: ventures ---");
    const venturesRes = await client.query("SELECT id, startup_name, decision_tier, overall_score, post_money_val, runway_months FROM ventures;");
    console.table(venturesRes.rows);

    console.log("--- TABLE: expenses ---");
    const expensesRes = await client.query("SELECT id, venture_id, category, description, amount, is_revenue, month_year FROM expenses;");
    console.table(expensesRes.rows);

    console.log("All tables populated with data in Aiven PostgreSQL defaultdb.");

  } catch (err) {
    console.error("Database seed error:", err);
    process.exit(1);
  } finally {
    await client.end();
    console.log("Connection closed.");
  }
}

runSeed();
