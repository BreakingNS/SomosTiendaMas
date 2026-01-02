-- Create product_draft table
CREATE TABLE IF NOT EXISTS product_draft (
    id BIGSERIAL PRIMARY KEY,
    owner_id VARCHAR(128) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'DRAFT',
    step INTEGER DEFAULT 1,
    data_json TEXT,
    images_json TEXT,
    meta_json TEXT,
    version BIGINT,
    committed_at TIMESTAMP,
    created_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_at TIMESTAMP,
    updated_by VARCHAR(255),
    deleted_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_product_draft_owner ON product_draft(owner_id);
CREATE INDEX IF NOT EXISTS idx_product_draft_status ON product_draft(status);
CREATE INDEX IF NOT EXISTS idx_product_draft_updated ON product_draft(updated_at);
