# Zenith - ZPM Interpreter

## Introduction
Zenith is a lightweight and efficient interpreter for ZPM (Z+-) (Zenith Programming Model) code. Designed with simplicity and performance in mind, Zenith allows for easy execution of ZPM scripts, making it ideal for educational purposes and small to medium-scale scripting tasks.

## Origins
Zenith is a ZPM language interpreter I developed in my CSE465 course. It adheres closely to provided specifications, showcasing the design and implementation principles of language interpreters. The project employs Java to transform theoretical parsing and interpretation concepts into a working software system. Zenith's development highlights the intersection of theoretical concepts and practical software engineering.

## Features
Zenith's technical framework is designed for robust ZPM script execution, emphasizing:

- **Variable Types**: Directly supports integer and string variables, enabling diverse data manipulation and interaction within scripts.

- **Arithmetic and Logical Operations**: Facilitates arithmetic (+, -, *) on integers and concatenation for strings, alongside equality checks, with optimized performance for each operation type.

- **For Loops**: Implements for loop constructs, allowing iterative execution over blocks of code, enhancing script logic and repeatability.

- **Error Diagnostics**: Offers comprehensive error reporting for both syntax and runtime issues, aiding in efficient troubleshooting and correction.

- **Efficient Execution**: Engineered for performance, Zenith ensures rapid parsing and execution of ZPM scripts, maintaining high efficiency across operations.

- **Dynamic Variable Management**: Provides advanced variable handling capabilities, supporting on-the-fly creation, assignment, and modification of both integer and string variables.

## Requirements
- Java 11 or above.

## Installation
To compile Zenith, follow these steps:
1. Clone the Zenith repository to your local machine.
2. Navigate to the Zenith directory.
3. Compile the Zenith source code: `javac Zpm.java` or `javac *.java`

## Usage
To run a ZPM script using Zenith: `java Zpm prog.zpm`


