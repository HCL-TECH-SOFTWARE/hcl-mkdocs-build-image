#!/bin/bash
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

# Handling preprocessor
runPreprocessor() {
   # build and watch
   if [[ $2 == b* ]]; then
      echo Running buildAndWatch
      java -jar /deployments/MkDocsVersions.jar $1
      java -jar /deployments/MkDocsVersions.jar $1 watch &
      runMkDocs serve $3 $4 $5 $6 $7 $8
   fi

   # watch only
   if [[ $2 == w* ]]; then
      echo Running watch only
      java -jar /deployments/MkDocsVersions.jar $1 watch &
      runMkDocs serve $3 $4 $5 $6 $7 $8
   fi

   # generate mkdocs after preprocessor run
   if [[ $2 == g* ]]; then
      echo Running build complete
      java -jar /deployments/MkDocsVersions.jar $1
      runMkDocs build $3 $4 $5 $6 $7 $8
      echo build complete
   fi

   #only run preprocessor
   if [[ $2 == o* ]]; then
      echo Running build complete
      java -jar /deployments/MkDocsVersions.jar $1
      echo Preprocessor run complete
   fi
}

# Handling mkdocs
runMkDocs() {
  if [ x$1x = "xx" ]; then
      mkdocs serve --dev-addr=0.0.0.0:8000 $1 $2 $3 $4 $5 $6
   else
      mkdocs --dev-addr=0.0.0.0:8000 $1 $2 $3 $4 $5 $6
    fi
}

# Check for parameters
if [ "$#" -eq 0 ]
then
  echo "You need to supply arguments (behind mkdocs:latest) to run this image!"
  echo "PREPROCESSOR:"
  echo "versions [path-to-config.yml] = run the versions preprocessor and exit"
  echo "versions [path-to-config.yml] buildAndWatch = run the versions preprocessor, switch to watch mode and run mkdocs serve"
  echo "versions [path-to-config.yml] watch = start versions preprocessor watch mode and run mkdocs serve"
  echo "versions [path-to-config.yml] generate = run versions preprocessor and run mkdocs build"
  echo "versions [path-to-config.yml] only = run only versions preprocessor and exit"
  echo "MKDOCS:"
  echo "serve = run mkdocs serve"
  echo "build = run mkdocs build"
  echo "See: https://www.mkdocs.org/user-guide/cli/"
  exit 1
fi

# check for Preprocesor vs.mkdocs
if [ "$1" = "versions" ]; then
   runPreprocessor $2 $3 $4 $5 $6 $7 $8
else
   runMkDocs $1 $2 $3 $4 $5 $6 $7 $8
fi