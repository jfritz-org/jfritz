# JFritz

# Build
We use maven for building. Just use ```mvn clean install``` and all files will be build into *target/dist* folder.

# Packaging of distribution files
We use maven profiles to package distribution files. Just activate one or more profiles and the packages will be generated into *target/release* folder.

| Profile         | Target OS | Comment                                                                 |
|-----------------|:---------:|-------------------------------------------------------------------------|
| packageArchives | all       |Creates zip, tar.gz and tar.bz2 archives                                 |
| packageDeb      | Linux     |Creates a DEB package to be distributed to Debian based operating systems|
| packageOsx      | Mac OS    |Creates a ZIP files to be distributed to Mac OS                          |
| packageWin      | Windows   |Creates a Setup-Exe file to be distributed to Windows systems            |

You might also chain the profiles, so use ```mvn clean install -P packageArchives,packageDeb``` to create archives and a DEB package.

If you want to activate ALL profiles, there is a shortcut. Just use ```mvn clean install -DpackageAll``` (yes, it is a D, not a P).

**Note**: Packaging has been only tested on linux!

**Note**: If you want to distribute a Setup-Exe for Windows systems you first have to update the two properties **exec.wine** and **exec.iscc** to appropriate paths.
