CREATE DATABASE collections;

\c collections

CREATE TABLE IF NOT EXISTS collections(
	name TEXT NOT NULL PRIMARY KEY
);

CREATE TABLE IF NOT EXISTS flashcards(
	collection TEXT NOT NULL REFERENCES collections(name),
	front TEXT NOT NULL,
	back TEXT NOT NULL
);
