import assert from 'node:assert/strict';
import { describe, it } from 'node:test';
import { decodeS3Key, outputKeys } from '../keys.mjs';

describe('outputKeys', () => {
  it('원본 경로를 변환본 경로로 바꾼다', () => {
    assert.deepEqual(outputKeys('tmp/members/1/abc', 'tmp/', 'images/'), {
      imageKey: 'images/members/1/abc.webp',
      thumbnailKey: 'images/members/1/abc-thumbnail.webp',
    });
  });

  it('원본 경로가 아니면 거절한다', () => {
    assert.throws(() => outputKeys('images/members/1/abc', 'tmp/', 'images/'));
  });
});

describe('decodeS3Key', () => {
  it('S3 이벤트의 URL 인코딩을 푼다', () => {
    assert.equal(decodeS3Key('tmp/a+b%2Fc'), 'tmp/a b/c');
  });
});
