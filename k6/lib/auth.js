import http from 'k6/http';
import {check} from 'k6';
import {config, defaultHeaders} from '../config.js';

/**
 * 테스트 로그인 수행 (인증번호 불필요)
 */
export function testLogin(phoneNumber) {
    const url = `${config.baseUrl}/member/login/test`;
    const payload = JSON.stringify({phoneNumber});

    const res = http.post(url, payload, {
        headers: defaultHeaders,
        tags: {name: 'login'},
    });

    const success = check(res, {
        'login status is 200': (r) => r.status === 200,
        'login has accessToken': (r) => {
            try {
                const body = JSON.parse(r.body);
                return body.data && body.data.accessToken;
            } catch {
                return false;
            }
        },
    });

    if (!success) {
        console.error(`Login failed for ${phoneNumber}: ${res.status} ${res.body}`);
        return null;
    }

    const body = JSON.parse(res.body);
    return body.data.accessToken;
}

/**
 * 인증 헤더 생성
 */
export function authHeaders(accessToken) {
    return {
        ...defaultHeaders,
        'Authorization': `Bearer ${accessToken}`,
    };
}

/**
 * VU별 고유한 테스트 전화번호 생성
 */
export function generateTestPhoneNumber(vuId) {
    const suffix = String(vuId).padStart(4, '0');
    return `0109999${suffix}`;
}
