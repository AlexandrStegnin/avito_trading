CREATE TABLE trading_sber
(
    id                   INT8 PRIMARY KEY,
    lot                  VARCHAR(250) NOT NULL,
    description          VARCHAR(8000)  DEFAULT NULL,
    address              VARCHAR(100)    DEFAULT NULL,
    trading_number       VARCHAR(50)   DEFAULT NULL,
    efrsb_id             VARCHAR(50)    DEFAULT NULL,
    auction_step         VARCHAR(20)    DEFAULT NULL,
    deposit_amount       VARCHAR(20)   DEFAULT NULL,
    trading_time         VARCHAR(20)    DEFAULT NULL,
    accept_requests_date VARCHAR(30)    DEFAULT NULL,
    url                  VARCHAR(1000)  DEFAULT NULL,
    price                DECIMAL(20, 2) DEFAULT NULL,
    seller               VARCHAR(20)    DEFAULT NULL
)
