all: build_frontends copy_frontends build_install_framework

build_frontends:
	cd frontends && \
	yarn install && \
	yarn link && \
	yarn workspace commons build && \
	yarn workspaces foreach -A run build && \
	cd .. 

copy_frontends:
	rsync -a --delete frontends/apps/cockpit/dist/assets/ cockpit/framework/src/main/resources/frontend/assets/
	rsync -a --delete frontends/apps/devserver/dist/assets/ core/maven/src/main/resources/devserver/assets/
	rsync -a --delete frontends/apps/framework/dist/assets/ core/framework/src/main/resources/frontend/assets/
	rsync -a --delete frontends/apps/p13n/dist/assets/ p13n/framework/src/main/resources/frontend/assets/
	rsync -a --delete frontends/apps/valuehelp/dist/assets/ valuehelp/framework/src/main/resources/frontend/assets/

build_install_framework:
	mvn clean install