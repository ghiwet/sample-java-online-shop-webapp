CREATE TABLE IF NOT EXISTS `mentor` (
	`login`	TEXT NOT NULL UNIQUE,
	`class_tag`	TEXT NOT NULL
);
