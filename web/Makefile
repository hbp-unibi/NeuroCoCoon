WEBAPP_JAR = ncc.jar
CHEERPJFY = /Applications/cheerpj/cheerpjfy.py

update pack: $(WEBAPP_JAR).js
pack: CHEERPJ_OPTS = --pack-jar=$*_pack.jar

run: update
	lsof -i :4443 || { ./runserver & }

%.jar.js: %.jar
	$(CHEERPJFY) $(CHEERPJ_OPTS) $<
