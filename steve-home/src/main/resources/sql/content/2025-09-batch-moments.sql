-- 朋友圈归档：2025-09-10 至 2025-09-30
-- 仅发布原图；Snipaste 朋友圈截图只用于核对文案、时间和图片顺序。
-- 可重复执行：动态按“原文案 + 原发布时间”判重，图片按“动态 + 顺序”判重。
START TRANSACTION;

CREATE TEMPORARY TABLE archived_moment_seed (
    content     TEXT     CHARACTER SET utf8mb4 NOT NULL,
    created_at  DATETIME                       NOT NULL,
    image_count INT                            NOT NULL,
    PRIMARY KEY (created_at)
);

INSERT INTO archived_moment_seed (content, created_at, image_count) VALUES
    (CONVERT(0xE9BB84E59F94E5869BE6A0A1E4B880E697A5E6B8B8 USING utf8mb4), '2025-09-10 21:56:00', 8),
    (CONVERT(0xE5B9BFE5B7A52BE78EAFE5B29BE29C85 USING utf8mb4), '2025-09-11 19:05:00', 6),
    (CONVERT(0xE8819AE698AFE4B880E59BA2E781ABEFBC8CE695A3E698AFE6BBA1E5A4A9E6989F0AE684BFE58F8BE8B08AE79A84E781ABE7A78DE6B0B8E4B88DE78684E781AD USING utf8mb4), '2025-09-13 22:35:00', 3),
    (CONVERT(0xE58D8EE5869C2BE4B8ADE5A4A7E29C85 USING utf8mb4), '2025-09-16 19:08:00', 9),
    (CONVERT(0x4461696C792064696574F09F8D9A USING utf8mb4), '2025-09-19 22:51:00', 9),
    (CONVERT(0xE58D8EE5B7A5E29C85 USING utf8mb4), '2025-09-21 21:24:00', 9),
    (CONVERT(0xE6818DE88BA5E698A8E697A5 USING utf8mb4), '2025-09-29 19:30:00', 1),
    (CONVERT(0x546865206669727374206D6F6E74682061732066726573686D616E202E USING utf8mb4), '2025-09-30 23:17:00', 9);

INSERT INTO moment (content, media_type, created_at, updated_at)
SELECT seed.content, 'image', seed.created_at, seed.created_at
FROM archived_moment_seed seed
WHERE NOT EXISTS (
    SELECT 1
    FROM moment existing
    WHERE existing.content = seed.content
      AND existing.created_at = seed.created_at
);

CREATE TEMPORARY TABLE archived_image_number (
    number_value INT NOT NULL PRIMARY KEY
);

INSERT INTO archived_image_number (number_value)
VALUES (0), (1), (2), (3), (4), (5), (6), (7), (8);

INSERT INTO moment_image (moment_id, url, sort)
SELECT moment.id,
       CONCAT(
           'https://cdn.jsdelivr.net/gh/stephen688/blog-images@main/images/steve-home-',
           DATE_FORMAT(seed.created_at, '%Y-%m-%d'),
           '-',
           LPAD(image_number.number_value + 1, 2, '0'),
           '.jpg'
       ),
       image_number.number_value
FROM archived_moment_seed seed
JOIN moment
  ON moment.content = seed.content
 AND moment.created_at = seed.created_at
JOIN archived_image_number image_number
  ON image_number.number_value < seed.image_count
LEFT JOIN moment_image existing
  ON existing.moment_id = moment.id
 AND existing.sort = image_number.number_value
WHERE existing.id IS NULL;

COMMIT;

DROP TEMPORARY TABLE archived_image_number;
DROP TEMPORARY TABLE archived_moment_seed;
