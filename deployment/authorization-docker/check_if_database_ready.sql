-- A very nasty SQL expression that will only succeed if certain schema migration (here: 3.5.1)
-- has been applied and throws an exception (leading to running script failure) otherwise.
-- Innermost select yields one (giving 1, so not null, mapped to 1) or zero (giving null,
-- so mapped by nvl2 to 0) rows.
-- If schema_version doesn't exist, this script will also fail.
select 1/(select nvl2(
  (select 1 from schema_version where version = '3.5.1' AND success = TRUE),
  1,
  0) limit 1);
