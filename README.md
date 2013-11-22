<!--

    Copyright (c) 2007-2013 Sonatype, Inc. All rights reserved.

    This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
    which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.

-->
## Nexus Performance Testing Library

A Sonatype Nexus quick & dirty performance regression and stress test library.

### Building

mvn clean install

This creates an uber jar in target which contains all the needed dependencies.

For certain dependencies to be resolved and code to be buildable ( ie. nexus pro features ) you need access to the
following Sonatype repository using your Sonatype Customer Credentials

https://repository.sonatype.org/content/groups/private-nexus-dev/

See https://support.sonatype.com/entries/21582466-How-can-I-write-a-custom-staging-rule- for more info

### How it works

Using the details in the scenario xml file, the program spins up request threads for nexus. During the scenario run,
metrics are captured. At scenario end, metrics are stored in a local h2 database. If asked, the program will compare
these new metrics with a previous run, and fail if the metrics are outside a threshold.

You can only tell this library:

- where you nexus lives
- what URLs to access
- authentication to use
- number of simulated clients
- rate of requests

### Creating Scenarios

Scenarios are defined using xml files in the scenarios directory. Use existing scenarios as an example or review
the code.

### Adding Scenario Data

CSV and standard NCSA log files ( tar/gzipped ) can be parsed to simulate actual requests.

### Configuring your Nexus Under Test

Setting up Nexus is up to you! This library does not aim to help you with that.

### Running

To run test scenario and record performance metrics in db
(obviously, use actual baseline version).

    ./runtest.sh sample-scenario 2.4.0-09

To run test scenario, record performance metrics and the db
and compare performance to an earlier scenario run

    ./runtest.sh sample-scenario 2.5.0-03 2.4.0-09

To run test scenario, compare performance to an earlier scenario run,
do not record metrics in the db. Useful to test scenario itself

    ./runtest.sh sample-scenario - 2.4.0-09

