-- 기존 데이터 삭제 (submit → answer → question → subject 순서)
DELETE
FROM dating_exam_submit;
DELETE
FROM dating_exam_answer;
DELETE
FROM dating_exam_question;
DELETE
FROM dating_exam_subject;

-- members 테이블의 is_dating_exam_submitted를 전체 false로 초기화
UPDATE members
SET is_dating_exam_submitted = FALSE;