# Flashcard Application Installer
A Java Swing-based installer for this flashcard management application: https://github.com/copia-fidei/flashcards

# Installed components
- Database: PostgreSQL or H2
- Java 21
- Apache Karaf application server (Karaf)
- Flashcard application + dependencies

# How the installer works
1. Wizard pages appear with configuration options (e.g. user, password).
2. After the configuration, the installation can be started.
Each installation step is displayed with a separate progress bar and log/error messages.

For the installation, it uses 
- Linux system commands
  - APT (Advanced Package Tool), e.g. `sudo apt install postgresql-14`
  - others: dpkg-query, update-alternatives, systemctl...
- file system operations:
  - extract Karaf as ZIP archive to the desired location
  - modify configuration files
- the psql (PostgreSQL) terminal front-end (to create databases, roles, passwords...)
- the Karaf console (to install OSGi-bundles, H2 database, database drivers...)

Some operations are run as sudo (Root).

# Requirements to run
- Linux system with all system utilities the installer calls.
- Internet access (for APT)
- Root password (for sudo)
- A system-wide Java installation is not required to run the installer itself. However, the installer installs Java 21 as part of the installation.

# Risks of usage
If you want to run this installer, be aware of the following risks:

- The installer runs `apt purge` to remove PostgreSQL.
  When this command is executed normally in a terminal, a prompt asks you to choose whether to delete or keep the database data. Choosing the wrong option could result in data loss.
  To avoid this, the installer automatically selects the option to keep the data. However, this behavior has only been tested on one machine.
- Reinstalling Karaf may overwrite existing files.
  - Karaf also contains the H2 database, which stores the flashcard data
- You need to enter your `sudo` password.
- The system-wide Java will be changed to Java 21. 
This may break other applications that depend on higher versions of Java, but is required for Karaf to run.

# How to use this installer
1. Download the `.deb` package from /releases
2. Open a terminal in the directory containing the downloaded package.
3. Install the package, it will be installed under /opt.
`sudo apt install ./<package-name>.deb`
4. Start the Installer from the Applications Menu. Search for "Flashcard App Installer".
Note: The **installer** for the flashcard application has been installed, not the flashcard application itself.

# How to build this installer
`mvn clean package`
This creates a Debian package in target/jpackage, the same as in release.

Use the -P option for a different language:
`mvn clean package -P german`

## Maven POM explanation
This application is a fully modular Java project. The build steps to achieve this are:

1. Build the application JAR and moving it into /modules
2. Copy all dependencies into /modules.
3. Add module-info to all non-modular JARs, using the ModiTect Maven plugin.
Non-modular JARs aka automatic modules can be determined with this Bash-script:
`for f in *.jar; do jar -d -f "$f" --release 25; done | grep automatic`
4. Build an os-native package with JPackage from all the modules.
