FROM squidfunk/mkdocs-material
LABEL org.opencontainers.image.source="https://github.com/HCL-TECH-SOFTWARE/hcl-mkdocs-build-image"
RUN apk update && apk upgrade && apk add git
RUN pip install mkdocs-awesome-pages-plugin mkdocs-git-revision-date-localized-plugin mike mkdocs-markdownextradata-plugin mkdocs-git-authors-plugin mkdocs-blog-plugin mkdocs-section-index mkdocs-macros-plugin
EXPOSE 8000
