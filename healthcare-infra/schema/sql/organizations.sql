CREATE TABLE IF NOT EXISTS organizations (
    id          UUID PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    address     VARCHAR(255) NOT NULL,
    city        VARCHAR(100) NOT NULL,
    state       VARCHAR(50)  NOT NULL,
    zip         VARCHAR(20)  NOT NULL,
    phone       VARCHAR(50)  NOT NULL,
    lat         DECIMAL(10,6),           -- system derived, not user input
    lon         DECIMAL(10,6),           -- system derived, not user input
    revenue     DECIMAL(12,2),           -- internal metric, nullable
    utilization INTEGER                 -- internal metric, nullable

);

CREATE INDEX IF NOT EXISTS idx_organizations_name ON organizations(name);