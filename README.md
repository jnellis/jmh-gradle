# jmh-gradle
A jmh benchmark build script and drop-in replacement workaround for the current issues with [jmh-gradle-plugin](https://github.com/melix/jmh-gradle-plugin) on windows.  It is not windows specific and can be a simpler script for running jmh benchmarks. 

# What/why is this?

Currently the jmh-gradle-plugin is broken for windows machines. On windows machines the plugin will hold file locks on the generated benchmark.jar and associated dependencies in the users home ./gradle/cache. It seems the gradle Worker API is at issue holding dependencies of the classpath passed to the worker daemon in the form of a file lock on windows. There are three workarounds to using the jmh-gradle-plugin on windows:

1. Use jmh-gradle-plugin version 0.4.3 and gradle 4.0.2 or earlier (these versions don't use the gradle Worker API which is the underlying issue.)
2. Do your benchmarking solely in WSL (windows subsystem for linux) for which file locking does not exist but sometimes zombie processes do pile up for some reason and you will likely need a machine with more RAM. 
3. Run with the `--no-daemon` gradle option. 
4. Replace jmh-gradle-plugin with this script.


### Issue related reading
[Not exiting JVM and keeping a lock on file after benchmarking is over, Windows 10 #134](https://github.com/melix/jmh-gradle-plugin/issues/134)

[fix: run GC after Runner finished #145](https://github.com/melix/jmh-gradle-plugin/pull/145)

[Gradle daemon locks files by leaving file handles open #937](https://github.com/gradle/gradle/issues/937)

# Usage:
If you've been using the jmh-gradle-plugin and have configured the `jmh` block that passes on runtime options to jmh in your `build.gradle` file then you might not have to do anything except comment out the jmh plugin and drop this `jmh.gradle` file into your project directory and then insert the following into your `build.gradle`.

`apply from: 'jmh.gradle'`

If you haven't used the jmh-gradle-plugin you will need to configure the jmh block. See the example project build.gradle file for an idea. A list of options are at the bottom here.

#Tasks
  ###jmh
  This task mirrors the jmh-gradle-plugin task and depends on the jmhJar task to create an uber/fat jar. If you have benchmarks that pull from your test sources then you need to set `includeTests = true` in the jmh block. 
  
  ###jmhClasspathRun
  This task bypasses creating an executable benchmark.jar file and executes benchmarks from the runtimeClasspath dependency chain.  Is this taboo? If you don't like your snow peas touching your mashed potatoes on your plate then maybe it is.
   
  ###jmhJar
  This task mirrors the jmh-gradle-plugin task in creating an uber/fat jar from the runtimeClasspath dependency chain. It doesn't consider if you are using the shadow plugin or if you have duplicate class issues in the dependency chain yet. Feel free to poke around, its a short task. 
  
### other notes
* It **has not** been tested on all the jmh options. 
* It does support additional jmh command line options like help and benchmark lists that can be comment/uncommented out since these exit the benchmarking run by default. 
* Options from the jmh-gradle-plugin's `jmh` block that are not supported will report that they are not supported but will not throw an error.
* You may not need to specify `jvmArgs = ['-Djmh.separateClasspathJAR=true']` anymore.
* It doesn't respond to actual command line options, everything is through the `jmh` block.
* It should work fine with the [jmhReport plugin](https://github.com/jzillmann/gradle-jmh-report) if you previously had the `jmh` task setup to finalizeBy jmhReport. If this was the case in your build then the `jmhClasspathRun` task is also hooked to run jmhReport as well.    

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
   benchmarkParameters =  ["paramName":[val1,val2,val3]] // Benchmark parameters.
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

# Previous sanity check version
The original version of this script is `jmh_mvn.gradle` and involves executing maven commands to create a benchmarks.jar file. It is very touchy and not recommended.  


