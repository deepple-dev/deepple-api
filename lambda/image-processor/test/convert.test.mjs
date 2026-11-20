import assert from 'node:assert/strict';
import { describe, it } from 'node:test';
import sharp from 'sharp';
import { convert } from '../convert.mjs';
import { PermanentImageError } from '../errors.mjs';

function solid(width, height) {
  return sharp({ create: { width, height, channels: 3, background: '#808080' } });
}

describe('convert', () => {
  it('EXIF 방향대로 돌리고 메타데이터 없이 WebP와 썸네일을 만든다', async () => {
    const input = await solid(400, 200)
      .jpeg()
      .withMetadata({ orientation: 6 })
      .withExif({ IFD0: { Artist: 'someone' } })
      .toBuffer();

    const { image, thumbnail } = await convert(input);

    const imageMeta = await sharp(image).metadata();
    assert.equal(imageMeta.format, 'webp');
    assert.equal(imageMeta.width, 200);
    assert.equal(imageMeta.height, 400);
    assert.equal(imageMeta.exif, undefined);
    assert.equal(imageMeta.orientation, undefined);
    const thumbnailMeta = await sharp(thumbnail).metadata();
    assert.equal(thumbnailMeta.format, 'webp');
    assert.equal(thumbnailMeta.width, 200);
  });

  it('큰 이미지는 긴 변 2048px, 썸네일은 너비 360px로 줄인다', async () => {
    const input = await solid(4000, 3000).png().toBuffer();

    const { image, thumbnail } = await convert(input);

    const imageMeta = await sharp(image).metadata();
    assert.deepEqual([imageMeta.width, imageMeta.height], [2048, 1536]);
    const thumbnailMeta = await sharp(thumbnail).metadata();
    assert.deepEqual([thumbnailMeta.width, thumbnailMeta.height], [360, 270]);
  });

  it('픽셀 수가 너무 많으면 영구 실패로 본다', async () => {
    const input = await solid(7000, 7000).png().toBuffer();

    await assert.rejects(convert(input), PermanentImageError);
  });

  it('이미지가 아니면 영구 실패로 본다', async () => {
    await assert.rejects(convert(Buffer.from('not an image')), PermanentImageError);
  });

  it('지원하지 않는 형식은 영구 실패로 본다', async () => {
    const input = await solid(10, 10).gif().toBuffer();

    await assert.rejects(convert(input), PermanentImageError);
  });
});
