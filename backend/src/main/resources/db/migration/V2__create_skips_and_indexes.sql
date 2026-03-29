-- V1: Add skips column and helpful indexes for play_stat
-- This migration is idempotent (uses IF NOT EXISTS where available) to be safe on re-run.

ALTER TABLE IF EXISTS play_stat
    ADD COLUMN IF NOT EXISTS skips integer;

-- Indexes to speed up common queries (per-user counts, per-track grouping)
CREATE INDEX IF NOT EXISTS idx_play_stat_user_id ON play_stat (user_id);
CREATE INDEX IF NOT EXISTS idx_play_stat_track_id ON play_stat (track_id);
CREATE INDEX IF NOT EXISTS idx_play_stat_user_track ON play_stat (user_id, track_id);

