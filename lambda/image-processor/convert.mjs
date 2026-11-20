import sharp from 'sharp';
import { PermanentImageError } from './errors.mjs';

const SUPPORTED_FORMATS = new Set(['jpeg', 'png', 'webp']);
const MAX_INPUT_PIXELS = 40_000_000;
const IMAGE_MAX_SIZE = 2048;
const THUMBNAIL_WIDTH = 360;

export async function convert(input) {
  try {
    const { format } = await sharp(input, { limitInputPixels: MAX_INPUT_PIXELS }).metadata();
    if (!SUPPORTED_FORMATS.has(format)) {
      throw new PermanentImageError(`지원하지 않는 형식입니다: ${format}`);
    }
    const oriented = sharp(input, { limitInputPixels: MAX_INPUT_PIXELS, failOn: 'error' }).autoOrient();
    const [image, thumbnail] = await Promise.all([
      oriented
        .clone()
        .resize({ width: IMAGE_MAX_SIZE, height: IMAGE_MAX_SIZE, fit: 'inside', withoutEnlargement: true })
        .webp({ quality: 80 })
        .toBuffer(),
      oriented
        .clone()
        .resize({ width: THUMBNAIL_WIDTH, withoutEnlargement: true })
        .webp({ quality: 75 })
        .toBuffer(),
    ]);
    return { image, thumbnail };
  } catch (error) {
    if (error instanceof PermanentImageError) {
      throw error;
    }
    throw new PermanentImageError(`이미지를 변환할 수 없습니다: ${error.message}`, { cause: error });
  }
}
