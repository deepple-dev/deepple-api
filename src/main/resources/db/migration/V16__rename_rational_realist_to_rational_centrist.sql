update dating_exam_answer
set personality_type = 'RATIONAL_CENTRIST'
where personality_type = 'RATIONAL_REALIST';

update dating_exam_submit_result
set dominant_personality_type = 'RATIONAL_CENTRIST'
where dominant_personality_type = 'RATIONAL_REALIST';

alter table dating_exam_submit_result
    rename column rational_realist_count to rational_centrist_count;
