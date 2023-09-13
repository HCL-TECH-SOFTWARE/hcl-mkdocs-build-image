#!/bin/bash
# Creates docker files with all MKDocs extensions we use
docker build --tag ghcr.io/HCL-TECH-SOFTWARE/mkdocs:latest .
echo done