# FastClasspathScanner

FastClasspathScanner is an uber-fast, ultra-lightweight classpath scanner for Java, Scala and other JVM languages. FastClasspathScanner has been [carefully optimized](https://github.com/lukehutch/fast-classpath-scanner/wiki#how-fast-is-fastclasspathscanner). The project is stable and actively maintained.

**What is classpath scanning?** Classpath scanning involves scanning directories and jar/zip files on the classpath to find files (especially classfiles) that meet certain criteria. In many ways, classpath scanning offers the *inverse of the Java class API and/or reflection:* for example, the Java class API can tell you the superclass of a given class, or give you the list of annotations on a class; classpath scanning can find all subclasses of a given class, or find all classes that are annotated with a given annotation.

**FastClasspathScanner is able to:**

* [Find classes that subclass/extend a given class](https://github.com/lukehutch/fast-classpath-scanner/wiki/3.1.-Finding-subclasses-or-superclasses).
* [Find classes that implement an interface](https://github.com/lukehutch/fast-classpath-scanner/wiki/3.2.-Finding-classes-that-implement-an-interface) or one of its subinterfaces, or whose superclasses implement the interface or one of its subinterfaces.
* [Find interfaces that extend a given interface](https://github.com/lukehutch/fast-classpath-scanner/wiki/3.3.-Finding-subinterfaces-or-superinterfaces) or one of its subinterfaces.
* [Find classes that have a specific class annotation or meta-annotation](https://github.com/lukehutch/fast-classpath-scanner/wiki/3.4.-Finding-classes-with-specific-annotations-or-meta-annotations).
* [Find classes that have methods with a given annotation](https://github.com/lukehutch/fast-classpath-scanner/wiki/3.5-Finding-classes-that-have-methods-or-fields-with-a-given-annotation).
* [Find classes that have fields with a given annotation](https://github.com/lukehutch/fast-classpath-scanner/wiki/3.5-Finding-classes-that-have-methods-or-fields-with-a-given-annotation).
* [Find all classes that contain a field of a given type](https://github.com/lukehutch/fast-classpath-scanner/wiki/3.6a.-Finding-classes-with-fields-of-a-given-type) (including identifying fields based on array element type and generic parameter type).
* Read the constant literal initializer value in a classfile's constant pool for a [specified static final field](https://github.com/lukehutch/fast-classpath-scanner/wiki/3.6b.-Reading-constant-initializer-values-of-static-final-fields).
* Find files (even non-classfiles) anywhere on the classpath that have a [path that matches a given string or regular expression, or with a specific file extension](https://github.com/lukehutch/fast-classpath-scanner/wiki/3.7.-Finding-classpath-files-based-on-filename-pattern).
* Return a list of the [names of all classes, interfaces and/or annotations on the classpath](https://github.com/lukehutch/fast-classpath-scanner/wiki/3.8.-Enumerating-all-classes-on-the-classpath) (after whitelist and blacklist filtering).
* Return a list of [all directories and files on the classpath](https://github.com/lukehutch/fast-classpath-scanner/wiki/1.-Usage#listing-classpath-elements) (i.e. all classpath elements) as a list of File objects, with the list deduplicated and filtered to include only classpath directories and files that actually exist, saving you from the complexities of working with the classpath and classloaders.
* [Detect changes](https://github.com/lukehutch/fast-classpath-scanner/wiki/1.-Usage#detecting-changes-to-classpath-contents) to the files within the classpath since the first time the classpath was scanned.
* [Generate a GraphViz .dot file](https://github.com/lukehutch/fast-classpath-scanner/wiki/3.9.-Generating-a-GraphViz-dot-file-from-the-classgraph) from the class graph for visualization purposes, as shown below. A class graph visualization depicts connections between classes, interfaces, annotations and meta-annotations, and connections between classes and the types of their fields.

<p align="center">
  <img src="https://github.com/lukehutch/fast-classpath-scanner/blob/master/src/test/java/com/xyz/classgraph-fig.png" alt="Class graph visualization"/>
</p>

## Status

FastClasspathScanner is stable, feature complete, optimized, and (at time of writing) has no known bugs, although there are some feature requests, e.g. adding Java 9 support. Every effort is made to fix bugs quickly when they are reported.

## Documentation

[See the wiki for full documentation.](https://github.com/lukehutch/fast-classpath-scanner/wiki)

## Mailing List

Feel free to subscribe to the [FastClasspathScanner-Users](https://groups.google.com/d/forum/fastclasspathscanner-users) email list for updates, or to ask questions.

## Downloading

Use the "Clone or download" button at the top right of this page to get the source. You can get a pre-built JAR (usable in JRE 1.7 or later) from [Sonatype](https://oss.sonatype.org/#nexus-search;quick~fast-classpath-scanner), or add the following Maven Central dependency:

```xml
<dependency>
    <groupId>io.github.lukehutch</groupId>
    <artifactId>fast-classpath-scanner</artifactId>
    <version>LATEST</version>
</dependency>
```

## License

The MIT License (MIT)

Copyright (c) 2016 Luke Hutchison
 
Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 
The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
