# Toorla-compiler
A comprehensive compiler for Toorla language, featuring code generation with detailed structure output, semantic analysis with symbol table and semantic error checking

## Overview

The Toorla Compiler is developed to for compiling Toorla source code, providing a process from code generation to semantic analysis and error checking.

## Features

1. **Code Generation :**
   - Takes Toorla code stored in a file as input.
   - Produces a structured output (`structure.png`) representing different code parts with details, including indentation.

2. **Semantic Analysis:**
   - Processes different scopes within the input program.
   - Generates a symbol table for each scope.
   - Prints symbol tables in order of the start line of each scope.

3. **Semantic Error Checking:**
   - Utilizes the symbol table to identify and report semantic errors.
   - Checks for reinitialization of methods, classes, and variables.
   - Detects inheritance errors, including cyclic inheritance.
  
## Usage
Execute `Compiler.java` located in `toorla/src/compiler/Compiler.java`.

**Grammar Specification:**
Toorla grammar is provided in `toorla/grammar/Toorla.g4`.

**Sample Inputs:**
Sample inputs are available in `toorla/sample/`.

