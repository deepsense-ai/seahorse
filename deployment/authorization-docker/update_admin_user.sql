UPDATE "users"
SET username = '${SEAHORSE_ADMIN_EMAIL}',
    email = '${SEAHORSE_ADMIN_EMAIL}',
    lastmodified = CURRENT_TIMESTAMP
WHERE id = '172dce21-ea97-42cc-b2ff-30ce4ec7de9d';
