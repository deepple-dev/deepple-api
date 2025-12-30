// k6 테스트 설정 파일
// 환경변수로 테스트 환경 전환 가능

export const config = {
    // 기본 URL 설정 (환경변수로 오버라이드 가능)
    baseUrl: __ENV.BASE_URL || 'http://localhost:8080',

};

// 성능 목표 (thresholds)
// p(95)<300 = 95% 요청이 300ms 이내
// rate<0.01 = 에러율 1% 미만
export const thresholds = {
    http_req_duration: ['p(95)<300'],
    http_req_failed: ['rate<0.01'],
};

// 시나리오별 부하 설정
export const loadProfiles = {
    smoke: {
        stages: [
            {duration: '5s', target: 1},   // 5초만에 1명 도달
            {duration: '25s', target: 1},  // 25초간 유지
        ],
    },
    load: {
        stages: [
            {duration: '1m', target: 50},
            {duration: '3m', target: 50},
            {duration: '1m', target: 0},
        ],
    },
    stress: {
        stages: [
            {duration: '2m', target: 100},
            {duration: '5m', target: 100},
            {duration: '2m', target: 200},
            {duration: '5m', target: 200},
            {duration: '2m', target: 0},
        ],
    },
    spike: {
        stages: [
            {duration: '10s', target: 10},
            {duration: '1m', target: 500},
            {duration: '10s', target: 10},
            {duration: '3m', target: 10},
            {duration: '10s', target: 0},
        ],
    },
    soak: {
        stages: [
            {duration: '5m', target: 100},
            {duration: '30m', target: 100},
            {duration: '5m', target: 0},
        ],
    },
};

// HTTP 헤더 기본값
export const defaultHeaders = {
    'Content-Type': 'application/json',
    'Accept': 'application/json',
};

