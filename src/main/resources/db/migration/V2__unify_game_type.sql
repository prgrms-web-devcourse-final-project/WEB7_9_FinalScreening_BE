-- 게임 타입 값 통일: '리그 오브 레전드' -> 'LEAGUE_OF_LEGENDS'
UPDATE game_account 
SET game_type = 'LEAGUE_OF_LEGENDS' 
WHERE game_type = '리그 오브 레전드';
