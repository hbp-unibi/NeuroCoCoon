WEBAPP_JAR = ncc.jar
CHEERPJFY = /MyData/SWDevelop/cheerpj/cheerpj_1.4/cheerpjfy.py

update: $(WEBAPP_JAR).js

run: update
	lsof -i :4443 || { ./runserver & }

%.jar.js: %.jar
	$(CHEERPJFY) $<
