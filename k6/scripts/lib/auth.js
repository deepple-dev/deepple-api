import http from 'k6/http';
import {config} from './config.js';

export function login(memberId) {
    const phoneNumber = `010${String(memberId).padStart(8, '0')}`;

    const res = http.post(
        `${config.baseUrl}/member/login/test`,
        JSON.stringify({phoneNumber}),
        {headers: {'Content-Type': 'application/json'}}
    );

    if (res.status !== 200) {
        console.warn(`Login failed for memberId=${memberId}: ${res.status}`);
        return null;
    }

    const body = JSON.parse(res.body);
    return {
        memberId,
        accessToken: body.data.accessToken,
    };
}

export function authHeaders(token) {
    return {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`,
    };
}