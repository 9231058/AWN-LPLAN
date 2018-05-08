# In The Name Of God
# ========================================
# [] File Name : Makefile
#
# [] Creation Date : 15-01-2016
#
# [] Created By : Parham Alvani (parham.alvani@gmail.com)
# =======================================

.PHONY: all
all: awn-lplan

awn-lplan: main.tex
	pdflatex main.tex
