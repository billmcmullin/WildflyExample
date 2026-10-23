ALTER TABLE calendar_events ADD COLUMN series_id BIGINT;
ALTER TABLE calendar_events ADD CONSTRAINT calendar_event_series_fk
    FOREIGN KEY (series_id) REFERENCES calendar_events(id) ON DELETE CASCADE;
CREATE INDEX calendar_events_series ON calendar_events(series_id);