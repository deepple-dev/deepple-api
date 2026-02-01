package deepple.deepple.member.command.domain.member;

import deepple.deepple.member.command.domain.member.exception.InvalidMemberEnumValueException;
import lombok.Getter;

@Getter
public enum District {
    // 서울특별시
    GANGNAM_GU(City.SEOUL, "강남구"),
    GANGDONG_GU(City.SEOUL, "강동구"),
    GANGBUK_GU(City.SEOUL, "강북구"),
    GANGSEO_GU(City.SEOUL, "강서구"),
    GWANAK_GU(City.SEOUL, "관악구"),
    GWANGJIN_GU(City.SEOUL, "광진구"),
    GURO_GU(City.SEOUL, "구로구"),
    GEUMCHEON_GU(City.SEOUL, "금천구"),
    NOWON_GU(City.SEOUL, "노원구"),
    DOBONG_GU(City.SEOUL, "도봉구"),
    DONGDAEMUN_GU(City.SEOUL, "동대문구"),
    DONGJAK_GU(City.SEOUL, "동작구"),
    MAPO_GU(City.SEOUL, "마포구"),
    SEODAEMUN_GU(City.SEOUL, "서대문구"),
    SEOCHO_GU(City.SEOUL, "서초구"),
    SEONGDONG_GU(City.SEOUL, "성동구"),
    SEONGBUK_GU(City.SEOUL, "성북구"),
    SONGPA_GU(City.SEOUL, "송파구"),
    YANGCHEON_GU(City.SEOUL, "양천구"),
    YEONGDEUNGPO_GU(City.SEOUL, "영등포구"),
    YONGSAN_GU(City.SEOUL, "용산구"),
    EUNPYEONG_GU(City.SEOUL, "은평구"),
    JONGNO_GU(City.SEOUL, "종로구"),
    JUNG_GU(City.SEOUL, "중구"),
    JUNGRANG_GU(City.SEOUL, "중랑구"),

    // 인천광역시
    GANGHWA_GUN(City.INCHEON, "강화군"),
    GYEYANG_GU(City.INCHEON, "계양구"),
    NAMDONG_GU(City.INCHEON, "남동구"),
    DONG_GU_INCHEON(City.INCHEON, "동구"),
    MICHUHOL_GU(City.INCHEON, "미추홀구"),
    BUPYEONG_GU(City.INCHEON, "부평구"),
    SEO_GU_INCHEON(City.INCHEON, "서구"),
    YEONSU_GU(City.INCHEON, "연수구"),
    ONGJIN_GUN(City.INCHEON, "옹진군"),
    JUNG_GU_INCHEON(City.INCHEON, "중구"),

    // 부산광역시
    GANGSEO_GU_BUSAN(City.BUSAN, "강서구"),
    GEUMJEONG_GU(City.BUSAN, "금정구"),
    GIJANG_GUN(City.BUSAN, "기장군"),
    NAM_GU_BUSAN(City.BUSAN, "남구"),
    DONG_GU_BUSAN(City.BUSAN, "동구"),
    DONGNAE_GU(City.BUSAN, "동래구"),
    BUSANJIN_GU(City.BUSAN, "부산진구"),
    BUK_GU_BUSAN(City.BUSAN, "북구"),
    SASANG_GU(City.BUSAN, "사상구"),
    SAHA_GU(City.BUSAN, "사하구"),
    SEO_GU_BUSAN(City.BUSAN, "서구"),
    SUYEONG_GU(City.BUSAN, "수영구"),
    YEONJE_GU(City.BUSAN, "연제구"),
    YEONGDO_GU(City.BUSAN, "영도구"),
    JUNG_GU_BUSAN(City.BUSAN, "중구"),
    HAEUNDAE_GU(City.BUSAN, "해운대구"),

    // 대전광역시
    DAEDEOK_GU(City.DAEJEON, "대덕구"),
    DONG_GU_DAEJEON(City.DAEJEON, "동구"),
    SEO_GU_DAEJEON(City.DAEJEON, "서구"),
    YUSEONG_GU(City.DAEJEON, "유성구"),
    JUNG_GU_DAEJEON(City.DAEJEON, "중구"),

    // 대구광역시
    NAM_GU_DAEGU(City.DAEGU, "남구"),
    DALSEO_GU(City.DAEGU, "달서구"),
    DALSEONG_GUN(City.DAEGU, "달성군"),
    DONG_GU_DAEGU(City.DAEGU, "동구"),
    BUK_GU_DAEGU(City.DAEGU, "북구"),
    SEO_GU_DAEGU(City.DAEGU, "서구"),
    SUSEONG_GU(City.DAEGU, "수성구"),
    JUNG_GU_DAEGU(City.DAEGU, "중구"),

    // 광주광역시
    GWANGSAN_GU(City.GWANGJU, "광산구"),
    NAM_GU_GWANGJU(City.GWANGJU, "남구"),
    DONG_GU_GWANGJU(City.GWANGJU, "동구"),
    BUK_GU_GWANGJU(City.GWANGJU, "북구"),
    SEO_GU_GWANGJU(City.GWANGJU, "서구"),

    // 울산광역시
    NAM_GU_ULSAN(City.ULSAN, "남구"),
    DONG_GU_ULSAN(City.ULSAN, "동구"),
    BUK_GU_ULSAN(City.ULSAN, "북구"),
    ULJU_GUN(City.ULSAN, "울주군"),
    JUNG_GU_ULSAN(City.ULSAN, "중구"),

    // 세종특별자치시
    SEJONG(City.SEJONG, "세종특별자치시"),

    // 강원도
    GANGNEUNG_SI(City.GANGWON, "강릉시"),
    GOSEONG_GUN(City.GANGWON, "고성군"),
    DONGHAE_SI(City.GANGWON, "동해시"),
    SAMCHEOK_SI(City.GANGWON, "삼척시"),
    SOKCHO_SI(City.GANGWON, "속초시"),
    YANGGU_GUN(City.GANGWON, "양구군"),
    YANGYANG_GUN(City.GANGWON, "양양군"),
    YEONGWOL_GUN(City.GANGWON, "영월군"),
    WONJU_SI(City.GANGWON, "원주시"),
    INJE_GUN(City.GANGWON, "인제군"),
    JEONGSEON_GUN(City.GANGWON, "정선군"),
    CHEORWON_GUN(City.GANGWON, "철원군"),
    CHUNCHEON_SI(City.GANGWON, "춘천시"),
    PYEONGCHANG_GUN(City.GANGWON, "평창군"),
    HONGCHEON_GUN(City.GANGWON, "홍천군"),
    HWACHEON_GUN(City.GANGWON, "화천군"),
    HOENGSEONG_GUN(City.GANGWON, "횡성군"),

    // 경기도
    GAPYEONG_GUN(City.GYEONGGI, "가평군"),
    GOYANG_SI(City.GYEONGGI, "고양시"),
    GWACHEON_SI(City.GYEONGGI, "과천시"),
    GWANGMYEONG_SI(City.GYEONGGI, "광명시"),
    GWANGJU_SI(City.GYEONGGI, "광주시"),
    GURI_SI(City.GYEONGGI, "구리시"),
    GUNPO_SI(City.GYEONGGI, "군포시"),
    GIMPO_SI(City.GYEONGGI, "김포시"),
    NAMYANGJU_SI(City.GYEONGGI, "남양주시"),
    DONGDUCHEON_SI(City.GYEONGGI, "동두천시"),
    BUCHEON_SI(City.GYEONGGI, "부천시"),
    SEONGNAM_SI(City.GYEONGGI, "성남시"),
    SUWON_SI(City.GYEONGGI, "수원시"),
    SIHEUNG_SI(City.GYEONGGI, "시흥시"),
    ANSAN_SI(City.GYEONGGI, "안산시"),
    ANSEONG_SI(City.GYEONGGI, "안성시"),
    ANYANG_SI(City.GYEONGGI, "안양시"),
    YANGJU_SI(City.GYEONGGI, "양주시"),
    YANGPYEONG_GUN(City.GYEONGGI, "양평군"),
    YEOJU_SI(City.GYEONGGI, "여주시"),
    YEONCHEON_GUN(City.GYEONGGI, "연천군"),
    OSAN_SI(City.GYEONGGI, "오산시"),
    YONGIN_SI(City.GYEONGGI, "용인시"),
    UIWANG_SI(City.GYEONGGI, "의왕시"),
    UIJUNGBU_SI(City.GYEONGGI, "의정부시"),
    ICHEON_SI(City.GYEONGGI, "이천시"),
    PAJU_SI(City.GYEONGGI, "파주시"),
    PYEONGTAEK_SI(City.GYEONGGI, "평택시"),
    POCHON_SI(City.GYEONGGI, "포천시"),
    HANAM_SI(City.GYEONGGI, "하남시"),
    HWASEONG_SI(City.GYEONGGI, "화성시"),

    // 경상남도
    GEOJE_SI(City.GYEONGSANGNAM, "거제시"),
    GEOCHANG_GUN(City.GYEONGSANGNAM, "거창군"),
    GOSEONG_GUN_GYEONGSANGNAM(City.GYEONGSANGNAM, "고성군"),
    GIMHAE_SI(City.GYEONGSANGNAM, "김해시"),
    NAMHAE_GUN(City.GYEONGSANGNAM, "남해군"),
    MILYANG_SI(City.GYEONGSANGNAM, "밀양시"),
    SACHEON_SI(City.GYEONGSANGNAM, "사천시"),
    SANCHANG_GUN(City.GYEONGSANGNAM, "산청군"),
    YANGSAN_SI(City.GYEONGSANGNAM, "양산시"),
    UIRYEONG_GUN(City.GYEONGSANGNAM, "의령군"),
    JINJU_SI(City.GYEONGSANGNAM, "진주시"),
    CHANGNYEONG_GUN(City.GYEONGSANGNAM, "창녕군"),
    CHANGWON_SI(City.GYEONGSANGNAM, "창원시"),
    TONGYEONG_SI(City.GYEONGSANGNAM, "통영시"),
    HADONG_GUN(City.GYEONGSANGNAM, "하동군"),
    HAMAN_GUN(City.GYEONGSANGNAM, "함안군"),
    HAMYANG_GUN(City.GYEONGSANGNAM, "함양군"),
    HAPCHEON_GUN(City.GYEONGSANGNAM, "합천군"),

    // 경상북도
    GORYEONG_GUN(City.GYEONGSANGBUK, "고령군"),
    GYEONGSAN_SI(City.GYEONGSANGBUK, "경산시"),
    GYEONGJU_SI(City.GYEONGSANGBUK, "경주시"),
    GIMCHEON_SI(City.GYEONGSANGBUK, "김천시"),
    ANDONG_SI(City.GYEONGSANGBUK, "안동시"),
    GUMI_SI(City.GYEONGSANGBUK, "구미시"),
    GUNWI_GUN(City.GYEONGSANGBUK, "군위군"),
    MUNGYEONG_SI(City.GYEONGSANGBUK, "문경시"),
    BONGHWA_GUN(City.GYEONGSANGBUK, "봉화군"),
    SANGJU_SI(City.GYEONGSANGBUK, "상주시"),
    SEONGJU_GUN(City.GYEONGSANGBUK, "성주군"),
    YEONGJU_SI(City.GYEONGSANGBUK, "영주시"),
    YEONGCHEON_SI(City.GYEONGSANGBUK, "영천시"),
    ULJIN_GUN(City.GYEONGSANGBUK, "울진군"),
    ULLUNG_GUN(City.GYEONGSANGBUK, "울릉군"),
    UISEONG_GUN(City.GYEONGSANGBUK, "의성군"),
    YEONGYANG_GUN(City.GYEONGSANGBUK, "영양군"),
    YEONGDEOK_GUN(City.GYEONGSANGBUK, "영덕군"),
    CHEONGSONG_GUN(City.GYEONGSANGBUK, "청송군"),
    CHEONGDO_GUN(City.GYEONGSANGBUK, "청도군"),
    CHILGOK_GUN(City.GYEONGSANGBUK, "칠곡군"),
    YECHON_GUN(City.GYEONGSANGBUK, "예천군"),
    POHANG_SI(City.GYEONGSANGBUK, "포항시"),

    // 충청남도
    GYERYONG_SI(City.CHUNGCHEONGNAM, "계룡시"),
    GONGJU_SI(City.CHUNGCHEONGNAM, "공주시"),
    GEUMSAN_GUN(City.CHUNGCHEONGNAM, "금산군"),
    NONSAN_SI(City.CHUNGCHEONGNAM, "논산시"),
    DANGJIN_SI(City.CHUNGCHEONGNAM, "당진시"),
    BORYEONG_SI(City.CHUNGCHEONGNAM, "보령시"),
    BUYEO_GUN(City.CHUNGCHEONGNAM, "부여군"),
    SEOSAN_SI(City.CHUNGCHEONGNAM, "서산시"),
    SECHEON_GUN(City.CHUNGCHEONGNAM, "서천군"),
    ASAN_SI(City.CHUNGCHEONGNAM, "아산시"),
    YESAN_GUN(City.CHUNGCHEONGNAM, "예산군"),
    CHEONAN_SI(City.CHUNGCHEONGNAM, "천안시"),
    CHEONGYANG_GUN(City.CHUNGCHEONGNAM, "청양군"),
    TAEAN_GUN(City.CHUNGCHEONGNAM, "태안군"),
    HONGSEONG_GUN(City.CHUNGCHEONGNAM, "홍성군"),
    // 충청북도
    GOESAN_GUN(City.CHUNGCHEONGBUK, "괴산군"),
    DANYANG_GUN(City.CHUNGCHEONGBUK, "단양군"),
    BOEUN_GUN(City.CHUNGCHEONGBUK, "보은군"),
    YEONGDONG_GUN(City.CHUNGCHEONGBUK, "영동군"),
    OKCHEON_GUN(City.CHUNGCHEONGBUK, "옥천군"),
    EUMSEONG_GUN(City.CHUNGCHEONGBUK, "음성군"),
    JECHEON_SI(City.CHUNGCHEONGBUK, "제천시"),
    JEUNGPYEONG_GUN(City.CHUNGCHEONGBUK, "증평군"),
    JINCHEON_GUN(City.CHUNGCHEONGBUK, "진천군"),
    CHEONGJU_SI(City.CHUNGCHEONGBUK, "청주시"),
    CHUNGJU_SI(City.CHUNGCHEONGBUK, "충주시"),

    // 전라남도
    GANGJIN_GUN(City.JEOLLANAM, "강진군"),
    GOHEUNG_GUN(City.JEOLLANAM, "고흥군"),
    GOKSEONG_GUN(City.JEOLLANAM, "곡성군"),
    GWANGYANG_SI(City.JEOLLANAM, "광양시"),
    GURYE_GUN(City.JEOLLANAM, "구례군"),
    NAJU_SI(City.JEOLLANAM, "나주시"),
    DAMYANG_GUN(City.JEOLLANAM, "담양군"),
    MOKPO_SI(City.JEOLLANAM, "목포시"),
    MUAN_GUN(City.JEOLLANAM, "무안군"),
    BOSEONG_GUN(City.JEOLLANAM, "보성군"),
    SUNCHEON_SI(City.JEOLLANAM, "순천시"),
    SHINAN_GUN(City.JEOLLANAM, "신안군"),
    YEOSU_SI(City.JEOLLANAM, "여수시"),
    YEONGGWANG_GUN(City.JEOLLANAM, "영광군"),
    YEONGAM_GUN(City.JEOLLANAM, "영암군"),
    WANDO_GUN(City.JEOLLANAM, "완도군"),
    JANGSEONG_GUN(City.JEOLLANAM, "장성군"),
    JANGHEUNG_GUN(City.JEOLLANAM, "장흥군"),
    JINDO_GUN(City.JEOLLANAM, "진도군"),
    HAMPYEONG_GUN(City.JEOLLANAM, "함평군"),
    HAENAM_GUN(City.JEOLLANAM, "해남군"),
    HWASUN_GUN(City.JEOLLANAM, "화순군"),

    // 전라북도
    GOCHANG_GUN(City.JEOLLABUK, "고창군"),
    GUNSAN_SI(City.JEOLLABUK, "군산시"),
    GIMJE_SI(City.JEOLLABUK, "김제시"),
    NAMWON_SI(City.JEOLLABUK, "남원시"),
    MUJU_GUN(City.JEOLLABUK, "무주군"),
    BUAN_GUN(City.JEOLLABUK, "부안군"),
    SUNCHANG_GUN(City.JEOLLABUK, "순창군"),
    WANJU_GUN(City.JEOLLABUK, "완주군"),
    IKSAN_SI(City.JEOLLABUK, "익산시"),
    IMSIL_GUN(City.JEOLLABUK, "임실군"),
    JANGSU_GUN(City.JEOLLABUK, "장수군"),
    JEONJU_SI(City.JEOLLABUK, "전주시"),
    JEONGEUP_SI(City.JEOLLABUK, "정읍시"),
    JINAN_GUN(City.JEOLLABUK, "진안군"),

    // 제주도
    JEJU_SI(City.JEJU, "제주시"),
    SEOGWIPO_SI(City.JEJU, "서귀포시");


    private final City city;
    private final String description;

    District(City city, String description) {
        this.city = city;
        this.description = description;
    }

    public static District from(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return District.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidMemberEnumValueException(value);
        }
    }
}

