# Getting Started

## Checking out / Cloning

on Mac or Linux:<br/>
`git clone https://github.com/WellRedPandit/smehotron.git`

on Windows, using [Github Desktop](https://desktop.github.com/) is recommended.

## Prerequisites

You need the [Java SE Development Kit 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) installed.

## Building

### Mac/Linux:

1. `cd smehotron`
1. to update the source code if needed: `git pull --all`
1. `./sbt dist`

### Windows:

* Launch GitHub for Windows
* Select smehotron repository
* Click the Sync button in the upper-right corner, update the code if needed, then

```
cd smehotron
sbt dist
```

### Expected result

Upon successful completion of the `sbt dist` command, a directory `smehotron-1.0.8` and a zip file `smehotron-1.0.8.zip` (`1.0.8` is an example version, naturally) that contain all required software are created. They have the following structure:

```
smehotron-1.0.8/
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

### General

To run, you need to call `smehotron.bat` (on Windows) or `smehotron` (on Mac or Linux).

Without any arguments it outputs usage info:

```
> ./smehotron
Error: no parameters supplied
smehotron 1.10.0
Usage: smehotron [options]

  -c, --cfg <config-file>  config (optional)
  -s, --sch <sch-driver>   schematron file (optional)
  -x, --xml <xml-file>     xml file (optional)
  -r, --root <path/to/dir>
                           path to a root dir (optional)
  -g, --generate           generate expected SVRLs
  -k, --keep               keep intermediate files
  -l, --loglevel <log level>
                           log level (case insensitive): OFF, ERROR (default), WARN, INFO, DEBUG, TRACE, ALL
```

### Go tests

Typically, you need to create a config file (see the `<go>...</go>` section in `docs/smehotron.sample.conf.xml` for an example) and run smehotron with it:

```
> ./smehotron -c path/to/your/smehotron-config.xml
```

For example, running with a basic test config (from the directory where you cloned the repository and ran `sbt dist`) will produce this:

```
> dist/smehotron -c src/test/resources/basic/basic-ok.smehotron.config.xml
<smehotron-results><test status="success">
      <module>basic</module>
      <sch-driver>src/test/resources/basic/basic.sch</sch-driver>
      <input-control>src/test/resources/basic/basic-ok.xml</input-control>
      <svrl>src/test/resources/basic/basic-ok.xml.svrl</svrl>
    </test></smehotron-results>
```

The above command can be used as a smoke test after you build smehotron.

### Nogo tests

Again, first, you need to create a config file (see the `<nogo>...</nogo>` section in `docs/smehotron.sample.conf.xml` for an example). The difference between go and nogo tests is that the nogo test needs a yardstick (expected svrl report) to compare all subsequent runs of the test with. Consequently, an expected svrl must be generated for every input control source. To achieve that,  run smehotron with the -g switch:

```
> ./smehotron -c path/to/your/smehotron-config.xml -g
```

If an expected svrl report already exists, smehotron will produce an error.

To run tests, run smehotron as usual without the -g switch, e.g.

```
> ./dist/smehotron -c src/test/resources/basic_nogo/basic-ok.smehotron.config.xml
<smehotron-results>
      <nogo><test status="success">
      <module>basic</module>
      <sch-driver>src/test/resources/basic_nogo/basic.sch</sch-driver>
      <input-control>src/test/resources/basic_nogo/basic-ok.xml</input-control>
      <expected-svrl>src/test/resources/basic_nogo/basic-ok-expected.svrl</expected-svrl>
      <actual-svrl>src/test/resources/basic_nogo/basic-ok-actual.xml.svrl</actual-svrl>
    </test></nogo>
    </smehotron-results>
```

A nogo test is successful (`status="success"`) if a freshly generated actual svrl is exactly the same as the expected one.
