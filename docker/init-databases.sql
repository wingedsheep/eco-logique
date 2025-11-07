-- Create schemas for each bounded context
CREATE SCHEMA IF NOT EXISTS payment;
CREATE SCHEMA IF NOT EXISTS products;
CREATE SCHEMA IF NOT EXISTS shipping;
CREATE SCHEMA IF NOT EXISTS inventory;
CREATE SCHEMA IF NOT EXISTS users;

-- Grant permissions
GRANT ALL ON SCHEMA payment TO economique_app;
GRANT ALL ON SCHEMA products TO economique_app;
GRANT ALL ON SCHEMA shipping TO economique_app;
GRANT ALL ON SCHEMA inventory TO economique_app;
GRANT ALL ON SCHEMA users TO economique_app;

-- Grant usage on all tables in schemas
GRANT ALL ON ALL TABLES IN SCHEMA payment TO economique_app;
GRANT ALL ON ALL TABLES IN SCHEMA products TO economique_app;
GRANT ALL ON ALL TABLES IN SCHEMA shipping TO economique_app;
GRANT ALL ON ALL TABLES IN SCHEMA inventory TO economique_app;
GRANT ALL ON ALL TABLES IN SCHEMA users TO economique_app;

-- Grant usage on all sequences in schemas
GRANT ALL ON ALL SEQUENCES IN SCHEMA payment TO economique_app;
GRANT ALL ON ALL SEQUENCES IN SCHEMA products TO economique_app;
GRANT ALL ON ALL SEQUENCES IN SCHEMA shipping TO economique_app;
GRANT ALL ON ALL SEQUENCES IN SCHEMA inventory TO economique_app;
GRANT ALL ON ALL SEQUENCES IN SCHEMA users TO economique_app;