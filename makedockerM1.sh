#!/bin/bash
# Creates docker files with all MKDocs extensions we use
docker build --tag ghcr.io/hcl-tech-software/mkdocs/mkdocs:m1 --file DockerfileM1 .
echo done