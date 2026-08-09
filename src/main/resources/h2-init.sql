CREATE TABLE IF NOT EXISTS collections(
    name TEXT NOT NULL, PRIMARY KEY (name)
);
CREATE TABLE IF NOT EXISTS flashcards(
    collection TEXT NOT NULL,
    front TEXT NOT NULL,
    back TEXT NOT NULL,
    CONSTRAINT fk_collection
      FOREIGN KEY(collection)
      REFERENCES collections(name)
);
