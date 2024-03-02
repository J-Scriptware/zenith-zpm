# Zenith - ZPM Interpreter

## Introduction
Zenith is a lightweight and efficient interpreter for ZPM (Z+-) (Zenith Programming Model) code. Zenith's capabilities and design make it an efficient and reliable tool for executing ZPM scripts effectively.

## Origins
Zenith is a ZPM language interpreter I developed for my CSE465 course. It adheres closely to provided specifications, showcasing the design and implementation principles of language interpreters. The project employs Java to transform theoretical parsing and interpretation concepts into a working software system. Zenith's development highlights the intersection of theoretical concepts and practical software engineering.

## Features
Zenith's technical framework is designed for robust ZPM script execution, emphasizing:

- **Variable Types**: Directly supports integer and string variables while emphasizing intuitive data manipulation and interaction within scripts.

- **Arithmetic and Logical Operations**: Supports arithmetic (+=, -=, *=) on integers, with support for string concatenation.

- **For Loops**: Implements for loop constructs. This capability supports iterative execution over blocks of code, enhancing script logic by allowing the execution of repeating tasks.

- **Error Diagnostics**: Offers comprehensive error reporting for both syntax and runtime issues. Unrecognized operations, type mismatches, unknown variables or incorrect loop syntax - all such errors are gracefully handled and conveyed to the user, assisting in efficient troubleshooting and correction.

- **Efficient Execution**: Zenith maintains high efficiency across operations. Its design ensures rapid parsing and execution of ZPM scripts, allowing for quick and responsive performance.

- **Dynamic Variable Management**: Advanced handling of integer and string variables is a primary focus of Zenith, supporting on-the-fly creation, assignment, and modification of variables.

## Requirements
- Java 11 or above.

## Installation
To compile Zenith, follow these steps:
1. Clone the Zenith repository to your local machine.
2. Navigate to the Zenith directory.
3. Compile the Zenith source code: `javac Zpm.java` or `javac *.java`

## Usage
To run a ZPM script using Zenith: `java Zpm prog.zpm`

## Academic Integrity Warning
Please note that Zenith was created as part of a coursework requirement for CSE465. While this project is publicly shared for educational and demonstrative purposes, I strongly advise against copying it, or any part of it, for academic submissions by other students



