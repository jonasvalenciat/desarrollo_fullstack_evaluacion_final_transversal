CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(255),
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE cart_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_name VARCHAR(100) NOT NULL,
    price DOUBLE NOT NULL,
    quantity INT NOT NULL,
    coupon_code VARCHAR(50),
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    user_id BIGINT,
    CONSTRAINT fk_cart_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE cart_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cart_item_id BIGINT NOT NULL,
    previous_status VARCHAR(20),
    new_status VARCHAR(20),
    previous_user_email VARCHAR(150),
    new_user_email VARCHAR(150),
    changed_at TIMESTAMP NOT NULL,
    comment VARCHAR(255),
    CONSTRAINT fk_history_cart FOREIGN KEY (cart_item_id) REFERENCES cart_items(id)
);
