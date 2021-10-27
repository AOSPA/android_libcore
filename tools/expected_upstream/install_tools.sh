#!/bin/bash

# prerequisite to run the script
pip3 install GitPython

git fetch aosp upstream-openjdk7u
git fetch aosp upstream-openjdk8u
git fetch aosp upstream-openjdk9
git fetch aosp upstream-openjdk11u

THIS_DIR=$(realpath $(dirname $BASH_SOURCE))
alias ojluni_refresh_files=${THIS_DIR}/ojluni_refresh_files.py
alias ojluni_modify_expectation=${THIS_DIR}/ojluni_modify_expectation.py


_ojluni_modify_expectation ()
{
  COMPREPLY=( $(ojluni_modify_expectation --autocomplete $COMP_CWORD ${COMP_WORDS[@]:1}))

  return 0
}

complete -o nospace -F _ojluni_modify_expectation ojluni_modify_expectation