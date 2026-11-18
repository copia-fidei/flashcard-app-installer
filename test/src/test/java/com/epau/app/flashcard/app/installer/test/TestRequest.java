package com.epau.app.flashcard.app.installer.test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;

import static java.net.URI.create;
import static java.net.http.HttpClient.newHttpClient;
import static org.junit.jupiter.api.Assertions.*;

class TestRequest {

	private static final URI URL = create("http://localhost:18181/flashcards/collections");

	static void sendAndAssert() throws IOException, InterruptedException {
		try (var client = newHttpClient()) {
			var request  = HttpRequest.newBuilder().uri(URL).build();
			var response = client.send(request, BodyHandlers.ofString());

			assertEquals(200, response.statusCode());
		}
	}
}