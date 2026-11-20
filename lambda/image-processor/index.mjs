import { S3Client } from '@aws-sdk/client-s3';
import { SQSClient } from '@aws-sdk/client-sqs';
import { convert } from './convert.mjs';
import { createHandler } from './handler.mjs';

export const handler = createHandler({
  s3: new S3Client({}),
  sqs: new SQSClient({}),
  convert,
  resultQueueUrl: process.env.RESULT_QUEUE_URL,
  sourcePrefix: process.env.SOURCE_PREFIX,
  outputPrefix: process.env.OUTPUT_PREFIX,
});
