// The Play plugin
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.5.8")

// web plugins

addSbtPlugin("com.typesafe.sbt" % "sbt-less" % "1.1.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-jshint" % "1.0.4")

addSbtPlugin("com.typesafe.sbt" % "sbt-rjs" % "1.0.8")

addSbtPlugin("com.typesafe.sbt" % "sbt-digest" % "1.1.1")

// Build for Elastic Beanstalk plugins

resolvers += Resolver.url("bintray-kipsigman-sbt-plugins", url("http://dl.bintray.com/kipsigman/sbt-plugins"))(Resolver.ivyStylePatterns)

addSbtPlugin("kipsigman" % "sbt-elastic-beanstalk" % "0.1.4")

addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.6.1")