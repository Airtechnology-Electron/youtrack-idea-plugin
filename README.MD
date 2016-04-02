YouTrack-Idea-Plugin
======================
[![Build Status](https://travis-ci.org/jk1/youtrack-idea-plugin.png?branch=master)](https://travis-ci.org/jk1/TeamCity-dependencies-gradle-plugin)

This plugin brings Jetbrains YouTrack bugtracker features to Intellij IDE family.

The latest released version can be found at [JetBrains IDE plugin repositiry](https://plugins.jetbrains.com/plugin/8215) or installed directly from an IDE itself.

**Compatible IDEs:** IntelliJ IDEA 15+, WebStorm 11+, PyCharm 5+, PhpStorm 10+, RubyMine 8+, AppCode 3.3+, CLion 1.2+, DataGrip 1.0+

**Compatible Youtrack versions:** 5.2+

##Installation and Configuration

This plugin relies on a long-standing task management plugin and it's context management. It will be installed automatically once YouTrack plugin is installed. Having YouTrack plugin installed from a plugin repository  proceed as foolows:

1. Navigate to Settings dialog -> Tools -> Tasks -> Servers
2. Configure connection to your YouTrack instance

That's it, plugin is now ready to work.

## Features

#### Command execution

Among the other context types task management plugin supports bug tracker issues. When an issue from YouTrack as set as an active task it's possible to execute YouTrack command against the issue. Hit Ctrl+Shift+Y of use Tools menu to invoke a command window.

Please refer to [YouTrack command guide](https://confluence.jetbrains.com/display/YTD65/Quick+Start+Guide.+Using+Command+Window) for the general command execution rules.

##Development

To get an Idea project open ```build.gradle``` file with Intellij Idea and follow import instructions.

Handy development tasks:

- ```./gradlew assemble``` to build plugin in build/distributions

- ```./gradlew tests``` to run tests

- ```./gradlew runIdea``` to run IntelliJ IDEA with YouTrack plugin enabled

Pull requests are most wellcome.


