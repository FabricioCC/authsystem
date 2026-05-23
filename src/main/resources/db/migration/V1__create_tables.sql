-- V1__create_tables.sql
-- Initial schema for AuthSystem RBAC entities

-- Sequences (explicit for Hibernate JPA compatibility)
CREATE SEQUENCE IF NOT EXISTS roles_seq START 1 INCREMENT 1;
CREATE SEQUENCE IF NOT EXISTS permissions_seq START 1 INCREMENT 1;
CREATE SEQUENCE IF NOT EXISTS audit_logs_seq START 1 INCREMENT 1;

-- Roles
CREATE TABLE IF NOT EXISTS roles (
  id BIGINT PRIMARY KEY DEFAULT nextval('roles_seq'),
  name VARCHAR(50) NOT NULL UNIQUE,
  description VARCHAR(255)
);

-- Permissions
CREATE TABLE IF NOT EXISTS permissions (
  id BIGINT PRIMARY KEY DEFAULT nextval('permissions_seq'),
  name VARCHAR(100) NOT NULL UNIQUE,
  description VARCHAR(255)
);

-- Users (UUID primary key)
CREATE TABLE IF NOT EXISTS users (
  id UUID PRIMARY KEY,
  email VARCHAR(100) NOT NULL UNIQUE,
  password_hash TEXT NOT NULL,
  name VARCHAR(255),
  enabled BOOLEAN NOT NULL DEFAULT true,
  created_at DATE NOT NULL DEFAULT CURRENT_DATE
);

-- Audit logs
CREATE TABLE IF NOT EXISTS audit_logs (
  id BIGINT PRIMARY KEY DEFAULT nextval('audit_logs_seq'),
  user_id UUID NOT NULL,
  action VARCHAR(50) NOT NULL,
  ip_address VARCHAR(45),
  success BOOLEAN NOT NULL,
  created_at DATE NOT NULL DEFAULT CURRENT_DATE,
  CONSTRAINT fk_audit_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Many-to-many: role_permissions
CREATE TABLE IF NOT EXISTS role_permissions (
  role_id BIGINT NOT NULL,
  permission_id BIGINT NOT NULL,
  PRIMARY KEY (role_id, permission_id),
  CONSTRAINT fk_rp_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
  CONSTRAINT fk_rp_permission FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE
);

-- Many-to-many: user_roles
CREATE TABLE IF NOT EXISTS user_roles (
  user_id UUID NOT NULL,
  role_id BIGINT NOT NULL,
  PRIMARY KEY (user_id, role_id),
  CONSTRAINT fk_ur_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT fk_ur_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- Optional indexes for common lookups
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_audit_user ON audit_logs(user_id);

