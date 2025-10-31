#!/bin/bash

# Create output directory
echo "Creating output directory..."
mkdir -p bin

# Clean old class files
echo "Cleaning old files..."
rm -f bin/*.class

# Compile
echo "Compiling..."
javac -d bin src/*.java

# Check compilation
if [ $? -eq 0 ]; then
    echo "Compilation successful!"
    echo "Running CodeBlue..."
    java -cp bin CodeBlue
else
    echo "Compilation failed!"
    exit 1
fi