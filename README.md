# MkDocs HCL Container image

## Objective

Create and maintain a container (a.k.a. Docker) image having all [MKDocs Material](https://squidfunk.github.io/mkdocs-material/) customizations HCL Labs and friends found useful. It supports both local run and is used in the HCL internal Jenkins pipelines.

## Projects using the template

- [HCL Volt MX Go](https://github.com/HCL-TECH-SOFTWARE/voltmxgo-documentation)
- [HCL Domino REST API](https://github.com/HCL-TECH-SOFTWARE/Domino-rest-api/branches)

## Multi-Version build

The image now contains our [mkdocs-preprocessor](preporcessor.md) which allows to comfortably maintain documentation versions for software version without duplicating sources.

## Build result

The build creates 2 flavors:

- Intel for local run
- M1 (Apple) for local run

The resulting container is available in our [Github repository](https://github.com/HCL-TECH-SOFTWARE/domino-jnx/pkgs/container/mkdocs).

## MKDocs plugins used

- [mkdocs-awesome-pages-plugin](https://github.com/lukasgeiter/mkdocs-awesome-pages-plugin/)
- [mkdocs-git-revision-date-localized-plugin](https://github.com/timvink/mkdocs-git-revision-date-localized-plugin)
- [mike](https://github.com/jimporter/mike)
- [mkdocs-markdownextradata-plugin](https://github.com/rosscdh/mkdocs-markdownextradata-plugin)
- [mkdocs-git-authors-plugin](https://github.com/timvink/mkdocs-git-authors-plugin)

## Local building

The images are built by GitHub, so typically, you don't need to build them locally.

Run `./makedocker.sh` for Intel or `./makedockerM1.sh` for Mac M1 in the project root directory. It will create the respecitve images:

- For Intel: ghcr.io/hcl-tech-software/mkdocs:latest
- For Mac M1: ghcr.io/hcl-tech-software/mkdocs:m1

## Local usage

We presume you follow the convention to keep your documentation in the `/docs` directory.
Navigate to your project root directory and run:

- For Mac M1: `docker run --rm -it -p 8000:8000 -v ${PWD}:/docs ghcr.io/hcl-tech-software/mkdocs:m1 $1 $2 $3`
- For Intel / Linux: `docker run --rm -it -p 8000:8000 -v ${PWD}:/docs ghcr.io/hcl-tech-software/mkdocs:latest $1 $2 $3`

Replace $1, $2, $3 with valid mkdocs commands or omit them.

## Command files

Copy the `docs` or `docsM1` file to `~/bin` ( you have that in your path, isn't it?), and you can enjoy quick launch by typing `docs` in your project root directory.

## Sample MKDocs setup file

Check the `samples` directory for `mkdocs.yml`.

YMMV
