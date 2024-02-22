#!/bin/sh
#
# ==========================================================================
# Copyright (C) 2023-2024 HCL America, Inc. ( https://www.hcl.com/ )
#                            All rights reserved.
# ==========================================================================
# Licensed under the  Apache License, Version 2.0  (the "License").  You may
# not use this file except in compliance with the License.  You may obtain a
# copy of the License at <http://www.apache.org/licenses/LICENSE-2.0>.
#
# Unless  required  by applicable  law or  agreed  to  in writing,  software
# distributed under the License is distributed on an  "AS IS" BASIS, WITHOUT
# WARRANTIES OR  CONDITIONS OF ANY KIND, either express or implied.  See the
# License for the  specific language  governing permissions  and limitations
# under the License.
# ==========================================================================
#

# RUN MKDOCS or MKDOCS with preprocessor
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk
export PATH=$PATH:$JAVA_HOME/bin
export LD_LIBRARY_PATH=$JAVA_HOME/lib/server

# Check for parameters
if [ "$#" -eq 0 ]
then
  echo "No arguments supplied, use 'versions' or mkdocs command"
  exit 1
fi

# check for Preprocesor vs.mkdocs
if [ "$1" = "versions" ]; then
   java -jar /deployments/MkDocsVersions.jar $2
   # TODO: Serve after generation
else
   if [ x$1x = "xx" ]; then
      mkdocs --dev-addr=0.0.0.0:8000 $1 $2 $3 $4 $5 $6
   else
      mkdocs serve --dev-addr=0.0.0.0:8000 $1 $2 $3 $4 $5 $6
    fi
fi