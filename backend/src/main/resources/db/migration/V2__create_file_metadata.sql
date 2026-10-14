CREATE SEQUENCE IF NOT EXISTS file_metadata_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE file_metadata (
    id           BIGINT PRIMARY KEY DEFAULT nextval('file_metadata_seq'),
    file_id      VARCHAR(255) NOT NULL UNIQUE,
    customer_id  INTEGER NOT NULL REFERENCES customers(id),
    file_name    VARCHAR(255) NOT NULL,
    content_type VARCHAR(255),
    status       VARCHAR(50) NOT NULL,
    created_at   TIMESTAMP NOT NULL DEFAULT now(),
    updated_at   TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_file_metadata_customer_id ON file_metadata(customer_id);