# MKDocs HCL Container image

## Objective

Create and maintain a container (a.k.a. Docker) image that contains all [MKDocs Material](https://squidfunk.github.io/mkdocs-material/) customisations HCL Labs and friends found useful.
It supports both local run and is used in the HCL internal Jenkins pipelines

## Projects using the template

- n.n.
- n.n.
- n.n.
- n.n.

## Build result

The build creates 2 flavours:

- Intel for local run
- M1 (Apple) for local run

The resulting container are available in our [Github repository](https://github.com/HCL-TECH-SOFTWARE/domino-jnx/pkgs/container/mkdocs)

## MKDocs plugins used

- [mkdocs-awesome-pages-plugin](https://github.com/lukasgeiter/mkdocs-awesome-pages-plugin/)
- [mkdocs-git-revision-date-localized-plugin](https://github.com/timvink/mkdocs-git-revision-date-localized-plugin)
- [mike](https://github.com/jimporter/mike)
- [mkdocs-markdownextradata-plugin](https://github.com/rosscdh/mkdocs-markdownextradata-plugin)
- [mkdocs-git-authors-plugin](https://github.com/timvink/mkdocs-git-authors-plugin)

## Local Building

The images are build by a github, so typically you don't need to build them locally

Run `./makedocker.sh` (Intel) or `./makedockerM1.sh` (Mac M1) in the project root directory. It will create the respecitve image:

- ghcr.io/HCL-TECH-SOFTWARE/mkdocs:latest (Intel)
- ghcr.io/HCL-TECH-SOFTWARE/mkdocs:m1 (Mac M1)

## Local useage

We presume you follow the convention to keep your documentation in the `/docs` directory
Navigate to your project root directory and run:

- `docker run --rm -it -p 8000:8000 -v ${PWD}:/docs ghcr.io/HCL-TECH-SOFTWARE/mkdocs:m1 $1 $2 $3` (Mac M1)
- `docker run --rm -it -p 8000:8000 -v ${PWD}:/docs ghcr.io/HCL-TECH-SOFTWARE/mkdocs:latest $1 $2 $3` (Intel / Linux)

Replace $1, $2 $3 with valid mkdocs commands ... or omit them

## Command files

Copy the `docs` or `docsM1` file to `~/bin` ( you have that in your path, isn't it?) and you can enjoy quick launch by typing `docs` in your project root directory.

## Sample MKDocs setup file

Check the `samples` directory for `mkdocs.yml`

YMMV
