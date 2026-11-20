import assert from 'node:assert/strict';
import { beforeEach, describe, it } from 'node:test';
import { PermanentImageError } from '../errors.mjs';
import { createHandler } from '../handler.mjs';

function s3Event(messageId, key) {
  return {
    messageId,
    body: JSON.stringify({ Records: [{ s3: { bucket: { name: 'bucket' }, object: { key } } }] }),
  };
}

describe('handler', () => {
  let puts;
  let results;
  let s3;
  let sqs;

  beforeEach(() => {
    puts = [];
    results = [];
    s3 = {
      send: async (command) => {
        if (command.constructor.name === 'GetObjectCommand') {
          return { Body: { transformToByteArray: async () => new Uint8Array([1, 2, 3]) } };
        }
        puts.push(command.input);
        return {};
      },
    };
    sqs = {
      send: async (command) => {
        results.push(JSON.parse(command.input.MessageBody));
        return {};
      },
    };
  });

  function handlerWith(convert) {
    return createHandler({
      s3,
      sqs,
      convert,
      resultQueueUrl: 'queue',
      sourcePrefix: 'tmp/',
      outputPrefix: 'images/',
    });
  }

  it('변환본과 썸네일을 저장하고 완료 결과를 보낸다', async () => {
    const handler = handlerWith(async () => ({ image: Buffer.from('i'), thumbnail: Buffer.from('t') }));

    const response = await handler({ Records: [s3Event('m1', 'tmp/members/1/abc')] });

    assert.deepEqual(response, { batchItemFailures: [] });
    assert.deepEqual(
      puts.map((put) => put.Key),
      ['images/members/1/abc.webp', 'images/members/1/abc-thumbnail.webp'],
    );
    assert.deepEqual(results, [{ sourceKey: 'tmp/members/1/abc', status: 'COMPLETED', failureReason: null }]);
  });

  it('영구 실패는 실패 결과를 보내고 재시도하지 않는다', async () => {
    const handler = handlerWith(async () => {
      throw new PermanentImageError('이미지가 아닙니다');
    });

    const response = await handler({ Records: [s3Event('m1', 'tmp/members/1/abc')] });

    assert.deepEqual(response, { batchItemFailures: [] });
    assert.equal(puts.length, 0);
    assert.deepEqual(results, [
      { sourceKey: 'tmp/members/1/abc', status: 'FAILED', failureReason: '이미지가 아닙니다' },
    ]);
  });

  it('원본이 없으면 실패 결과를 보내고 재시도하지 않는다', async () => {
    s3.send = async () => {
      throw Object.assign(new Error('missing'), { name: 'NoSuchKey' });
    };
    const handler = handlerWith(async () => {
      throw new Error('호출되면 안 된다');
    });

    const response = await handler({ Records: [s3Event('m1', 'tmp/members/1/abc')] });

    assert.deepEqual(response, { batchItemFailures: [] });
    assert.deepEqual(results, [
      { sourceKey: 'tmp/members/1/abc', status: 'FAILED', failureReason: '원본이 없습니다' },
    ]);
  });

  it('일시적 실패는 그 메시지만 재시도한다', async () => {
    const handler = handlerWith(async () => {
      throw new Error('일시적 오류');
    });

    const response = await handler({ Records: [s3Event('m1', 'tmp/members/1/abc')] });

    assert.deepEqual(response, { batchItemFailures: [{ itemIdentifier: 'm1' }] });
    assert.equal(results.length, 0);
  });

  it('S3 테스트 이벤트는 건너뛴다', async () => {
    const handler = handlerWith(async () => assert.fail('호출되면 안 된다'));

    const response = await handler({
      Records: [{ messageId: 'm1', body: JSON.stringify({ Event: 's3:TestEvent' }) }],
    });

    assert.deepEqual(response, { batchItemFailures: [] });
  });
});
