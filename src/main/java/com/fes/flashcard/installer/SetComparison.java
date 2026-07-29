package com.fes.flashcard.installer;

import java.util.HashSet;
import java.util.Set;

public interface SetComparison {

	static void printSimilarElements(Set<?> set1, Set<?> set2) {
		var similar = new HashSet<>(set1);
		//noinspection SuspiciousMethodCalls
		similar.retainAll(set2);
		similar.forEach(IO::println);
	}

	static void printDifferentElements(Set<?> set1, Set<?> set2) {
		var similar   = new HashSet<>(set1);
		var different = new HashSet<>();
		different.addAll(set1);
		different.addAll(set2);
		//noinspection SuspiciousMethodCalls
		similar.retainAll(set2);
		different.removeAll(similar);
		different.forEach(IO::println);
	}

	static void printElementsOnlyInSet1(Set<?> set1, Set<?> set2) {
		var onlyInSet1 = new HashSet<>(set1);
		onlyInSet1.removeAll(set2);
		onlyInSet1.forEach(IO::println);
	}

	static void printElementsOnlyInSet2(Set<?> set1, Set<?> set2) {
		var onlyInSet2 = new HashSet<>(set2);
		onlyInSet2.removeAll(set1);
		onlyInSet2.forEach(IO::println);
	}
}
