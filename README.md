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
