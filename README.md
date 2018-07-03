# Palladio-Build-BuckminsterToTychoMigrationPlugin

Just copy the following POM file into your buckminster project and issue the command `mvn package`. The target folder will contain the migrated project. Please note, that you still have to define the target platform to get a properly working build.

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
				<version>0.1.0</version>
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
