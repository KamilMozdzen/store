CREATE TABLE app_users(
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY ,
  email VARCHAR(255) NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  first_name VARCHAR(255) NOT NULL,
  last_name VARCHAR(255) NOT NULL,
  role VARCHAR(30) NOT NULL,
  created_at TIMESTAMP(6) NOT NULL,

  CONSTRAINT uk_app_users_email UNIQUE (email)
);