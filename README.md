# jmh-gradle
A drop-in replacement workaround for the current issues with [jmh-gradle-plugin](https://github.com/melix/jmh-gradle-plugin) on windows.

# What/why is this?

Currently the jmh-gradle-plugin is broken for windows machines. On windows machines the plugin will hold file locks on the generated benchmark.jar and associated dependencies in the users home ./gradle/cache. It seems the gradle Worker API is at issue holding dependencies of the classpath passed to the worker daemon in the form of a file lock on windows. There are three workarounds to using the jmh-gradle-plugin on windows:

1. Use jmh-gradle-plugin version 0.4.3 and gradle 4.0.2 or earlier (these versions don't use the gradle Worker API which is the underlying issue.)
2. Do your benchmarking solely in WSL (windows subsystem for linux) for which file locking does not exist but sometimes zombie processes do pile up for some reason and you will likely need a machine with more RAM. 
3. Run with the `--no-daemon` gradle option. 
4. Replace jmh-gradle-plugin with this script until things get sorted.


### Issue related reading
[Not exiting JVM and keeping a lock on file after benchmarking is over, Windows 10 #134](https://github.com/melix/jmh-gradle-plugin/issues/134)

[fix: run GC after Runner finished #145](https://github.com/melix/jmh-gradle-plugin/pull/145)

[Gradle daemon locks files by leaving file handles open #937](https://github.com/gradle/gradle/issues/937)


# Dependencies:
* Maven installed and on path.
* Being able/wanting to publish to mavenLocal.

# Usage:
If you've been using the jmh-gradle-plugin and have configured the `jmh` block that passes on runtime options to jmh in your build.gradle file then you might not have to do anything except comment out the jmh plugin and drop this jmh.gradle file into your project directory and then insert the following into your build.gradle.

`apply from: 'jmh.gradle'`

# What is involved / What to expect
This workaround is no magic beans. The JMH web pages suggest [running benchmarks](https://openjdk.java.net/projects/code-tools/jmh/) from a maven project and so that is what this script is doing. It basically runs an Exec task that creates a maven pom project in your build directory, throws out the sample benchmark and links to your `src/jmh/java` directory for sources, relies on the gradle task `publishToMavenLocal` so it can see your project jar and then builds and runs the benchmarks. 

Will it work for everyone? Probably not but the script is short and you can probably hack on it to your needs very quickly.
* It's not specific to windows so its likely to work on linux (it works fine in WSL.)
* It may require you build/publish an uber jar.
* It doesn't support the jmh-gradle-plugin `includedTests` so you will have to create a publish task for your tests jar and then add that as a dependency to the generated pom.xml file. 
* It **has not** been tested on all the jmh options. 
* It does support additional jmh command line options like help and benchmark lists that can be comment/uncommented out since these exit the benchmarking run by default. 
* Options from the jmh-gradle-plugin's `jmh` block that are not supported will report that they are not supported.
* You may not need to specify `jvmArgs = ['-Djmh.separateClasspathJAR=true']` anymore.
* The jmh option `benchmarkParameters` hasn't been implemented yet. whoops.
* It doesn't respond to actual command line options, everything is through the `jmh` block.
* It should work fine with jmhReport. 
* Consider just using maven with benchmarks as a subproject and not bother with gradle or any of this.

### Recapping `jmh` block parameters used in jmh-gradle-plugin (this is in your build.gradle)
```
jmh {
   include = ['some regular expression'] // include pattern (regular expression) for benchmarks to be executed
   exclude = ['some regular expression'] // exclude pattern (regular expression) for benchmarks to be executed
   iterations = 10 // Number of measurement iterations to do.
   benchmarkMode = ['thrpt','ss'] // Benchmark mode. Available modes are: [Throughput/thrpt, AverageTime/avgt, SampleTime/sample, SingleShotTime/ss, All/all]
   batchSize = 1 // Batch size: number of benchmark method calls per operation. (some benchmark modes can ignore this setting)
   fork = 2 // How many times to forks a single benchmark. Use 0 to disable forking altogether
   failOnError = false // Should JMH fail immediately if any benchmark had experienced the unrecoverable error?
   forceGC = false // Should JMH force GC between iterations?
   jvm = 'myjvm' // Custom JVM to use when forking.
   jvmArgs = ['Custom JVM args to use when forking.']
   jvmArgsAppend = ['Custom JVM args to use when forking (append these)']
   jvmArgsPrepend =[ 'Custom JVM args to use when forking (prepend these)']
   humanOutputFile = project.file("${project.buildDir}/reports/jmh/human.txt") // human-readable output file
   resultsFile = project.file("${project.buildDir}/reports/jmh/results.txt") // results file
   operationsPerInvocation = 10 // Operations per invocation.
   benchmarkParameters =  [:] // Benchmark parameters.
   profilers = [] // Use profilers to collect additional data. Supported profilers: [cl, comp, gc, stack, perf, perfnorm, perfasm, xperf, xperfasm, hs_cl, hs_comp, hs_gc, hs_rt, hs_thr]
   timeOnIteration = '1s' // Time to spend at each measurement iteration.
   resultFormat = 'CSV' // Result format type (one of CSV, JSON, NONE, SCSV, TEXT)
   synchronizeIterations = false // Synchronize iterations?
   threads = 4 // Number of worker threads to run with.
   threadGroups = [2,3,4] //Override thread group distribution for asymmetric benchmarks.
   timeout = '1s' // Timeout for benchmark iteration.
   timeUnit = 'ms' // Output time unit. Available time units are: [m, s, ms, us, ns].
   verbosity = 'NORMAL' // Verbosity mode. Available modes are: [SILENT, NORMAL, EXTRA]
   warmup = '1s' // Time to spend at each warmup iteration.
   warmupBatchSize = 10 // Warmup batch size: number of benchmark method calls per operation.
   warmupForks = 0 // How many warmup forks to make for a single benchmark. 0 to disable warmup forks.
   warmupIterations = 1 // Number of warmup iterations to do.
   warmupMode = 'INDI' // Warmup mode for warming up selected benchmarks. Warmup modes are: [INDI, BULK, BULK_INDI].
   warmupBenchmarks = ['.*Warmup'] // Warmup benchmarks to include in the run in addition to already selected. JMH will not measure these benchmarks, but only use them for the warmup.

   zip64 = true // Use ZIP64 format for bigger archives
   jmhVersion = '1.21' // Specifies JMH version
   includeTests = true // Allows to include test sources into generate JMH jar, i.e. use it when benchmarks depend on the test classes.
   duplicateClassesStrategy = 'fail' // Strategy to apply when encountring duplicate classes during creation of the fat jar (i.e. while executing jmhJar task)
   
  //additional options not in jmh-gradle-plugin
//  help = true  // uncomment for jmh CLI help screen, then exit
//  list = true  // uncomment to list benchmarks that match filter, and exit
//  listp = true // uncomment to list benchmarks that match filter along with
                 // their parameter list, then exit
//  lprof = true // uncomment to list available profilers, then exit
//  lrf = true   // uncomment to list available result formats, then exit
}
```



