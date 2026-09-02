-- $$
DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM friendship
        GROUP BY LEAST(requester_id, recipient_id), GREATEST(requester_id, recipient_id)
        HAVING COUNT(*) > 1
    ) THEN
        RAISE EXCEPTION
            'Cannot enforce unordered friendship uniqueness: reciprocal duplicate rows exist.';
    END IF;
END
$$;
-- ;

DROP INDEX IF EXISTS uk_friendship_pair;

CREATE UNIQUE INDEX uk_friendship_pair
    ON friendship (
                   LEAST(requester_id, recipient_id),
                   GREATEST(requester_id, recipient_id)
        );