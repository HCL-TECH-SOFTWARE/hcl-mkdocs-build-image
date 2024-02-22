#!/bin/bash
# Creates docker files with all MKDocs extensions we use
mvn clean package
docker build --tag ghcr.io/hcl-tech-software/mkdocs:latest .
echo done