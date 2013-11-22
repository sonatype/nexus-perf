#!/bin/sh
#
# Copyright (c) 2007-2013 Sonatype, Inc. All rights reserved.
#
# This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
# which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
#

# open the h2 database
# Useful query:
# select * from executions;
java -cp h2*.jar org.h2.tools.Shell -url jdbc:h2:~/nexusperftest $1 $2 $3
