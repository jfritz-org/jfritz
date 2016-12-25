# Creating release versions of JFritz

1. On a Linux machine:
  1. Build DIST version with ant:
```
ant -f build.xml
```
  1. Zip ant/dist folder to jfritz-x.y.z_r.zip

  1. Build Linux and OSX-Releases:
```
ant -f build-release.xml prepare create-linux create-mac
```
  1. Rename ant/release files to contain the Revision!

1. Copy ant/dist folder to Windows share, under Release/WIN/binaries

1. On a Windows machine:
  1. Install Inno Setup Compiler
  1. Open file Release/WIN/jfritz.iss
  1. Run/Compile the script
  1. Copy file from Release/WIN/Output to ant/release folder and rename it to jfritz-x.y.z_r-Setup.exe

# Upload files to Sourceforge

# Update JFritz-Homepage

# Create new Thread in IP-Phone-Forum
