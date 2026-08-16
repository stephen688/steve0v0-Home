-- 朋友圈归档：2025-09-09 20:27
-- 可重复执行；以原始发布时间和文案共同判重。
START TRANSACTION;

INSERT INTO moment (content, media_type, created_at, updated_at)
SELECT CONVERT(0xE5ADA4E78BACE698AFE4B880E7A78DE781B5E9AD82E79A84E4BAABE58F97 USING utf8mb4),
       'image', '2025-09-09 20:27:00', '2025-09-09 20:27:00'
WHERE NOT EXISTS (
    SELECT 1
    FROM moment
    WHERE content = CONVERT(0xE5ADA4E78BACE698AFE4B880E7A78DE781B5E9AD82E79A84E4BAABE58F97 USING utf8mb4)
      AND created_at = '2025-09-09 20:27:00'
);

SET @archived_moment_id = (
    SELECT id
    FROM moment
    WHERE content = CONVERT(0xE5ADA4E78BACE698AFE4B880E7A78DE781B5E9AD82E79A84E4BAABE58F97 USING utf8mb4)
      AND created_at = '2025-09-09 20:27:00'
    ORDER BY id DESC
    LIMIT 1
);

INSERT INTO moment_image (moment_id, url, sort)
SELECT @archived_moment_id, source.url, source.sort
FROM (
    SELECT 'https://cdn.jsdelivr.net/gh/stephen688/blog-images@main/images/steve-home-2025-09-09-01.jpg' AS url, 0 AS sort
    UNION ALL SELECT 'https://cdn.jsdelivr.net/gh/stephen688/blog-images@main/images/steve-home-2025-09-09-02.jpg', 1
    UNION ALL SELECT 'https://cdn.jsdelivr.net/gh/stephen688/blog-images@main/images/steve-home-2025-09-09-03.jpg', 2
    UNION ALL SELECT 'https://cdn.jsdelivr.net/gh/stephen688/blog-images@main/images/steve-home-2025-09-09-04.jpg', 3
    UNION ALL SELECT 'https://cdn.jsdelivr.net/gh/stephen688/blog-images@main/images/steve-home-2025-09-09-05.jpg', 4
    UNION ALL SELECT 'https://cdn.jsdelivr.net/gh/stephen688/blog-images@main/images/steve-home-2025-09-09-06.jpg', 5
    UNION ALL SELECT 'https://cdn.jsdelivr.net/gh/stephen688/blog-images@main/images/steve-home-2025-09-09-07.jpg', 6
    UNION ALL SELECT 'https://cdn.jsdelivr.net/gh/stephen688/blog-images@main/images/steve-home-2025-09-09-08.jpg', 7
    UNION ALL SELECT 'https://cdn.jsdelivr.net/gh/stephen688/blog-images@main/images/steve-home-2025-09-09-09.jpg', 8
) AS source
WHERE @archived_moment_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM moment_image existing
      WHERE existing.moment_id = @archived_moment_id
        AND existing.sort = source.sort
  );

COMMIT;
