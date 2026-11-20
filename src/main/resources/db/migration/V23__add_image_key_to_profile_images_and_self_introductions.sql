alter table profile_images
    add column image_id  bigint,
    add column image_key varchar(255);

alter table self_introductions
    add column image_id  bigint,
    add column image_key varchar(255);

update profile_images
set image_key = case
    when locate('/', url, 9) = 0 then null
    when url like 'http://localhost%' or url like 'http://127.0.0.1%'
        then substring(url, locate('/', url, locate('/', url, 9) + 1) + 1)
    else substring(url, locate('/', url, 9) + 1)
end
where url is not null;

update self_introductions
set image_key = case
    when locate('/', image_url, 9) = 0 then null
    when image_url like 'http://localhost%' or image_url like 'http://127.0.0.1%'
        then substring(image_url, locate('/', image_url, locate('/', image_url, 9) + 1) + 1)
    else substring(image_url, locate('/', image_url, 9) + 1)
end
where image_url is not null;

insert into images (owner_type, owner_id, content_type, status, source_key, image_key, thumbnail_key, created_at,
                    updated_at)
select 'MEMBER',
       legacy.member_id,
       case lower(substring_index(legacy.image_key, '.', -1))
           when 'png' then 'PNG'
           when 'webp' then 'WEBP'
           else 'JPEG'
           end,
       'COMPLETED',
       concat('legacy/', legacy.image_key),
       legacy.image_key,
       legacy.image_key,
       now(6),
       now(6)
from (select member_id, image_key
      from profile_images
      where member_id is not null
        and image_key is not null
      union
      select member_id, image_key
      from self_introductions
      where member_id is not null
        and image_key is not null) legacy;

update profile_images pi
    join images i on i.source_key = concat('legacy/', pi.image_key) and i.owner_id = pi.member_id
set pi.image_id = i.id
where pi.image_key is not null;

update self_introductions si
    join images i on i.source_key = concat('legacy/', si.image_key) and i.owner_id = si.member_id
set si.image_id = i.id
where si.image_key is not null;
