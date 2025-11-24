# 1. Clean up old bindings to be safe
rm -rf src/main/java/com/example/ffmpeg/generated

# 2. Define the Apple Silicon path
INCLUDE_PATH="/opt/homebrew/include"
OUTPUT_DIR="src/main/java"
PACKAGE="com.example.ffmpeg.generated"

echo "Generating bindings from $INCLUDE_PATH..."

# 3. Run Jextract
# Updated command without library flags
jextract \
  --output $OUTPUT_DIR \
  --target-package $PACKAGE \
  -I $INCLUDE_PATH \
  --header-class-name FFmpeg \
  $INCLUDE_PATH/libavformat/avformat.h \
  $INCLUDE_PATH/libavcodec/avcodec.h \
  $INCLUDE_PATH/libavutil/avutil.h \
  $INCLUDE_PATH/libavutil/imgutils.h \
  $INCLUDE_PATH/libswscale/swscale.h \
  $INCLUDE_PATH/libavfilter/avfilter.h \
  $INCLUDE_PATH/libavfilter/buffersrc.h \
  $INCLUDE_PATH/libavfilter/buffersink.h