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

FROM squidfunk/mkdocs-material:latest
LABEL org.opencontainers.image.source="https://github.com/HCL-TECH-SOFTWARE/hcl-mkdocs-build-image"
LABEL org.opencontainers.image.description="Multi-Version MkDocs build image"
RUN apk update && apk upgrade && apk add --no-cache git openjdk17 bash
RUN pip install mkdocs-awesome-pages-plugin mkdocs-git-revision-date-localized-plugin mike mkdocs-markdownextradata-plugin mkdocs-git-authors-plugin mkdocs-blog-plugin mkdocs-section-index mkdocs-macros-plugin
COPY target/MkDocsVersions.jar /deployments/
# COPY src/main/docker/config.yml /deployments/config.yml
COPY --chmod=555 src/main/docker/entrypoint.sh /deployments/starthere.sh
ENV JAVA_APP_JAR="/deployments/MkDocsVersions.jar"
EXPOSE 8000
ENTRYPOINT [ "../deployments/starthere.sh" ]
