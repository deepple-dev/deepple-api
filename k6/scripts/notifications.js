import http from 'k6/http';
import {check, sleep} from 'k6';
import {config} from './lib/config.js';
import {authHeaders, login} from './lib/auth.js';

const MAX_VUS = 100;
const MEMBER_COUNT = 1000000;

export const options = {
    scenarios: {
        notifications_test: {
            executor: 'ramping-arrival-rate',
            startRate: 0,
            timeUnit: '1s',
            preAllocatedVUs: MAX_VUS,
            maxVUs: MAX_VUS,
            stages: [
                {duration: '1m', target: 50},
                {duration: '1m', target: 100},
                {duration: '1m', target: 150},
                {duration: '1m', target: 200},
                {duration: '1m', target: 250},
            ],
        },
    },
    thresholds: {
        http_req_duration: ['p(95)<500', 'p(99)<1000'],
        http_req_failed: ['rate<0.01'],
    },
};

function getReceiverId(senderId) {
    const isMale = senderId % 2 === 1;
    const senderIndex = Math.floor((senderId - 1) / 2);
    const targetIndex = (senderIndex + 1) % (MEMBER_COUNT / 2);
    return isMale ? (targetIndex + 1) * 2 : targetIndex * 2 + 1;
}

export function setup() {
    console.log(`Setup: ${MAX_VUS}명 토큰 획득 시작`);

    const tokens = [];
    for (let i = 0; i < MAX_VUS; i++) {
        const memberId = i + 1;
        const result = login(memberId);
        if (result) tokens.push(result);
    }

    console.log(`Setup 완료: ${tokens.length}개 토큰 획득`);
    return {tokens};
}

export default function (data) {
    const {tokens} = data;

    if (!tokens.length || __VU > tokens.length) {
        return;
    }

    const sender = tokens[__VU - 1];
    const receiverId = getReceiverId(sender.memberId);

    const res = http.post(
        `${config.baseUrl}/notifications/test?receiverId=${receiverId}`,
        null,
        {headers: authHeaders(sender.accessToken)}
    );

    check(res, {
        'status 200': (r) => r.status === 200,
    });

    sleep(Math.random() * 0.3 + 0.1);
}