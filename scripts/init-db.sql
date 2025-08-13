-- Initialize Chimera database with extensions and basic setup
-- This runs automatically when PostgreSQL container starts

-- Create required extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_stat_statements";

-- Create basic schemas for organized data
CREATE SCHEMA IF NOT EXISTS market_data;
CREATE SCHEMA IF NOT EXISTS fund_data; 
CREATE SCHEMA IF NOT EXISTS news_data;
CREATE SCHEMA IF NOT EXISTS features;
CREATE SCHEMA IF NOT EXISTS rankings;
CREATE SCHEMA IF NOT EXISTS system;

-- Create basic tables for immediate development
CREATE TABLE IF NOT EXISTS system.health_check (
    id SERIAL PRIMARY KEY,
    service_name VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL,
    last_check TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    details JSONB
);

-- Insert initial health check record
INSERT INTO system.health_check (service_name, status, details) 
VALUES ('database', 'UP', '{"version": "16", "extensions": ["uuid-ossp", "pg_stat_statements"]}')
ON CONFLICT DO NOTHING;

-- Grant permissions
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO chimera;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA market_data TO chimera;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA fund_data TO chimera;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA news_data TO chimera;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA features TO chimera;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA rankings TO chimera;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA system TO chimera;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO chimera;
GRANT USAGE ON ALL SCHEMAS TO chimera;