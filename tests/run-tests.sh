#!/bin/sh
set -eu
cd "$(dirname "$0")/.."
mkdir -p build/pure-tests
javac -d build/pure-tests app/src/main/java/com/pratik/imagetap/CropGeometry.java app/src/main/java/com/pratik/imagetap/TargetSchedule.java app/src/main/java/com/pratik/imagetap/TapTiming.java app/src/main/java/com/pratik/imagetap/RegionRules.java app/src/main/java/com/pratik/imagetap/DuplicateSimilarity.java app/src/main/java/com/pratik/imagetap/MergeStorage.java tests/CropGeometryTest.java tests/TargetScheduleTest.java tests/TapTimingTest.java tests/RegionRulesTest.java tests/DuplicateSimilarityTest.java tests/MergeStorageTest.java
for name in CropGeometry TargetSchedule TapTiming RegionRules DuplicateSimilarity MergeStorage; do
    java -cp build/pure-tests "com.pratik.imagetap.${name}Test"
done
if [ -n "${OPENCV_DESKTOP_JAR:-}" ]; then
    mkdir -p build/native-tests
    javac -cp "$OPENCV_DESKTOP_JAR" -d build/native-tests app/src/main/java/com/pratik/imagetap/TapTiming.java app/src/main/java/com/pratik/imagetap/RegionRules.java app/src/main/java/com/pratik/imagetap/VisionMatcher.java tests/VisionMatcherTest.java tests/FullFrameScanTest.java tests/HybridRegressionTest.java tests/HybridParityTest.java tests/BestMatchTest.java tests/FreshRecheckTest.java app/src/main/java/com/pratik/imagetap/ScanEngine.java tests/ParallelScanTest.java
    java -cp "$OPENCV_DESKTOP_JAR:build/native-tests" com.pratik.imagetap.VisionMatcherTest
    java -cp "$OPENCV_DESKTOP_JAR:build/native-tests" com.pratik.imagetap.FullFrameScanTest
    java -cp "$OPENCV_DESKTOP_JAR:build/native-tests" com.pratik.imagetap.HybridRegressionTest
    java -cp "$OPENCV_DESKTOP_JAR:build/native-tests" com.pratik.imagetap.BestMatchTest
    java -cp "$OPENCV_DESKTOP_JAR:build/native-tests" com.pratik.imagetap.FreshRecheckTest
    java -cp "$OPENCV_DESKTOP_JAR:build/native-tests" com.pratik.imagetap.ParallelScanTest
    java -cp "$OPENCV_DESKTOP_JAR:build/native-tests" com.pratik.imagetap.HybridParityTest
fi

javac -d build/pure-tests app/src/main/java/com/pratik/imagetap/TargetSchedule.java app/src/main/java/com/pratik/imagetap/CaptureGate.java tests/ReviewRegressionTest.java
java -cp build/pure-tests com.pratik.imagetap.ReviewRegressionTest
javac -d build/pure-tests app/src/main/java/com/pratik/imagetap/FrameOrder.java tests/FrameOrderTest.java
java -cp build/pure-tests com.pratik.imagetap.FrameOrderTest
