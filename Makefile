all: build_frontends copy_frontends build_install_framework

build_frontends:
	cd frontends && \
	yarn install && \
	yarn link && \
	yarn workspace commons build && \
	yarn workspaces foreach -A run build && \
	cd .. 

copy_frontends:
	rm -rf cockpit/framework/src/main/resources/frontend/assets
	mkdir -p cockpit/framework/src/main/resources/frontend/assets
	cp -r frontends/apps/cockpit/dist/assets/ cockpit/framework/src/main/resources/frontend

	rm -rf core/maven/src/main/resources/devserver/assets
	mkdir -p core/maven/src/main/resources/devserver/assets
	cp -r frontends/apps/devserver/dist/assets/ core/maven/src/main/resources/devserver

	rm -rf core/framework/src/main/resources/frontend/assets
	mkdir -p core/framework/src/main/resources/frontend/assets
	cp -r frontends/apps/framework/dist/assets/ core/framework/src/main/resources/frontend
	rm -rf p13n/framework/src/main/resources/frontend/assets
	mkdir -p p13n/framework/src/main/resources/frontend/assets
	cp -r frontends/apps/p13n/dist/assets/ p13n/framework/src/main/resources/frontend

	rm -rf valuehelp/framework/src/main/resources/frontend/assets
	mkdir -p valuehelp/framework/src/main/resources/frontend/assets
	cp -r frontends/apps/valuehelp/dist/assets/ valuehelp/framework/src/main/resources/frontend

build_install_framework:
	mvn clean install