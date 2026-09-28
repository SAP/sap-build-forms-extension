all: build_frontends copy_frontends build_install_framework

# Check the operating system
ifeq ($(OS),Windows_NT)
    DETECTED_OS := Windows
	SHELL := C:/PROGRA~1/Git/usr/bin/bash.exe
	.SHELLFLAGS := --login -c
else
    DETECTED_OS := $(shell uname -s)
endif

# build the frontends with multiple steps (1. commons lib, 2. build all workspaces)
build_frontends:
	cd frontends && \
	yarn install && \
	yarn link && \
	yarn workspace commons build && \
	yarn workspaces foreach -A run build && \
	cd .. 

# copy the built frontends to the respective resources folders, this is OS dependent
copy_frontends:
ifeq ($(DETECTED_OS),Windows)
	$(SHELL) -c 'rm -rf cockpit/framework/src/main/resources/frontend/assets && \
		mkdir -p cockpit/framework/src/main/resources/frontend/assets && \
		cp -r frontends/apps/cockpit/dist/assets/ cockpit/framework/src/main/resources/frontend && \
		rm -rf core/maven/src/main/resources/devserver/assets && \
		mkdir -p core/maven/src/main/resources/devserver/assets && \
		cp -r frontends/apps/devserver/dist/assets/ core/maven/src/main/resources/devserver && \
		rm -rf core/framework/src/main/resources/frontend/assets && \
		mkdir -p core/framework/src/main/resources/frontend/assets && \
		cp -r frontends/apps/framework/dist/assets/ core/framework/src/main/resources/frontend && \
		rm -rf p13n/framework/src/main/resources/frontend/assets && \
		mkdir -p p13n/framework/src/main/resources/frontend/assets && \
		cp -r frontends/apps/p13n/dist/assets/ p13n/framework/src/main/resources/frontend && \
		rm -rf valuehelp/framework/src/main/resources/frontend/assets && \
		mkdir -p valuehelp/framework/src/main/resources/frontend/assets && \
		cp -r frontends/apps/valuehelp/dist/assets/ valuehelp/framework/src/main/resources/frontend'
else
	rsync -a --delete frontends/apps/cockpit/dist/assets/ cockpit/framework/src/main/resources/frontend/assets/
	rsync -a --delete frontends/apps/devserver/dist/assets/ core/maven/src/main/resources/devserver/assets/
	rsync -a --delete frontends/apps/framework/dist/assets/ core/framework/src/main/resources/frontend/assets/
	rsync -a --delete frontends/apps/p13n/dist/assets/ p13n/framework/src/main/resources/frontend/assets/
	rsync -a --delete frontends/apps/valuehelp/dist/assets/ valuehelp/framework/src/main/resources/frontend/assets/
endif

# build and install the framework to the local maven repository
build_install_framework:
	mvn clean install

release_version:
	mvn build-helper:parse-version versions:set -DnewVersion=${parsedVersion.majorVersion}.${parsedVersion.minorVersion}.${parsedVersion.incrementalVersion}
