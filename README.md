Introduction
============
This project contains a set of glu scripts that have been contributed by the community. It also
serves as an example on how to write and test glu scripts. Checkout the
[glu project](https://www.github.com/linkedin/glu) for more details about glu.

How to write and test a glu script
----------------------------------
First, you should check the documentation which gives some information about 
[glu scripts](http://linkedin.github.com/glu/docs/latest/html/glu-script.html).

Next, you should check the sample glu script checked in with this project which demonstrates
how to use some basic features as well as how to write a unit test for your glu script. The javadoc
is fairly extensive and should allow you to bootstrap pretty quickly.

You may also want to check a real-life glu script for more details about advanced features by
checking the [jetty glu script](https://github.com/linkedin/glu/blob/master/scripts/org.linkedin.glu.script-jetty/src/main/groovy/JettyGluScript.groovy).

If you want to embed glu scripts in your own build lifecycle, the critical piece is the dependencies 
section in the `scripts/build.gradle` file:

    dependencies {
      compile spec.external.linkedinUtilsGroovy
      compile spec.external.gluAgentAPI
      groovy spec.external.groovy

      testCompile spec.external.gluScriptsTestFwk
      testCompile spec.external.junit
    }
