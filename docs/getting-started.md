# Getting Started

## Checking out / Cloning

on Mac or Linux:<br/>
`git clone https://github.com/WellRedPandit/smehotron.git`

on Windows, using [Github Desktop](https://desktop.github.com/) is recommended.

## Prerequisites

You need the [Java SE Development Kit 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) installed.

## Building

Mac/Linux:

```bash
cd smehotron
./sbt dist
```

Windows:

```
cd smehotron
sbt dist
```

Upon successful completion of the `sbt dist` command, a directory called `dist` that contains all required software is created. It has the following structure:

```
dist/
├── saxon
│   ├── resolver.jar
│   └── saxon.he.9.7.0.7.jar
├── schematron
│   ├── iso_abstract_expand.xsl
│   ├── iso_dsdl_include.xsl
│   ├── iso_schematron_skeleton_for_saxon.xsl
│   └── iso_svrl_for_xslt2.xsl
├── smehotron.bat
├── smehotron.jar
└── smehotron
```

## Running

You can rename `dist` to anything you like and relocate it anywhere you like. To run, you need to call `smehotron.bat` (on Windows) or `smehotron` (on Mac or Linux).

Without any arguments it outputs usage info:

```
> ./smehotron
Error: no parameters supplied
smehotron 1.0.3
Usage: smehotron [options]

  -c, --cfg <config-file>  config (optional)
  -s, --sch <sch-driver>   schematron file (optional)
  -x, --xml <xml-file>     xml file (optional)
  -r, --root <path/to/dir>
                           path to a root dir (optional)
  -l, --loglevel <log level>
                           log level (case insensitive): OFF, ERROR (default), WARN, INFO, DEBUG, TRACE, ALL
```

Typically, you need to create a config file (see docs/smehotron.sample.conf.xml for an example) and run smehotron with it:

```
> ./smehotron -c path/to/your/smehotron-config.xml
```

For example, running with a basic test config (from the directory where you cloned the repository and ran `sbt dist`) will produce this:

```
> dist/smehotron -c src/test/resources/basic/basic-ok.smehotron.config.xml
<smehatron-results><test status="success">
      <module>basic</module>
      <sch-driver>src/test/resources/basic/basic.sch</sch-driver>
      <input-control>src/test/resources/basic/basic-ok.xml</input-control>
      <svrl>src/test/resources/basic/basic-ok.xml.svrl</svrl>
    </test></smehatron-results>
```

The above command can be used as a smoke test after you build smehotron.
