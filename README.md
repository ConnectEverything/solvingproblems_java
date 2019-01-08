# Solving Problems with NATS (Java Edition)

This repository holds the Java templates/solutions for the _Solving Problems with NATS_ courseware.

This project depends on Gradle, the gradlew executable can be used to get the latest version.

The solutions are in `main/java/solutions`. Your version of the exercise code should go in `main/java/exercises`.

To compile, run `./gradlew fatjar`. This will build a single jar with all of the code and dependencies at
`build/libs/solvingproblems_java-1.0-fat.jar`. To run a solution or your own version, you simply need to include 
this jar in the class path. For example:

```java
java -cp build/libs/solvingproblems_java-1.0-fat.jar solutions.ex01_connecting.Connecting
```