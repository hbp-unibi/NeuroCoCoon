WEBAPP_JAR = ncc.jar
CHEERPJFY = /Applications/cheerpj/cheerpjfy.py
PUBLIC_WEB_DIR = ../../NeuroCoCoon_GitHub_Pages/NeuroCoCoon

help:
	@echo "update: build cheerpj JavaScript version of JAR as a prototype"
	@echo "run: update plus start local web server for prototype of web app"
	@echo "packjar, stripjar: like update, but build reduced versions of the"
	@echo "                   full Java archive file for faster web deployment"
	@echo ""
	@echo "public: update prototype and copy to checkout of GitHub pages branch"
	@echo "push: copy prototype, commit and push to GitHub pages branch;"
	@echo "      push does NOT trigger an update, but uses the current prototype"

update stripjar packjar: $(WEBAPP_JAR).js
stripjar: CHEERPJ_OPTS = --strip-jar=$*_strip.jar
packjar: CHEERPJ_OPTS = --pack-jar=$*_pack.jar

public: update copy

copy:
	cp -p $(WEBAPP_JAR) $(WEBAPP_JAR).js $(PUBLIC_WEB_DIR)
	sed -e 's:/app/:/app/NeuroCoCoon/:' ncc.html > $(PUBLIC_WEB_DIR)/index.html

push: copy
	( cd $(PUBLIC_WEB_DIR); \
	  git add $(WEBAPP_JAR) $(WEBAPP_JAR).js index.html; \
	  git commit -m "Updated to latest prototype version"; \
	  git push \
	)

run: update
	lsof -i :4443 || { ./runserver & }

%.jar.js: %.jar
	$(CHEERPJFY) $(CHEERPJ_OPTS) $<
