package com.epau.app.flashcard.app.installer.operations;

import org.jetbrains.annotations.NonNls;

import java.util.List;

import static java.util.List.of;

/// app dependencies
interface App {

	// @formatter:off
	@NonNls
	List<String> DEPENDENCIES = of(
			"feature:repo-add mvn:no.priv.bang.karaf/jersey/LATEST/xml/features",
			"feature:install jersey-karaf-feature",
			"feature:install war",
			"feature:install http",
			"feature:install jndi",
			"feature:install jdbc",
			"feature:install pax-jdbc-postgresql",
			"feature:install pax-jdbc-sqlite",
			"bundle:install mvn:org.glassfish.jersey.media/jersey-media-multipart/2.47",
			"bundle:install mvn:org.glassfish.jersey.ext/jersey-mvc/2.47",
			"bundle:install mvn:org.glassfish.jersey.ext/jersey-mvc-mustache/2.47",
			"bundle:install wrap:mvn:com.github.spullara.mustache.java/compiler/0.9.14",
			"bundle:install mvn:org.jvnet.mimepull/mimepull/1.9.15",
			"bundle:install mvn:com.univocity/univocity-parsers/2.9.1"
	);
	// @formatter:on


}
