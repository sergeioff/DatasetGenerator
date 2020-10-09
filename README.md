# DatasetGenerator

Fake data generator. Generates CSV files

## Build
```bash
gradle clean shadowJar
```

## Run
```
java -jar DatasetGenerator-1.0-SNAPSHOT-all.jar 20000000
```

## Optimisation
If you have Scala installed mark it as `compileOnly` in the `build.gradle`