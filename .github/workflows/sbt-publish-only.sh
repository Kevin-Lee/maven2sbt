#!/bin/bash -e

set -x

if [ -z "$1" ]
  then
    echo "Missing parameters. Please enter the [Scala version]."
    echo "sbt-build.sh 2.13.4"
    exit 1
else

  scala_version=$1
  echo "============================================"
  echo "Publish projects"
  echo "--------------------------------------------"
  echo ""
  echo "mkdir -p dotty-docs"
  mkdir -p dotty-docs
  export SOURCE_DATE_EPOCH=$(date +%s)
  echo "SOURCE_DATE_EPOCH=$SOURCE_DATE_EPOCH"

  echo ""
  sbt \
    -J-Xmx2048m \
    ++${scala_version}! \
    -v \
    clean \
    test \
    packagedArtifacts \
    publish

  echo "============================================"
  echo "Publish projects: Done"
  echo "============================================"

fi
