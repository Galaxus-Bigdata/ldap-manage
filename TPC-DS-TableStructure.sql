CREATE TABLE store_returns (
    sr_returned_date_sk INT,
    sr_customer_sk INT,
    sr_store_sk INT,
    sr_return_amt DECIMAL(7, 2)
);

CREATE TABLE date_dim (
    d_date_sk INT,
    d_year INT
);

CREATE TABLE store (
    s_store_sk INT,
    s_state STRING
);

CREATE TABLE customer (
    c_customer_sk INT,
    c_customer_id STRING
);
