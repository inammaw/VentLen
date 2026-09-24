import pg from 'pg';
import 'dotenv/config';

const { Client } = pg;

async function clearAivenData() {
  console.log("Connecting to Aiven Cloud PostgreSQL to clear rows...");
  const client = new Client({
    host: process.env.PG_HOST || process.env.AIVEN_PG_HOST || "pg-24c49dac-gowthamgowri73-1585.h.aivencloud.com",
    port: parseInt(process.env.PG_PORT || process.env.AIVEN_PG_PORT || "28072", 10),
    database: process.env.PG_DATABASE || process.env.AIVEN_PG_DB || "defaultdb",
    user: process.env.PG_USER || process.env.AIVEN_PG_USER || "avnadmin",
    password: process.env.PG_PASSWORD || process.env.AIVEN_PG_PASSWORD || "",
    ssl: { rejectUnauthorized: false }
  });

  try {
    await client.connect();
    console.log("Connected to Aiven PostgreSQL.");

    // Truncate tables to remove default seed rows
    await client.query("TRUNCATE TABLE expenses, ventures CASCADE;");
    console.log("[OK] Truncated tables: expenses, ventures.");

    // Check counts
    const vRes = await client.query("SELECT count(*) FROM ventures;");
    const eRes = await client.query("SELECT count(*) FROM expenses;");
    console.log(`Remaining ventures count in Aiven: ${vRes.rows[0].count}`);
    console.log(`Remaining expenses count in Aiven: ${eRes.rows[0].count}`);

    // Reset sequences
    await client.query("ALTER SEQUENCE ventures_id_seq RESTART WITH 1;");
    await client.query("ALTER SEQUENCE expenses_id_seq RESTART WITH 1;");
    console.log("[OK] Sequences reset to 1.");

  } catch (err) {
    console.error("Error clearing Aiven database:", err);
    process.exit(1);
  } finally {
    await client.end();
    console.log("Aiven Cloud database cleaned.");
  }
}

clearAivenData();
