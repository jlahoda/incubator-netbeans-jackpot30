<!--

    Licensed to the Apache Software Foundation (ASF) under one
    or more contributor license agreements.  See the NOTICE file
    distributed with this work for additional information
    regarding copyright ownership.  The ASF licenses this file
    to you under the Apache License, Version 2.0 (the
    "License"); you may not use this file except in compliance
    with the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.

-->

# Apache NetBeans Jackpot

This tool allows to run NetBeans Java code checker without the NetBeans IDE. This includes running the custom Java hints specified in META-INF/upgrade/*.hint.

### Requirements

#### To Build

* NetBeans 9.0
* JDK 8 and JDK 10
* bash
* ant 1.9.9 or above

#### To Run

* JDK 10

### Building

Build using:

```
$ ant -DNETBEANS_PLATFORM=<path-to-NetBeans-9.0> -DJDK10=<path-to-JDK-10> build-and-test
```

The built product is in tool/build/jackpot.

### Running

Run using:

```
$ jackpot <options> <files-to-inspect>
```

To get help on the options, run:

```
$ jackpot --help
```
