CREATE DATABASE collections;

\c collections

CREATE TABLE collections(
	name TEXT NOT NULL,
	PRIMARY KEY (name)
);

CREATE TABLE flashcards(
	collection TEXT NOT NULL,
	front TEXT NOT NULL,
	back TEXT NOT NULL,
	CONSTRAINT fk_collection
      FOREIGN KEY(collection)
        REFERENCES collections(name)
);
