#!/bin/sh
set -eu
cd "$(dirname "$0")/.."
: "${OPENCV_DESKTOP_JAR:?Set this to the OpenPnP OpenCV 4.9.0-0 desktop jar}"
mkdir -p build/benchmark-old build/benchmark-new
common="app/src/main/java/com/pratik/imagetap/TapTiming.java app/src/main/java/com/pratik/imagetap/RegionRules.java tests/HybridBenchmark.java"
javac -cp "$OPENCV_DESKTOP_JAR" -d build/benchmark-old tests/benchmark-reference/VisionMatcher.java $common
javac -cp "$OPENCV_DESKTOP_JAR" -d build/benchmark-new app/src/main/java/com/pratik/imagetap/VisionMatcher.java $common
java -cp "$OPENCV_DESKTOP_JAR:build/benchmark-old" com.pratik.imagetap.HybridBenchmark legacy
java -cp "$OPENCV_DESKTOP_JAR:build/benchmark-new" com.pratik.imagetap.HybridBenchmark
