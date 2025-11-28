-- Seed polymorphic content data
-- TextPost
insert into Content (id, content_type, title, author, views, body, wordCount) 
values(1, 'TextPost', 'My Thoughts', 'Alice', 1000, 'Quarkus is fast.', 3);

-- VideoPost
insert into Content (id, content_type, title, author, views, videoUrl, durationSeconds, bitrate, fsk) 
values(2, 'VideoPost', 'Funny Cat', 'Bob', 50000, 'http://vid.eo/cat', 60, '4k', '12');
