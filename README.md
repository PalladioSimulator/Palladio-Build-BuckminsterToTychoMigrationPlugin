# Palladio-Build-BuckminsterToTychoMigrationPlugin

Just copy the following POM file into the root of your project (i.e., the folder that contains a folder for the buckminster Eclipse project), adjust it as described below, and issue the command `mvn package`. The target folder will contain the migrated project. Please note, that you still have to define the target platform to get a properly working build.

Please do the following adjustements:
* targetGroupId: The group id to be used, which will also be part of the name of the update site
* updatesiteCategory: The category to place the features in (support, qual, core, addons)

```
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>

	<groupId>org.palladiosimulator</groupId>
	<artifactId>migration</artifactId>
	<version>0.1.0-SNAPSHOT</version>
	<packaging>pom</packaging>

	<build>
		<plugins>
			<plugin>
				<groupId>org.palladiosimulator</groupId>
				<artifactId>buckminster-tycho-migrator</artifactId>
				<executions>
					<execution>
						<id>migration</id>
						<phase>package</phase>
						<goals>
							<goal>migrate</goal>
						</goals>
						<configuration>
							<updatesiteCategory>addons</updatesiteCategory>
							<targetGroupId>org.palladiosimulator.solver</targetGroupId>
						</configuration>
					</execution>
				</executions>
			</plugin>
		</plugins>
	</build>

</project>

```

After the script run successfully
* remove everything from the root folder except the target folder (git rm -r)
* copy the contents of the target folder into the root folder (including the .mvn folder and the new pom.xml)
* add all newly added folders (git add)

Git automatically detects the movement of the files.

Now start the build using `mvn clean verify` and determine missing dependencies. Locate the dependencies on our update site (or other update sites) and add entries to the target platform definition. Please note that
* you must not use the Eclipse target platform editor because it discards additional attributes we need
* you must not use the aggregated update site in the target platform
* for our dependencies, you have to add two entries (one for nightly and one for release)

You can have a look at [other target platform definitions](https://github.com/PalladioSimulator/Palladio-Analyzer-SimuLizar/blob/master/releng/org.palladiosimulator.simulizar.targetplatform/tp.target) to get an idea of how to create such target platforms.
