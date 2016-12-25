# Creating release versions of JFritz

1. On a Linux machine:
  1. Build DIST version with ant:
```
ant -f build.xml
```

  1. Build Linux and OSX-Releases:
```
ant -f build-release.xml prepare create-linux create-mac
```

1. On a Windows machine:
  1. TODO
