/**
 * 회원 프로필 조회 API 테스트
 *
 * 테스트 대상:
 * - GET /member/{memberId}
 *
 * 실행:
 *   k6 run k6/api/member-profile.js
 *   k6 run -e BASE_URL=https://your-api.com k6/api/member-profile.js
 */

import http from 'k6/http';
import {check, sleep} from 'k6';
import {config, loadProfiles, thresholds} from '../config.js';
import {authHeaders, generateTestPhoneNumber, testLogin} from '../lib/auth.js';

const TEST_TYPE = __ENV.TEST_TYPE || 'smoke';

export const options = {
    thresholds,
    scenarios: {
        member_profile: {
            executor: 'ramping-vus',
            startVUs: 0,
            stages: loadProfiles[TEST_TYPE].stages || [
                {duration: loadProfiles[TEST_TYPE].duration, target: loadProfiles[TEST_TYPE].vus},
            ],
            gracefulRampDown: '30s',
        },
    },
};

let vuTokens = {};

export function setup() {
    console.log(`=== Member Profile API Test ===`);
    console.log(`Target: ${config.baseUrl}`);
    console.log(`Test Type: ${TEST_TYPE}`);
    return {};
}

export default function () {
    const vuId = __VU;

    // VU별 토큰 캐싱 (매 iteration마다 로그인 안 함)
    if (!vuTokens[vuId]) {
        const phoneNumber = generateTestPhoneNumber(vuId);
        console.log(`VU ${vuId}: 로그인 시도 - ${phoneNumber}`);
        const accessToken = testLogin(phoneNumber);
        if (!accessToken) {
            console.log(`VU ${vuId}: 로그인 실패`);
            sleep(1);
            return;
        }
        console.log(`VU ${vuId}: 로그인 성공`);
        vuTokens[vuId] = accessToken;
    }

    const headers = authHeaders(vuTokens[vuId]);

    // 타 회원 프로필 조회 (테스트 환경에 맞게 ID 수정)
    const targetIds = [3, 4, 5];
    const targetId = targetIds[Math.floor(Math.random() * targetIds.length)];
    const res = http.get(`${config.baseUrl}/member/${targetId}`, {
        headers,
        tags: {name: 'memberProfile'},
    });

    if (res.status !== 200 && res.status !== 404) {
        console.log(`회원 ${targetId} 조회 실패: ${res.status} - ${res.body}`);
    }

    check(res, {
        'status is 200 or 404': (r) => r.status === 200 || r.status === 404,
    });

    sleep(0.5);
}

export function teardown() {
    console.log('=== Member Profile API Test Completed ===');
}
