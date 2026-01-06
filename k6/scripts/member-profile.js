import http from 'k6/http';
import {check, sleep} from 'k6';
import {config} from './lib/config.js';
import {authHeaders, login} from './lib/auth.js';

const TARGET_RPS = parseInt(__ENV.TARGET_RPS) || 100;
const MAX_VUS = TARGET_RPS * 2;
const MEMBER_OFFSET = 400000;
const FEMALE_COUNT = 500000;
const INTROS_PER_MEMBER = 30;

export const options = {
    scenarios: {
        member_profile: {
            executor: 'ramping-arrival-rate',
            startRate: 0,
            timeUnit: '1s',
            preAllocatedVUs: MAX_VUS,
            maxVUs: MAX_VUS,
            stages: [
                {duration: '1m', target: Math.floor(TARGET_RPS * 0.1)},
                {duration: '1m', target: Math.floor(TARGET_RPS * 0.5)},
                {duration: '1m', target: TARGET_RPS},
                {duration: '1m', target: TARGET_RPS * 2},
                {duration: '1m', target: TARGET_RPS * 3},
            ],
        },
    },
    thresholds: {
        http_req_duration: ['p(95)<500', 'p(99)<1000'],
        http_req_failed: ['rate<0.01'],
    },
};

function getIntroducedFemaleId(maleId, introIndex) {
    return 2 * ((Math.floor((maleId - 1) / 2) + introIndex * 7) % FEMALE_COUNT + 1);
}

export function setup() {
    console.log(`Setup: ${MAX_VUS}명 토큰 획득 시작`);

    const tokens = [];
    for (let i = 0; i < MAX_VUS; i++) {
        const memberId = MEMBER_OFFSET + (i * 2) + 1;
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

    const requester = tokens[__VU - 1];
    const introIndex = (__ITER % INTROS_PER_MEMBER) + 1;
    const targetMemberId = getIntroducedFemaleId(requester.memberId, introIndex);

    const res = http.get(
        `${config.baseUrl}/member/${targetMemberId}`,
        {headers: authHeaders(requester.accessToken)}
    );

    check(res, {
        'status is 200': (r) => r.status === 200,
    });

    sleep(Math.random() * 0.3 + 0.1);
}