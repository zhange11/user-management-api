CREATE SEQUENCE IF NOT EXISTS customer_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE customers (
                           id                INTEGER PRIMARY KEY DEFAULT nextval('customer_seq'),
                           username          VARCHAR(255) NOT NULL,
                           email             VARCHAR(255) NOT NULL UNIQUE,
                           age               INTEGER NOT NULL,
                           gender            VARCHAR(50),
                           password          VARCHAR(255) NOT NULL,
                           profile_image_id  VARCHAR(255) UNIQUE,
                           role              VARCHAR(50)
);