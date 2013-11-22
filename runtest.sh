#!/bin/bash
#
# Copyright (c) 2007-2013 Sonatype, Inc. All rights reserved.
#
# This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
# which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
#


scenario=$1
buildid=$2
baselineid=$3

NEXUS_URL=http://localhost:8081/nexus
NEXUS_USERNAME=admin
NEXUS_PASSWORD=admin123

# scenario is performance/stress test scenario to execute (json file in scenarios/)
# buildid is fully qualified version of the nexus instance running at $NEXUS_URL,
#         if provided enables recording of performance metris in the database
#         special '-' value disables performance metrics recording
# baselineid is baseline buildid, if provided, performance of this build will be
#         asserted to be within tolerance range compared to the baseline.


extra_vmargs=

if [ -n "$buildid" ]; then
    extra_vmargs="$extra_vmargs -Dperftest.buildId=$buildid"

    if [ -n "$baselineid" ]; then
        extra_vmargs="$extra_vmargs -Dperftest.baselineId=$baselineid"
    fi
fi

timestamp=$(date '+%Y%m%d-%H%M%S')

mkdir logs

java -cp target/nexus-perftest-0.0.1-SNAPSHOT-jar-with-dependencies.jar \
   -Dnexus.baseurl=$NEXUS_URL \
   -Dnexus.username=$NEXUS_USERNAME -Dnexus.password=$NEXUS_PASSWORD \
   -Dperftest.http.timeout=300000 \
   $extra_vmargs \
   com.sonatype.nexus.perftest.PerformanceTestRunner \
   scenarios/$scenario.xml 2>&1 | tee logs/$scenario-$buildid-$timestamp.log
