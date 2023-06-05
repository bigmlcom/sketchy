# Contributing to sketchy


## Build using deps.edn

We made some scripts to build via deps.edn

```
# Compile java files with javac
./bin/build javac

# Build the jar
./bin/build jar

# Run tests
./bin/test

# Deploy a new version to clojars (maintainers only)
./bin/build deploy
```