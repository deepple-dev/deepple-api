import { GetObjectCommand, PutObjectCommand } from '@aws-sdk/client-s3';
import { SendMessageCommand } from '@aws-sdk/client-sqs';
import { PermanentImageError } from './errors.mjs';
import { decodeS3Key, outputKeys } from './keys.mjs';

const WEBP = 'image/webp';
const CACHE_CONTROL = 'public, max-age=31536000, immutable';

export function createHandler({ s3, sqs, convert, resultQueueUrl, sourcePrefix, outputPrefix }) {
  async function sendResult(result) {
    await sqs.send(
      new SendMessageCommand({ QueueUrl: resultQueueUrl, MessageBody: JSON.stringify(result) }),
    );
  }

  async function put(bucket, key, body) {
    await s3.send(
      new PutObjectCommand({
        Bucket: bucket,
        Key: key,
        Body: body,
        ContentType: WEBP,
        CacheControl: CACHE_CONTROL,
      }),
    );
  }

  async function read(bucket, key) {
    try {
      const object = await s3.send(new GetObjectCommand({ Bucket: bucket, Key: key }));
      return Buffer.from(await object.Body.transformToByteArray());
    } catch (error) {
      if (error.name === 'NoSuchKey') {
        throw new PermanentImageError('원본이 없습니다', { cause: error });
      }
      throw error;
    }
  }

  async function processObject(bucket, sourceKey) {
    const { imageKey, thumbnailKey } = outputKeys(sourceKey, sourcePrefix, outputPrefix);

    let converted;
    try {
      converted = await convert(await read(bucket, sourceKey));
    } catch (error) {
      if (error instanceof PermanentImageError) {
        console.warn(JSON.stringify({ message: '이미지 변환 영구 실패', sourceKey, reason: error.message }));
        await sendResult({ sourceKey, status: 'FAILED', failureReason: error.message });
        return;
      }
      throw error;
    }

    await put(bucket, imageKey, converted.image);
    await put(bucket, thumbnailKey, converted.thumbnail);
    await sendResult({ sourceKey, status: 'COMPLETED', failureReason: null });
  }

  async function processRecord(record) {
    const body = JSON.parse(record.body);
    if (body.Event === 's3:TestEvent') {
      return;
    }
    for (const s3Record of body.Records ?? []) {
      await processObject(s3Record.s3.bucket.name, decodeS3Key(s3Record.s3.object.key));
    }
  }

  return async function handler(event) {
    const batchItemFailures = [];
    for (const record of event.Records) {
      try {
        await processRecord(record);
      } catch (error) {
        console.error(JSON.stringify({ message: '이미지 처리 실패', messageId: record.messageId, error: error.stack }));
        batchItemFailures.push({ itemIdentifier: record.messageId });
      }
    }
    return { batchItemFailures };
  };
}
