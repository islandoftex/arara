################################################################
## Makefile for arara project
################################################################
## Definitions
################################################################
.SILENT:
SHELL := /bin/bash
.PHONY: all clean
################################################################
## Name definition
################################################################
DOCUMENATION = arara-usermanual
AUXFILES = aux cod log out tmp toc 
################################################################
## Colordefinition
################################################################
NO_COLOR    = \x1b[0m
OK_COLOR    = \x1b[32;01m
WARN_COLOR  = \x1b[33;01m
################################################################
## make help
################################################################
help:
	@echo 
	@echo -e "\t$(WARN_COLOR)The following definitions provided by this Makefile"
	@echo -e "$(OK_COLOR)\tmake pdf\t\t--\ttypesets the documenation"
	@echo -e "\tmake clean\t\t--\tremove all helpfiles"
	@echo -e "\tmake all\t\t--\trun pdf clean"
	@echo -e "\t$(WARN_COLOR)End help$(NO_COLOR)"

################################################################
## Compilation
################################################################
%.pdf: %.tex
	NAME=`basename $< .tex` ;\
	echo -e "\n\t$(ERROR_COLOR)Typesetting $$NAME$(NO_COLOR)" ;\
	pdflatex -draftmode -interaction=nonstopmode $< > /dev/null ;\
	if [ $$? = 0 ] ; then \
	  echo -e "\t$(OK_COLOR)compilation in draftmode without erros$(NO_COLOR)" ;\
	  echo -e "\t$(OK_COLOR)Run PDFLaTeX again on $$NAME.tex$(NO_COLOR)" ;\
	  pdflatex -interaction=nonstopmode $< > /dev/null ;\
	else \
	  echo -e "\t$(ERROR_COLOR)compilation in draftmode with erros$(NO_COLOR)" ;\
	  exit 0;\
	fi ;\
	echo -e "\t$(OK_COLOR)Typesetting $$NAME finished $(NO_COLOR)\n" ;\

pdf: $(DOCUMENATION).pdf

clean:  
	echo -e "\n\t$(ERROR_COLOR)Start removing help files$(NO_COLOR)" ;\
	for I in $(FILELIST) ;\
	do \
	  for J in $(AUXFILES) ;\
	  do \
	    rm -rf $$I.$$J ;\
	  done ;\
	done ;\
	echo -e "\t$(OK_COLOR)Removing finished$(NO_COLOR)\n" ;\

all:	pdf clean

################################################################
## End input
################################################################make 
