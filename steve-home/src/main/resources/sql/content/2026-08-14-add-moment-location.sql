-- 为历史朋友圈保留发布时携带的原始地点。
SET @location_column_exists = (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'moment'
      AND column_name = 'location'
);

SET @add_location_sql = IF(
    @location_column_exists = 0,
    'ALTER TABLE moment ADD COLUMN location VARCHAR(200) NULL COMMENT ''朋友圈发布时携带的原始地点'' AFTER media_url',
    'SELECT 1'
);

PREPARE add_location_statement FROM @add_location_sql;
EXECUTE add_location_statement;
DEALLOCATE PREPARE add_location_statement;
