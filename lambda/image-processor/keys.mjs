export function outputKeys(sourceKey, sourcePrefix, outputPrefix) {
  if (!sourceKey.startsWith(sourcePrefix)) {
    throw new Error(`원본 경로가 아닙니다: ${sourceKey}`);
  }
  const path = outputPrefix + sourceKey.slice(sourcePrefix.length);
  return {
    imageKey: `${path}.webp`,
    thumbnailKey: `${path}-thumbnail.webp`,
  };
}

export function decodeS3Key(key) {
  return decodeURIComponent(key.replace(/\+/g, ' '));
}
